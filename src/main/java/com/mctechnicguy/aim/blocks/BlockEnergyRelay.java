package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.blocks.property.PropertyAIMMode;
import com.mctechnicguy.aim.tileentity.TileEntityEnergyRelay;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockEnergyRelay extends BlockAIMModulatedDevice {

    public static final String NAME = "energyrelay";
    public static final PropertyAIMMode MODE = PropertyAIMMode.create("mode", "all", "hotbar", "mainhand", "offhand");

    public BlockEnergyRelay() {
        super(NAME);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityEnergyRelay();
    }

    @Override
    public PropertyAIMMode getModeProperty() {
        return MODE;
    }

}
