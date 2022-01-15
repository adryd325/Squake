package squeek.quakemovement;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import java.io.File;
import java.util.List;

public class ModConfig implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = ModInfo.MOD_ID + ".json";

    public static class Config {
        public static final ConfigBoolean uncappedBunnyhopEnabled = new ConfigBoolean("uncappedBunnyhopEnabled", false, "");
        public static final ConfigDouble hardCapThreshold = new ConfigDouble("hardCapThreshold", 2d, 1d, 100d, "");
        public static final ConfigDouble softCapThreshold = new ConfigDouble("softCapThreshold", 1.4d, 1d, 100d,"");
        public static final ConfigDouble softCapDegen = new ConfigDouble("softCapDegen", 0.65d, 0d, 1d, "");

        public static final ConfigBoolean sharkingEnabled = new ConfigBoolean("sharkingEnabled", true, "Allow speed to be maintained in water");
        public static final ConfigDouble sharkingSurfaceTension = new ConfigDouble("sharkingSurfaceTension", 0.2d, 0d, 1d, "Vertical velocity multiplier when landing in water");
        public static final ConfigDouble sharkingWaterFriction = new ConfigDouble("sharkingWaterFriction", 0.99d, 0d, 1d, "Velocity multiplier per tick");

        public static final ConfigDouble groundAccelerate = new ConfigDouble("groundAccelerate", 10d, 0d, 10000d, false, "");
        public static final ConfigDouble airAccelerate = new ConfigDouble("airAccelerate", 14d, 0d, 10000d, false, "");
        public static final ConfigDouble maxAirAccelerationPerTick = new ConfigDouble("maxAirAccelerationPerTick", 0.045d, 0d, 1d, false, "Maximum air acceleration per tick");

        public static final ConfigBoolean trimpEnabled = new ConfigBoolean("trimpEnabled", true, "Pressing shift while going at high speeds will launch you in the air");
        public static final ConfigDouble trimpMultiplier = new ConfigDouble("trimpMultiplier", 1.4d, "");

        // unused
        //public static final ConfigDouble fallDistanceThresholdIncrease = new ConfigDouble("fallDistanceThresholdIncrease", 0d, 0d, 1d, false, "Increase fall damage threshold");

        public static final ConfigHotkey openConfigGui = new ConfigHotkey("openConfigGui", "U,C", "");
        public static final ConfigBooleanHotkeyed enabled = new ConfigBooleanHotkeyed("enabled", true, "U,E", "");

        public static final ConfigOptionList speedometerPosition = new ConfigOptionList("speedometerPosition", SpeedometerPosition.BOTTOM_LEFT, "");
        public static final ConfigBoolean useBlocksPerSecond = new ConfigBoolean("useBlocksPerSecond", true, "Use blocks per second instead of blocks per tick in speedometer");

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(

                uncappedBunnyhopEnabled,
                hardCapThreshold,
                softCapThreshold,
                softCapDegen,

                sharkingEnabled,
                sharkingSurfaceTension,
                sharkingWaterFriction,

                groundAccelerate,
                airAccelerate,
                maxAirAccelerationPerTick,

                trimpEnabled,
                trimpMultiplier,

                // when malilib updates to 0.11.3
                // new BooleanHotkeyGuiWrapper("enabled", enabled, enabled.getKeybind()),
                enabled,
                new ConfigTypeWrapper(ConfigType.HOTKEY, enabled),
                openConfigGui,

                speedometerPosition,
                useBlocksPerSecond
        );

        public static final List<IHotkey> HOTKEY_LIST = ImmutableList.of(
                enabled,
                openConfigGui
        );
    }

    public enum SpeedometerPosition implements IConfigOptionListEntry {
        TOP_LEFT("top_left", "Top left"),
        TOP_RIGHT("top_right", "Top right"),
        BOTTOM_LEFT("bottom_left", "Bottom left"),
        BOTTOM_RIGHT("bottom_right", "Bottom right"),
        OFF("off", "OFF");

        private final String configString;
        private final String displayName;

        SpeedometerPosition(String configString, String displayName) {
            this.configString = configString;
            this.displayName = displayName;
        }

        @Override
        public String getStringValue() {
            return this.configString;
        }

        @Override
        public String getDisplayName() {
            return this.displayName;
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            int id = this.ordinal();
            if (forward) {
                ++id;
                if (id >= values().length) {
                    id = 0;
                }
            } else {
                --id;
                if (id < 0) {
                    id = values().length - 1;
                }
            }

            return values()[id % values().length];
        }

        public SpeedometerPosition fromString(String name) {
            return fromStringStatic(name);
        }

        public static SpeedometerPosition fromStringStatic(String name) {
            for (SpeedometerPosition speedometerPosition : values()) {
                if (speedometerPosition.configString.equalsIgnoreCase(name)) {
                    return speedometerPosition;
                }
            }

            return BOTTOM_LEFT;
        }
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
