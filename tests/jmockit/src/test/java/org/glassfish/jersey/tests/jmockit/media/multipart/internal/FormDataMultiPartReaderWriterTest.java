/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.jmockit.media.multipart.internal;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEParsingException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests for multipart {@code MessageBodyReader} and {@code MessageBodyWriter} as well as {@code FormDataMultiPart} and {@code
 * FormDataParam} injections.
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class FormDataMultiPartReaderWriterTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig()
                .register(MediaTypeWithBoundaryResource.class)
                .register(FileResource.class)
                .register(new MultiPartFeature());
    }

    @Path("/MediaTypeWithBoundaryResource")
    public static class MediaTypeWithBoundaryResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public String get(
                @Context final HttpHeaders h,
                @FormDataParam("submit") final String s) {
            final String b = h.getMediaType().getParameters().get("boundary");
            assertEquals("XXXX_YYYY", b);
            return s;
        }

    }


    @Path("/FileResource")
    @Consumes("multipart/form-data")
    @Produces("text/plain")
    public static class FileResource {

        @POST
        @Path("InjectedFileNotCopied")
        public String injectedFileNotCopied(@FormDataParam("file") final File file) {
            final String path = file.getAbsolutePath();
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return path;
        }

        @POST
        @Path("ExceptionInMethod")
        public String exceptionInMethod(@FormDataParam("file") final File file) {
            throw new WebApplicationException(Response.serverError().entity(file.getAbsolutePath()).build());
        }

        @POST
        @Path("SuccessfulMethod")
        public String successfulMethod(@FormDataParam("file") final File file) {
            return file.getAbsolutePath();
        }

        @POST
        @Path("FileSize")
        public long fileSize(@FormDataParam("file") final File file) {
            return file.length();
        }
    }

    /**
     * JERSEY-2663 reproducer. Make sure that temporary file created by MIMEPull is not copied into new temporary file created
     * by Jersey.
     */
    @Test
    public void testInjectedFileNotCopied(@Mocked final BodyPartEntity entity) throws Exception {
        final FormDataMultiPart multipart = new FormDataMultiPart();
        final FormDataBodyPart bodypart = new FormDataBodyPart(FormDataContentDisposition.name("file").fileName("file").build(),
                "CONTENT");
        multipart.bodyPart(bodypart);

        final Response response = target().path("FileResource").path("InjectedFileNotCopied")
                .register(MultiPartFeature.class)
                .request()
                .post(Entity.entity(multipart, MediaType.MULTIPART_FORM_DATA));

        // Make sure that the Mimepull temp file has been moved to specific file.
        new Verifications() {{
            entity.moveTo(withInstanceOf(File.class));
            times = 1;
        }};

        // Make sure that the temp file has been removed.
        final String pathname = response.readEntity(String.class);
        // Wait a second to make sure the file doesn't exist.
        Thread.sleep(1000);

        assertThat("Temporary file, " + pathname + ", on the server has not been removed",
                new File(pathname).exists(), is(false));
    }

    /**
     * Mocked JERSEY-2794 reproducer. Real test is under integration tests.
     */
    @Test
    public void mimeTempFileRemovedAfterAbortedUpload(@Mocked final MIMEMessage message) throws Exception {
        new Expectations() {{
            message.getAttachments();
            result = new MIMEParsingException();
        }};

        final URL url = new URL(getBaseUri().toString() + "MediaTypeWithBoundaryResource");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=XXXX_YYYY");

        connection.setDoOutput(true);
        connection.connect();

        final OutputStream outputStream = connection.getOutputStream();
        outputStream.write("--XXXX_YYYY".getBytes());
        outputStream.write('\n');
        outputStream.write("Content-Type: text/plain".getBytes());
        outputStream.write('\n');
        outputStream.write("Content-Disposition: form-data; name=\"big-part\"".getBytes());
        outputStream.write('\n');
        outputStream.write('\n');

        // Send big chunk of data.
        for (int i = 0; i < 16 * 4096; i++) {
            outputStream.write('E');
            if (i % 1024 == 0) {
                outputStream.flush();
            }
        }

        // Do NOT send end of the MultiPart message to simulate the issue.

        // Get Response ...
        final int response = connection.getResponseCode();
        // ... Disconnect.
        connection.disconnect();

        assertThat("Bad Request expected", response, is(400));

        // Make sure that the Mimepull message and it's parts have been closed and temporary files deleted.
        new Verifications() {{
            message.close();
            times = 1;
        }};
    }
}
