package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntitySolarGenerator;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockSolarGenerator extends BlockAIMBase implements IAIMGenerator {

    public static final PropertyBool PRODUCING = PropertyBool.create("producing");
    public static final String NAME = "solargenerator";

    public BlockSolarGenerator() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(PRODUCING, false));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntitySolarGenerator();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PRODUCING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(PRODUCING, meta > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PRODUCING) ? 1 : 0;
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[] {AdvancedInventoryManagement.MAX_SOLAR_POWER_OUTPUT};
    }

}
