package dev.dakoda.dvr.skills.component

import dev.dakoda.dvr.skills.Skill
import dev.onyxstudios.cca.api.v3.component.Component

interface ISkillsDiscoveredComponent : Component {

    var skillsDiscovered: MutableMap<Skill, Boolean>
}
