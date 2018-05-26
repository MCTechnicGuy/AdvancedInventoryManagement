package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkCable;
import com.mctechnicguy.aim.util.AIMUtils;
import com.mctechnicguy.aim.util.IWrenchDestroyable;
import com.mctechnicguy.aim.util.NetworkUtils;
import com.mctechnicguy.aim.util.RayTracingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockNetworkCable extends Block implements ITileEntityProvider, IWrenchDestroyable, IManualEntry{

	private float Pixel = 1F / 16F;
	private double extension = 5 * Pixel;
	private double coreWidth = 6 * Pixel;
	public static final String NAME = "networkcable";
	private static final PropertyBool UP = PropertyBool.create("up");
	private static final PropertyBool DOWN = PropertyBool.create("down");
	private static final PropertyBool NORTH = PropertyBool.create("north");
	private static final PropertyBool SOUTH = PropertyBool.create("south");
	private static final PropertyBool EAST = PropertyBool.create("east");
	private static final PropertyBool WEST = PropertyBool.create("west");
	private static final PropertyBool ISCOREACTIVE = PropertyBool.create("iscoreactive");

	@Nonnull
	public String getName() {
		return NAME;
	}

	public BlockNetworkCable() {
		super(Material.GLASS);
		this.setUnlocalizedName(NAME);
		this.setRegistryName(getName());
		this.setSoundType(SoundType.GLASS);
		this.setResistance(2000F);
		this.setHardness(3.5F);
		this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(UP, false).withProperty(DOWN, false).withProperty(NORTH, false).withProperty(EAST, false)
		.withProperty(WEST, false).withProperty(ISCOREACTIVE, false));
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess w, BlockPos pos) {
		return new AxisAlignedBB(5 * Pixel, 5 * Pixel, 5 * Pixel, 1 - 5 * Pixel, 1 - 5 * Pixel, 1 - 5 * Pixel);
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

    @Nonnull
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
    	return BlockRenderLayer.CUTOUT;
    }
    

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
    
    @Override
    public void onNeighborChange(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos, BlockPos neighborBlock)
    {
    	this.updateConnections(worldIn, pos);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity e, boolean b)
    {
    	if (AdvancedInventoryManagement.TRAVERSABLE_CABLES) return;

		AxisAlignedBB box = null;

		for (int i = 0; i < 7; i++) {

			double mX = pos.getX();
			double mY = pos.getY();
			double mZ = pos.getZ();
			double MX = pos.getX() + 1;
			double MY = pos.getY() + 1;
			double MZ = pos.getZ() + 1;
			AxisAlignedBB bb = this.getBoundingBox(state, worldIn, pos);

			if (i == 6)
				box = new AxisAlignedBB((double) pos.getX() + bb.minX, (double) pos.getY() + bb.minY,
						(double) pos.getZ() + bb.minZ, (double) pos.getX() + bb.maxX, (double) pos.getY() + bb.maxY, (double) pos.getZ() + bb.maxZ);

			TileEntityNetworkCable cable = (TileEntityNetworkCable) worldIn.getTileEntity(pos);
			if (cable != null && i < 6) {
				if (cable.hasRealConnection(EnumFacing.getFront(i))) {
					switch (EnumFacing.getFront(i).getFrontOffsetX()) {
					case 1:
						mX += (extension + coreWidth);
						break;
					case -1:
						MX -= (extension + coreWidth);
						break;
					default:
						mX += extension;
						MX -= extension;
						break;
					}
					switch (EnumFacing.getFront(i).getFrontOffsetY()) {
					case 1:
						mY += (extension + coreWidth);
						break;
					case -1:
						MY -= (extension + coreWidth);
						break;
					default:
						mY += extension;
						MY -= extension;
						break;
					}
					switch (EnumFacing.getFront(i).getFrontOffsetZ()) {
					case 1:
						mZ += (extension + coreWidth);
						break;
					case -1:
						MZ -= (extension + coreWidth);
						break;
					default:
						mZ += extension;
						MZ -= extension;
						break;
					}
					box = new AxisAlignedBB(mX, mY, mZ, MX, MY, MZ);
				}
			}

			if (box != null && mask.intersects(box)) {
				list.add(box);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
		TileEntityNetworkCable cable = (TileEntityNetworkCable) worldIn.getTileEntity(pos);
		if (cable != null) {
			AxisAlignedBB bb = this.getBoundingBox(state, worldIn, pos);
			return new AxisAlignedBB(
					(double) pos.getX() + bb.minX - (cable.hasRealConnection(EnumFacing.WEST) ? extension : 0),
					(double) pos.getY() + bb.minY - (cable.hasRealConnection(EnumFacing.DOWN) ? extension : 0),
					(double) pos.getZ() + bb.minZ - (cable.hasRealConnection(EnumFacing.NORTH) ? extension : 0),
					(double) pos.getX() + bb.maxX + (cable.hasRealConnection(EnumFacing.EAST) ? extension : 0),
					(double) pos.getY() + bb.maxY + (cable.hasRealConnection(EnumFacing.UP) ? extension : 0),
					(double) pos.getZ() + bb.maxZ + (cable.hasRealConnection(EnumFacing.SOUTH) ? extension : 0));
		} else
			return super.getCollisionBoundingBox(state, worldIn, pos);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityNetworkCable();
	}

	@Override
	public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, EntityLivingBase placer, @Nonnull ItemStack stack)
    {
		if (!NetworkUtils.canPlaceAIMBlock(placer, worldIn, pos)) {
			this.breakBlock(worldIn, pos, state);
			if (placer instanceof EntityPlayer) {
				this.harvestBlock(worldIn, (EntityPlayer) placer, pos, state, worldIn.getTileEntity(pos), stack);
				super.removedByPlayer(state, worldIn, pos, (EntityPlayer) placer, true);
			}
			return;
		}
		this.updateConnections(worldIn, pos);

	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
		return RayTracingHelper.rayTrace(worldIn, pos, start, end, blockState);
	}

	@Override
	 public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
		if (((TileEntityNetworkCable) world.getTileEntity(pos)).isPlayerAccessAllowed(player))
			return super.removedByPlayer(state, world, pos, player, willHarvest);
		else {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			return false;
		}
	}

	final void updateConnections(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		if (worldIn.getTileEntity(pos) instanceof TileEntityNetworkCable) {
			((TileEntityNetworkCable)worldIn.getTileEntity(pos)).updateConnections();
		}
	}

	@Override
	public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, float sideX, float sideY, float sideZ)
    {
		if (player == null || player.getHeldItem(hand).getItem() instanceof ItemAIMInfoProvider) return false;
		ItemStack heldItem = player.getHeldItem(hand);

		if (player.isSneaking() && !heldItem.isEmpty() && heldItem.getItem() != ModElementList.itemAIMWrench && AIMUtils.isWrench(heldItem)) {
			this.destroyWithWrench(player, world, pos, heldItem);
			return true;
		}
		
		if (player.isSneaking()) return false;
		
		if (AIMUtils.isWrench(heldItem)) {
			if (!world.isRemote && ((TileEntityNetworkCable) world.getTileEntity(pos)).isPlayerAccessAllowed(player)) {
				TileEntityNetworkCable cable = (TileEntityNetworkCable) world.getTileEntity(pos);
				cable.updateConnections();
				AxisAlignedBB bb = this.getBoundingBox(state, world, pos);
				if (sideX <= bb.maxX && sideX >= bb.minX && sideY <= bb.maxY && sideY >= bb.minY
						&& sideZ <= bb.maxZ && sideZ >= bb.minZ) {
					// if the core is hit
					if (sideY == 0.3125) {
						if (cable.isConnectionBlocked(EnumFacing.DOWN))
							cable.setConnectionBlocked(0, false);
						if (AIMUtils.getTEAtSide(EnumFacing.DOWN, world, pos) instanceof TileEntityNetworkCable) {
							((TileEntityNetworkCable) AIMUtils.getTEAtSide(EnumFacing.DOWN, world, pos))
									.setConnectionBlocked(
											EnumFacing.DOWN.getOpposite().ordinal(), false);
						}
					} else if (sideY == 0.6875) {
						if (cable.isConnectionBlocked(EnumFacing.UP))
							cable.setConnectionBlocked(1, false);
						if (AIMUtils.getTEAtSide(EnumFacing.UP, world, pos) instanceof TileEntityNetworkCable) {
							((TileEntityNetworkCable) AIMUtils.getTEAtSide(EnumFacing.UP, world, pos))
									.setConnectionBlocked(EnumFacing.UP.getOpposite().ordinal(),
											false);
						}
					} else if (sideZ == 0.3125) {
						if (cable.isConnectionBlocked(EnumFacing.NORTH))
							cable.setConnectionBlocked(2, false);
						if (AIMUtils.getTEAtSide(EnumFacing.NORTH, world, pos) instanceof TileEntityNetworkCable) {
							((TileEntityNetworkCable) AIMUtils.getTEAtSide(EnumFacing.NORTH, world, pos))
									.setConnectionBlocked(
											EnumFacing.NORTH.getOpposite().ordinal(), false);
						}
					} else if (sideZ == 0.6875) {
						if (cable.isConnectionBlocked(EnumFacing.SOUTH))
							cable.setConnectionBlocked(3, false);
						if (AIMUtils.getTEAtSide(EnumFacing.SOUTH, world, pos) instanceof TileEntityNetworkCable) {
							((TileEntityNetworkCable) AIMUtils.getTEAtSide(EnumFacing.SOUTH, world, pos))
									.setConnectionBlocked(
											EnumFacing.SOUTH.getOpposite().ordinal(), false);
						}
					} else if (sideX == 0.3125) {
						if (cable.isConnectionBlocked(EnumFacing.WEST))
							cable.setConnectionBlocked(4, false);
						if (AIMUtils.getTEAtSide(EnumFacing.WEST, world, pos) instanceof TileEntityNetworkCable) {
							((TileEntityNetworkCable) AIMUtils.getTEAtSide(EnumFacing.WEST, world, pos))
									.setConnectionBlocked(
											EnumFacing.WEST.getOpposite().ordinal(), false);
						}
					} else if (sideX == 0.6875) {
						if (cable.isConnectionBlocked(EnumFacing.EAST))
							cable.setConnectionBlocked(5, false);
						if (AIMUtils.getTEAtSide(EnumFacing.EAST, world, pos) instanceof TileEntityNetworkCable) {
							((TileEntityNetworkCable) AIMUtils.getTEAtSide(EnumFacing.EAST, world, pos))
									.setConnectionBlocked(
											EnumFacing.EAST.getOpposite().ordinal(), false);
						}
					}
				} else { // If one of the connectors is hit
					if (sideX >= 0.3125 && sideX <= 0.6875 && sideY >= 0.3125 && sideY <= 0.6875 && sideZ > 0.6875) {
						if (AIMUtils.checkForAIMBlockAtSide(EnumFacing.getFront(3), world, pos)) {
							cable.setConnectionBlocked(3, true);
							if (world.getTileEntity(pos.south()) instanceof TileEntityNetworkCable) {
								((TileEntityNetworkCable) world.getTileEntity(pos.south())).setConnectionBlocked(
										EnumFacing.getFront(3).getOpposite().ordinal(),
										true);
							}
						}
					} else if (sideX >= 0.3125 && sideX <= 0.6875 && sideY >= 0.3125 && sideY <= 0.6875
							&& sideZ < 0.3125) {
						if (AIMUtils.checkForAIMBlockAtSide(EnumFacing.getFront(2), world, pos)) {
							cable.setConnectionBlocked(2, true);
							if (world.getTileEntity(pos.north()) instanceof TileEntityNetworkCable) {
								((TileEntityNetworkCable) world.getTileEntity(pos.north())).setConnectionBlocked(
										EnumFacing.getFront(2).getOpposite().ordinal(),
										true);
							}
						}
					} else if (sideZ >= 0.3125 && sideZ <= 0.6875 && sideY >= 0.3125 && sideY <= 0.6875
							&& sideX > 0.6875) {
						if (AIMUtils.checkForAIMBlockAtSide(EnumFacing.getFront(5), world, pos)) {
							cable.setConnectionBlocked(5, true);
							if (world.getTileEntity(pos.east()) instanceof TileEntityNetworkCable) {
								((TileEntityNetworkCable) world.getTileEntity(pos.east())).setConnectionBlocked(
										EnumFacing.getFront(5).getOpposite().ordinal(),
										true);
							}
						}
					} else if (sideZ >= 0.3125 && sideZ <= 0.6875 && sideY >= 0.3125 && sideY <= 0.6875
							&& sideX < 0.3125) {
						if (AIMUtils.checkForAIMBlockAtSide(EnumFacing.getFront(4), world, pos)) {
							cable.setConnectionBlocked(4, true);
							if (world.getTileEntity(pos.west()) instanceof TileEntityNetworkCable) {
								((TileEntityNetworkCable) world.getTileEntity(pos.west())).setConnectionBlocked(
										EnumFacing.getFront(4).getOpposite().ordinal(),
										true);
							}
						}
					} else if (sideX >= 0.3125 && sideX <= 0.6875 && sideZ >= 0.3125 && sideZ <= 0.6875
							&& sideY > 0.6875) {
						if (AIMUtils.checkForAIMBlockAtSide(EnumFacing.getFront(1), world, pos)) {
							cable.setConnectionBlocked(1, true);
							if (world.getTileEntity(pos.up()) instanceof TileEntityNetworkCable) {
								((TileEntityNetworkCable) world.getTileEntity(pos.up())).setConnectionBlocked(
										EnumFacing.getFront(1).getOpposite().ordinal(),
										true);
							}
						}
					} else if (sideX >= 0.3125 && sideX <= 0.6875 && sideZ >= 0.3125 && sideZ <= 0.6875
							&& sideY < 0.3125) {
						if (AIMUtils.checkForAIMBlockAtSide(EnumFacing.getFront(0), world, pos)) {
							cable.setConnectionBlocked(0, true);
							if (world.getTileEntity(pos.down()) instanceof TileEntityNetworkCable) {
								((TileEntityNetworkCable) world.getTileEntity(pos.down())).setConnectionBlocked(
										EnumFacing.getFront(0).getOpposite().ordinal(),
										true);
							}
						}
					}
				}
			}
			return true;
		} else {
			return false;
		}

	}
	
	@Nonnull
	@Override
	public IBlockState getActualState(IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		this.updateConnections(worldIn, pos);
	    TileEntity te = worldIn.getTileEntity(pos);
	    if (!(te instanceof TileEntityNetworkCable)) return state.withProperty(UP, false).withProperty(DOWN, false).withProperty(NORTH, false).withProperty(EAST, false)
	    		.withProperty(WEST, false).withProperty(ISCOREACTIVE, false);
	    	TileEntityNetworkCable cable = (TileEntityNetworkCable) te;
	    	return state.withProperty(UP, cable.hasRealConnection(EnumFacing.UP))
	    			.withProperty(DOWN, cable.hasRealConnection(EnumFacing.DOWN))
	    			.withProperty(NORTH, cable.hasRealConnection(EnumFacing.NORTH))
	    			.withProperty(SOUTH, cable.hasRealConnection(EnumFacing.SOUTH))
	    			.withProperty(EAST, cable.hasRealConnection(EnumFacing.EAST))
	    			.withProperty(WEST, cable.hasRealConnection(EnumFacing.WEST))
	    			.withProperty(ISCOREACTIVE, cable.hasCore() && cable.getCore().isActive());
	}
	    
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
	    return new BlockStateContainer(this, UP, DOWN, NORTH, SOUTH, EAST, WEST, ISCOREACTIVE);
	}
	
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
	    return getDefaultState().withProperty(UP, false).withProperty(DOWN, false).withProperty(NORTH, false).withProperty(EAST, false)
	    		.withProperty(WEST, false).withProperty(ISCOREACTIVE, false);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
	    return 0;
	}

	@Override
	public void destroyWithWrench(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull ItemStack item) {
		if (((TileEntityNetworkCable) world.getTileEntity(pos)).isPlayerAccessAllowed(player)) {
			this.breakBlock(world, pos, world.getBlockState(pos));
			this.harvestBlock(world, player, pos, world.getBlockState(pos), world.getTileEntity(pos), item);
			this.removedByPlayer(world.getBlockState(pos), world, pos, player, true);
		}
	}
	
	@Override
	 public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
		if (!world.isRemote && world.getTileEntity(pos) instanceof TileEntityNetworkCable && ((TileEntityNetworkCable)world.getTileEntity(pos)).hasCore())
			((TileEntityNetworkCable)world.getTileEntity(pos)).getCore().forceNetworkUpdate(5);
		super.breakBlock(world, pos, state);
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

	@Override
	public boolean doesProvideOwnContent() {
		return false;
	}

	@Nonnull
	@Override
	public Object[] getParams(int page) {
		return new Object[] {AdvancedInventoryManagement.POWER_PER_CABLE};
	}

	@Override
	public boolean needsSmallerFont() {
		return false;
	}
}
