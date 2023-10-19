@file:Suppress("MemberVisibilityCanBePrivate")

package dev.dakoda.dvr.skills

import dev.dakoda.dvr.skills.component.DVRSkillsComponents.Companion.COMP_SKILLS_DISCOVERED
import dev.dakoda.dvr.skills.component.DVRSkillsComponents.Companion.COMP_SKILLS_EXP
import dev.dakoda.dvr.skills.config.DVRSkillsConfig
import dev.dakoda.dvr.skills.event.PlayerGainEXPEvent
import dev.dakoda.dvr.skills.exp.data.EXPGain
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments.SILK_TOUCH
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Logger

object DVRSkills {
    const val modID = "dvr-skills"

    lateinit var CONFIG: DVRSkillsConfig
    internal lateinit var LOGGER: Logger

    internal fun resource(location: String) = Identifier(modID, location)

    internal val ItemStack.hasSilkTouch get() = SILK_TOUCH in EnchantmentHelper.get(this).keys
    internal val ItemStack.hasAnyEnchantment get() = EnchantmentHelper.get(this).isNotEmpty()

    fun gainEXP(player: PlayerEntity, gainAmount: Int, gainSkill: Skill.Sub) = gainEXP(player, gainAmount to gainSkill)

    fun gainEXP(player: PlayerEntity, gain: EXPGain) {
        val discoveredSkills = COMP_SKILLS_DISCOVERED[player].skillsDiscovered
        var discoveredNewSkill = false
        if (discoveredSkills[gain.skill] != true) {
            discoveredSkills[gain.skill] = true
            discoveredNewSkill = true
        }

        val playerSkills = COMP_SKILLS_EXP[player].skills
        playerSkills.increase(gain)
        PlayerGainEXPEvent.EVENT.invoker().handle(player, gain, discoveredNewSkill)
        COMP_SKILLS_EXP.sync(player)
        COMP_SKILLS_DISCOVERED.sync(player)
    }

    fun gainEXP(player: PlayerEntity, gain: Pair<Int, Skill.Sub>) = gainEXP(player, EXPGain(gain))
}