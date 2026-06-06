package com.alonie.advancedaccessorysystem.feature.ride.logic;

import com.alonie.advancedaccessorysystem.feature.ride.rules.RideAccessoryHelper;
import com.alonie.advancedaccessorysystem.feature.ride.sync.PlayerRideSyncManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;

public final class PlayerHeadAccessoryBreakManager {
    private PlayerHeadAccessoryBreakManager() {
    }

    public static void register() {
    }

    public static void tryBreakPlayerHeadAccessoryOnCritical(ServerPlayer attacker, Entity target) {
        if (attacker == null || !(target instanceof ServerPlayer victim) || attacker == victim) {
            return;
        }

        breakHeadAccessory(victim);
    }

    private static void breakHeadAccessory(ServerPlayer victim) {
        if (!RideAccessoryHelper.destroyBoatAccessory(victim)) {
            return;
        }

        PlayerRideSyncManager.forceDismountPassengers(victim, "head_accessory_broken");
        playBreakEffects((ServerLevel) victim.level(), victim);
    }

    private static void playBreakEffects(ServerLevel world, ServerPlayer victim) {
        double x = victim.getX();
        double y = victim.getY() + victim.getBbHeight() * 0.85D;
        double z = victim.getZ();

        world.playSound(null, x, y, z, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        world.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.FIRE_CORAL_BLOCK.defaultBlockState()),
                x,
                y + 0.2D,
                z,
                24,
                0.25D,
                0.25D,
                0.25D,
                0.05D
        );
    }
}
