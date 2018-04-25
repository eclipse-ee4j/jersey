/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.routing;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.internal.Errors;
import org.glassfish.jersey.internal.guava.CacheBuilder;
import org.glassfish.jersey.internal.guava.CacheLoader;
import org.glassfish.jersey.internal.guava.LoadingCache;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.JerseyResourceContext;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.ComponentModelValidator;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.ModelValidationException;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.model.ResourceModelComponent;
import org.glassfish.jersey.server.model.internal.ModelErrors;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * Base for sub-resource locator runtime model builder.
 *
 * @author Michal Gajdos
 */
final class RuntimeLocatorModelBuilder {

    private static final Logger LOGGER = Logger.getLogger(RuntimeLocatorModelBuilder.class.getName());

    private final Configuration config;
    private final RuntimeModelBuilder runtimeModelBuilder;
    private final MessageBodyWorkers messageBodyWorkers;
    private final Collection<ValueParamProvider> valueSuppliers;
    private final JerseyResourceContext resourceContext;
    private final Iterable<ModelProcessor> modelProcessors;
    private final Function<Class<?>, ?> createServiceFunction;
    private final LoadingCache<LocatorCacheKey, LocatorRouting> cache;

    // Configuration.
    private final boolean disableValidation;
    private final boolean ignoreValidationErrors;
    private final boolean enableJerseyResourceCaching;

    /**
     * Create a new instance of the runtime model builder for sub-resource locators.
     *
     * @param config                configuration of the application.
     * @param messageBodyWorkers    message body workers registred in an application.
     * @param valueSuppliers        all value registered value providers.
     * @param resourceContext       resource context to bind sub-resource locator singleton instances.
     * @param runtimeModelBuilder   runtime model builder to build routers for locator models.
     * @param modelProcessors       all registered model processors.
     * @param createServiceFunction function that is able to create and initialize new service.
     */
    RuntimeLocatorModelBuilder(final Configuration config,
                               final MessageBodyWorkers messageBodyWorkers,
                               final Collection<ValueParamProvider> valueSuppliers,
                               final JerseyResourceContext resourceContext,
                               final RuntimeModelBuilder runtimeModelBuilder,
                               final Iterable<ModelProcessor> modelProcessors,
                               final Function<Class<?>, ?> createServiceFunction) {

        this.config = config;
        this.messageBodyWorkers = messageBodyWorkers;
        this.valueSuppliers = valueSuppliers;
        this.runtimeModelBuilder = runtimeModelBuilder;
        this.resourceContext = resourceContext;
        this.modelProcessors = modelProcessors;
        this.createServiceFunction = createServiceFunction;

        // Configuration.
        this.disableValidation = ServerProperties.getValue(config.getProperties(),
                ServerProperties.RESOURCE_VALIDATION_DISABLE,
                Boolean.FALSE,
                Boolean.class);
        this.ignoreValidationErrors = ServerProperties.getValue(config.getProperties(),
                ServerProperties.RESOURCE_VALIDATION_IGNORE_ERRORS,
                Boolean.FALSE,
                Boolean.class);
        this.enableJerseyResourceCaching = ServerProperties.getValue(config.getProperties(),
                ServerProperties.SUBRESOURCE_LOCATOR_CACHE_JERSEY_RESOURCE_ENABLED,
                Boolean.FALSE,
                Boolean.class);

        final int size = ServerProperties.getValue(config.getProperties(),
                ServerProperties.SUBRESOURCE_LOCATOR_CACHE_SIZE,
                ServerProperties.SUBRESOURCE_LOCATOR_DEFAULT_CACHE_SIZE,
                Integer.class);
        final int age = ServerProperties.getValue(config.getProperties(),
                ServerProperties.SUBRESOURCE_LOCATOR_CACHE_AGE,
                -1,
                Integer.class);

        // Cache.
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        if (size > 0) {
            // Size eviction policy.
            cacheBuilder.maximumSize(size);
        } else {
            LOGGER.log(Level.CONFIG, LocalizationMessages.SUBRES_LOC_CACHE_INVALID_SIZE(size,
                    ServerProperties.SUBRESOURCE_LOCATOR_DEFAULT_CACHE_SIZE));

            // Invalid value. Fallback to the default value.
            cacheBuilder.maximumSize(ServerProperties.SUBRESOURCE_LOCATOR_DEFAULT_CACHE_SIZE);
        }
        if (age > 0) {
            // Age eviction policy.
            cacheBuilder.expireAfterAccess(age, TimeUnit.SECONDS);
        }
        cache = cacheBuilder.build(new CacheLoader<LocatorCacheKey, LocatorRouting>() {
            @Override
            public LocatorRouting load(final LocatorCacheKey key) throws Exception {
                return key.clazz != null ? createRouting(key.clazz) : buildRouting(key.resource);
            }
        });
    }

