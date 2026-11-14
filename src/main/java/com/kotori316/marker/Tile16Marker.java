package com.kotori316.marker;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.kotori316.marker.render.Box;
import com.yogpc.qp.tile.IMarker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.api.tiles.TilesAPI;

import static com.kotori316.marker.TileFlexMarker.BC_TILE_ID;

@net.minecraftforge.fml.common.Optional.Interface(modid = TileFlexMarker.BC_TILE_ID, iface = "buildcraft.api.tiles.ITileAreaProvider")
@net.minecraftforge.fml.common.Optional.Interface(modid = TileFlexMarker.BC_TILE_ID, iface = "buildcraft.api.tiles.IDebuggable")
@net.minecraftforge.fml.common.Optional.Interface(modid = "quarryplus", iface = "com.yogpc.qp.tile.IMarker")
public class Tile16Marker extends TileEntity implements ITileAreaProvider, IDebuggable, IMarker, IAreaConfigurable {

    @Nullable private BlockPos min;
    @Nullable private BlockPos max;
    @Nullable public Box[] boxes;
    private boolean bcLoaded = Loader.isModLoaded(BC_TILE_ID);
    private EnumFacing.AxisDirection xDirection = EnumFacing.AxisDirection.NEGATIVE,
        zDirection = EnumFacing.AxisDirection.POSITIVE;
    private int size = 16;

    public void init(EnumFacing.AxisDirection xDirection, EnumFacing.AxisDirection zDirection) {
        this.xDirection = Objects.requireNonNull(xDirection);
        this.zDirection = Objects.requireNonNull(zDirection);
        changeSize(this.size);
    }

    public void changeSize(int size) {
        int y = getPos().getY();
        changeSize(size, y, y);
    }

    public void changeSize(int size, int yMax, int yMin) {
        if (!canChangeSize(size, yMax, yMin))
            return;
        this.size = size;
        BlockPos edge1 = getPos().add(xDirection.getOffset() * (size + 1), 0, zDirection.getOffset() * (size + 1));
        BlockPos edge2 = getPos();
        min = new BlockPos(Math.min(edge1.getX(), edge2.getX()), yMin, Math.min(edge1.getZ(), edge2.getZ()));
        max = new BlockPos(Math.max(edge1.getX(), edge2.getX()), yMax, Math.max(edge1.getZ(), edge2.getZ()));
        setRender();
    }

    public boolean canChangeSize(int size, int yMax, int yMin) {
        return validSize(getPos(), xDirection, zDirection, size, yMax, yMin,
            world == null ? 256 : world.getHeight());
    }

    static boolean validSize(BlockPos origin, EnumFacing.AxisDirection xDirection,
        EnumFacing.AxisDirection zDirection, int size, int yMax, int yMin, int height) {
        if (size < 16 || size % 16 != 0 || yMin < 0 || yMax < yMin || yMax >= height)
            return false;
        long distance = (long) size + 1;
        long x = origin.getX() + xDirection.getOffset() * distance;
        long z = origin.getZ() + zDirection.getOffset() * distance;
        // Minecraft's horizontal build limit; also keeps packed BlockPos NBT/network values lossless.
        return x >= -30_000_000 && x < 30_000_000 && z >= -30_000_000 && z < 30_000_000;
    }

    private void setRender() {
        if (world == null || !world.isRemote)
            return;
        boxes = IAreaConfigurable.Util.getRenderBox(min(), max());
    }

    @SideOnly(Side.CLIENT)
    public int getSize() {
        return getConfiguredSize();
    }

    /** Common-side accessor for server-authoritative UI synchronization. */
    public int getConfiguredSize() {
        return size;
    }

    // TileEntity overrides
    @Override
    public NBTTagCompound getUpdateTag() {
        return super.serializeNBT();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && !world.isRemote && world.isBlockLoaded(pos) && world.getTileEntity(pos) == this) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 2);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 128 * 128 * 2;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        min = compound.hasKey("min", 4) ? BlockPos.fromLong(compound.getLong("min")) : getPos();
        max = compound.hasKey("max", 4) ? BlockPos.fromLong(compound.getLong("max")) : getPos();
        xDirection = compound.getBoolean("x") ? EnumFacing.AxisDirection.POSITIVE : EnumFacing.AxisDirection.NEGATIVE;
        zDirection = !compound.hasKey("z", 99) || compound.getBoolean("z")
            ? EnumFacing.AxisDirection.POSITIVE
            : EnumFacing.AxisDirection.NEGATIVE;
        size = compound.hasKey("size", 99) ? compound.getInteger("size") : 16;
        // Preserve saved geometry; validate new edits rather than silently replacing an existing area on load.
        if (size < 16 || size % 16 != 0) {
            size = 16;
        }
        if (hasWorld()) {
            setRender();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("min", min().toLong());
        compound.setLong("max", max().toLong());
        compound.setBoolean("x", xDirection == EnumFacing.AxisDirection.POSITIVE);
        compound.setBoolean("z", zDirection == EnumFacing.AxisDirection.POSITIVE);
        compound.setInteger("size", size);
        return super.writeToNBT(compound);
    }

    // Interface implementations
    @Override
    public boolean hasLink() {
        return IAreaConfigurable.isUsableArea(min(), max());
    }

    @Override
    public BlockPos min() {
        return min == null ? getPos() : min;
    }

    @Override
    public BlockPos max() {
        return max == null ? getPos() : max;
    }

    @Override
    public List<ItemStack> removeFromWorldWithItem() {
        NonNullList<ItemStack> list = NonNullList.create();
        Marker.block16Marker.getDrops(list, getWorld(), getPos(), getWorld().getBlockState(getPos()), 0);
        getWorld().setBlockToAir(getPos());
        return list;
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = TileFlexMarker.BC_CORE_ID)
    public boolean isValidFromLocation(BlockPos pos) {
        return false;
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = TileFlexMarker.BC_CORE_ID)
    public void removeFromWorld() {
        if (!getWorld().isRemote) {
            getWorld().destroyBlock(getPos(), true);
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        String[] strings = {
            "Pos: x=" + pos.getX() + " y=" + pos.getY() + " z=" + pos.getZ(),
            "Facing: " + (xDirection == null ? "Unknown" : EnumFacing.getFacingFromAxis(xDirection, EnumFacing.Axis.X))
                +
                ", " + (zDirection == null ? "Unknown" : EnumFacing.getFacingFromAxis(zDirection, EnumFacing.Axis.Z)),
            "Min: x=" + min().getX() + " y=" + min().getY() + " z=" + min().getZ(),
            "Max: x=" + max().getX() + " y=" + max().getY() + " z=" + max().getZ(),
        };
        left.addAll(Arrays.asList(strings));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (bcLoaded) {
            if (capability == TilesAPI.CAP_TILE_AREA_PROVIDER) {
                return true;
            }
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (bcLoaded) {
            if (capability == TilesAPI.CAP_TILE_AREA_PROVIDER) {
                return TilesAPI.CAP_TILE_AREA_PROVIDER.cast(this);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public Runnable setMinMax(BlockPos min, BlockPos max) {
        return () -> {
            this.max = max;
            this.min = min;
            setRender();
            size = (max.getX() - min.getX() - 1);
        };
    }

}
