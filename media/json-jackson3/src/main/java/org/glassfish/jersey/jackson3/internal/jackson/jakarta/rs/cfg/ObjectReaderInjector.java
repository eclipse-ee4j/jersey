package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg;

import java.util.concurrent.atomic.AtomicBoolean;

import tools.jackson.databind.ObjectReader;

/**
 * This class allows registering a
 * modifier ({@link ObjectReaderModifier}) that can be used to
 * reconfigure {@link ObjectReader}
 * that Jakarta-RS Resource will use for reading input into Java Objects.
 * Usually this class is accessed from a Servlet or Jakarta-RS filter
 * before execution reaches resource.
 */
public class ObjectReaderInjector
{
    protected static final ThreadLocal<ObjectReaderModifier> _threadLocal = new ThreadLocal<ObjectReaderModifier>();

    /**
     * Simple marker used to optimize out {@link ThreadLocal} access in cases
     * where this feature is not being used
     */
    protected static final AtomicBoolean _hasBeenSet = new AtomicBoolean(false);

    private ObjectReaderInjector() { }

    public static void set(ObjectReaderModifier mod) {
        _hasBeenSet.set(true);
        _threadLocal.set(mod);
    }

    public static ObjectReaderModifier get() {
        return _hasBeenSet.get() ? _threadLocal.get() : null;
    }

    public static ObjectReaderModifier getAndClear() {
        ObjectReaderModifier mod = get();
        if (mod != null) {
            _threadLocal.remove();
        }
        return mod;
    }
}
