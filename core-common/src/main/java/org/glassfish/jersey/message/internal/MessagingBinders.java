/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.internal;

import java.security.AccessController;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import javax.inject.Singleton;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.ServiceFinderBinder;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.Tokenizer;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

/**
 * Binding definitions for the default set of message related providers (readers,
 * writers, header delegates).
 *
 * @author Marek Potociar
 * @author Libor Kramolis
 */
public final class MessagingBinders {

    private static final Logger LOGGER = Logger.getLogger(MessagingBinders.class.getName());

    /**
     * Prevents instantiation.
     */
    private MessagingBinders() {
    }

    /**
     * Message body providers injection binder.
     */
    public static class MessageBodyProviders extends AbstractBinder {

        private final Map<String, Object> applicationProperties;

        private final RuntimeType runtimeType;

        /**
         * Create new message body providers injection binder.
         *
         * @param applicationProperties map containing application properties. May be {@code null}.
         * @param runtimeType           runtime (client or server) where the binder is used.
         */
        public MessageBodyProviders(final Map<String, Object> applicationProperties, final RuntimeType runtimeType) {
            this.applicationProperties = applicationProperties;
            this.runtimeType = runtimeType;
        }

        @Override
        protected void configure() {

            // Message body providers (both readers & writers)
            bindSingletonWorker(ByteArrayProvider.class);
            // bindSingletonWorker(DataSourceProvider.class);
            bindSingletonWorker(FileProvider.class);
            bindSingletonWorker(FormMultivaluedMapProvider.class);
            bindSingletonWorker(FormProvider.class);
            bindSingletonWorker(InputStreamProvider.class);
            bindSingletonWorker(BasicTypesMessageProvider.class);
            bindSingletonWorker(ReaderProvider.class);
            // bindSingletonWorker(RenderedImageProvider.class); - enabledProvidersBinder
            bindSingletonWorker(StringMessageProvider.class);
            bindSingletonWorker(EnumMessageProvider.class);

            // Message body readers -- enabledProvidersBinder
            // bind(SourceProvider.StreamSourceReader.class).to(MessageBodyReader.class).in(Singleton.class);
            // bind(SourceProvider.SaxSourceReader.class).to(MessageBodyReader.class).in(Singleton.class);
            // bind(SourceProvider.DomSourceReader.class).to(MessageBodyReader.class).in(Singleton.class);
            /*
             * TODO: com.sun.jersey.core.impl.provider.entity.EntityHolderReader
             */

            // Message body writers
            bind(StreamingOutputProvider.class).to(MessageBodyWriter.class).in(Singleton.class);
            // bind(SourceProvider.SourceWriter.class).to(MessageBodyWriter.class).in(Singleton.class); - enabledProvidersBinder

            final EnabledProvidersBinder enabledProvidersBinder = new EnabledProvidersBinder();
            if (applicationProperties != null && applicationProperties.get(CommonProperties.PROVIDER_DEFAULT_DISABLE) != null) {
                enabledProvidersBinder.markDisabled(
                        String.valueOf(applicationProperties.get(CommonProperties.PROVIDER_DEFAULT_DISABLE))
                );
            }
            enabledProvidersBinder.bindToBinder(this);

            // Header Delegate Providers registered in META-INF.services
            install(new ServiceFinderBinder<>(HeaderDelegateProvider.class, applicationProperties, runtimeType));
        }

        private <T extends MessageBodyReader & MessageBodyWriter> void bindSingletonWorker(final Class<T> worker) {
            bind(worker).to(MessageBodyReader.class).to(MessageBodyWriter.class).in(Singleton.class);
        }
    }

    /**
     * Header delegate provider injection binder.
     */
    public static class HeaderDelegateProviders extends AbstractBinder {

        private final Set<HeaderDelegateProvider> providers;

        public HeaderDelegateProviders() {
            Set<HeaderDelegateProvider> providers = new HashSet<>();
            providers.add(new CacheControlProvider());
            providers.add(new CookieProvider());
            providers.add(new DateProvider());
            providers.add(new EntityTagProvider());
            providers.add(new LinkProvider());
            providers.add(new LocaleProvider());
            providers.add(new MediaTypeProvider());
            providers.add(new NewCookieProvider());
            providers.add(new StringHeaderProvider());
            providers.add(new UriProvider());
            this.providers = providers;
        }

        @Override
        protected void configure() {
            providers.forEach(provider -> bind(provider).to(HeaderDelegateProvider.class));
        }

        /**
         * Returns all {@link HeaderDelegateProvider} register internally by Jersey.
         *
         * @return all internally registered {@link HeaderDelegateProvider}.
         */
        public Set<HeaderDelegateProvider> getHeaderDelegateProviders() {
            return providers;
        }
    }

