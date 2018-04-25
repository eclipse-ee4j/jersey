/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.internal.util.collection.NullableMultivaluedHashMap;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ParamException;
import org.glassfish.jersey.server.internal.InternalServerProperties;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.Parameter;

/**
 * Value factory provider supporting the {@link FormParam} injection annotation.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
final class FormParamValueParamProvider extends AbstractValueParamProvider {

    /**
     * Injection constructor.
     *
     * @param mpep extractor provider.
     */
    public FormParamValueParamProvider(Provider<MultivaluedParameterExtractorProvider> mpep) {
        super(mpep, Parameter.Source.FORM);
    }

    @Override
    public Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        String parameterName = parameter.getSourceName();

        if (parameterName == null || parameterName.isEmpty()) {
            // Invalid query parameter name
            return null;
        }

        MultivaluedParameterExtractor e = get(parameter);
        if (e == null) {
            return null;
        }
        return new FormParamValueProvider(e, !parameter.isEncoded());
    }

    private static final class FormParamValueProvider implements Function<ContainerRequest, Object> {

        private static final Annotation encodedAnnotation = getEncodedAnnotation();
        private final MultivaluedParameterExtractor<?> extractor;
        private final boolean decode;

        FormParamValueProvider(MultivaluedParameterExtractor<?> extractor, boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }

        private static Form getCachedForm(final ContainerRequest request, boolean decode) {
            return (Form) request.getProperty(decode ? InternalServerProperties
                    .FORM_DECODED_PROPERTY : InternalServerProperties.FORM_PROPERTY);
        }

        private static ContainerRequest ensureValidRequest(final ContainerRequest request) throws IllegalStateException {
            if (request.getMethod().equals("GET")) {
                throw new IllegalStateException(
                        LocalizationMessages.FORM_PARAM_METHOD_ERROR());
            }

            if (!MediaTypes.typeEqual(MediaType.APPLICATION_FORM_URLENCODED_TYPE, request.getMediaType())) {
                throw new IllegalStateException(
                        LocalizationMessages.FORM_PARAM_CONTENT_TYPE_ERROR());
            }
            return request;
        }

        private static Annotation getEncodedAnnotation() {
            /**
             * Encoded-annotated class.
             */
            @Encoded
            final class EncodedAnnotationTemp {
            }
            return EncodedAnnotationTemp.class.getAnnotation(Encoded.class);
        }

        @Override
        public Object apply(ContainerRequest request) {
            Form form = getCachedForm(request, decode);

            if (form == null) {
                Form otherForm = getCachedForm(request, !decode);
                if (otherForm != null) {
                    form = switchUrlEncoding(request, otherForm);
                    cacheForm(request, form);
                } else {
                    form = getForm(request);
                    cacheForm(request, form);
                }
            }

            try {
                return extractor.extract(form.asMap());
            } catch (ExtractorException e) {
                throw new ParamException.FormParamException(e.getCause(),
                        extractor.getName(), extractor.getDefaultValueString());
            }
        }

        private Form switchUrlEncoding(final ContainerRequest request, final Form otherForm) {
            final Set<Map.Entry<String, List<String>>> entries = otherForm.asMap().entrySet();

            MultivaluedMap<String, String> formMap = new NullableMultivaluedHashMap<>();
            for (Map.Entry<String, List<String>> entry : entries) {
                final String charsetName = ReaderWriter.getCharset(MediaType.valueOf(
                        request.getHeaderString(HttpHeaders.CONTENT_TYPE))).name();

                String key;
                try {
                    key = decode ? URLDecoder.decode(entry.getKey(), charsetName) : URLEncoder.encode(entry.getKey(),
                            charsetName);

                    for (String value : entry.getValue()) {
                        if (value != null) {
                            formMap.add(key,
                                    decode ? URLDecoder.decode(value, charsetName) : URLEncoder.encode(value, charsetName));
                        } else {
                            formMap.add(key, null);
                        }
                    }

                } catch (UnsupportedEncodingException uee) {
                    throw new ProcessingException(LocalizationMessages.ERROR_UNSUPPORTED_ENCODING(charsetName,
                            extractor.getName()), uee);
                }
            }
            return new Form(formMap);
        }

        private void cacheForm(final ContainerRequest request, final Form form) {
            request.setProperty(decode ? InternalServerProperties
                    .FORM_DECODED_PROPERTY : InternalServerProperties.FORM_PROPERTY, form);
        }

        private Form getForm(final ContainerRequest request) {
            return getFormParameters(ensureValidRequest(request));
        }

        private Form getFormParameters(ContainerRequest request) {
            if (MediaTypes.typeEqual(MediaType.APPLICATION_FORM_URLENCODED_TYPE, request.getMediaType())) {
                request.bufferEntity();
                Form form;
                if (decode) {
                    form = request.readEntity(Form.class);
                } else {
                    Annotation[] annotations = new Annotation[1];
                    annotations[0] = encodedAnnotation;
                    form = request.readEntity(Form.class, annotations);
                }

                return (form == null ? new Form() : form);
            } else {
                return new Form();
            }
        }
    }
}
