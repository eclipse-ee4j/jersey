package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg;

import java.util.concurrent.atomic.AtomicBoolean;

import tools.jackson.databind.ObjectWriter;

/**
 * This class allows "overriding" of {@link ObjectWriter}
 * that Jakarta-RS Resource will use; usually this is done from a Servlet
 * or Jakarta-RS filter before execution reaches resource.
 */
public class ObjectWriterInjector
{
    protected static final ThreadLocal<ObjectWriterModifier> _threadLocal = new ThreadLocal<ObjectWriterModifier>();

    /**
     * Simple marker used to optimize out {@link ThreadLocal} access in cases
     * where this feature is not being used
     */
    protected static final AtomicBoolean _hasBeenSet = new AtomicBoolean(false);

    private ObjectWriterInjector() { }

    public static void set(ObjectWriterModifier mod) {
        _hasBeenSet.set(true);
        _threadLocal.set(mod);
    }

    public static ObjectWriterModifier get() {
        return _hasBeenSet.get() ? _threadLocal.get() : null;
    }

    public static ObjectWriterModifier getAndClear() {
        ObjectWriterModifier mod = get();
        if (mod != null) {
            _threadLocal.remove();
        }
        return mod;
    }
}
