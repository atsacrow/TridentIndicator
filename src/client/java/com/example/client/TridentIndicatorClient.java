package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

/**
 * Клиентская точка входа мода.
 * Каждый кадр проверяем всех игроков, которых видит клиент
 * (client.world.getPlayers() возвращает только тех, кто в радиусе
 * прорисовки/трекинга сервера) — и если хоть один держит ПКМ с
 * трезубцем в руке (заряжает бросок), рисуем иконку на экране.
 *
 * Если же трезубец уже брошен и летит (TridentEntity есть в мире),
 * показываем другую иконку — состояние "трезубец в полёте" имеет
 * приоритет над состоянием "кто-то заряжает". Свой собственный
 * трезубец (и заряжаемый, и летящий) не учитывается — только чужие.
 */
public class TridentIndicatorClient implements ClientModInitializer {

    // Иконка "кто-то заряжает трезубец"
    public static final Identifier ICON_TEXTURE =
            new Identifier("tridentindicator", "textures/gui/trident_icon.png");

    // Иконка "трезубец уже летит"
    public static final Identifier ICON_TEXTURE_FLYING =
            new Identifier("tridentindicator", "textures/gui/trident_icon_flying.png");

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

        // Приоритет 1: чужой трезубец уже летит
        if (isTridentFlying(client)) {
            drawIcon(context, client, ICON_TEXTURE_FLYING);
            return;
        }

        // Приоритет 2: кто-то (не мы) заряжает трезубец
        boolean someoneChargingTrident = false;

        for (PlayerEntity player : client.world.getPlayers()) {
            // Пропускаем самого себя — иконка должна показывать,
            // что трезубец заряжает КТО-ТО ДРУГОЙ, а не сам игрок.
            if (player == client.player) {
                continue;
            }

            if (isChargingTrident(player)) {
                someoneChargingTrident = true;
                break;
            }
        }

        if (someoneChargingTrident) {
            drawIcon(context, client, ICON_TEXTURE);
        }
    }

    private boolean isChargingTrident(PlayerEntity player) {
        return player.isUsingItem() && player.getActiveItem().isOf(Items.TRIDENT);
    }

    private boolean isTridentFlying(MinecraftClient client) {
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof TridentEntity trident) {
                // Пропускаем свой собственный брошенный трезубец —
                // иконка "трезубец летит" тоже должна касаться только чужих.
                if (trident.getOwner() == client.player) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private void drawIcon(DrawContext context, MinecraftClient client, Identifier texture) {
        int screenWidth = client.getWindow().getScaledWidth();
        int x = (screenWidth - ICON_SIZE) / 2;
        int y = MARGIN_TOP;

        context.drawTexture(texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
}
