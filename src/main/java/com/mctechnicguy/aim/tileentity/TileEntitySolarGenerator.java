package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.blocks.BlockSolarGenerator;
import net.minecraft.util.ITickable;
import net.minecraft.world.biome.Biome;

public class TileEntitySolarGenerator extends TileEntityAIMDevice implements ITickable {

    private boolean lastActive;

    @Override
    public void update() {
        if (hasWorld() && world.isRemote) return;

        if (shouldBeActive()) {
            outputPower();
            if (!lastActive && hasWorld()) {
                lastActive = true;
                world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockSolarGenerator.PRODUCING, true));
            }
        } else {
            if (lastActive && hasWorld()) {
                lastActive = false;
                world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockSolarGenerator.PRODUCING, false));
            }
        }
    }

    private int getOutput() {
        int output =  AdvancedInventoryManagement.MAX_SOLAR_POWER_OUTPUT;
        if (world.provider.getBiomeForCoords(getPos()).getTempCategory() == Biome.TempCategory.WARM) {
            output *= AdvancedInventoryManagement.SOLAR_POWER_BIOME_MULTIPLIER;
        }
        return output;
    }

    public boolean shouldBeActive() {
        return world.canBlockSeeSky(pos.add(0,1,0)) && !world.isRaining() && !world.provider.isNether() && !world.isThundering() && world.isDaytime();
    }

    private void outputPower() {
        if (this.hasCore()) {
            this.getCore().changePower(getOutput());
        }
    }

}
