package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityGenerator;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGenerator extends BlockAIMMachine implements IManualEntry, IAIMGenerator {

    public static final PropertyInteger BURNING_STAGE = PropertyInteger.create("burning_stage", 0, 5);
    public static final String NAME = "generator";

    public BlockGenerator() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BURNING_STAGE, 0));
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityGenerator();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BURNING_STAGE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BURNING_STAGE, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BURNING_STAGE);
    }

    @Override
    public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player == null || player.getHeldItem(hand).getItem() instanceof ItemAIMInfoProvider) return false;
        ItemStack heldItem = player.getHeldItem(hand);

        if (!player.isSneaking() && !heldItem.isEmpty() && TileEntityFurnace.isItemFuel(heldItem) && !(heldItem.getItem() instanceof ItemBucket)) {

            if (world.getTileEntity(pos) instanceof TileEntityGenerator && TileEntityFurnace.getItemBurnTime(heldItem) <= TileEntityGenerator.MAX_BURN_TIME - ((TileEntityGenerator)world.getTileEntity(pos)).burnTimeRemaining) {
                if (!world.isRemote) {
                    ((TileEntityGenerator)world.getTileEntity(pos)).addBurnTime(TileEntityFurnace.getItemBurnTime(heldItem));
                    heldItem.shrink(1);
                    if (heldItem.getCount() <= 0) heldItem = ItemStack.EMPTY;
                    player.setHeldItem(hand, heldItem);
                }
                return true;
            }
        }

        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state) {
        return state.getValue(BURNING_STAGE) * 3;
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
        return new Object[] {AdvancedInventoryManagement.MAX_GENERATOR_POWER_OUTPUT};
    }

    @Override
    public boolean needsSmallerFont() {
        return true;
    }
}
