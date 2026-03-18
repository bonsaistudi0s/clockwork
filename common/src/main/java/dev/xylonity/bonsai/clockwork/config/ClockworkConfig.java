package dev.xylonity.bonsai.clockwork.config;

import dev.xylonity.knightlib.api.config.AutoConfig;
import dev.xylonity.knightlib.api.config.ConfigEntry;

@AutoConfig(
        file = "clockwork",
        accentColor = 0xFFC48533
)
public final class ClockworkConfig {

    @ConfigEntry(
            category = "Clockwork Wings",
            comment = "Default durability",
            min = 0, max = 100000
    )
    public static int CLOCKWORK_WINGS_DEFAULT_DURABILITY = 320;

    @ConfigEntry(
            category = "Clockwork Wings",
            comment = "Boost velocity multiplier",
            min = 0.0, max = 1000.0
    )
    public static double CLOCKWORK_WINGS_BOOST_VELOCITY = 1.0;

    @ConfigEntry(
            category = "Clockwork Wings",
            comment = "Default velocity reduction multiplier. This is multiplied each tick, so even high values can stop the entity",
            min = 0.0, max = 1000.0
    )
    public static double CLOCKWORK_WINGS_DEFAULT_VELOCITY = 0.97725;

    @ConfigEntry(
            category = "Clockwork Wings",
            comment = "Default flying sink force",
            min = 0.0, max = 1000.0
    )
    public static double CLOCKWORK_WINGS_DEFAULT_SINK = 0.004225;

    @ConfigEntry(
            category = "Clockwork Wings",
            comment = "Boost cooldown ticks",
            min = 0, max = 1000
    )
    public static int CLOCKWORK_WINGS_BOOST_COOLDOWN_TICKS = 60;

    @ConfigEntry(
            category = "Dragonfly",
            comment = "Default health",
            min = 0.0, max = 1000.0
    )
    public static double DRAGONFLY_DEFAULT_HEALTH = 20;

    @ConfigEntry(
            category = "Dragonfly",
            comment = "Default flying speed",
            min = 0.0, max = 1000.0
    )
    public static double DRAGONFLY_DEFAULT_FLYING_SPEED = 0.65f;

    @ConfigEntry(
            category = "Dragonfly",
            comment = "Default walking speed",
            min = 0.0, max = 1000.0
    )
    public static double DRAGONFLY_DEFAULT_WALKING_SPEED = 0.20f;

    @ConfigEntry(
            category = "Dragonfly",
            comment = "How much health does the clockwork gear restore",
            min = 0.0, max = 1000.0
    )
    public static double CLOCKWORK_GEAR_HEAL_AMOUNT = 3;

    @ConfigEntry(
            category = "Clockwork Drill",
            comment = "Number of clockwork gears needed to repair the drill once it has broken",
            min = 1, max = 100
    )
    public static int DRILL_CLOCKWORK_GEAR_AMOUNT = 1;

}
