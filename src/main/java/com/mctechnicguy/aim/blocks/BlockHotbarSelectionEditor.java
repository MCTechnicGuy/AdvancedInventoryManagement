package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityHotbarSelectionEditor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;


public class BlockHotbarSelectionEditor extends BlockAIMDevice {

    public static final String NAME = "hotbarselectioneditor";

    public BlockHotbarSelectionEditor() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
    }

    @Override
    public void neighborChanged(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, Block blockIn, BlockPos neighbor) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityHotbarSelectionEditor) {
            ((TileEntityHotbarSelectionEditor)worldIn.getTileEntity(pos)).forceHotbarSelection(worldIn.isBlockIndirectlyGettingPowered(pos));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityHotbarSelectionEditor();
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[] {AdvancedInventoryManagement.POWER_PER_SLOT_SELECTION};
    }

}
