package com.mctechnicguy.aim.network;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import com.mctechnicguy.aim.util.AIMUtils;
import com.mctechnicguy.aim.util.NBTUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketRequestServerInfo implements IMessage {

    private short requestedPacketId;
    private BlockPos corePos;
    private int coreDim;

    public PacketRequestServerInfo() {

    }

    public PacketRequestServerInfo(short requestedPacketId) {
        this.requestedPacketId = requestedPacketId;
    }

    public PacketRequestServerInfo(short requestedPacketId, BlockPos corePos, int coreDim) {
        if (requestedPacketId != 6) {
            throw new IllegalArgumentException("Only a PacketOpenInfoGUI can be adapted to a given cores position!");
        }
        this.requestedPacketId = requestedPacketId;
        this.corePos = corePos;
        this.coreDim = coreDim;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pf = new PacketBuffer(buf);
        this.requestedPacketId = pf.readShort();
        if (this.requestedPacketId == 6) {
            this.corePos = pf.readBlockPos();
            this.coreDim = pf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pf = new PacketBuffer(buf);
        pf.writeShort(this.requestedPacketId);
        if (this.requestedPacketId == 6) {
            pf.writeBlockPos(corePos);
            pf.writeInt(coreDim);
        }
    }

    public static class PacketRequestServerInfoHandler implements IMessageHandler<PacketRequestServerInfo, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull final PacketRequestServerInfo message, @Nonnull final MessageContext ctx) {
            AdvancedInventoryManagement.proxy.addScheduledTask(() -> processMessage(message, ctx), ctx);
            return null;
        }

        void processMessage(@Nonnull PacketRequestServerInfo message, MessageContext ctx) {
            switch (message.requestedPacketId) {
                case 4: { //NetworkCoreList
                    NBTTagList list = NBTUtils.readPlayerInfo(ctx.getServerHandler().player.getUniqueID(), ctx.getServerHandler().player.world);
                    if (list != null) {
                        PacketHelper.sendPacketToClient(new PacketNetworkCoreList(list, AIMUtils.isPlayerAccessible(ctx.getServerHandler().player)), ctx.getServerHandler().player);
                    }
                    break;
                }
                case 6: { //PacketNetworkInfo
                    World world = DimensionManager.getWorld(message.coreDim);
                    if (world == null || !world.isBlockLoaded(message.corePos)) {
                        PacketHelper.sendPacketToClient(new PacketNetworkInfo(null, message.corePos, message.coreDim), ctx.getServerHandler().player);
                    } else {
                        PacketHelper.sendPacketToClient(new PacketNetworkInfo((TileEntityAIMCore)world.getTileEntity(message.corePos), message.corePos, message.coreDim), ctx.getServerHandler().player);
                    }
                    break;
                }
                default: break;
            }
        }
    }

}