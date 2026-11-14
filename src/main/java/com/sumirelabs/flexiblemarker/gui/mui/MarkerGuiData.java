package com.sumirelabs.flexiblemarker.gui.mui;

import com.cleanroommc.modularui.factory.PosGuiData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class MarkerGuiData extends PosGuiData {

    public final int id;
    private final World world;
    public MarkerGuiData(EntityPlayer player, BlockPos pos, int id) {
        super(player, pos);
        this.id = id;
        this.world = player.world;
    }
    @Override
    public World getWorld() {
        return world;
    }
    @Override
    public TileEntity getTileEntity() {
        // Avoid PosGuiData's shared mutable position on integrated client/server threads.
        return getWorld().getTileEntity(getBlockPos());
    }

}
