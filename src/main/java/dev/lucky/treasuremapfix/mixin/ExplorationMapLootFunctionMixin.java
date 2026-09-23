package dev.lucky.treasuremapfix.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ExplorationMapLootFunction;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplorationMapLootFunction.class)
public abstract class ExplorationMapLootFunctionMixin {
    @Unique
    private static final int TREASURE_MAP_SEARCH_RADIUS_CHUNKS = 100;

    @Shadow
    @Final
    private TagKey<Structure> destination;

    @Unique
    private boolean treasureMapFix$isBuriedTreasureMapLookup() {
        return StructureTags.ON_TREASURE_MAPS.equals(this.destination);
    }

    @ModifyArg(
        method = "process",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;locateStructure(Lnet/minecraft/registry/tag/TagKey;Lnet/minecraft/util/math/BlockPos;IZ)Lnet/minecraft/util/math/BlockPos;"
        ),
        index = 2
    )
    private int treasureMapFix$extendBuriedTreasureSearchRadius(int originalRadius) {
        if (!treasureMapFix$isBuriedTreasureMapLookup()) {
            return originalRadius;
        }

        return Math.max(originalRadius, TREASURE_MAP_SEARCH_RADIUS_CHUNKS);
    }

    @Inject(method = "process", at = @At("RETURN"))
    private void treasureMapFix$discardUnresolvedBuriedTreasureMap(
        ItemStack input,
        LootContext context,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        if (!treasureMapFix$isBuriedTreasureMapLookup() || !input.isOf(Items.MAP)) {
            return;
        }

        ItemStack result = cir.getReturnValue();
        if (result != null && result.isOf(Items.MAP)) {
            result.setCount(0);
        }
    }
}
