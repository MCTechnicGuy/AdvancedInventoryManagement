package com.mctechnicguy.aim.util;

import net.minecraft.item.ItemStack;

public class NumberedItemStackHolder {

    private ItemStack content;
    private int number;

    public NumberedItemStackHolder(ItemStack content, int number) {
        this.content = content;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public ItemStack getContent() {
        return content;
    }
}
