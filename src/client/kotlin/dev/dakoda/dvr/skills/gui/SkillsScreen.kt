package dev.dakoda.dvr.skills.gui

import dev.dakoda.dvr.skills.DVRIdentifiers.ICONS_TEXTURE
import dev.dakoda.dvr.skills.DVRSkills
import dev.dakoda.dvr.skills.DVRSkillsClient.game
import dev.dakoda.dvr.skills.DVRSkillsClientInitialiser.Companion.KEYBINDING_SKILLS_MENU
import dev.dakoda.dvr.skills.Skill
import dev.dakoda.dvr.skills.Skill.Companion.NULL_CAT
import dev.dakoda.dvr.skills.component.DVRSkillsComponents.Companion.COMP_SKILLS_DISCOVERED
import dev.dakoda.dvr.skills.component.DVRSkillsComponents.Companion.COMP_SKILLS_EXP
import dev.dakoda.dvr.skills.gui.DVRSkillsGUI.WINDOW
import dev.dakoda.dvr.skills.gui.SkillCategoryWidget.Companion.BUTTON_SEPARATE
import dev.dakoda.dvr.skills.gui.SkillCategoryWidget.Companion.makeCategoryButton
import dev.dakoda.dvr.skills.gui.SkillCategoryWidget.Companion.menuInventory
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.text.Text

class SkillsScreen : Screen(Text.literal("Skills")) {

    private val window get() = game.window

    val skills = COMP_SKILLS_EXP.get(game.player!!).skills
    private val discoveries = COMP_SKILLS_DISCOVERED.get(game.player!!).skillsDiscovered

    override fun shouldPause() = false

    private val categories = Skill.allCategories.filter { it != NULL_CAT }

    private val activeCategory: Skill.Category
        get() = TrackLastCategory.last

    private val activeCategoryContent: SkillCategoryContent
        get() = SkillCategoryContent.of(activeCategory)

    object TrackLastCategory {
        var set: Boolean = false
        lateinit var last: Skill.Category
    }

    private fun swapCategory(skill: Skill.Category) {
        if (skill in categories) TrackLastCategory.last = skill
        init()
    }

    override fun init() {
        clearChildren()
        super.init()

        categories.filter { it.subSkills.any { discoveries[it] ?: DVRSkills.CONFIG.isDiscoveredByDefault(it) } }.forEachIndexed { index, skillCategory ->
            if (!TrackLastCategory.set) {
                TrackLastCategory.set = true
                TrackLastCategory.last = skillCategory
            }
            addDrawableChild(
                makeCategoryButton(this, skillCategory, 0, BUTTON_SEPARATE * index) {
                    swapCategory(skillCategory)
                }.apply {
                    if (activeCategory == skillCategory) selected = true
                }
            )
        }
        addDrawable(
            Drawable { context, _, _, _ ->
                context.drawGuiTexture(
                    WINDOW,
                    window.leftOfInventory + 22,
                    window.topOfInventory - 3,
                    176,
                    166
                )
            }
        )
        if (!TrackLastCategory.set) {
            addDrawable(blankPage())
        } else {
            val content = activeCategoryContent
            content.getDrawables(skills, discoveries).forEach { addDrawable(it) }
            content.getDrawableChildren(skills, discoveries).forEach { addDrawableChild(it) }
        }
        addDrawableChild(
            menuInventory(this) {
                client?.setScreen(InventoryScreen(client?.player))
            }
        )
    }

    fun blankPage(): Drawable {
        return Drawable { context, _, _, _ ->
            windowDecor(context)
            val orderedText = Text.translatable("dvr.skills.no.discovered").asOrderedText()
            context.drawText(
                game.textRenderer,
                orderedText,
                (game.window.scaledWidth / 2) - (game.textRenderer.getWidth(orderedText) / 2),
                (game.window.scaledHeight / 2) - (game.textRenderer.fontHeight / 2),
                0x666666,
                false
            )
        }
    }

    companion object {
        fun windowDecor(context: DrawContext) {
            val decorX: Int = (game.window.scaledWidth / 2) - 73
            val decorY: Int = game.window.topOfInventory + 4
            context.setShaderColor(0.5f, 0.5f, 0.5f, 1f)
            context.drawTexture(ICONS_TEXTURE, decorX, decorY, 0f, 69f, 146, 8, 200, 200)
            context.drawTexture(ICONS_TEXTURE, decorX, decorY + 144, 0f, 69f, 146, 8, 200, 200)
            context.setShaderColor(1f, 1f, 1f, 1f)
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.render(context, mouseX, mouseY, partialTicks)
//        SkillCategoryContent.of(activeCategory).render(this, skills, matrices, mouseX, mouseY, partialTicks)
    }

    override fun keyPressed(ch: Int, keyCode: Int, modifiers: Int): Boolean {
        val wasInventoryKey = client?.options?.inventoryKey?.matchesKey(ch, 0) == true
        val wasSkillsKey = KEYBINDING_SKILLS_MENU.matchesKey(ch, 0)
        val wasEscapeKey = ch == 256
        if (wasInventoryKey or wasSkillsKey or wasEscapeKey) this.close()
        // Don't forward the inventory button because it will cause the inventory
        // to reappear again.
        return if (!wasInventoryKey && !wasSkillsKey) super.keyPressed(ch, keyCode, modifiers) else true
    }
}
