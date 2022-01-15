package squeek.quakemovement;

import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import squeek.quakemovement.ModConfig.SpeedometerPosition;

public class ModQuakeMovement implements ModInitializer {

    @Override
    public void onInitialize() {
        //Cause this class to be loaded so the config loads on startup
		InitializationHandler.getInstance().registerInitializationHandler(new ModInitHandler());
    }

    public static void drawSpeedometer(MatrixStack matrices) {
        if (ModConfig.Config.speedometerPosition.getOptionListValue() == SpeedometerPosition.OFF) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        double deltaX = player.getX() - player.prevX;
        double deltaZ = player.getZ() - player.prevZ;
        // multiply by 20 for blocks per second
        double multiplier = ModConfig.Config.useBlocksPerSecond.getBooleanValue() ? 20 : 1;
        double speed = MathHelper.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ)) * multiplier;
        String speedString = String.format("%.02f", speed);
        int x;
        int y;
        if (ModConfig.Config.speedometerPosition.getOptionListValue() == SpeedometerPosition.BOTTOM_LEFT || ModConfig.Config.speedometerPosition.getOptionListValue()  == SpeedometerPosition.TOP_LEFT) {
            x = 10;
        } else {
            x = mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(speedString) - 10;
        }
        if (ModConfig.Config.speedometerPosition.getOptionListValue() == SpeedometerPosition.TOP_RIGHT || ModConfig.Config.speedometerPosition.getOptionListValue() == SpeedometerPosition.TOP_LEFT) {
            y = 10;
        } else {
            y = mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 10;
        }
        matrices.push();
        mc.textRenderer.drawWithShadow(matrices, speedString, x, y, 0xFFDDDDDD);
        matrices.pop();
    }
}