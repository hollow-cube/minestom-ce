package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.*;
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket;

public class EntityActionListener {

    public static void listener(ClientEntityActionPacket packet, Player player) {
        switch (packet.action()) {
            case START_SNEAKING -> EntityActionListener.setSneaking(player, true);
            case STOP_SNEAKING -> EntityActionListener.setSneaking(player, false);
            case START_SPRINTING -> EntityActionListener.setSprinting(player, true);
            case STOP_SPRINTING -> EntityActionListener.setSprinting(player, false);
            case START_FLYING_ELYTRA -> EntityActionListener.startFlyingElytra(player);

            // TODO do remaining actions
        }
    }

    private static void setSneaking(Player player, boolean sneaking) {
        boolean oldState = player.isSneaking();
        player.setSneaking(sneaking);

        if (oldState != sneaking) {
            if (sneaking) {
                player.getGlobalEventHandler().call(new PlayerStartSneakingEvent(player));
            } else {
                player.getGlobalEventHandler().call(new PlayerStopSneakingEvent(player));
            }
        }
    }

    private static void setSprinting(Player player, boolean sprinting) {
        boolean oldState = player.isSprinting();

        player.setSprinting(sprinting);

        if (oldState != sprinting) {
            if (sprinting) {
                player.getGlobalEventHandler().call(new PlayerStartSprintingEvent(player));
            } else {
                player.getGlobalEventHandler().call(new PlayerStopSprintingEvent(player));
            }
        }
    }

    private static void startFlyingElytra(Player player) {
        player.setFlyingWithElytra(true);
        player.getGlobalEventHandler().call(new PlayerStartFlyingWithElytraEvent(player));
    }
}
