package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.blocks.BlockSolarGenerator;
import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileEntitySolarGenerator extends TileEntityAIMDevice implements ITickable {

    private boolean lastActive;
    private int currentOutput;
    public boolean isActive;

    @Override
    public void update() {
        if (!hasWorld() || world.isRemote) return;

        if (shouldBeActive()) {
            outputPower();
            if (!lastActive) {
                lastActive = true;
                world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockSolarGenerator.PRODUCING, true), 3);
            }
        } else {
            if (lastActive) {
                lastActive = false;
                world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockSolarGenerator.PRODUCING, false), 3);
            }
        }
    }

    @Override
    public void onLoad() {
        this.lastActive = world.getBlockState(pos).getValue(BlockSolarGenerator.PRODUCING);
    }

    private int getOutput() {
        int output =  AdvancedInventoryManagement.MAX_SOLAR_POWER_OUTPUT;
        if (world.provider.getBiomeForCoords(getPos()).getTempCategory() == Biome.TempCategory.WARM) {
            output *= AdvancedInventoryManagement.SOLAR_POWER_BIOME_MULTIPLIER;
        }
        return output;
    }

    private boolean shouldBeActive() {
        return world.canBlockSeeSky(pos.add(0,1,0)) && !world.isRaining() && !world.provider.isNether() && !world.isThundering() && world.isDaytime();
    }

    private void outputPower() {
        if (this.hasCore()) {
            this.getCore().changePower(getOutput());
        }
    }

    @Nullable
    @Override
    public NBTTagCompound getTagForOverlayUpdate() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("output", getOutput());
        return nbt;
    }

    @Override
    public void handleTagForOverlayUpdate(NBTTagCompound nbt) {
        super.handleTagForOverlayUpdate(nbt);
        currentOutput = nbt.getInteger("output");
        hasAccurateServerInfo = true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatusInformation(NetworkInfoOverlayRenderer renderer) {
        renderer.renderStatusString(hasCore() ? shouldBeActive() ? "aimoverlay.generatorstatus.active" : "aimoverlay.generatorstatus.idle" : "aimoverlay.generatorstatus.offline",
                hasCore() ? shouldBeActive() ? TextFormatting.GREEN : TextFormatting.YELLOW : TextFormatting.RED);
        renderer.renderTileValues("poweroutput", TextFormatting.GREEN, !hasAccurateServerInfo, hasCore() && shouldBeActive() ? currentOutput : 0);
    }
}
