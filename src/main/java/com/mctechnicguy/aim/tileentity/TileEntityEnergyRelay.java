package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Optional.InterfaceList({
        @Optional.Interface(iface="net.darkhax.tesla.api.ITeslaConsumer", modid="tesla"),
        @Optional.Interface(iface="net.darkhax.tesla.api.ITeslaHolder", modid="tesla")
})
public class TileEntityEnergyRelay extends TileEntityAIMDevice implements IEnergyStorage, net.darkhax.tesla.api.ITeslaConsumer, net.darkhax.tesla.api.ITeslaHolder{

    private int lastPowerFlow;

    @CapabilityInject(net.darkhax.tesla.api.ITeslaConsumer.class)
    private static Capability<ITeslaConsumer> POWER_STORAGE_CAP = null;

    @CapabilityInject(net.darkhax.tesla.api.ITeslaHolder.class)
    private static Capability<ITeslaHolder> POWER_HOLDER_CAP = null;

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || (POWER_STORAGE_CAP != null && capability == POWER_STORAGE_CAP) ||
                (POWER_HOLDER_CAP != null && capability == POWER_HOLDER_CAP) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (POWER_STORAGE_CAP != null && capability == POWER_STORAGE_CAP) return (T) this;
        if (POWER_HOLDER_CAP != null && capability == POWER_HOLDER_CAP) return (T) this;
        if (capability == CapabilityEnergy.ENERGY) return (T) this;
        return super.getCapability(capability, facing);
    }

    private int addPowerToPlayer(int maxReceive, boolean simulate) {
        int toReceive = maxReceive;
        if (!isCoreActive()) return 0;
        for (ItemStack stack : getSelectedItems()) {
            if (toReceive <= 0) return maxReceive;
            if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) { //Forgepower-Item
                toReceive -= stack.getCapability(CapabilityEnergy.ENERGY, null).receiveEnergy(toReceive, simulate);
            } else if (POWER_STORAGE_CAP != null && POWER_HOLDER_CAP != null && stack.hasCapability(POWER_HOLDER_CAP, null)
                    && stack.hasCapability(POWER_STORAGE_CAP, null)) { //Tesla-Item
                toReceive -= stack.getCapability(POWER_STORAGE_CAP, null).givePower(toReceive, simulate);
            }
        }
        lastPowerFlow = maxReceive - toReceive;
        return maxReceive - toReceive;
    }

    private int getPowerAtPlayer() {
        if (!isCoreActive()) return 0;
        return getCurrentPowerFromItemList(getSelectedItems());
    }

    private int getMaxPowerAtPlayer() {
        if (!isCoreActive()) return 0;
        return getMaxPowerFromItemList(getSelectedItems());
    }

    private NonNullList<ItemStack> getSelectedItems() {
        if (!isCoreActive()) return null;
        switch (getDeviceMode()) {
            case 0: {
                NonNullList<ItemStack> list = NonNullList.create();
                list.addAll(getPlayerInventory().offHandInventory);
                list.addAll(getPlayerInventory().armorInventory);
                list.addAll(getPlayerInventory().mainInventory);
                return list;
            }
            case 1: {
                NonNullList<ItemStack> list = NonNullList.create();
                list.addAll(getPlayerInventory().mainInventory.subList(0, InventoryPlayer.getHotbarSize()));
                return list;
            }
            case 2: {
                NonNullList<ItemStack> list = NonNullList.create();
                list.add(getPlayer().getHeldItem(EnumHand.MAIN_HAND));
                return list;
            }
            case 3: {
                return getPlayerInventory().offHandInventory;
            }
        }
        return null;
    }

    private int getCurrentPowerFromItemList(NonNullList<ItemStack> list) {
        int PowerSum = 0;
        for (ItemStack stack : list) {
            if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) { //Forgepower-Item
                PowerSum += stack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored();
            } else if (POWER_STORAGE_CAP != null && POWER_HOLDER_CAP != null && stack.hasCapability(POWER_HOLDER_CAP, null)
                    && stack.hasCapability(POWER_STORAGE_CAP, null)) { //Tesla-Item
                PowerSum += stack.getCapability(POWER_HOLDER_CAP, null).getStoredPower();
            }
        }
        return PowerSum;
    }

    private int getMaxPowerFromItemList(NonNullList<ItemStack> list) {
        int PowerSum = 0;
        for (ItemStack stack : list) {
             if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) { //Forgepower-Item
                PowerSum += stack.getCapability(CapabilityEnergy.ENERGY, null).getMaxEnergyStored();
            } else if (POWER_STORAGE_CAP != null && POWER_HOLDER_CAP != null && stack.hasCapability(POWER_HOLDER_CAP, null)
                    && stack.hasCapability(POWER_STORAGE_CAP, null)) { //Tesla-Item
                PowerSum += stack.getCapability(POWER_HOLDER_CAP, null).getCapacity();
            }
        }
        return PowerSum;
    }

    @Override
    public long givePower(long powerOffered, boolean simulated) {
        return addPowerToPlayer((int)powerOffered, simulated);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return addPowerToPlayer(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return getPowerAtPlayer();
    }

    @Override
    public int getMaxEnergyStored() {
        return getMaxPowerAtPlayer();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return getMaxPowerAtPlayer() - getPowerAtPlayer() > 0;
    }

    @Override
    public long getStoredPower() {
        return getPowerAtPlayer();
    }

    @Override
    public long getCapacity() {
        return getMaxPowerAtPlayer();
    }

    @Nullable
    @Override
    public NBTTagCompound getTagForOverlayUpdate() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("lastPowerFlow", lastPowerFlow);
        return nbt;
    }

    @Override
    public void handleTagForOverlayUpdate(NBTTagCompound nbt) {
        if (nbt.hasKey("lastPowerFlow")) {
            this.lastPowerFlow = nbt.getInteger("lastPowerFlow");
            hasAccurateServerInfo = true;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatusInformation(NetworkInfoOverlayRenderer renderer) {
        super.renderStatusInformation(renderer);
        renderer.renderTileValues("powerinput", TextFormatting.GREEN, !hasAccurateServerInfo, lastPowerFlow);
    }
}