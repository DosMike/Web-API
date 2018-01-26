package valandur.webapi.block;

import com.flowpowered.math.vector.Vector3i;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BiomeVolume;
import valandur.webapi.WebAPI;
import valandur.webapi.api.block.IBlockOperation;
import valandur.webapi.api.block.IBlockService;
import valandur.webapi.api.cache.world.ICachedWorld;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BlockService implements IBlockService {
    private static Map<UUID, IBlockOperation> blockOps = new ConcurrentHashMap<>();

    private static final String UNKOWN_BIOME_ID = "<unknown>";
    private static int MAX_BLOCK_GET_SIZE = 1000000;
    private static int MAX_BLOCK_UPDATE_SIZE = 1000000;
    private static int MAX_BLOCKS_PER_SECOND = 10000;
    private static Vector3i BIOME_INTERVAL = new Vector3i(4, 0, 4);


    public void init(ConfigurationNode config) {
        MAX_BLOCK_GET_SIZE = config.getNode("maxBlockGetSize").getInt();
        MAX_BLOCK_UPDATE_SIZE = config.getNode("maxBlockUpdateSize").getInt();
        MAX_BLOCKS_PER_SECOND = config.getNode("maxBlocksPerSecond").getInt();
    }

    @Override
    public IBlockOperation startBlockOperation(IBlockOperation operation) {
        blockOps.put(operation.getUUID(), operation);
        operation.start();
        return operation;
    }

    @Override
    public Collection<IBlockOperation> getBlockOperations() {
        return blockOps.values().stream().map(b -> (IBlockOperation)b).collect(Collectors.toList());
    }
    @Override
    public Optional<IBlockOperation> getBlockOperation(UUID uuid) {
        if (!blockOps.containsKey(uuid))
            return Optional.empty();
        return Optional.of(blockOps.get(uuid));
    }

    @Override
    public Optional<BlockState> getBlockAt(ICachedWorld world, Vector3i pos) {
        return WebAPI.runOnMain(() -> {
            Optional<?> obj = world.getLive();

            if (!obj.isPresent())
                return null;

            World w = (World)obj.get();
            return w.getBlock(pos).copy();
        });
    }


    @Override
    public int getMaxBlocksPerSecond() {
        return MAX_BLOCKS_PER_SECOND;
    }
    @Override
    public int getMaxGetBlocks() {
        return MAX_BLOCK_GET_SIZE;
    }
    @Override
    public int getMaxUpdateBlocks() {
        return MAX_BLOCK_UPDATE_SIZE;
    }

    @Override
    public Vector3i getBiomeInterval() {
        return BIOME_INTERVAL;
    }

    @Override
    public String getUnknownBiomeId() {
        return UNKOWN_BIOME_ID;
    }

    @Override
    public Optional<String[][]> getBiomes(ICachedWorld world, Vector3i min, Vector3i max) {
        return WebAPI.runOnMain(() -> {
            Optional<?> obj = world.getLive();

            if (!obj.isPresent())
                return null;

            World w = (World)obj.get();
            BiomeVolume vol = w.getBiomeView(min, max).getRelativeBiomeView();
            Vector3i size = vol.getBiomeSize();

            int maxX = (int)Math.ceil(size.getX() / (float)BIOME_INTERVAL.getX());
            int maxZ = (int)Math.ceil(size.getZ() / (float)BIOME_INTERVAL.getZ());
            String[][] biomes = new String[maxX][maxZ];
            for (int x = 0; x < maxX; x++) {
                for (int z = 0; z < maxZ; z++) {
                    int newX = x * BIOME_INTERVAL.getX();
                    int newZ = z * BIOME_INTERVAL.getZ();
                    biomes[x][z] = vol.getBiome(newX, 0, newZ).getId();
                    if (biomes[x][z] == null) {
                        WebAPI.getLogger().warn("Unknown biome at [" + (min.getX() + newX) + "," + (min.getZ() + newZ) + "]");
                        biomes[x][z] = UNKOWN_BIOME_ID;
                    }
                }
            }

            return biomes;
        });
    }
}
