package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityEnergyRelay;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockEnergyRelay extends BlockAIMDevice implements IManualEntry{

    public static final String NAME = "energyrelay";
    public static final PropertyEnum MODE = PropertyEnum.create("mode", BlockEnergyRelay.EnumType.class);

    public BlockEnergyRelay() {
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

        if (world.isRemote) return true;
        TileEntity te = (world.getTileEntity(pos));
        if (AIMUtils.isWrench(heldItem) && te instanceof TileEntityAIMDevice && ((TileEntityAIMDevice)te).isPlayerAccessAllowed(player)) {
            int mode = ((TileEntityAIMDevice) te).getDeviceMode();
            if (mode < BlockEnergyRelay.EnumType.values().length - 1) {
                mode++;
            } else
                mode = 0;
            world.setBlockState(pos, world.getBlockState(pos).withProperty(MODE, BlockEnergyRelay.EnumType.fromID(mode)), 2);
            TextComponentTranslation modeName = new TextComponentTranslation("mode.charge." + BlockEnergyRelay.EnumType.fromID(mode).getName());
            modeName.getStyle().setColor(TextFormatting.AQUA);
            AIMUtils.sendChatMessageWithArgs("message.modechange", player, TextFormatting.RESET, modeName);
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MODE, ISACTIVE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(MODE, EnumType.fromID(meta)).withProperty(ISACTIVE, false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumType type = (EnumType) state.getValue(MODE);
        return type.getID();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityEnergyRelay();
    }

    @Nonnull
    @Override
    public String getManualName() {
        return NAME;
    }

    @Override
    public int getPageCount() {
        return 0;
    }

    @Override
    public boolean doesProvideOwnContent() {
        return false;
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[0];
    }

    @Override
    public boolean needsSmallerFont() {
        return false;
    }

    public enum EnumType implements IStringSerializable {

        ALL(0, "all"),
        HOTBAR(1, "hotbar"),
        MAINHAND(2, "mainhand"),
        OFFHAND(3, "offhand");

        private int id;
        private String name;


        EnumType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        public int getID() {
            return id;
        }

        @Override
        public String toString() {
            return getName();
        }

        @Nonnull
        public static EnumType fromID(int id) {
            switch(id) {
                case 0: return ALL;
                case 1: return HOTBAR;
                case 2: return MAINHAND;
                case 3: return OFFHAND;
                default: return ALL;
            }
        }


    }
}
