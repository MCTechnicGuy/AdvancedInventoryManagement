package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.blocks.BlockPlayerMonitor;
import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;

public class TileEntityPlayerMonitor extends TileEntityAIMDevice implements ITickable{

    private int mode;
    private int redstoneBehaviour;
    private String modeFormatted;

    private String preSentFormattedValue;
    private String preSentPercentageValue;

    private int powerLevel;
    private double prevValue;
    private int pulseTicks;
    private boolean needsUpdate;
    private boolean lastCoreActive;

    @Nonnull
    private DecimalFormat doubleRound = new DecimalFormat("#.##");

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        mode = nbt.getInteger("MonitorMode");
        redstoneBehaviour = nbt.getInteger("RedstoneBehaviour");
        needsUpdate = true;
    }

    public SPacketUpdateTileEntity getUpdatePacket() {
        if (world.isRemote) return null;
        NBTTagCompound nbtTag = getUpdateTag();
        this.writeCoreData(nbtTag);
        return new SPacketUpdateTileEntity(this.getPos(), 0, nbtTag);
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbtTag =  super.getUpdateTag();
        nbtTag.setInteger("RedstoneBehaviour", redstoneBehaviour);
        nbtTag.setInteger("MonitorMode", mode);
        if (getFormattedValue() != null) nbtTag.setString("FormattedValue", getFormattedValue());
        if (getPercentageValue() != null) nbtTag.setString("PercentageValue", getPercentageValue());
        return nbtTag;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        mode = packet.getNbtCompound().getInteger("MonitorMode");
        redstoneBehaviour = packet.getNbtCompound().getInteger("RedstoneBehaviour");
        modeFormatted = I18n.format("mode.monitordisplay." + BlockPlayerMonitor.EnumMode.fromID(mode).getName());
        preSentFormattedValue = packet.getNbtCompound().getString("FormattedValue");
        if (preSentFormattedValue.equals("true") || preSentFormattedValue.equals("false")) preSentFormattedValue = I18n.format("message." + preSentFormattedValue);
        preSentPercentageValue = packet.getNbtCompound().getString("PercentageValue");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setInteger("RedstoneBehaviour", redstoneBehaviour);
        nbt.setInteger("MonitorMode", mode);
        return nbt;
    }


    private double getMaxValue() {
        if (!isCoreActive() || getPlayer() == null) return 0D;
        switch (mode) {
            case 0: return (double)getPlayer().getMaxHealth();
            case 2: return 20D; //Maximum food level
            case 3: return 20D; //Maximum saturation level
            case 4: return 300D; //Maximum air value (in Ticks)
            case 8: return 20D; //Maximum armor value (should be...)
            case 20: return (double)InventoryPlayer.getHotbarSize();
            default: return Double.NaN;
        }
    }


    private double getCurrentValue() {
        if (!isCoreActive() || getPlayer() == null) return 0D;
        switch (mode) {
            case 0: return (double)getPlayer().getHealth();
            case 1: return (double)getPlayer().experienceLevel;
            case 2: return (double)getPlayer().getFoodStats().getFoodLevel();
            case 3: return (double)getPlayer().getFoodStats().getSaturationLevel();
            case 4: return (double)getPlayer().getAir();
            case 5: return getPlayer().chasingPosX - getPlayer().prevChasingPosX;
            case 6: return getPlayer().chasingPosY - getPlayer().prevChasingPosY;
            case 7: return getPlayer().chasingPosZ - getPlayer().prevChasingPosZ;
            case 8: return (double)getPlayer().getTotalArmorValue();
            case 9: return getPlayer().posX;
            case 10: return getPlayer().posY;
            case 11: return getPlayer().posZ;
            case 12: return getPlayer().isBurning() ? 1D : 0D;
            case 13: return getPlayer().isInWater() ? 1D : 0D;
            case 14: return getPlayer().isInLava() ? 1D : 0D;
            case 15: return !getPlayer().onGround ? 1D : 0D;
            case 16: return getPlayer().isSneaking() ? 1D : 0D;
            case 17: return getPlayer().isSprinting() ? 1D : 0D;
            case 18: return getPlayer().fallDistance > 0D ? 1D : 0D;
            case 19: return (double)getPlayer().inventory.currentItem;
            case 20: return (double)getPlayer().dimension;
            default: return Double.NaN;
        }
    }


    private String getCurrentFormattedValue() {
        if (!isCoreActive() || getPlayer() == null) return null;
        switch (mode) {
            case 0: return doubleRound.format(getPlayer().getHealth()) + " / " + getPlayer().getMaxHealth() + " HP";
            case 1: return getPlayer().experienceLevel + " XP";
            case 2: return getPlayer().getFoodStats().getFoodLevel() + " / " + getMaxValue();
            case 3: return getPlayer().getFoodStats().getSaturationLevel() + " / " + getMaxValue();
            case 4: return Math.round(getPlayer().getAir() / 30) + " / " + getMaxValue();
            case 5: return doubleRound.format((getPlayer().chasingPosX - getPlayer().prevChasingPosX) * 20) + " m/s";
            case 6: return doubleRound.format((getPlayer().chasingPosY - getPlayer().prevChasingPosY) * 20) + " m/s";
            case 7: return doubleRound.format((getPlayer().chasingPosZ - getPlayer().prevChasingPosZ) * 20) + " m/s";
            case 8: return getPlayer().getTotalArmorValue() + " / " + getMaxValue();
            case 9: return doubleRound.format(getPlayer().posX);
            case 10: return doubleRound.format(getPlayer().posY);
            case 11: return doubleRound.format(getPlayer().posZ);
            case 19: return String.valueOf(getPlayer().inventory.currentItem + 1);
            case 20: return "DIM-" + getPlayer().dimension;
            default: return null;
        }
    }

    private boolean isBooleanMode() {
        return mode > 11 && mode < 19;
    }

    @Nullable
    public String getFormattedValue() {
        if (!isCoreActive()) return null;
        if (AdvancedInventoryManagement.proxy.playerEqualsClient(getCore().playerConnectedID)) {
            if (isBooleanMode() && !Double.isNaN(getCurrentValue())) {
                if (world.isRemote) return AdvancedInventoryManagement.proxy.tryToLocalizeString(getCurrentValue() == 1D ? "message.true" : "message.false");
                else return String.valueOf(getCurrentValue() == 1D);
            }
            else return getCurrentFormattedValue();
        }
        else return preSentFormattedValue;
    }

    @Nullable
    public String getPercentageValue() {
        if (!isCoreActive()) return null;
        if (AdvancedInventoryManagement.proxy.playerEqualsClient(getCore().playerConnectedID)) {
            if (Double.isNaN(getMaxValue()) || getMaxValue() <= 0 || mode > 11) return null;
            else return doubleRound.format((getCurrentValue() / getMaxValue()) * 100) + "%";
        } else return preSentPercentageValue;
    }

    private double getPercentageAsNumber() {
        if (!isCoreActive()) return Double.NaN;
        if (Double.isNaN(getMaxValue()) || getMaxValue() <= 0 || mode > 11) return Double.NaN;
        else return getCurrentValue() / getMaxValue();
    }

    @Nullable
    @Override
    public EntityPlayer getPlayer() {
        if (!world.isRemote) return super.getPlayer();
        if (AdvancedInventoryManagement.proxy.playerEqualsClient(getCore().playerConnectedID)) {
            return AdvancedInventoryManagement.proxy.getClientPlayer();
        }
        return null;
    }

    public int getDeviceMode() {
        return mode;
    }

    public void setDeviceMode(int m) {
        mode = m;
        needsUpdate = true;
    }

    public int getRedstoneBehaviour() {
        return redstoneBehaviour;
    }

    public void setRedstoneBehaviour(int redstoneBehaviour) {
        this.redstoneBehaviour = redstoneBehaviour;
        needsUpdate = true;
    }

    public String getModeFormatted() {
        return modeFormatted;
    }

    private void resetToDefaults() {
        prevValue = getCurrentValue();
        pulseTicks = 0;
        powerLevel = 0;
        setStaticRedstoneOutput();
        updateBlock();
        needsUpdate = false;
    }

    private int getCurrentRedstoneValue() {
        if (isBooleanMode()) {
            return getCurrentValue() == 1 ? 15 : 0;
        } else
            return Double.isNaN(getPercentageAsNumber()) ? (int)Math.ceil(Math.min(getCurrentValue(), 15)) : (int)Math.ceil(getPercentageAsNumber() * 15);
    }

    private void setStaticRedstoneOutput() {
        if (getRedstoneBehaviour() == 2) {
            powerLevel = 15;
        } else if (getRedstoneBehaviour() == 3) {
            powerLevel = getCurrentRedstoneValue();
        } else if (getRedstoneBehaviour() == 4) {
            powerLevel = 15 - getCurrentRedstoneValue();
        }
    }

    private void setDynamicRedstoneOutput() {
        if (getRedstoneBehaviour() == 1) {
            powerLevel = 15;
            pulseTicks = 5;
        } else if (getRedstoneBehaviour() == 2) {
            powerLevel = 0;
            pulseTicks = 5;
        } else if (getRedstoneBehaviour() == 5) {
            if (isBooleanMode()) {
                powerLevel = 15;
            } else {
                powerLevel = Double.isNaN(getPercentageAsNumber()) ? (int)Math.round(Math.min(15, Math.abs(getCurrentValue() - prevValue))) : (int)Math.round(Math.min(15, Math.abs(getPercentageAsNumber() - (prevValue / getMaxValue())) * 15));
            }
            pulseTicks = 5;
        }
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        if (isCoreActive() != lastCoreActive) {
            if (isCoreActive()) needsUpdate = true;
            else resetToDefaults();
            lastCoreActive = isCoreActive();
        }

        if (needsUpdate) {
            resetToDefaults();
            return;
        }

        if (!isCoreActive()) return;

        if (pulseTicks > 0) {
            pulseTicks--;
            if (pulseTicks == 0) {
                resetToDefaults();
            }
        }

        //If a change in value occurred
        if (!Double.isNaN(getCurrentValue()) && !Double.isNaN(prevValue) && (getCurrentValue() != prevValue)) {
            setStaticRedstoneOutput();
            setDynamicRedstoneOutput();
            prevValue = getCurrentValue();
            updateBlock();
        }
    }

    @Override
    public void updateBlock() {
        if (this.hasWorld()) {
            world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 3);
            this.markDirty();
        }
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public int getMaxRSMode() { //0 = Deactivated, 1 = Emit pulse on change, 2= Emit pulse on Change (Inverted), 3 = Show Status, 4 = Show status (inverted), 5 = Emit last percentage change as pulse
        switch (mode) {
            case 0: return 5;
            case 1: return 5;
            case 2: return 5;
            case 3: return 5;
            case 4: return 5;
            case 5: return 5;
            case 6: return 5;
            case 7: return 5;
            case 8: return 5;
            case 9: return 2;
            case 10: return 2;
            case 11: return 2;
            case 12: return 4;
            case 13: return 4;
            case 14: return 4;
            case 15: return 4;
            case 16: return 4;
            case 17: return 4;
            case 18: return 4;
            case 19: return 5;
            case 20: return 2;
            default: return 0;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatusInformation(NetworkInfoOverlayRenderer renderer) {
        super.renderStatusInformation(renderer);
        renderer.renderModeString("mode.monitor." + BlockPlayerMonitor.EnumMode.fromID(mode).getName());
        renderer.renderTileValues("redstonemode", TextFormatting.AQUA, false, I18n.format("rsmode.monitor." + this.redstoneBehaviour));
    }
}
