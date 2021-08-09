package org.glassfish.jersey;

import org.glassfish.jersey.internal.AbstractServiceFinderConfigurator;
import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.util.PropertiesHelper;

import javax.ws.rs.RuntimeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractFeatureConfigurator<T> extends AbstractServiceFinderConfigurator<T> {
    /**
     * Create a new configurator.
     *
     * @param contract    contract of the service providers bound by this binder.
     * @param runtimeType runtime (client or server) where the service finder binder is used.
     */
    protected AbstractFeatureConfigurator(Class contract, RuntimeType runtimeType) {
        super(contract, runtimeType);
    }

    /**
     * Specification specific implementation which allows find classes by specified classloader
     *
     * @param applicationProperties map of properties to check if search is allowed
     * @param loader specific classloader (must not be NULL)
     * @return list of found classes
     */
    protected List<Class<T>> loadImplementations(Map<String, Object> applicationProperties, ClassLoader loader) {
        if (PropertiesHelper.isMetaInfServicesEnabled(applicationProperties, getRuntimeType())) {
            return Stream.of(ServiceFinder.find(getContract(), loader, true).toClassArray())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Allows feature registration as part of autoDiscoverables list
     *
     * @param features list of features to be registered
     * @param bootstrapBag place where features are being registered
     */
    protected void registerFeatures(List<Class<T>> features,
                                    BootstrapBag bootstrapBag) {
        final List<AutoDiscoverable> autoDiscoverables = new ArrayList<>();

        features.forEach(feature -> autoDiscoverables.add(registerClass(feature)));

        bootstrapBag.getAutoDiscoverables().addAll(autoDiscoverables);
    }

    /**
     * Register particular feature as an autoDiscoverable
     *
     * @param classToRegister class to be registered
     * @param <T> type of class which is being registered
     * @return initialized autoDiscoverable
     */
    private static <T> AutoDiscoverable registerClass(Class<T> classToRegister) {
        return context -> {
            if (!context.getConfiguration().isRegistered(classToRegister)) {
                context.register(classToRegister, AutoDiscoverable.DEFAULT_PRIORITY);
            }
        };
    }

}
