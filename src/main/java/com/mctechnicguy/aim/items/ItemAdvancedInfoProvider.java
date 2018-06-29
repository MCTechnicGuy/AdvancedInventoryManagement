package com.mctechnicguy.aim.items;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.network.PacketHelper;
import com.mctechnicguy.aim.network.PacketOpenNetworkCoreList;
import com.mctechnicguy.aim.util.AIMUtils;
import com.mctechnicguy.aim.util.NBTUtils;
import com.mctechnicguy.aim.util.NetworkUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemAdvancedInfoProvider extends Item implements IManualEntry {

    public static final String NAME = "advancedinfoprovider";

    public ItemAdvancedInfoProvider() {
        this.setMaxStackSize(1);
        this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
        this.setUnlocalizedName(NAME);
        this.setRegistryName(NAME);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking()) return EnumActionResult.PASS;
        if (world.isRemote) return EnumActionResult.SUCCESS;
        return NetworkUtils.updateOverlayData(player, world, pos);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn, @Nonnull EnumHand hand)
    {
        if (playerIn.isSneaking() && !worldIn.isRemote) {
            this.displayPlayerDetails(worldIn, playerIn);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }


    private void displayPlayerDetails(@Nonnull World world, @Nonnull EntityPlayer player) {
        if (!world.isRemote) {
            NBTTagList list = NBTUtils.readPlayerInfo(player.getUniqueID(), world);
            if (list != null) {
                PacketHelper.sendPacketToClient(new PacketOpenNetworkCoreList(list, AIMUtils.isPlayerAccessible(player)), (EntityPlayerMP) player);
            }
        }
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

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[0];
    }

}
