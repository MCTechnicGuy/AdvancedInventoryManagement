package com.mctechnicguy.aim.network;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.GuiNetworkInfo;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkElement;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class PacketNetworkInfo implements IMessage {

	public int power;
    public int powerDrain;
    public int maxPower;
    public String playerConnectedName;
    public BlockPos corePos;
    public int coreDim;
    public short problems;
    public boolean isLoaded;
    public boolean isActive;
    public boolean isSecured;
    public HashMap<Integer, Integer> connectedDevices;
    public int numberElementsConnected;

	public PacketNetworkInfo() {

	}

	public PacketNetworkInfo(TileEntityAIMCore core, BlockPos corePos, int coreDim) {
	    this.corePos = corePos;
	    this.coreDim = coreDim;
	    if (core == null) {
	        this.isLoaded = false;
        } else {
	        this.isLoaded = true;
	        this.power = core.Power;
	        this.powerDrain = core.getNetworkPowerDrain();
	        this.maxPower = core.MaxPower();
	        this.playerConnectedName = core.playerConnectedName == null ? "" : core.playerConnectedName;
	        this.problems = core.getProblemFlag();
            this.isActive = core.isActive();
            this.isSecured = core.hasUpgrade(0) && core.playerConnectedName != null && !core.playerConnectedName.isEmpty();
            this.connectedDevices = new HashMap<>();
            for (TileEntityNetworkElement element : core.registeredDevices) {
                int blockId = Block.getIdFromBlock(element.getBlockType());
                if (this.connectedDevices.containsKey(blockId)) {
                    this.connectedDevices.put(blockId, this.connectedDevices.get(blockId) + 1);
                } else {
                    this.connectedDevices.put(blockId, 1);
                }
            }
        }
	}

	@Override
	public void fromBytes(@Nonnull ByteBuf buf) {
	    PacketBuffer pf = new PacketBuffer(buf);
	    this.isLoaded = pf.readBoolean();
        corePos = pf.readBlockPos();
        coreDim = pf.readInt();
	    if (isLoaded) {
            power = pf.readInt();
            powerDrain = pf.readInt();
            maxPower = pf.readInt();
            playerConnectedName = pf.readString(20);
            if (playerConnectedName.isEmpty()) playerConnectedName = null;
            problems = pf.readShort();
            isActive = pf.readBoolean();
            isSecured = pf.readBoolean();
            int stackCount = pf.readInt();
            this.connectedDevices = new HashMap<>();
            numberElementsConnected = 0;
            for (int i = 0; i < stackCount; i++) {
                int id = pf.readInt();
                int count = pf.readInt();
                this.connectedDevices.put(id, count);
                this.numberElementsConnected += count;
            }
        }
	}

	@Override
	public void toBytes(@Nonnull ByteBuf buf) {
        PacketBuffer pf = new PacketBuffer(buf);
        pf.writeBoolean(isLoaded);
        pf.writeBlockPos(corePos);
        pf.writeInt(coreDim);
        if (isLoaded) {
            pf.writeInt(power);
            pf.writeInt(powerDrain);
            pf.writeInt(maxPower);
            pf.writeString(playerConnectedName);
            pf.writeShort(problems);
            pf.writeBoolean(isActive);
            pf.writeBoolean(isSecured);
            pf.writeInt(connectedDevices.size());
            for (int blockId : connectedDevices.keySet()) {
                pf.writeInt(blockId);
                pf.writeInt(connectedDevices.get(blockId));
            }
        }
	}

	@SideOnly(Side.CLIENT)
	public static class PacketNetworkInfoHandler implements IMessageHandler<PacketNetworkInfo, IMessage> {

		@Nullable
        @Override
		public IMessage onMessage(@Nonnull final PacketNetworkInfo message, @Nonnull final MessageContext ctx) {
			AdvancedInventoryManagement.proxy.addScheduledTask(() -> processMessage(message, ctx), ctx);
			return null;
		}
		
		void processMessage(@Nonnull PacketNetworkInfo message, MessageContext ctx) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiNetworkInfo(message));
		}

	}

}
