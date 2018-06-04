package com.mctechnicguy.aim.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHasModes {

    int getIDFromState(IBlockState state);

    String getCurrentModeUnlocalizedName(World world, BlockPos pos);

    void cycleToNextMode(World world, BlockPos pos, EntityPlayer causer);

    void setMode(World world, BlockPos pos, int id, EntityPlayer causer);

}
