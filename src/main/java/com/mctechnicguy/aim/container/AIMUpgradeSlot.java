package com.mctechnicguy.aim.container;

import com.mctechnicguy.aim.items.ItemAIMUpgrade;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class AIMUpgradeSlot extends Slot {

	public AIMUpgradeSlot(@Nonnull IInventory inv, int id, int x, int y) {
		super(inv, id, x, y);
	}
	
	@Override
	public boolean isItemValid(@Nonnull ItemStack stack)
    {
        return stack.getItem() instanceof ItemAIMUpgrade;
    }

}
