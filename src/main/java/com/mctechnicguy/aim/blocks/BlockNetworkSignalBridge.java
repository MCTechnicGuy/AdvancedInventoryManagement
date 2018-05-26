package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkSignalBridge;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockNetworkSignalBridge extends BlockAIMDevice implements IManualEntry {

    public static final String NAME = "networksignalbridge";
    private static final PropertyBool UP = PropertyBool.create("up");
    private static final PropertyBool DOWN = PropertyBool.create("down");
    private static final PropertyBool NORTH = PropertyBool.create("north");
    private static final PropertyBool SOUTH = PropertyBool.create("south");
    private static final PropertyBool EAST = PropertyBool.create("east");
    private static final PropertyBool WEST = PropertyBool.create("west");

    public BlockNetworkSignalBridge() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
    }

    @Override
    public boolean onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player == null || player.getHeldItem(hand).getItem() instanceof ItemAIMInfoProvider) return false;
        ItemStack heldItem = player.getHeldItem(hand);

        if (player.isSneaking() && !heldItem.isEmpty() && heldItem.getItem() != ModElementList.itemAIMWrench && AIMUtils.isWrench(heldItem)) {
            this.destroyWithWrench(player, world, pos, heldItem);
            return true;
        }
        if (heldItem.getItem() == ModElementList.itemPositionCard) {
            TileEntity bridge = world.getTileEntity(pos);
            if (bridge instanceof TileEntityNetworkSignalBridge) {
                ((TileEntityNetworkSignalBridge)bridge).transferCardData(heldItem, player);
            }
            return true;
        }

        if (world.isRemote) return true;

        TileEntity te = (world.getTileEntity(pos));
        if (te instanceof TileEntityNetworkSignalBridge && ((TileEntityNetworkSignalBridge)te).isPlayerAccessAllowed(player)) {

            if (AIMUtils.isWrench(heldItem)) {
                BlockPos destination = ((TileEntityNetworkSignalBridge)te).getDestination();
                if (destination == null) {
                    AIMUtils.sendChatMessage("message.nodestinationset", player, TextFormatting.AQUA);
                } else {
                    AIMUtils.sendChatMessageWithArgs("message.destinationat", player, TextFormatting.AQUA, destination.getX(), destination.getY(), destination.getZ());
                }
                return true;
            }
        }
        return false;
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


    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity bridge = world.getTileEntity(pos);
        if (bridge instanceof TileEntityNetworkSignalBridge) {
            ((TileEntityNetworkSignalBridge)bridge).clearDestination(null);
        }
        super.breakBlock(world, pos, state);
    }


    @Nonnull
    @Override
    public IBlockState getActualState(IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEntityNetworkSignalBridge)) return super.getActualState(state, worldIn, pos)
                .withProperty(UP, false)
                .withProperty(DOWN, false)
                .withProperty(NORTH, false)
                .withProperty(EAST, false)
                .withProperty(WEST, false);
        TileEntityNetworkSignalBridge bridge = (TileEntityNetworkSignalBridge) te;
        return super.getActualState(state, worldIn, pos)
                .withProperty(UP, bridge.hasRealConnection(EnumFacing.UP))
                .withProperty(DOWN, bridge.hasRealConnection(EnumFacing.DOWN))
                .withProperty(NORTH, bridge.hasRealConnection(EnumFacing.NORTH))
                .withProperty(SOUTH, bridge.hasRealConnection(EnumFacing.SOUTH))
                .withProperty(EAST, bridge.hasRealConnection(EnumFacing.EAST))
                .withProperty(WEST, bridge.hasRealConnection(EnumFacing.WEST));
    }


    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, UP, DOWN, NORTH, SOUTH, EAST, WEST, ISACTIVE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(UP, false).withProperty(DOWN, false).withProperty(NORTH, false).withProperty(EAST, false)
                .withProperty(WEST, false).withProperty(ISACTIVE, false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }
    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityNetworkSignalBridge();
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
        return new Object[] {AdvancedInventoryManagement.POWER_PER_BRIDGE};
    }

    @Override
    public boolean needsSmallerFont() {
        return false;
    }
}
