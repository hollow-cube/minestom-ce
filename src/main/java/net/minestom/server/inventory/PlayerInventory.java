package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.inventory.click.*;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.CloseWindowPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents the inventory of a {@link Player}, retrieved with {@link Player#getInventory()}.
 */
public class PlayerInventory extends AbstractInventory {

    public static final int INVENTORY_SIZE = 46;
    public static final int INNER_SIZE = 36;

    public static final int HELMET_SLOT = 5;
    public static final int CHESTPLATE_SLOT = 6;
    public static final int LEGGINGS_SLOT = 7;
    public static final int BOOTS_SLOT = 8;
    public static final int OFFHAND_SLOT = 45;

    public static final int HOTBAR_START = 36;

    public static final @NotNull ClickHandler CLICK_HANDLER = new StandardClickHandler(
            (player, inventory, item, slot) -> {
                slot = Math.abs(slot);

                IntIterator base = IntIterators.EMPTY_ITERATOR;

                var equipmentSlot = item.material().registry().equipmentSlot();
                if (equipmentSlot != null && slot != equipmentSlot.armorSlot()) {
                    base = IntIterators.concat(base, IntIterators.singleton(equipmentSlot.armorSlot()));
                }

                if (item.material() == Material.SHIELD && slot != OFFHAND_SLOT) {
                    base = IntIterators.concat(base, IntIterators.singleton(OFFHAND_SLOT));
                }

                if (slot < 9 || slot > 35) {
                    base = IntIterators.concat(base, IntIterators.fromTo(9, 36));
                }

                if (slot < 36 || slot > 44) {
                    base = IntIterators.concat(base, IntIterators.fromTo(36, 45));
                }

                if (slot == 0) {
                    base = IntIterators.wrap(IntArrays.reverse(IntIterators.unwrap(base)));
                }

                return base;
            },
            (player, inventory, item, slot) -> IntIterators.fromTo(1, inventory.getSize())
    );

    protected final ClickPreprocessor clickPreprocessor = new ClickPreprocessor(this);

    public PlayerInventory() {
        super(INVENTORY_SIZE);
    }

    @Override
    public synchronized void clear() {
        super.clear();

        // Update equipment
        for (var player : getViewers()) {
            player.sendPacketToViewersAndSelf(player.getEquipmentsPacket());
        }
    }

    @Override
    protected void UNSAFE_itemInsert(int slot, @NotNull ItemStack itemStack) {
        for (var player : getViewers()) {
            final EquipmentSlot equipmentSlot = fromSlotIndex(slot, player.getHeldSlot());
            if (equipmentSlot == null) continue;

            EntityEquipEvent entityEquipEvent = new EntityEquipEvent(player, itemStack, equipmentSlot);
            EventDispatcher.call(entityEquipEvent);
            itemStack = entityEquipEvent.getEquippedItem();
        }

        super.UNSAFE_itemInsert(slot, itemStack);
    }

    @Override
    public void refreshSlot(int slot, @NotNull ItemStack itemStack) {
        super.refreshSlot(slot, itemStack);
        sendPacketToViewers(new SetSlotPacket((byte) 0, 0, (short) slot, itemStack));

        for (var player : getViewers()) {
            var equipmentSlot = fromSlotIndex(slot, player.getHeldSlot());
            if (equipmentSlot == null) continue;

            player.syncEquipment(equipmentSlot, itemStack);
        }
    }

    @Override
    public void handleClose(@NotNull Player player) {
        super.handleClose(player);
        player.sendPacket(new CloseWindowPacket((byte) 0));
    }

    @Override
    public void update(@NotNull Player player) {
        super.update(player);
        player.sendPacket(new WindowItemsPacket((byte) 0, 0, List.of(itemStacks), cursorPlayersItem.getOrDefault(player, ItemStack.AIR)));
    }

    private int getSlotIndex(@NotNull EquipmentSlot slot, int heldSlot) {
        return switch (slot) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> slot.armorSlot();
            case OFF_HAND -> OFFHAND_SLOT;
            case MAIN_HAND -> HOTBAR_START + heldSlot;
        };
    }

    private @Nullable EquipmentSlot fromSlotIndex(int slot, int heldSlot) {
        return switch (slot) {
            case HELMET_SLOT -> EquipmentSlot.HELMET;
            case CHESTPLATE_SLOT -> EquipmentSlot.CHESTPLATE;
            case LEGGINGS_SLOT -> EquipmentSlot.LEGGINGS;
            case BOOTS_SLOT -> EquipmentSlot.BOOTS;
            case OFFHAND_SLOT -> EquipmentSlot.OFF_HAND;
            default -> slot == (HOTBAR_START + heldSlot) ? EquipmentSlot.MAIN_HAND : null;
        };
    }

    public @NotNull ItemStack getEquipment(@NotNull EquipmentSlot slot, int heldSlot) {
        return getItemStack(getSlotIndex(slot, heldSlot));
    }

    public void setEquipment(@NotNull EquipmentSlot slot, int heldSlot, @NotNull ItemStack newValue) {
        setItemStack(getSlotIndex(slot, heldSlot), newValue);
    }

    private static final int[] EXISTING_ADD_SLOTS = IntStream.concat(
            IntStream.concat(
                    IntStream.rangeClosed(36, 44),
                    IntStream.of(OFFHAND_SLOT)
            ),
            IntStream.rangeClosed(9, 35)
    ).toArray();

    private static final int[] AIR_ADD_SLOTS = IntStream.concat(
            IntStream.rangeClosed(36, 44),
            IntStream.rangeClosed(9, 35)
    ).toArray();

    private static final int[] TAKE_SLOTS = IntStream.concat(
            IntStream.rangeClosed(36, 45),
            IntStream.concat(
                    IntStream.rangeClosed(9, 35),
                    IntStream.rangeClosed(0, 8)
            )
    ).toArray();

    @Override
    public <T> @NotNull T addItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.add(() -> IntIterators.wrap(EXISTING_ADD_SLOTS), () -> IntIterators.wrap(AIR_ADD_SLOTS)), option);
    }

    @Override
    public <T> @NotNull T takeItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.take(() -> IntIterators.wrap(TAKE_SLOTS)), option);
    }

    @Override
    public @Nullable ClickResult handleClick(@NotNull Player player, @NotNull ClickInfo clickInfo) {
        return handleClick(this, CLICK_HANDLER, player, clickInfo);
    }

    public static @NotNull IntIterator getInnerShiftClickSlots(@NotNull Player player, @NotNull AbstractInventory inventory, @NotNull ItemStack item, int slot) {
        return IntIterators.fromTo(ClickPreprocessor.PLAYER_INVENTORY_OFFSET + 9, ClickPreprocessor.PLAYER_INVENTORY_OFFSET + 45);
    }

    public static @NotNull IntIterator getInnerDoubleClickSlots(@NotNull Player player, @NotNull AbstractInventory inventory, @NotNull ItemStack item, int slot) {
        return IntIterators.fromTo(ClickPreprocessor.PLAYER_INVENTORY_OFFSET + 9, ClickPreprocessor.PLAYER_INVENTORY_OFFSET + 45);
    }
}
