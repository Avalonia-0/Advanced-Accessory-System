package com.alonie.advancedaccessorysystem.feature.headshulker.state;

import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotProvider;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.VanillaHeadSlotProvider;
import com.alonie.advancedaccessorysystem.feature.headshulker.logic.ShulkerBoxCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.NonNullList;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class HeadShulkerSessionState implements HeadShulkerSessionHandle {
    private static final int SLOT_COUNT = 27;

    private interface CarrierReader {
        ItemStack read();
    }

    private interface CarrierWriter {
        void write(ItemStack carrierStack) throws Throwable;
    }

    private final String key;
    private final ServerPlayer target;
    private final NonNullList<ItemStack> items;
    private final CarrierReader reader;
    private final CarrierWriter writer;
    private final Set<HeadShulkerSessionWatcher> watchers = new HashSet<>();
    private ItemStack carrierStack;
    private ItemContainerContents lastKnownComponent;
    private boolean syncing;

    private HeadShulkerSessionState(String key, ServerPlayer target, CarrierReader reader, CarrierWriter writer) {
        this.key = key;
        this.target = target;
        this.reader = reader;
        this.writer = writer;
        this.items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        this.carrierStack = sanitizeCarrier(reader.read());
        this.lastKnownComponent = this.carrierStack.get(DataComponents.CONTAINER);
        if (this.lastKnownComponent != null) {
            this.lastKnownComponent.copyInto(this.items);
        }
    }

    public static HeadShulkerSessionState forVanilla(String key, ServerPlayer player) {
        return forSlot(key, player, new VanillaHeadSlotProvider());
    }

    public static HeadShulkerSessionState forSlot(String key, ServerPlayer player, AccessorySlotProvider slot) {
        CarrierReader reader = () -> slot.getStack(player);
        CarrierWriter writer = carrierStack -> {
            if (!slot.setStack(player, carrierStack)) {
                throw new IllegalStateException("Failed to write to slot " + slot.name());
            }
        };
        return new HeadShulkerSessionState(key, player, reader, writer);
    }

    public synchronized boolean isCarrierValid() {
        ItemStack fresh = sanitizeCarrier(reader.read());
        return !fresh.isEmpty() && ShulkerBoxCompat.isShulkerBox(fresh);
    }

    public synchronized void addWatcher(HeadShulkerSessionWatcher watcher) {
        watchers.add(watcher);
    }

    public synchronized void removeWatcher(HeadShulkerSessionWatcher watcher) {
        watchers.remove(watcher);
        cleanupIfEmpty();
    }

    public synchronized boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public synchronized ItemStack getStack(int slot) {
        return items.get(slot);
    }

    public synchronized ItemStack removeStack(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            markDirty();
        }
        return removed;
    }

    public synchronized ItemStack removeStack(int slot) {
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty()) {
            markDirty();
        }
        return removed;
    }

    public synchronized void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        markDirty();
    }

    public synchronized void clearItems() {
        items.clear();
        markDirty();
    }

    public synchronized void markDirty() {
        if (syncing) {
            return;
        }
        ItemStack freshCarrier = sanitizeCarrier(reader.read());
        if (freshCarrier.isEmpty() || !ShulkerBoxCompat.isShulkerBox(freshCarrier)) {
            closeAllWatchers();
            return;
        }

        syncing = true;
        try {
            carrierStack = freshCarrier;
            ItemContainerContents sessionComponent = ItemContainerContents.fromItems(items);
            if (!Objects.equals(sessionComponent, this.lastKnownComponent)) {
                this.lastKnownComponent = sessionComponent;
                carrierStack.set(DataComponents.CONTAINER, sessionComponent);
                try {
                    writer.write(carrierStack);
                } catch (Throwable ignored) {
                }
                syncWatchers();
            }
        } finally {
            syncing = false;
        }
    }

    @Override
    public synchronized void pollCarrierAndSync() {
        if (syncing) {
            return;
        }
        if (watchers.isEmpty()) {
            cleanupIfEmpty();
            return;
        }

        syncing = true;
        try {
            validateWatchers();
            if (watchers.isEmpty()) {
                cleanupIfEmpty();
                return;
            }

            ItemStack freshCarrier = sanitizeCarrier(reader.read());
            if (freshCarrier.isEmpty() || !ShulkerBoxCompat.isShulkerBox(freshCarrier)) {
                closeAllWatchers();
                cleanupIfEmpty();
                return;
            }
            carrierStack = freshCarrier;

            boolean changed = false;
            ItemContainerContents sessionComponent = ItemContainerContents.fromItems(items);
            if (!Objects.equals(sessionComponent, this.lastKnownComponent)) {
                this.lastKnownComponent = sessionComponent;
                carrierStack.set(DataComponents.CONTAINER, sessionComponent);
                try {
                    writer.write(carrierStack);
                } catch (Throwable ignored) {
                }
                changed = true;
            }

            ItemContainerContents current = carrierStack.get(DataComponents.CONTAINER);
            if (!Objects.equals(current, this.lastKnownComponent)) {
                this.lastKnownComponent = current;
                for (int i = 0; i < this.items.size(); i++) {
                    this.items.set(i, ItemStack.EMPTY);
                }
                if (current != null) {
                    current.copyInto(this.items);
                }
                changed = true;
            }

            if (changed) {
                syncWatchers();
            }
        } finally {
            syncing = false;
        }
    }

    private void validateWatchers() {
        Set<HeadShulkerSessionWatcher> snapshot = new HashSet<>(watchers);
        for (HeadShulkerSessionWatcher watcher : snapshot) {
            if (!watcher.isStillValidRemoteUse()) {
                watchers.remove(watcher);
                watcher.forceClose();
            }
        }
    }

    private void closeAllWatchers() {
        Set<HeadShulkerSessionWatcher> snapshot = new HashSet<>(watchers);
        watchers.clear();
        for (HeadShulkerSessionWatcher watcher : snapshot) {
            watcher.forceClose();
        }
        cleanupIfEmpty();
    }

    private void cleanupIfEmpty() {
        if (watchers.isEmpty()) {
            HeadShulkerSessionRegistry.remove(key, this);
        }
    }

    private void syncWatchers() {
        Set<HeadShulkerSessionWatcher> snapshot = new HashSet<>(watchers);
        for (HeadShulkerSessionWatcher watcher : snapshot) {
            try {
                watcher.refreshFromSession();
            } catch (Throwable ignored) {
            }
        }
    }

    private static ItemStack sanitizeCarrier(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }
}
