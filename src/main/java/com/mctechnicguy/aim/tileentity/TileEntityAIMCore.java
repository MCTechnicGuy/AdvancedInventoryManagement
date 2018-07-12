package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.blocks.BlockAIMCore;
import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import com.mctechnicguy.aim.items.ItemAIMUpgrade;
import com.mctechnicguy.aim.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Optional.InterfaceList({
		@Optional.Interface(iface="net.darkhax.tesla.api.ITeslaConsumer", modid="tesla"),
		@Optional.Interface(iface="ic2.api.energy.tile.IEnergySink", modid="ic2")
})
public class TileEntityAIMCore extends TileEntity implements IInventory, ITickable, IEnergyStorage, IProvidesNetworkInfo, net.darkhax.tesla.api.ITeslaConsumer, ic2.api.energy.tile.IEnergySink {

	private static final float BASE_MAX_POWER = 2000000F;
	private static final float BASE_MAX_POWER_DRAIN = 50000F;

	public int Power;
    private int lastPower;
    private int currentPowerInput;

    //Client-Side-Variables, not used on the server!
    private int maxPower;
    private int powerDrain;
    private boolean hasAccurateServerInfo;
    public boolean playerAccessible;

	private int EU_energy_buffer;
    private int NumberCablesConnected;
    private int NumberDevicesConnected;
	private int NumberBridgesConnected;
	private int NumberGeneratorsConnected;

	private boolean active;
	private boolean lastPlayerCheckSuccess;
	private boolean lastStructureCheckSuccess;
	private int TickCounter = Math.round(AdvancedInventoryManagement.CORE_UPDATE_TIME * 0.9F);
	private boolean firstUpdate;

	@Nonnull
	public List<TileEntityNetworkElement> registeredDevices = new ArrayList<>();

	@Nonnull
	private NonNullList<ItemStack> slots = NonNullList.withSize(5, ItemStack.EMPTY);

	@Nullable
	public UUID playerConnectedID;
	public String playerConnectedName;
	@Nullable
	private EntityPlayer currentPlayerConnected;
	private boolean isRegistered;

