package dev.dakoda.dvr.skills.component

import dev.dakoda.dvr.skills.DVRSkills
import dev.dakoda.dvr.skills.Skill
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.nbt.NbtCompound

class SkillsDiscoveredComponent : ISkillsDiscoveredComponent, AutoSyncedComponent {

    override var skillsDiscovered: MutableMap<Skill, Boolean> = mutableMapOf()

    override fun readFromNbt(tag: NbtCompound) {
        val newMap = mutableMapOf<Skill, Boolean>()
        Skill.all.forEach {
            newMap[it] = tag.getSkill(it).getBoolean("discovered")
        }
        skillsDiscovered.clear()
        skillsDiscovered.putAll(newMap)
    }

    private fun NbtCompound.getSkill(skill: Skill) = this.getCompound(skill.name)

    override fun writeToNbt(tag: NbtCompound) {
        Skill.all.forEach {
            tag.put(
                it.name,
                NbtCompound().apply {
                    putBoolean("discovered", skillsDiscovered[it] ?: DVRSkills.CONFIG.isDiscoveredByDefault(it))
                }
            )
        }
    }
}