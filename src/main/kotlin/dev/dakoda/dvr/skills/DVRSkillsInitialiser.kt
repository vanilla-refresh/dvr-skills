package dev.dakoda.dvr.skills

import com.google.gson.GsonBuilder
import dev.dakoda.dvr.skills.DVRSkills.CONFIG
import dev.dakoda.dvr.skills.DVRSkills.LOGGER
import dev.dakoda.dvr.skills.config.DVRSkillsConfig
import dev.dakoda.dvr.skills.exp.AbstractBreakBlockChecker.BreakBlockParams
import dev.dakoda.dvr.skills.exp.AbstractEntityKillChecker.EntityKillParams
import dev.dakoda.dvr.skills.exp.AbstractUseBlockChecker.UseBlockParams
import dev.dakoda.dvr.skills.exp.BreakBlockChecker
import dev.dakoda.dvr.skills.exp.CookingChecker
import dev.dakoda.dvr.skills.exp.EntityHuntChecker
import dev.dakoda.dvr.skills.exp.EntityKillChecker
import dev.dakoda.dvr.skills.exp.FishingChecker
import dev.dakoda.dvr.skills.exp.UseBlockChecker
import dev.dakoda.dvr.skills.exp.map.EXPMap.Entry.Settings.Order.AFTER
import dev.dakoda.dvr.skills.exp.map.EXPMap.Entry.Settings.Order.BEFORE
import dev.dakoda.dvr.skills.exp.map.EXPMap.Entry.Settings.Order.DONT_CARE
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult.PASS
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.pathString

class DVRSkillsInitialiser : ModInitializer {

	override fun onInitialize() {
		LOGGER = LogManager.getLogger(DVRSkills.modID)

		try {
			val root = FabricLoader.getInstance().configDir.pathString

			Files.createDirectories(Paths.get("$root/dmo"))
			Files.createDirectories(Paths.get("$root/dmo/skills"))

			val configPath = "$root/dmo/skills/config.json"
			val configFile = File(configPath)

			val gson = GsonBuilder().setPrettyPrinting().create()
			if (!configFile.exists()) {
				val defaultConfig = DVRSkillsConfig.default
				val asJson = gson.toJson(defaultConfig)
				configFile.writeText(asJson)
			}

			val json = configFile.readText()
			CONFIG = gson.fromJson(json, DVRSkillsConfig::class.java)
			// Updates the file with any new config values
			configFile.writeText(gson.toJson(CONFIG))
		} catch (e: Exception) {
			LOGGER.error("Severe failure when reading/writing from mod config file, using default config for now. You should fix that.")
			LOGGER.error(e)
			CONFIG = DVRSkillsConfig.default
		}

		initialiseCheckers()

		PlayerBlockBreakEvents.AFTER.register { world, player, blockPos, blockState, blockEntity ->
			if (player.isSurvival) {
				BreakBlockChecker.resolve(
					BreakBlockParams(
						world,
						player.mainHandStack,
						blockPos,
						blockState,
						blockEntity
					),
					order = AFTER
				)?.let {
					DVRSkills.gainEXP(player, it)
				}
			}
		}

		PlayerBlockBreakEvents.BEFORE.register { world, player, blockPos, blockState, blockEntity ->
			if (player.isSurvival) {
				BreakBlockChecker.resolve(
					BreakBlockParams(
						world,
						player.mainHandStack,
						blockPos,
						blockState,
						blockEntity
					),
					order = BEFORE
				)?.let {
					DVRSkills.gainEXP(player, it)
				}
			}
			true
		}

		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register { world, entity, killedEntity ->
			if (entity is PlayerEntity && entity.isSurvival) {
				EntityKillChecker.resolve(EntityKillParams(world, entity, killedEntity), order = DONT_CARE)?.let {
					DVRSkills.gainEXP(entity, it)
				}
				EntityHuntChecker.resolve(EntityKillParams(world, entity, killedEntity), order = DONT_CARE)?.let {
					DVRSkills.gainEXP(entity, it)
				}
			}
		}

		UseBlockCallback.EVENT.register { player, world, _, entityHitResult ->
			if (player.isSurvival) {
				UseBlockChecker.resolve(
					UseBlockParams(
						player.mainHandStack,
						world,
						world.getBlockState(entityHitResult.blockPos),
						entityHitResult.blockPos
					),
					order = DONT_CARE
				)?.let {
					DVRSkills.gainEXP(player, it)
				}
			}
			return@register PASS
		}
	}

	private val PlayerEntity.isSurvival get() = !this.isCreative && !this.isSpectator

	private fun initialiseCheckers() {
		BreakBlockChecker; EntityHuntChecker; EntityKillChecker; FishingChecker

		if (FabricLoader.getInstance().isModLoaded("dmo-cooking")) {
			CONFIG.exp.cooking.overridden = true
			println("Cooking EXP has been overridden by dmo-cooking; disabling crafting hooks")
		} else {
			CookingChecker
		}
	}
}