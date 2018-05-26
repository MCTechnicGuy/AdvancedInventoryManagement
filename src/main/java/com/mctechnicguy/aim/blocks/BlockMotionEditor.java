package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityMotionEditor;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMotionEditor extends BlockAIMDevice implements IManualEntry{

    public static final String NAME = "motioneditor";
    private static final PropertyEnum MODE = PropertyEnum.create("mode", EnumFacing.class);

    public BlockMotionEditor() {
        super(NAME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISACTIVE, false).withProperty(MODE, EnumFacing.DOWN));
    }

    @Override
    public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player == null || player.getHeldItem(hand).getItem() instanceof ItemAIMInfoProvider) return false;
        ItemStack heldItem = player.getHeldItem(hand);

        if (player.isSneaking() && !heldItem.isEmpty() && heldItem.getItem() != ModElementList.itemAIMWrench && AIMUtils.isWrench(heldItem)) {
            this.destroyWithWrench(player, world, pos, heldItem);
            return true;
        }

        if (world.isRemote) return true;
        TileEntity te = (world.getTileEntity(pos));
        if (AIMUtils.isWrench(heldItem) && te instanceof TileEntityAIMDevice && ((TileEntityAIMDevice)te).isPlayerAccessAllowed(player)) {
            int mode = ((TileEntityAIMDevice) te).getDeviceMode();
            if (mode < EnumFacing.values().length - 1) {
                mode++;
            } else
                mode = 0;
            world.setBlockState(pos, world.getBlockState(pos).withProperty(MODE, EnumFacing.getFront(mode)), 2);
            TextComponentTranslation modeName = new TextComponentTranslation("mode." + EnumFacing.getFront(mode).getName());
            modeName.getStyle().setColor(TextFormatting.AQUA);
            AIMUtils.sendChatMessageWithArgs("message.modechange", player, TextFormatting.RESET, modeName);
            return true;
        }
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, Block blockIn, BlockPos neighbor) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityMotionEditor) {
            ((TileEntityMotionEditor)worldIn.getTileEntity(pos)).addMotionToPlayer(worldIn.isBlockIndirectlyGettingPowered(pos));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityMotionEditor();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MODE, ISACTIVE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(MODE, EnumFacing.getFront(meta)).withProperty(ISACTIVE, false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing type = (EnumFacing) state.getValue(MODE);
        return type.getIndex();
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
        return new Object[] {AdvancedInventoryManagement.POWER_PER_MOTION_EDIT, AdvancedInventoryManagement.POWER_PER_MOTION_EDIT * 225};
    }

    @Override
    public boolean needsSmallerFont() {
        return true;
    }
}
