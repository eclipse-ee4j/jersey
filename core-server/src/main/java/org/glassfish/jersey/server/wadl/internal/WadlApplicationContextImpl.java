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

package org.glassfish.jersey.server.wadl.internal;

import java.net.URI;
import java.security.AccessController;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.server.ExtendedResourceContext;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.wadl.WadlApplicationContext;
import org.glassfish.jersey.server.wadl.WadlGenerator;
import org.glassfish.jersey.server.wadl.config.WadlGeneratorConfig;
import org.glassfish.jersey.server.wadl.config.WadlGeneratorConfigLoader;

import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Doc;
import com.sun.research.ws.wadl.Grammars;
import com.sun.research.ws.wadl.Include;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;

/**
 * WADL application context implementation.
 *
 * @author Paul Sandoz
 */
public final class WadlApplicationContextImpl implements WadlApplicationContext {

    private static final Logger LOGGER = Logger.getLogger(WadlApplicationContextImpl.class.getName());

    /**
     * Jersey WADL extension XML namespace.
     */
    static final String WADL_JERSEY_NAMESPACE = "http://jersey.java.net/";
    /**
     * Jersey WADL extension XML element.
     */
    static final JAXBElement EXTENDED_ELEMENT =
            new JAXBElement<>(new QName(WADL_JERSEY_NAMESPACE, "extended", "jersey"), String.class, "true");

    private final ExtendedResourceContext resourceContext;
    private final InjectionManager injectionManager;
    private final WadlGeneratorConfig wadlGeneratorConfig;
    private final JAXBContext jaxbContext;

    private volatile boolean wadlGenerationEnabled = true;

    /**
     * Injection constructor.
     *
     * @param injectionManager injection manager.
     * @param configuration    runtime application configuration.
     * @param resourceContext  extended resource context.
     */
    @Inject
    public WadlApplicationContextImpl(
            final InjectionManager injectionManager,
            final Configuration configuration,
            final ExtendedResourceContext resourceContext) {
        this.injectionManager = injectionManager;
        this.wadlGeneratorConfig = WadlGeneratorConfigLoader.loadWadlGeneratorsFromConfig(configuration.getProperties());
        this.resourceContext = resourceContext;

        // TODO perhaps this should be done another way for the moment
        // create a temporary generator just to do this one task
        final WadlGenerator wadlGenerator = wadlGeneratorConfig.createWadlGenerator(injectionManager);

        JAXBContext jaxbContextCandidate;

        final ClassLoader contextClassLoader = AccessController.doPrivileged(ReflectionHelper.getContextClassLoaderPA());
        try {
            // Nasty ClassLoader magic. JAXB-API has some strange limitation about what class loader can
            // be used in OSGi environment - it must be same as context ClassLoader. Following code just
            // workarounds this limitation
            // see JERSEY-1818
            // see JSR222-46

            final ClassLoader jerseyModuleClassLoader =
                    AccessController.doPrivileged(ReflectionHelper.getClassLoaderPA(wadlGenerator.getClass()));

            AccessController.doPrivileged(ReflectionHelper.setContextClassLoaderPA(jerseyModuleClassLoader));

            jaxbContextCandidate = JAXBContext.newInstance(wadlGenerator.getRequiredJaxbContextPath(), jerseyModuleClassLoader);

        } catch (final JAXBException ex) {
            try {
                // fallback for glassfish
                LOGGER.log(Level.FINE, LocalizationMessages.WADL_JAXB_CONTEXT_FALLBACK(), ex);
                jaxbContextCandidate = JAXBContext.newInstance(wadlGenerator.getRequiredJaxbContextPath());
            } catch (final JAXBException innerEx) {
                throw new ProcessingException(LocalizationMessages.ERROR_WADL_JAXB_CONTEXT(), ex);
            }
        } finally {
            AccessController.doPrivileged(ReflectionHelper.setContextClassLoaderPA(contextClassLoader));
        }

        jaxbContext = jaxbContextCandidate;
    }

