package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityNetworkElement;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BlockAIMDevice extends BlockAIMMachine {

	public static final PropertyBool ISACTIVE = PropertyBool.create("isactive");
	
	public BlockAIMDevice(@Nonnull String bname) {
		super(bname);
	}

	@Nonnull
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof TileEntityNetworkElement)) return state.withProperty(ISACTIVE, false);
		else return state.withProperty(ISACTIVE,((TileEntityNetworkElement)te).getCoreActive() || ((TileEntityNetworkElement)te).isCoreActive());
	}
	
	@Override
	public abstract boolean onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ);

	@Nullable
	@Override
	public abstract TileEntity createNewTileEntity(World world, int i);
	
}
