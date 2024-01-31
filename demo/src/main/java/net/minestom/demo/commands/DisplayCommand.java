package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.ServerFacade;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.TemporalUnit;

public class DisplayCommand extends Command {

    private final ServerFacade serverFacade;

    public DisplayCommand(ServerFacade serverFacade) {
        super("display");
        this.serverFacade = serverFacade;

        var follow = ArgumentType.Literal("follow");

        addSyntax(this::spawnItem, ArgumentType.Literal("item"));
        addSyntax(this::spawnBlock, ArgumentType.Literal("block"));
        addSyntax(this::spawnText, ArgumentType.Literal("text"));

        addSyntax(this::spawnItem, ArgumentType.Literal("item"), follow);
        addSyntax(this::spawnBlock, ArgumentType.Literal("block"), follow);
        addSyntax(this::spawnText, ArgumentType.Literal("text"), follow);
    }

    public void spawnItem(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof Player player))
            return;

        var entity = new Entity(serverFacade, EntityType.ITEM_DISPLAY);
        var meta = (ItemDisplayMeta) entity.getEntityMeta();
        meta.setTransformationInterpolationDuration(20);
        meta.setItemStack(ItemStack.of(Material.STICK));
        entity.setInstance(player.getInstance(), player.getPosition());

        if (context.has("follow")) {
            startSmoothFollow(entity, player);
        }
    }

    public void spawnBlock(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof Player player))
            return;

        var entity = new Entity(serverFacade, EntityType.BLOCK_DISPLAY);
        var meta = (BlockDisplayMeta) entity.getEntityMeta();
        meta.setTransformationInterpolationDuration(20);
        meta.setBlockState(Block.ORANGE_CANDLE_CAKE.stateId());
        entity.setInstance(player.getInstance(), player.getPosition()).join();

        if (context.has("follow")) {
            startSmoothFollow(entity, player);
        }
    }

    public void spawnText(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof Player player))
            return;

        var entity = new Entity(serverFacade, EntityType.TEXT_DISPLAY);
        var meta = (TextDisplayMeta) entity.getEntityMeta();
        meta.setTransformationInterpolationDuration(20);
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setText(Component.text("Hello, world!"));
        entity.setInstance(player.getInstance(), player.getPosition());

        if (context.has("follow")) {
            startSmoothFollow(entity, player);
        }
    }

    private void startSmoothFollow(@NotNull Entity entity, @NotNull Player player) {
//        entity.setCustomName(Component.text("MY CUSTOM NAME"));
//        entity.setCustomNameVisible(true);
        TemporalUnit serverTick = TimeUnit.getServerTick(serverFacade.getServerSettings());
        serverFacade.getSchedulerManager().buildTask(() -> {
            var meta = (AbstractDisplayMeta) entity.getEntityMeta();
            meta.setNotifyAboutChanges(false);
            meta.setTransformationInterpolationStartDelta(1);
            meta.setTransformationInterpolationDuration(20);
//            meta.setPosRotInterpolationDuration(20);
//            entity.teleport(player.getPosition());
//            meta.setScale(new Vec(5, 5, 5));
            meta.setTranslation(player.getPosition().sub(entity.getPosition()));
            meta.setNotifyAboutChanges(true);
        }).delay(20, serverTick).repeat(20, serverTick).schedule();
    }
}
