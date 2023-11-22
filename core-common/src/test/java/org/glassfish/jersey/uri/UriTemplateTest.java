/*
 * Copyright (c) 2010, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.uri;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

import org.glassfish.jersey.uri.internal.UriTemplateParser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Taken from Jersey 1: jersey-tests: com.sun.jersey.impl.uri.UriTemplateTest
 *
 * @author Paul Sandoz
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
public class UriTemplateTest {

    /**
     * Test the URI resolution as defined in RFC 3986,
     * <a href="http://tools.ietf.org/html/rfc3986#section-5.4.1">sect. 5.4.1</a> and
     * and <a href="http://tools.ietf.org/html/rfc3986#section-5.4.2">sect. 5.4.2</a>.
     */
    @Test
    public void testResolveUri() {
        final URI baseUri = URI.create("http://a/b/c/d;p?q");

        // Normal examples (RFC 3986, sect. 5.4.1)
        assertThat(UriTemplate.resolve(baseUri, URI.create("g:h")), equalTo(URI.create("g:h")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g:h")), equalTo(URI.create("g:h")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g")), equalTo(URI.create("http://a/b/c/g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("./g")), equalTo(URI.create("http://a/b/c/g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g/")), equalTo(URI.create("http://a/b/c/g/")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("/g")), equalTo(URI.create("http://a/g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("//g")), equalTo(URI.create("http://g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("?y")), equalTo(URI.create("http://a/b/c/d;p?y")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g?y")), equalTo(URI.create("http://a/b/c/g?y")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("#s")), equalTo(URI.create("http://a/b/c/d;p?q#s")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g#s")), equalTo(URI.create("http://a/b/c/g#s")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g?y#s")), equalTo(URI.create("http://a/b/c/g?y#s")));
        assertThat(UriTemplate.resolve(baseUri, URI.create(";x")), equalTo(URI.create("http://a/b/c/;x")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g;x")), equalTo(URI.create("http://a/b/c/g;x")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g;x?y#s")), equalTo(URI.create("http://a/b/c/g;x?y#s")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("")), equalTo(URI.create("http://a/b/c/d;p?q")));
        assertThat(UriTemplate.resolve(baseUri, URI.create(".")), equalTo(URI.create("http://a/b/c/")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("./")), equalTo(URI.create("http://a/b/c/")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("..")), equalTo(URI.create("http://a/b/")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("../")), equalTo(URI.create("http://a/b/")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("../g")), equalTo(URI.create("http://a/b/g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("../..")), equalTo(URI.create("http://a/")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("../../")), equalTo(URI.create("http://a/")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("../../g")), equalTo(URI.create("http://a/g")));

        // Abnormal examples (RFC 3986, sect. 5.4.2)
        assertThat(UriTemplate.resolve(baseUri, URI.create("../../../g")), equalTo(URI.create("http://a/g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("../../../../g")), equalTo(URI.create("http://a/g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("/./g")), equalTo(URI.create("http://a/g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("/../g")), equalTo(URI.create("http://a/g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g.")), equalTo(URI.create("http://a/b/c/g.")));
        assertThat(UriTemplate.resolve(baseUri, URI.create(".g")), equalTo(URI.create("http://a/b/c/.g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g..")), equalTo(URI.create("http://a/b/c/g..")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("..g")), equalTo(URI.create("http://a/b/c/..g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("./../g")), equalTo(URI.create("http://a/b/g")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("./g/.")), equalTo(URI.create("http://a/b/c/g/")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g/./h")), equalTo(URI.create("http://a/b/c/g/h")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g/../h")), equalTo(URI.create("http://a/b/c/h")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g;x=1/./y")), equalTo(URI.create("http://a/b/c/g;x=1/y")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g;x=1/../y")), equalTo(URI.create("http://a/b/c/y")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g?y/./x")), equalTo(URI.create("http://a/b/c/g?y/./x")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g?y/../x")), equalTo(URI.create("http://a/b/c/g?y/../x")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g#s/./x")), equalTo(URI.create("http://a/b/c/g#s/./x")));
        assertThat(UriTemplate.resolve(baseUri, URI.create("g#s/../x")), equalTo(URI.create("http://a/b/c/g#s/../x")));
        // Per RFC 3986, test below should resolve to "http:g" for strict parsers and "http://a/b/c/g" for backward compatibility
        assertThat(UriTemplate.resolve(baseUri, URI.create("http:g")), equalTo(URI.create("http:g")));

        // JDK bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535
        assertThat(UriTemplate.resolve(baseUri, URI.create("")), equalTo(baseUri));
    }

    @Test
    public void testRelativizeUri() {
        URI baseUri;

        baseUri = URI.create("http://a/b/c/d");
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/e")), equalTo(URI.create("e")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/./e")), equalTo(URI.create("e")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/e/../f")), equalTo(URI.create("f")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/e/.././f")), equalTo(URI.create("f")));

        baseUri = URI.create("http://a/b/c/d?q=v");
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/e")), equalTo(URI.create("e")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/./e")), equalTo(URI.create("e")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/e/../f")), equalTo(URI.create("f")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/e/.././f")), equalTo(URI.create("f")));

        // NOTE: At the moment, in sync with the JDK implementation of relativize() method,
        // we do not support relativization of URIs that do not fully prefix the base URI.
        // Once (if) we decide to improve this support beyond what JDK supports, we may need
        // to update the assertions below.
        baseUri = URI.create("http://a/b/c/d");
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/e")), equalTo(URI.create("http://a/b/c/e")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/./e")), equalTo(URI.create("http://a/b/c/e")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/.././e")), equalTo(URI.create("http://a/b/c/e")));

        baseUri = URI.create("http://a/b/c/d?q=v");
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/e")), equalTo(URI.create("http://a/b/c/e")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/./e")), equalTo(URI.create("http://a/b/c/e")));
        assertThat(UriTemplate.relativize(baseUri, URI.create("http://a/b/c/d/.././e")), equalTo(URI.create("http://a/b/c/e")));
    }

    @Test
    public void testTemplateNames() {
        _testTemplateNames("{a}", "a");
        _testTemplateNames("{  a}", "a");
        _testTemplateNames("{  a  }", "a");
        _testTemplateNames("{a:}", "a");
        _testTemplateNames("{a :}", "a");
        _testTemplateNames("{a : }", "a");

        _testTemplateNames("http://example.org/{a}/{b}/", "a", "b");
        _testTemplateNames("http://example.org/page1#{a}", "a");
        _testTemplateNames("{scheme}://{20}.example.org?date={wilma}&option={a}", "scheme", "20", "wilma", "a");
        _testTemplateNames("http://example.org/{a-b}", "a-b");
        _testTemplateNames("http://example.org?{p}", "p");
        _testTemplateNames("http://example.com/order/{c}/{c}/{c}/", "c", "c", "c");
    }

    void _testTemplateNames(final String template, final String... names) {
        final UriTemplate t = new UriTemplate(template);
        _testTemplateNames(t.getTemplateVariables(), names);
    }

    void _testTemplateNames(final List<String> regexNames, final String... names) {
        assertEquals(names.length, regexNames.size());

        final Iterator<String> i = regexNames.iterator();
        for (final String name : names) {
            assertEquals(name, i.next());
        }
    }

    @Test
    public void testMatching() {
        _testMatching("http://example.org/{a}/{b}/",
                "http://example.org/fred/barney/",
                "fred", "barney");
        _testMatching("http://example.org/page1#{a}",
                "http://example.org/page1#fred",
                "fred");
        _testMatching("{scheme}://{20}.example.org?date={wilma}&option={a}",
                "https://this-is-spinal-tap.example.org?date=2008&option=fred",
                "https", "this-is-spinal-tap", "2008", "fred");
        _testMatching("http://example.org/{a-b}",
                "http://example.org/none%20of%20the%20above",
                "none%20of%20the%20above");
        _testMatching("http://example.org?{p}",
                "http://example.org?quote=to+bo+or+not+to+be",
                "quote=to+bo+or+not+to+be");
        _testMatching("http://example.com/order/{c}/{c}/{c}/",
                "http://example.com/order/cheeseburger/cheeseburger/cheeseburger/",
                "cheeseburger", "cheeseburger", "cheeseburger");
        _testMatching("http://example.com/{q}",
                "http://example.com/hullo#world",
                "hullo#world");
        _testMatching("http://example.com/{e}/",
                "http://example.com/xxx/",
                "xxx");
    }

    @Test
    public void testTemplateRegexes() {
        _testTemplateRegex("{a:}", "(" + UriTemplateParser.TEMPLATE_VALUE_PATTERN.pattern() + ")");
        _testTemplateRegex("{a:.*}", "(.*)");
        _testTemplateRegex("{a:  .*}", "(.*)");
        _testTemplateRegex("{a:  .*  }", "(.*)");
        _testTemplateRegex("{a :  .*  }", "(.*)");
    }

    private void _testTemplateRegex(final String template, final String regex) {
        final UriTemplate t = new UriTemplate(template);
        assertEquals(regex, t.getPattern().toString());
    }

    @Test
    public void testRegexMatching() {
        _testMatching("{b: .+}",
                "1",
                "1");

        _testMatching("{b: .+}",
                "1/2/3",
                "1/2/3");

        _testMatching("http://example.org/{a}/{b: .+}",
                "http://example.org/fred/barney/x/y/z",
                "fred", "barney/x/y/z");

        _testMatching("{b: \\d+}",
                "1234567890",
                "1234567890");

        _testMatching("{a}/{b: .+}/{c}{d: (/.*)?}",
                "1/2/3/4",
                "1", "2/3", "4", "");

        _testMatching("{a}/{b: .+}/{c}{d: (/.*)?}",
                "1/2/3/4/",
                "1", "2/3", "4", "/");
    }

    @Test
    public void testRegexMatchingWithNestedGroups() {
        _testMatching("{b: (\\d+)}",
                "1234567890",
                "1234567890");

        _testMatching("{b: (\\d+)-(\\d+)-(\\d+)}",
                "12-34-56",
                "12-34-56");

        _testMatching("{a: (\\d)(\\d*)}-{b: (\\d)(\\d*)}-{c: (\\d)(\\d*)}",
                "12-34-56",
                "12", "34", "56");
    }

    void _testMatching(final String template, final String uri, final String... values) {
        final UriTemplate t = new UriTemplate(template);
        final Map<String, String> m = new HashMap<String, String>();

        boolean isMatch = t.match(uri, m);
        assertTrue(isMatch, "No match for '" + uri + "' & params '" + Arrays.toString(values) + "`");
        assertEquals(values.length, t.getTemplateVariables().size());

        final Iterator<String> names = t.getTemplateVariables().iterator();
        for (final String value : values) {
            final String mapValue = m.get(names.next());
            assertEquals(value, mapValue);
        }

        final List<String> matchedValues = new ArrayList<String>();
        isMatch = t.match(uri, matchedValues);
        assertTrue(isMatch);
        assertEquals(values.length, matchedValues.size());

        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], matchedValues.get(i));
        }

        final MatchResult mr = t.getPattern().match(uri);
        assertNotNull(mr);
        assertEquals(values.length, mr.groupCount());
        assertEquals(uri, mr.group());
        assertEquals(uri, mr.group(0));
        assertEquals(0, mr.start());
        assertEquals(uri.length(), mr.end());
        assertEquals(0, mr.start(0));
        assertEquals(uri.length(), mr.end(0));
        for (int i = 0; i < mr.groupCount(); i++) {
            assertEquals(values[i], mr.group(i + 1));
            int start = mr.start(i + 1);
            int end = mr.end(i + 1);
            assertEquals(values[i], start == -1 ? null : uri.substring(start, end));
        }
    }

    @Test
    public void testNullMatching() {
        final Map<String, String> m = new HashMap<String, String>();

        UriTemplate t = UriTemplate.EMPTY;
        assertEquals(false, t.match("/", m));
        assertEquals(true, t.match(null, m));
        assertEquals(true, t.match("", m));

        t = new UriTemplate("/{v}");
        assertEquals(false, t.match(null, m));
        assertEquals(true, t.match("/one", m));
    }

    @Test
    public void testOrder() {
        final List<UriTemplate> l = new ArrayList<UriTemplate>();

        l.add(UriTemplate.EMPTY);
        l.add(new UriTemplate("/{a}"));
        l.add(new UriTemplate("/{a}/{b}"));
        l.add(new UriTemplate("/{a}/one/{b}"));

        Collections.sort(l, UriTemplate.COMPARATOR);

        assertEquals(new UriTemplate("/{a}/one/{b}").getTemplate(),
                l.get(0).getTemplate());
        assertEquals(new UriTemplate("/{a}/{b}").getTemplate(),
                l.get(1).getTemplate());
        assertEquals(new UriTemplate("/{a}").getTemplate(),
                l.get(2).getTemplate());
        assertEquals(UriTemplate.EMPTY.getTemplate(),
                l.get(3).getTemplate());
    }

    @Test
    public void testOrderDuplicitParams() {
        final List<UriTemplate> l = new ArrayList<UriTemplate>();

        l.add(new UriTemplate("/{a}"));
        l.add(new UriTemplate("/{a}/{a}"));

        Collections.sort(l, UriTemplate.COMPARATOR);

        assertEquals(new UriTemplate("/{a}/{a}").getTemplate(),
                l.get(0).getTemplate());
        assertEquals(new UriTemplate("/{a}").getTemplate(),
                l.get(1).getTemplate());
    }

    @Test
    public void testSubstitutionArray() {
        _testSubstitutionArray("http://example.org/{a}/{b}/",
                "http://example.org/fred/barney/",
                "fred", "barney");
        _testSubstitutionArray("http://example.org/page1#{a}",
                "http://example.org/page1#fred",
                "fred");
        _testSubstitutionArray("{scheme}://{20}.example.org?date={wilma}&option={a}",
                "https://this-is-spinal-tap.example.org?date=&option=fred",
                "https", "this-is-spinal-tap", "", "fred");
        _testSubstitutionArray("http://example.org/{a-b}",
                "http://example.org/none%20of%20the%20above",
                "none%20of%20the%20above");
        _testSubstitutionArray("http://example.org?{p}",
                "http://example.org?quote=to+bo+or+not+to+be",
                "quote=to+bo+or+not+to+be");
        _testSubstitutionArray("http://example.com/order/{c}/{c}/{c}/",
                "http://example.com/order/cheeseburger/cheeseburger/cheeseburger/",
                "cheeseburger");
        _testSubstitutionArray("http://example.com/{q}",
                "http://example.com/hullo#world",
                "hullo#world");
        _testSubstitutionArray("http://example.com/{e}/",
                "http://example.com//",
                "");
        _testSubstitutionArray("http://example.com/{a}/{b}/{a}",
                "http://example.com/fred/barney/fred",
                "fred", "barney", "joe");
    }

    @Test
    public void testGroupIndexes() throws Exception {
        UriTemplate template = new UriTemplate("/a");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[0]));

        template = new UriTemplate("/{a}");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[] {1}));

        template = new UriTemplate("/{a}/b");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[] {1}));

        template = new UriTemplate("/{a}/{b}");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[] {1, 2}));

        template = new UriTemplate("/{a}/{b}");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[] {1, 2}));

        template = new UriTemplate("/{a}/b/{c}");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[] {1, 2}));

        template = new UriTemplate("/{a: (abc)+}");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[] {1}));

        template = new UriTemplate("/{a: (abc)+}/b");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[] {1}));

        template = new UriTemplate("/{a: (abc)+}/{b}");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[] {1, 3}));

        template = new UriTemplate("/{a: (abc)+}/b/{c}");
        assertThat(template.getPattern().getGroupIndexes(), equalTo(new int[] {1, 3}));
    }

    void _testSubstitutionArray(final String template, final String uri, final String... values) {
        final UriTemplate t = new UriTemplate(template);

        assertEquals(uri, t.createURI(values));
    }

    @Test
    public void testSubstitutionMap() {
        _testSubstitutionMap("http://example.org/{a}/{b}/",
                "http://example.org/fred/barney/",
                "a", "fred",
                "b", "barney");
        _testSubstitutionMap("http://example.org/page1#{a}",
                "http://example.org/page1#fred",
                "a", "fred");
        _testSubstitutionMap("{scheme}://{20}.example.org?date={wilma}&option={a}",
                "https://this-is-spinal-tap.example.org?date=&option=fred",
                "scheme", "https",
                "20", "this-is-spinal-tap",
                "wilma", "",
                "a", "fred");
        _testSubstitutionMap("http://example.org/{a-b}",
                "http://example.org/none%20of%20the%20above",
                "a-b", "none%20of%20the%20above");
        _testSubstitutionMap("http://example.org?{p}",
                "http://example.org?quote=to+bo+or+not+to+be",
                "p", "quote=to+bo+or+not+to+be");
        _testSubstitutionMap("http://example.com/order/{c}/{c}/{c}/",
                "http://example.com/order/cheeseburger/cheeseburger/cheeseburger/",
                "c", "cheeseburger");
        _testSubstitutionMap("http://example.com/{q}/z",
                "http://example.com/hullo%23world/z",
                "q", "hullo#world");
        _testSubstitutionMap("http://example.com/{e}/",
                "http://example.com//",
                "e", "");
    }

    void _testSubstitutionMap(final String template, final String uri, final String... variablesAndvalues) {
        final UriTemplate t = new UriTemplate(template);

        final Map<String, String> variableMap = new HashMap<String, String>();
        for (int i = 0; i < variablesAndvalues.length; i += 2) {
            variableMap.put(variablesAndvalues[i], variablesAndvalues[i + 1]);
        }

        assertEquals(uri, t.createURI(variableMap));
    }

    @Test
    public void testNormalizesURIs() throws Exception {
        this.validateNormalize("/some-path", "/some-path");
        this.validateNormalize("http://example.com/some/../path", "http://example.com/path");
        // note, that following behaviour differs from Jersey-1.x UriHelper.normalize(), the '..' segment is simply left out in
        // this case, where older UriHelper.normalize() would return the path including the '..' segment. It is also mentioned
        // in the UriTemplate.normalize() javadoc.
        this.validateNormalize("http://example.com/../path", "http://example.com/path");
        this.validateNormalize("http://example.com//path", "http://example.com//path");
    }

    private void validateNormalize(final String path, final String expected) throws Exception {
        final URI result = UriTemplate.normalize(path);
        assertEquals(expected, result.toString());
    }

    @Test
    public void testSingleQueryParameter() throws Exception {
        final UriTemplate tmpl = new UriTemplate("/test{?query}");

        final Map<String, String> result = new HashMap<String, String>();
        tmpl.match("/test?query=x", result);

        assertEquals(
                1,
                result.size(),
                "incorrect size for match string"
        );

        assertEquals(
                "x",
                result.get("query"),
                "query parameter is not matched"
        );
    }

    @Test
    public void testDoubleQueryParameter() throws Exception {
        final UriTemplate tmpl = new UriTemplate("/test{?query,secondQuery}");

        final List<String> list = new ArrayList<String>();
        tmpl.match("/test?query=x&secondQuery=y", list);

        final Map<String, String> result = new HashMap<String, String>();
        tmpl.match("/test?query=x&secondQuery=y", result);

        assertEquals(
                2,
                result.size(),
                "incorrect size for match string"
        );

        assertEquals(
                "x",
                result.get("query"),
                "query parameter is not matched"
        );
        assertEquals(
                "y",
                result.get("secondQuery"),
                "query parameter is not matched"
        );
    }

    @Test
    public void testSettingQueryParameter() throws Exception {
        final UriTemplate tmpl = new UriTemplate("/test{?query}");

        final Map<String, String> values = new HashMap<String, String>();
        values.put("query", "example");

        final String uri = tmpl.createURI(values);
        assertEquals(
                "/test?query=example",
                uri,
                "query string is not set"
        );
    }

    @Test
    public void testSettingTwoQueryParameter() throws Exception {
        final UriTemplate tmpl = new UriTemplate("/test{?query,other}");

        final Map<String, String> values = new HashMap<String, String>();
        values.put("query", "example");
        values.put("other", "otherExample");

        final String uri = tmpl.createURI(values);
        assertEquals(
                "/test?query=example&other=otherExample",
                uri,
                "query string is not set"
        );

    }

    @Test
    public void testNotSettingQueryParameter() throws Exception {
        final UriTemplate tmpl = new UriTemplate("/test{?query}");

        final Map<String, String> values = new HashMap<String, String>();

        final String uri = tmpl.createURI(values);
        assertEquals(
                "/test",
                uri,
                "query string is set"
        );

    }

    @Test
    public void testSettingMatrixParameter() throws Exception {
        final UriTemplate tmpl = new UriTemplate("/test{;matrix}/other");

        final Map<String, String> values = new HashMap<String, String>();
        values.put("matrix", "example");

        final String uri = tmpl.createURI(values);
        assertEquals(
                "/test;matrix=example/other",
                uri,
                "query string is not set"
        );

    }

    @Test
    public void testSettingTwoMatrixParameter() throws Exception {
        final UriTemplate tmpl = new UriTemplate("/test{;matrix,other}/other");

        final Map<String, String> values = new HashMap<String, String>();
        values.put("matrix", "example");
        values.put("other", "otherExample");

        final String uri = tmpl.createURI(values);
        assertEquals(
                "/test;matrix=example;other=otherExample/other",
                uri,
                "query string is not set"
        );

    }

    @Test
    public void testSettingTwoSeperatedMatrixParameter() throws Exception {
        final UriTemplate tmpl = new UriTemplate("/test{;matrix}/other{;other}");

        final Map<String, String> values = new HashMap<String, String>();
        values.put("matrix", "example");
        values.put("other", "otherExample");

        final String uri = tmpl.createURI(values);
        assertEquals(
                "/test;matrix=example/other;other=otherExample",
                uri,
                "query string is not set"
        );
    }

    @Test
    public void testNotSettingMatrixParameter() throws Exception {
        final UriTemplate tmpl = new UriTemplate("/test{;query}/other");

        final Map<String, String> values = new HashMap<String, String>();

        final String uri = tmpl.createURI(values);
        assertEquals(
                "/test/other",
                uri,
                "query string is set"
        );
    }

    /*
        RFC 6570, section 3.2:

             count := ("one", "two", "three")
             dom   := ("example", "com")
             dub   := "me/too"
             hello := "Hello World!"
             half  := "50%"
             var   := "value"
             who   := "fred"
             base  := "http://example.com/home/"
             path  := "/foo/bar"
             list  := ("red", "green", "blue")
             keys  := [("semi",";"),("dot","."),("comma",",")]
             v     := "6"
             x     := "1024"
             y     := "768"
             empty := ""
             empty_keys  := []
             undef := null
     */
    private static final List<String> count = Arrays.asList("one", "two", "three");
    private static final List<String> dom = Arrays.asList("example", "com");
    private static final String dub = "me/too";
    private static final String hello = "Hello World!";
    private static final String half = "50%";
    private static final String var = "value";
    private static final String who = "fred";
    private static final String base = "http://example.com/home/";
    private static final String path = "/foo/bar";
    private static final List<String> list = Arrays.asList("red", "green", "blue");
    private static final Map<String, String> keys = new LinkedHashMap<String, String>() {{
        put("semi", ";");
        put("dot", ".");
        put("comma", ",");
    }};
    private static final String v = "6";
    private static final String x = "1024";
    private static final String y = "768";
    private static final String empty = "";
    private static final Map<String, String> emptyKeys = Collections.emptyMap();

    @Test
    public void testRfc6570QueryTemplateExamples() {
        /*
            RFC 6570, section 3.2.8:

               {?who}             ?who=fred
               {?half}            ?half=50%25
               {?x,y}             ?x=1024&y=768
               {?x,y,empty}       ?x=1024&y=768&empty=
               {?x,y,undef}       ?x=1024&y=768
               {?var:3}           ?var=val
               {?list}            ?list=red,green,blue
               {?list*}           ?list=red&list=green&list=blue
               {?keys}            ?keys=semi,%3B,dot,.,comma,%2C
               {?keys*}           ?semi=%3B&dot=.&comma=%2C
        */

        assertEncodedQueryTemplateExpansion("?who=fred", "{?who}", who);
        assertEncodedQueryTemplateExpansion("?half=50%25", "{?half}", half);
        assertEncodedQueryTemplateExpansion("?x=1024&y=768", "{?x,y}", x, y);
        assertEncodedQueryTemplateExpansion("?x=1024&y=768&empty=", "{?x,y,empty}", x, y, empty);
        assertEncodedQueryTemplateExpansion("?x=1024&y=768", "{?x,y,undef}", x, y);

        assertEncodedQueryTemplateExpansion("?var=val", "{?var:3}", var);
        assertEncodedQueryTemplateExpansion("?list=red,green,blue", "{?list}", list);
        assertEncodedQueryTemplateExpansion("?list=red&list=green&list=blue", "{?list*}", list);
        assertEncodedQueryTemplateExpansion("?keys=semi,%3B,dot,.,comma,%2C", "{?keys}", new Object[]{keys});
        assertEncodedQueryTemplateExpansion("?semi=%3B&dot=.&comma=%2C", "{?keys*}", new Object[]{keys});
    }

    @Test
    public void testRfc6570QueryContinuationTemplateExamples() {
        /*
            RFC 6570, section 3.2.9:

           {&who}             &who=fred
           {&half}            &half=50%25
           ?fixed=yes{&x}     ?fixed=yes&x=1024
           {&x,y,empty}       &x=1024&y=768&empty=
           {&x,y,undef}       &x=1024&y=768

           {&var:3}           &var=val
           {&list}            &list=red,green,blue
           {&list*}           &list=red&list=green&list=blue
           {&keys}            &keys=semi,%3B,dot,.,comma,%2C
           {&keys*}           &semi=%3B&dot=.&comma=%2C
        */

        assertEncodedQueryTemplateExpansion("&who=fred", "{ &who}", who);
        assertEncodedQueryTemplateExpansion("&half=50%25", "{&half}", half);
        assertEncodedQueryTemplateExpansion("?fixed=yes&x=1024", "?fixed=yes{&x}", x, y);
        assertEncodedQueryTemplateExpansion("&x=1024&y=768&empty=", "{&x,y,empty}", x, y, empty);
        assertEncodedQueryTemplateExpansion("&x=1024&y=768", "{&x,y,undef}", x, y);

        assertEncodedQueryTemplateExpansion("&var=val", "{&var:3}", var);
        assertEncodedQueryTemplateExpansion("&list=red,green,blue", "{&list}", list);
        assertEncodedQueryTemplateExpansion("&list=red&list=green&list=blue", "{&list*}", list);
        assertEncodedQueryTemplateExpansion("&keys=semi,%3B,dot,.,comma,%2C", "{&keys}", new Object[]{keys});
        assertEncodedQueryTemplateExpansion("&semi=%3B&dot=.&comma=%2C", "{&keys*}", new Object[]{keys});
    }

    private void assertEncodedQueryTemplateExpansion(final String expectedExpansion,
                                                     final String queryTemplate,
                                                     final Object... values) {
        assertEquals(expectedExpansion,
                UriTemplate.createURI(null, null, null, null, null, null, queryTemplate, null, values, true, false),
                "Unexpected encoded query template expansion result.");
    }

    private void assertEncodedQueryTemplateExpansion(final String expectedExpansion,
                                                     final String queryTemplate,
                                                     final Map<String, ?> values) {
        assertEquals(expectedExpansion,
                UriTemplate.createURI(null, null, null, null, null, null, queryTemplate, null, values, true, false),
                "Unexpected encoded query template expansion result.");
    }

    @Test
    public void testRfc6570MatrixTemplateExamples() {
        /*
            RFC 6570, section 3.2.7:

               {;who}             ;who=fred
               {;half}            ;half=50%25
               {;empty}           ;empty
               {;v,empty,who}     ;v=6;empty;who=fred
               {;v,bar,who}       ;v=6;who=fred
               {;x,y}             ;x=1024;y=768
               {;x,y,empty}       ;x=1024;y=768;empty
               {;x,y,undef}       ;x=1024;y=768
               {;hello:5}         ;hello=Hello
               {;list}            ;list=red,green,blue
               {;list*}           ;list=red;list=green;list=blue
               {;keys}            ;keys=semi,%3B,dot,.,comma,%2C
               {;keys*}           ;semi=%3B;dot=.;comma=%2C
       */
        assertEncodedPathTemplateExpansion(";who=fred", "{;who}", who);
        assertEncodedPathTemplateExpansion(";half=50%25", "{;half}", half);
        assertEncodedPathTemplateExpansion(";empty", "{;empty}", empty);
        assertEncodedPathTemplateExpansion(";v=6;empty;who=fred", "{;v,empty,who}", v, empty, who);
        assertEncodedPathTemplateExpansion(";v=6;who=fred", "{;v,bar,who}", new HashMap<String, String>() {{
            put("v", v);
            put("who", who);
        }});
        assertEncodedPathTemplateExpansion(";x=1024;y=768", "{;x,y}", x, y);
        assertEncodedPathTemplateExpansion(";x=1024;y=768;empty", "{;x,y,empty}", x, y, empty);
        assertEncodedPathTemplateExpansion(";x=1024;y=768", "{;x,y,undef}", x, y);
        assertEncodedPathTemplateExpansion(";hello=Hello", "{;hello:5}", hello);
        assertEncodedPathTemplateExpansion(";list=red,green,blue", "{;list}", list);
        assertEncodedPathTemplateExpansion(";list=red;list=green;list=blue", "{;list*}", list);
        assertEncodedPathTemplateExpansion(";keys=semi,%3B,dot,.,comma,%2C", "{;keys}", new Object[]{keys});
        assertEncodedPathTemplateExpansion(";semi=%3B;dot=.;comma=%2C", "{;keys*}", new Object[]{keys});
    }

    @Test
    void testRfc6570DefaultTemplateExamples() {
        /*
            RFC 6570, section 3.2.2
               {var}              value
               {hello}            Hello%20World%21
               {half}             50%25
               O{empty}X          OX
               O{undef}X          OX
               {x,y}              1024,768
               {x,hello,y}        1024,Hello%20World%21,768
               ?{x,empty}         ?1024,
               ?{x,undef}         ?1024
               ?{undef,y}         ?768
               {var:3}            val
               {var:30}           value
               {list}             red,green,blue
               {list*}            red,green,blue
               {keys}             semi,%3B,dot,.,comma,%2C
               {keys*}            semi=%3B,dot=.,comma=%2C
         */

        // TODO assertEncodedPathTemplateExpansion("Hello%20World%21", "{hello}", hello); // conflicts with rfc3986 Path
        assertEncodedPathTemplateExpansion("50%25", "{half}", half);
        assertEncodedPathTemplateExpansion("0X", "0{empty}X", empty);
        // TODO assertEncodedPathTemplateExpansion("0X", "0{undef}X"); // conflicts with UriBuilder
        // TODO assertEncodedPathTemplateExpansion("1024,Hello%20World%21,768", "{x,hello,y}", x, hello, y); //Path is {+}
        assertEncodedPathTemplateExpansion("?1024,", "?{x,empty}", x, empty);
        // TODO assertEncodedPathTemplateExpansion("?1024", "?{x,undef}", x); // conflicts with UriBuilder
        assertEncodedPathTemplateExpansion("val", "{var:3}", var);
        assertEncodedPathTemplateExpansion("value", "{var:30}", var);
        assertEncodedPathTemplateExpansion("red,green,blue", "{list}", list);
        // TODO assertEncodedPathTemplateExpansion("semi,%3B,dot,.,comma,%2C", "{keys}", keys);
        // TODO assertEncodedPathTemplateExpansion("semi=%3B,dot=.,comma=%2C", "{keys*}", keys);

        // TODO Proprietary minus template
//        assertEncodedPathTemplateExpansion("Hello%20World%21", "{-hello}", hello);
//        assertEncodedPathTemplateExpansion("50%25", "{-half}", half);
//        assertEncodedPathTemplateExpansion("0X", "0{-empty}X", empty);
//        assertEncodedPathTemplateExpansion("0X", "0{-undef}X");
//        assertEncodedPathTemplateExpansion("1024,Hello%20World%21,768", "{-x,hello,y}", x, hello, y);
//        assertEncodedPathTemplateExpansion("?1024,", "?{-x,empty}", x, empty);
//        assertEncodedPathTemplateExpansion("?1024", "?{-x,undef}", x);
//        assertEncodedPathTemplateExpansion("val", "{-var:3}", var);
//        assertEncodedPathTemplateExpansion("value", "{-var:30}", var);
//        assertEncodedPathTemplateExpansion("red,green,blue", "{-list}", list);
//        assertEncodedPathTemplateExpansion("semi,%3B,dot,.,comma,%2C", "{-keys}", new Object[]{keys});
//        assertEncodedPathTemplateExpansion("semi=%3B,dot=.,comma=%2C", "{-keys*}", new Object[]{keys});
    }

    @Test
    void testRfc6570PlusTemplateExamples() {
        /*
            RFC 6570, section 3.2.3
               {+var}                value
               {+hello}              Hello%20World!
               {+half}               50%25

               {base}index           http%3A%2F%2Fexample.com%2Fhome%2Findex
               {+base}index          http://example.com/home/index
               O{+empty}X            OX
               O{+undef}X            OX

               {+path}/here          /foo/bar/here
               here?ref={+path}      here?ref=/foo/bar
               up{+path}{var}/here   up/foo/barvalue/here
               {+x,hello,y}          1024,Hello%20World!,768
               {+path,x}/here        /foo/bar,1024/here

               {+path:6}/here        /foo/b/here
               {+list}               red,green,blue
               {+list*}              red,green,blue
               {+keys}               semi,;,dot,.,comma,,
               {+keys*}              semi=;,dot=.,comma=,
         */
        assertEncodedPathTemplateExpansion("Hello%20World!", "{+hello}", hello);
        assertEncodedPathTemplateExpansion("50%25", "{+half}", half);
        assertEncodedPathTemplateExpansion("50%25", "{+half}", half);
//        assertEncodedPathTemplateExpansion("http%3A%2F%2Fexample.com%2Fhome%2Findex", "{-base}index", base);
        assertEncodedPathTemplateExpansion("http://example.com/home/index", "{+base}index", base);
        assertEncodedPathTemplateExpansion("/foo/bar/here", "{+path}/here", path);
        assertEncodedPathTemplateExpansion("here?ref=/foo/bar", "here?ref={+path}", path);
        assertEncodedPathTemplateExpansion("up/foo/barvalue/here", "up{+path}{var}/here", path, var);
        assertEncodedPathTemplateExpansion("1024,Hello%20World!,768", "{+x,hello,y}", x, hello, y);
        assertEncodedPathTemplateExpansion("/foo/bar,1024/here", "{+path,x}/here", path, x);
        assertEncodedPathTemplateExpansion("/foo/b/here", "{+path:6}/here", path);
        assertEncodedPathTemplateExpansion("red,green,blue", "{+list}", list);
        assertEncodedPathTemplateExpansion("red,green,blue", "{+list*}", list);
        assertEncodedPathTemplateExpansion("semi,;,dot,.,comma,,", "{+keys}", new Object[]{keys});
        assertEncodedPathTemplateExpansion("semi=;,dot=.,comma=,", "{+keys*}", new Object[]{keys});
    }

    @Test
    void testRfc6570HashTemplateExamples() {
        /*
            RFC 6570, section 3.2.4
               {#var}             #value
               {#hello}           #Hello%20World!
               {#half}            #50%25
               foo{#empty}        foo#
               foo{#undef}        foo
               {#x,hello,y}       #1024,Hello%20World!,768
               {#path,x}/here     #/foo/bar,1024/here
               {#path:6}/here     #/foo/b/here
               {#list}            #red,green,blue
               {#list*}           #red,green,blue
               {#keys}            #semi,;,dot,.,comma,,
               {#keys*}           #semi=;,dot=.,comma=,
         */
        assertEncodedPathTemplateExpansion("#Hello%20World!", "{#hello}", hello);
        assertEncodedPathTemplateExpansion("#50%25", "{#half}", half);
        assertEncodedPathTemplateExpansion("0#X", "0{#empty}X", empty);
        assertEncodedPathTemplateExpansion("0X", "0{#undef}X");
        assertEncodedPathTemplateExpansion("#1024,Hello%20World!,768", "{#x,hello,y}", x, hello, y);
        assertEncodedPathTemplateExpansion("#/foo/bar,1024/here", "{#path,x}/here", path, x);
        assertEncodedPathTemplateExpansion("#/foo/b/here", "{#path:6}/here", path);
        assertEncodedPathTemplateExpansion("#red,green,blue", "{#list}", list);
        assertEncodedPathTemplateExpansion("#red,green,blue", "{#list*}", list);
        assertEncodedPathTemplateExpansion("#semi,;,dot,.,comma,,", "{#keys}", new Object[]{keys});
        assertEncodedPathTemplateExpansion("#semi=;,dot=.,comma=,", "{#keys*}", new Object[]{keys});
    }

    @Test
    void testRfc6570DotTemplateExamples() {
        /*
            RFC 6570, section 3.2.5
               {.who}             .fred
               {.who,who}         .fred.fred
               {.half,who}        .50%25.fred
               www{.dom*}         www.example.com
               X{.var}            X.value
               X{.empty}          X.
               X{.undef}          X
               X{.var:3}          X.val
               X{.list}           X.red,green,blue
               X{.list*}          X.red.green.blue
               X{.keys}           X.semi,%3B,dot,.,comma,%2C
               X{.keys*}          X.semi=%3B.dot=..comma=%2C
               X{.empty_keys}     X
               X{.empty_keys*}    X
         */
        assertEncodedPathTemplateExpansion(".fred", "{.who}", who);
        assertEncodedPathTemplateExpansion(".fred.fred", "{.who,who}", who);
        assertEncodedPathTemplateExpansion(".50%25.fred", "{.half,who}", half, who);
        assertEncodedPathTemplateExpansion("www.example.com", "www{.dom*}", dom);
        assertEncodedPathTemplateExpansion("X.value", "X{.var}", var);
        assertEncodedPathTemplateExpansion("X.", "X{.empty}", empty);
        assertEncodedPathTemplateExpansion("X", "X{.undef}");
        assertEncodedPathTemplateExpansion("X.val", "X{.var:3}", var);
        assertEncodedPathTemplateExpansion("X.red,green,blue", "X{.list}", list);
        assertEncodedPathTemplateExpansion("X.red.green.blue", "X{.list*}", list);
        assertEncodedPathTemplateExpansion("X.semi,%3B,dot,.,comma,%2C", "X{.keys}", new Object[]{keys});
        assertEncodedPathTemplateExpansion("X.semi=%3B.dot=..comma=%2C", "X{.keys*}", new Object[]{keys});
        assertEncodedPathTemplateExpansion("X", "X{.empty_keys}", emptyKeys);
        assertEncodedPathTemplateExpansion("X", "X{.empty_keys*}", emptyKeys);
    }

    @Test
    void testRfc6570SlashTemplateExamples() {
        /*
            RFC 6570, section 3.2.6

                   {/who}             /fred
                   {/who,who}         /fred/fred
                   {/half,who}        /50%25/fred
                   {/who,dub}         /fred/me%2Ftoo
                   {/var}             /value
                   {/var,empty}       /value/
                   {/var,undef}       /value
                   {/var,x}/here      /value/1024/here
                   {/var:1,var}       /v/value
                   {/list}            /red,green,blue
                   {/list*}           /red/green/blue
                   {/list*,path:4}    /red/green/blue/%2Ffoo
                   {/keys}            /semi,%3B,dot,.,comma,%2C
                   {/keys*}           /semi=%3B/dot=./comma=%2C
         */
        assertEncodedPathTemplateExpansion("/fred", "{/who}", who);
        assertEncodedPathTemplateExpansion("/fred/fred", "{/who,who}", who);
        assertEncodedPathTemplateExpansion("/50%25/fred", "{/half,who}", half, who);
        assertEncodedPathTemplateExpansion("/fred/me%2Ftoo", "{/who,dub}", who, dub);
        assertEncodedPathTemplateExpansion("/value", "{/var}", var);
        assertEncodedPathTemplateExpansion("/value/", "{/var,empty}", var, empty);
        assertEncodedPathTemplateExpansion("/value", "{/var,undef}", var);
        assertEncodedPathTemplateExpansion("/v/value", "{/var:1,var}", var);
        assertEncodedPathTemplateExpansion("/red,green,blue", "{/list}", list);
        assertEncodedPathTemplateExpansion("/red/green/blue", "{/list*}", list);
        assertEncodedPathTemplateExpansion("/red/green/blue/%2Ffoo", "{/list*,path:4}", list, path);
        assertEncodedPathTemplateExpansion("/semi,%3B,dot,.,comma,%2C", "{/keys}", new Object[]{keys});
        assertEncodedPathTemplateExpansion("/semi=%3B/dot=./comma=%2C", "{/keys*}", new Object[]{keys});
    }

    @Test
    void testRfc6570MultiplePathArgs() {
        _testTemplateNames("/{a,b,c}", "a", "b", "c");
        _testMatching("/uri/{a}", "/uri/hello", "hello");
        _testMatching("/uri/{a,b}", "/uri/hello,world", "hello", "world");
        _testMatching("/uri/{a,b}", "/uri/x", "x", null);
        _testMatching("/uri{?a,b}", "/uri?a=hello&b=world", "hello", "world");
        _testMatching("/uri/{a,b,c}", "/uri/hello,world,!", "hello", "world", "!");
        _testMatching("/uri/{a,b,c}", "/uri/hello,world", "hello", "world", null);
        _testMatching("/uri/{a,b,c}", "/uri/hello", "hello", null, null);
        _testMatching("/uri/{a,b,c}", "/uri/", null, null, null);
    }

    @Test
    public void testRegularExpressionIsNotOptional() {
        Assertions.assertThrows(AssertionFailedError.class,
                () -> _testMatching("/{name: [a-z0-9]{3,128}}", "/", new String[]{null}));
    }

    @Test
    void testRfc6570PathLength() {
        _testMatching("/uri/{a:5}", "/uri/hello", "hello");
        _testMatching("/uri/{a:5,b:6}", "/uri/hello,world!", "hello", "world!");
        assertEncodedPathTemplateExpansion("102,7", "{x:3,y:1}", x, y);
    }

    @Test
    void testInvalidRegexp() {
        _assertMatchingThrowsIAE("/uri/{a**}");
        _assertMatchingThrowsIAE("/uri/{a*a}");
        _assertMatchingThrowsIAE("/uri/{a{");
        _assertMatchingThrowsIAE("/uri/{*}");
        _assertMatchingThrowsIAE("/uri/{}}");
        _assertMatchingThrowsIAE("/uri/{?a:12345}"); //Query knows just length, but the length must be less than 10000
        _assertMatchingThrowsIAE("/uri/{?a:0}");
        _assertMatchingThrowsIAE("/uri/{?a:-1}");
        _assertMatchingThrowsIAE("/uri/{??a}");
        _assertMatchingThrowsIAE("/uri/{--a}");
        _assertMatchingThrowsIAE("/uri/{++a}");
    }

    @Test
    public void ignoreLastComma() {
        UriTemplateParser parser = new UriTemplateParser("/{a,b,}");
        Assertions.assertEquals(2, parser.getNames().size());
    }

    void _assertMatchingThrowsIAE(String uri) {
        try {
            _testMatching(uri, "/uri/hello", "hello");
            throw new IllegalStateException("IllegalArgumentException checking incorrect uri " + uri + " has not been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private void assertEncodedPathTemplateExpansion(final String expectedExpansion,
                                                    final String pathTemplate,
                                                    final Object... values) {
        assertEquals(expectedExpansion,
                UriTemplate.createURI(null, null, null, null, null, pathTemplate, null, null, values, true, false),
                "Unexpected encoded matrix parameter template expansion result.");
    }

    private void assertEncodedPathTemplateExpansion(final String expectedExpansion,
                                                    final String pathTemplate,
                                                    final Map<String, ?> values) {
        assertEquals(expectedExpansion,
                UriTemplate.createURI(null, null, null, null, null, pathTemplate, null, null, values, true, false),
                "Unexpected encoded matrix parameter template expansion result.");
    }
}
