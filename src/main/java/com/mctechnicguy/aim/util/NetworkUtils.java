package com.mctechnicguy.aim.util;

import com.mctechnicguy.aim.blocks.BlockAIMBase;
import com.mctechnicguy.aim.blocks.BlockNetworkCable;
import com.mctechnicguy.aim.blocks.BlockNetworkSignalBridge;
import com.mctechnicguy.aim.blocks.IAIMGenerator;
import com.mctechnicguy.aim.network.PacketHelper;
import com.mctechnicguy.aim.network.PacketOpenInfoGUI;
import com.mctechnicguy.aim.network.PacketUpdateOverlayInfo;
import com.mctechnicguy.aim.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class NetworkUtils {

	public static boolean isNetworkElement(TileEntity target) {
		return target instanceof TileEntityNetworkElement;
	}

	public static boolean isNetworkCable(Block target) {
		return target instanceof BlockNetworkCable;
	}

	public static boolean isNetworkDevice(Block target) {
		return target instanceof BlockAIMBase;
	}

	public static boolean isNetworkBridge(Block target) {
		return target instanceof BlockNetworkSignalBridge;
	}

	public static boolean isGenerator(Block target) {
		return target instanceof IAIMGenerator;
	}

	public static boolean canPlaceAIMBlock(EntityLivingBase player, @Nonnull World world, @Nonnull BlockPos pos) {
		if (!(player instanceof EntityPlayer)) return true;
		for (EnumFacing dir : EnumFacing.VALUES) {
			TileEntity te = AIMUtils.getTEAtSide(dir, world, pos);
			if (te instanceof TileEntityNetworkCable && !((TileEntityNetworkCable) te).isPlayerAccessAllowed((EntityPlayer) player)) {
				return false;
			} else if (te instanceof TileEntityAIMDevice && !((TileEntityAIMDevice) te).isPlayerAccessAllowed((EntityPlayer) player)) {
				return false;
			} else if (te instanceof TileEntityAIMCore && !((TileEntityAIMCore) te).isPlayerAccessAllowed((EntityPlayer) player)) {
				return false;
			}
		}
		return true;
	}
	
	@Nonnull
    public static EnumActionResult checkNetworkStats(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityNetworkElement) {
			if (!((TileEntityNetworkElement) te).hasCore()) {
				AIMUtils.sendChatMessage("message.noCore", player, TextFormatting.RED);
                return EnumActionResult.SUCCESS;
			} else if (((TileEntityNetworkElement) te).getCore().isPlayerAccessAllowed(player)) {
				displayInformation(player, world, ((TileEntityNetworkElement) te).getCore());
			}
            return EnumActionResult.SUCCESS;
		} else if (te instanceof TileEntityAIMCore && ((TileEntityAIMCore) te).isPlayerAccessAllowed(player)) {
			displayInformation(player, world, (TileEntityAIMCore) te);
            return EnumActionResult.SUCCESS;
		} else {
            return EnumActionResult.PASS;
        }
	}

	@Nonnull
	public static EnumActionResult updateOverlayData(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IProvidesNetworkInfo && ((IProvidesNetworkInfo)te).getTagForOverlayUpdate() != null) {
            PacketHelper.sendPacketToClient(new PacketUpdateOverlayInfo(te.getPos(), ((IProvidesNetworkInfo)te).getTagForOverlayUpdate()), (EntityPlayerMP) player);
            return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	private static void displayInformation(EntityPlayer player, @Nonnull World world, @Nonnull TileEntityAIMCore core) {
		if (!world.isRemote) {
			PacketHelper.sendPacketToClient(new PacketOpenInfoGUI(core), (EntityPlayerMP) player);
		}
	}


	
	
}
