package com.alonie.advancedaccessorysystem.feature.headshulker.logic;

import com.alonie.advancedaccessorysystem.compat.api.HeadAccessorySlotRef;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionHandle;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionRegistry;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionState;
import com.alonie.advancedaccessorysystem.feature.headshulker.state.HeadShulkerSessionWatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

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

    public static boolean open(ServerPlayerEntity viewer, ServerPlayerEntity target, HeadAccessorySlotRef slotRef) {
        if (viewer == null || target == null || slotRef == null || slotRef.stack() == null || slotRef.stack().isEmpty()) {
            return false;
        }

        HeadShulkerSessionState session = HeadShulkerSessionRegistry.getOrCreate(
                HeadShulkerSessionRegistry.cosmeticKey(target.getUuid(), slotRef.slotIndex()),
                () -> HeadShulkerSessionState.forCosmetic(
                        HeadShulkerSessionRegistry.cosmeticKey(target.getUuid(), slotRef.slotIndex()),
                        target,
                        slotRef
                ),
                HeadShulkerSessionState.class
        );
        return openWithSession(viewer, target, session, slotRef.stack().getName());
    }

    public static boolean openVanilla(ServerPlayerEntity viewer, ServerPlayerEntity target, ItemStack stack) {
        if (viewer == null || target == null || stack == null || stack.isEmpty()) {
            return false;
        }

        HeadShulkerSessionState session = HeadShulkerSessionRegistry.getOrCreate(
                HeadShulkerSessionRegistry.vanillaKey(target.getUuid()),
                () -> HeadShulkerSessionState.forVanilla(HeadShulkerSessionRegistry.vanillaKey(target.getUuid()), target),
                HeadShulkerSessionState.class
        );
        return openWithSession(viewer, target, session, stack.getName());
    }

    private static boolean openWithSession(ServerPlayerEntity viewer, ServerPlayerEntity target, HeadShulkerSessionState session, Text title) {
        if (session == null || !session.isCarrierValid()) {
            return false;
        }

        if (viewer.currentScreenHandler instanceof BridgeScreenHandler existing) {
            if (existing.belongsTo(session, viewer, target)) {
                return true;
            }
            viewer.closeHandledScreen();
        }

        BridgeInventory inventory = new BridgeInventory(session);
        viewer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, serverPlayer) -> new BridgeScreenHandler(syncId, playerInventory, inventory, session, viewer, target),
                title
        ));
        playShulkerSound(target, SoundEvents.BLOCK_SHULKER_BOX_OPEN);
        return true;
    }

    private static void playShulkerSound(ServerPlayerEntity player, SoundEvent sound) {
        player.getEntityWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundCategory.BLOCKS,
                0.5F,
                1.0F
        );
    }

    private static final class BridgeScreenHandler extends ShulkerBoxScreenHandler implements HeadShulkerSessionWatcher {
        private final HeadShulkerSessionState session;
        private final ServerPlayerEntity viewer;
        private final ServerPlayerEntity target;
        private final BridgeInventory bridgeInventory;
        private final long openedAtTick;
        private boolean suppressContentCallback;
        private boolean forciblyClosing;

        private BridgeScreenHandler(int syncId,
                                    net.minecraft.entity.player.PlayerInventory playerInventory,
                                    BridgeInventory inventory,
                                    HeadShulkerSessionState session,
                                    ServerPlayerEntity viewer,
                                    ServerPlayerEntity target) {
            super(syncId, playerInventory, inventory);
            this.session = session;
            this.viewer = viewer;
            this.target = target;
            this.bridgeInventory = inventory;
            this.openedAtTick = target != null ? target.getEntityWorld().getTime() : 0L;
            this.session.addWatcher(this);
        }

        private boolean belongsTo(HeadShulkerSessionState expectedSession, ServerPlayerEntity expectedViewer, ServerPlayerEntity expectedTarget) {
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
                    && this.viewer.squaredDistanceTo(this.target) <= MAX_USE_DISTANCE_SQUARED
                    && this.session.isCarrierValid()
                    && this.viewer.currentScreenHandler == this;
        }

        @Override
        public void refreshFromSession() {
            this.suppressContentCallback = true;
            try {
                super.onContentChanged(this.bridgeInventory);
                this.sendContentUpdates();
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
                if (viewer != null && viewer.currentScreenHandler == this) {
                    viewer.closeHandledScreen();
                }
            } catch (Throwable ignored) {
            } finally {
                forciblyClosing = false;
            }
        }

        @Override
        public void onContentChanged(Inventory inventory) {
            super.onContentChanged(inventory);
            if (!suppressContentCallback) {
                session.markDirty();
            }
        }

        @Override
        public void onClosed(PlayerEntity player) {
            super.onClosed(player);
            session.removeWatcher(this);
            if (!forciblyClosing) {
                session.markDirty();
            }
            if (target != null && target.getEntityWorld().getTime() > openedAtTick) {
                playShulkerSound(target, SoundEvents.BLOCK_SHULKER_BOX_CLOSE);
            }
        }
    }

    private static final class BridgeInventory implements Inventory {
        private final HeadShulkerSessionState session;

        private BridgeInventory(HeadShulkerSessionState session) {
            this.session = session;
        }

        @Override
        public int size() {
            return 27;
        }

        @Override
        public boolean isEmpty() {
            return session.isEmpty();
        }

        @Override
        public ItemStack getStack(int slot) {
            return session.getStack(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            return session.removeStack(slot, amount);
        }

        @Override
        public ItemStack removeStack(int slot) {
            return session.removeStack(slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            session.setStack(slot, stack);
        }

        @Override
        public void markDirty() {
            session.markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return player != null && !player.isRemoved();
        }

        @Override
        public void clear() {
            session.clearItems();
        }
    }
}
