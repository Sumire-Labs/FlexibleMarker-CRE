package com.kotori316.marker.packet;

import com.kotori316.marker.IAreaConfigurable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.ByteBuf;

/**
 * To Client Only
 */
public class AreaMessage implements IMessage {

    private BlockPos pos;
    private int dim;
    private BlockPos min, max;

    @SuppressWarnings("unused")
    public AreaMessage() {}

    public AreaMessage(BlockPos pos, int dim, BlockPos min, BlockPos max) {
        this.pos = pos;
        this.dim = dim;
        this.min = min;
        this.max = max;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer p = new PacketBuffer(buf);
        pos = p.readBlockPos();
        dim = p.readInt();
        min = p.readBlockPos();
        max = p.readBlockPos();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer p = new PacketBuffer(buf);
        p.writeBlockPos(pos).writeInt(dim);
        p.writeBlockPos(min).writeBlockPos(max);
    }

    public static IMessage onReceive(AreaMessage message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT)
            applyOnClient(message);
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static void applyOnClient(AreaMessage message) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null || world.provider.getDimension() != message.dim || !world.isBlockLoaded(message.pos))
                return;
            TileEntity entity = world.getTileEntity(message.pos);
            if (entity instanceof IAreaConfigurable marker)
                marker.setMinMax(message.min, message.max).run();
        });
    }

}
