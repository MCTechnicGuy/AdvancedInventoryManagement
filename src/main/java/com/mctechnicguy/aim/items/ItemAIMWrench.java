package com.mctechnicguy.aim.items;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.util.IWrenchDestroyable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemAIMWrench extends Item implements IManualEntry{

	public static final String NAME = "aimwrench";

	public ItemAIMWrench() {
		this.setMaxStackSize(1);
		this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
		this.setUnlocalizedName(NAME);
		this.setRegistryName(NAME);
	}

	@Nonnull
    @Override
	public EnumActionResult onItemUse(@Nullable EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player == null) return EnumActionResult.PASS;
		ItemStack stack = player.getHeldItem(hand);

		Block block = world.getBlockState(pos).getBlock();
		boolean success = false;
		if (!world.isRemote && player.isSneaking()) {
			if (block instanceof IWrenchDestroyable) {
				((IWrenchDestroyable) block).destroyWithWrench(player, world, pos, stack);
				success = true;
			}
			if (success) {
				player.swingArm(hand);
				return EnumActionResult.SUCCESS;
			}
		}
		if (world.isRemote && player.isSneaking() && block instanceof IWrenchDestroyable) return EnumActionResult.SUCCESS;
		return EnumActionResult.PASS;
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

	@Override
	public boolean doesProvideOwnContent() {
		return false;
	}

	@Nonnull
    @Override
	public Object[] getParams(int page) {
		return new Object[0];
	}

	@Override
	public boolean needsSmallerFont() {
		return false;
	}
}
