package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.blocks.property.PropertyAIMMode;
import com.mctechnicguy.aim.tileentity.TileEntityMotionEditor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockMotionEditor extends BlockAIMModulatedDevice {

    public static final String NAME = "motioneditor";
    private static final PropertyAIMMode MODE = PropertyAIMMode.create("mode", "down", "up", "north", "south", "west", "east");

    public BlockMotionEditor() {
        super(NAME);
    }

    @Override
    public void neighborChanged(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, Block blockIn, BlockPos neighbor) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityMotionEditor) {
            ((TileEntityMotionEditor)worldIn.getTileEntity(pos)).addMotionToPlayer(worldIn.isBlockIndirectlyGettingPowered(pos));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityMotionEditor();
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[] {AdvancedInventoryManagement.POWER_PER_MOTION_EDIT, AdvancedInventoryManagement.POWER_PER_MOTION_EDIT * 225};
    }


    @Override
    protected PropertyAIMMode getModeProperty() {
        return MODE;
    }

}
