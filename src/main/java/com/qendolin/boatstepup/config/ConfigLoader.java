package com.qendolin.boatstepup.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.qendolin.boatstepup.Main;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class ConfigLoader {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .create();

    private static boolean isSubPath(Path root, Path path) {
        Path parent = path.getParent();
        while (parent != null) {
            if (parent.equals(root))
                return true;
            parent = parent.getParent();
        }
        return false;
    }

    public static <T extends Config> T createOrLoad(Class<T> configClazz) {
        FabricLoader loader = FabricLoader.getInstance();

        GsonConfig configOptions = configClazz.getAnnotation(GsonConfig.class);
        if(configOptions == null) {
            throw new RuntimeException("Config needs a GsonConfig annotation");
        }
        String configFileName = configOptions.value();
        configFileName += ".json";
        int configVersion = configOptions.version();

        Path configPath = loader.getConfigDir().resolve(configFileName);
        if(!isSubPath(loader.getConfigDir().toAbsolutePath(), configPath.toAbsolutePath())) {
            throw new RuntimeException("Cannot have config file outside config directory");
        }

        File configFile = configPath.toFile();

        if (configFile.exists()) {
            try (FileReader fileReader = new FileReader(configFile)) {
                JsonReader jsonReader = new JsonReader(fileReader);
                jsonReader.setLenient(true);
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(fileReader).getAsJsonObject();
                int saveVersion = object.get("__version").getAsInt();
                if(saveVersion == configVersion) {
                    T config = GSON.fromJson(object, configClazz);
                    Main.LOGGER.info("Loaded config '{}'.", configFileName);
                    return config;
                } else {
                    Main.LOGGER.info("Saved config has old version, discarding.");
                }
            } catch (Exception e) {
                Main.LOGGER.error("Cannot load config!", e);
            }
        }

        T config;
        try {
            config = configClazz.getConstructor().newInstance();
        } catch (Exception e) {
            Main.LOGGER.error("Cannot instance config, need default constructor!", e);
            throw new RuntimeException(e);
        }

        try {
            JsonElement json = GSON.toJsonTree(config);
            json.getAsJsonObject().addProperty("__version", configVersion);
            String jsonString = GSON.toJson(json);
            Files.writeString(configPath, jsonString, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        } catch (Exception e) {
            Main.LOGGER.error("Cannot create config!", e);
            throw new RuntimeException(e);
        }

        Main.LOGGER.info("Created config '{}'.", configFileName);
        return config;
    }
}