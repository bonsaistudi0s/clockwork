package dev.xylonity.bonsai.clockwork.client.sound.handler.custom;

import dev.xylonity.bonsai.clockwork.client.sound.handler.SoundHandler;
import dev.xylonity.bonsai.clockwork.common.item.PotionSprayer;
import dev.xylonity.bonsai.clockwork.registry.ClockworkSounds;
import dev.xylonity.knightlib.mixin.AbstractTickableSoundInstanceAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PotionSprayerSoundHandler implements SoundHandler<Player> {

    private final Map<UUID, SprayerSoundState> stateByPlayer = new HashMap<>();

    @Override
    public boolean canHandle(Object object) {
        if (!(object instanceof Player player)) return false;

        return (player.getMainHandItem().getItem() instanceof PotionSprayer) || (player.getOffhandItem().getItem() instanceof PotionSprayer);
    }

    @Override
    public void tick(Player player) {
        UUID id = player.getUUID();
        ItemStack sprayer = getSprayerItem(player);

        if (sprayer.isEmpty()) {
            stopAll(player);
            return;
        }

        SprayerSoundState state = stateByPlayer.get(id);
        if (isSpraying(sprayer)) {
            if (state == null) {
                state = new SprayerSoundState(player);
                stateByPlayer.put(id, state);
                state.startSpraying();
            }
            else {
                state.tick(player);
            }

        }
        else {
            if (state != null && state.isActive()) {
                state.stopSpraying();
                stateByPlayer.remove(id);
            }

        }

    }

    @Override
    public void stopAll(Player player) {
        SprayerSoundState state = stateByPlayer.remove(player.getUUID());
        if (state != null) {
            state.stopSpraying();
        }

    }

    @Override
    public void clear() {
        stateByPlayer.values().forEach(SprayerSoundState::forceStop);
        stateByPlayer.clear();
    }

    private static ItemStack getSprayerItem(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof PotionSprayer) {
            return main;
        }

        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof PotionSprayer) {
            return off;
        }

        return ItemStack.EMPTY;
    }

    private static boolean isSpraying(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().getBoolean(PotionSprayer.NBT_SPRAYING);
    }

    private static class SprayerSoundState {

        private final UUID playerId;
        private SprayerLoop loopSound;
        private boolean active = false;

        SprayerSoundState(Player player) {
            this.playerId = player.getUUID();
        }

        void startSpraying() {
            if (active) return;
            active = true;

            Player player = getPlayer();
            if (player == null) return;

            playOnce(ClockworkSounds.SPRAYER_START.get(), player);

            if (loopSound == null || loopSound.isStopped()) {
                loopSound = new SprayerLoop(ClockworkSounds.SPRAYER_LOOP.get(), player);
                play(loopSound);
            }
            else {
                loopSound.updatePosition(player);
            }

        }

        void tick(Player player) {
            if (!active) return;

            if (loopSound == null || loopSound.isStopped()) {
                loopSound = new SprayerLoop(ClockworkSounds.SPRAYER_LOOP.get(), player);
                play(loopSound);
            }
            else {
                loopSound.updatePosition(player);
            }

        }

        void stopSpraying() {
            if (!active) return;

            stopImmediate(loopSound);
            loopSound = null;

            Player player = getPlayer();
            if (player != null) {
                playOnce(ClockworkSounds.SPRAYER_END.get(), player);
            }

            active = false;
        }

        void forceStop() {
            stopImmediate(loopSound);
            loopSound = null;
            active = false;
        }

        boolean isActive() {
            return active;
        }

        private Player getPlayer() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return null;
            }

            return minecraft.level.getPlayerByUUID(playerId);
        }

    }

    private static void play(AbstractTickableSoundInstance s) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() != null) {
            minecraft.getSoundManager().play(s);
        }
    }

    private static void playOnce(SoundEvent event, Player player) {
        if (player == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() != null) {
            minecraft.getSoundManager().play(new SimpleSoundInstance(
                    event,
                    SoundSource.PLAYERS,
                    0.6f,
                    1.0f,
                    SoundInstance.createUnseededRandom(),
                    (float) player.getX(),
                    (float) player.getY(),
                    (float) player.getZ()
            ));

        }

    }

    private static void stopImmediate(AbstractTickableSoundInstance s) {
        if (s != null) {
            ((AbstractTickableSoundInstanceAccessor) s).stopAccessor();
        }

    }

    private static final class SprayerLoop extends AbstractTickableSoundInstance {

        private Player player;

        SprayerLoop(SoundEvent event, Player player) {
            super(event, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.5f;
            this.pitch = 1.0f;
            this.x = (float) player.getX();
            this.y = (float) player.getY();
            this.z = (float) player.getZ();
        }

        void updatePosition(Player p) {
            if (p != null) {
                this.player = p;
                this.x = (float) p.getX();
                this.y = (float) p.getY();
                this.z = (float) p.getZ();
            }

        }

        @Override
        public void tick() {
            if (player == null || player.isRemoved() || !player.level().isClientSide) {
                this.stop(); return;
            }

            this.x = (float) player.getX();
            this.y = (float) player.getY();
            this.z = (float) player.getZ();

            ItemStack main = player.getMainHandItem();
            ItemStack off  = player.getOffhandItem();

            boolean spraying = false;
            ItemStack stack = ItemStack.EMPTY;
            if (main.getItem() instanceof PotionSprayer) {
                stack = main;
            }
            else if (off.getItem() instanceof PotionSprayer) {
                stack = off;
            }

            if (!stack.isEmpty()) {
                spraying = stack.hasTag() && stack.getOrCreateTag().getBoolean(PotionSprayer.NBT_SPRAYING);
            }

            if (!((main.getItem() instanceof PotionSprayer) || (off.getItem() instanceof PotionSprayer)) || !spraying) {
                this.stop();
            }
        }

    }

}