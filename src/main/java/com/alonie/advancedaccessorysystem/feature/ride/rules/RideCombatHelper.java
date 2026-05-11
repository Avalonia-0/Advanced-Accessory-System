package com.alonie.advancedaccessorysystem.feature.ride.rules;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public final class RideCombatHelper {
    private static final double PASSENGER_LEG_COLLISION_CUTOFF_RATIO = 0.45D;
    private static final double PASSENGER_LEG_COLLISION_MAX_CUTOFF = 0.75D;
    private static final double PASSENGER_MIN_REMAINING_HITBOX_HEIGHT = 0.60D;

    private RideCombatHelper() {
    }

    public static boolean shouldTrimPassengerHitbox(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return false;
        }

        // ServerPlayerEntity can recalculate its bounding box during construction,
        // before spectator/network state has been initialized.
        return player.getVehicle() instanceof PlayerEntity;
    }

    public static Box trimPassengerHitbox(Box box) {
        double boxHeight = box.maxY - box.minY;
        double maxCutoff = boxHeight - PASSENGER_MIN_REMAINING_HITBOX_HEIGHT;
        if (maxCutoff <= 0.0D) {
            return box;
        }

        double cutoff = Math.min(PASSENGER_LEG_COLLISION_MAX_CUTOFF, boxHeight * PASSENGER_LEG_COLLISION_CUTOFF_RATIO);
        cutoff = Math.min(cutoff, maxCutoff);

        if (cutoff <= 0.0D) {
            return box;
        }

        return new Box(box.minX, box.minY + cutoff, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    public static Box getPassengerCollisionBox(Entity entity, Box originalBox) {
        if (!shouldTrimPassengerHitbox(entity)) {
            return originalBox;
        }

        return trimPassengerHitbox(originalBox);
    }

    public static boolean isRidePair(PlayerEntity attacker, Entity target) {
        if (!(target instanceof PlayerEntity targetPlayer)) {
            return false;
        }

        return attacker.getVehicle() == targetPlayer || targetPlayer.getVehicle() == attacker;
    }
}
