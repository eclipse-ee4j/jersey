/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * This package defines a high-level (proxy-based) client API.
 * The API enables utilization of the server-side JAX-RS annotations
 * to describe the server-side resources and dynamically generate client-side
 * proxy objects for them.
 * <p>
 * Consider a server which exposes a resource at http://localhost:8080. The resource
 * can be described by the following interface:
 * </p>
 *
 * <pre>
 * &#064;Path("myresource")
 * public interface MyResourceIfc {
 *     &#064;GET
 *     &#064;Produces("text/plain")
 *     String get();
 *
 *     &#064;POST
 *     &#064;Consumes("application/xml")
 *     &#064;Produces("application/xml")
 *     MyBean postEcho(MyBean bean);
 *
 *     &#064;Path("{id}")
 *     &#064;GET
 *     &#064;Produces("text/plain")
 *     String getById(&#064;PathParam("id") String id);
 * }
 * </pre>
 *
 * <p>
 * You can use <a href="WebResourceFactory.html">WebResourceFactory</a> class defined
 * in this package to access the server-side resource using this interface.
 * Here is an example:
 * </p>
 *
 * <pre>
 * Client client = ClientBuilder.newClient();
 * WebTarget target = client.target("http://localhost:8080/");
 * MyResourceIfc resource = WebResourceFactory.newResource(MyResourceIfc.class, target);
 *
 * String responseFromGet = resource.get();
 * MyBean responseFromPost = resource.postEcho(myBeanInstance);
 * String responseFromGetById = resource.getById("abc");
 * </pre>
 */
package org.glassfish.jersey.client.proxy;
