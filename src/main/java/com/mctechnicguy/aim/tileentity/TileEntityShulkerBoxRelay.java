package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.util.NumberedItemStackHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class TileEntityShulkerBoxRelay extends TileEntitySlotSelectionRelay {

    public String filter;

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        try {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) this;
        } catch (ClassCastException x) {
            return super.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        if (filter != null) {
            nbt.setString("filter", filter);
        }
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("filter")) {
            this.filter = nbt.getString("filter");
        } else {
            this.filter = null;
        }
        super.readFromNBT(nbt);
    }


    private ArrayList<NumberedItemStackHolder> getApplicableShulkerBoxes() {
        if (!isCoreActive() || getPlayer() == null) return null;
        ArrayList<NumberedItemStackHolder> list = new ArrayList<>();
        if (getCurrentSlotID() == -1) {
            for (int i = 0; i < playerInventory().getSlots(); i++) {
                ItemStack stack = playerInventory().getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemShulkerBox) {
                    if (isUnfiltered() || stack.getDisplayName().equals(filter)) {
                        list.add(new NumberedItemStackHolder(stack, i));
                    }
                }
            }
        } else {
            ItemStack stack = playerInventory().getStackInSlot(getCurrentSlotID());
            if (!stack.isEmpty() && stack.getItem() instanceof ItemShulkerBox) {
                if (isUnfiltered() || stack.getDisplayName().equals(filter)) {
                    list.add(new NumberedItemStackHolder(stack, getCurrentSlotID()));
                }
            }
        }
        return list;
    }

    private NonNullList<ItemStack> getShulkerBoxContent() {
        ArrayList<NumberedItemStackHolder> boxes = getApplicableShulkerBoxes();
        if (boxes == null) return null;

        NonNullList<ItemStack> items = NonNullList.create();
        for (NumberedItemStackHolder boxHolder : boxes) {
            this.addNBTIfNecessary(boxHolder.getContent());
            NBTTagCompound nbt = boxHolder.getContent().getTagCompound();
            NBTTagCompound blockEntityTag = nbt.getCompoundTag("BlockEntityTag");
            NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);
            items.addAll(nonnulllist);
        }
        return items;
    }

    private IItemHandler playerInventory() {
        return getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    @Override
    public int getSlots() {
        NonNullList<ItemStack> boxContent = getShulkerBoxContent();
        return boxContent == null ? 1 : boxContent.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        NonNullList<ItemStack> boxContent = getShulkerBoxContent();
        return boxContent != null ? boxContent.get(slot) : ItemStack.EMPTY;
    }

    private int translateBoxSlotToPlayerInventorySlot(int boxSlot) {
        ArrayList<NumberedItemStackHolder> boxes = getApplicableShulkerBoxes();
        if (boxes == null || (int)Math.floor(boxSlot / 27) > boxes.size() - 1) return -1;
        return boxes.get((int)Math.floor(boxSlot / 27)).getNumber();
    }

    private ItemStack addNBTIfNecessary(ItemStack box) {
        NBTTagCompound nbt;
        if (!box.hasTagCompound()) {
            nbt = new NBTTagCompound();
        } else {
            nbt = box.getTagCompound();
        }
        NBTTagCompound blockEntityTag;
        if (!nbt.hasKey("BlockEntityTag", 10)) {
            blockEntityTag = new NBTTagCompound();
        } else {
            blockEntityTag = nbt.getCompoundTag("BlockEntityTag");
        }
        if (!blockEntityTag.hasKey("Items", 9)) {
            blockEntityTag.setTag("Items", new NBTTagList());
        }
        nbt.setTag("BlockEntityTag", blockEntityTag);
        box.setTagCompound(nbt);
        return box;
    }

    private boolean isItemValidForInsertion(ItemStack stack) {
        return !(Block.getBlockFromItem(stack.getItem()) instanceof BlockShulkerBox);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!this.isItemValidForInsertion(stack)) return stack;
        NonNullList<ItemStack> shulkerBoxContent = getShulkerBoxContent();
        if (shulkerBoxContent == null || shulkerBoxContent.isEmpty() || shulkerBoxContent.size() < (int)Math.floor(slot / 27) + 1) return stack;
        else {
            int inventorySlot = translateBoxSlotToPlayerInventorySlot(slot);
            ItemStack requestedBox = playerInventory().extractItem(inventorySlot, 1, simulate);
            if (!requestedBox.isEmpty() && requestedBox.getItem() instanceof ItemShulkerBox) {
                this.addNBTIfNecessary(requestedBox);
                NBTTagCompound nbt = requestedBox.getTagCompound();
                NBTTagCompound blockEntityTag = nbt.getCompoundTag("BlockEntityTag");
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);
                int boxSlot = slot % 27;
                ItemStack stackToWriteTo = nonnulllist.get(boxSlot);
                if (stackToWriteTo.isEmpty()) {
                    nonnulllist.set(boxSlot, stack);
                    stack = ItemStack.EMPTY;
                    ItemStackHelper.saveAllItems(blockEntityTag, nonnulllist, false);
                } else if (stackToWriteTo.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == stackToWriteTo.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, stackToWriteTo)) {
                    int fullCount = stackToWriteTo.getCount() + stack.getCount();
                    int maxSize = stackToWriteTo.getMaxStackSize();

                    if (fullCount <= maxSize) {
                        stack = ItemStack.EMPTY;
                        stackToWriteTo.setCount(fullCount);
                    } else if (stackToWriteTo.getCount() < maxSize) {
                        stack.shrink(maxSize - stackToWriteTo.getCount());
                        stackToWriteTo.setCount(maxSize);
                    }
                    nonnulllist.set(boxSlot, stackToWriteTo);
                    ItemStackHelper.saveAllItems(blockEntityTag, nonnulllist, false);
                }
                nbt.setTag("BlockEntityTag", blockEntityTag);
                requestedBox.setTagCompound(nbt);
            }

            if (!simulate) {
               playerInventory().insertItem(inventorySlot, requestedBox, false);
            }
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack result = ItemStack.EMPTY;
        NonNullList<ItemStack> shulkerBoxContent = getShulkerBoxContent();
        if (shulkerBoxContent == null || shulkerBoxContent.isEmpty() || shulkerBoxContent.size() < (int)Math.floor(slot / 27) + 1) return ItemStack.EMPTY;
        else {
            int inventorySlot = translateBoxSlotToPlayerInventorySlot(slot);
            ItemStack requestedBox = playerInventory().extractItem(inventorySlot, 1, simulate);
            if (!requestedBox.isEmpty() && requestedBox.getItem() instanceof ItemShulkerBox) {
                this.addNBTIfNecessary(requestedBox);
                NBTTagCompound nbt = requestedBox.getTagCompound();
                NBTTagCompound blockEntityTag = nbt.getCompoundTag("BlockEntityTag");
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);
                int boxSlot = slot % 27;
                ItemStack stackToGet = nonnulllist.get(boxSlot);
                if (!stackToGet.isEmpty()) {
                    result = stackToGet.copy();
                    nonnulllist.set(boxSlot, ItemStack.EMPTY);
                    ItemStackHelper.saveAllItems(blockEntityTag, nonnulllist);
                }
                nbt.setTag("BlockEntityTag", blockEntityTag);
                requestedBox.setTagCompound(nbt);
            }

            if (!simulate) {
                playerInventory().insertItem(inventorySlot, requestedBox, false);
            }
        }
        return result;
    }

    private int getCurrentSlotID() {
        switch (this.getDeviceMode()) {
            case 0:
                return -1;
            case 1:
                return this.getPlayerInventory().currentItem;
            case 2:
                return this.getPlayerInventory().mainInventory.size() + this.getPlayerInventory().armorInventory.size();
            case 3:
                return this.slotID;
            default:
                return -1;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }


    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void clearFilter() {
        this.filter = null;
    }

    private boolean isUnfiltered() {
        return this.filter == null || this.filter.isEmpty();
    }

}
