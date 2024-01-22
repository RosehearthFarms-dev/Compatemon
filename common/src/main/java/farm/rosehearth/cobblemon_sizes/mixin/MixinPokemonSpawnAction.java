package farm.rosehearth.cobblemon_sizes.mixin;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType;
import com.cobblemon.mod.common.api.spawning.context.SpawningContext;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import farm.rosehearth.cobblemon_sizes.CobblemonSizes;

import virtuoel.pehkui.util.ScaleUtils;
import virtuoel.pehkui.api.*;
@Mixin(value = PokemonSpawnAction.class, remap = false)
abstract class MixinPokemonSpawnAction extends SpawnAction<PokemonEntity> {

    public MixinPokemonSpawnAction(@NotNull SpawningContext ctx, @NotNull SpawnDetail detail) {

        super(ctx, detail);
    }

    @Inject(method = "createEntity()Lcom/cobblemon/mod/common/entity/pokemon/PokemonEntity;", at = @At("RETURN"))
    private void adjustEntitySizing(CallbackInfoReturnable<PokemonEntity> cir) {
       // CustomPokemonProperty.Companion.getProperties();
        float size_scale = CobblemonSizes.getSizeModifier();
        float weight_scale = CobblemonSizes.getWeightModifier();
        CobblemonSizes.LOGGER.debug("Adjusting size of " + cir.getReturnValue().getPokemon().getDisplayName() + " to " + size_scale);
        ScaleUtils.setScaleOnSpawn(cir.getReturnValue(),size_scale);

    }

    @Shadow
    private PokemonProperties props;

}