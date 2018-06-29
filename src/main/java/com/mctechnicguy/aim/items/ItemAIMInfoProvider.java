package com.mctechnicguy.aim.items;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.util.AIMUtils;
import com.mctechnicguy.aim.util.NBTUtils;
import com.mctechnicguy.aim.util.NetworkUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemAIMInfoProvider extends Item implements IManualEntry{

	public static final String NAME = "infoprovider";
	
	public ItemAIMInfoProvider() {
		this.setMaxStackSize(1);
		this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
		this.setUnlocalizedName(NAME);
		this.setRegistryName(NAME);
	}

    @Nonnull
	@Override
	public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking()) return EnumActionResult.PASS;
        if (world.isRemote) return EnumActionResult.SUCCESS;
		return NetworkUtils.checkNetworkStats(player, world, pos);
    }

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn, @Nonnull EnumHand hand)
    {
		if (playerIn.isSneaking() && !worldIn.isRemote) {
			this.displayPlayerDetails(worldIn, playerIn);
		}
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }


	private void displayPlayerDetails(@Nonnull World world, @Nonnull EntityPlayer player) {
		if (!world.isRemote) {
			NBTTagList list = NBTUtils.readPlayerInfo(player.getUniqueID(), world);
			AIMUtils.sendChatMessageWithArgs("message.playerCoreLocation.start", player, TextFormatting.AQUA, player.getDisplayName());
			if (list == null || list.tagCount() == 0) {
				AIMUtils.sendChatMessageWithArgs("message.playerCoreLocation.noneFound", player, TextFormatting.RED);
			} else {
				for (int i = 0; i < list.tagCount(); i++) {
					NBTTagCompound ct = list.getCompoundTagAt(i);
					AIMUtils.sendChatMessageWithArgs("message.playerCoreLocation.tag", player, TextFormatting.RESET, ct.getInteger("coreX"),
								ct.getInteger("coreY"), ct.getInteger("coreZ"), ct.getString("DimName"), ct.getInteger("DimID"));
				}
			}
			AIMUtils.sendChatMessageWithArgs(
					AIMUtils.isPlayerAccessible(player) ? "message.playerAccessibility.true" : "message.playerAccessibility.false", player,
					AIMUtils.isPlayerAccessible(player) ? TextFormatting.GREEN : TextFormatting.RED);
			AIMUtils.sendChatMessageWithArgs("message.playerCoreLocation.end", player, TextFormatting.AQUA, player.getDisplayName());
		}
	}

	@Nonnull
	@Override
	public String getManualName() {
		return NAME;
	}

	@Override
	public int getPageCount() {
		return 1;
	}

	@Nonnull
	@Override
	public Object[] getParams(int page) {
		return new Object[0];
	}


}
