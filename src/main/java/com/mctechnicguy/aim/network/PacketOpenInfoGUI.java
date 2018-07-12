package com.mctechnicguy.aim.network;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import com.mctechnicguy.aim.util.AIMUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketOpenInfoGUI implements IMessage {

	private int Power;
	private BlockPos pos;
	private boolean playerAccessible;
	
	
	public PacketOpenInfoGUI() {
		
	}
	
	public PacketOpenInfoGUI(@Nonnull TileEntityAIMCore core) {
		this.pos = core.getPos();
		this.Power = core.Power;
		this.playerAccessible = AIMUtils.isPlayerAccessible(core.getConnectedPlayer());
	}

	@Override
	public void fromBytes(@Nonnull ByteBuf buf) {
	    PacketBuffer pf = new PacketBuffer(buf);
		pos = pf.readBlockPos();
		Power = pf.readInt();
		playerAccessible = pf.readBoolean();
	}

	@Override
	public void toBytes(@Nonnull ByteBuf buf) {
        PacketBuffer pf = new PacketBuffer(buf);
		pf.writeBlockPos(pos);
        pf.writeInt(Power);
        pf.writeBoolean(playerAccessible);
	}

    @SideOnly(Side.CLIENT)
	public static class PacketOpenInfoGUIHandler implements IMessageHandler<PacketOpenInfoGUI, IMessage> {

		@Nullable
        @Override
		public IMessage onMessage(@Nonnull final PacketOpenInfoGUI message, @Nonnull final MessageContext ctx) {
			AdvancedInventoryManagement.proxy.addScheduledTask(() -> processMessage(message, ctx), ctx);
			return null;
		}
		
		void processMessage(@Nonnull PacketOpenInfoGUI message, MessageContext ctx) {
			TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
			if (!(te instanceof TileEntityAIMCore)) return;
			((TileEntityAIMCore)te).searchForDevicesInNetwork();
			((TileEntityAIMCore)te).Power = message.Power;
			((TileEntityAIMCore)te).playerAccessible = message.playerAccessible;
		}

	}

}
