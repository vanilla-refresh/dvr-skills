package dev.dakoda.dvr.skills

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier

object DVRSkillsClient {

    val game: MinecraftClient by lazy { MinecraftClient.getInstance() }

    internal fun resource(location: String) = Identifier(DVRSkills.modID, location)
}
