package com.alonie.advancedaccessorysystem.feature.headshulker.logic;

import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotProvider;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.VanillaHeadSlotProvider;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionHandle;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionRegistry;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionState;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionWatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

public final class CosmeticHeadShulkerBridge {
    private static final double MAX_USE_DISTANCE_SQUARED = 64.0D;

    private CosmeticHeadShulkerBridge() {
    }

    public static void tickAllSessions() {
        for (HeadShulkerSessionHandle session : HeadShulkerSessionRegistry.snapshot()) {
            try {
                session.pollCarrierAndSync();
            } catch (Throwable ignored) {
            }
        }
    }

    public static boolean openVanilla(ServerPlayer viewer, ServerPlayer target, ItemStack stack) {
        return openVanilla(viewer, target, stack, new VanillaHeadSlotProvider());
    }

    public static boolean openVanilla(ServerPlayer viewer, ServerPlayer target, ItemStack stack,
                                       AccessorySlotProvider provider) {
        if (viewer == null || target == null || stack == null || stack.isEmpty()) {
            return false;
        }

        String key = HeadShulkerSessionRegistry.slotKey(target.getUUID(), provider.name());
        HeadShulkerSessionState session = HeadShulkerSessionRegistry.getOrCreate(
                key,
                () -> HeadShulkerSessionState.forSlot(key, target, provider),
                HeadShulkerSessionState.class
        );
        return openWithSession(viewer, target, session, stack.getHoverName());
    }

    private static boolean openWithSession(ServerPlayer viewer, ServerPlayer target, HeadShulkerSessionState session, Component title) {
        if (session == null || !session.isCarrierValid()) {
            return false;
        }

        if (viewer.containerMenu instanceof BridgeScreenHandler existing) {
            if (existing.belongsTo(session, viewer, target)) {
                return true;
            }
            viewer.closeContainer();
        }

        BridgeInventory inventory = new BridgeInventory(session);
        viewer.openMenu(new SimpleMenuProvider(
                (syncId, playerInventory, serverPlayer) -> new BridgeScreenHandler(syncId, playerInventory, inventory, session, viewer, target),
                title
        ));
        playShulkerSound(target, SoundEvents.SHULKER_BOX_OPEN);
        return true;
    }

    private static void playShulkerSound(ServerPlayer player, SoundEvent sound) {
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundSource.BLOCKS,
                0.5F,
                1.0F
        );
    }

    private static final class BridgeScreenHandler extends ShulkerBoxMenu implements HeadShulkerSessionWatcher {
        private final HeadShulkerSessionState session;
        private final ServerPlayer viewer;
        private final ServerPlayer target;
        private final BridgeInventory bridgeInventory;
        private final long openedAtTick;
        private boolean suppressContentCallback;
        private boolean forciblyClosing;

        private BridgeScreenHandler(int syncId,
                                    net.minecraft.world.entity.player.Inventory playerInventory,
                                    BridgeInventory inventory,
                                    HeadShulkerSessionState session,
                                    ServerPlayer viewer,
                                    ServerPlayer target) {
            super(syncId, playerInventory, inventory);
            this.session = session;
            this.viewer = viewer;
            this.target = target;
            this.bridgeInventory = inventory;
            this.openedAtTick = target != null ? target.level().getGameTime() : 0L;
            this.session.addWatcher(this);
        }

        private boolean belongsTo(HeadShulkerSessionState expectedSession, ServerPlayer expectedViewer, ServerPlayer expectedTarget) {
            return this.session == expectedSession && this.viewer == expectedViewer && this.target == expectedTarget;
        }

        @Override
        public boolean isStillValidRemoteUse() {
            return this.viewer != null
                    && this.target != null
                    && !this.viewer.isRemoved()
                    && !this.target.isRemoved()
                    && this.viewer.isAlive()
                    && this.target.isAlive()
                    && !this.viewer.isSpectator()
                    && !this.target.isSpectator()
                    && this.viewer.distanceToSqr(this.target) <= MAX_USE_DISTANCE_SQUARED
                    && this.session.isCarrierValid()
                    && this.viewer.containerMenu == this;
        }

        @Override
        public void refreshFromSession() {
            this.suppressContentCallback = true;
            try {
                super.slotsChanged(this.bridgeInventory);
                this.broadcastChanges();
            } catch (Throwable ignored) {
            } finally {
                this.suppressContentCallback = false;
            }
        }

        @Override
        public void forceClose() {
            if (forciblyClosing) {
                return;
            }
            forciblyClosing = true;
            try {
                if (viewer != null && viewer.containerMenu == this) {
                    viewer.closeContainer();
                }
            } catch (Throwable ignored) {
            } finally {
                forciblyClosing = false;
            }
        }

        @Override
        public void slotsChanged(Container inventory) {
            super.slotsChanged(inventory);
            if (!suppressContentCallback) {
                session.markDirty();
            }
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            session.removeWatcher(this);
            if (!forciblyClosing) {
                session.markDirty();
            }
            if (target != null && target.level().getGameTime() > openedAtTick) {
                playShulkerSound(target, SoundEvents.SHULKER_BOX_CLOSE);
            }
        }
    }

    private static final class BridgeInventory implements Container {
        private final HeadShulkerSessionState session;

        private BridgeInventory(HeadShulkerSessionState session) {
            this.session = session;
        }

        @Override
        public int getContainerSize() {
            return 27;
        }

        @Override
        public boolean isEmpty() {
            return session.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return session.getStack(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return session.removeStack(slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return session.removeStack(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            session.setStack(slot, stack);
        }

        @Override
        public void setChanged() {
            session.markDirty();
        }

        @Override
        public boolean stillValid(Player player) {
            return player != null && !player.isRemoved();
        }

        @Override
        public void clearContent() {
            session.clearItems();
        }
    }
}
