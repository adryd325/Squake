package squeek.quakemovement;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ModGUI extends GuiConfigsBase {
    private static GuiTabs tab = GuiTabs.TWEAKS;
    private static GuiTabs lastTab;

    public ModGUI() {
        super(10, 50, ModInfo.MOD_ID, null, "squake.options.title");
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;

        for (GuiTabs tab : GuiTabs.values()) {
            x += this.createButton(x, y, -1, tab);
        }
    }

    private int createButton(int x, int y, int width, GuiTabs tab) {
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
        button.setEnabled(ModGUI.tab != tab);
        this.addButton(button, new ButtonListener(tab, this));

        return button.getWidth() + 2;
    }


    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<? extends IConfigBase> configs;
        GuiTabs tab = ModGUI.tab;

        switch (tab) {
            case TWEAKS:
                configs = ModConfig.Config.OPTIONS;
                break;
            default:
                return Collections.emptyList();
        }

        return ConfigOptionWrapper.createFor(configs);
    }

    private static class ButtonListener implements IButtonActionListener {
        private final ModGUI parent;
        private final GuiTabs tab;

        public ButtonListener(GuiTabs tab, ModGUI parent) {
            this.tab = tab;
            this.parent = parent;
        }


        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            ModGUI.lastTab = ModGUI.tab;
            ModGUI.tab = this.tab;

            this.parent.reCreateListWidget(); // apply the new config width
            Objects.requireNonNull(this.parent.getListWidget()).resetScrollbarPosition();
            this.parent.initGui();
        }
    }


    public enum GuiTabs {
        TWEAKS("squake.options.general");

        private final String translationKey;

        GuiTabs(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }

    }
}
