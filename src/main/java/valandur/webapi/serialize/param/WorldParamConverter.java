package valandur.webapi.serialize.param;

import valandur.webapi.WebAPI;
import valandur.webapi.api.cache.world.ICachedWorld;
import valandur.webapi.api.cache.world.ICachedWorldFull;
import valandur.webapi.cache.CacheService;
import valandur.webapi.util.Util;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.ParamConverter;
import java.util.Optional;
import java.util.UUID;

public class WorldParamConverter implements ParamConverter<ICachedWorld> {

    @Override
    public ICachedWorld fromString(String value) {
        // If we didn't request a world don't try to find one
        if (value == null)
            return null;

        if (!Util.isValidUUID(value))
            throw new BadRequestException("Invalid world uuid");

        CacheService srv = WebAPI.getCacheService();

        Optional<ICachedWorldFull> optWorld = srv.getWorld(UUID.fromString(value));
        if (!optWorld.isPresent())
            throw new NotFoundException("Could not find world with uuid " + value);
        return optWorld.get();
    }

    @Override
    public String toString(ICachedWorld value) {
        return value.getLink();
    }
}
