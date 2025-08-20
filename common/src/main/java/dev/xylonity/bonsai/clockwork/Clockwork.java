package dev.xylonity.bonsai.clockwork;

import dev.xylonity.bonsai.clockwork.platform.ClockworkPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class Clockwork {

    public static final String MOD_ID = "clockwork";
    public static final Logger LOGGER = LoggerFactory.getLogger("Clockwork");

    public static final ClockworkPlatform PLATFORM = ServiceLoader.load(ClockworkPlatform.class).findFirst().orElseThrow();

    public static void init() {
        ;;
    }

}