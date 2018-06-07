package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkElement;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BlockAIMDevice extends BlockAIMBase {

	public static final PropertyBool ISACTIVE = PropertyBool.create("isactive");
	
	public BlockAIMDevice(@Nonnull String bname) {
		super(bname);
		this.setDefaultState(blockState.getBaseState().withProperty(ISACTIVE, false));
	}

    @Nonnull
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof TileEntityNetworkElement)) return state.withProperty(ISACTIVE, false);
		else return state.withProperty(ISACTIVE,((TileEntityNetworkElement)te).getCoreActive() || ((TileEntityNetworkElement)te).isCoreActive());
	}

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ISACTIVE, false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ISACTIVE);
    }


    @Override
    protected EnumRightClickResult onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, TileEntity tileEntity, ItemStack heldItem) {
        EnumRightClickResult superResult = super.onBlockActivated(world, pos, state, player, hand, side, tileEntity, heldItem);
        if (superResult == EnumRightClickResult.ACTION_PASS) {

            if (tileEntity instanceof TileEntityNetworkElement && !((TileEntityNetworkElement)tileEntity).isPlayerAccessAllowed(player)) return EnumRightClickResult.ACTION_DONE;

            if (!(tileEntity instanceof TileEntityAIMDevice)) return EnumRightClickResult.ACTION_DISABLED;

            return EnumRightClickResult.ACTION_PASS;
        } else return superResult;
    }

    @Nullable
	@Override
	public abstract TileEntity createNewTileEntity(World world, int i);


}
