package com.mctechnicguy.aim.items;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.IManualEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemAIMManual extends Item implements IManualEntry{

    public static final String NAME = "inventorymanagementmanual";

    public ItemAIMManual() {
        super();
        this.setMaxStackSize(1);
        this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
        this.setUnlocalizedName(NAME);
        this.setRegistryName(NAME);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn, EnumHand hand) {
        if (worldIn.isRemote) playerIn.openGui(AdvancedInventoryManagement.instance, AdvancedInventoryManagement.guiIDGuide, worldIn, 0, 0, 0); //Pass page numbers?
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }

    @Nonnull
    @Override
    public String getManualName() {
        return NAME;
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[0];
    }
}
