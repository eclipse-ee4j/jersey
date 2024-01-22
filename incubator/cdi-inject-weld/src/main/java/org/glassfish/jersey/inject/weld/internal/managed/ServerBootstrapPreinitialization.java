/*
 * Copyright (c) 2021, 2024 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.jersey.inject.weld.internal.managed;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.GenericType;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.jersey.inject.weld.spi.BootstrapPreinitialization;
import org.glassfish.jersey.internal.ServiceFinderBinder;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.ClassBinding;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.servlet.WebConfig;
import org.glassfish.jersey.servlet.spi.AsyncContextDelegateProvider;
import org.glassfish.jersey.servlet.spi.FilterUrlMappingsProvider;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;


/**
 * Jersey server side pre-initialization implementation.
 */
// TODO : put to a proper module
public class ServerBootstrapPreinitialization implements BootstrapPreinitialization {


    /**
     * Referencing factory for Grizzly request.
     */
    private static class GrizzlyRequestReferencingFactory extends ReferencingFactory<Request> {

        @Inject
        public GrizzlyRequestReferencingFactory(final Provider<Ref<Request>> referenceFactory) {
            super(referenceFactory);
        }
    }

    /**
     * Referencing factory for Grizzly response.
     */
    private static class GrizzlyResponseReferencingFactory extends ReferencingFactory<Response> {

        @Inject
        public GrizzlyResponseReferencingFactory(final Provider<Ref<Response>> referenceFactory) {
            super(referenceFactory);
        }
    }

    @SuppressWarnings("JavaDoc")
    private static class HttpServletRequestReferencingFactory extends ReferencingFactory<HttpServletRequest> {

        @Inject
        public HttpServletRequestReferencingFactory(final Provider<Ref<HttpServletRequest>> referenceFactory) {
            super(referenceFactory);
        }
    }

    @SuppressWarnings("JavaDoc")
    private static class HttpServletResponseReferencingFactory extends ReferencingFactory<HttpServletResponse> {

        @Inject
        public HttpServletResponseReferencingFactory(final Provider<Ref<HttpServletResponse>> referenceFactory) {
            super(referenceFactory);
        }
    }

    private static class WebConfigInitializer implements WebConfig {

        @Override
        public ConfigType getConfigType() {
            return ConfigType.ServletConfig;
        }

        @Override
        public ServletConfig getServletConfig() {
            return new ServletConfig() {
                @Override
                public String getServletName() {
                    return "Preinit";
                }

                @Override
                public ServletContext getServletContext() {
                    return WebConfigInitializer.this.getServletContext();
                }

                @Override
                public String getInitParameter(String name) {
                    return null;
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    return null;
                }
            };
        }

        @Override
        public FilterConfig getFilterConfig() {
            return null;
        }

        @Override
        public String getName() {
            return "Preinit";
        }

        @Override
        public String getInitParameter(String name) {
            return getName();
        }

        @Override
        public Enumeration getInitParameterNames() {
            return null;
        }

        @Override
        public ServletContext getServletContext() {
            return new ServletContext() {
                @Override
                public String getContextPath() {
                    return null;
                }

                @Override
                public ServletContext getContext(String uripath) {
                    return WebConfigInitializer.this.getServletContext();
                }

                @Override
                public int getMajorVersion() {
                    return 0;
                }

                @Override
                public int getMinorVersion() {
                    return 0;
                }

                @Override
                public int getEffectiveMajorVersion() {
                    return 0;
                }

                @Override
                public int getEffectiveMinorVersion() {
                    return 0;
                }

                @Override
                public String getMimeType(String file) {
                    return null;
                }

                @Override
                public Set<String> getResourcePaths(String path) {
                    return null;
                }

                @Override
                public URL getResource(String path) throws MalformedURLException {
                    return null;
                }

                @Override
                public InputStream getResourceAsStream(String path) {
                    return null;
                }

                @Override
                public RequestDispatcher getRequestDispatcher(String path) {
                    return null;
                }

                @Override
                public RequestDispatcher getNamedDispatcher(String name) {
                    return null;
                }

                @Override
                public void log(String msg) {

                }

                @Override
                public void log(String message, Throwable throwable) {

                }

                @Override
                public String getRealPath(String path) {
                    return null;
                }

                @Override
                public String getServerInfo() {
                    return null;
                }

                @Override
                public String getInitParameter(String name) {
                    return null;
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    return null;
                }

                @Override
                public boolean setInitParameter(String name, String value) {
                    return false;
                }

                @Override
                public Object getAttribute(String name) {
                    return null;
                }

                @Override
                public Enumeration<String> getAttributeNames() {
                    return null;
                }

                @Override
                public void setAttribute(String name, Object object) {

                }

                @Override
                public void removeAttribute(String name) {

                }

                @Override
                public String getServletContextName() {
                    return null;
                }

                @Override
                public ServletRegistration.Dynamic addServlet(String servletName, String className) {
                    return null;
                }

                @Override
                public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
                    return null;
                }

                @Override
                public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
                    return null;
                }

