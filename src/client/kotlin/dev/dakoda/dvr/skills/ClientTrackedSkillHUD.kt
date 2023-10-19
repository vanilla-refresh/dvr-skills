package dev.dakoda.dvr.skills

import dev.dakoda.dvr.skills.DVRIdentifiers.ICONS_TEXTURE
import dev.dakoda.dvr.skills.DVRSkillsClient.game
import dev.dakoda.dvr.skills.Skills.EXP.Companion.perLevel
import dev.dakoda.dvr.skills.component.DVRSkillsComponents.Companion.COMP_SKILLS_EXP
import dev.dakoda.dvr.skills.component.DVRSkillsComponents.Companion.COMP_SKILLS_TRACKED
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import kotlin.math.roundToInt
import kotlin.math.roundToLong

object ClientTrackedSkillHUD {

    private var olds: MutableMap<Skill.Sub, Long>? = null

    private fun Skill.Sub.old() = olds!![this]

    val LISTENER: HudRenderCallback = HudRenderCallback { context, tickDelta ->
        if (game.player == null) return@HudRenderCallback
        if (olds == null) {
            val skills = COMP_SKILLS_EXP.get(game.player!!).skills
            olds = Skill.allSubSkills.associateWith {
                skills.progressOf(it).raw
            }.toMutableMap()
        }

        val skills = COMP_SKILLS_EXP.get(game.player!!).skills
        val tracked = COMP_SKILLS_TRACKED.get(game.player!!).trackedSkills.filter { it.value == 1 }
        val toTrack = skills.subSkills().filter { it.skill in tracked.keys }

        val trackerStartX = 12
        val trackerStartY = 12
        val trackerDivider = 16
        toTrack.forEachIndexed { index, exp ->
            if (exp.skill is Skill.Sub) {
                val y = trackerStartY + (index * trackerDivider)
                val barWidth = 62
                val expWidth = ((exp.raw.toFloat() / perLevel) * barWidth.toFloat()).roundToInt()
                val xx = trackerStartX + 10
                val yy = y - 2
                context.drawTexture(ICONS_TEXTURE, xx, yy, 1f, 29f, barWidth, 5, 200, 200)
                context.drawItemWithoutEntity(exp.skill.stack(), trackerStartX - 8, y - 8)

                if ((exp.skill in olds!!)) {
                    var old = (exp.skill as Skill.Sub).old() ?: 0L
                    if (exp.raw < old) {
                        old = 0L
                    }

                    if (old < exp.raw) {
                        if (old + tickDelta > exp.raw) {
                            old = exp.raw
                        } else {
                            old += tickDelta.roundToLong()
                        }

                        val oldExpWidth = ((old.toDouble() / perLevel) * barWidth.toDouble()).roundToInt()
                        context.drawTexture(ICONS_TEXTURE, xx, yy, 1f, 39f, expWidth, 5, 200, 200)
                        context.drawTexture(ICONS_TEXTURE, xx, yy, 1f, 34f, oldExpWidth, 5, 200, 200)
                    } else if (old == exp.raw) {
                        context.drawTexture(ICONS_TEXTURE, xx, yy, 1f, 34f, expWidth, 5, 200, 200)
                    }
                    olds!![exp.skill as Skill.Sub] = old
                }

                if (exp.level > 1) {
                    val text = exp.level.toString()
                    val textWidth = game.textRenderer.getWidth(text)
                    val levelTextX = (xx.toFloat() + (barWidth / 2) - (textWidth / 2)).roundToInt()
                    val levelTextY = (yy.toFloat() - 4f).roundToInt()

                    context.drawText(game.textRenderer, text, levelTextX - 1, levelTextY + 0, 0x000000, false)
                    context.drawText(game.textRenderer, text, levelTextX + 1, levelTextY + 0, 0x000000, false)
                    context.drawText(game.textRenderer, text, levelTextX + 0, levelTextY - 1, 0x000000, false)
                    context.drawText(game.textRenderer, text, levelTextX + 0, levelTextY + 1, 0x000000, false)
                    context.drawText(game.textRenderer, text, levelTextX + 0, levelTextY + 0, 0x4ae0f7, false)
                }
            }
        }
    }
}
