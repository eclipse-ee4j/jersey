/*
 * Copyright (c) 2010, 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Locale;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import jakarta.inject.Singleton;
import jakarta.xml.bind.Marshaller;

import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.ExtendedResource;
import org.glassfish.jersey.server.wadl.WadlApplicationContext;

import com.sun.research.ws.wadl.Application;

/**
 *
 * @author Paul Sandoz
 */
@Singleton
@Path("application.wadl")
@ExtendedResource
public final class WadlResource {

    public static final String HTTPDATEFORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private volatile URI lastBaseUri;
    private volatile boolean lastDetailedWadl;

    private byte[] wadlXmlRepresentation;
    private String lastModified;
    private final Lock lock = new ReentrantLock();

    @Context
    private WadlApplicationContext wadlContext;


    public WadlResource() {
        this.lastModified = new SimpleDateFormat(HTTPDATEFORMAT, Locale.US).format(new Date());
    }

    private boolean isCached(UriInfo uriInfo, boolean detailedWadl) {
        return (lastBaseUri != null && lastBaseUri.equals(uriInfo.getBaseUri()) && lastDetailedWadl == detailedWadl);
    }

    @Produces({"application/vnd.sun.wadl+xml", "application/xml"})
    @GET
    public Response getWadl(@Context UriInfo uriInfo) {
        lock.lock();
        try {
            if (!wadlContext.isWadlGenerationEnabled()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            final boolean detailedWadl = WadlUtils.isDetailedWadlRequested(uriInfo);
            if ((wadlXmlRepresentation == null) || (!isCached(uriInfo, detailedWadl))) {
                this.lastBaseUri = uriInfo.getBaseUri();
                lastDetailedWadl = detailedWadl;
                this.lastModified = new SimpleDateFormat(HTTPDATEFORMAT, Locale.US).format(new Date());

                ApplicationDescription applicationDescription = wadlContext.getApplication(uriInfo,
                        detailedWadl);

                Application application = applicationDescription.getApplication();

                try {
                    final Marshaller marshaller = wadlContext.getJAXBContext().createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();
                    marshaller.marshal(application, os);
                    wadlXmlRepresentation = os.toByteArray();
                    os.close();
                } catch (Exception e) {
                    throw new ProcessingException("Could not marshal the wadl Application.", e);
                }
            }

            return Response.ok(new ByteArrayInputStream(wadlXmlRepresentation)).header("Last-modified", lastModified).build();
        } catch (Exception e) {
            throw new ProcessingException("Error generating /application.wadl.", e);
        } finally {
            lock.unlock();
        }
    }


    @Produces({"application/xml"})
    @GET
    @Path("{path}")
    public Response getExternalGrammar(
            @Context UriInfo uriInfo,
            @PathParam("path") String path) {
        lock.lock();
        try {
            // Fail if wadl generation is disabled
            if (!wadlContext.isWadlGenerationEnabled()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            ApplicationDescription applicationDescription =
                    wadlContext.getApplication(uriInfo, WadlUtils.isDetailedWadlRequested(uriInfo));

            // Fail is we don't have any metadata for this path
            ApplicationDescription.ExternalGrammar externalMetadata = applicationDescription.getExternalGrammar(path);

            if (externalMetadata == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Return the data
            return Response.ok().type(externalMetadata.getType())
                    .entity(externalMetadata.getContent())
                    .build();
        } catch (Exception e) {
            throw new ProcessingException(LocalizationMessages.ERROR_WADL_RESOURCE_EXTERNAL_GRAMMAR(), e);
        } finally {
            lock.unlock();
        }
    }

}