	@CapabilityInject(net.darkhax.tesla.api.ITeslaConsumer.class)
	private static Capability<net.darkhax.tesla.api.ITeslaConsumer> POWER_STORAGE_CAP = null;

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY || (POWER_STORAGE_CAP != null && capability == POWER_STORAGE_CAP) || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (POWER_STORAGE_CAP != null && capability == POWER_STORAGE_CAP) return (T) this;
		if (capability == CapabilityEnergy.ENERGY) return (T) this;
		return super.getCapability(capability, facing);
	}

	/**
	 * Implementation for the Tesla-Api Power Consumption
     */
	public long givePower(long powerOffered, boolean simulated) {
		long powerToAccept = Math.min(powerOffered, Math.min(MaxPower() - Power, MaxPowerDrain()));
		if (!simulated) Power += powerToAccept;
		return powerToAccept;
	}

	/**
	 * Implementation for the IC2-Api Power Consumption
	 */
	@Optional.Method(modid = "ic2")
	public double getDemandedEnergy() {
		return Math.min(MaxPower() - Power, MaxPowerDrain()) * AdvancedInventoryManagement.EU_TO_RF;
	}

	@Optional.Method(modid = "ic2")
	public int getSinkTier() {
		return 4;
	}

	@Optional.Method(modid = "ic2")
	public double injectEnergy(EnumFacing enumFacing, double amount, double voltage) {
		if (amount < AdvancedInventoryManagement.EU_TO_RF) {
			EU_energy_buffer += amount;
			if (EU_energy_buffer >= AdvancedInventoryManagement.EU_TO_RF) {
				EU_energy_buffer -= AdvancedInventoryManagement.EU_TO_RF;
				amount = AdvancedInventoryManagement.EU_TO_RF;
				double powerToAccept = Math.min(amount / AdvancedInventoryManagement.EU_TO_RF, Math.min(MaxPower() - Power, MaxPowerDrain()));
				Power += powerToAccept;
			}
		} else {
			double powerToAccept = Math.min(amount / AdvancedInventoryManagement.EU_TO_RF, Math.min(MaxPower() - Power, MaxPowerDrain()));
			Power += powerToAccept;
		}
		return 0;
	}

	@Optional.Method(modid="ic2")
	public boolean acceptsEnergyFrom(ic2.api.energy.tile.IEnergyEmitter iEnergyEmitter, EnumFacing enumFacing) {
		return true;
	}

	@Optional.Method(modid = "ic2")
	private void register()
	{
		if(!world.isRemote)
		{
			ic2.api.energy.tile.IEnergyTile registered = ic2.api.energy.EnergyNet.instance.getTile(world, getPos());

			if(registered != this)
			{
				if(registered != null)
				{
					MinecraftForge.EVENT_BUS.post(new ic2.api.energy.event.EnergyTileUnloadEvent(registered));
				}
				else
				{
					MinecraftForge.EVENT_BUS.post(new ic2.api.energy.event.EnergyTileLoadEvent(this));
				}
			}
		}
		isRegistered = true;
	}

	@Optional.Method(modid = "ic2")
	private void deregister()
	{
		if(!world.isRemote)
		{
			ic2.api.energy.tile.IEnergyTile registered = ic2.api.energy.EnergyNet.instance.getTile(world, getPos());

			if(registered != null)
			{
				MinecraftForge.EVENT_BUS.post(new ic2.api.energy.event.EnergyTileUnloadEvent(registered));
			}
		}
		isRegistered = false;
	}

	@Override
	public void onChunkUnload() {
		this.setIsActive(false);
		if (ModCompatHelper.isIC2Loaded) deregister();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (ModCompatHelper.isIC2Loaded) deregister();
	}


	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		firstUpdate = true;

		if (!nbt.getString("playeruuid").isEmpty()) {
			this.playerConnectedID = UUID.fromString(nbt.getString("playeruuid"));
		}
		if (!nbt.getString("playername").isEmpty()) {
			this.playerConnectedName = nbt.getString("playername");
		}

		this.slots = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);

		ItemStackHelper.loadAllItems(nbt, slots);
		this.Power = nbt.getInteger("power");
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		if (this.playerConnectedID != null) {
			nbt.setString("playeruuid", this.playerConnectedID.toString());
		}
		if (this.playerConnectedName != null) {
			nbt.setString("playername", this.playerConnectedName);
		}

		ItemStackHelper.saveAllItems(nbt, slots);

		nbt.setInteger("power", this.Power);

		return nbt;
	}


	void setConnectedPlayer(@Nonnull EntityPlayer newPlayer) {
		this.playerConnectedID = newPlayer.getUniqueID();
		this.playerConnectedName = newPlayer.getDisplayNameString();
		TickCounter = AdvancedInventoryManagement.CORE_UPDATE_TIME;
		NBTUtils.writeToNBTFile(this, playerConnectedID);
		this.updateBlock();
	}

	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		nbtTag.setBoolean("active", isActive());
		if (playerConnectedName != null && !playerConnectedName.isEmpty())
			nbtTag.setString("pName", playerConnectedName);
		if (playerConnectedID != null)
			nbtTag.setString("pID", playerConnectedID.toString());
		return new SPacketUpdateTileEntity(pos, 0, nbtTag);
	}

	@SideOnly(Side.CLIENT)
	public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity packet) {
		this.active = packet.getNbtCompound().getBoolean("active");
		this.playerConnectedName = packet.getNbtCompound().getString("pName");
		if (!packet.getNbtCompound().getString("pID").isEmpty()) {
			this.playerConnectedID = UUID.fromString(packet.getNbtCompound().getString("pID"));
		} else this.playerConnectedID = null;
		if (hasWorld()) world.markBlockRangeForRenderUpdate(pos, pos);
	}

	@Override
    @Nonnull
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbtTag = super.getUpdateTag();
		nbtTag.setBoolean("active", isActive());
		if (playerConnectedName != null && !playerConnectedName.isEmpty())
			nbtTag.setString("pName", playerConnectedName);
		if (playerConnectedID != null)
			nbtTag.setString("pID", playerConnectedID.toString());
		return nbtTag;
	}

	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		this.setIsActive(tag.getBoolean("active"));
		this.playerConnectedName = tag.getString("pName");
		if (!tag.getString("pID").isEmpty()) {
			this.playerConnectedID = UUID.fromString(tag.getString("pID"));
		} else this.playerConnectedID = null;
		if (hasWorld()) world.markBlockRangeForRenderUpdate(pos, pos);
	}

	private int MaxPowerDrain() {
		if (!this.hasUpgrade(2))
			return (int) BASE_MAX_POWER_DRAIN;
		else
			return (int) (BASE_MAX_POWER_DRAIN
					+ BASE_MAX_POWER_DRAIN * (Math.min(this.getUpgradeCount(2), 16) / 4F));
	}

	public int MaxPower() {
		if (!this.hasUpgrade(2))
			return (int) BASE_MAX_POWER;
		else
			return (int) (BASE_MAX_POWER + (BASE_MAX_POWER*0.5625)* Math.min(this.getUpgradeCount(2), 16));
	}

	private float getUpgradeCount(int i) {
		float count = 0;
		for (ItemStack stack : this.slots) {
			if (!stack.isEmpty() && stack.getItem() instanceof ItemAIMUpgrade && stack.getItemDamage() == i) {
				count += stack.getCount();
			}
		}
		return count;
	}

	@Override
	public void update() {

		if (this.hasWorld() && !this.world.isRemote) {

			if (ModCompatHelper.isIC2Loaded && !isRegistered) register();

			this.currentPowerInput = this.Power - this.lastPower;
			if (this.currentPowerInput < 0) this.currentPowerInput = 0;

			if (Power > 0 && this.active)
				this.Power -= this.getNetworkPowerDrain();
			if (Power < 0)
				Power = 0;

            this.lastPower = Power;

			this.rechargeFromItem();

			if (this.TickCounter >= AdvancedInventoryManagement.CORE_UPDATE_TIME) {
				if (firstUpdate) {
					updateBlock();
					firstUpdate = false;
				}

				lastStructureCheckSuccess = this.searchForDevicesInNetwork();
				lastPlayerCheckSuccess = this.getConnectedPlayer() != null;
				setIsActive(lastStructureCheckSuccess && lastPlayerCheckSuccess && hasEnoughPower()
						&& !(this.hasUpgrade(1) && world.isBlockIndirectlyGettingPowered(pos) > 0));
				
				TickCounter = 0;
			} else {
				this.TickCounter++;
				this.setIsActive(lastStructureCheckSuccess && lastPlayerCheckSuccess && hasEnoughPower()
						&& !(this.hasUpgrade(1) && world.isBlockIndirectlyGettingPowered(pos) > 0) && !isPlayerDead());
			}
		}
	}

	private boolean isPlayerDead() {
		if (currentPlayerConnected == null) return false;
		else return currentPlayerConnected.isDead || currentPlayerConnected.deathTime > 0;
    }

	private boolean hasEnoughPower() {
		if (!AdvancedInventoryManagement.DOES_USE_POWER) return true;
		return isActive() ? Power > 0 : Power > getNetworkPowerDrain() * 5; //It takes 5 times the needed Power to start, but it will only shutdown when the power reaches 0.
	}

	private void rechargeFromItem() {
		if (!slots.get(4).isEmpty() && slots.get(4).hasCapability(CapabilityEnergy.ENERGY, null)) {
			IEnergyStorage i = slots.get(4).getCapability(CapabilityEnergy.ENERGY, null);
			if (i != null) {
				if (i.getEnergyStored() != 0 && this.Power < MaxPower()) {
					this.Power += i.extractEnergy(MaxPower() - Power > MaxPowerDrain() ? MaxPowerDrain() : MaxPower() - Power, false);
				}
			}
		}
	}

	public boolean isActive() {
		return active;
	}

	private void setIsActive(boolean isActive) {
		if (isActive != active) {
			active = isActive;
			this.updateBlock();
			for (TileEntityNetworkElement te : this.registeredDevices) {
				te.updateBlock();
			}
		}
	}

	private void updateBlock() {
		if (this.hasWorld() && !world.isRemote) {
			world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 3);
		}
		this.markDirty();
	}

	public int getNetworkPowerDrain() {
		if (!AdvancedInventoryManagement.DOES_USE_POWER)
			return 0;
		return AdvancedInventoryManagement.BASE_POWER_USAGE
				+ (AdvancedInventoryManagement.POWER_PER_CABLE * NumberCablesConnected)
				+ (AdvancedInventoryManagement.POWER_PER_MACHINE * NumberDevicesConnected)
                + (AdvancedInventoryManagement.POWER_PER_BRIDGE * NumberBridgesConnected);
	}

	/**
	 * Searches for Devices connected to the core
	 * @return true if the search ended successfully.
	 */
	public boolean searchForDevicesInNetwork() {
		List<WorldXYZNetworkCoordinate> checked = new ArrayList<>();
		List<TileEntityNetworkElement> newDevices = new ArrayList<>();
		LinkedList<WorldXYZNetworkCoordinate> queue = new LinkedList<>();
		WorldXYZNetworkCoordinate startCoord = new WorldXYZNetworkCoordinate(pos, 0, world);
		NumberCablesConnected = 0;
		NumberDevicesConnected = 0;
		NumberGeneratorsConnected = 0;
		NumberBridgesConnected = 0;
		queue.add(startCoord);
		checked.add(startCoord);

		while (!queue.isEmpty()) {

			WorldXYZNetworkCoordinate examinedBlock = queue.poll();


			ArrayList<WorldXYZNetworkCoordinate> neighbors = new ArrayList<>();
			for (EnumFacing face : EnumFacing.VALUES) {
			    neighbors.add(new WorldXYZNetworkCoordinate(examinedBlock.getPos().offset(face),
                        examinedBlock.getSearchIndex() + 1, world, face));
            }
            if (examinedBlock.getTileInWorld() instanceof TileEntityNetworkSignalBridge) {
			    TileEntityNetworkSignalBridge bridge = (TileEntityNetworkSignalBridge)examinedBlock.getTileInWorld();
			    if (bridge.getDestination() != null) {
			        neighbors.add(new WorldXYZNetworkCoordinate(bridge.getDestination(), examinedBlock.getSearchIndex() + 1, world));
                }
            }

			for (WorldXYZNetworkCoordinate target : neighbors) {
				if (checked.contains(target)) continue;

				if (examinedBlock.isCable() &&
						!((TileEntityNetworkCable)examinedBlock.getTileInWorld()).canTransferSignal(target.getAttachedOnFace(), false)) continue;
				
				if (target.isCable() &&
						!((TileEntityNetworkCable)target.getTileInWorld()).canTransferSignal(target.getAttachedOnFace().getOpposite(), true)) continue;

				checked.add(target);
				if (target.getBlockInWorld(this.world) instanceof BlockAIMCore) {
					endSearch(newDevices);
					return false;
				}

				if (NetworkUtils.isNetworkCable(target.getBlockInWorld(world))) {
					this.NumberCablesConnected++;
					if (target.isCable()) {
						newDevices.add((TileEntityNetworkElement) target.getTileInWorld());
					}
					if (this.NumberCablesConnected > AdvancedInventoryManagement.MAX_NETWORK_CABLES) {
						endSearch(newDevices);
						return false;
					}
				} else if (NetworkUtils.isNetworkDevice(target.getBlockInWorld(world))) {
					if (NetworkUtils.isNetworkBridge(target.getBlockInWorld(world))) {
					    this.NumberBridgesConnected++;
                    } else if (NetworkUtils.isGenerator(target.getBlockInWorld(world))) {
					    this.NumberGeneratorsConnected++;
                    } else {
                        this.NumberDevicesConnected++;
                    }
					if (target.getTileInWorld() instanceof TileEntityNetworkElement) {
						newDevices.add((TileEntityNetworkElement) target.getTileInWorld());
					}

					if (this.numberDevicesConnected() > AdvancedInventoryManagement.MAX_NETWORK_ELEMENTS) {
						endSearch(newDevices);
						return false;
					}
				}

				if (examinedBlock.getSearchIndex() >= AdvancedInventoryManagement.MAX_CABLE_LENGHT) {
					endSearch(newDevices);
					return false;
				} else if (NetworkUtils.isNetworkElement(target.getTileInWorld())) {
					queue.add(target);
				}
			}

		}
		endSearch(newDevices);
		return true;
	}

	public int numberDevicesConnected() {
	    return this.NumberDevicesConnected + this.NumberBridgesConnected + this.NumberGeneratorsConnected;
    }

    public int numberCablesConnected() {
	    return this.NumberCablesConnected;
    }

	private void endSearch(@Nonnull List<TileEntityNetworkElement> newDevices) {
		if (!world.isRemote) {
			registeredDevices.removeAll(newDevices);
			for (TileEntityNetworkElement te : registeredDevices) {
				te.setCore(null);
			}
		}
		registeredDevices.clear();
		registeredDevices.addAll(newDevices);
		if (!world.isRemote) {
			for (TileEntityNetworkElement te : registeredDevices) {
				te.setCore(this);
			}
		}
	}

	@Override
	public int getSizeInventory() {
		return this.slots.size();
	}

	@Override
	public boolean isEmpty()
	{
		for (ItemStack itemstack : this.slots)
		{
			if (!itemstack.isEmpty())
			{
				return false;
			}
		}

		return true;
	}

	@Nullable
	public EntityPlayer getConnectedPlayer() {
		if (this.playerConnectedID == null)
			return null;
		if (this.currentPlayerConnected == null || this.currentPlayerConnected.isDead)
			currentPlayerConnected = AIMUtils.getPlayerbyID(this.playerConnectedID);
		if (currentPlayerConnected != null && !currentPlayerConnected.isDead && currentPlayerConnected.deathTime <= 0
				&& AIMUtils.isPlayerAccessible(currentPlayerConnected)) {
			return currentPlayerConnected;
		}
		return null;
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int i) {
		return this.slots.get(i);
	}

	@Nonnull
	@Override
	public ItemStack decrStackSize(int slotNumber, int amount) {
		if (!this.slots.get(slotNumber).isEmpty()) {
			ItemStack itemstack;

			if (this.slots.get(slotNumber).getCount() <= amount) {
				itemstack = this.slots.get(slotNumber);
				this.slots.set(slotNumber, ItemStack.EMPTY);
				return itemstack;
			} else {
				itemstack = this.slots.get(slotNumber).splitStack(amount);

				if (this.slots.get(slotNumber).getCount() == 0) {
					this.slots.set(slotNumber, ItemStack.EMPTY);
				}

				return itemstack;
			}
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int i, @Nonnull ItemStack stack) {
		this.slots.set(i, stack);

		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}

		if (Power > MaxPower())
			Power = MaxPower();
	}

	@Nonnull
	@Override
	public String getName() {
		return "container.aimcore";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 16;
	}


	@Override
	public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
		return this.isPlayerAccessAllowed(entityplayer) && this.world.getTileEntity(this.pos) == this && entityplayer.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64.0D;
	}

	public boolean isPlayerAccessAllowed(@Nonnull EntityPlayer entityplayer) {
		if (!this.hasUpgrade(0) || this.playerConnectedID == null)
			return true;
		else if (!this.world.isRemote && this.hasUpgrade(0)
				&& this.playerConnectedID.compareTo(entityplayer.getUniqueID()) != 0) {
			AIMUtils.sendChatMessageWithArgs("message.coreaccess.false", entityplayer, TextFormatting.RED,
					TextFormatting.BLUE + this.playerConnectedName + "!");
			return false;
		}
		return entityplayer.getUniqueID().compareTo(playerConnectedID) == 0;
	}

	public boolean hasUpgrade(int meta) {
		for (int i = 0; i < 4; i++) {
			if (!slots.get(i).isEmpty())
				if (slots.get(i).getItem() instanceof ItemAIMUpgrade && slots.get(i).getItemDamage() == meta)
					return true;
		}
		return false;
	}

	@Override
	public void openInventory(@Nonnull EntityPlayer player) {
	}

	@Override
	public void closeInventory(@Nonnull EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
		if (slot < 4)
			return stack.getItem() instanceof ItemAIMUpgrade && !this.hasUpgrade(0);
		else
			return stack.hasCapability(CapabilityEnergy.ENERGY, null) && !this.hasUpgrade(0);
	}

	@SideOnly(Side.CLIENT)
	public int getPowerRemainingScaled(int scale) {
		return (int)Math.round(((double)this.Power / (double)this.MaxPower()) * scale);
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentTranslation(this.getName());
	}

	@Nonnull
	@Override
	public ItemStack removeStackFromSlot(int i) {
		if (!this.slots.get(i).isEmpty()) {
			ItemStack itemstack = this.slots.get(i);
			this.slots.set(i, ItemStack.EMPTY);
			return itemstack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public int getField(int id) {
		switch (id) {
		case 0:
			return this.Power;
		case 1:
			return this.NumberCablesConnected;
		case 2:
			return this.NumberDevicesConnected;
        case 3:
            return this.NumberBridgesConnected;
        case 4:
            return this.NumberGeneratorsConnected;
		default:
			return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch (id) {
		case 0:
			this.Power = value;
			break;
		case 1:
			this.NumberCablesConnected = value;
			break;
		case 2:
			this.NumberDevicesConnected = value;
			break;
        case 3:
            this.NumberBridgesConnected = value;
			break;
        case 4:
            this.NumberGeneratorsConnected = value;
            break;
		}
	}

	@Override
	public int getFieldCount() {
		return 5;
	}

	@Override
	public void clear() {}
	
	void changePower(int change) {
		if (AdvancedInventoryManagement.DOES_USE_POWER) {
			this.Power += change;
			if (this.Power > this.MaxPower()) this.Power = this.MaxPower();
			if (this.Power < 0) this.Power = 0;
		}
	}

	public void forceNetworkUpdate(int delay) {
		this.TickCounter = AdvancedInventoryManagement.CORE_UPDATE_TIME - delay;
		if (this.TickCounter < 0)
			this.TickCounter = 0;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (this.Power >= MaxPower())
			return 0;
		int drainedPower;
		if (maxReceive <= this.MaxPowerDrain())
			drainedPower = maxReceive;
		else
			drainedPower = this.MaxPowerDrain();

		if ((drainedPower) > (this.MaxPower() - this.Power))
			drainedPower = (this.MaxPower() - this.Power);

		if (simulate)
			return drainedPower;
		this.Power += drainedPower;
		if (this.Power >= this.MaxPower())
			this.Power = this.MaxPower();
		return drainedPower;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored() {
		return Power;
	}

	@Override
	public int getMaxEnergyStored() {
		return MaxPower();
	}

	@Override
	public boolean canExtract() {
		return false;
	}

	@Override
	public boolean canReceive() {
		return MaxPower() > Power;
	}

	@Override
    @SideOnly(Side.CLIENT)
	public String getNameForOverlay() {
		return I18n.format("tile.aimcore.name");
	}

    /**
     * Returns a flag containing all the reasons the network is currently not active
     * @return A bitwise flag containing these error values: 0: No errors, 1: No player connected, 2: Invalid network structure, 4: Not enough power, 8: Player inaccessible, 16: Redstone disabled
     */
	public short getProblemFlag() {
	    short flag = 0;
	    if (this.playerConnectedID == null) flag |= 1;
        if (!this.lastStructureCheckSuccess) flag |= 2;
        if (!hasEnoughPower()) flag |= 4;
        if (getConnectedPlayer() == null) flag |= 8;
        if (this.hasUpgrade(1) && world.isBlockIndirectlyGettingPowered(pos) > 0) flag |= 16;
	    return flag;
    }

	@Override
    @SideOnly(Side.CLIENT)
	public void renderStatusInformation(NetworkInfoOverlayRenderer renderer) {
	    renderer.renderStatusString(active);
        renderer.renderInventoryContent(hasAccurateServerInfo ? slots : null);
        renderer.renderTileValues("power", TextFormatting.GREEN, !hasAccurateServerInfo, Power, maxPower);
        renderer.renderTileValues("powerchange", TextFormatting.YELLOW, !hasAccurateServerInfo, currentPowerInput, powerDrain);
        renderer.renderTileValues("playerconnected", TextFormatting.AQUA, !hasAccurateServerInfo, playerConnectedName);
	}

	@Nullable
	@Override
	public NBTTagCompound getTagForOverlayUpdate() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("Power", this.Power);
		nbt.setInteger("PowerDrain", active ? this.getNetworkPowerDrain() : 0);
		nbt.setInteger("MaxPower", this.MaxPower());
		nbt.setInteger("PowerInput", this.currentPowerInput);
        ItemStackHelper.saveAllItems(nbt, slots);
        return nbt;
	}

	@Override
	public void handleTagForOverlayUpdate(NBTTagCompound nbt) {
        this.Power = nbt.getInteger("Power");
        this.maxPower = nbt.getInteger("MaxPower");
        this.powerDrain = nbt.getInteger("PowerDrain");
        this.currentPowerInput = nbt.getInteger("PowerInput");
        ItemStackHelper.loadAllItems(nbt, slots);
        this.hasAccurateServerInfo = true;
	}

    @Override
    public void invalidateServerInfo() {
        this.hasAccurateServerInfo = false;
    }
}
