"""Export losslessly compressed PNGs from the textured Minecraft render."""
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
SOURCE = Path(__file__).resolve().parent / 'flexible-marker-minecraft-transparent.png'
DEST = ROOT / 'docs/images'
DEST.mkdir(parents=True, exist_ok=True)
icon = Image.open(SOURCE).convert('RGBA')
files = []


def save(image, path):
    image.save(path, format='PNG', optimize=True, compress_level=9)
    with Image.open(path) as check:
        check.load()
        assert check.size == image.size
    files.append({'path': path.relative_to(ROOT).as_posix(),
                  'width': image.width, 'height': image.height,
                  'bytes': path.stat().st_size})


save(icon.resize((128, 128), Image.Resampling.LANCZOS), ROOT / 'src/main/resources/logo.png')
for platform in ['curseforge', 'modrinth']:
    square = Image.new('RGBA', (512, 512), (35, 39, 47, 255))
    square.alpha_composite(icon.resize(square.size, Image.Resampling.LANCZOS))
    save(square.convert('RGB'), DEST / f'{platform}-icon.png')

# Keep text and subject well inside the edges for social sharing crops.
banner = Image.new('RGB', (1280, 640), (35, 39, 47))
draw = ImageDraw.Draw(banner)
bold = 'C:/Windows/Fonts/segoeuib.ttf'
regular = 'C:/Windows/Fonts/segoeui.ttf'
draw.rounded_rectangle((80, 133, 268, 173), radius=8, fill=(54, 68, 56))
draw.text((94, 138), 'CLEANROOM EDITION', font=ImageFont.truetype(bold, 16), fill=(154, 224, 150))
draw.text((76, 193), 'Flexible', font=ImageFont.truetype(bold, 82), fill=(244, 247, 250))
draw.text((76, 290), 'Marker CRE', font=ImageFont.truetype(bold, 82), fill=(244, 247, 250))
draw.rectangle((80, 420, 154, 425), fill=(81, 204, 56))
draw.text((80, 452), 'Configure your working area.', font=ImageFont.truetype(regular, 26), fill=(185, 194, 205))
draw.text((80, 495), 'Minecraft 1.12.2', font=ImageFont.truetype(regular, 22), fill=(143, 154, 170))
subject = icon.resize((620, 620), Image.Resampling.LANCZOS)
banner.paste(subject, (650, 10), subject)
save(banner, DEST / 'github-social-preview.png')
assert (DEST / 'modrinth-icon.png').stat().st_size < 256 * 1024
assert (DEST / 'github-social-preview.png').stat().st_size < 1024 * 1024
(DEST / 'image-manifest.json').write_text(json.dumps(files, indent=2) + '\n')
print(json.dumps(files, indent=2))
