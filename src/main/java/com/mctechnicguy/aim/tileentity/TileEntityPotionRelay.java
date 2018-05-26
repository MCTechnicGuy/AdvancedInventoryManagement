package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModElementList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collection;

public class TileEntityPotionRelay extends TileEntityAIMDevice implements IItemHandler{

    @Nonnull
	private ItemStack bottleStack = ItemStack.EMPTY;
	
	public TileEntityPotionRelay() {}

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
	public int getSlots() {
		switch(this.getDeviceMode()) {
			case 0: return 2;
			case 1: return (this.isCoreActive() ? (1 + getValidPotionEffects().size()) : 1);
			default: return 2;
		}
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
        if (stack.getItem() instanceof ItemPotion && !simulate) {
            for (PotionEffect eff : PotionUtils.getEffectsFromStack(stack)) {
                if (eff.getDuration() == 0) eff = new PotionEffect(eff.getPotion(), 1, eff.getAmplifier(), eff.getIsAmbient(), eff.doesShowParticles());
                this.getPlayer().addPotionEffect(eff);
            }
            if (this.bottleStack.isEmpty()) this.bottleStack = new ItemStack(Items.GLASS_BOTTLE);
            else this.bottleStack.grow(1);
            if (this.bottleStack.getCount() > this.bottleStack.getMaxStackSize()) this.bottleStack.setCount(bottleStack.getMaxStackSize());
        } else {
            if (!simulate) this.getPlayer().curePotionEffects(stack);
        }
        return ItemStack.EMPTY;
	}

	@Nonnull
    @Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!isCoreActive() || !canExtractItem(slot)) return ItemStack.EMPTY;
        if (slot == 0) {
            if (this.getDeviceMode() == 0) {
                if (simulate) return bottleStack.copy().splitStack(amount);
                return this.bottleStack.splitStack(amount);
            }
            else return ItemStack.EMPTY;
        } else if (this.getDeviceMode() == 1 && !this.bottleStack.isEmpty() && this.bottleStack.getCount() > 0) {
            ItemStack stack = this.getPotionItemFromEffect(slot - 1);
            if (!simulate) {
                PotionEffect effect = (PotionEffect) this.getPlayer().getActivePotionEffects().toArray()[slot - 1];
                this.getPlayer().removePotionEffect(effect.getPotion());
                this.bottleStack.shrink(1);
                if (AdvancedInventoryManagement.DOES_USE_POWER) this.getCore().changePower(-AdvancedInventoryManagement.POWER_PER_POTION_GENERATION);
            }
            return stack;
        }
        return ItemStack.EMPTY;
	}

    @Override
    public int getSlotLimit(int slot) {
        return slot == 0 ? 64 : 1;
    }

    @Nonnull
    @Override
	public ItemStack getStackInSlot(int slotID) {
		if (slotID == 0) {
			return bottleStack;
		}
		return this.isCoreActive() && this.getDeviceMode() == 1 ? this.getPotionItemFromEffect(slotID - 1) : ItemStack.EMPTY;
	}

    private boolean canInsertItem(int index, @Nonnull ItemStack stack) {
        if (index == 0) return stack.getItem() instanceof ItemGlassBottle && this.getDeviceMode() == 1;
        else return this.getDeviceMode() == 0 && isValidPotion(stack);
    }

    private boolean canExtractItem(int index) {
        if (index == 0) return this.getDeviceMode() == 0 && !this.bottleStack.isEmpty() && this.bottleStack.getCount() > 0;
        else return this.getDeviceMode() == 1 && !this.bottleStack.isEmpty() && this.bottleStack.getCount() > 0;
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
	
	@Nonnull
    private Collection<PotionEffect> getValidPotionEffects() {
		Collection<PotionEffect> c = this.getPlayer().getActivePotionEffects();
		for (PotionEffect pe : c) {
			if (pe.getIsAmbient()) c.remove(pe);
		}
		return c;
	}
	
	private boolean isValidPotion(@Nonnull ItemStack stack) {
		if (stack.getItem() instanceof ItemPotion) return true;
		if (!this.isCoreActive()) return false;
		for (PotionEffect eff : this.getPlayer().getActivePotionEffects()) {
			if (eff.isCurativeItem(stack)) return true;
		}
		return false;
	}
	
	@Nonnull
    private ItemStack getPotionItemFromEffect(int slotID) {
        ItemStack stack = new ItemStack(Items.POTIONITEM);
        PotionEffect effect = (PotionEffect) this.getPlayer().getActivePotionEffects().toArray()[slotID];
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("CustomPotionEffects", new NBTTagList());
        NBTTagList customPotionEffects = nbt.getTagList("CustomPotionEffects", 10);
        customPotionEffects.appendTag(effect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
        stack.setTagCompound(nbt);
        stack.setStackDisplayName("Artificial Potion");
        return stack;
    }

	@Nonnull
    @Override
	public String getLocalizedName() {
		return "tile.potionrelay.name";
	}

	@Nonnull
    @Override
	public ItemStack getDisplayStack() {
		return new ItemStack(ModElementList.blockPotionRelay);
	}

}
