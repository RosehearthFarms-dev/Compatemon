package farm.rosehearth.compatemon.mixin.compat.apotheosis;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.boss.ApothBoss;
import dev.shadowsoffire.apotheosis.adventure.boss.BossEvents;
import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.shadowsoffire.apotheosis.adventure.client.BossSpawnMessage;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat;
import dev.shadowsoffire.placebo.network.PacketDistro;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import farm.rosehearth.compatemon.Compatemon;
import farm.rosehearth.compatemon.events.CompatemonEvents;
import farm.rosehearth.compatemon.events.apotheosis.ApothBossSpawnedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static farm.rosehearth.compatemon.utils.CompatemonDataKeys.MOD_ID_COMPATEMON;

@Mixin(BossEvents.class)
abstract class MixinBossEvents {
	
	@Shadow(remap = false)
	@Nullable
	private Component getName(Mob boss) {
		return boss.getSelfAndPassengers().filter(e -> ((PokemonEntity)e).getPersistentData().getCompound(MOD_ID_COMPATEMON).contains("apoth.boss")).findFirst().map(Entity::getCustomName).orElse(null);
	}
	@Shadow(remap = false)
	private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist){return true;}
	@Inject(at=@At("HEAD"),
	method="naturalBosses",
	remap=false)
	private void compatemon$makePokemonBossesWork(MobSpawnEvent.FinalizeSpawn e, CallbackInfo cir)
	{
		
		CompatemonEvents.APOTH_BOSS_SPAWNED.postThen(new ApothBossSpawnedEvent(e.getEntity()), savedEvent -> null, savedEvent -> {
			//Compatemon.LOGGER.debug("Wowie! A boss has spawned!");
			return null;
		});
		if (e.getEntity() instanceof PokemonEntity) {
			LivingEntity entity = e.getEntity();
			RandomSource rand = e.getLevel().getRandom();
			if ( !e.getLevel().isClientSide() && entity instanceof PokemonEntity ) {
				ServerLevelAccessor sLevel = (ServerLevelAccessor) e.getLevel();
				ResourceLocation dimId = sLevel.getLevel().dimension().location();
				Pair<Float, BossEvents.BossSpawnRules> rules = AdventureConfig.BOSS_SPAWN_RULES.get(dimId);
				if (rules == null) return;
				Compatemon.LOGGER.debug("Next point of error");
				if (rand.nextFloat() <= rules.getLeft() && rules.getRight().test(sLevel, BlockPos.containing(e.getX(), e.getY(), e.getZ()))) {
					Player player = sLevel.getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
					if (player == null) return; // Spawns require player context
					ApothBoss item = BossRegistry.INSTANCE.getRandomItem(rand, player.getLuck(), WeightedDynamicRegistry.IDimensional.matches(sLevel.getLevel()), GameStagesCompat.IStaged.matches(player));
					if (item == null) {
						AdventureModule.LOGGER.error("Attempted to spawn a boss in dimension {} using configured boss spawn rule {}/{} but no bosses were made available.", dimId, rules.getRight(), rules.getLeft());
						return;
					}
					Compatemon.LOGGER.debug("We've made the item now...");
					Mob boss = item.createBoss(sLevel, BlockPos.containing(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), rand, player.getLuck());
					if (AdventureConfig.bossAutoAggro) {
						boss.setTarget(player);
					}
					if (canSpawn(sLevel, boss, player.distanceToSqr(boss))) {
						sLevel.addFreshEntityWithPassengers(boss);
						e.setResult(Event.Result.DENY);
						AdventureModule.debugLog(boss.blockPosition(), "Surface Boss - " + boss.getName().getString());
						Component name = this.getName(boss);
						if (name == null || name.getStyle().getColor() == null) AdventureModule.LOGGER.warn("A Boss {} ({}) has spawned without a custom name!", boss.getName().getString(), EntityType.getKey(boss.getType()));
						else {
							sLevel.players().forEach(p -> {
								Vec3 tPos = new Vec3(boss.getX(), AdventureConfig.bossAnnounceIgnoreY ? p.getY() : boss.getY(), boss.getZ());
								if (p.distanceToSqr(tPos) <= AdventureConfig.bossAnnounceRange * AdventureConfig.bossAnnounceRange) {
									((ServerPlayer) p).connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("info.apotheosis.boss_spawn", name, (int) boss.getX(), (int) boss.getY())));
									TextColor color = name.getStyle().getColor();
									PacketDistro.sendTo(Apotheosis.CHANNEL, new BossSpawnMessage(boss.blockPosition(), color == null ? 0xFFFFFF : color.getValue()), player);
								}
							});
						}
						//this.bossCooldowns.put(entity.level().dimension().location(), AdventureConfig.bossSpawnCooldown);
					}
				}
			}
		}
	}
}
