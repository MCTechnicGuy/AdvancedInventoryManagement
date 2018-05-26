package com.mctechnicguy.aim.network;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketHotbarSlotChanged implements IMessage {

	private short newSlot;


	public PacketHotbarSlotChanged() {

	}

	public PacketHotbarSlotChanged(short nSlot) {
		newSlot = nSlot;
	}

	@Override
	public void fromBytes(@Nonnull ByteBuf buf) {
		newSlot = buf.readShort();
	}

	@Override
	public void toBytes(@Nonnull ByteBuf buf) {
		buf.writeShort(newSlot);
	}

	public static class PacketHotbarSlotChangedHandler implements IMessageHandler<PacketHotbarSlotChanged, IMessage> {

		@Nullable
        @Override
		public IMessage onMessage(@Nonnull final PacketHotbarSlotChanged message, @Nonnull final MessageContext ctx) {
			AdvancedInventoryManagement.proxy.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					processMessage(message, ctx);
				}
				
			}, ctx);
			return null;
		}
		
		public void processMessage(@Nonnull PacketHotbarSlotChanged message, MessageContext ctx) {
			Minecraft.getMinecraft().player.inventory.currentItem = message.newSlot;
		}

	}

}
