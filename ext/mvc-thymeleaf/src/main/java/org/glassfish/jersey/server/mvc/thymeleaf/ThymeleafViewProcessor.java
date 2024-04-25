/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc.thymeleaf;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

/**
 * {@link org.glassfish.jersey.server.mvc.spi.TemplateProcessor Template processor} providing support for Thymeleaf templates.
 *
 * @author Dmytro Dovnar (dimonmc@gmail.com)
 */
public final class ThymeleafViewProcessor extends AbstractTemplateProcessor<TemplateEngine> {
    private final ThymeleafConfigurationFactory factory;

    /**
     * Create an instance of this processor with injected {@link jakarta.ws.rs.core.Configuration config}.
     *
     * @param config           config to configure this processor from.
     * @param injectionManager injection manager.
     */
    @Inject
    public ThymeleafViewProcessor(Configuration config, InjectionManager injectionManager) {
        super(config, injectionManager.getInstance(ServletContext.class), "thymeleaf", "html");
        this.factory = getTemplateObjectFactory(injectionManager::createAndInitialize, ThymeleafConfigurationFactory.class,
                () -> {
                    ThymeleafConfigurationFactory configuration =
                            getTemplateObjectFactory(
                                    injectionManager::createAndInitialize,
                                    ThymeleafConfigurationFactory.class, Values.empty());
                    if (configuration == null) {
                        return new ThymeleafDefaultConfigurationFactory(config);
                    } else {
                        return new ThymeleafSuppliedConfigurationFactory(configuration);
                    }
                });
    }

    @Override
    protected TemplateEngine resolve(final String templatePath, final Reader reader) throws Exception {
        return factory.getTemplateEngine();
    }

    @Override
    public void writeTo(final TemplateEngine templateEngine, final Viewable viewable, final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders, final OutputStream out) throws IOException {
        Context context = new Context();

        Object model = viewable.getModel();
        if (!(model instanceof Map)) {
            context.setVariable("model", viewable.getModel());
        } else {
            context.setVariables((Map) viewable.getModel());
        }

        if (context.containsVariable("lang")) {
            Object langValue = context.getVariable("lang");
            if (langValue instanceof Locale) {
                context.setLocale((Locale) langValue);
            } else if (langValue instanceof String) {
                Locale locale = Locale.forLanguageTag((String) langValue);
                context.setLocale(locale);
            }
        }

        Charset encoding = setContentType(mediaType, httpHeaders);

        final Writer writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
        templateEngine.process(viewable.getTemplateName(), context, writer);
    }
}
