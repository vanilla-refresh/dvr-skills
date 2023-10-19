package dev.dakoda.dvr.skills.mixin.farming;

import dev.dakoda.dvr.skills.DVRSkills;
import dev.dakoda.dvr.skills.Skill;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TameableEntity.class)
public abstract class TameableEntityMixin {

    @Inject(method = "setOwner", at = @At("HEAD"))
    void mixin_setOwner(PlayerEntity player, CallbackInfo ci) {
        if (player != null && !player.isCreative() && !player.isSpectator()) {
            DVRSkills.INSTANCE.gainEXP(player, DVRSkills.CONFIG.getExp().getAnimalCare().getSources().getAction().getTaming(), Skill.Companion.getANIMAL_CARE());
        }
    }
}