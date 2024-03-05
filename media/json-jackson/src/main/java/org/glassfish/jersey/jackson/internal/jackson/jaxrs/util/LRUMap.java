package org.glassfish.jersey.jackson.internal.jackson.jaxrs.util;

import java.util.LinkedHashMap;
import java.util.Map;

// TO BE REMOVED FROM JACKSON 2.18 or later
/**
 * Helper for simple bounded LRU maps used for reusing lookup values.
 *
 * @since 2.2
 *
 * @deprecated Since 2.16.1 Use one from {@code jackson-databind} instead.
 */
@Deprecated // since 2.16.1
@SuppressWarnings("serial")
public class LRUMap<K,V> extends LinkedHashMap<K,V>
{
    protected final int _maxEntries;
    
    public LRUMap(int initialEntries, int maxEntries)
    {
        super(initialEntries, 0.8f, true);
        _maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest)
    {
        return size() > _maxEntries;
    }
}
