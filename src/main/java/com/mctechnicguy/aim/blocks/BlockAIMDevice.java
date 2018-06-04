package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkElement;
import com.mctechnicguy.aim.util.AIMUtils;
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
	}

	@Nonnull
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof TileEntityNetworkElement)) return state.withProperty(ISACTIVE, false);
		else return state.withProperty(ISACTIVE,((TileEntityNetworkElement)te).getCoreActive() || ((TileEntityNetworkElement)te).isCoreActive());
	}


    @Override
    protected EnumRightClickResult onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, TileEntity tileEntity, ItemStack heldItem) {
        EnumRightClickResult superResult = super.onBlockActivated(world, pos, state, player, hand, side, tileEntity, heldItem);
        if (superResult == EnumRightClickResult.ACTION_PASS) {

            if (tileEntity instanceof TileEntityNetworkElement && !((TileEntityNetworkElement)tileEntity).isPlayerAccessAllowed(player)) return EnumRightClickResult.ACTION_DONE;

            if (!(tileEntity instanceof TileEntityAIMDevice)) return EnumRightClickResult.ACTION_DISABLED;

            if (this instanceof IHasModes && AIMUtils.isWrench(heldItem) && ((TileEntityAIMDevice) tileEntity).isPlayerAccessAllowed(player)) {
                if (!world.isRemote) ((IHasModes)this).cycleToNextMode(world, pos, player);
                return EnumRightClickResult.ACTION_DONE;
            }

            return EnumRightClickResult.ACTION_PASS;
        } else return superResult;
    }

    @Nullable
	@Override
	public abstract TileEntity createNewTileEntity(World world, int i);

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ISACTIVE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return this instanceof IHasModes ? ((IHasModes)this).getIDFromState(state) : 0;
    }

}
