package com.alonie.advancedaccessorysystem.feature.headshulker.state;

import com.alonie.advancedaccessorysystem.compat.CompatServices;
import com.alonie.advancedaccessorysystem.compat.api.HeadAccessorySlotRef;
import com.alonie.advancedaccessorysystem.feature.headshulker.logic.ShulkerBoxCompat;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.lang.reflect.Method;
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
    private final ServerPlayerEntity target;
    private final DefaultedList<ItemStack> items;
    private final CarrierReader reader;
    private final CarrierWriter writer;
    private final Set<HeadShulkerSessionWatcher> watchers = new HashSet<>();
    private ItemStack carrierStack;
    private ContainerComponent lastKnownComponent;
    private boolean syncing;

    private HeadShulkerSessionState(String key, ServerPlayerEntity target, CarrierReader reader, CarrierWriter writer) {
        this.key = key;
        this.target = target;
        this.reader = reader;
        this.writer = writer;
        this.items = DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY);
        this.carrierStack = sanitizeCarrier(reader.read());
        this.lastKnownComponent = this.carrierStack.get(DataComponentTypes.CONTAINER);
        if (this.lastKnownComponent != null) {
            this.lastKnownComponent.copyTo(this.items);
        }
    }

    public static HeadShulkerSessionState forCosmetic(String key, ServerPlayerEntity target, HeadAccessorySlotRef slotRef) {
        Method setStackMethod = findIntObjectMethod(slotRef.inventory().getClass(), "setStack", "method_5447");
        CarrierReader reader = () -> {
            HeadAccessorySlotRef current = CompatServices.headAccessoryBridge().getCosmeticHeadSlotRef(target);
            return current == null ? ItemStack.EMPTY : current.stack();
        };
        CarrierWriter writer = carrierStack -> {
            HeadAccessorySlotRef current = CompatServices.headAccessoryBridge().getCosmeticHeadSlotRef(target);
            if (current != null && setStackMethod != null) {
                setStackMethod.invoke(current.inventory(), current.slotIndex(), carrierStack);
            }
        };
        return new HeadShulkerSessionState(key, target, reader, writer);
    }

    public static HeadShulkerSessionState forVanilla(String key, ServerPlayerEntity player) {
        CarrierReader reader = () -> player.getEquippedStack(EquipmentSlot.HEAD);
        CarrierWriter writer = carrierStack -> player.equipStack(EquipmentSlot.HEAD, carrierStack);
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
        ItemStack removed = Inventories.splitStack(items, slot, amount);
        if (!removed.isEmpty()) {
            markDirty();
        }
        return removed;
    }

    public synchronized ItemStack removeStack(int slot) {
        ItemStack removed = Inventories.removeStack(items, slot);
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
            ContainerComponent sessionComponent = ContainerComponent.fromStacks(items);
            if (!Objects.equals(sessionComponent, this.lastKnownComponent)) {
                this.lastKnownComponent = sessionComponent;
                carrierStack.set(DataComponentTypes.CONTAINER, sessionComponent);
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
            ContainerComponent sessionComponent = ContainerComponent.fromStacks(items);
            if (!Objects.equals(sessionComponent, this.lastKnownComponent)) {
                this.lastKnownComponent = sessionComponent;
                carrierStack.set(DataComponentTypes.CONTAINER, sessionComponent);
                try {
                    writer.write(carrierStack);
                } catch (Throwable ignored) {
                }
                changed = true;
            }

            ContainerComponent current = carrierStack.get(DataComponentTypes.CONTAINER);
            if (!Objects.equals(current, this.lastKnownComponent)) {
                this.lastKnownComponent = current;
                for (int i = 0; i < this.items.size(); i++) {
                    this.items.set(i, ItemStack.EMPTY);
                }
                if (current != null) {
                    current.copyTo(this.items);
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

    private static Method findIntObjectMethod(Class<?> type, String... names) {
        for (String name : names) {
            for (Method method : type.getMethods()) {
                if (!method.getName().equals(name)) {
                    continue;
                }
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 2 && parameters[0] == int.class && ItemStack.class.isAssignableFrom(parameters[1])) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }
}
