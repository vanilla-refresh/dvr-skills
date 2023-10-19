package dev.dakoda.dvr.skills.mixin.dungeoneer;

import dev.dakoda.dvr.skills.DVRSkills;
import dev.dakoda.dvr.skills.Skill;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin {

    @Shadow @Nullable protected Identifier lootTableId;

    /*
        Injects code to gain Dungeoneer EXP when a chest is opened for the first time.
     */
    @Inject(method = "checkLootInteraction", at = @At("HEAD"))
    void mixin_checkLootInteraction(PlayerEntity player, CallbackInfo ci) {
        if (lootTableId != null && player != null) {
            DVRSkills.INSTANCE.gainEXP(
                    player,
                    DVRSkills.INSTANCE.getCONFIG().getExp().getDungeoneer().getSources().getUse().getChest(),
                    Skill.Companion.getDUNGEONEER()
            );
        }
    }
}