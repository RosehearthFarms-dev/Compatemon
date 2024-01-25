package farm.rosehearth.compatemon.mixin;

import com.cobblemon.mod.common.api.spawning.context.SpawningContext;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import com.cobblemon.mod.common.util.*;
import farm.rosehearth.compatemon.Compatemon;

import virtuoel.pehkui.util.ScaleUtils;

@Mixin(value = PokemonSpawnAction.class, remap = false)
abstract class MixinPokemonSpawnAction extends SpawnAction<PokemonEntity> {


    public MixinPokemonSpawnAction(@NotNull SpawningContext ctx, @NotNull SpawnDetail detail) {

        super(ctx, detail);
    }

    @Inject(method = "createEntity()Lcom/cobblemon/mod/common/entity/pokemon/PokemonEntity;", at = @At("RETURN"))
    private void adjustEntitySizing(CallbackInfoReturnable<PokemonEntity> cir) {
       // CustomPokemonProperty.Companion.getProperties();
        float size_scale = Compatemon.getSizeModifier();
        float weight_scale = Compatemon.getWeightModifier();
        //cir.getReturnValue().getPokemon().saveToNBT();
        Compatemon.LOGGER.debug("Adjusting size of " + cir.getReturnValue().getPokemon().getDisplayName() + " to " + size_scale);
        ScaleUtils.setScaleOnSpawn(cir.getReturnValue(),size_scale);

    }

}