    private static final class EnabledProvidersBinder {
        private enum Provider {
            DATASOURCE("javax.activation.DataSource"),
            DOMSOURCE("javax.xml.transform.dom.DOMSource"),
            RENDEREDIMAGE("java.awt.image.RenderedImage"),
            SAXSOURCE("javax.xml.transform.sax.SAXSource"),
            SOURCE("javax.xml.transform.Source"),
            STREAMSOURCE("javax.xml.transform.stream.StreamSource");
            Provider(String className) {
                this.className = className;
            }
            private String className;
        }

        private static final String ALL = "ALL";
        private HashSet<Provider> enabledProviders = new HashSet<>();

        private EnabledProvidersBinder() {
            for (Provider provider : Provider.values()) {
                enabledProviders.add(provider);
            }
        }

        private void markDisabled(String properties) {
            String[] tokens = Tokenizer.tokenize(properties);
            for (int tokenIndex = 0; tokenIndex != tokens.length; tokenIndex++) {
                String token = tokens[tokenIndex].toUpperCase(Locale.ROOT);
                if (ALL.equals(token)) {
                    enabledProviders.clear();
                    return;
                }
                for (Iterator<Provider> iterator = enabledProviders.iterator(); iterator.hasNext();) {
                    Provider provider = iterator.next();
                    if (provider.name().equals(token)) {
                        iterator.remove();
                    }
                }
            }
        }

        private void bindToBinder(AbstractBinder binder) {
            ProviderBinder providerBinder = null;
            for (Provider provider : enabledProviders) {
                if (isClass(provider.className)) {
                    switch (provider) {
                        case DATASOURCE:
                            providerBinder = new DataSourceBinder();
                            break;
                        case DOMSOURCE:
                            providerBinder = new DomSourceBinder();
                            break;
                        case RENDEREDIMAGE:
                            providerBinder = new RenderedImageBinder();
                            break;
                        case SAXSOURCE:
                            providerBinder = new SaxSourceBinder();
                            break;
                        case SOURCE:
                            providerBinder = new SourceBinder();
                            break;
                        case STREAMSOURCE:
                            providerBinder = new StreamSourceBinder();
                            break;
                    }
                    providerBinder.bind(binder, provider);
                } else {
                    switch (provider) {
                        case DOMSOURCE:
                        case SAXSOURCE:
                        case STREAMSOURCE:
                            LOGGER.warning(LocalizationMessages.DEPENDENT_CLASS_OF_DEFAULT_PROVIDER_NOT_FOUND(provider.className,
                                    "MessageBodyReader<" + provider.className + ">")
                            );
                            break;
                        case DATASOURCE:
                        case RENDEREDIMAGE:
                        case SOURCE:
                            LOGGER.warning(LocalizationMessages.DEPENDENT_CLASS_OF_DEFAULT_PROVIDER_NOT_FOUND(provider.className,
                                    "MessageBodyWriter<" + provider.className + ">")
                            );
                            break;
                    }
                }
            }
        }

        private static boolean isClass(String className) {
            return null != AccessController.doPrivileged(ReflectionHelper.classForNamePA(className));
        }

        private interface ProviderBinder {
            void bind(AbstractBinder binder, Provider provider);
        }

        private static class DataSourceBinder implements ProviderBinder {
            @Override
            public void bind(AbstractBinder binder, Provider provider) {
                binder.bind(DataSourceProvider.class)
                        .to(MessageBodyReader.class).to(MessageBodyWriter.class).in(Singleton.class);
            }
        }

        private static class DomSourceBinder implements ProviderBinder {
            @Override
            public void bind(AbstractBinder binder, Provider provider) {
                binder.bind(SourceProvider.DomSourceReader.class).to(MessageBodyReader.class).in(Singleton.class);
            }
        }

        private static class RenderedImageBinder implements ProviderBinder {
            @Override
            public void bind(AbstractBinder binder, Provider provider) {
                binder.bind(RenderedImageProvider.class)
                        .to(MessageBodyReader.class).to(MessageBodyWriter.class).in(Singleton.class);
            }
        }

        private static class SaxSourceBinder implements ProviderBinder {
            @Override
            public void bind(AbstractBinder binder, Provider provider) {
                binder.bind(SourceProvider.SaxSourceReader.class).to(MessageBodyReader.class).in(Singleton.class);
            }
        }

        private static class SourceBinder implements ProviderBinder {
            @Override
            public void bind(AbstractBinder binder, Provider provider) {
                binder.bind(SourceProvider.SourceWriter.class).to(MessageBodyWriter.class).in(Singleton.class);
            }
        }

        private static class StreamSourceBinder implements ProviderBinder {
            @Override
            public void bind(AbstractBinder binder, Provider provider) {
                binder.bind(SourceProvider.StreamSourceReader.class).to(MessageBodyReader.class).in(Singleton.class);
            }
        }
    }
}
