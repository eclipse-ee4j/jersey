/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.server;

import org.glassfish.jersey.server.model.Resource;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * When use the instance to create the rest resource, the instance should not be difference.
 * via {@link Resource}'s programmatic API.
 *
 * @author DangCat (fan.shutian@zte.com.cn)
 */
public class InstanceResourceTest {
    private ServiceOneImpl serviceOne = new ServiceOneImpl();

    @Path("/srv1")
    public static class ServiceOneImpl {
        @GET
        public String getName() {
            return toString();
        }
    }

    private ApplicationHandler createApplication() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.registerResources(Resource.from(ServiceOneImpl.class, serviceOne, false));
        return new ApplicationHandler(resourceConfig);
    }

    @Test
    public void testInstanceResource() throws InterruptedException, ExecutionException {
        ApplicationHandler application = createApplication();
        Object result = application.apply(RequestContextBuilder.from("/srv1",
                "GET")
                .build())
                .get().getEntity();
        assertEquals(serviceOne.getName(), result);
    }
}
