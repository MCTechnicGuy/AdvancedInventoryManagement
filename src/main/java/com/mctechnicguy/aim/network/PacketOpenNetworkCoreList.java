package com.mctechnicguy.aim.network;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.GuiAdvancedNetworkInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketOpenNetworkCoreList implements IMessage {

	private NBTTagCompound cores;
	private boolean playerAccessible;

	public PacketOpenNetworkCoreList() {

	}

	public PacketOpenNetworkCoreList(@Nonnull NBTTagList tagList, boolean playerAccessible) {
		this.cores = new NBTTagCompound();
		cores.setTag("list", tagList);
		this.playerAccessible = playerAccessible;
	}

	@Override
	public void fromBytes(@Nonnull ByteBuf buf) {
        this.cores = ByteBufUtils.readTag(buf);
        this.playerAccessible = buf.readBoolean();
	}

	@Override
	public void toBytes(@Nonnull ByteBuf buf) {
        ByteBufUtils.writeTag(buf, cores);
        buf.writeBoolean(playerAccessible);
	}

	@SideOnly(Side.CLIENT)
	public static class PacketOpenNetworkCoreListHandler implements IMessageHandler<PacketOpenNetworkCoreList, IMessage> {

		@Nullable
        @Override
		public IMessage onMessage(@Nonnull final PacketOpenNetworkCoreList message, @Nonnull final MessageContext ctx) {
			AdvancedInventoryManagement.proxy.addScheduledTask(() -> processMessage(message, ctx), ctx);
			return null;
		}
		
		void processMessage(@Nonnull PacketOpenNetworkCoreList message, MessageContext ctx) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiAdvancedNetworkInfo(message.cores.getTagList("list", 10), message.playerAccessible));
		}

	}

}
