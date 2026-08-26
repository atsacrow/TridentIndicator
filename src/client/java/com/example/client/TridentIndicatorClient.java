package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

/**
 * Клиентская точка входа мода.
 * Каждый кадр проверяем всех игроков, которых видит клиент
 * (client.world.getPlayers() возвращает только тех, кто в радиусе
 * прорисовки/трекинга сервера) — и если хоть один держит ПКМ с
 * трезубцем в руке (заряжает бросок), рисуем иконку на экране.
 */
public class TridentIndicatorClient implements ClientModInitializer {

    // Текстура иконки: assets/tridentindicator/textures/gui/trident_icon.png
    public static final Identifier ICON_TEXTURE =
            new Identifier("tridentindicator", "textures/gui/trident_icon.png");

    // Размер иконки на экране в пикселях. Должен совпадать с реальным
    // размером PNG-файла, иначе текстура растянется/обрежется некорректно.
    private static final int ICON_SIZE = 32;

    // Отступ от верхнего края экрана
    private static final int MARGIN_TOP = 10;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return;
        }

        // Не рисуем иконку поверх других экранов (инвентарь, меню и т.п.)
        if (client.currentScreen != null) {
            return;
        }

        boolean someoneChargingTrident = false;

        for (PlayerEntity player : client.world.getPlayers()) {
            if (isChargingTrident(player)) {
                someoneChargingTrident = true;
                break;
            }
        }

        if (someoneChargingTrident) {
            drawIcon(context, client);
        }
    }

    private boolean isChargingTrident(PlayerEntity player) {
        return player.isUsingItem() && player.getActiveItem().isOf(Items.TRIDENT);
    }

    private void drawIcon(DrawContext context, MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int x = (screenWidth - ICON_SIZE) / 2;
        int y = MARGIN_TOP;

        context.drawTexture(ICON_TEXTURE, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
}
