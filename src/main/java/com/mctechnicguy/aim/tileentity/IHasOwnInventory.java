package com.mctechnicguy.aim.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IHasOwnInventory {

    NonNullList<ItemStack> getOwnInventoryContent();
    ItemStack getStackInOwnInventorySlot(int slot);
    int getOwnInventorySize();
}
