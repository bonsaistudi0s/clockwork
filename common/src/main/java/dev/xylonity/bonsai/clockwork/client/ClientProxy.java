package dev.xylonity.bonsai.clockwork.client;

import dev.xylonity.bonsai.clockwork.proxy.IProxy;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class ClientProxy implements IProxy {

    @Override
    public void playScopeCrossbowSound() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), ClockworkSounds.CLOCKWORK_CROSSBOW_LOADING_END.get(), SoundSource.PLAYERS, 1, 1, true);
        }

    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

}
