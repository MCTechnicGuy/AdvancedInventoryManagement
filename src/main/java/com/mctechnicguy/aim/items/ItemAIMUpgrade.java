package com.mctechnicguy.aim.items;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModInfo;
import com.mctechnicguy.aim.gui.GuiAIMGuide;
import com.mctechnicguy.aim.gui.ICustomManualEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemAIMUpgrade extends Item implements ICustomManualEntry{
	
	public static final String[] names = new String[] {"security", "redstone", "energy"};
	public static final String NAME = "aimupgrade";
	
	public ItemAIMUpgrade() {
		super();
		this.setMaxStackSize(16);
		this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
		this.setUnlocalizedName(NAME);
		this.setRegistryName(NAME);
		this.setHasSubtypes(true);
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(@Nonnull ItemStack stack) {
		int i = MathHelper.clamp(stack.getItemDamage(), 0, 3);
		return super.getUnlocalizedName() + "_" + names[i];
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			for (int i = 0; i < names.length; i++) {
				items.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Nonnull
	@Override
	public String getManualName() {
		return NAME;
	}

	@Override
	public int getPageCount() {
		return 3;
	}

	@Nonnull
	@Override
	public Object[] getParams(int page) {
		return new Object[0];
	}


	@Override
	public boolean hasLeftSidePicture(int page) {
		return true;
	}

	@Override
	public void drawLeftSidePicture(int page, Minecraft mc, GuiAIMGuide gui, float zLevel) {
	}

	@Override
	public boolean showCraftingRecipe(int page) {
		return true;
	}

	@Nullable
	@Override
	public ResourceLocation getRecipeForPage(int page) {
	    return new ResourceLocation(ModInfo.ID + ":" + NAME + "_" + page);
	}

	@Override
	public boolean showHeaderOnPage(int page) {
		return true;
	}
}
