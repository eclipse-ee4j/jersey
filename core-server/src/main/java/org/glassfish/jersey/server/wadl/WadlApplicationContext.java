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

package org.glassfish.jersey.server.wadl;

import javax.ws.rs.core.UriInfo;

import javax.xml.bind.JAXBContext;

import org.glassfish.jersey.server.wadl.internal.ApplicationDescription;

import com.sun.research.ws.wadl.Application;

/**
 * A context to obtain WADL-based information.
 *
 * @author Paul Sandoz
 */
public interface WadlApplicationContext {
//    /**
//     * Get a WADL builder initiated with the configured {@link WadlGenerator}
//     * for the Web application.
//     *
//     * @return the WADL builder.
//     */
//    WadlBuilder getWadlBuilder();

//    /**
//     * Get a new instance of a  {@link ApplicationDescription} corresponding to all
//     * the root resource classes.
//     *
//     * @return the application description, the contents may be modified.
//     */
//    ApplicationDescription getApplication();

    /**
     * Get a new instance of a {@link ApplicationDescription} corresponding to all
     * the root resource classes, and configure the base URI.
     *
     * @param ui the URI information from which the base URI is set on the
     *           WADL application.
     * @param detailedWadl flag indicating whether or not detailed WADL should be generated.
     * @return the application description, the contents may be modified.
     */
    ApplicationDescription getApplication(UriInfo ui, boolean detailedWadl);


    /**
     * Get a new instance of {@link Application} for a particular resource.
     *
     * @param info     the URI information from which the base URI is set on the
     *                 WADL application.
     * @param resource the resource to build the Application for
     * @param detailedWadl flag indicating whether or not detailed WADL should be generated.
     * @return the application for this resource
     */
    Application getApplication(UriInfo info,
                               org.glassfish.jersey.server.model.Resource resource, boolean detailedWadl);

    /**
     * Get the default JAXB context associated with the {@link WadlGenerator}
     * for the Web application.
     *
     * @return the default JAXB context.
     */
    JAXBContext getJAXBContext();

//    /**
//     * Get the default JAXB context path to create a {@link JAXBContext}.
//     *
//     * @return the default JAXB context.
//     */
//    String getJAXBContextPath();

    /**
     * Enable/disable WADL generation.
     *
     * @param wadlGenerationEnabled if wadlGenerationEnabled is true and
     *                              {@link org.glassfish.jersey.server.ServerProperties#WADL_FEATURE_DISABLE}
     *                              is false, WADL generation is enabled. In all other cases is disabled.
     */
    void setWadlGenerationEnabled(boolean wadlGenerationEnabled);

    /**
     * Get WADL generation status.
     *
     * @return true when WADL generation is enabled. Does not take
     *         {@link org.glassfish.jersey.server.ServerProperties#WADL_FEATURE_DISABLE}
     */
    boolean isWadlGenerationEnabled();
}
