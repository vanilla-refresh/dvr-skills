package dev.dakoda.dvr.skills.mixin.farming;

import dev.dakoda.dvr.skills.DVRSkills;
import dev.dakoda.dvr.skills.Skill;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin {

    @Shadow private @Nullable UUID lovingPlayer;

    @Inject(method = "lovePlayer", at = @At("HEAD"))
    void mixin_lovePlayer(PlayerEntity player, CallbackInfo ci) {
        if (player != null && !player.isCreative() && !player.isSpectator()) {
            DVRSkills.INSTANCE.gainEXP(
                    player,
                    DVRSkills.CONFIG.getExp().getAnimalCare().getSources().getAction().getFeeding(),
                    Skill.Companion.getANIMAL_CARE()
            );
        }
    }

    @Inject(method = "breed", at = @At("HEAD"))
    void mixin_breed(ServerWorld world, AnimalEntity other, CallbackInfo ci) {
        if (lovingPlayer != null) {
            PlayerEntity playerOne = world.getPlayerByUuid(lovingPlayer);
            if (playerOne != null && !playerOne.isCreative() && !playerOne.isSpectator()) {
                DVRSkills.INSTANCE.gainEXP(
                        playerOne,
                        DVRSkills.CONFIG.getExp().getAnimalCare().getSources().getAction().getBreeding(),
                        Skill.Companion.getANIMAL_CARE()
                );
            }
        }
        if (other.getLovingPlayer() != null) {
            PlayerEntity playerTwo = world.getPlayerByUuid(lovingPlayer);
            if (playerTwo != null && !playerTwo.isCreative() && !playerTwo.isSpectator()) {
                DVRSkills.INSTANCE.gainEXP(
                        playerTwo,
                        DVRSkills.CONFIG.getExp().getAnimalCare().getSources().getAction().getBreeding(),
                        Skill.Companion.getANIMAL_CARE()
                );
            }
        }
    }
}