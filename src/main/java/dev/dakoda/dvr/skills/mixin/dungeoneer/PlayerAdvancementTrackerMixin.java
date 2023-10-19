package dev.dakoda.dvr.skills.mixin.dungeoneer;

import dev.dakoda.dvr.skills.DVRSkills;
import dev.dakoda.dvr.skills.Skill;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {

    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancement/Advancement;rewards()Lnet/minecraft/advancement/AdvancementRewards;",
            shift = At.Shift.BEFORE,
            ordinal = 0
    ))
    void mixin_grantCriterion(AdvancementEntry advancementEntry, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (owner != null) {
            List<String> validAdvancements = new ArrayList<>();
            validAdvancements.add("minecraft:story/enter_the_nether");
            validAdvancements.add("minecraft:story/enter_the_end");
            validAdvancements.add("minecraft:nether/find_fortress");
            validAdvancements.add("minecraft:end/find_end_city");

            if (validAdvancements.contains(advancementEntry.id().toString())) {
                DVRSkills.INSTANCE.gainEXP(owner, 120, Skill.Companion.getDUNGEONEER());
            }
        }
    }
}