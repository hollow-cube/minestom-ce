package net.bytemc.minestom.server.inventory.item.impl;

import net.bytemc.minestom.server.inventory.SingletonInventory;
import net.bytemc.minestom.server.inventory.item.Item;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public final class SwitchItem implements Item {
    private final Map<Item, Consumer<Player>> switches;

    private int currentItem;
    private Predicate<Player> predicate;

    public SwitchItem(ItemStack item, Consumer<Player> consumer) {
        this.switches = new HashMap<>();
        this.switches.put(new ClickableItem(item), consumer);

        this.currentItem = 0;
    }

    public SwitchItem(ItemStack item, Consumer<Player> consumer, Predicate<Player> predicate) {
        this.switches = new HashMap<>();
        this.switches.put(new ClickableItem(item), consumer);
        this.predicate = predicate;

        this.currentItem = 0;
    }

    public SwitchItem addSwitch(Item item, Consumer<Player> onSwitch) {
        this.switches.put(item, onSwitch);
        return this;
    }

    public ItemStack getItemStack() {
        return switches.keySet().stream().toList().get(0).getItemStack();
    }

    public void click(Player player, SingletonInventory inventory, int slot) {
        if(predicate != null && !this.predicate.test(player)) {
            return;
        }
        currentItem++;
        if(currentItem >= switches.size()) {
            currentItem = 0;
        }

        var item = switches.entrySet().stream().toList().get(0);
        item.getValue().accept(player);
        inventory.fill(slot, item.getKey());
    }
}
