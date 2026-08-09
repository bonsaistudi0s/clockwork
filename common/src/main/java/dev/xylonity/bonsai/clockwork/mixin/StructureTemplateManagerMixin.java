package dev.xylonity.bonsai.clockwork.mixin;

import dev.xylonity.bonsai.clockwork.Clockwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {

    @Unique
    private static final ResourceLocation CLOCKWORK$EMPTY_CAGE = ResourceLocation.fromNamespaceAndPath("minecraft", "pillager_outpost/feature_cage2");
    @Unique
    private static final String CLOCKWORK$BROKEN_DRAGONFLY = Clockwork.MOD_ID + ":broken_dragonfly";
    @Unique
    private static final BlockPos CLOCKWORK$DRAGONFLY_BLOCK_POS = new BlockPos(3, 0, 3);

    @Inject(method = "tryLoad", at = @At("RETURN"))
    private void clockwork$addBrokenDragonfly(ResourceLocation id, CallbackInfoReturnable<Optional<StructureTemplate>> cir) {
        if (!CLOCKWORK$EMPTY_CAGE.equals(id)) {
            return;
        }

        cir.getReturnValue().ifPresent(template -> {
            final List<StructureTemplate.StructureEntityInfo> entities = ((StructureTemplateAccessor) template).clockwork$getEntityInfoList();
            if (entities.stream().anyMatch(info -> CLOCKWORK$BROKEN_DRAGONFLY.equals(info.nbt.getString("id")))) {
                return;
            }

            final CompoundTag entityTag = new CompoundTag();
            entityTag.putString("id", CLOCKWORK$BROKEN_DRAGONFLY);
            entityTag.putBoolean("PersistenceRequired", true);

            entities.add(new StructureTemplate.StructureEntityInfo(
                    Vec3.atBottomCenterOf(CLOCKWORK$DRAGONFLY_BLOCK_POS),
                    CLOCKWORK$DRAGONFLY_BLOCK_POS,
                    entityTag
            ));

        });

    }

}