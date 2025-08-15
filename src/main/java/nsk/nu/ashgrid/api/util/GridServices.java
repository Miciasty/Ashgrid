package nsk.nu.ashgrid.api.util;

import nsk.nu.ashcore.api.spi.Identified;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service discovery helper for Identified SPI implementations.
 */
public final class GridServices {
    private GridServices() {}

    /** Load all implementations for a given SPI type and index them by {@link Identified#id()}. */
    public static <T extends Identified> Map<String, T> byId(Class<T> spi) {
        ServiceLoader<T> loader = ServiceLoader.load(spi);
        Map<String, T> map = new LinkedHashMap<>();
        for (T impl : loader) map.put(impl.id(), impl);
        return map;
    }

    /** Get a specific implementation by id or throw if missing. */
    public static <T extends Identified> T require(Class<T> spi, String id) {
        return Optional.ofNullable(byId(spi).get(id))
                .orElseThrow(() -> new NoSuchElementException(spi.getSimpleName()+" id="+id+" not found"));
    }

    /** List available implementation ids for debugging/docs. */
    public static <T extends Identified> List<String> ids(Class<T> spi) {
        return byId(spi).keySet().stream().toList();
    }
}