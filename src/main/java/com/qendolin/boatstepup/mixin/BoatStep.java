package com.qendolin.boatstepup.mixin;

import com.qendolin.boatstepup.Main;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BoatEntity.class)
public abstract class BoatStep extends Entity {
    {
        stepHeight = Main.CONFIG.groundStepHeight;
    }

    public BoatStep(EntityType<?> type, World world) {
        super(type, world);
    }

    // This is how much higher the fluid height can be than the top of the boat.
    // Since the top of the boat is almost at the fluid height this wouldn't allow for swimming up stream as even
    // the slightest dip would make the boat sink.
    @ModifyConstant(
            constant = @Constant(doubleValue = 0.001, ordinal = 0),
            method = "getUnderWaterLocation()Lnet/minecraft/entity/vehicle/BoatEntity$Location;")
    private double changeUnderWaterHeight(double padding) {
        return 0.001 + Main.CONFIG.waterStepHeight / 9d;
    }

    // Minecraft only checks the blocks at the bottom bounds of the boat but not at the top which can lead to an
    // incorrect waterLevel value;
    @ModifyVariable(
            at = @At(value = "STORE", ordinal = 0), index = 5,
            method = "checkBoatInWater()Z")
    private int changeInWaterCheckHeight(int maxY) {
        return MathHelper.ceil(this.getBoundingBox().maxY);
    }
}
