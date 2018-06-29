package com.mctechnicguy.aim.tileentity;

import net.minecraft.init.Items;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityXPRelay extends TileEntityAIMDevice implements IItemHandler, IHasOwnInventory {

	@Nonnull
    private ItemStack bottleStack = ItemStack.EMPTY;
    private static final int XP_PER_BOTTLE = 8;
	
	public TileEntityXPRelay() {}

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        try {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) this;
        } catch (ClassCastException x) {
            return super.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.bottleStack = new ItemStack(Items.GLASS_BOTTLE, nbt.getInteger("bottleCount"));
	}

	@Nonnull
    @Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		nbt.setInteger("bottleCount", this.bottleStack == ItemStack.EMPTY ? 0 : this.bottleStack.getCount());
		return nbt;
	}

	@Override
	public int getSlots() {
		return 2;
	}

	@Nonnull
    @Override
	public ItemStack getStackInSlot(int slotID) {
		if (slotID == 0) {
			return bottleStack;
		}
		return this.isCoreActive() && this.getDeviceMode() == 1 ? this.getAvailableXPBottles() : ItemStack.EMPTY;
	}

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!isCoreActive() || !canInsertItem(slot, stack)) return stack;
        if (slot == 0) {
            if (bottleStack.isEmpty()) bottleStack = new ItemStack(Items.GLASS_BOTTLE, 0);
            int toInsert = Math.min(stack.getCount(), 64 - bottleStack.getCount());
            if (!simulate) bottleStack.grow(toInsert);
            return stack.splitStack(stack.getCount() - toInsert);
        }
        if (stack.getItem() instanceof ItemExpBottle && !simulate) {
            addXPToPlayer(stack.getCount());
            if (this.bottleStack.isEmpty()) this.bottleStack = new ItemStack(Items.GLASS_BOTTLE, stack.getCount());
            else this.bottleStack.grow(stack.getCount());
            if (this.bottleStack.getCount() > this.bottleStack.getMaxStackSize()) this.bottleStack.setCount(this.bottleStack.getMaxStackSize());
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int size, boolean simulate) {
        if (!isCoreActive() || !canExtractItem(slot)) return ItemStack.EMPTY;
        if (slot == 0) {
            if (this.getDeviceMode() == 0) {
                if (simulate) return bottleStack.copy().splitStack(size);
                return this.bottleStack.splitStack(size);
            }
            else return ItemStack.EMPTY;
        } else if (this.getDeviceMode() == 1 && !this.bottleStack.isEmpty() && this.bottleStack.getCount() > 0) {
            ItemStack stack = this.getAvailableXPBottles();
            if (stack.isEmpty()) return ItemStack.EMPTY;
            int amount = removeXPFromPlayer(Math.min(size, stack.getCount()), simulate);
            if (!simulate) {
                this.bottleStack.shrink(amount);
                if (bottleStack.getCount() <= 0) bottleStack = ItemStack.EMPTY;
            }
            return new ItemStack(Items.EXPERIENCE_BOTTLE, amount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    private boolean canInsertItem(int index, @Nonnull ItemStack stack) {
        if (index == 0) return stack.getItem() instanceof ItemGlassBottle && this.getDeviceMode() == 1;
        else return this.getDeviceMode() == 0 && stack.getItem() instanceof ItemExpBottle;
    }

    private boolean canExtractItem(int index) {
        if (index == 0) return this.getDeviceMode() == 0 && !bottleStack.isEmpty() && this.bottleStack.getCount() > 0;
        else return this.getDeviceMode() == 1 && !this.bottleStack.isEmpty() && this.bottleStack.getCount() > 0;
    }

    @Nonnull
	private ItemStack getAvailableXPBottles() {
		if (bottleStack.isEmpty() || bottleStack.getCount() <= 0 || !isCoreActive() || !(getPlayer().experienceTotal >= XP_PER_BOTTLE)) return ItemStack.EMPTY;
        else return new ItemStack(Items.EXPERIENCE_BOTTLE,  currentPlayerXP() / XP_PER_BOTTLE);
	}

    private int removeXPFromPlayer(int xpBottleAmount, boolean simulate) {
        if (!isCoreActive()) return 0;
        else {
            int max_remove = currentPlayerXP();
            int toRemove = xpBottleAmount * XP_PER_BOTTLE;

            if (toRemove > max_remove)
            {
                toRemove = max_remove;
            }
            if (!simulate) {
                getPlayer().addScore(-toRemove);
                int newTotal = currentPlayerXP() - toRemove;
                getPlayer().experienceTotal = newTotal;
                getPlayer().experienceLevel = getLevelForXP(newTotal);
                float xpDiff = newTotal - xpForLevel(getPlayer().experienceLevel);
                float xpToNextLevel = xpForLevel(getPlayer().experienceLevel + 1) - xpForLevel(getPlayer().experienceLevel);
                getPlayer().experience = xpDiff / xpToNextLevel;
            }
            return toRemove / XP_PER_BOTTLE;
        }
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

    private void addXPToPlayer(int xpBottleAmount) {
		if (!isCoreActive()) return;
        int toAdd = ((int)(Math.random() * 9 + 3)) * xpBottleAmount;
		getPlayer().addScore(toAdd);
		int newTotal = currentPlayerXP() + toAdd;
		getPlayer().experienceTotal = newTotal;
		getPlayer().experienceLevel = getLevelForXP(newTotal);
		float xpDiff = newTotal - xpForLevel(getPlayer().experienceLevel);
		float xpToNextLevel = xpForLevel(getPlayer().experienceLevel + 1) - xpForLevel(getPlayer().experienceLevel);
		getPlayer().experience = xpDiff / xpToNextLevel;
    }

    @Override
    public NonNullList<ItemStack> getOwnInventoryContent() {
        if (world.isRemote && !hasAccurateServerInfo) return null;
        else return NonNullList.withSize(1, this.bottleStack);
    }

    @Override
    public ItemStack getStackInOwnInventorySlot(int slot) {
        return this.bottleStack;
    }

    @Override
    public int getOwnInventorySize() {
        return 1;
    }

    @Override
    public NBTTagCompound getTagForOverlayUpdate() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("bottleCount", this.bottleStack.getCount());
        return nbt;
    }

    @Override
    public void handleTagForOverlayUpdate(NBTTagCompound nbt) {
        if (nbt.getInteger("bottleCount") > 0) {
            this.bottleStack = new ItemStack(Items.GLASS_BOTTLE, nbt.getInteger("bottleCount"));
        } else this.bottleStack = ItemStack.EMPTY;
        this.hasAccurateServerInfo = true;
    }

}
