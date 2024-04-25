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

import jakarta.ws.rs.core.Configuration;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Map;

/**
 * Handy {@link ThymeleafConfigurationFactory} that supplies a minimally
 * configured {@link org.thymeleaf.TemplateEngine } able to
 * render Thymeleaf templates.
 * The recommended method to provide custom Thymeleaf engine settings is to
 * sub-class this class, further customize the
 * {@link org.thymeleaf.TemplateEngine settings} as desired in that
 * class, and then register the sub-class with the {@link ThymeleafMvcFeature}
 * TEMPLATE_OBJECT_FACTORY property.
 *
 * @author Dmytro Dovnar (dimonmc@gmail.com)
 */
public class ThymeleafDefaultConfigurationFactory implements ThymeleafConfigurationFactory {
    private final Configuration config;
    private final TemplateEngine templateEngine;

    public ThymeleafDefaultConfigurationFactory(Configuration config) {
        this.config = config;
        this.templateEngine = initTemplateEngine();
    }

    @Override
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    private ITemplateResolver getTemplateResolver() {
        Map<String, Object> properties = config.getProperties();
        String basePath = (String) PropertiesHelper.getValue(properties,
                "jersey.config.server.mvc.templateBasePath" + ThymeleafMvcFeature.SUFFIX,
                String.class, (Map) null);
        if (basePath == null) {
            basePath = (String) PropertiesHelper.getValue(properties,
                    "jersey.config.server.mvc.templateBasePath", "", (Map) null);
        }

        if (basePath != null && !basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }

        String templateFileSuffix = (String) PropertiesHelper.getValue(properties,
                "jersey.config.server.mvc.templateFileSuffix" + ThymeleafMvcFeature.SUFFIX,
                ".html", (Map) null);

        String templateFileMode = (String) PropertiesHelper.getValue(properties,
                "jersey.config.server.mvc.templateMode" + ThymeleafMvcFeature.SUFFIX,
                "HTML5", (Map) null);

        Boolean cacheEnabled = (Boolean) PropertiesHelper.getValue(properties,
                "jersey.config.server.mvc.caching" + ThymeleafMvcFeature.SUFFIX, Boolean.class, (Map) null);
        if (cacheEnabled == null) {
            cacheEnabled = (Boolean) PropertiesHelper.getValue(properties,
                    "jersey.config.server.mvc.caching", false, (Map) null);
        }

        Long cacheLiveMs = (Long) PropertiesHelper.getValue(properties,
                "jersey.config.server.mvc.cacheTTLMs" + ThymeleafMvcFeature.SUFFIX, 3600000L, (Map) null);

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix(basePath);
        templateResolver.setSuffix(templateFileSuffix);
        templateResolver.setTemplateMode(templateFileMode);
        templateResolver.setCacheTTLMs(cacheLiveMs);
        templateResolver.setCacheable(cacheEnabled);
        return templateResolver;
    }

    private TemplateEngine initTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(getTemplateResolver());
        return templateEngine;
    }

    private IMessageResolver getMessageResolver() {
        StandardMessageResolver messageResolver = new StandardMessageResolver();
        return messageResolver;
    }
}
