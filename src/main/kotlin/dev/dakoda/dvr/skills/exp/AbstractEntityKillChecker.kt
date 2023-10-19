package dev.dakoda.dvr.skills.exp

import dev.dakoda.dvr.skills.Skill
import dev.dakoda.dvr.skills.Skill.Companion.HUNTER
import dev.dakoda.dvr.skills.Skill.Companion.MELEE
import dev.dakoda.dvr.skills.Skill.Companion.RANGER
import dev.dakoda.dvr.skills.exp.AbstractChecker.ChecksEntities
import dev.dakoda.dvr.skills.exp.AbstractEntityKillChecker.EntityKillParams
import dev.dakoda.dvr.skills.exp.AbstractEntityKillChecker.EntityKillRules
import dev.dakoda.dvr.skills.exp.data.EXPGain
import dev.dakoda.dvr.skills.exp.data.EXPGain.Provider
import dev.dakoda.dvr.skills.exp.data.EXPGain.Provider.Callback
import dev.dakoda.dvr.skills.exp.data.EXPGain.Provider.Default
import dev.dakoda.dvr.skills.exp.data.EXPGain.Provider.Params
import dev.dakoda.dvr.skills.exp.data.EXPGain.Rules
import dev.dakoda.dvr.skills.exp.map.EXPMap
import dev.dakoda.dvr.skills.exp.map.EXPMap.Entry.Settings
import dev.dakoda.dvr.skills.exp.map.EXPMap.Entry.Settings.Order
import dev.dakoda.dvr.skills.exp.map.EXPMap.Entry.Settings.Order.DONT_CARE
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags.BOWS
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags.SPEARS
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.Monster
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.world.ServerWorld

abstract class AbstractEntityKillChecker(
    private val checkerSkill: Skill.Sub,
    private val useBackupEXP: Boolean = false
) : ChecksEntities<EntityKillParams, EntityKillRules>() {

    class EntityKillParams(
        val serverWorld: ServerWorld,
        val player: PlayerEntity,
        val killedEntity: Entity
    ) : Params()

    final override val registry = EXPMap.entities<EntityKillRules>()

    override fun haveEntryFor(key: EntityType<*>, order: Order): Boolean {
        return registry.contains(key, order)
    }

    override fun getEntry(key: EntityType<*>, order: Order): EXPMap.Entry<EntityKillRules>? {
        return registry.get(key, order)
    }

    override fun resolve(params: EntityKillParams, order: Order): EXPGain? {
        val weaponUsed = params.player.mainHandStack
        val targetSkill = if (checkerSkill == HUNTER) HUNTER else {
            if (weaponUsed.isIn(BOWS) or weaponUsed.isIn(SPEARS)) RANGER else MELEE
        }

        if (!haveEntryFor(params.killedEntity.type, order)) {
            return run {
                if (useBackupEXP && params.killedEntity is Monster) EXPGain(3, targetSkill) else null
            }
        }

        val entry = getEntry(params.killedEntity.type, order)!!
        return if (isValidGain(params, entry.rules)) {
            val provider = entry.expGainProvider
            if (provider is EntityKillProvider) provider.supply(params)

            val gain = provider.resolveEXP()
            return gain?.apply {
                skill = targetSkill
            }
        } else {
            null
        }
    }

    private fun isValidGain(params: EntityKillParams, rules: EntityKillRules): Boolean {
        return true
    }

    protected class EntityKillProvider(
        private val callback: (ServerWorld, PlayerEntity, Entity) -> EXPGain?
    ) : Provider(), Callback<EntityKillParams> {

        private lateinit var params: EntityKillParams

        override fun supply(params: EntityKillParams) {
            this.params = params
        }

        override fun resolveEXP(): EXPGain? {
            with(params) {
                return callback(serverWorld, player, killedEntity)
            }
        }
    }

    class EntityKillRules(
        val handTags: List<TagKey<Item>> = listOf()
    ) : Rules()

    protected fun rules(
        handTags: List<TagKey<Item>> = listOf()
    ) = EntityKillRules(handTags)

    protected fun flat(
        gain: Int,
        rules: EntityKillRules = EntityKillRules(),
        settings: KillCheckerSettings = KillCheckerSettings()
    ) = EXPMap.Entry(Default(EXPGain(gain, checkerSkill)), rules, settings)

    protected fun callback(
        rules: EntityKillRules = EntityKillRules(),
        settings: KillCheckerSettings = KillCheckerSettings(),
        callback: (ServerWorld, PlayerEntity, Entity) -> Int?
    ) = EXPMap.Entry(EntityKillProvider { w, e, k -> callback(w, e, k)?.let { (it to checkerSkill).expGain } }, rules, settings)

    protected class KillCheckerSettings : Settings(order = DONT_CARE)

    protected fun settings() = KillCheckerSettings()
}