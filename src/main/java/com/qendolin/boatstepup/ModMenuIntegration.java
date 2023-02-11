package com.qendolin.boatstepup;

import com.qendolin.boatstepup.config.Config;
import com.qendolin.boatstepup.config.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;

import java.util.List;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new CustomConfigScreen(parent, Main.RUNTIME_CONFIG);
    }

    static class CustomConfigScreen extends ConfigScreen<ModConfig> {

        private final Text notInstalledText = Text.translatable("boat_step_up.config.notInstalledOnServer");
        private final Text usingServerText = Text.translatable("boat_step_up.config.usingServerConfig");

        public CustomConfigScreen(Screen parent, ModConfig config) {
            super(parent, config, CustomConfigScreen::onClose);
        }

        public static <C extends Config> void onClose(boolean save, C config, List<ConfigScreen.EntryValueSetter<?>> valueSetters) {
            ConfigScreen.onCloseDefault(save, config, valueSetters);
            if(save) {
                Main.CONFIG.copyFrom(Main.RUNTIME_CONFIG);
                IntegratedServer server = MinecraftClient.getInstance().getServer();
                if(server != null) Main.CONFIG.syncToClients(server);
            }
        }


        @Override
        protected void init() {
            super.init();
            if(!client.isInSingleplayer() && client.world != null) {
                doneButton.active = false;
                for (ConfigScreen.ConfigEntry entry : entries.values()) {
                    entry.widget().active = false;
                }
            }
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            if(!client.isInSingleplayer() && client.world != null) {
                if(!Main.RUNTIME_CONFIG.serverEnabled) {
                    drawCenteredText(matrices, textRenderer, notInstalledText, width/2, height-textRenderer.fontHeight-30-8, 0xffaaaa);
                } else {
                    drawCenteredText(matrices, textRenderer, usingServerText, width/2, height-textRenderer.fontHeight-30-8, 0xffaaaa);
                }
            }
        }
    }
}
