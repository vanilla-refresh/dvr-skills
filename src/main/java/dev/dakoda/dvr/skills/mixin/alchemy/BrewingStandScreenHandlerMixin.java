package dev.dakoda.dvr.skills.mixin.alchemy;


import dev.dakoda.dvr.skills.DVRSkills;
import dev.dakoda.dvr.skills.exp.AbstractPotionBrewingChecker.BrewingParams;
import dev.dakoda.dvr.skills.exp.PotionBrewingChecker;
import dev.dakoda.dvr.skills.exp.data.EXPGain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import static dev.dakoda.dvr.skills.exp.map.EXPMap.Entry.Settings.Order.DONT_CARE;

@Mixin(targets = {"net.minecraft.screen.BrewingStandScreenHandler$PotionSlot"})
public class BrewingStandScreenHandlerMixin {

    @Inject(method = "onTakeItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancement/criterion/BrewedPotionCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/potion/Potion;)V",
            shift = At.Shift.BEFORE,
            ordinal = 0
    ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    void mixin_onTakeItem(PlayerEntity player, ItemStack stack, CallbackInfo ci, Potion potion) {
        if (player != null && stack != null) {
            EXPGain gain = PotionBrewingChecker.INSTANCE.resolve(new BrewingParams(potion), DONT_CARE);
            if (gain != null) DVRSkills.INSTANCE.gainEXP(player, gain);
        }
    }
}