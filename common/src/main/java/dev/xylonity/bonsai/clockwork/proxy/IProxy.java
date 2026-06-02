package dev.xylonity.bonsai.clockwork.proxy;

import net.minecraft.world.entity.player.Player;

public interface IProxy {

    default void playScopeCrossbowSound() {
        ;;
    }

    default Player getClientPlayer() {
        return null;
    }

    default void registerClientEvents() {
        ;;
    }

    default void trySpawnFirstPersonFlameParticles(Player player) {
        ;;
    }

}