    /**
     * Obtain a sub-resource locator router for given resource method.
     *
     * @param resourceMethod resource method to obtain the router for.
     * @return sub-resource locator router.
     */
    Router getRouter(final ResourceMethod resourceMethod) {
        return new SubResourceLocatorRouter(createServiceFunction, valueSuppliers, resourceMethod, resourceContext, this);
    }

    /**
     * Build (or obtain from cache) a resource model and router for given sub-resource locator class.
     *
     * @param locatorClass sub-resource locator class to built model and router for.
     * @return [locator, router] pair with built model and router for sub-resource locator.
     */
    LocatorRouting getRouting(final Class<?> locatorClass) {
        try {
            return cache.get(new LocatorCacheKey(locatorClass));
        } catch (final ExecutionException ee) {
            LOGGER.log(Level.FINE, LocalizationMessages.SUBRES_LOC_CACHE_LOAD_FAILED(locatorClass), ee);
            return createRouting(locatorClass);
        }
    }

    /**
     * Build (or obtain from cache) a resource model and router for given sub-resource injectionManager
     * {@link org.glassfish.jersey.server.model.Resource resource}.
     *
     * @param subresource sub-resource injectionManager resource to built model and router for.
     * @return [injectionManager, router] pair with built model and router for sub-resource injectionManager.
     */
    LocatorRouting getRouting(final Resource subresource) {
        if (enableJerseyResourceCaching) {
            try {
                return cache.get(new LocatorCacheKey(subresource));
            } catch (final ExecutionException ee) {
                LOGGER.log(Level.FINE, LocalizationMessages.SUBRES_LOC_CACHE_LOAD_FAILED(subresource), ee);
                return buildRouting(subresource);
            }
        } else {
            return buildRouting(subresource);
        }
    }

    /**
     * Check if the model builder contains a cached [locator, router] pair for a given sub-resource locator class.
     *
     * @param srlClass sub-resource locator class.
     * @return {@code true} if the [locator, router] pair  for the sub-resource locator class is present in the cache,
     * {@code false} otherwise.
     */
    boolean isCached(final Class<?> srlClass) {
        return cache.getIfPresent(srlClass) != null;
    }

    private LocatorRouting createRouting(final Class<?> locatorClass) {
        Resource.Builder builder = Resource.builder(locatorClass, disableValidation);
        if (builder == null) {
            // resource is empty - do not throw 404, wait if ModelProcessors add any method
            builder = Resource.builder().name(locatorClass.getName());
        }

        return buildRouting(builder.build());
    }

    private LocatorRouting buildRouting(final Resource subResource) {
        final ResourceModel model = new ResourceModel.Builder(true).addResource(subResource).build();
        final ResourceModel enhancedModel = enhance(model);

        if (!disableValidation) {
            validateResource(enhancedModel);
        }

        final Resource enhancedLocator = enhancedModel.getResources().get(0);
        for (final Class<?> handlerClass : enhancedLocator.getHandlerClasses()) {
            resourceContext.bindResource(handlerClass);
        }

        return new LocatorRouting(enhancedModel,
                runtimeModelBuilder.buildModel(enhancedModel.getRuntimeResourceModel(), true));
    }

    private void validateResource(final ResourceModelComponent component) {
        Errors.process(new Runnable() {
            @Override
            public void run() {
                final ComponentModelValidator validator = new ComponentModelValidator(valueSuppliers, messageBodyWorkers);
                validator.validate(component);

                if (Errors.fatalIssuesFound() && !ignoreValidationErrors) {
                    throw new ModelValidationException(LocalizationMessages.ERROR_VALIDATION_SUBRESOURCE(), ModelErrors
                            .getErrorsAsResourceModelIssues());
                }
            }
        });
    }

    private ResourceModel enhance(ResourceModel subResourceModel) {
        for (final ModelProcessor modelProcessor : modelProcessors) {
            subResourceModel = modelProcessor.processSubResource(subResourceModel, config);
            validateSubResource(subResourceModel);
        }
        return subResourceModel;
    }

    private void validateSubResource(final ResourceModel subResourceModel) {
        if (subResourceModel.getResources().size() != 1) {
            throw new ProcessingException(LocalizationMessages.ERROR_SUB_RESOURCE_LOCATOR_MORE_RESOURCES(subResourceModel
                    .getResources().size()));
        }
    }

    private static class LocatorCacheKey {

        private final Class<?> clazz;
        private final Resource resource;

        public LocatorCacheKey(final Class<?> clazz) {
            this(clazz, null);
        }

        public LocatorCacheKey(final Resource resource) {
            this(null, resource);
        }

        private LocatorCacheKey(final Class<?> clazz, final Resource resource) {
            this.clazz = clazz;
            this.resource = resource;
        }

        @Override
        @SuppressWarnings("RedundantIfStatement")
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final LocatorCacheKey that = (LocatorCacheKey) o;

            if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) {
                return false;
            }
            if (resource != null ? !resource.equals(that.resource) : that.resource != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = clazz != null ? clazz.hashCode() : 0;
            result = 31 * result + (resource != null ? resource.hashCode() : 0);
            return result;
        }
    }
}
