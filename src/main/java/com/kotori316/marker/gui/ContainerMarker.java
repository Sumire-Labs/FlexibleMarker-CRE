package com.kotori316.marker.gui;

import javax.annotation.Nullable;

import com.kotori316.marker.Tile16Marker;
import com.kotori316.marker.TileFlexMarker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ContainerMarker extends Container {

    private final EntityPlayer owner;
    private final TileEntity marker;

    public ContainerMarker(EntityPlayer player) {
        this(player, null);
    }

    public ContainerMarker(EntityPlayer player, @Nullable TileEntity marker) {
        this.owner = player;
        this.marker = marker;
        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlotToContainer(new Slot(player.inventory, v + h * 9 + 9, 29 + v * oneBox, 107 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlotToContainer(new Slot(player.inventory, vertical, 29 + vertical * oneBox, 165));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn == owner && playerIn.isEntityAlive() && marker != null && !marker.isInvalid()
            && playerIn.world == marker.getWorld() && playerIn.world.isBlockLoaded(marker.getPos())
            && playerIn.getDistanceSqToCenter(marker.getPos()) <= 64
            && playerIn.world.getTileEntity(marker.getPos()) == marker;
    }

    public boolean isOpenFor(EntityPlayer player, int id, BlockPos pos) {
        return canInteractWith(player) && marker.getPos().equals(pos)
            && ((id == GuiHandler.Marker_ID && marker instanceof TileFlexMarker)
                || (id == GuiHandler.Marker16_ID && marker instanceof Tile16Marker));
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return ItemStack.EMPTY;
    }

}
