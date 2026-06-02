package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.common.entity.passive.DragonflyEntity;
import dev.xylonity.bonsai.clockwork.common.entity.tool.ClockworkDrillEntity;
import dev.xylonity.knightlib.api.sound.persistent.KnightLibPersistentSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class ClockworkPersistentSounds {

    public static void init() {
        KnightLibPersistentSounds.profile(DragonflyEntity.class)
                .sound("clockwork:fly")
                    .event(ClockworkSounds.DRAGONFLY_FLY)
                    .volume(0.3f)
                    .build()
                .sound("clockwork:idle")
                    .event(ClockworkSounds.DRAGONFLY_IDLE)
                    .volume(0.3f)
                    .build()
                .submit();

        KnightLibPersistentSounds.profile(Player.class)
                .sound("clockwork:spray")
                    .event(ClockworkSounds.SPRAYER_LOOP)
                    .source(SoundSource.PLAYERS)
                    .volume(0.5f)
                    .onStart(ClockworkSounds.SPRAYER_START, 0.6f)
                    .onStop(ClockworkSounds.SPRAYER_END, 0.6f)
                    .build()
                .sound("clockwork:flamethrower")
                    .event(ClockworkSounds.FLAMETHROWER_LOOP)
                        .source(SoundSource.PLAYERS)
                        .volume(1f)
                    .onStop(ClockworkSounds.FLAMETHROWER_END, 0.6f)
                    .build()
                .submit();

        KnightLibPersistentSounds.profile(ClockworkDrillEntity.class)
                .sound("clockwork:drill_loop")
                    .event(ClockworkSounds.DRILL_LOOP)
                    .volume(1f)
                    .build()
                .sound("clockwork:drill_walk_loop")
                    .event(ClockworkSounds.DRILL_WALK_LOOP)
                    .volume(0.4f)
                    .build()
                .sound("clockwork:drill_idle")
                    .event(ClockworkSounds.DRILL_IDLE)
                    .volume(0.6f)
                    .build()
                .submit();

    }

}
