package squeek.quakemovement;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.MinecraftClient;

public class KeyCallbacks {
    public static void init(MinecraftClient client) {
        IHotkeyCallback callbackGeneric = new KeyCallbackHotkeysGeneric(client);

        ModConfig.Config.openConfigGui.getKeybind().setCallback(callbackGeneric);
    }

    private static class KeyCallbackHotkeysGeneric implements IHotkeyCallback {
        private final MinecraftClient client;

        public KeyCallbackHotkeysGeneric(MinecraftClient client) {
            this.client = client;
        }

        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (key == ModConfig.Config.openConfigGui.getKeybind()) {
                GuiBase.openGui(new ModGUI());
                return true;
            }

            return false;
        }
    }
}