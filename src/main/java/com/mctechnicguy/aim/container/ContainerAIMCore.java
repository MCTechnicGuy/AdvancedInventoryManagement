package com.mctechnicguy.aim.container;

import com.mctechnicguy.aim.items.ItemAIMUpgrade;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ContainerAIMCore extends Container{

	private TileEntityAIMCore AIMCore;
	private int LastPower;

	public ContainerAIMCore (@Nonnull InventoryPlayer invplayer, @Nonnull TileEntityAIMCore entity) {
		this.AIMCore = entity;
		this.addSlotToContainer(new AIMUpgradeSlot(entity, 0, 134, 38));
		this.addSlotToContainer(new AIMUpgradeSlot(entity, 1, 134, 56));
		this.addSlotToContainer(new AIMUpgradeSlot(entity, 2, 152, 38));
		this.addSlotToContainer(new AIMUpgradeSlot(entity, 3, 152, 56));
		this.addSlotToContainer(new AIMEnergyInputSlot(entity, 4, 8, 75));

		//Player inventory
		for(int i = 0; i < 3; i++ ) {
			for (int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invplayer, j + i*9 + 9,  8 + j*18, 97 + i*18));
			}
		}
		//Hotbar
		for (int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invplayer, i, 8 + i*18, 155));
		}
	}
	
	@SideOnly(Side.CLIENT)
    @Override
	public void updateProgressBar (int par1, int par2) {
		this.AIMCore.Power = par1 * 1000 + par2;

	}

    @Override
	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return AIMCore.isUsableByPlayer(entityplayer);
	}

    @Override
	public void detectAndSendChanges () {
	    super.detectAndSendChanges();
        for (IContainerListener listener : this.listeners) {
            if (this.LastPower != this.AIMCore.Power) {
                short firstShort = (short) Math.floor(this.AIMCore.Power / 1000);
                short secondShort = (short) (this.AIMCore.Power - firstShort * 1000);
                listener.sendWindowProperty(this, firstShort, secondShort);
            }
        }
        this.LastPower = this.AIMCore.Power;
    }
	
	@Nonnull
    @Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNumber)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(slotNumber);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            
            if (slotNumber > 4)
            {
                if (itemstack1.getItem() instanceof ItemAIMUpgrade)
                {
                    if (!this.mergeItemStack(itemstack1, 0, 4, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (itemstack1.hasCapability(CapabilityEnergy.ENERGY, null)) {
                	if (!this.mergeItemStack(itemstack1, 4, 5, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (slotNumber < 32)
                {
                    if (!this.mergeItemStack(itemstack1, 32, 41, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (slotNumber < 41 && !this.mergeItemStack(itemstack1, 5, 32, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 5, 41, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0)
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onSlotChanged();
        }

        return itemstack;
    }

}
