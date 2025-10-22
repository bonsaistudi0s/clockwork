package dev.xylonity.bonsai.clockwork.client.sound;

import dev.xylonity.bonsai.clockwork.proxy.ISoundProxy;

public final class Sounds {

    private static volatile ISoundProxy INSTANCE = ISoundProxy.DUMMY;

    private Sounds() {}

    public static ISoundProxy proxy() {
        return INSTANCE;
    }

    public static void registerClientProxy(ISoundProxy clientProxy) {
        INSTANCE = (clientProxy != null) ? clientProxy : ISoundProxy.DUMMY;
    }

}