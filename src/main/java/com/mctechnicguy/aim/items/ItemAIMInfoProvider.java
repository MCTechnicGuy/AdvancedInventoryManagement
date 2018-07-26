package com.mctechnicguy.aim.items;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.gui.IManualEntry;
import com.mctechnicguy.aim.network.PacketHelper;
import com.mctechnicguy.aim.network.PacketNetworkCoreList;
import com.mctechnicguy.aim.network.PacketNetworkInfo;
import com.mctechnicguy.aim.tileentity.IProvidesNetworkInfo;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkElement;
import com.mctechnicguy.aim.util.AIMUtils;
import com.mctechnicguy.aim.util.NBTUtils;
import com.mctechnicguy.aim.util.NetworkUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemAIMInfoProvider extends Item implements IManualEntry{

	public static final String NAME = "infoprovider";
	
	public ItemAIMInfoProvider() {
		this.setMaxStackSize(1);
		this.setCreativeTab(AdvancedInventoryManagement.AIMTab);
		this.setUnlocalizedName(NAME);
		this.setRegistryName(NAME);
	}

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        if (player.isSneaking()) {
            if (te instanceof TileEntityNetworkElement) {
                if (world.isRemote) return EnumActionResult.SUCCESS;
                if (((TileEntityNetworkElement)te).hasServerCore()) {
                    PacketHelper.sendPacketToClient(new PacketNetworkInfo(((TileEntityNetworkElement)te).getCore(), ((TileEntityNetworkElement)te).getCore().getPos(), ((TileEntityNetworkElement)te).getCore().getWorld().provider.getDimension()), (EntityPlayerMP)player);
                } else {
                    AIMUtils.sendChatMessage("message.noCore", player, TextFormatting.AQUA);
                }
            } else if (te instanceof TileEntityAIMCore) {
                if (world.isRemote) return EnumActionResult.SUCCESS;
                PacketHelper.sendPacketToClient(new PacketNetworkInfo((TileEntityAIMCore)te, te.getPos(), te.getWorld().provider.getDimension()), (EntityPlayerMP)player);
            } else if (world.isRemote) {
                return EnumActionResult.PASS;
            }
        } else if (world.isRemote) {
            return te instanceof IProvidesNetworkInfo ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        }
        return NetworkUtils.updateOverlayData(player, world, pos);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn, @Nonnull EnumHand hand)
    {
        if (playerIn.isSneaking()) {
            if (worldIn.isRemote) {
                AdvancedInventoryManagement.proxy.openLoadingGui();
            } else {
                this.displayPlayerDetails(worldIn, playerIn);
            }

        }
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }


    private void displayPlayerDetails(@Nonnull World world, @Nonnull EntityPlayer player) {
        NBTTagList list = NBTUtils.readPlayerInfo(player.getUniqueID(), world);
        if (list == null) list = new NBTTagList();
        PacketHelper.sendPacketToClient(new PacketNetworkCoreList(list, AIMUtils.isPlayerAccessible(player)), (EntityPlayerMP) player);
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
