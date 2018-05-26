package com.mctechnicguy.aim.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;

public class AIMEnergyInputSlot extends Slot {

	public AIMEnergyInputSlot(@Nonnull IInventory inv, int id, int x, int y) {
		super(inv, id, x, y);
	}
	
	@Override
	public boolean isItemValid(@Nonnull ItemStack stack)
    {
        return stack.hasCapability(CapabilityEnergy.ENERGY, null);
    }
	
}
