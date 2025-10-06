/*
 * Copyright (c) 2012, 2025 Oracle and/or its affiliates. All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

import jakarta.inject.Singleton;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.innate.inject.InjectionIds;
import org.glassfish.jersey.innate.inject.InternalBinder;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.ServiceFinderBinder;
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
    private static final Map<EnabledProvidersBinder.Provider, AtomicBoolean> warningMap;

    static {
        warningMap = new HashMap<>();
        for (EnabledProvidersBinder.Provider provider : EnabledProvidersBinder.Provider.values()) {
            warningMap.put(provider, new AtomicBoolean(false));
        }
    }

    /**
     * Prevents instantiation.
     */
    private MessagingBinders() {
    }

    /**
     * Message body providers injection binder.
     */
    public static class MessageBodyProviders extends InternalBinder {

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
            bindSingletonWorker(ByteArrayProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_BYTE_ARRAY_PROVIDER.id() : InjectionIds.SERVER_BYTE_ARRAY_PROVIDER.id());
            // bindSingletonWorker(DataSourceProvider.class);
            bindSingletonWorker(FileProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_FILE_PROVIDER.id() : InjectionIds.SERVER_FILE_PROVIDER.id());
            bindSingletonWorker(PathProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_PATH_PROVIDER.id() : InjectionIds.SERVER_PATH_PROVIDER.id());
            bindSingletonWorker(FormMultivaluedMapProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_MULTIVALUED_MAP_PROVIDER.id() : InjectionIds.SERVER_MULTIVALUED_MAP_PROVIDER.id());
            bindSingletonWorker(FormProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_FORM_PROVIDER.id() : InjectionIds.SERVER_FORM_PROVIDER.id());
            bindSingletonWorker(InputStreamProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_INPUT_STREAM_PROVIDER.id() : InjectionIds.SERVER_INPUT_STREAM_PROVIDER.id());
            bindSingletonWorker(BasicTypesMessageProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_BASIC_TYPES_PROVIDER.id() : InjectionIds.SERVER_BASIC_TYPES_PROVIDER.id());
            bindSingletonWorker(ReaderProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_READER_PROVIDER.id() : InjectionIds.SERVER_READER_PROVIDER.id());
            // bindSingletonWorker(RenderedImageProvider.class); - enabledProvidersBinder
            bindSingletonWorker(StringMessageProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_STRING_MESSAGE_PROVIDER.id() : InjectionIds.SERVER_STRING_MESSAGE_PROVIDER.id());
            bindSingletonWorker(EnumMessageProvider.class, runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_ENUM_MESSAGE_PROVIDER.id() : InjectionIds.SERVER_ENUM_MESSAGE_PROVIDER.id());

            // Message body readers -- enabledProvidersBinder
            // bind(SourceProvider.StreamSourceReader.class).to(MessageBodyReader.class).in(Singleton.class);
            // bind(SourceProvider.SaxSourceReader.class).to(MessageBodyReader.class).in(Singleton.class);
            // bind(SourceProvider.DomSourceReader.class).to(MessageBodyReader.class).in(Singleton.class);
            /*
             * TODO: com.sun.jersey.core.impl.provider.entity.EntityHolderReader
             */

            // Message body writers
            bind(StreamingOutputProvider.class).to(MessageBodyWriter.class).in(Singleton.class)
                    .id(runtimeType == RuntimeType.CLIENT
                            ? InjectionIds.CLIENT_STREAMING_OUTPUT_PROVIDER.id()
                            : InjectionIds.SERVER_STREAMING_OUTPUT_PROVIDER.id());
            // bind(SourceProvider.SourceWriter.class).to(MessageBodyWriter.class).in(Singleton.class); - enabledProvidersBinder

            final EnabledProvidersBinder enabledProvidersBinder = new EnabledProvidersBinder();
            if (applicationProperties != null && applicationProperties.get(CommonProperties.PROVIDER_DEFAULT_DISABLE) != null) {
                enabledProvidersBinder.markDisabled(
                        String.valueOf(applicationProperties.get(CommonProperties.PROVIDER_DEFAULT_DISABLE))
                );
            }
            enabledProvidersBinder.bindToBinder(this, runtimeType);

            // Header Delegate Providers registered in META-INF.services
            install(new ServiceFinderBinder<>(HeaderDelegateProvider.class, applicationProperties, runtimeType));
        }

        private <T extends MessageBodyReader & MessageBodyWriter> void bindSingletonWorker(final Class<T> worker, long id) {
            bind(worker).to(MessageBodyReader.class).to(MessageBodyWriter.class).in(Singleton.class).id(id);
        }
    }

    /**
     * Header delegate provider injection binder.
     */
    public static class HeaderDelegateProviders extends InternalBinder {

        private final Map<HeaderDelegateProvider, Integer> providers;
        private final RuntimeType runtimeType;

        public HeaderDelegateProviders(RuntimeType runtimeType) {
            this.runtimeType = runtimeType;
            Map<HeaderDelegateProvider, Integer> providers = new LinkedHashMap<>();
            providers.put(new CacheControlProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_CACHE_CONTROL_PROVIDER.id() : InjectionIds.SERVER_CACHE_CONTROL_PROVIDER.id());
            providers.put(new CookieProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_COOKIE_PROVIDER.id() : InjectionIds.SERVER_COOKIE_PROVIDER.id());
            providers.put(new DateProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_DATE_PROVIDER.id() : InjectionIds.SERVER_DATE_PROVIDER.id());
            providers.put(new EntityTagProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_ENTITY_TAG_PROVIDER.id() : InjectionIds.SERVER_ENTITY_TAG_PROVIDER.id());
            providers.put(new LinkProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_LINK_PROVIDER.id() : InjectionIds.SERVER_LINK_PROVIDER.id());
            providers.put(new LocaleProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_LOCALE_PROVIDER.id() : InjectionIds.SERVER_LOCALE_PROVIDER.id());
            providers.put(new MediaTypeProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_MEDIA_TYPE_PROVIDER.id() : InjectionIds.SERVER_MEDIA_TYPE_PROVIDER.id());
            providers.put(new NewCookieProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_NEW_COOKIE_PROVIDER.id() : InjectionIds.SERVER_NEW_COOKIE_PROVIDER.id());
            providers.put(new StringHeaderProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_STRING_HEADER_PROVIDER.id() : InjectionIds.SERVER_STRING_HEADER_PROVIDER.id());
            providers.put(new UriProvider(), runtimeType == RuntimeType.CLIENT
                    ? InjectionIds.CLIENT_URI_PROVIDER.id() : InjectionIds.SERVER_URI_PROVIDER.id());
            this.providers = providers;
        }

        @Override
        protected void configure() {
            providers.forEach((provider, id) -> bind(provider).to(HeaderDelegateProvider.class).id(id));
        }

        /**
         * Returns all {@link HeaderDelegateProvider} register internally by Jersey.
         *
         * @return all internally registered {@link HeaderDelegateProvider}.
         */
        public Set<HeaderDelegateProvider> getHeaderDelegateProviders() {
            return providers.keySet();
        }
    }

    private static final class EnabledProvidersBinder {
        private enum Provider {
            DATASOURCE("jakarta.activation.DataSource"),
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

        private void bindToBinder(InternalBinder binder, RuntimeType runtimeType) {
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
                    providerBinder.bind(binder, provider, runtimeType);
                } else {
                    if (warningMap.get(provider).compareAndSet(false, true)) {
                        switch (provider) {
                            case DOMSOURCE:
                            case SAXSOURCE:
                            case STREAMSOURCE:
                                LOGGER.warning(LocalizationMessages.DEPENDENT_CLASS_OF_DEFAULT_PROVIDER_NOT_FOUND(
                                        provider.className, "MessageBodyReader<" + provider.className + ">")
                                );
                                break;
                            case DATASOURCE:
                            case RENDEREDIMAGE:
                            case SOURCE:
                                LOGGER.warning(LocalizationMessages.DEPENDENT_CLASS_OF_DEFAULT_PROVIDER_NOT_FOUND(
                                        provider.className, "MessageBodyWriter<" + provider.className + ">")
                                );
                                break;
                        }
                    }
                }
            }
        }

        private static boolean isClass(String className) {
            return null != AccessController.doPrivileged(ReflectionHelper.classForNamePA(className));
        }

        private interface ProviderBinder {
            void bind(InternalBinder binder, Provider provider, RuntimeType runtimeType);
        }

        private static class DataSourceBinder implements ProviderBinder {
            @Override
            public void bind(InternalBinder binder, Provider provider, RuntimeType runtimeType) {
                binder.bind(DataSourceProvider.class)
                        .to(MessageBodyReader.class).to(MessageBodyWriter.class).in(Singleton.class)
                        .id(runtimeType == RuntimeType.CLIENT
                                ? InjectionIds.CLIENT_DATA_SOURCE_PROVIDER.id()
                                : InjectionIds.SERVER_DATA_SOURCE_PROVIDER.id());
            }
        }

        private static class DomSourceBinder implements ProviderBinder {
            @Override
            public void bind(InternalBinder binder, Provider provider, RuntimeType runtimeType) {
                binder.bind(SourceProvider.DomSourceReader.class).to(MessageBodyReader.class).in(Singleton.class)
                        .id(runtimeType == RuntimeType.CLIENT
                                ? InjectionIds.CLIENT_DOM_SOURCE_READER.id()
                                : InjectionIds.SERVER_DOM_SOURCE_READER.id());
            }
        }

        private static class RenderedImageBinder implements ProviderBinder {
            @Override
            public void bind(InternalBinder binder, Provider provider, RuntimeType runtimeType) {
                binder.bind(RenderedImageProvider.class)
                        .to(MessageBodyReader.class).to(MessageBodyWriter.class).in(Singleton.class)
                        .id(runtimeType == RuntimeType.CLIENT
                                ? InjectionIds.CLIENT_RENDERED_IMAGE_PROVIDER.id()
                                : InjectionIds.SERVER_RENDERED_IMAGE_PROVIDER.id());
            }
        }

        private static class SaxSourceBinder implements ProviderBinder {
            @Override
            public void bind(InternalBinder binder, Provider provider, RuntimeType runtimeType) {
                binder.bind(SourceProvider.SaxSourceReader.class).to(MessageBodyReader.class).in(Singleton.class)
                        .id(runtimeType == RuntimeType.CLIENT
                                ? InjectionIds.CLIENT_SAX_SOURCE_READER.id()
                                : InjectionIds.SERVER_SAX_SOURCE_READER.id());
            }
        }

        private static class SourceBinder implements ProviderBinder {
            @Override
            public void bind(InternalBinder binder, Provider provider, RuntimeType runtimeType) {
                binder.bind(SourceProvider.SourceWriter.class).to(MessageBodyWriter.class).in(Singleton.class)
                        .id(runtimeType == RuntimeType.CLIENT
                                ? InjectionIds.CLIENT_SOURCE_WRITER.id()
                                : InjectionIds.SERVER_SOURCE_WRITER.id());
            }
        }

        private static class StreamSourceBinder implements ProviderBinder {
            @Override
            public void bind(InternalBinder binder, Provider provider, RuntimeType runtimeType) {
                binder.bind(SourceProvider.StreamSourceReader.class).to(MessageBodyReader.class).in(Singleton.class)
                        .id(runtimeType == RuntimeType.CLIENT
                                ? 2075 : 3075);
            }
        }
    }
}
