package net.minestom.server.instance.palette;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerSettings;
import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaletteOptimizationTest {

    @Test
    public void empty() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var palette = createPalette(minecraftServer);
        paletteEquals(palette.palette, palette.optimizedPalette());
    }

    @Test
    public void single() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var palette = createPalette(minecraftServer);
        palette.set(0, 0, 0, 1);
        paletteEquals(palette.palette, palette.optimizedPalette());
    }

    @Test
    public void random() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var random = new Random(12345);
        var palette = createPalette(minecraftServer);
        palette.setAll((x, y, z) -> random.nextInt(256));
        paletteEquals(palette.palette, palette.optimizedPalette());
        palette.setAll((x, y, z) -> random.nextInt(2));
        paletteEquals(palette.palette, palette.optimizedPalette());
    }

    @Test
    public void manualFill() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var palette = createPalette(minecraftServer);
        palette.setAll((x, y, z) -> 1);
        paletteEquals(palette.palette, palette.optimizedPalette());
        palette.setAll((x, y, z) -> 2);
        paletteEquals(palette.palette, palette.optimizedPalette());
        palette.setAll((x, y, z) -> 0);
        paletteEquals(palette.palette, palette.optimizedPalette());
    }

    AdaptivePalette createPalette(MinecraftServer minecraftServer) {
        return (AdaptivePalette) Palette.blocks(minecraftServer);
    }

    void paletteEquals(Palette palette, Palette optimized) {
        // Verify content
        assertEquals(palette.dimension(), optimized.dimension());
        for (int y = 0; y < palette.dimension(); y++) {
            for (int z = 0; z < palette.dimension(); z++) {
                for (int x = 0; x < palette.dimension(); x++) {
                    assertEquals(palette.get(x, y, z), optimized.get(x, y, z));
                }
            }
        }
        // Verify size
        {
            var array = NetworkBuffer.makeArray(networkBuffer -> networkBuffer.write(palette));
            int length1 = array.length;
            array = NetworkBuffer.makeArray(networkBuffer -> networkBuffer.write(optimized));
            int length2 = array.length;
            assertTrue(length1 >= length2, "Optimized palette is bigger than the original one: " + length1 + " : " + length2);
        }
    }
}
