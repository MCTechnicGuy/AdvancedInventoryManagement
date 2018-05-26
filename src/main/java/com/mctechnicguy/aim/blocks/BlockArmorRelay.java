package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityArmorRelay;
import com.mctechnicguy.aim.gui.IManualEntry;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockArmorRelay extends BlockAIMDevice implements IManualEntry{

	public static final String NAME = "armorrelay";
	
	public BlockArmorRelay() {
		super(NAME);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
	}
	
	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityArmorRelay();
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
	    return new BlockStateContainer(this, new IProperty[] { ISACTIVE });
	}

	@Override
	public boolean onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
	    return this.getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
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
