package dev.xylonity.bonsai.clockwork.common.biome;

import com.mojang.serialization.Codec;
import dev.xylonity.bonsai.clockwork.Clockwork;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ClockworkSpawnBiomeModifier implements BiomeModifier {

    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Clockwork.MOD_ID);

    private static final RegistryObject<Codec<? extends BiomeModifier>> SERIALIZER = RegistryObject.create(Clockwork.resource("clockwork_mob_spawns"), ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Clockwork.MOD_ID);

    @Override
    public void modify(Holder<Biome> holder, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD) {
            ClockworkMobSpawns.addBiomeSpawns(holder, builder);
        }

    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return SERIALIZER.get();
    }

    public static Codec<ClockworkSpawnBiomeModifier> makeCodec() {
        return Codec.unit(ClockworkSpawnBiomeModifier::new);
    }

}
