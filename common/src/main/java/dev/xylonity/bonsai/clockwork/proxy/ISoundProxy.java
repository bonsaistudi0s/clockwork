package dev.xylonity.bonsai.clockwork.proxy;

import net.minecraft.world.entity.Entity;

/**
 * Client-sided sound proxy abstraction. This proxy is agnostic, as the proxy bridge definition states
 */
public interface ISoundProxy {

    ISoundProxy DUMMY = new ISoundProxy() { ;; };

    default void tickSounds(Entity entity) { ;; }
    default void stopAllFor(Entity entity) { ;; }
    default void clearAll() { ;; }
}