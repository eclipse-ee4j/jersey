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

package org.glassfish.jersey.server.mvc.freemarker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;

/**
 * Handy {@link FreemarkerConfigurationFactory} that supplies a minimally
 * configured {@link freemarker.template.Configuration Configuration} able to
 * create {@link freemarker.template.Template Freemarker templates}.
 * The recommended method to provide custom Freemarker configuration is to
 * sub-class this class, further customize the
 * {@link freemarker.template.Configuration configuration} as desired in that
 * class, and then register the sub-class with the {@link FreemarkerMvcFeature}
 * TEMPLATE_OBJECT_FACTORY property.
 *
 * @author Jeff Wilde (jeff.wilde at complicatedrobot.com)
 */
public class FreemarkerDefaultConfigurationFactory implements FreemarkerConfigurationFactory {

    protected final Configuration configuration;

    public FreemarkerDefaultConfigurationFactory(ServletContext servletContext) {
        super();

        // Create different loaders.
        final List<TemplateLoader> loaders = new ArrayList<>();
        if (servletContext != null) {
            loaders.add(new WebappTemplateLoader(servletContext));
        }
        loaders.add(new ClassTemplateLoader(FreemarkerDefaultConfigurationFactory.class, "/"));
        try {
            loaders.add(new FileTemplateLoader(new File("/")));
        } catch (IOException e) {
            // NOOP
        }

        // Create Base configuration.
        configuration = new Configuration();
        configuration.setTemplateLoader(new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()])));

    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

}
