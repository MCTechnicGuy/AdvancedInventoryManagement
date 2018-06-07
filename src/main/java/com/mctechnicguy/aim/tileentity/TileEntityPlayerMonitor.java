package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.blocks.BlockPlayerMonitor;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;

public class TileEntityPlayerMonitor extends TileEntityAIMDevice implements ITickable{

    private int mode;
    private int redstone_behaviour;
    private String mode_formatted;

    private String preSentFormattedValue;
    private String preSentPercentageValue;

    private int powerLevel;
    private double prevValue;
    private boolean prevBoolValue;
    private int pulseTicks;
    private boolean needsUpdate;
    private boolean lastCoreActive;

    @Nonnull
    private DecimalFormat doubleRound = new DecimalFormat("#.##");

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        mode = nbt.getInteger("MonitorMode");
        redstone_behaviour = nbt.getInteger("RedstoneBehaviour");
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
        nbtTag.setInteger("RedstoneBehaviour", redstone_behaviour);
        nbtTag.setInteger("MonitorMode", mode);
        if (getFormattedValue() != null) nbtTag.setString("FormattedValue", getFormattedValue());
        if (getPercentageValue() != null) nbtTag.setString("PercentageValue", getPercentageValue());
        return nbtTag;
    }

    @Override
    public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        mode = packet.getNbtCompound().getInteger("MonitorMode");
        redstone_behaviour = packet.getNbtCompound().getInteger("RedstoneBehaviour");
        mode_formatted = I18n.format("mode.monitordisplay." + BlockPlayerMonitor.EnumMode.fromID(mode).getName());
        preSentFormattedValue = packet.getNbtCompound().getString("FormattedValue");
        if (preSentFormattedValue.equals("true") || preSentFormattedValue.equals("false")) preSentFormattedValue = I18n.format("message." + preSentFormattedValue);
        preSentPercentageValue = packet.getNbtCompound().getString("PercentageValue");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setInteger("RedstoneBehaviour", redstone_behaviour);
        nbt.setInteger("MonitorMode", mode);
        return nbt;
    }

    private double getMaxValue() {
        if (!isCoreActive() || getPlayer() == null) return 0;
        switch (mode) {
            case 0: return getPlayer().getMaxHealth();
            case 2: return 20; //Maximum food level
            case 3: return 20; //Maximum saturation level
            case 4: return 300; //Maximum air value (in Ticks)
            case 8: return 20; //Maximum armor value (should be...)
            case 20: return InventoryPlayer.getHotbarSize();
            default: return Double.NaN;
        }
    }

    private double getCurrentValue() {
        if (!isCoreActive() || getPlayer() == null) return 0;
        switch (mode) {
            case 0: return getPlayer().getHealth();
            case 1: return getPlayer().experienceLevel;
            case 2: return getPlayer().getFoodStats().getFoodLevel();
            case 3: return getPlayer().getFoodStats().getSaturationLevel();
            case 4: return getPlayer().getAir();
            case 5: return getPlayer().chasingPosX - getPlayer().prevChasingPosX;
            case 6: return getPlayer().chasingPosY - getPlayer().prevChasingPosY;
            case 7: return getPlayer().chasingPosZ - getPlayer().prevChasingPosZ;
            case 8: return getPlayer().getTotalArmorValue();
            case 10: return getPlayer().posX;
            case 11: return getPlayer().posY;
            case 12: return getPlayer().posZ;
            case 20: return getPlayer().inventory.currentItem;
            case 21: return getPlayer().dimension;
            default: return Double.NaN;

        }
    }

    private String getCurrentFormattedValue() {
        if (!isCoreActive() || getPlayer() == null) return null;
        switch (mode) {
            case 0: return getPlayer().getHealth() + " / " + getPlayer().getMaxHealth() + " HP";
            case 1: return getPlayer().experienceLevel + " XP";
            case 2: return getPlayer().getFoodStats().getFoodLevel() + " / 20";
            case 3: return getPlayer().getFoodStats().getSaturationLevel() + " / 20";
            case 4: return Math.round(getPlayer().getAir() / 30) + " / 10";
            case 5: return doubleRound.format((getPlayer().chasingPosX - getPlayer().prevChasingPosX) * 20) + " m/s";
            case 6: return doubleRound.format((getPlayer().chasingPosY - getPlayer().prevChasingPosY) * 20) + " m/s";
            case 7: return doubleRound.format((getPlayer().chasingPosZ - getPlayer().prevChasingPosZ) * 20) + " m/s";
            case 8: return getPlayer().getTotalArmorValue() + " / 20";
            case 9: return doubleRound.format(getPlayer().posX);
            case 10: return doubleRound.format(getPlayer().posY);
            case 11: return doubleRound.format(getPlayer().posZ);
            case 19: return String.valueOf(getPlayer().inventory.currentItem + 1);
            case 20: return "DIM-" + getPlayer().dimension;
            default: return null;
        }
    }

    @Nullable
    public String getFormattedValue() {
        if (!isCoreActive()) return null;
        if (AdvancedInventoryManagement.proxy.playerEqualsClient(getCore().playerConnectedID)) {
            if (mode > 11 && mode < 19) {
                if (world.isRemote) return I18n.format(getBooleanValue() ? "message.true" : "message.false");
                else return String.valueOf(getBooleanValue());
            }
            else return getCurrentFormattedValue();
        }
        else return preSentFormattedValue;
    }

    private boolean getBooleanValue() {
        if (!isCoreActive() || getPlayer() == null) return false;
        switch (mode) {
            case 12: return getPlayer().isBurning();
            case 13: return getPlayer().isInWater();
            case 14: return getPlayer().isInLava();
            case 15: return !getPlayer().onGround;
            case 16: return getPlayer().isSneaking();
            case 17: return getPlayer().isSprinting();
            case 18: return getPlayer().fallDistance > 0;
            default: return false;
        }
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

    public int getRedstone_behaviour() {
        return redstone_behaviour;
    }

    public void setRedstone_behaviour(int redstone_behaviour) {
        this.redstone_behaviour = redstone_behaviour;
        needsUpdate = true;
    }

    public String getMode_formatted() {
        return mode_formatted;
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        if (isCoreActive() != lastCoreActive) {
            if (isCoreActive()) needsUpdate = true;
            else {
                prevBoolValue = false;
                powerLevel = 0;
                prevValue = 0;
                pulseTicks = 0;
                updateBlock();
            }
            lastCoreActive = isCoreActive();
        }

        if (needsUpdate) {
            prevBoolValue = false;
            powerLevel = 0;
            prevValue = 0;
            pulseTicks = 0;
        }

        if (!isCoreActive()) return;

        if (pulseTicks > 0) {
            pulseTicks--;
            if (pulseTicks == 0) {
                if (redstone_behaviour == 1 || redstone_behaviour == 5) powerLevel = 0;
                if (redstone_behaviour == 2) powerLevel = 15;
                updateBlock();
            }
        }

        if (mode > 11 && mode < 19) {
            if (getBooleanValue() != prevBoolValue || needsUpdate) {
                switch (redstone_behaviour) {
                    case 0: powerLevel = 0; break;
                    case 1: powerLevel = 15; pulseTicks = 5; break;
                    case 2: powerLevel = 0; pulseTicks = 5; break;
                    case 3: powerLevel = getBooleanValue() ? 15 : 0; break;
                    case 4: powerLevel = getBooleanValue() ? 0 : 15; break;
                    default: //Do nothing
                }
                prevBoolValue = getBooleanValue();
                updateBlock();
            }
        } else {
            //FIXME: Proper update mechanics for redstone output...
            if (getCurrentValue() != prevValue || needsUpdate) {
                switch (redstone_behaviour) {
                    case 0: powerLevel = 0; break;
                    case 1: powerLevel = 15; pulseTicks = 5; break;
                    case 2: powerLevel = 0; pulseTicks = 5; break;
                    case 3: powerLevel = Double.isNaN(getPercentageAsNumber()) ? (int)Math.ceil(Math.min(getCurrentValue(), 15)) : (int)Math.ceil(getPercentageAsNumber() * 15); break;
                    case 4: powerLevel = 15 - (Double.isNaN(getPercentageAsNumber()) ? (int)Math.ceil(Math.min(getCurrentValue(), 15)) : (int)Math.ceil(getPercentageAsNumber() * 15)); break;
                    case 5: {
                        powerLevel = Double.isNaN(getPercentageAsNumber()) ? (int)Math.round(Math.min(15, Math.abs(getCurrentValue() - prevValue))) : (int)Math.round(Math.min(15, Math.abs(getPercentageAsNumber() - (prevValue / getMaxValue())) * 15));
                        pulseTicks = 5;
                        break;
                    }
                    default: //Do nothing
                }
                prevValue = getCurrentValue();
                updateBlock();
            }
        }
        if (needsUpdate) needsUpdate = false;
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

}
