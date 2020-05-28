/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc.freemarker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * {@link org.glassfish.jersey.server.mvc.spi.TemplateProcessor Template processor} providing support for Freemarker templates.
 *
 * @author Pavel Bucek
 * @author Michal Gajdos
 * @author Jeff Wilde (jeff.wilde at complicatedrobot.com)
 */
final class FreemarkerViewProcessor extends AbstractTemplateProcessor<Template> {

    private final FreemarkerConfigurationFactory factory;

    /**
     * Create an instance of this processor with injected {@link jakarta.ws.rs.core.Configuration config} and
     * (optional) {@link jakarta.servlet.ServletContext servlet context}.
     *
     * @param config           config to configure this processor from.
     * @param injectionManager injection manager.
     */
    @Inject
    public FreemarkerViewProcessor(jakarta.ws.rs.core.Configuration config, InjectionManager injectionManager) {
        super(config, injectionManager.getInstance(ServletContext.class), "freemarker", "ftl");

        this.factory = getTemplateObjectFactory(injectionManager::createAndInitialize, FreemarkerConfigurationFactory.class,
                () -> {
                    Configuration configuration =
                            getTemplateObjectFactory(injectionManager::createAndInitialize, Configuration.class, Values.empty());
                    if (configuration == null) {
                        return new FreemarkerDefaultConfigurationFactory(injectionManager.getInstance(ServletContext.class));
                    } else {
                        return new FreemarkerSuppliedConfigurationFactory(configuration);
                    }
                });
    }

    @Override
    protected Template resolve(final String templateReference, final Reader reader) throws Exception {
        return factory.getConfiguration().getTemplate(templateReference);
    }

    @Override
    public void writeTo(final Template template, final Viewable viewable, final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders, final OutputStream out) throws IOException {
        try {
            Object model = viewable.getModel();
            if (!(model instanceof Map)) {
                model = new HashMap<String, Object>() {{
                    put("model", viewable.getModel());
                }};
            }
            Charset encoding = setContentType(mediaType, httpHeaders);

            template.process(model, new OutputStreamWriter(out, encoding));
        } catch (TemplateException te) {
            throw new ContainerException(te);
        }
    }
}
