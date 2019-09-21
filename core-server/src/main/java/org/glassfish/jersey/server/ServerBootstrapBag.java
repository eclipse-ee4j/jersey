/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collection;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.internal.JerseyResourceContext;
import org.glassfish.jersey.server.internal.ProcessingProviders;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.process.RequestProcessingContext;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.ResourceMethodInvoker;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.spi.ComponentProvider;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * {@inheritDoc}
 * <p>
 * This bootstrap bag is specialized for server part of Jersey.
 *
 * @author Petr Bouda
 */
public class ServerBootstrapBag extends BootstrapBag {

    private Application application;
    private ApplicationHandler applicationHandler;
    private Collection<ValueParamProvider> valueParamProviders;
    private MultivaluedParameterExtractorProvider multivaluedParameterExtractorProvider;
    private ProcessingProviders processingProviders;
    private JerseyResourceContext resourceContext;
    private LazyValue<Collection<ComponentProvider>> componentProviders;
    private ResourceMethodInvoker.Builder resourceMethodInvokerBuilder;
    private ResourceBag resourceBag;
    private ResourceModel resourceModel;
    private Collection<ModelProcessor> modelProcessors;

    public Collection<ModelProcessor> getModelProcessors() {
        return modelProcessors;
    }

    public void setModelProcessors(Collection<ModelProcessor> modelProcessors) {
        this.modelProcessors = modelProcessors;
    }

    public ResourceBag getResourceBag() {
        requireNonNull(resourceBag, ResourceBag.class);
        return resourceBag;
    }

    public void setResourceBag(ResourceBag resourceBag) {
        this.resourceBag = resourceBag;
    }

    public ResourceConfig getRuntimeConfig() {
        return (ResourceConfig) getConfiguration();
    }

    public Application getApplication() {
        requireNonNull(application, Application.class);
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ApplicationHandler getApplicationHandler() {
        requireNonNull(applicationHandler, ApplicationHandler.class);
        return applicationHandler;
    }

    public void setApplicationHandler(ApplicationHandler applicationHandler) {
        this.applicationHandler = applicationHandler;
    }

    public ProcessingProviders getProcessingProviders() {
        requireNonNull(processingProviders, ProcessingProviders.class);
        return processingProviders;
    }

    public void setProcessingProviders(ProcessingProviders processingProviders) {
        this.processingProviders = processingProviders;
    }

    public MultivaluedParameterExtractorProvider getMultivaluedParameterExtractorProvider() {
        requireNonNull(multivaluedParameterExtractorProvider, MultivaluedParameterExtractorProvider.class);
        return multivaluedParameterExtractorProvider;
    }

    public void setMultivaluedParameterExtractorProvider(MultivaluedParameterExtractorProvider provider) {
        this.multivaluedParameterExtractorProvider = provider;
    }

    public Collection<ValueParamProvider> getValueParamProviders() {
        requireNonNull(valueParamProviders, new GenericType<Collection<ValueParamProvider>>() {}.getType());
        return valueParamProviders;
    }

    public void setValueParamProviders(Collection<ValueParamProvider> valueParamProviders) {
        this.valueParamProviders = valueParamProviders;
    }

    public JerseyResourceContext getResourceContext() {
        requireNonNull(resourceContext, JerseyResourceContext.class);
        return resourceContext;
    }

    public void setResourceContext(JerseyResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    public LazyValue<Collection<ComponentProvider>> getComponentProviders() {
        requireNonNull(componentProviders, new GenericType<LazyValue<Collection<ComponentProvider>>>() {}.getType());
        return componentProviders;
    }

    public void setComponentProviders(LazyValue<Collection<ComponentProvider>> componentProviders) {
        this.componentProviders = componentProviders;
    }

    public ResourceMethodInvoker.Builder getResourceMethodInvokerBuilder() {
        requireNonNull(resourceMethodInvokerBuilder, ResourceMethodInvoker.Builder.class);
        return resourceMethodInvokerBuilder;
    }

    public void setResourceMethodInvokerBuilder(ResourceMethodInvoker.Builder resourceMethodInvokerBuilder) {
        this.resourceMethodInvokerBuilder = resourceMethodInvokerBuilder;
    }

    public ResourceModel getResourceModel() {
        requireNonNull(resourceModel, ResourceModel.class);
        return resourceModel;
    }

    public void setResourceModel(ResourceModel resourceModel) {
        this.resourceModel = resourceModel;
    }
}
