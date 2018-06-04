package com.mctechnicguy.aim.util;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.blocks.BlockAIMBase;
import com.mctechnicguy.aim.blocks.BlockNetworkCable;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkCable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class AIMUtils {

	@Nullable
    public static EntityPlayerMP getPlayerbyID(@Nullable UUID id) {
		if (id == null)
			return null;
		PlayerList playersOnline = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
		return playersOnline.getPlayerByUUID(id);
	}

	public static boolean isPlayerAccessible(@Nullable EntityPlayer player) {
		if (player == null)
			return false;
		if (!player.hasCapability(AdvancedInventoryManagement.PLAYER_ACCESS_CAP, null))
			return false;
		return player.getCapability(AdvancedInventoryManagement.PLAYER_ACCESS_CAP, null).isAccessible();
	}	

	public static boolean checkForAIMBlockAtSide(@Nonnull EnumFacing dir, @Nullable World w, @Nonnull BlockPos pos) {
		if (w == null)
			return false;
		if (dir.equals(EnumFacing.UP)
				&& ((w.getBlockState(pos.up()).getBlock() instanceof BlockNetworkCable && w.getTileEntity(pos.up()) != null
						&& !((TileEntityNetworkCable) w.getTileEntity(pos.up()))
								.isConnectionBlocked(dir.getOpposite()))
				|| w.getBlockState(pos.up()).getBlock() instanceof BlockAIMBase))
			return true;

		else if (dir.equals(EnumFacing.DOWN)
				&& ((w.getBlockState(pos.down()).getBlock() instanceof BlockNetworkCable && w.getTileEntity(pos.down()) != null
						&& !((TileEntityNetworkCable) w.getTileEntity(pos.down()))
								.isConnectionBlocked(dir.getOpposite()))
				|| w.getBlockState(pos.down()).getBlock() instanceof BlockAIMBase))
			return true;

		else if (dir.equals(EnumFacing.WEST)
				&& ((w.getBlockState(pos.west()).getBlock() instanceof BlockNetworkCable && w.getTileEntity(pos.west()) != null
						&& !((TileEntityNetworkCable) w.getTileEntity(pos.west()))
								.isConnectionBlocked(dir.getOpposite()))
				|| w.getBlockState(pos.west()).getBlock() instanceof BlockAIMBase))
			return true;

		else if (dir.equals(EnumFacing.EAST)
				&& ((w.getBlockState(pos.east()).getBlock() instanceof BlockNetworkCable && w.getTileEntity(pos.east()) != null
						&& !((TileEntityNetworkCable) w.getTileEntity(pos.east()))
								.isConnectionBlocked(dir.getOpposite()))
				|| w.getBlockState(pos.east()).getBlock() instanceof BlockAIMBase))
			return true;

		else if (dir.equals(EnumFacing.NORTH)
				&& ((w.getBlockState(pos.north()).getBlock() instanceof BlockNetworkCable && w.getTileEntity(pos.north()) != null
						&& !((TileEntityNetworkCable) w.getTileEntity(pos.north()))
								.isConnectionBlocked(dir.getOpposite()))
				|| w.getBlockState(pos.north()).getBlock() instanceof BlockAIMBase))
			return true;

		else if (dir.equals(EnumFacing.SOUTH)
				&& ((w.getBlockState(pos.south()).getBlock() instanceof BlockNetworkCable && w.getTileEntity(pos.south()) != null
						&& !((TileEntityNetworkCable) w.getTileEntity(pos.south()))
								.isConnectionBlocked(dir.getOpposite()))
				|| w.getBlockState(pos.south()).getBlock() instanceof BlockAIMBase))
			return true;

		else
			return false;
	}

	public static Block getBlockAtSide(@Nonnull EnumFacing dir, @Nonnull World world, @Nonnull BlockPos pos) {
		return world.getBlockState(pos.offset(dir)).getBlock();
	}

	@Nullable
    public static TileEntity getTEAtSide(@Nonnull EnumFacing dir, @Nonnull World world, @Nonnull BlockPos pos) {
		return world.getTileEntity(pos.offset(dir));
	}

	public static void sendChatMessage(@Nonnull String message, @Nonnull EntityPlayer player, @Nonnull TextFormatting color) {
		TextComponentTranslation cmp = new TextComponentTranslation(message);
		cmp.getStyle().setColor(color);
		player.sendMessage(cmp);
	}

	public static void sendChatMessageWithArgs(@Nonnull String message, @Nonnull EntityPlayer player, @Nonnull TextFormatting color,
                                               Object... args) {
		TextComponentTranslation cmp = new TextComponentTranslation(message, args);
		cmp.getStyle().setColor(color);
		player.sendMessage(cmp);
	}

	public static EnumFacing get2PDirection(@Nonnull BlockPos examinedBlock, @Nonnull BlockPos target) {
		if (target.getY() > examinedBlock.getY())
			return EnumFacing.UP;
		else if (target.getY() < examinedBlock.getY())
			return EnumFacing.DOWN;
		else if (target.getX() > examinedBlock.getX())
			return EnumFacing.EAST;
		else if (target.getX() < examinedBlock.getX())
			return EnumFacing.WEST;
		else if (target.getZ() > examinedBlock.getZ())
			return EnumFacing.SOUTH;
		else if (target.getZ() < examinedBlock.getZ())
			return EnumFacing.NORTH;
		return null;
	}

	public static boolean isWrench(@Nonnull ItemStack item) {
		if (item.isEmpty())
			return false;
		if (item.getItem().getUnlocalizedName().toLowerCase().contains("wrench")
				|| item.getItem().getUnlocalizedName().toLowerCase().contains("configurator")
				|| item.getItem().getUnlocalizedName().toLowerCase().contains("hammer"))
			return true;
		for (int i : OreDictionary.getOreIDs(item)) {
			if (OreDictionary.getOreName(i).toLowerCase().contains("wrench"))
				return true;
		}
		return false;
	}


}
