package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.common.item.crossbow.ScopeCrossbow;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {

    @Shadow @Final private Entity entity;

    @Inject(method = "sendChanges", at = @At("HEAD"), require = 0)
    private void clockwork$syncScopeArrowsEveryTick(CallbackInfo ci) {
        if (clockwork$isScopeArrow() && clockwork$maxComponent(entity.getDeltaMovement()) > 3.9) {
            entity.hasImpulse = true;
        }

    }

    @ModifyArg(method = {"sendChanges", "sendPairingData"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundSetEntityMotionPacket;<init>(ILnet/minecraft/world/phys/Vec3;)V"), index = 1, require = 0)
    private Vec3 clockwork$keepScopeArrowDirection(Vec3 deltaMovement) {
        final double max = clockwork$maxComponent(deltaMovement);
        if (max > 3.9 && clockwork$isScopeArrow()) {
            return deltaMovement.scale(3.9 / max);
        }

        return deltaMovement;
    }

    @Unique
    private boolean clockwork$isScopeArrow() {
        return entity instanceof AbstractArrow && entity.getTags().contains(ScopeCrossbow.SCOPE_ARROW_TAG);
    }

    @Unique
    private static double clockwork$maxComponent(Vec3 vec) {
        return Math.max(Math.abs(vec.x), Math.max(Math.abs(vec.y), Math.abs(vec.z)));
    }

}
