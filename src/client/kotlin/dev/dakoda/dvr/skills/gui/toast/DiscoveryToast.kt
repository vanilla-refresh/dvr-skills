package dev.dakoda.dvr.skills.gui.toast

import com.mojang.blaze3d.systems.RenderSystem
import dev.dakoda.dvr.skills.DVRSkills.CONFIG
import dev.dakoda.dvr.skills.DVRSkillsClient.game
import dev.dakoda.dvr.skills.Skill
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.toast.Toast
import net.minecraft.client.toast.Toast.Visibility.HIDE
import net.minecraft.client.toast.Toast.Visibility.SHOW
import net.minecraft.client.toast.ToastManager
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Environment(value = EnvType.CLIENT)
class DiscoveryToast(
    private val skill: Skill
) : Toast {

    private val duration = CONFIG.discovery.toastDurationSeconds * 1000L

    private val title = Text.literal("Skill discovered!")
    private val description = Text.literal(skill.name.lowercase().replaceFirstChar { it.titlecaseChar() })

    private var justUpdated: Boolean = true
    private var _startTime: Long = 0

    override fun draw(drawContext: DrawContext, manager: ToastManager, startTime: Long): Toast.Visibility {
        if (justUpdated) {
            _startTime = startTime
            justUpdated = false
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

        drawContext.drawTexture(Identifier("toast/advancement"), 0, 0, 0, 96, this.width, this.height)
        drawContext.drawItemWithoutEntity(skill.icon.defaultStack, 6, 6)

        drawContext.drawText(game.textRenderer, title, 30, 7, -11534256, false)
        drawContext.drawText(game.textRenderer, description, 30, 18, -16777216, false)

        return if (startTime - _startTime < duration) SHOW else HIDE
    }
}