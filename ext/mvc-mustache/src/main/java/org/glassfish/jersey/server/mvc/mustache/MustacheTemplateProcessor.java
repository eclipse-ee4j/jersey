/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc.mustache;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * {@link TemplateProcessor Template processor} providing support for Mustache templates.
 *
 * @author Michal Gajdos
 * @see MustacheMvcFeature
 * @since 2.3
 */
@Singleton
final class MustacheTemplateProcessor extends AbstractTemplateProcessor<Mustache> {

    private final MustacheFactory factory;

    /**
     * Create an instance of this processor with injected {@link Configuration config} and (nullable)
     * {@link ServletContext servlet context}.
     *
     * @param config           configuration to configure this processor from.
     * @param injectionManager injection manager.
     */
    @Inject
    public MustacheTemplateProcessor(Configuration config, InjectionManager injectionManager) {
        super(config, injectionManager.getInstance(ServletContext.class), "mustache", "mustache");

        this.factory = getTemplateObjectFactory(injectionManager::createAndInitialize, MustacheFactory.class,
                DefaultMustacheFactory::new);
    }

    @Override
    protected Mustache resolve(final String templatePath, final Reader reader) {
        return factory.compile(reader, templatePath);
    }

    @Override
    public void writeTo(final Mustache mustache, final Viewable viewable, final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders, final OutputStream out) throws IOException {
        Charset encoding = setContentType(mediaType, httpHeaders);
        mustache.execute(new OutputStreamWriter(out, encoding), viewable.getModel()).flush();
    }
}
