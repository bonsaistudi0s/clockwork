package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import dev.xylonity.knightlib.api.sound.persistent.KnightLibPersistentSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class ClockworkPersistentSounds {

    public static void init() {
        KnightLibPersistentSounds.profile(DragonflyEntity.class)
                .sound("fly")
                    .event(ClockworkSounds.DRAGONFLY_FLY)
                    .volume(0.4f)
                    .build()
                .sound("idle")
                    .event(ClockworkSounds.DRAGONFLY_IDLE)
                    .volume(0.4f)
                    .build()
                .submit();

        KnightLibPersistentSounds.profile(Player.class)
                .sound("spray")
                    .event(ClockworkSounds.SPRAYER_LOOP)
                    .source(SoundSource.PLAYERS)
                    .volume(0.5f)
                    .onStart(ClockworkSounds.SPRAYER_START, 0.6f)
                    .onStop(ClockworkSounds.SPRAYER_END, 0.6f)
                    .build()
                .submit();

    }

}
