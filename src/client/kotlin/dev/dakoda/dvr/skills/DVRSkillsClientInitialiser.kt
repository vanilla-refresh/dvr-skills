package dev.dakoda.dvr.skills

import dev.dakoda.dvr.skills.event.PlayerGainEXPEvent
import dev.dakoda.dvr.skills.exp.data.EXPGain
import dev.dakoda.dvr.skills.gui.SkillCategoryWidget
import dev.dakoda.dvr.skills.gui.SkillsScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResult.PASS
import org.lwjgl.glfw.GLFW

class DVRSkillsClientInitialiser : ClientModInitializer {

    companion object {
        val KEYBINDING_SKILLS_MENU: KeyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "dvr.skills.keybindings.open_skills_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "dvr.skills.keybindings.category"
            )
        )
    }

    override fun onInitializeClient() {
        registerKeybindings()
        addSkillsButtonToInventory()
        HudRenderCallback.EVENT.register(ClientTrackedSkillHUD.LISTENER)
        PlayerGainEXPEvent.EVENT.register(object : PlayerGainEXPEvent {
            override fun handle(playerEntity: PlayerEntity, gain: EXPGain, discovered: Boolean): ActionResult {
//                if (discovered) {
//                    game.toastManager.add(DiscoveryToast(gain.second))
//                }
                return PASS
            }
        })
    }

    private fun registerKeybindings() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (KEYBINDING_SKILLS_MENU.wasPressed()) {
                client.setScreen(SkillsScreen())
            }
        }
    }

    private fun addSkillsButtonToInventory() {
        ScreenEvents.AFTER_INIT.register { client, screen, _, _ ->
            if (screen is InventoryScreen) {
                screen.buttons.add(
                    SkillCategoryWidget.menu {
                        client?.setScreen(SkillsScreen())
                    }
                )
            }
        }
    }

    private val Screen.buttons: MutableList<ClickableWidget>
        get() = Screens.getButtons(this)
}
