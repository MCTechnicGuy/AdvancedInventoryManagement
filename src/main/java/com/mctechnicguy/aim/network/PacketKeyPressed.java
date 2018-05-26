package com.mctechnicguy.aim.network;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.util.AIMUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketKeyPressed implements IMessage {

	public PacketKeyPressed() {
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}
	
	public static class PacketKeyPressedHandler implements IMessageHandler<PacketKeyPressed, IMessage> {

		@Nullable
        @Override
		public IMessage onMessage(final PacketKeyPressed message, @Nonnull final MessageContext ctx) {
			AdvancedInventoryManagement.proxy.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					processMessage(message, ctx);
				}
				
			}, ctx);
			return null;
		}
		
		public void processMessage(PacketKeyPressed message, @Nonnull MessageContext ctx) {
			EntityPlayer player = AdvancedInventoryManagement.proxy.getPlayer(ctx);
			if (player == null) return;
			if (!player.hasCapability(AdvancedInventoryManagement.PLAYER_ACCESS_CAP, null)) return;
			boolean CurrentState = player.getCapability(AdvancedInventoryManagement.PLAYER_ACCESS_CAP, null).isAccessible();
			player.getCapability(AdvancedInventoryManagement.PLAYER_ACCESS_CAP, null).setAccessible(!CurrentState);
			if (!CurrentState) {
				AIMUtils.sendChatMessage("message.changedAccessibility.true", player, TextFormatting.GREEN);
			} else {
				AIMUtils.sendChatMessage("message.changedAccessibility.false", player, TextFormatting.RED);
			}
		}
		
	}
		
		
}
	
