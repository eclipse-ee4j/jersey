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

package org.glassfish.jersey.linking;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
public class LinkELContextTest {

    @Test
    public void testExpressionFactory() {
        System.out.println("Create expression factory");
        ExpressionFactory factory = ExpressionFactory.newInstance();
        assertNotNull(factory);
    }

    @Test
    public void testLiteralExpression() {
        System.out.println("Literal expression");
        ExpressionFactory factory = ExpressionFactory.newInstance();
        LinkELContext context = new LinkELContext(new BooleanBean(), null);
        ValueExpression expr = factory.createValueExpression(context,
                "${1+2}", int.class);
        Object value = expr.getValue(context);
        assertEquals(3, value);
    }

    public static final String ID = "10";
    public static final String NAME = "TheName";

    public static class EntityBean {

        private String id = ID;
        private String name = NAME;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void testExpression() {
        System.out.println("Raw expression");
        ExpressionFactory factory = ExpressionFactory.newInstance();
        LinkELContext context = new LinkELContext(new EntityBean(), null);
        ValueExpression expr = factory.createValueExpression(context,
                "${entity.id}", String.class);
        Object value = expr.getValue(context);
        assertEquals(ID, value);
    }

    @Test
    public void testEmbeddedExpression() {
        System.out.println("Embedded expression");
        ExpressionFactory factory = ExpressionFactory.newInstance();
        LinkELContext context = new LinkELContext(new EntityBean(), null);
        ValueExpression expr = factory.createValueExpression(context,
                "foo/${entity.id}/bar", String.class);
        Object value = expr.getValue(context);
        assertEquals("foo/" + ID + "/bar", value);
    }

    @Test
    public void testMultipleExpressions() {
        System.out.println("Multiple expressions");
        ExpressionFactory factory = ExpressionFactory.newInstance();
        LinkELContext context = new LinkELContext(new EntityBean(), null);
        ValueExpression expr = factory.createValueExpression(context,
                "foo/${entity.id}/bar/${entity.name}", String.class);
        Object value = expr.getValue(context);
        assertEquals("foo/" + ID + "/bar/" + NAME, value);
    }

    public static class OuterEntityBean {

        private EntityBean inner = new EntityBean();

        public EntityBean getInner() {
            return inner;
        }
    }

    @Test
    public void testNestedExpression() {
        System.out.println("Nested expression");
        ExpressionFactory factory = ExpressionFactory.newInstance();
        LinkELContext context = new LinkELContext(new OuterEntityBean(), null);
        ValueExpression expr = factory.createValueExpression(context,
                "${entity.inner.id}", String.class);
        Object value = expr.getValue(context);
        assertEquals(ID, value);
    }

    public static class BooleanBean {

        public boolean getEnabled() {
            return true;
        }

        public boolean getValue(boolean value) {
            return value;
        }
    }

    @Test
    public void testBooleanExpression() {
        System.out.println("Boolean expression");
        ExpressionFactory factory = ExpressionFactory.newInstance();
        LinkELContext context = new LinkELContext(new BooleanBean(), null);
        ValueExpression expr = factory.createValueExpression(context,
                "${entity.enabled}", boolean.class);
        Object value = expr.getValue(context);
        assertEquals(true, value);
        expr = factory.createValueExpression(context,
                "${entity.getValue(true)}", boolean.class);
        value = expr.getValue(context);
        assertEquals(true, value);
        expr = factory.createValueExpression(context,
                "${entity.getValue(false)}", boolean.class);
        value = expr.getValue(context);
        assertEquals(false, value);
    }

}
