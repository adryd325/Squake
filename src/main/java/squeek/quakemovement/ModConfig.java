package squeek.quakemovement;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import java.io.File;
import java.util.List;

public class ModConfig implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = ModInfo.MOD_ID + ".json";

    public static class Config {
        public static final ConfigDouble trimpMultiplier = new ConfigDouble("trimpMultiplier", 1.4d, "");
        public static final ConfigDouble hardCapThreshold = new ConfigDouble("hardCapThreshold", 2d, "");
        public static final ConfigDouble softCapThreshold = new ConfigDouble("softCapThreshold", 1.4d, "");
        public static final ConfigDouble softCapDegen = new ConfigDouble("softCapDegen", 0.65d, "");
        public static final ConfigBoolean sharkingEnabled = new ConfigBoolean("sharkingEnabled", true, "");
        public static final ConfigDouble sharkingSurfaceTension = new ConfigDouble("sharkingSurfaceTension", 0.2d, "");
        public static final ConfigDouble sharkingWaterFriction = new ConfigDouble("sharkingWaterFriction", 0.99d, "");
        public static final ConfigDouble groundAccelerate = new ConfigDouble("groundAccelerate", 10d, "");
        public static final ConfigDouble airAccelerate = new ConfigDouble("airAccelerate", 14d, "");
        public static final ConfigBoolean uncappedBunnyhopEnabled = new ConfigBoolean("uncappedBunnyhopEnabled", false, "");
        public static final ConfigBoolean trimpEnabled = new ConfigBoolean("trimpEnabled", true, "");
        public static final ConfigDouble fallDistanceThresholdIncrease = new ConfigDouble("fallDistanceThresholdIncrease", 0.0d, "");
        public static final ConfigDouble maxAirAccelerationPerTick = new ConfigDouble("maxAirAccelerationPerTick", 0.045d, "");
        public static final ConfigBoolean enabled = new ConfigBoolean("enabled", true, "");
        public static final ConfigHotkey openConfigGui = new ConfigHotkey("openConfigGui", "U,C", "");
        public static final ConfigHotkey toggleEnabled = new ConfigHotkey("toggleEnabled", "U,E", KeybindSettings.MODIFIER_INGAME, "");

        public static final ImmutableList<IConfigValue> OPTIONS = ImmutableList.of(
                trimpMultiplier,
                hardCapThreshold,
                softCapThreshold,
                softCapDegen,
                sharkingEnabled,
                sharkingSurfaceTension,
                sharkingWaterFriction,
                groundAccelerate,
                airAccelerate,
                uncappedBunnyhopEnabled,
                trimpEnabled,
                fallDistanceThresholdIncrease,
                maxAirAccelerationPerTick,
                enabled,
                openConfigGui,
                toggleEnabled
        );

        public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
                toggleEnabled,
                openConfigGui
        );
    }

    public enum SpeedometerPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        OFF
    }


    public static final SpeedometerPosition speedometerPosition = SpeedometerPosition.OFF;

    public static SpeedometerPosition getSpeedometerPosition() {
        return speedometerPosition;
    }

    public static void loadFromFile() {
        File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME);

        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            JsonElement element = JsonUtils.parseJsonFile(configFile);
            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();

                ConfigUtils.readConfigBase(root, "params", ModConfig.Config.OPTIONS);
            }
        }
    }

    public static void saveToFile() {
        File dir = FileUtils.getConfigDirectory();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            JsonObject root = new JsonObject();

            ConfigUtils.writeConfigBase(root, "params", ModConfig.Config.OPTIONS);

            JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveToFile();
    }

}
