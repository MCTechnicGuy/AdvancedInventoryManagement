package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.IManualEntry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;

public class BlockFluidMoltenXP extends BlockFluidClassic implements IManualEntry {

    public static final String NAME = "fluid_moltenxp";

    public BlockFluidMoltenXP(@Nonnull Fluid fluid) {
        super(fluid, Material.WATER);
        setUnlocalizedName(NAME);
        setRegistryName(NAME);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Nonnull
    @Override
    public String getManualName() {
        return "moltenxp";
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
        return new Object[] {AdvancedInventoryManagement.XP_PER_BUCKET};
    }

    @Override
    public boolean needsSmallerFont() {
        return false;
    }
}
