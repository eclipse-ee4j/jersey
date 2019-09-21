/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.filtering;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation used to create entity filtering annotations for entity (model) classes and resource methods and resources.
 * <p>
 * Entity Data Filtering via annotations is supposed to be used to annotate:
 * <ul>
 * <li>entity classes (supported on both, server and client sides), and</li>
 * <li>resource methods / resource classes (server side)</li>
 * </ul>
 * </p>
 * <p>
 * In entity filtering, a <i>entity-filtering</i> annotation is first defined using the {@code @EntityFiltering} meta-annotation:
 * <pre>
 *  &#64;Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
 *  &#64;Retention(value = RetentionPolicy.RUNTIME)
 *  <b>&#64;EntityFiltering</b>
 *  <b>public @interface DetailedView</b> {
 *
 *      public static class Factory extends <b>AnnotationLiteral&lt;DetailedView&gt;</b> implements <b>DetailedView</b> {
 *
 *         public static <b>DetailedView</b> get() {
               return new Factory();
           }
 *      }
 *  }
 * </pre>
 * </p>
 * <p>
 * Entity-filtering annotation should provide a factory class/method to create an instance of the annotation. Example of such
 * factory can be seen in the {@code DetailedView} above. Such instances can be then passed to the client/server runtime to
 * define/override entity-filtering scopes.
 * </p>
 * <p>
 * The defined entity-filtering annotation is then used to decorate a entity, it's property accessors or fields (more than one
 * entity may be decorated with the same entity-filtering annotation):
 * <pre>
 *  public class MyEntityClass {
 *
 *      <b>&#64;DetailedView</b>
 *      private String myField;
 *
 *      ...
 *  }
 * </pre>
 * </p>
 * <p>
 * At last, on the server-side, the entity-filtering annotations are applied to the resource or resource method(s) to which the
 * entity-filtering should be applied:
 * <pre>
 *  &#64;Path("/")
 *  public class MyResourceClass {
 *
 *      &#64;GET
 *      &#64;Produces("text/plain")
 *      &#64;Path("{id}")
 *      <b>&#64;DetailedView</b>
 *      public MyEntityClass get(@PathParam("id") String id) {
 *          // Return MyEntityClass.
 *      }
 *  }
 * </pre>
 * </p>
 * <p>
 * At last, on the client-side, the entity-filtering annotations are passed to the runtime via
 * {@link javax.ws.rs.client.Entity#entity(Object, javax.ws.rs.core.MediaType, java.lang.annotation.Annotation[]) Entity.entity()}
 * method and the entity-filtering scopes are then derived from the annotations:
 * <pre>
 *  ClientBuilder.newClient()
 *      .target("resource")
 *      .request()
 *      .post(Entity.entity(myentity, "application/json", <b>new Annotation[] {MyEntityClass.Factory.get()}</b>));
 * </pre>
 * </p>
 *
 * @author Michal Gajdos
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EntityFiltering {
}
