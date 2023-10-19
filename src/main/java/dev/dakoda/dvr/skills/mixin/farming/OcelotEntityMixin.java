package dev.dakoda.dvr.skills.mixin.farming;

import dev.dakoda.dvr.skills.DVRSkills;
import dev.dakoda.dvr.skills.Skill;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OcelotEntity.class)
public abstract class OcelotEntityMixin {

    @Shadow abstract boolean isTrusting();

    @Inject(method = "interactMob", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/passive/OcelotEntity;setTrusting(Z)V",
            shift = At.Shift.AFTER,
            ordinal = 0
    ))
    void mixin_interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (player != null && !player.isCreative() && !player.isSpectator()) {
            if (!isTrusting()) {
                DVRSkills.INSTANCE.gainEXP(player, DVRSkills.CONFIG.getExp().getAnimalCare().getSources().getAction().getTaming(), Skill.Companion.getANIMAL_CARE());
            }
        }
    }
}