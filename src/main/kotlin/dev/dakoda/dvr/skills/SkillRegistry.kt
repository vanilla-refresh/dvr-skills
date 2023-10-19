package dev.dakoda.dvr.skills

object SkillRegistry {

    val registered: MutableList<Skill> = mutableListOf()

    fun Skill.Sub.register(): Skill.Sub {
        registered.add(this)
        return this
    }

    fun Skill.Category.register(): Skill.Category {
        this.subSkills.forEach {
            it.parent = this
        }
        registered.add(this)
        return this
    }
}