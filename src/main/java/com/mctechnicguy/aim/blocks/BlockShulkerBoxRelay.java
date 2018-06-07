package com.mctechnicguy.aim.blocks;

import com.mctechnicguy.aim.blocks.property.PropertyAIMMode;
import com.mctechnicguy.aim.tileentity.TileEntityAIMDevice;
import com.mctechnicguy.aim.tileentity.TileEntityShulkerBoxRelay;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockShulkerBoxRelay extends BlockAIMModulatedDevice {

    public static final String NAME = "shulkerboxrelay";
    public static final PropertyAIMMode MODE = PropertyAIMMode.create("mode", "all", "mainhand", "offhand", "slotbyid");

    public BlockShulkerBoxRelay() {
        super(NAME);
    }

    @Override
    protected EnumRightClickResult onBlockActivated(@Nonnull World world, BlockPos pos, IBlockState state, @Nullable EntityPlayer player, EnumHand hand, EnumFacing side, TileEntity tileEntity, ItemStack heldItem) {
        EnumRightClickResult superResult = super.onBlockActivated(world, pos, state, player, hand, side, tileEntity, heldItem);
        if (superResult == EnumRightClickResult.ACTION_PASS) {
            if (heldItem.isEmpty()) {
                if (tileEntity instanceof TileEntityShulkerBoxRelay && this.getMetaFromState(state) == 3) {
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

    @Override
    protected PropertyAIMMode getModeProperty() {
        return MODE;
    }


}
