# ByteMC - Minestom

### Description
Will be released soon as possible. The bytemc team is on the way to implements all basic functions.


# Documentation
1. FakePlayers
    ```kotlin
    FakePlayer.initPlayer(PlayerSkin, Instance, Pos, Consumer<FakePlayer>)
    ```
    Also manipulate `FakePlayers` with skin layers:
    ```kotlin
    fakePlayer.meta.allowAllSkinLayers()
    ```
2. Instance
    ```kotlin
    // Disable time:
    Instance.disableTimeRotation()
    ```
3. Extension
    ```kotlin
   // Return path:
   Extension.getConfigPath()
    ```
4. Inventory
   ```kotlin
   // ClickableItem
   ClickableItem(ItemStack).subscribe(Consumer<Player>, ClickType)
   
   // Singleton inventory:
   class Class: SingletonInventory(String, InventoryType, Boolean) { 
        fill(ClickableItem)
        fill(Int, ClickableItem)
        fill(Int, Int, ClickableItem)
    }
   
   // Pageable inventory:
   class Class: PageableInventory<Object>(String, InventoryType, Boolean, List<Object>) { 
        fill(ClickableItem)
        fill(Int, ClickableItem)
        fill(Int, Int, ClickableItem)
        
        override fun construct(Object): ClickableItem
    }
   
   // Anvil inventory:
   class Class: AnvilInventory(String, Player) { 
        override fun onSubmit(Player, String)
    }
   ```
5. Hologram
   ```kotlin
   Hologram(Instance, Point, List<String>)
   // Methods
   spawn()
   destroy()
   ```

6. Point
``` java
point.blockCenter() // return the middle of a block
```

7. Direction
```java

```

# Todo
## API
- clickable block
- clickable Item
- clickable entities
- Minecraft Colors