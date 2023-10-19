package dev.dakoda.dvr.skills.gui

import dev.dakoda.dvr.skills.DVRSkills
import dev.dakoda.dvr.skills.DVRSkillsClient.game
import dev.dakoda.dvr.skills.Skill
import dev.dakoda.dvr.skills.Skills
import dev.dakoda.dvr.skills.component.DVRSkillsComponents.Companion.COMP_SKILLS_TRACKED
import dev.dakoda.dvr.skills.component.SkillsTrackedComponent
import dev.dakoda.dvr.skills.gui.DVRSkillsGUI.EXP_BAR_LONG_EMPTY
import dev.dakoda.dvr.skills.gui.DVRSkillsGUI.EXP_BAR_LONG_EMPTY_HOVERED
import dev.dakoda.dvr.skills.gui.DVRSkillsGUI.EXP_BAR_LONG_FULL
import dev.dakoda.dvr.skills.gui.SkillsScreen.Companion.windowDecor
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.NarrationSupplier
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import kotlin.math.roundToInt

private const val BAR_WIDTH = 144

class SkillCategoryContent(
    private val category: Skill.Category,
    private val translationPrefix: String
) {

    private val window get() = game.window

    private fun shouldShow(skill: Skill, discoveries: Map<Skill, Boolean>): Boolean {
        return discoveries[skill] ?: DVRSkills.CONFIG.isDiscoveredByDefault(skill)
    }

    fun titleX() = (window.leftOfInventory.toFloat() + 48f).roundToInt()
    fun titleY(index: Int) = (window.topOfInventory.toFloat() + 40f + (index * 18f)).roundToInt()

    fun getDrawables(skills: Skills, discoveries: Map<Skill, Boolean>) = getSubSkillTexts(
        skills.subSkills(category).filter {
            shouldShow(it.skill, discoveries)
        }
    ) + getHeader()

    fun getDrawableChildren(skills: Skills, discoveries: Map<Skill, Boolean>): List<ButtonWidget> {
        with(skills.subSkills(category).filter { shouldShow(it.skill, discoveries) }) {
            return getSubSkillPinToggle(this) + getSubSkillProgressBar(this)
        }
    }

    private fun getHeader(): Drawable {
        return Drawable { context, _, _, _ ->
            val headerText = Text.literal("§7- ")
                .append(Text.translatable("dvr.skills.${category.name.lowercase()}"))
                .append("§7 -")
            val headerWidth = game.textRenderer.getWidth(headerText)
            val headerX = ((window.scaledWidth / 2).toFloat() - (headerWidth / 2)).roundToInt()
            val headerY = (window.topOfInventory.toFloat() + 20f).roundToInt()
            windowDecor(context)
            context.drawText(game.textRenderer, headerText, headerX, headerY, 0x000000, false)
        }
    }

    private fun getSubSkillTexts(subSkills: List<Skills.EXP>): List<Drawable> {
        return subSkills.flatMapIndexed { index, exp ->
            listOf(
                // Icon
                Drawable { context, _, _, _ ->
                    context.drawItemWithoutEntity(exp.skill.stack(), titleX() - 20, titleY(index))
                },
                // Title
                Drawable { context, _, _, _ ->
                    context.drawText(
                        game.textRenderer,
                        Text.translatable("$translationPrefix.${exp.skill.name.lowercase()}"),
                        titleX(),
                        titleY(index),
                        0x000000,
                        false,
                    )
                }
            )
        }
    }

    private fun getSubSkillPinToggle(subSkills: List<Skills.EXP>): List<ButtonWidget> {
        return subSkills.mapIndexed { index, exp ->
            object : ButtonWidget(
                titleX() + BAR_WIDTH - 16,
                titleY(index) - 2,
                12,
                12,
                Text.empty(),
                {
//                    println("Toggling pin for ${exp.skill.name}")
                    (COMP_SKILLS_TRACKED.get(game.player!!) as SkillsTrackedComponent).toggle(exp.skill as Skill.Sub)
                    COMP_SKILLS_TRACKED.sync(game.player!!)
                },
                NarrationSupplier { Text.empty() }
            ) {
                override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
                    val isTracked = COMP_SKILLS_TRACKED.get(game.player!!).trackedSkills[exp.skill] == 1
                    val pinTex = Identifier("dvr-skills",
                        "menu/pin_${if (isTracked) "" else "un"}selected"
                    )
                    context.drawGuiTexture(pinTex, x, y, 10, 10)

                    super.render(context, mouseX, mouseY, delta)
                }

                override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
                    // Don't render as a button
                }
            }
        }
    }

    private fun getSubSkillProgressBar(subSkills: List<Skills.EXP>): List<ButtonWidget> {
        return subSkills.mapIndexed { index, exp ->
            val xx = titleX() - 1
            val yy = titleY(index) + 9
            object : ButtonWidget(
                xx,
                yy,
                BAR_WIDTH,
                7,
                Text.empty(),
                { },
                NarrationSupplier { Text.empty() }
            ) {

                override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
                    hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
                    val texVModified = if (hovered) 14 else 0
                    val expWidth = ((exp.raw.toFloat() / Skills.EXP.perLevel) * BAR_WIDTH.toFloat()).roundToInt()

                    if (isHovered) {
                        context.drawGuiTexture(EXP_BAR_LONG_EMPTY_HOVERED, xx, yy, 1, BAR_WIDTH, height)
                    } else {
                        context.drawGuiTexture(EXP_BAR_LONG_EMPTY, xx, yy, 1, BAR_WIDTH, height)
                    }
                    context.drawGuiTexture(EXP_BAR_LONG_FULL, 144, 7, 0, 0, xx, yy, 1, expWidth, height)

                    if (exp.level > 1) {
                        val text = exp.level.toString()
                        val levelTextX = (xx.toFloat() + (BAR_WIDTH / 2) - 2f).roundToInt()
                        val levelTextY = (yy.toFloat() - 4f).roundToInt()

                        context.drawText(game.textRenderer, text, levelTextX - 1, levelTextY + 0, 0x000000, true)
                        context.drawText(game.textRenderer, text, levelTextX + 1, levelTextY + 0, 0x000000, true)
                        context.drawText(game.textRenderer, text, levelTextX + 0, levelTextY - 1, 0x000000, true)
                        context.drawText(game.textRenderer, text, levelTextX + 0, levelTextY + 1, 0x000000, true)
                        context.drawText(game.textRenderer, text, levelTextX + 0, levelTextY + 0, 0x4ae0f7, true)
                    }

                    if (isHovered) {
                        context.drawTooltip(
                            game.textRenderer,
                            listOf(
                                Text.translatable("dvr.skills.progress", exp.raw, Skills.EXP.perLevel),
//                                Text.translatable("${translationPrefix}.${exp.skill.name.lowercase()}.desc"),
                            ) + Text.translatable("${translationPrefix}.${exp.skill.name.lowercase()}.desc").string.split("\n").map {
                                Text.literal("§7$it")
                            },
                            mouseX,
                            mouseY
                        )
                    }
                }
            }
        }
    }

    companion object {

        fun of(skill: Skill.Category) = SkillCategoryContent(skill, "dvr.skills.${skill.name.lowercase()}")
    }
}