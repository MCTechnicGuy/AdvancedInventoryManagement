package com.mctechnicguy.aim.network;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.IProvidesNetworkInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketUpdateOverlayInfo implements IMessage{

    private BlockPos pos;
    private NBTTagCompound data;


    public PacketUpdateOverlayInfo() {

    }

    public PacketUpdateOverlayInfo(BlockPos tileEntityPos, NBTTagCompound data) {
        this.pos = tileEntityPos;
        this.data = data;
    }

    @Override
    public void fromBytes(@Nonnull ByteBuf buf) {
        PacketBuffer pf = new PacketBuffer(buf);
        pos = pf.readBlockPos();
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(@Nonnull ByteBuf buf) {
        PacketBuffer bf = new PacketBuffer(buf);
        bf.writeBlockPos(pos);
        ByteBufUtils.writeTag(buf, data);
    }

    public static class PacketUpdateOverlayInfoHandler implements IMessageHandler<PacketUpdateOverlayInfo, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull final PacketUpdateOverlayInfo message, @Nonnull final MessageContext ctx) {
            AdvancedInventoryManagement.proxy.addScheduledTask(() -> processMessage(message, ctx), ctx);
            return null;
        }

        void processMessage(@Nonnull PacketUpdateOverlayInfo message, MessageContext ctx) {
            TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
            if (!(te instanceof IProvidesNetworkInfo)) return;
            ((IProvidesNetworkInfo)te).handleTagForOverlayUpdate(message.data);
        }

    }
}
