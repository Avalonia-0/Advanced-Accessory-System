package com.alonie.advancedaccessorysystem.mixin.shared;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityRideDebugMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedAccessorySystem/RideDetachDebug");

    @Inject(method = "stopRiding", at = @At("HEAD"))
    private void advancedaccessorysystem$logStopRiding(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Entity vehicle = self.getVehicle();
        if (!(self instanceof PlayerEntity) && !(vehicle instanceof PlayerEntity)) {
            return;
        }

        LOGGER.warn(
                "stopRiding() called: rider={}, vehicle={}, riderHasVehicleBefore={}, vehiclePassengersBefore={}",
                describeEntity(self),
                describeEntity(vehicle),
                self.hasVehicle(),
                vehicle == null ? "null" : describePassengerIds(vehicle)
        );
        LOGGER.warn("stopRiding() stack trace", new Throwable("ride-detach-trace"));
    }

    @Inject(method = "removePassenger", at = @At("HEAD"))
    private void advancedaccessorysystem$logRemovePassenger(Entity passenger, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof PlayerEntity) && !(passenger instanceof PlayerEntity)) {
            return;
        }

        LOGGER.warn(
                "removePassenger() called: vehicle={}, passenger={}, vehiclePassengersBefore={}",
                describeEntity(self),
                describeEntity(passenger),
                describePassengerIds(self)
        );
        LOGGER.warn("removePassenger() stack trace", new Throwable("ride-remove-passenger-trace"));
    }

    @Inject(method = "removeAllPassengers", at = @At("HEAD"))
    private void advancedaccessorysystem$logRemoveAllPassengers(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        boolean relevant = self instanceof PlayerEntity;
        if (!relevant) {
            for (Entity passenger : self.getPassengerList()) {
                if (passenger instanceof PlayerEntity) {
                    relevant = true;
                    break;
                }
            }
        }
        if (!relevant) {
            return;
        }

        LOGGER.warn(
                "removeAllPassengers() called: vehicle={}, vehiclePassengersBefore={}",
                describeEntity(self),
                describePassengerIds(self)
        );
        LOGGER.warn("removeAllPassengers() stack trace", new Throwable("ride-remove-all-passengers-trace"));
    }

    private static String describeEntity(Entity entity) {
        if (entity == null) {
            return "null-entity";
        }
        String name = entity.getName().getString();
        return name + "[type=" + entity.getType() + ",id=" + entity.getId() + ",uuid=" + entity.getUuidAsString() + "]";
    }

    private static String describePassengerIds(Entity entity) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Entity passenger : entity.getPassengerList()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append(passenger.getId())
                    .append(':')
                    .append(passenger.getName().getString())
                    .append('@')
                    .append(passenger.getType());
        }
        builder.append(']');
        return builder.toString();
    }
}
