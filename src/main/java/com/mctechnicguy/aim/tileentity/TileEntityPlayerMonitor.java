package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;

public class TileEntityPlayerMonitor extends TileEntityAIMDevice implements ITickable{

    public EnumMode mode = EnumMode.HEALTH;
    private int redstoneBehaviour;
    private int powerLevel;
    private double prevValue;
    private int pulseTicks;
    private boolean needsUpdate;
    private boolean lastCoreActive;

    private double prevPlayerPosX;
    private double prevPlayerPosY;
    private double prevPlayerPosZ;

    private double clientValue;
    private double clientMaxValue;
    private double clientPercentageValue;

    private String playerConnectedName;

    private int ticksToNextUpdate = 20;
    private boolean updateQueued;

    @Nonnull
    private DecimalFormat doubleRound = new DecimalFormat("#.##");

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        mode = EnumMode.fromID(nbt.getInteger("MonitorMode"));
        redstoneBehaviour = nbt.getInteger("RedstoneBehaviour");
        needsUpdate = true;
    }

    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = getUpdateTag();
        this.writePacketCoreData(nbtTag);
        return new SPacketUpdateTileEntity(this.getPos(), 0, nbtTag);
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbtTag =  super.getUpdateTag();
        nbtTag.setInteger("RedstoneBehaviour", redstoneBehaviour);
        nbtTag.setInteger("MonitorMode", mode.getID());
        if (isCoreActive()) {
            nbtTag.setString("playerConnectedName", this.getCore().playerConnectedName);
            nbtTag.setDouble("MonitorValue", this.getCurrentValue());
            if (mode.hasMaxValue()) {
                nbtTag.setDouble("MonitorMaxValue", this.getMaxValue());
            }
            if (!Double.isNaN(getPercentageAsNumber())) {
                nbtTag.setDouble("MonitorPercentage", this.getPercentageAsNumber());
            }
        }
        return nbtTag;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        this.handleUpdateTag(packet.getNbtCompound());
    }


    private void handleServerUpdateTag(NBTTagCompound tag) {
        mode = EnumMode.fromID(tag.getInteger("MonitorMode"));
        redstoneBehaviour = tag.getInteger("RedstoneBehaviour");
        playerConnectedName = tag.getString("playerConnectedName");
        if (tag.hasKey("MonitorValue")) {
            this.clientValue = tag.getDouble("MonitorValue");
            if (tag.hasKey("MonitorMaxValue")) {
                this.clientMaxValue = tag.getDouble("MonitorMaxValue");
            }
            if (tag.hasKey("MonitorPercentage")) {
                this.clientPercentageValue = tag.getDouble("MonitorPercentage");
            }
        }
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        this.handleServerUpdateTag(tag);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setInteger("RedstoneBehaviour", redstoneBehaviour);
        if (mode != null) {
            nbt.setInteger("MonitorMode", mode.getID());
        }
        return nbt;
    }


    private double getMaxValue() {
        if (!isCoreActive() || getPlayer() == null) return 0D;
        switch (mode) {
            case HEALTH: return (double)getPlayer().getMaxHealth();
            case HUNGER: return 20D; //Maximum food level
            case SATURATION: return 20D; //Maximum saturation level
            case AIR: return 300D; //Maximum air value (in Ticks)
            case ARMOR: return 20D; //Maximum armor value (should be...)
            case SELECTEDSLOT: return (double)InventoryPlayer.getHotbarSize();
            default: return Double.NaN;
        }
    }


    private double getCurrentValue() {
        if (!isCoreActive() || getPlayer() == null) return 0D;
        switch (mode) {
            case HEALTH: return getPlayer().getHealth();
            case XP: return getPlayer().experienceLevel;
            case HUNGER: return getPlayer().getFoodStats().getFoodLevel();
            case SATURATION: return getPlayer().getFoodStats().getSaturationLevel();
            case AIR: return Math.max(0, getPlayer().getAir());
            case MOTIONX: return (getPlayer().posX - this.prevPlayerPosX) * 20;
            case MOTIONY: return (getPlayer().posY - this.prevPlayerPosY) * 20;
            case MOTIONZ: return (getPlayer().posZ - this.prevPlayerPosZ) * 20;
            case ARMOR: return getPlayer().getTotalArmorValue();
            case POSX: return getPlayer().posX;
            case POSY: return getPlayer().posY;
            case POSZ: return getPlayer().posZ;
            case ISBURNING: return getPlayer().isBurning() ? 1D : 0D;
            case ISINWATER: return getPlayer().isInWater() ? 1D : 0D;
            case ISINLAVA: return getPlayer().isInLava() ? 1D : 0D;
            case ISAIRBORNE: return !getPlayer().onGround ? 1D : 0D;
            case ISSNEAKING: return getPlayer().isSneaking() ? 1D : 0D;
            case ISSPRINTING: return getPlayer().isSprinting() ? 1D : 0D;
            case ISFALLING: return getPlayer().fallDistance > 0D ? 1D : 0D;
            case SELECTEDSLOT: return getPlayer().inventory.currentItem + 1;
            case DIMENSION: return getPlayer().dimension;
            default: return Double.NaN;
        }
    }

    private double getPercentageAsNumber() {
        if (!isCoreActive() || !mode.hasPercentage() || getMaxValue() <= 0) return Double.NaN;
        else return getCurrentValue() / getMaxValue();
    }

    public int getDeviceMode() {
        return mode.getID();
    }

    public void setDeviceMode(int m) {
        mode = EnumMode.fromID(m);
        needsUpdate = true;
    }

    public int getRedstoneBehaviour() {
        return redstoneBehaviour;
    }

    public void setRedstoneBehaviour(int redstoneBehaviour) {
        this.redstoneBehaviour = redstoneBehaviour;
        needsUpdate = true;
    }

    private void resetToDefaults() {
        prevValue = getCurrentValue();
        if (isCoreActive()) {
            prevPlayerPosX = getPlayer().posX;
            prevPlayerPosY = getPlayer().posY;
            prevPlayerPosZ = getPlayer().posZ;
        }
        pulseTicks = 0;
        powerLevel = 0;
        setStaticRedstoneOutput();
        queueUpdate();
        needsUpdate = false;
    }

    private int getCurrentRedstoneValue() {
        if (mode.isBooleanValue()) {
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
            if (mode.isBooleanValue()) {
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
            queueUpdate();
        }

        ticksToNextUpdate--;
        if (ticksToNextUpdate <= 0) {
            ticksToNextUpdate = 20;
            if (updateQueued) this.updateBlock();
        }

        prevPlayerPosX = getPlayer().posX;
        prevPlayerPosY = getPlayer().posY;
        prevPlayerPosZ = getPlayer().posZ;

    }

    private void queueUpdate() {
        this.updateQueued = true;
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

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatusInformation(NetworkInfoOverlayRenderer renderer) {
        super.renderStatusInformation(renderer);
        renderer.renderModeString("mode.monitor." + mode.getName());
        renderer.renderTileValues("redstonemode", TextFormatting.AQUA, false, I18n.format("rsmode.monitor." + this.redstoneBehaviour));
    }

    @SideOnly(Side.CLIENT)
    public String getFormattedValue() {
        Object[] params;
        if (mode.hasMaxValue()) {
            if (mode.isBooleanValue()) {
                params = new Object[]{I18n.format(clientValue == 1D ? "mode.monitordisplay.boolean.true" : "mode.monitordisplay.boolean.false"), doubleRound.format(clientMaxValue)};
            } else {
                params = new Object[]{doubleRound.format(clientValue), doubleRound.format(clientMaxValue)};
            }
        } else {
            if (mode.isBooleanValue()) {
                params = new Object[]{I18n.format(clientValue == 1D ? "mode.monitordisplay.boolean.true" : "mode.monitordisplay.boolean.false")};
            } else {
                params = new Object[]{doubleRound.format(clientValue)};
            }
        }
        return I18n.format("mode.monitordisplay." + mode.getName() + ".format", params);
    }

    @SideOnly(Side.CLIENT)
    public String getPercentageFormatted() {
        if (!mode.hasPercentage()) return null;
        return I18n.format("mode.monitordisplay.percent", doubleRound.format(clientPercentageValue * 100)) + "%";
    }

    @SideOnly(Side.CLIENT)
    public String getPlayerConnectedName() {
        return playerConnectedName;
    }

    public enum EnumMode implements IStringSerializable {

        HEALTH(0, "health", true, false, true, 5),
        XP(1, "xp", false, false, false, 5),
        HUNGER(2, "hunger", true, false, true, 5),
        SATURATION(3, "saturation", true, false, true, 5),
        AIR(4, "air", true, false, true, 5),
        MOTIONX(5, "motionx", false, false, false, 5),
        MOTIONY(6, "motiony", false, false, false, 5),
        MOTIONZ(7, "motionz", false, false, false, 5),
        ARMOR(8, "armor", true, false, true, 5),
        POSX(9, "posx", false, false, false, 2),
        POSY(10, "posy", false, false, false, 2),
        POSZ(11, "posz", false, false, false, 2),
        ISBURNING(12, "isburning", false, true, false, 4),
        ISINWATER(13, "isinwater", false, true, false, 4),
        ISINLAVA(14, "isinlava", false, true, false, 4),
        ISAIRBORNE(15, "isairborne", false, true, false, 4),
        ISSNEAKING(16, "issneaking", false, true, false, 4),
        ISSPRINTING(17, "issprinting", false, true, false, 4),
        ISFALLING(18, "isfalling", false, true, false, 4),
        SELECTEDSLOT(19, "selectedslot", true, false, false, 5),
        DIMENSION(20, "dimension", false, false, false, 2);

        private int id;
        private String name;
        private boolean hasMaxValue;
        private boolean isBooleanValue;
        private boolean hasPercentage;

        //0 = Deactivated, 1 = Emit pulse on change, 2= Emit pulse on Change (Inverted), 3 = Show Status, 4 = Show status (inverted), 5 = Emit last percentage change as pulse
        private int maxRSMode;

        EnumMode(int id, String name, boolean hasMaxValue, boolean isBooleanValue, boolean hasPercentage, int maxRSMode) {
            this.id = id;
            this.name = name;
            this.hasMaxValue = hasMaxValue;
            this.isBooleanValue = isBooleanValue;
            this.maxRSMode = maxRSMode;
            this.hasPercentage = hasPercentage;
        }

        public boolean hasPercentage() {
            return hasPercentage;
        }

        public int getMaxRSMode() {
            return maxRSMode;
        }

        @Override
        public String getName() {
            return name;
        }

        public int getID() {
            return id;
        }

        public boolean hasMaxValue() {
            return hasMaxValue;
        }

        public boolean isBooleanValue() {
            return isBooleanValue;
        }


        public static TileEntityPlayerMonitor.EnumMode fromID(int id) {
            return values()[id];
        }
    }

}
