package com.mctechnicguy.aim.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWrenchDestroyable {

	void destroyWithWrench(EntityPlayer player, World world, BlockPos pos, ItemStack stack);
}
