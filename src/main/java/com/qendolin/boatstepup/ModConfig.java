package com.qendolin.boatstepup;


import com.qendolin.boatstepup.config.Config;
import com.qendolin.boatstepup.config.GsonConfig;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

@GsonConfig(value = Main.MODID, version = 2)
public class ModConfig implements Config {
    public float groundStepHeight = 0.25f;
    public float waterStepHeight = 3;
    public transient boolean serverEnabled = false;

    public PacketByteBuf writePacket() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeFloat(groundStepHeight);
        buf.writeFloat(waterStepHeight);
        return buf;
    }

    public void readPacket(PacketByteBuf buf) {
        this.groundStepHeight = buf.readFloat();
        this.waterStepHeight = buf.readFloat();
    }

    protected ModConfig copyFrom(ModConfig newValue) {
        groundStepHeight = newValue.groundStepHeight;
        waterStepHeight = newValue.waterStepHeight;
        return this;
    }

    @Override
    public String toString() {
        return "ModConfig{" +
                "groundStepHeight=" + groundStepHeight +
                ", waterStepHeight=" + waterStepHeight +
                '}';
    }
}
