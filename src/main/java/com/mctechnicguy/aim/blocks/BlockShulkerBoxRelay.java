package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityShulkerBoxRelay;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemNameTag;
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

public class BlockShulkerBoxRelay extends BlockAIMDevice implements IHasModes {

    public static final String NAME = "shulkerboxrelay";
    public static final PropertyEnum MODE = PropertyEnum.create("mode", EnumType.class);

    public BlockShulkerBoxRelay() {
        super(NAME);
        this.setDefaultState(
                this.blockState.getBaseState().withProperty(MODE, EnumType.ALL).withProperty(ISACTIVE, false));
    }

    @Override
    protected EnumRightClickResult onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, TileEntity tileEntity, ItemStack heldItem) {
        EnumRightClickResult superResult = super.onBlockActivated(world, pos, state, player, hand, side, tileEntity, heldItem);
        if (superResult == EnumRightClickResult.ACTION_PASS) {
            if (heldItem.isEmpty()) {
                if (tileEntity instanceof TileEntityShulkerBoxRelay && this.getMetaFromState(state) == EnumType.SLOTBYID.getID()) {
                    if (!world.isRemote) {
                        int change = player.isSneaking() ? -1 : 1;
                        Object[] args = ((TileEntityShulkerBoxRelay)tileEntity).setSlotID(((TileEntityShulkerBoxRelay)tileEntity).slotID + change);
                        if (args == null) AIMUtils.sendChatMessage("message.slotidchanged.nocore", player, TextFormatting.RED);
                        else AIMUtils.sendChatMessageWithArgs("message.slotidchanged" + args[1], player, TextFormatting.AQUA, args[0]);
                    }
                    return EnumRightClickResult.ACTION_DONE;
                }
            } else if (tileEntity instanceof TileEntityAIMDevice && ((TileEntityAIMDevice) tileEntity).isPlayerAccessAllowed(player)) {
                if (heldItem.getItem() instanceof ItemNameTag) {
                    if (world.isRemote) return EnumRightClickResult.ACTION_DONE;
                    if (!heldItem.hasTagCompound()) {
                        ((TileEntityShulkerBoxRelay)tileEntity).clearFilter();
                        AIMUtils.sendChatMessage("message.shulkerboxrelay.filtercleared", player, TextFormatting.RESET);
                    } else {
                        ((TileEntityShulkerBoxRelay)tileEntity).setFilter(heldItem.getDisplayName());
                        AIMUtils.sendChatMessageWithArgs("message.shulkerboxrelay.filterset", player, TextFormatting.RESET, heldItem.getDisplayName());
                    }
                    return EnumRightClickResult.ACTION_DONE;
                }
            }
            return EnumRightClickResult.ACTION_PASS;
        } else return superResult;
    }

    public TileEntity createNewTileEntity(World w, int i) {
        return new TileEntityShulkerBoxRelay();
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
    public int getIDFromState(IBlockState state) {
        EnumType type = (EnumType) state.getValue(MODE);
        return type.getID();
    }

    @Override
    public String getCurrentModeUnlocalizedName(World world, BlockPos pos) {
        return "mode." + EnumType.fromID(getIDFromState(world.getBlockState(pos))).getName();
    }

    @Override
    public void cycleToNextMode(World world, BlockPos pos, EntityPlayer causer) {
        int mode = getIDFromState(world.getBlockState(pos));
        if (mode < EnumType.values().length - 1) {
            mode++;
        } else
            mode = 0;
        setMode(world, pos, mode, causer);
    }

    @Override
    public void setMode(World world, BlockPos pos, int id, EntityPlayer causer) {
        world.setBlockState(pos, world.getBlockState(pos).withProperty(MODE, EnumType.fromID(id)), 2);
        if (causer != null) {
            TextComponentTranslation modeName = new TextComponentTranslation("mode." + EnumType.fromID(id).getName());
            modeName.getStyle().setColor(TextFormatting.AQUA);
            AIMUtils.sendChatMessageWithArgs("message.modechange", causer, TextFormatting.RESET, modeName);
        }
    }

    public enum EnumType implements IStringSerializable {

        ALL(0, "all"), MAINHAND(1, "mainhand"), OFFHAND(2, "offhand"), SLOTBYID(3, "slotbyid");

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
            switch (id) {
                case 0:
                    return ALL;
                case 1:
                    return MAINHAND;
                case 2:
                    return OFFHAND;
                case 3:
                    return SLOTBYID;
                default:
                    return ALL;
            }
        }

    }

}
