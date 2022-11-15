/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.gf;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;

@ExtendWith(ArquillianExtension.class)
public class GFTest {
    @Deployment(testable = false)
    public static WebArchive createDeployment() throws IOException {
        return createDeployment(
                "gf-test",
                GFTestApp.class,
                GFTestResource.class
        );
    }

    private static WebArchive createDeployment(String archiveName, Class<?>... classes) {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, archiveName + ".war");
        archive.addClasses(classes);
        return archive;
    }

    @Test
    public void testUriInfo() {
        try (Response response = ClientBuilder.newClient().target("http://localhost:" + port())
                .path("gf-test/test/info").request().get()) {
            String entity = response.readEntity(String.class);
            System.out.println(entity);
            Assertions.assertEquals(200, response.getStatus());
            Assertions.assertTrue(entity.contains("gf-test/test"));
        }
    }

    @Test
    public void testReload() {
        try (Response response = ClientBuilder.newClient().target("http://localhost:" + port())
                .path("gf-test/test/reload").request().get()) {
            Assertions.assertEquals(200, response.getStatus());
            Assertions.assertEquals(GFTestApp.RELOADER, response.readEntity(String.class));
        }
        testUriInfo();
    }

    private static int port() {
        int port = Integer.parseInt(System.getProperty("webServerPort"));
        return port;
    }
}
