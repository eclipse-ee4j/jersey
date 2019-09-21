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

package org.glassfish.jersey.tests.integration.jersey2776;

import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.internal.MultiPartReaderClientSide;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.AttachmentBuilder;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.junit.Ignore;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Code under test: {@link MultiPartReaderClientSide} (unquoteMediaTypeParameters)
 *
 * @author Jonatan Jönsson (jontejj at gmail.com)
 */
@Ignore
public class Jersey2776ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Path("/files")
    public interface ApacheCxfMultipartClient {

        @POST
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        @Produces(MediaType.TEXT_PLAIN)
        String uploadDocument(MultipartBody content);
    }

    @Test
    public void testThatMultipartServerSupportsBoundaryQuotesEvenWithInterferingRuntimeDelegate() {
        final JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setAddress(getBaseUri().toString());
        bean.setServiceClass(ApacheCxfMultipartClient.class);
        final ApacheCxfMultipartClient cxfClient = bean.create(ApacheCxfMultipartClient.class);

        final String originalContent = "abc";
        final byte[] content = originalContent.getBytes(StandardCharsets.US_ASCII);
        final Attachment fileAttachment = new AttachmentBuilder()
                .object(content)
                .contentDisposition(new ContentDisposition("form-data; filename=\"abc-file\"; name=\"file_path\""))
                .build();

        final String fileContentReturnedFromServer = cxfClient.uploadDocument(new MultipartBody(fileAttachment));
        assertThat(fileContentReturnedFromServer, equalTo(originalContent));
    }
}
