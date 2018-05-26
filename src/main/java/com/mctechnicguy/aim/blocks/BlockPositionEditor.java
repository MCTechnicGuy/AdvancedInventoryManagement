package com.mctechnicguy.aim.blocks;


import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityPositionEditor;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPositionEditor extends BlockAIMDevice implements IManualEntry {

    public static final String NAME = "positioneditor";

    public BlockPositionEditor() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false));
    }

    @Override
    public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player == null || player.getHeldItem(hand).getItem() instanceof ItemAIMInfoProvider) return false;
        ItemStack heldItem = player.getHeldItem(hand);
        if (player.isSneaking() && !heldItem.isEmpty() && heldItem.getItem() != ModElementList.itemAIMWrench && AIMUtils.isWrench(heldItem)) {
            this.destroyWithWrench(player, world, pos, heldItem);
            return true;
        }
        if (heldItem.getItem() == ModElementList.itemPositionCard) {
            TileEntity editor = world.getTileEntity(pos);
            if (editor instanceof TileEntityPositionEditor) {
                ((TileEntityPositionEditor)editor).transferCardData(heldItem, player);
            }
            return true;
        }
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, Block blockIn, BlockPos neighbor) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityPositionEditor) {
            ((TileEntityPositionEditor)worldIn.getTileEntity(pos)).onRedstoneChanged(worldIn.isBlockIndirectlyGettingPowered(pos));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityPositionEditor();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ISACTIVE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ISACTIVE, false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
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
        return new Object[] {AdvancedInventoryManagement.POWER_PER_TELEPORT};
    }

    @Override
    public boolean needsSmallerFont() {
        return true;
    }
}
