package dev.xylonity.bonsai.clockwork.registry;

import dev.xylonity.bonsai.clockwork.Clockwork;
import dev.xylonity.knightlib.api.registrar.ResourceDispatcher;
import dev.xylonity.knightlib.api.registrar.ResourceEntry;
import dev.xylonity.knightlib.api.registrar.ResourceRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ClockworkSounds {

    public static final ResourceRegistry<SoundEvent> SOUNDS = ResourceDispatcher.create(BuiltInRegistries.SOUND_EVENT, Clockwork.MOD_ID);

    public static final ResourceEntry<SoundEvent> CLOCKWORK_WINGS_CLOSE = SOUNDS.register("clockwork_wings_close", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "clockwork_wings_close")));
    public static final ResourceEntry<SoundEvent> CLOCKWORK_WINGS_FLAP = SOUNDS.register("clockwork_wings_flap", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "clockwork_wings_flap")));
    public static final ResourceEntry<SoundEvent> CLOCKWORK_WINGS_OPEN = SOUNDS.register("clockwork_wings_open", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "clockwork_wings_open")));

    public static final ResourceEntry<SoundEvent> DRAGONFLY_FLY = SOUNDS.register("dragonfly_fly_loop", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "dragonfly_fly_loop")));
    public static final ResourceEntry<SoundEvent> DRAGONFLY_HURT = SOUNDS.register("dragonfly_hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "dragonfly_hurt")));
    public static final ResourceEntry<SoundEvent> DRAGONFLY_IDLE = SOUNDS.register("dragonfly_idle_loop", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "dragonfly_idle_loop")));

}
