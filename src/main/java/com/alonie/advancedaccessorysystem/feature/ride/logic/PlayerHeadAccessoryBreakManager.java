package com.alonie.advancedaccessorysystem.feature.ride.logic;

import com.alonie.advancedaccessorysystem.feature.ride.rules.RideAccessoryHelper;
import com.alonie.advancedaccessorysystem.feature.ride.sync.PlayerRideSyncManager;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class PlayerHeadAccessoryBreakManager {
    private PlayerHeadAccessoryBreakManager() {
    }

    public static void register() {
    }

    public static void tryBreakPlayerHeadAccessoryOnCritical(ServerPlayerEntity attacker, Entity target) {
        if (attacker == null || !(target instanceof ServerPlayerEntity victim) || attacker == victim) {
            return;
        }

        breakHeadAccessory(victim);
    }

    private static void breakHeadAccessory(ServerPlayerEntity victim) {
        if (!RideAccessoryHelper.destroyBoatAccessory(victim)) {
            return;
        }

        PlayerRideSyncManager.forceDismountPassengers(victim, "head_accessory_broken");
        playBreakEffects((ServerWorld) victim.getEntityWorld(), victim);
    }

    private static void playBreakEffects(ServerWorld world, ServerPlayerEntity victim) {
        double x = victim.getX();
        double y = victim.getY() + victim.getHeight() * 0.85D;
        double z = victim.getZ();

        world.playSound(null, x, y, z, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        world.spawnParticles(
                new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.FIRE_CORAL_BLOCK.getDefaultState()),
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
