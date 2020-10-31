package com.hanyuone.checkpoint.container;

import com.hanyuone.checkpoint.register.BlockRegister;
import com.hanyuone.checkpoint.register.ContainerRegister;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;


public class CheckpointContainer extends Container {
    private final TileEntity tileEntity;
    private final IItemHandler inventory;

    private final BlockPos suitablePos;

    // GUI constants
    private static final int SLOT_SIZE = 18;
    private static final int INVENTORY_LEFT = 10;
    private static final int INVENTORY_TOP = 70;
    private static final int HOTBAR_TOP = 128;

    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLS = 9;

    public CheckpointContainer(int id, World world, BlockPos pos, PlayerInventory inventory, BlockPos suitablePos) {
        super(ContainerRegister.CHECKPOINT.get(), id);

        this.tileEntity = world.getTileEntity(pos);
        this.inventory = new InvWrapper(inventory);

        if (this.tileEntity instanceof CheckpointTileEntity) {
            this.tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler ->
                    addSlot(new SlotItemHandler(handler, 0, 46, 24))
            );
        }

        this.suitablePos = suitablePos;

        addInventorySlots();
    }

    private void addInventorySlots() {
        // The actual IInventory has the hotbar *first* and then the inventory,
        // but the GUI has the inventory first, so we have to first generate
        // the inventory (with initial index 9) and then generate the hotbar
        // so the GUI portion works
        int guiIndex = 9;

        // Generate inventory
        int invX = INVENTORY_LEFT;
        int invY = INVENTORY_TOP;

        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {
                addSlot(new SlotItemHandler(this.inventory, guiIndex, invX, invY));
                invX += SLOT_SIZE;
                guiIndex++;
            }

            invX = INVENTORY_LEFT;
            invY += SLOT_SIZE;
        }

        guiIndex = 0;

        // Generate hotbar
        int hotbarX = INVENTORY_LEFT;
        int hotbarY = HOTBAR_TOP;

        for (int col = 0; col < INVENTORY_COLS; col++) {
            addSlot(new SlotItemHandler(this.inventory, guiIndex, hotbarX, hotbarY));
            hotbarX += SLOT_SIZE;
            guiIndex++;
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos()), playerIn, BlockRegister.CHECKPOINT.get());
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        // 0 is Checkpoint
        // 1-27 is inventory
        // 28-36 is hotbar
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index == 0) {
                // Checkpoint -> inventory
                // Attempting to move ender eyes into the player's inventory
                if (!this.mergeItemStack(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(stack, result);
            } else {
                // Inventory -> Checkpoint
                if (stack.getItem() == Items.ENDER_PEARL) {
                    // Attempting to move ender eyes from the inventory into the Checkpoint
                    if (!this.mergeItemStack(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.mergeItemStack(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 37 && !this.mergeItemStack(stack, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }

        return result;
    }

    public TileEntity getTileEntity() {
        return this.tileEntity;
    }

    public BlockPos getSuitablePos() {
        return this.suitablePos;
    }
}
