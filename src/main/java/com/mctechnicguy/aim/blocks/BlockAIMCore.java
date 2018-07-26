package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkElement;
import com.mctechnicguy.aim.util.NBTUtils;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockAIMCore extends BlockAIMBase implements IManualEntry{

	private final Random rand = new Random();
	public static final String NAME = "aimcore";
	public static final PropertyBool ISACTIVE = PropertyBool.create("isactive");

	public BlockAIMCore() {
		super(NAME);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
	}
	
	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
	    TileEntity te = worldIn.getTileEntity(pos);
	    if (!(te instanceof TileEntityAIMCore))
	    	return state.withProperty(ISACTIVE, false);
	    else return state.withProperty(ISACTIVE, ((TileEntityAIMCore)te).isActive());
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
	    return new BlockStateContainer(this, ISACTIVE);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
	    return 0;
	}
	
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityAIMCore();
	}

    @Override
    protected EnumRightClickResult onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, @Nullable TileEntity tileEntity, ItemStack heldItem) {
	    EnumRightClickResult superResult = super.onBlockActivated(world, pos, state, player, hand, side, tileEntity, heldItem);
        if (superResult == EnumRightClickResult.ACTION_PASS) {
            if (tileEntity instanceof TileEntityAIMCore
                    && ((TileEntityAIMCore) tileEntity).isPlayerAccessAllowed(player) && !world.isRemote) {
                FMLNetworkHandler.openGui(player, AdvancedInventoryManagement.instance, AdvancedInventoryManagement.guiIDCore, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return EnumRightClickResult.ACTION_DONE;
        } else return superResult;
    }


	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
		if (worldIn.getTileEntity(pos) instanceof TileEntityAIMCore) {
			TileEntityAIMCore tileentityaimcore = (TileEntityAIMCore)worldIn.getTileEntity(pos);
			for (TileEntityNetworkElement te : tileentityaimcore.registeredDevices) {
				te.setCore(null);
			}

			if (tileentityaimcore.playerConnectedID != null) {
				NBTUtils.deleteCoreInfoFromTagList(tileentityaimcore.playerConnectedID, tileentityaimcore);
			}

			for (int j1 = 0; j1 < tileentityaimcore.getSizeInventory(); ++j1) {
				ItemStack itemstack = tileentityaimcore.getStackInSlot(j1);

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
						EntityItem entityitem = new EntityItem(worldIn, (double) ((float) pos.getX() + f), (double) ((float) pos.getY() + f1),
								(double) ((float) pos.getZ() + f2), new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));

						if (itemstack.hasTagCompound()) {
							entityitem.getItem().setTagCompound(itemstack.getTagCompound().copy());
						}

						float f3 = 0.05F;
						entityitem.motionX = (double) ((float) this.rand.nextGaussian() * f3);
						entityitem.motionY = (double) ((float) this.rand.nextGaussian() * f3 + 0.2F);
						entityitem.motionZ = (double) ((float) this.rand.nextGaussian() * f3);
						worldIn.spawnEntity(entityitem);
					}
				}
			}

		}

		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (((TileEntityAIMCore) world.getTileEntity(pos)).isPlayerAccessAllowed(player))
			return super.removedByPlayerSupercall(state, world, pos, player, willHarvest);
		else {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			return false;
		}
	}

	@Override
	public void destroyWithWrench(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull ItemStack item) {
		if (world.getTileEntity(pos) instanceof TileEntityAIMCore) {
			if (((TileEntityAIMCore) world.getTileEntity(pos)).isPlayerAccessAllowed(player)) {
				this.breakBlock(world, pos, world.getBlockState(pos));
				this.harvestBlock(world, player, pos, world.getBlockState(pos), world.getTileEntity(pos), item);
				this.removedByPlayer(world.getBlockState(pos), world, pos, player, true);
			}	
		}
	}


}
