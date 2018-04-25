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

/**
 * Provides support for Model, View and Controller (MVC).
 * <p/>
 * Given the MVC pattern the Controller corresponds to a resource class, the View to a template referenced by a template name,
 * and the Model to a Java object (or a Java bean).
 * <p/>
 * A resource method of a resource class may return an instance of {@link org.glassfish.jersey.server.mvc.Viewable} that
 * encapsulates the template name and the model. In this respect the instance of{@link org.glassfish.jersey.server.mvc.Viewable}
 * is the response entity. Such a viewable response entity may be set in contexts other than a resource method but for the
 * purposes of this section the focus is on resource methods.
 * <p/>
 * The {@link org.glassfish.jersey.server.mvc.Viewable}, returned by a resource method,
 * will be processed such that the template name is resolved to a template reference that identifies a template capable of
 * being processed by an appropriate view processor.
 * <br/>
 * The view processor then processes template given the model to produce a response entity that is returned to the client.
 * <p/>
 * For example, the template name could reference a Java Server Page (JSP) and the model will be accessible to that JSP. The
 * JSP view processor will process the JSP resulting in an HTML document that is returned as the response entity. (See later
 * for more details.)
 * <p/>
 * Two forms of returning {@link org.glassfish.jersey.server.mvc.Viewable} instances are supported: explicit; and implicit.
 */
package org.glassfish.jersey.server.mvc;
