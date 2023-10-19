package dev.dakoda.dvr.skills.exp

import dev.dakoda.dvr.skills.DVRSkills.CONFIG
import dev.dakoda.dvr.skills.Skill.Companion.HUNTER
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.EvokerEntity
import net.minecraft.entity.mob.IllagerEntity
import net.minecraft.registry.tag.EntityTypeTags

object EntityHuntChecker : AbstractEntityKillChecker(HUNTER, useBackupEXP = false) {
    init {
        val hunter = CONFIG.exp.hunter.sources.killing
        registry[EntityTypeTags.RAIDERS] = callback { _, _, entity ->
            with(entity as IllagerEntity) {
                if (isPatrolLeader) {
                    hunter.pillagerPatrolLeader
                } else if (this is EvokerEntity) {
                    hunter.evoker
                } else {
                    null
                }
            }
        }
        registry[EntityType.WITCH] = flat(hunter.witch)
        registry[EntityType.ENDERMAN] = flat(hunter.enderman)
        registry[EntityType.WITHER_SKELETON] = flat(hunter.witherSkeleton)
        registry[EntityType.ELDER_GUARDIAN] = flat(hunter.elderGuardian)
        registry[EntityType.WARDEN] = flat(hunter.warden)
        registry[EntityType.WITHER] = flat(hunter.wither)
        registry[EntityType.ENDER_DRAGON] = flat(hunter.enderDragon)
    }
}
