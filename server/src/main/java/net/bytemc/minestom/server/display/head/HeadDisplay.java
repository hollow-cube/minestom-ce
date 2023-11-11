package net.bytemc.minestom.server.display.head;

import net.bytemc.minestom.server.display.head.misc.HeadLetter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class HeadDisplay {
    private final String value;
    private final Instance instance;
    private final Pos pos;

    private final HeadSettings settings;
    private final List<EntityCreature> entities;

    public HeadDisplay(String value, Instance instance, Pos pos, Consumer<HeadSettings> settings) {
        this.value = value;
        this.instance = instance;
        this.pos = pos;

        var headSettings = new HeadSettings();
        settings.accept(headSettings);
        this.settings = headSettings;
        this.entities = new ArrayList<>();
    }

    public void spawn() {
        for (int i = 0; i < value.length(); i++) {
            var letter = String.valueOf(value.charAt(i)).toUpperCase();
            if(letter.isEmpty() || letter.equals(" ")) {
                if(!settings.getSpacer()) {
                    continue;
                }
                letter = "BLANK";
            }
            var stack = ItemStack.of(HeadLetter.getOrThrow(letter).getSkinData());
            var entity = getEntityCreature(stack);
            var spawnPos = rotatePos(pos, settings.getDirection(), i / (settings.getHeadSize().getDistance() - settings.getAdditionDistance()));
            entity.setInstance(instance, spawnPos).whenComplete((unused, throwable) -> {
                entity.spawn();
            });
            entities.add(entity);
        }
    }

    public void destroy() {
        for (EntityCreature entity : entities) {
            entity.remove();
        }
        entities.clear();
    }

    @NotNull
    private EntityCreature getEntityCreature(ItemStack stack) {
        var entity = new EntityCreature(EntityType.ARMOR_STAND);
        var meta = (ArmorStandMeta) entity.getEntityMeta();

        entity.setNoGravity(true);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        meta.setHasArms(true);

        switch (settings.getHeadSize()) {
            case BIG -> {
                meta.setSmall(false);
                entity.setHelmet(stack);
            }
            case MID -> {
                meta.setSmall(true);
                entity.setHelmet(stack);
            }
            case SMALL -> {
                entity.setInvisible(false);
                meta.setSmall(false);
                //meta.setRightArmRotation(new Vec(280, 0, 0));
                meta.setRightArmRotation(new Vec(330, 360, 0));
                entity.setItemInMainHand(stack);
            }
            // TODO: Do SMALL and TINY
        }
        return entity;
    }

    private Pos rotatePos(Pos pos, Direction direction, double distance) {
        var newPos = pos.withYaw(settings.getDirection().getYaw());
        switch (direction) {
            case UP -> newPos = newPos.add(0, distance, 0);
            case DOWN -> newPos = newPos.sub(0, distance, 0);
            case NORTH -> newPos = newPos.sub(distance, 0, 0);
            case SOUTH -> newPos = newPos.add(distance, 0, 0);
            case WEST -> newPos = newPos.add(0, 0, distance);
            case EAST -> newPos = newPos.sub(0, 0, distance);
        }
        return newPos;
    }
}
