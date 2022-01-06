package com.qendolin.boatstepup;


import com.qendolin.boatstepup.config.Config;
import com.qendolin.boatstepup.config.Entry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModConfig implements Config {
    @Entry.FloatRange(max = 1, step = 1f/16f, stringer = "groundStepHeightStringer")
    public float groundStepHeight = 0.25f;
    @Entry.FloatRange(max = 9, step = 1f/9f, stringer = "waterStepHeightStringer")
    public float waterStepHeight = 3;
    public transient boolean serverEnabled = false;

    protected String groundStepHeightStringer(float value) {
        return String.format("%d/16", Math.round(value * 16));
    }

    protected String waterStepHeightStringer(float value) {
        return String.format("%d/9", Math.round(value));
    }

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
    public String getId() {
        return Main.MODID;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    public void syncToClients(MinecraftServer server) {
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            syncToClient(player);
        }
    }

    public void syncToClient(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, Main.CONFIG_PACKET_ID, this.writePacket());
    }

    @Override
    public String toString() {
        return "ModConfig{" +
                "groundStepHeight=" + groundStepHeight +
                ", waterStepHeight=" + waterStepHeight +
                ", serverEnabled=" + serverEnabled +
                '}';
    }
}
