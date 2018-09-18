package org.glassfish.jersey.message.internal;

import java.lang.reflect.Type;
import java.util.Collections;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.message.AbstractEntityProviderModel;
import org.glassfish.jersey.message.WriterModel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.glassfish.jersey.message.internal.MessageBodyFactory.WORKER_BY_TYPE_COMPARATOR;

public class MessageBodyFactoryTest {

    @Test
    public void testWorkerByTypeComparatorContract() {
        ArrayList<WriterModel> list = new ArrayList<>();
        list.add(new WriterModel(new BarMessageBodyWriter(), new ArrayList<>(), true));
        list.add(new WriterModel(new ObjectMessageBodyWriter(), new ArrayList<>(), true));
        list.add(new WriterModel(new BazMessageBodyWriter(), new ArrayList<>(), true));

        for (WriterModel a : list) {
            for (WriterModel b : list) {
                assertEquals(
                        "Comparator breaks contract: compare(a, b) != -compare(b, a)",
                        -Integer.signum(WORKER_BY_TYPE_COMPARATOR.compare(a, b)),
                        Integer.signum(WORKER_BY_TYPE_COMPARATOR.compare(b, a)));

                for (WriterModel c : list) {
                    if (WORKER_BY_TYPE_COMPARATOR.compare(a, b) > 0
                              && WORKER_BY_TYPE_COMPARATOR.compare(b, c) > 0
                              && WORKER_BY_TYPE_COMPARATOR.compare(a, c) <= 0) {
                        fail("Comparator breaks contract: a > b and b > c but a <= c");
                    }

                    if (WORKER_BY_TYPE_COMPARATOR.compare(a, b) == 0) {
                        assertEquals(
                                "Comparator breaks contract: a == b but a < c and b > c or vice versa",
                                Integer.signum(WORKER_BY_TYPE_COMPARATOR.compare(a, c)),
                                Integer.signum(WORKER_BY_TYPE_COMPARATOR.compare(b, c)));
                    }
                }
            }
        }
    }

    static class Bar {

    }

    static class Baz extends Bar {

    }

    /** Implements writer for arbitrary class. */
    static class BarMessageBodyWriter implements MessageBodyWriter<Bar> {

        @Override
        public long getSize(Bar bar, Class<?> type, Type genericType, Annotation[] annotation, MediaType mediaType) {
            return -1;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotation, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(
            Bar bar,
            Class<?> type,
            Type genericType,
            Annotation[] annotation,
            MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream entityStream) {}
    }

    /** Implements writer for class that extends from something already provided. */
    static class BazMessageBodyWriter implements MessageBodyWriter<Baz> {

        @Override
        public long getSize(Baz baz, Class<?> type, Type genericType, Annotation[] annotation, MediaType mediaType) {
            return -1;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotation, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(
            Baz baz,
            Class<?> type,
            Type genericType,
            Annotation[] annotation,
            MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream entityStream) {}
    }

    /** Implements writer for arbitrary class that cannot be assigned to from other providers. */
    static class ObjectMessageBodyWriter implements MessageBodyWriter<Integer> {

        @Override
        public long getSize(Integer i, Class<?> type, Type genericType, Annotation[] annotation, MediaType mediaType) {
            return -1;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotation, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(
            Integer i,
            Class<?> type,
            Type genericType,
            Annotation[] annotation,
            MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream entityStream) {}
    }
}
