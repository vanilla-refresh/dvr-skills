package dev.dakoda.dvr.skills.mixin.fishing;

import dev.dakoda.dvr.skills.DVRSkills;
import dev.dakoda.dvr.skills.exp.AbstractFishingChecker.FishingParams;
import dev.dakoda.dvr.skills.exp.FishingChecker;
import dev.dakoda.dvr.skills.exp.data.EXPGain;
import dev.dakoda.dvr.skills.exp.map.EXPMap.Entry.Settings.Order;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                    shift = At.Shift.AFTER,
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    void mixin_use(
            ItemStack usedItem, CallbackInfoReturnable<Integer> cir,
            PlayerEntity playerEntity, int i,
            LootContextParameterSet lootContextParameterSet, LootTable lootTable,
            List<ItemStack> list, Iterator<ItemStack> iterator,
            ItemStack itemStack
    ) {
        if (!playerEntity.isCreative() && !playerEntity.isSpectator()) {
            EXPGain gain = FishingChecker.INSTANCE.resolve(new FishingParams(itemStack.getItem()), Order.DONT_CARE);
            if (gain != null) DVRSkills.INSTANCE.gainEXP(playerEntity, gain);
        }
    }
}