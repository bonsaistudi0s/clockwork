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

    public static final ResourceEntry<SoundEvent> CLOCKWORK_ARROW_HIT_GROUND = SOUNDS.register("clockwork_arrow_hit_ground", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "clockwork_arrow_hit_ground")));

    public static final ResourceEntry<SoundEvent> DRAGONFLY_FLY = SOUNDS.register("dragonfly_fly_loop", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "dragonfly_fly_loop")));
    public static final ResourceEntry<SoundEvent> DRAGONFLY_HURT = SOUNDS.register("dragonfly_hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "dragonfly_hurt")));
    public static final ResourceEntry<SoundEvent> DRAGONFLY_IDLE = SOUNDS.register("dragonfly_idle_loop", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "dragonfly_idle_loop")));

    public static final ResourceEntry<SoundEvent> CLOCKWORK_BARREL_CROSSBOW_LOADING = SOUNDS.register("clockwork_barrel_crossbow_loading", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "clockwork_barrel_crossbow_loading")));
    public static final ResourceEntry<SoundEvent> CLOCKWORK_SCOPE_CROSSBOW_LOADING = SOUNDS.register("clockwork_crossbow_loading", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "clockwork_crossbow_loading")));
    public static final ResourceEntry<SoundEvent> CLOCKWORK_CROSSBOW_LOADING_END = SOUNDS.register("clockwork_crossbow_loading_end", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "clockwork_crossbow_loading_end")));
    public static final ResourceEntry<SoundEvent> CLOCKWORK_CROSSBOW_SHOOT = SOUNDS.register("clockwork_crossbow_shoot", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "clockwork_crossbow_shoot")));

    public static final ResourceEntry<SoundEvent> SPRAYER_END = SOUNDS.register("sprayer_end", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "sprayer_end")));
    public static final ResourceEntry<SoundEvent> SPRAYER_LOOP = SOUNDS.register("sprayer_loop", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "sprayer_loop")));
    public static final ResourceEntry<SoundEvent> SPRAYER_START = SOUNDS.register("sprayer_start", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "sprayer_start")));

    public static final ResourceEntry<SoundEvent> DRAGONFLY_GEAR = SOUNDS.register("dragonfly_gear", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Clockwork.MOD_ID, "dragonfly_gear")));

}