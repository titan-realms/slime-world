package net.titanrealms.titan;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Position;
import net.minestom.server.world.DimensionType;
import net.titanrealms.titan.objects.TitanWorld;

import java.util.UUID;

public class Test {

    private void test() {
        TitanWorld titanWorld = new TitanWorld();
        titanWorld.load();

        InstanceContainer instanceContainer = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD, null);
        instanceContainer.setChunkLoader(new TitanChunkLoader(titanWorld));
        instanceContainer.setBlock(0, 64, 0, Block.BEDROCK);

        GlobalEventHandler events = MinecraftServer.getGlobalEventHandler();
        events.addEventCallback(PlayerLoginEvent.class, (event) -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Position(0.5, 64, 0.5));
            player.setGameMode(GameMode.CREATIVE);
        });
        events.addEventCallback(PlayerDisconnectEvent.class, (event) -> {
            instanceContainer.saveChunksToStorage(titanWorld::save);
        });
    }

        /*
        public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();
        OptifineSupport.enable();

        MinecraftServer.getStorageManager().defineDefaultStorageSystem(FileStorageSystem::new);
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        StorageLocation storageLocation = MinecraftServer.getStorageManager().getLocation("chunk_data");
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(storageLocation);
        instanceContainer.setBlock(0, 64, 0, Block.BEDROCK);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Position(0, 65, 0));
            player.setGameMode(GameMode.CREATIVE);
        });
        globalEventHandler.addEventCallback(PlayerDisconnectEvent.class, event -> {
            instanceContainer.saveChunksToStorage();
        });
        minecraftServer.start("0.0.0.0", 3000);
    }
     */
}
