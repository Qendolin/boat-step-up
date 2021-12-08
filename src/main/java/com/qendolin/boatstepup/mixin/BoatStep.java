package com.qendolin.boatstepup.mixin;

import com.qendolin.boatstepup.Main;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatStep extends Entity {
    public BoatStep(EntityType<?> type, World world) {
        super(type, world);
    }

//    Since the config is not dynamically changeable this shouldn't be needed
//    @Inject(at = @At("HEAD"), method = "tick()V")
//    private void setStepHeight(CallbackInfo ci) {
//        if(world.isClient() && Main.RUNTIME_CONFIG.serverEnabled)
//            this.stepHeight = Main.RUNTIME_CONFIG.groundStepHeight;
//    }

    @Inject(at = @At("TAIL"), method = "<init>*")
    private void setStepHeightCtor(CallbackInfo ci) {
        if(!world.isClient() || Main.RUNTIME_CONFIG.serverEnabled)
            this.stepHeight = Main.RUNTIME_CONFIG.groundStepHeight;
    }

    /**
     * A generous approximation of the <a href="https://en.wikipedia.org/wiki/Freeboard_(nautical)">freeboard</a>.<br/>
     * It is calculated as the boat height * someFactor.
     * Here 2/9 is used as 'padding' but the correct value would be 0.35 (ca. 3/9).
     */
    private double approximateFreeboard() {
        return this.getHeight() * (2d/9d);
    }

    /**
     * This constant changes how much higher the fluid height can be than the top of the boat.
     * As the <a href="https://en.wikipedia.org/wiki/Freeboard_(nautical)">freeboard</a> (1.6875/9) is insufficient
     * to clear the gap between a still (level 8/9) and a shallow (level 1/9) flowing water source that is one block higher (2/9)
     * it is not possible to swim upstream in vanilla minecraft.<br/>
     * The configured waterStepHeight does not consider the already-present freeboard so it's subtracted first.
     * A waterStepHeight of 2 would have an extra 0.3125/9 as padding (actual freeboard would be 2.3125/9).
     * The magic number 9 is from {@link net.minecraft.fluid.FlowableFluid#getHeight(net.minecraft.fluid.FluidState) FlowableFluid.getHeight()}
     */
    @ModifyConstant(
            constant = @Constant(doubleValue = 0.001, ordinal = 0),
            method = "getUnderWaterLocation()Lnet/minecraft/entity/vehicle/BoatEntity$Location;")
    private double changeUnderWaterHeight(double padding) {
        if(!Main.RUNTIME_CONFIG.serverEnabled) return padding;
        return (Main.RUNTIME_CONFIG.waterStepHeight / 9d) - approximateFreeboard();
    }

    /**
     * Minecraft only checks the blocks at the bottom bounds of the boat but not at the top which can lead to an
     * incorrect {@link BoatEntity#waterLevel} value.
      */
    @ModifyVariable(
            at = @At(value = "STORE", ordinal = 0), index = 5,
            method = "checkBoatInWater()Z")
    private int changeInWaterCheckHeight(int maxY) {
        if(!Main.RUNTIME_CONFIG.serverEnabled) return maxY;
        return MathHelper.ceil(this.getBoundingBox().maxY);
    }
}
