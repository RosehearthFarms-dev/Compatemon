package farm.rosehearth.compatemon.utils;

import farm.rosehearth.compatemon.Compatemon;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static farm.rosehearth.compatemon.utils.CompatemonDataKeys.MOD_ID_APOTHEOSIS;

/**
 *
 */
public class CompateUtils {
	public static Random Rand = new Random();
	
	/**
	 * Used to generate a PokemonProperties string that can be passed to PokemonProperties.Companion.parse to create a pokemon
	 * @param world server level
	 * @param pos Position where the pokemon will attempt to generate
	 * @param minLevel Minimum level for the pokemon to generate
	 * @param levelRange Range of levels for the pokemon to generate in
	 * @return String for PokemonProperties to parse
	 */
	public static String GeneratePokemonProperties(ServerLevelAccessor world, BlockPos pos, int minLevel, int levelRange){
		String pokemon = "species=";
		String uncatch = "uncatchable";
		String lev = "l=" + (Rand.nextInt(levelRange) + minLevel);
		if(Compatemon.ShouldLoadMod(MOD_ID_APOTHEOSIS)){
		
		}
		var b = world.getBiome(pos);
		
		return "";
	}
}

