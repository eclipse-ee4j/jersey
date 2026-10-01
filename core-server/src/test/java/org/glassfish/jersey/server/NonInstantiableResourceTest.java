/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.glassfish.jersey.innate.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ComponentProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Resource classes which cannot be instantiated (interfaces, abstract classes) are ignored by the
 * binding. Their resource methods must not end up in the resource model either, where they would
 * collide with methods of real resources using the same path or expose endpoints failing with 500.
 * <p>
 * Such classes typically come from class path scanning: API interfaces implemented by resources,
 * MicroProfile REST Client interfaces, or abstract base classes.
 */
public class NonInstantiableResourceTest {

    private final List<LogRecord> warnings = new ArrayList<>();

    private final Handler warningCollector = new Handler() {
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                warnings.add(record);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    private final Logger logger = Logger.getLogger(ResourceModelConfigurator.class.getName());

    @BeforeEach
    public void collectWarnings() {
        logger.addHandler(warningCollector);
    }

    @AfterEach
    public void stopCollectingWarnings() {
        logger.removeHandler(warningCollector);
    }

    @Path("shared")
    public interface SharedApi {
        @GET
        String get();
    }

    @Path("shared")
    public static class SharedResource implements SharedApi {
        @Override
        public String get() {
            return "shared-resource";
        }
    }

    @Test
    public void interfaceImplementedByResourceWithSamePath() throws Exception {
        ApplicationHandler app = new ApplicationHandler(new ResourceConfig(SharedApi.class, SharedResource.class));

        assertGet(app, "/shared", 200, "shared-resource");
        assertEquals(List.of(), warnings);
    }

    @Path("greeting")
    public interface GreetingClient {
        @GET
        String greet();
    }

    @Path("greeting")
    public static class GreetingResource {
        @GET
        public String greet() {
            return "greeting-resource";
        }
    }

    @Test
    public void unrelatedInterfaceWithSamePath() throws Exception {
        ApplicationHandler app = new ApplicationHandler(
                new ResourceConfig(GreetingClient.class, GreetingResource.class));

        assertGet(app, "/greeting", 200, "greeting-resource");
        assertEquals(List.of(), warnings);
    }

    @Path("orphan")
    public interface OrphanApi {
        @GET
        String get();
    }

    @Path("items")
    public interface ItemsClient {
        @GET
        @Path("remote")
        String remote();
    }

    @Path("items")
    public static class ItemsResource {
        @GET
        public String list() {
            return "items-resource";
        }
    }

    @Test
    public void interfacesWithoutImplementation() throws Exception {
        ApplicationHandler app = new ApplicationHandler(
                new ResourceConfig(OrphanApi.class, ItemsClient.class, ItemsResource.class));

        assertGet(app, "/items", 200, "items-resource");
        assertGet(app, "/items/remote", 404, null);
        assertGet(app, "/orphan", 404, null);
        assertEquals(List.of(), warnings);
    }

    @Path("abstract")
    public abstract static class AbstractResource {
        @GET
        public abstract String get();
    }

    @Path("abstract")
    public static class ConcreteResource extends AbstractResource {
        @Override
        public String get() {
            return "concrete-resource";
        }
    }

    @Test
    public void abstractClassExtendedByResourceWithSamePath() throws Exception {
        ApplicationHandler app = new ApplicationHandler(
                new ResourceConfig(AbstractResource.class, ConcreteResource.class));

        assertGet(app, "/abstract", 200, "concrete-resource");
        assertEquals(1, warnings.size(), "Abstract classes are still reported: " + warnings);
    }

    /**
     * Marks interfaces {@link InterfaceComponentProvider} binds to the given implementation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface BoundTo {
        Class<?> value();
    }

    @Path("bound")
    @BoundTo(BoundImplementation.class)
    public interface BoundApi {
        @GET
        String get();
    }

    public static class BoundImplementation implements BoundApi {
        @Override
        public String get() {
            return "bound-implementation";
        }
    }

    /**
     * Component provider which is able to bind interfaces, as some DI integrations do.
     */
    public static class InterfaceComponentProvider implements ComponentProvider {

        private InjectionManager injectionManager;

        @Override
        public void initialize(InjectionManager injectionManager) {
            this.injectionManager = injectionManager;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {
            BoundTo boundTo = component.getAnnotation(BoundTo.class);
            if (boundTo == null) {
                return false;
            }
            injectionManager.register(Bindings.supplier(() -> newInstance(boundTo.value())).to((Class) component));
            return true;
        }

        private static Object newInstance(Class<?> implementation) {
            try {
                return implementation.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void done() {
        }
    }

    @Test
    public void interfaceBoundByComponentProviderIsKept() throws Exception {
        ApplicationHandler app = new ApplicationHandler(new ResourceConfig(BoundApi.class));

        assertGet(app, "/bound", 200, "bound-implementation");
        assertEquals(List.of(), warnings);
    }

    private static void assertGet(ApplicationHandler app, String path, int expectedStatus, String expectedEntity)
            throws Exception {
        ContainerResponse response = app.apply(RequestContextBuilder.from(path, "GET").build()).get();
        assertEquals(expectedStatus, response.getStatus(), "HTTP status of " + path);
        if (expectedEntity != null) {
            assertEquals(expectedEntity, response.getEntity(), "Response of " + path);
        }
    }
}
