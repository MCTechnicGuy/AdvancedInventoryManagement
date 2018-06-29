package com.mctechnicguy.aim.items;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.gui.IManualEntry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemPositionCard extends Item  implements IManualEntry {

    public static final String NAME = "aimpositioncard";

    public ItemPositionCard() {
        this.setMaxStackSize(1);
        this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
        this.setUnlocalizedName(NAME);
        this.setRegistryName(NAME);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nullable EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player == null || world.getBlockState(pos).getBlock() == ModElementList.blockPositionEditor) return EnumActionResult.PASS;
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) return EnumActionResult.PASS;
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound nbt = stack.getTagCompound();
        nbt.setInteger("x", pos.getX());
        nbt.setInteger("y", pos.getY());
        nbt.setInteger("z", pos.getZ());
        nbt.setInteger("dim", world.provider.getDimension());
        stack.setTagCompound(nbt);
        return EnumActionResult.PASS;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTagCompound();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (stack.hasTagCompound()) {
            NBTTagCompound nbt = stack.getTagCompound();
            tooltip.add("X: " + nbt.getInteger("x"));
            tooltip.add("Y: " + nbt.getInteger("y"));
            tooltip.add("Z: " + nbt.getInteger("z"));
            tooltip.add("Dimension: " + nbt.getInteger("dim"));
        }
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
