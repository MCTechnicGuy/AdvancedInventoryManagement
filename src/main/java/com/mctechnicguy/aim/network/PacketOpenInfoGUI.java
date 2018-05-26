package com.mctechnicguy.aim.network;

import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.util.AIMUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketOpenInfoGUI implements IMessage {

	private int x, y, z, Power;
	private boolean playerAccessible;
	
	
	public PacketOpenInfoGUI() {
		
	}
	
	public PacketOpenInfoGUI(@Nonnull TileEntityAIMCore core) {
		this.x = core.getPos().getX();
		this.y = core.getPos().getY();
		this.z = core.getPos().getZ();
		this.Power = core.Power;
		this.playerAccessible = AIMUtils.isPlayerAccessible(core.getConnectedPlayer());
	}

	@Override
	public void fromBytes(@Nonnull ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		Power = buf.readInt();
		playerAccessible = buf.readBoolean();
	}

	@Override
	public void toBytes(@Nonnull ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(Power);
		buf.writeBoolean(playerAccessible);
	}

	public static class PacketOpenInfoGUIHandler implements IMessageHandler<PacketOpenInfoGUI, IMessage> {

		@Nullable
        @Override
		public IMessage onMessage(@Nonnull final PacketOpenInfoGUI message, @Nonnull final MessageContext ctx) {
			AdvancedInventoryManagement.proxy.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					processMessage(message, ctx);
				}
				
			}, ctx);
			return null;
		}
		
		public void processMessage(@Nonnull PacketOpenInfoGUI message, MessageContext ctx) {
			TileEntity te = Minecraft.getMinecraft().world.getTileEntity(new BlockPos(message.x, message.y, message.z));
			if (!(te instanceof TileEntityAIMCore)) return;
			((TileEntityAIMCore)te).searchForDevicesInNetwork();
			((TileEntityAIMCore)te).Power = message.Power;
			((TileEntityAIMCore)te).playerAccessible = message.playerAccessible;
			FMLNetworkHandler.openGui(Minecraft.getMinecraft().player, AdvancedInventoryManagement.instance, AdvancedInventoryManagement.guiIDNetworkInfo,
					Minecraft.getMinecraft().world, message.x, message.y, message.z);
		}

	}

}
