/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Martin Matula
 */
public class WebResourceFactoryTest extends JerseyTest {

    private MyResourceIfc resource;
    private MyResourceIfc resource2;
    private MyResourceIfc resourceWithXML;

    @Override
    protected ResourceConfig configure() {
        // mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory
        // mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory
        // mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.jdkhttp.JdkHttpServerTestContainerFactory
        // mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.simple.SimpleTestContainerFactory
        enable(TestProperties.LOG_TRAFFIC);
        //        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(MyResource.class).register(JacksonJaxbJsonProvider.class);
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        resource = WebResourceFactory.newResource(MyResourceIfc.class, target().register(JacksonJaxbJsonProvider.class));
        resource2 = WebResourceFactory.newResource(MyResourceIfc.class, target());

        final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>(1);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
        resourceWithXML = WebResourceFactory
                .newResource(MyResourceIfc.class, target(), false, headers, Collections.<Cookie>emptyList(), new Form());
    }

    @Test
    public void testGetIt() {
        assertEquals("Got it!", resource.getIt());
    }

    @Test
    public void testPostIt() {
        final MyBean bean = new MyBean();
        bean.name = "Ahoj";
        assertEquals("Ahoj", resource.postIt(Collections.singletonList(bean)).get(0).name);
    }

    @Test
    public void testPostValid() {
        final MyBean bean = new MyBean();
        bean.name = "Ahoj";
        assertEquals("Ahoj", resource.postValid(bean).name);
    }

    @Test
    public void testPathParam() {
        assertEquals("jouda", resource.getId("jouda"));
    }

    @Test
    public void testQueryParam() {
        assertEquals("jiri", resource.getByName("jiri"));
    }

    @Test
    public void testFormParam() {
        assertEquals("jiri", resource.postByNameFormParam("jiri"));
    }

    @Test
    public void testCookieParam() {
        assertEquals("jiri", resource.getByNameCookie("jiri"));
    }

    @Test
    public void testHeaderParam() {
        assertEquals("jiri", resource.getByNameHeader("jiri"));
    }

    @Test
    public void testMatrixParam() {
        assertEquals("jiri", resource.getByNameMatrix("jiri"));
    }

    @Test
    public void testSubResource() {
        assertEquals("Got it!", resource.getSubResource().getMyBean().name);
    }

    @Test
    public void testQueryParamsAsList() {
        final List<String> list = new ArrayList<>();
        list.add("a");
        list.add("bb");
        list.add("ccc");

        assertEquals("3:[a, bb, ccc]", resource.getByNameList(list));
    }

    @Test
    public void testQueryParamsAsSet() {
        final Set<String> set = new HashSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.getByNameSet(set);
        checkSet(result);
    }

    @Test
    public void testQueryParamsAsSortedSet() {
        final SortedSet<String> set = new TreeSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.getByNameSortedSet(set);
        assertEquals("3:[a, bb, ccc]", result);
    }

    @Test
    @Disabled("See issue JERSEY-2441")
    public void testHeaderCookieAsList() {
        final List<String> list = new ArrayList<>();
        list.add("a");
        list.add("bb");
        list.add("ccc");

        assertEquals("3:[a, bb, ccc]", resource.getByNameCookieList(list));
    }

    @Test
    @Disabled("See issue JERSEY-2441")
    public void testHeaderCookieAsSet() {
        final Set<String> set = new HashSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.getByNameCookieSet(set);
        checkSet(result);
    }

    @Test
    @Disabled("See issue JERSEY-2441")
    public void testHeaderCookieAsSortedSet() {
        final SortedSet<String> set = new TreeSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.getByNameCookieSortedSet(set);
        assertEquals("3:[a, bb, ccc]", result);
    }

    /**
     * This cannot work with jersey now. Server side parses header params only if they are send as more
     * lines in the request. Jersey has currently no possibility to do so. See JERSEY-2263.
     */
    @Test
    @Disabled("See issue JERSEY-2263")
    public void testHeaderParamsAsList() {
        final List<String> list = new ArrayList<>();
        list.add("a");
        list.add("bb");
        list.add("ccc");

        assertEquals("3:[a, bb, ccc]", resource.getByNameHeaderList(list));
    }

    @Test
    @Disabled("See issue JERSEY-2263")
    public void testHeaderParamsAsSet() {
        final Set<String> set = new HashSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.getByNameHeaderSet(set);
        checkSet(result);
    }

    @Test
    @Disabled("See issue JERSEY-2263")
    public void testHeaderParamsAsSortedSet() {
        final SortedSet<String> set = new TreeSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.getByNameHeaderSortedSet(set);
        assertEquals("3:[a, bb, ccc]", result);
    }

    @Test
    public void testMatrixParamsAsList() {
        final List<String> list = new ArrayList<>();
        list.add("a");
        list.add("bb");
        list.add("ccc");

        assertEquals("3:[a, bb, ccc]", resource.getByNameMatrixList(list));
    }

    @Test
    public void testMatrixParamsAsSet() {
        final Set<String> set = new HashSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.getByNameMatrixSet(set);
        checkSet(result);
    }

    @Test
    public void testMatrixParamsAsSortedSet() {
        final SortedSet<String> set = new TreeSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.getByNameMatrixSortedSet(set);
        assertEquals("3:[a, bb, ccc]", result);
    }

    private void checkSet(final String result) {
        assertTrue(result.startsWith("3:["), "Set does not contain 3 items.");
        assertTrue(result.contains("a"), "Set does not contain 'a' item.");
        assertTrue(result.contains("bb"), "Set does not contain 'bb' item.");
        assertTrue(result.contains("ccc"), "Set does not contain 'ccc' item.");
    }

    @Test
    public void testFormParamsAsList() {
        final List<String> list = new ArrayList<>();
        list.add("a");
        list.add("bb");
        list.add("ccc");

        assertEquals("3:[a, bb, ccc]", resource.postByNameFormList(list));
    }

    @Test
    public void testFormParamsAsSet() {
        final Set<String> set = new HashSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.postByNameFormSet(set);
        checkSet(result);
    }

    @Test
    public void testFormParamsAsSortedSet() {
        final SortedSet<String> set = new TreeSet<>();
        set.add("a");
        set.add("bb");
        set.add("ccc");

        final String result = resource.postByNameFormSortedSet(set);
        assertEquals("3:[a, bb, ccc]", result);
    }

    @Test
    public void testAcceptHeader() {
        assertTrue(resource.isAcceptHeaderValid(null), "Accept HTTP header does not match @Produces annotation");
    }

    @Test
    public void testPutWithExplicitContentType() {
        assertEquals(resourceWithXML.putIt(new MyBean()), MediaType.APPLICATION_XML,
                "Content-Type HTTP header does not match explicitly provided type");
    }

    @Test
    public void testToString() throws Exception {
        final String actual = resource.toString();
        final String expected = target().path("myresource").toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testHashCode() throws Exception {
        int h1 = resource.hashCode();
        int h2 = resource2.hashCode();
        assertNotEquals(h1, h2, "The hash codes should not match");
    }

    @Test
    public void testEquals() {
        assertFalse(resource.equals(resource2), "The two resource instances should not be considered equals as they are unique");
    }

    @Test
    void testInstrumentingClientWorks() {
        MultivaluedMap<String, String> expected = new MultivaluedHashMap<>();
        expected.putSingle("key1", "value1");
        expected.putSingle("key2", "value2");
        Map<String, List<String>> result = resource.instrumentQueryParams();
        assertThat(result).isNotNull().containsExactlyInAnyOrderEntriesOf(expected);
    }
}