    @Override
    public ApplicationDescription getApplication(final UriInfo uriInfo, final boolean detailedWadl) {
        final ApplicationDescription applicationDescription = getWadlBuilder(detailedWadl, uriInfo)
                .generate(resourceContext.getResourceModel().getRootResources());
        final Application application = applicationDescription.getApplication();
        for (final Resources resources : application.getResources()) {
            if (resources.getBase() == null) {
                resources.setBase(uriInfo.getBaseUri().toString());
            }
        }
        attachExternalGrammar(application, applicationDescription, uriInfo.getRequestUri());
        return applicationDescription;
    }

    @Override
    public Application getApplication(final UriInfo info,
                                      final org.glassfish.jersey.server.model.Resource resource, final boolean detailedWadl) {

        // Get the root application description
        //

        final ApplicationDescription description = getApplication(info, detailedWadl);

        final WadlGenerator wadlGenerator = wadlGeneratorConfig.createWadlGenerator(injectionManager);
        final Application application = new WadlBuilder(wadlGenerator, detailedWadl, info).generate(description, resource);
        if (application == null) {
            return null;
        }

        for (final Resources resources : application.getResources()) {
            resources.setBase(info.getBaseUri().toString());
        }

        // Attach any grammar we may have

        attachExternalGrammar(application, description,
                info.getRequestUri());

        for (final Resources resources : application.getResources()) {
            final Resource r = resources.getResource().get(0);
            r.setPath(info.getBaseUri().relativize(info.getAbsolutePath()).toString());

            // remove path params since path is fixed at this point
            r.getParam().clear();
        }

        return application;
    }

    @Override
    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }

    private WadlBuilder getWadlBuilder(final boolean detailedWadl, final UriInfo uriInfo) {
        return (this.wadlGenerationEnabled ? new WadlBuilder(wadlGeneratorConfig.createWadlGenerator(injectionManager),
                detailedWadl, uriInfo) : null);
    }

    @Override
    public void setWadlGenerationEnabled(final boolean wadlGenerationEnabled) {
        this.wadlGenerationEnabled = wadlGenerationEnabled;
    }

    @Override
    public boolean isWadlGenerationEnabled() {
        return wadlGenerationEnabled;
    }

    /**
     * Update the application object to include the generated grammar objects.
     */
    private void attachExternalGrammar(
            final Application application,
            final ApplicationDescription applicationDescription,
            URI requestURI) {

        // Massage the application.wadl URI slightly to get the right effect
        //

        try {
            final String requestURIPath = requestURI.getPath();

            if (requestURIPath.endsWith("application.wadl")) {
                requestURI = UriBuilder.fromUri(requestURI)
                        .replacePath(
                                requestURIPath
                                        .substring(0, requestURIPath.lastIndexOf('/') + 1))
                        .build();
            }

            final String root = application.getResources().get(0).getBase();
            final UriBuilder extendedPath = root != null
                    ? UriBuilder.fromPath(root).path("/application.wadl/") : UriBuilder.fromPath("./application.wadl/");
            final URI rootURI = root != null ? UriBuilder.fromPath(root).build() : null;

            // Add a reference to this grammar
            //

            final Grammars grammars;
            if (application.getGrammars() != null) {
                LOGGER.info(LocalizationMessages.ERROR_WADL_GRAMMAR_ALREADY_CONTAINS());
                grammars = application.getGrammars();
            } else {
                grammars = new Grammars();
                application.setGrammars(grammars);
            }

            // Create a reference back to the root WADL
            //

            for (final String path : applicationDescription.getExternalMetadataKeys()) {
                final URI schemaURI = extendedPath.clone().path(path).build();
                final String schemaPath = rootURI != null ? requestURI.relativize(schemaURI).toString() : schemaURI.toString();

                final Include include = new Include();
                include.setHref(schemaPath);
                final Doc doc = new Doc();
                doc.setLang("en");
                doc.setTitle("Generated");
                include.getDoc().add(doc);

                // Finally add to list
                grammars.getInclude().add(include);
            }
        } catch (final Exception e) {
            throw new ProcessingException(LocalizationMessages.ERROR_WADL_EXTERNAL_GRAMMAR(), e);
        }
    }
}
