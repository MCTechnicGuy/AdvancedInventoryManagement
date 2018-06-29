package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.blocks.property.PropertyAIMMode;
import com.mctechnicguy.aim.tileentity.TileEntityPotionRelay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockPotionRelay extends BlockAIMModulatedDevice {

	public static final String NAME = "potionrelay";
	private final Random rand = new Random();
	public static final PropertyAIMMode MODE = PropertyAIMMode.create("mode", "inlet", "outlet");
	
	public BlockPotionRelay() {
		super(NAME);
	}

	public TileEntity createNewTileEntity(World w, int i) {
		return new TileEntityPotionRelay();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		if (world.getTileEntity(pos) instanceof TileEntityPotionRelay) {
            TileEntityPotionRelay te = (TileEntityPotionRelay) world.getTileEntity(pos);
            ItemStack itemstack = te.getStackInSlot(0);

            if (!itemstack.isEmpty()) {
                float f = this.rand.nextFloat() * 0.8F + 0.1F;
                float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
                float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

                while (itemstack.getCount() > 0) {
                    int k1 = this.rand.nextInt(21) + 10;

                    if (k1 > itemstack.getCount()) {
                        k1 = itemstack.getCount();
                    }

                    itemstack.shrink(k1);
                    EntityItem entityitem = new EntityItem(world, (double) ((float) pos.getX() + f), (double) ((float) pos.getY() + f1),
                            (double) ((float) pos.getZ() + f2), new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));

                    if (itemstack.hasTagCompound()) {
                        entityitem.getItem().setTagCompound(itemstack.getTagCompound().copy());
                    }

                    float f3 = 0.05F;
                    entityitem.motionX = (double) ((float) this.rand.nextGaussian() * f3);
                    entityitem.motionY = (double) ((float) this.rand.nextGaussian() * f3 + 0.2F);
                    entityitem.motionZ = (double) ((float) this.rand.nextGaussian() * f3);
                    world.spawnEntity(entityitem);
                }
            }
		}
		super.breakBlock(world, pos, state);
	}

	@Nonnull
	@Override
	public Object[] getParams(int page) {
		return new Object[] {AdvancedInventoryManagement.POWER_PER_POTION_GENERATION};
	}

    @Override
    protected PropertyAIMMode getModeProperty() {
        return MODE;
    }



}
