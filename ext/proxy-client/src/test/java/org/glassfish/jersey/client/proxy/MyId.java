package org.glassfish.jersey.client.proxy;

import java.io.Serializable;

import static java.lang.Long.parseLong;
import static org.glassfish.jersey.internal.guava.MoreObjects.toStringHelper;

public class MyId implements Serializable {
    private final Long value;

    private MyId() {
        this(null);
    }

    private MyId(Long value) {
        this.value = value;
    }

    public static MyId myId(Long value) {
        return new MyId(value);
    }

    public static MyId myId(String value) {
        return myId(parseLong(value));
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyId)) return false;

        MyId that = (MyId) o;
        return value == that.value || value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("value", value)
            .toString();
    }
}
