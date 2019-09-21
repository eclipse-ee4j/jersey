/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.selectable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Feature;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.examples.entityfiltering.selectable.domain.Address;
import org.glassfish.jersey.examples.entityfiltering.selectable.domain.Person;
import org.glassfish.jersey.examples.entityfiltering.selectable.domain.PhoneNumber;
import org.glassfish.jersey.examples.entityfiltering.selectable.resource.PersonResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.filtering.SelectableEntityFilteringFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link PersonResource} unit tests.
 *
 * @author Andy Pemberton (pembertona at gmail.com)
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class PersonResourceTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][]{{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    private final Class<Feature> filteringProvider;

    public PersonResourceTest(final Class<Feature> filteringProvider) {
        super(new ResourceConfig(SelectableEntityFilteringFeature.class)
                .packages("org.glassfish.jersey.examples.entityfiltering.selectable")
                .property(SelectableEntityFilteringFeature.QUERY_PARAM_NAME, "select")
                .register(filteringProvider));

        this.filteringProvider = filteringProvider;

        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(filteringProvider);
    }

    @Test
    public void testNoFilter() throws Exception {
        final Person entity = target("people").path("1234").request().get(Person.class);

        // Not null values.
        assertThat(entity.getFamilyName(), notNullValue());
        assertThat(entity.getGivenName(), notNullValue());
        assertThat(entity.getHonorificPrefix(), notNullValue());
        assertThat(entity.getHonorificSuffix(), notNullValue());
        assertThat(entity.getRegion(), notNullValue());

        final List<Address> addresses = entity.getAddresses();
        assertThat(addresses, notNullValue());
        final Address address = addresses.get(0);
        assertThat(address, notNullValue());
        assertThat(address.getRegion(), notNullValue());
        assertThat(address.getStreetAddress(), notNullValue());
        PhoneNumber phoneNumber = address.getPhoneNumber();
        assertThat(phoneNumber, notNullValue());
        assertThat(phoneNumber.getAreaCode(), notNullValue());
        assertThat(phoneNumber.getNumber(), notNullValue());

        final Map<String, PhoneNumber> phoneNumbers = entity.getPhoneNumbers();
        assertThat(phoneNumbers, notNullValue());

        // TODO: enable for MOXy as well when JERSEY-2751 gets fixed.
        if (JacksonFeature.class.isAssignableFrom(filteringProvider)) {
            phoneNumber = phoneNumbers.get("HOME");
            assertThat(phoneNumber, notNullValue());
            assertThat(phoneNumber.getAreaCode(), notNullValue());
            assertThat(phoneNumber.getNumber(), notNullValue());
        }
    }

    @Test
    public void testInvalidFilter() throws Exception {
        final Person entity = target("people").path("1234")
                .queryParam("select", "invalid").request().get(Person.class);

        // All null values.
        assertThat(entity.getFamilyName(), nullValue());
        assertThat(entity.getGivenName(), nullValue());
        assertThat(entity.getHonorificPrefix(), nullValue());
        assertThat(entity.getHonorificSuffix(), nullValue());
        assertThat(entity.getRegion(), nullValue());
        assertThat(entity.getAddresses(), nullValue());
        assertThat(entity.getPhoneNumbers(), nullValue());
    }

    /**
     * Test first level filters.
     */
    @Test
    public void testFilters() throws Exception {
        final Person entity = target("people").path("1234")
                .queryParam("select", "familyName,givenName").request()
                .get(Person.class);

        // Not null values.
        assertThat(entity.getFamilyName(), notNullValue());
        assertThat(entity.getGivenName(), notNullValue());

        // Null values.
        assertThat(entity.getAddresses(), nullValue());
        assertThat(entity.getPhoneNumbers(), nullValue());
        assertThat(entity.getRegion(), nullValue());
    }

    /**
     * Test empty (but valid) filters.
     * Valid empty filters are:
     *  . ,. , .. .,
     *
     *
     * result is empty object (nothing is returned) but Jersey will not throw any exception
     */
    @Test
    public void testEmptyFilters() throws Exception {
        final Person entity = target("people").path("1234")
                .queryParam("select", ".").request()
                .get(Person.class);

        // Null values (all elements).
        assertThat(entity.getFamilyName(), nullValue());
        assertThat(entity.getGivenName(), nullValue());
        assertThat(entity.getAddresses(), nullValue());
        assertThat(entity.getPhoneNumbers(), nullValue());
        assertThat(entity.getRegion(), nullValue());
    }

    /**
     * Test 2nd and 3rd level filters.
     */
    @Test
    public void testSubFilters() throws Exception {
        final Person entity = target("people")
                .path("1234")
                .queryParam("select",
                        "familyName,givenName,addresses.streetAddress,addresses.phoneNumber.areaCode")
                .request().get(Person.class);

        // Not null values.
        assertThat(entity.getFamilyName(), notNullValue());
        assertThat(entity.getGivenName(), notNullValue());
        assertThat(entity.getAddresses().get(0).getStreetAddress(), notNullValue());
        assertThat(entity.getAddresses().get(0).getPhoneNumber().getAreaCode(), notNullValue());

        // Null values.
        assertThat(entity.getRegion(), nullValue());
        assertThat(entity.getAddresses().get(0).getPhoneNumber().getNumber(), nullValue());
    }

    /**
     * Test that 1st and 2nd level filters with the same name act as expected.
     */
    @Test
    public void testFiltersSameName() throws Exception {
        final Person firstLevel = target("people").path("1234")
                .queryParam("select", "familyName,region").request()
                .get(Person.class);
        final Person secondLevel = target("people").path("1234")
                .queryParam("select", "familyName,addresses.region").request()
                .get(Person.class);

        // Not null values.
        assertThat(firstLevel.getRegion(), notNullValue());
        assertThat(secondLevel.getAddresses().get(0).getRegion(), notNullValue());

        // Null values.
        assertThat(firstLevel.getAddresses(), nullValue()); //confirms 2nd level region on addresses is null
        assertThat(secondLevel.getRegion(), nullValue());
    }

}
