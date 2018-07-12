package com.mctechnicguy.aim.items;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.IManualEntry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking()) return EnumActionResult.PASS;
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof IManualEntry) {
            AdvancedInventoryManagement.proxy.openManualGui((IManualEntry)block);
            return EnumActionResult.SUCCESS;
        } else return EnumActionResult.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn, EnumHand hand) {
        if (worldIn.isRemote) {
            AdvancedInventoryManagement.proxy.openManualGui(null);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
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
