package dev.dakoda.dvr.skills.gui

import com.mojang.blaze3d.systems.RenderSystem
import dev.dakoda.dvr.skills.DVRIdentifiers
import dev.dakoda.dvr.skills.DVRSkillsClient.game
import dev.dakoda.dvr.skills.Skill
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.BOOK
import net.minecraft.item.Items.CHEST
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class SkillCategoryWidget(
    pressAction: PressAction = PressAction { },
    x: Int = 0,
    y: Int = 0,
    width: Int = 20,
    height: Int = 20,
    var texU: Int = 0,
    var texV: Int = 0,
    var hoveredVOffset: Int = height,
    var tex: Identifier = DVRIdentifiers.WIDGETS_TEXTURE,
    texW: Int = width,
    texH: Int = height * 2
) : TexturedButtonWidget(x, y, width, height, ButtonTextures(tex, tex), pressAction) {

    private val window get() = game.window

    var selected: Boolean = false

    private val texVModified get() = if (this.isHovered or this.selected) texV + hoveredVOffset else texV

    lateinit var renderOverride: (context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) -> Unit

    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (this::renderOverride.isInitialized) {
            renderOverride(context, mouseX, mouseY, delta)
        } else {
            super.renderButton(context, mouseX, mouseY, delta)
        }
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {

        private const val U_BLANK_BUTTON = 0

        const val BUTTON_SIZE = 20
        const val BUTTON_SEPARATE = BUTTON_SIZE + 2

        fun menu(
            xOffset: Int = 0,
            yOffset: Int = 0,
            pressAction: PressAction
        ) = SkillCategoryWidget(pressAction).apply {
            renderOverride = { context, mouseX, mouseY, _ ->
                context?.drawTexture(tex, x, y, texU.float, texVModified.float, width, height, 100, 100)

                context?.drawItemWithoutEntity(BOOK.defaultStack, x + 2, y + 2)
                if (isHovered) {
                    context?.drawTooltip(game.textRenderer, Text.translatable("dvr.skills"), mouseX, mouseY)
                }
            }
            x = window.leftOfInventory + xOffset; y = window.topOfInventory + yOffset
            texU = 0
        }

        fun menuInventory(
            screen: Screen?,
            xOffset: Int = 0,
            yOffset: Int = 0,
            pressAction: PressAction
        ) = SkillCategoryWidget(pressAction).apply {
            renderOverride = { context, mouseX, mouseY, _ ->
                RenderSystem.setShaderTexture(0, tex)
                context?.drawTexture(tex, x, y, texU.float, texVModified.float, width, height, 100, 100)

                context?.drawItemWithoutEntity(CHEST.defaultStack, x + 2, y + 1)
                if (isHovered) {
                    context?.drawTooltip(game.textRenderer, Text.translatable("dvr.skills.inventory.back"), mouseX, mouseY)
                }
            }
            x = window.leftOfInventory + xOffset; y = window.topOfInventory + yOffset
            texU = U_BLANK_BUTTON
        }

        private fun categoryButton(
            screen: Screen?,
            itemIcon: ItemStack,
            toolTipKey: String,
            pressAction: PressAction
        ) = SkillCategoryWidget(pressAction).apply {
            renderOverride = { context, mouseX, mouseY, _ ->
                // Draw button background
                RenderSystem.setShaderTexture(0, tex)
                context?.drawTexture(tex, x, y, texU.float, texVModified.float, width, height, 100, 100)

                // Draw button icon
                context?.drawItemWithoutEntity(itemIcon, x + 2, y + 2)

                if (isHovered) {
                    context?.drawTooltip(game.textRenderer, Text.translatable(toolTipKey), mouseX, mouseY)
                }
            }
        }

        fun makeCategoryButton(
            screen: Screen?,
            skill: Skill.Category,
            xOffset: Int = 0,
            yOffset: Int = 0,
            pressAction: PressAction
        ) = categoryButton(
            screen,
            skill.stack(),
            "dvr.skills.${skill.name.lowercase()}",
            pressAction
        ).apply {
            x = window.rightOfInventory + xOffset; y = window.topOfInventory + yOffset
            texU = U_BLANK_BUTTON
        }

        private val Int.float get() = this.toFloat()
    }
}