                @Override
                public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
                    return null;
                }

                @Override
                public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
                    return null;
                }

                @Override
                public ServletRegistration getServletRegistration(String servletName) {
                    return null;
                }

                @Override
                public Map<String, ? extends ServletRegistration> getServletRegistrations() {
                    return null;
                }

                @Override
                public FilterRegistration.Dynamic addFilter(String filterName, String className) {
                    return null;
                }

                @Override
                public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
                    return null;
                }

                @Override
                public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
                    return null;
                }

                @Override
                public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
                    return null;
                }

                @Override
                public FilterRegistration getFilterRegistration(String filterName) {
                    return null;
                }

                @Override
                public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
                    return null;
                }

                @Override
                public SessionCookieConfig getSessionCookieConfig() {
                    return null;
                }

                @Override
                public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

                }

                @Override
                public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
                    return null;
                }

                @Override
                public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
                    return null;
                }

                @Override
                public void addListener(String className) {

                }

                @Override
                public <T extends EventListener> void addListener(T t) {

                }

                @Override
                public void addListener(Class<? extends EventListener> listenerClass) {

                }

                @Override
                public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
                    return null;
                }

                @Override
                public JspConfigDescriptor getJspConfigDescriptor() {
                    return null;
                }

                @Override
                public ClassLoader getClassLoader() {
                    return null;
                }

                @Override
                public void declareRoles(String... roleNames) {

                }

                @Override
                public String getVirtualServerName() {
                    return null;
                }

                @Override
                public int getSessionTimeout() {
                    return 0;
                }

                @Override
                public void setSessionTimeout(int sessionTimeout) {

                }

                @Override
                public String getRequestCharacterEncoding() {
                    return null;
                }

                @Override
                public void setRequestCharacterEncoding(String encoding) {

                }

                @Override
                public String getResponseCharacterEncoding() {
                    return null;
                }

                @Override
                public void setResponseCharacterEncoding(String encoding) {

                }
            };
        }
    }

    private static class PreinitializationFeatureContext implements FeatureContext {

        private final AbstractBinder binder;

        private PreinitializationFeatureContext(AbstractBinder binder) {
            this.binder = binder;
        }

        @Override
        public Configuration getConfiguration() {
            return new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        }

        @Override
        public FeatureContext property(String name, Object value) {
            return this;
        }

        @Override
        public FeatureContext register(Class<?> componentClass) {
            binder.bindAsContract(componentClass);
            return this;
        }

        @Override
        public FeatureContext register(Class<?> componentClass, int priority) {
            binder.bindAsContract(componentClass).ranked(priority);
            return this;
        }

        @Override
        public FeatureContext register(Class<?> componentClass, Class<?>... contracts) {
            final ClassBinding binding = binder.bind(componentClass);
            if (contracts != null) {
                for (Class<?> contract : contracts) {
                    binding.to(contract);
                }
            }
            return this;
        }

        @Override
        public FeatureContext register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
            for (Map.Entry<Class<?>, Integer> contract : contracts.entrySet()) {
                final AbstractBinder abstractBinder = new AbstractBinder() {
                    @Override
                    protected void configure() {
                    }
                };
                final ClassBinding binding = abstractBinder.bind(componentClass);
                binding.to(contract.getKey()).ranked(contract.getValue());
                binder.install(abstractBinder);
            }
            return this;
        }

        @Override
        public FeatureContext register(Object component) {
            if (AbstractBinder.class.isInstance(component)) {
                binder.install((AbstractBinder) component);
            } else {
                binder.bind(component).to(component.getClass());
            }
            return this;
        }

        @Override
        public FeatureContext register(Object component, int priority) {
            binder.bind(component).to(component.getClass()).ranked(priority);
            return this;
        }

        @Override
        public FeatureContext register(Object component, Class<?>... contracts) {
            Binding binding = binder.bind(component);
            if (contracts != null) {
                for (Class<?> contract : contracts) {
                    binding.to(contract);
                }
            }
            return this;
        }

        @Override
        public FeatureContext register(Object component, Map<Class<?>, Integer> contracts) {
            for (Map.Entry<Class<?>, Integer> contract : contracts.entrySet()) {
                final AbstractBinder abstractBinder = new AbstractBinder() {
                    @Override
                    protected void configure() {
                    }
                };
                final Binding binding = abstractBinder.bind(component);
                binding.to(contract.getKey()).ranked(contract.getValue());
                binder.install(abstractBinder);
            }
            return this;
        }
    }

    @Override
    public void register(RuntimeType runtimeType, AbstractBinder binder) {
//        binder.install(new MessagingBinders.MessageBodyProviders(null, RuntimeType.SERVER),
//                new MessagingBinders.HeaderDelegateProviders());
//
//        // Server Binder
//        binder.install(new MappableExceptionWrapperInterceptor.Binder(),
//                new MonitoringContainerListener.Binder());
//        binder.bind(ChunkedResponseWriter.class).to(MessageBodyWriter.class).in(Singleton.class);
//        binder.bind(JsonWithPaddingInterceptor.class).to(WriterInterceptor.class).in(Singleton.class);

        if (runtimeType == null /*RuntimeType.SERVER*/) {
            // new ApplicationHandler(new ResourceConfig());

            //grizzly
            binder.bindFactory(GrizzlyRequestReferencingFactory.class).to(Request.class)
                    .proxy(false).in(RequestScoped.class);
            binder.bindFactory(ReferencingFactory.<Request>referenceFactory()).to(new GenericType<Ref<Request>>() {})
                    .in(RequestScoped.class);

            binder.bindFactory(GrizzlyResponseReferencingFactory.class).to(Response.class)
                    .proxy(true).proxyForSameScope(false).in(RequestScoped.class);
            binder.bindFactory(ReferencingFactory.<Response>referenceFactory()).to(new GenericType<Ref<Response>>() {})
                    .in(RequestScoped.class);

            // servlet
            binder.bindFactory(HttpServletRequestReferencingFactory.class).to(HttpServletRequest.class)
                    .proxy(true).proxyForSameScope(false).in(RequestScoped.class);

            binder.bindFactory(ReferencingFactory.referenceFactory())
                    .to(new GenericType<Ref<HttpServletRequest>>() {}).in(RequestScoped.class);

            binder.bindFactory(HttpServletResponseReferencingFactory.class).to(HttpServletResponse.class)
                    .proxy(true).proxyForSameScope(false).in(RequestScoped.class);
            binder.bindFactory(ReferencingFactory.referenceFactory())
                    .to(new GenericType<Ref<HttpServletResponse>>() {}).in(RequestScoped.class);

            final WebConfig webConfig = new WebConfigInitializer();
            final Map<String, Object> applicationProperties = Collections.EMPTY_MAP;

            binder.bindFactory(() -> webConfig.getServletContext()).to(ServletContext.class).in(Singleton.class);
            binder.bindFactory(() -> webConfig).to(WebConfig.class).in(Singleton.class);
            binder.install(
                    new ServiceFinderBinder<>(AsyncContextDelegateProvider.class, applicationProperties, RuntimeType.SERVER));
            binder.install(
                    new ServiceFinderBinder<>(FilterUrlMappingsProvider.class, applicationProperties, RuntimeType.SERVER));

            final ServletConfig servletConfig = webConfig.getServletConfig();
            binder.bindFactory(() -> servletConfig).to(ServletConfig.class).in(Singleton.class);

//            // WADL TODO put to a proper module
//            try {
//                new WadlFeature().configure(new PreinitializationFeatureContext(binder) {
//                    @Override
//                    public FeatureContext register(Class<?> componentClass) {
//                        if (WadlModelProcessor.class.isAssignableFrom(componentClass)) {
//                            super.register(WadlModelProcessor.OptionsHandler.class);
//                        }
//                        super.register(componentClass);
//                        return this;
//                    }
//                });
//            } catch (Exception e) {
//
//            }
//            try {
//                Class[] classes = OptionsMethodProcessor.class.getDeclaredClasses();
//                for (Class clz : classes) {
//                    binder.bindAsContract(clz);
//                }
//            } catch (Exception e) {
//
//            }
        }

//
//        //ApplicationConfigurator
//        binder.bind(new InitializableInstanceBinding((Application) null).to(Application.class));
    }
}
