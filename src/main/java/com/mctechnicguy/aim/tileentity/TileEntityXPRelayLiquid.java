package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.util.ModCompatHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileEntityXPRelayLiquid extends TileEntityAIMDevice implements IFluidHandler {

    private double XP_PER_MILLI_BUCKET;

    public TileEntityXPRelayLiquid() {
        XP_PER_MILLI_BUCKET = (double)AdvancedInventoryManagement.XP_PER_BUCKET / 1000D;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        try {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) this;
        } catch (ClassCastException x) {
            return super.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public String getLocalizedName() {
        return "tile.xprelay_liquid.name.short";
    }

    @Override
    public ItemStack getDisplayStack() {
        return new ItemStack(ModElementList.blockXPRelayLiquid);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[] {new FluidTankProperties(new FluidStack(getCurrentFluid(), isCoreActive() ? getFluidAmountFromPlayerXP() : 0), isCoreActive() ? Integer.MAX_VALUE - getFluidAmountFromPlayerXP() : 0)};
    }

    private Fluid getCurrentFluid() {
        switch (getDeviceMode()) {
            case 0: return ModElementList.fluidMoltenXP;
            case 1: return AdvancedInventoryManagement.USE_LIQUID_XP && ModCompatHelper.xpjuice != null ? ModCompatHelper.xpjuice : ModElementList.fluidMoltenXP;
            default: return ModElementList.fluidMoltenXP;
        }
    }

    private int getFluidAmountFromPlayerXP() {
        if (!isCoreActive()) return 0;
        else return XPToMB(currentPlayerXP());
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (!isCoreActive() || !isValidFluid(resource.getFluid())) return 0;
        int toFill = Math.min(Integer.MAX_VALUE - getFluidAmountFromPlayerXP(), resource.amount);
        if (doFill) {
            addXPToPlayer(toFill);
        }
        return toFill;
    }

    private boolean isValidFluid(Fluid fluid) {
        return fluid.equals(ModElementList.fluidMoltenXP) || (AdvancedInventoryManagement.USE_LIQUID_XP && ModCompatHelper.xpjuice != null && fluid.equals(ModCompatHelper.xpjuice));
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (!isCoreActive() || !isValidFluid(resource.getFluid())) return null;
        int toDrain = Math.min(resource.amount, getFluidAmountFromPlayerXP());
        toDrain = removeXPFromPlayer(toDrain, doDrain);
        return new FluidStack(resource.getFluid(), toDrain);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (!isCoreActive()) return null;
        int toDrain = Math.min(maxDrain, getFluidAmountFromPlayerXP());
        toDrain = removeXPFromPlayer(toDrain, doDrain);
        return new FluidStack(getCurrentFluid(), toDrain);
    }

    private int mbToXP(int mb) {
        return (int)Math.round(mb * XP_PER_MILLI_BUCKET);
    }

    private int XPToMB(int XP) {
        return (int)Math.round(XP / XP_PER_MILLI_BUCKET);
    }

    private int currentPlayerXP() {
        return xpForLevel(getPlayer().experienceLevel) + Math.round(getPlayer().experience * getPlayer().xpBarCap());
    }

    private int xpForLevel(int level) {
        if (level <= 0) return 0;
        return ((level - 1) >= 30 ? 112 + ((level - 1) - 30) * 9 : ((level - 1) >= 15 ? 37 + ((level - 1) - 15) * 5 : 7 + (level - 1) * 2)) + xpForLevel(--level);
    }

    private int getLevelForXP(int xp) {
        int level = 0;
        while(xpForLevel(level + 1) <= xp ) level++;
        return level;
    }

    private int removeXPFromPlayer(int mbXP, boolean doDrain) {
        int max_remove = currentPlayerXP();
        int toRemove = mbToXP(mbXP);

        if (toRemove > max_remove)
        {
            toRemove = max_remove;
        }

        if (doDrain) {
            getPlayer().addScore(-toRemove);
            int newTotal = currentPlayerXP() - toRemove;
            getPlayer().experienceTotal = newTotal;
            getPlayer().experienceLevel = getLevelForXP(newTotal);
            float xpDiff = newTotal - xpForLevel(getPlayer().experienceLevel);
            float xpToNextLevel = xpForLevel(getPlayer().experienceLevel + 1) - xpForLevel(getPlayer().experienceLevel);
            getPlayer().experience = xpDiff / xpToNextLevel;
        }
        return XPToMB(toRemove);
    }

    private void addXPToPlayer(int mbXP) {
        getPlayer().addScore(mbToXP(mbXP));
        int newTotal = currentPlayerXP() + mbToXP(mbXP);
        getPlayer().experienceTotal = newTotal;
        getPlayer().experienceLevel = getLevelForXP(newTotal);
        float xpDiff = newTotal - xpForLevel(getPlayer().experienceLevel);
        float xpToNextLevel = xpForLevel(getPlayer().experienceLevel + 1) - xpForLevel(getPlayer().experienceLevel);
        getPlayer().experience = xpDiff / xpToNextLevel;
    }
}
