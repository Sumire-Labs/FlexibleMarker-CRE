"""Render the repository's Minecraft cuboids and face UVs without image generation.

Requires numpy and Pillow. Run this script from any working directory.
"""
import json
from pathlib import Path

import numpy as np
from PIL import Image


ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parent
ASSETS = ROOT / "src/main/resources/assets/flexiblemarker"
MODEL = ASSETS / "models/block/markerplus_post.json"
TEXTURE = ASSETS / "textures/blocks/marker.png"


def render(size=1536):
    elements = json.loads(MODEL.read_text())["elements"]
    texture = np.asarray(Image.open(TEXTURE).convert("RGBA"))
    # Orthographic camera: 45 degrees around the block, 30 degrees above it.
    elevation = np.radians(30)
    toward_camera = np.array([
        np.cos(elevation) / np.sqrt(2), np.sin(elevation),
        np.cos(elevation) / np.sqrt(2),
    ])
    right = np.array([1, 0, -1]) / np.sqrt(2)
    up = np.cross(toward_camera, right)
    direction = -toward_camera
    center = np.array([8, 5, 8], dtype=float)
    vertices = np.array([
        [x, y, z] for element in elements
        for x in [element["from"][0], element["to"][0]]
        for y in [element["from"][1], element["to"][1]]
        for z in [element["from"][2], element["to"][2]]
    ]) - center
    vertical = vertices @ up
    center += up * ((vertical.min() + vertical.max()) / 2)
    span = (vertical.max() - vertical.min()) / 0.82
    coords = ((np.arange(size, dtype=np.float32) + 0.5) / size - 0.5) * span
    xx, yy = np.meshgrid(coords, -coords)
    origins = (center + toward_camera * 30
               + xx[..., None] * right + yy[..., None] * up).astype(np.float32)
    nearest = np.full((size, size), np.inf, dtype=np.float32)
    result = np.zeros((size, size, 4), dtype=np.uint8)

    for element in elements:
        lo = np.array(element["from"], dtype=np.float32)
        hi = np.array(element["to"], dtype=np.float32)
        ta = (lo - origins) / direction
        tb = (hi - origins) / direction
        entry = np.minimum(ta, tb)
        exit_ = np.maximum(ta, tb)
        near = entry.max(axis=2)
        far = exit_.min(axis=2)
        axis = entry.argmax(axis=2)
        visible = (near <= far) & (near >= 0) & (near < nearest)
        # Camera sees the positive X, Y and Z faces (east, up, south).
        for axis_index, face, shade in [(0, "east", 0.60), (1, "up", 1.0), (2, "south", 0.80)]:
            mask = visible & (axis == axis_index)
            if not mask.any():
                continue
            hit = origins[mask] + near[mask, None] * direction
            relative = (hit - lo) / (hi - lo)
            if face == "east":
                u, v = 1 - relative[:, 2], 1 - relative[:, 1]
            elif face == "south":
                u, v = relative[:, 0], 1 - relative[:, 1]
            else:
                u, v = relative[:, 0], relative[:, 2]
            u0, v0, u1, v1 = element["faces"][face]["uv"]
            tx = np.clip(np.floor((u0 + u * (u1-u0)) * texture.shape[1] / 16).astype(int), 0, texture.shape[1]-1)
            ty = np.clip(np.floor((v0 + v * (v1-v0)) * texture.shape[0] / 16).astype(int), 0, texture.shape[0]-1)
            pixels = texture[ty, tx].copy()
            pixels[:, :3] = np.rint(pixels[:, :3] * shade).astype(np.uint8)
            result[mask] = pixels
        nearest[visible] = near[visible]
    return Image.fromarray(result)


if __name__ == "__main__":
    icon = render().resize((1024, 1024), Image.Resampling.LANCZOS)
    icon.save(OUT / "flexible-marker-minecraft-transparent.png")
    background = Image.new("RGBA", icon.size, (35, 39, 47, 255))
    background.alpha_composite(icon)
    background.convert("RGB").save(OUT / "flexible-marker-minecraft-icon.png")
    icon.resize((256, 256), Image.Resampling.LANCZOS).save(OUT / "flexible-marker-minecraft-256.png")
    print("Rendered original model and texture: 1024px background / transparent; 256px transparent")
