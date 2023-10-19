package dev.dakoda.dvr.skills.component

import dev.dakoda.dvr.skills.Skill
import dev.onyxstudios.cca.api.v3.component.Component

interface ISkillsTrackedComponent : Component {

    var trackedSkills: MutableMap<Skill, Int>
}