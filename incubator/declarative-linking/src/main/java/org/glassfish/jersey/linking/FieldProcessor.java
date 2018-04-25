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

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

import javax.xml.bind.annotation.XmlTransient;

import org.glassfish.jersey.linking.contributing.ResourceLinkContributionContext;
import org.glassfish.jersey.linking.mapping.ResourceMappingContext;

/**
 * Utility class that can inject links into {@link org.glassfish.jersey.linking.InjectLink} annotated fields in
 * an entity.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class FieldProcessor<T> {

    private EntityDescriptor instanceDescriptor;
    private static final Logger log = Logger.getLogger(FieldProcessor.class.getName());

    FieldProcessor(Class<T> c) {
        instanceDescriptor = EntityDescriptor.getInstance(c);
    }

    /**
     * Inject any {@link org.glassfish.jersey.linking.InjectLink} annotated fields in the supplied entity and
     * recursively process its fields.
     *
     * @param entity  the entity object returned by the resource method
     * @param uriInfo the uriInfo for the request
     * @param rmc     the ResourceMappingContext used for building URIs
     * @param rlcc    the ResourceLinkContributionContext used to find link contributors
     */
    void processLinks(T entity, UriInfo uriInfo, ResourceMappingContext rmc, ResourceLinkContributionContext rlcc) {
        Set<Object> processed = new HashSet<Object>();
        Object resource = uriInfo.getMatchedResources().get(0);
        processLinks(entity, resource, entity, processed, uriInfo, rmc, rlcc);
    }

    /**
     * Inject any {@link org.glassfish.jersey.linking.InjectLink} annotated fields in the supplied instance. Called
     * once for the entity and then recursively for each member and field.
     *
     * @param entity    the entity object returned by the resource method
     * @param processed a list of already processed objects, used to break
     *                  recursion when processing circular references.
     * @param uriInfo   the uriInfo for the request
     * @param rmc       the ResourceMappingContext used for building URIs
     * @param rlcc      the ResourceLinkContributionContext used to find link contributors
     */
    private void processLinks(Object entity, Object resource, Object instance,
            Set<Object> processed, UriInfo uriInfo,
            ResourceMappingContext rmc, ResourceLinkContributionContext rlcc) {

        try {
            if (instance == null || processed.contains(instance)) {
                return; // ignore null properties and defeat circular references
            }
            if (instance.getClass().getName().startsWith("java.lang")) {
                return;
            }
            processed.add(instance);
        } catch (RuntimeException e) {
            // fix for JERSEY-1656
            log.log(Level.INFO, LinkMessages.WARNING_LINKFILTER_PROCESSING(instance.getClass().getName()), e);
        }

        // Process any @Link annotated fields in entity
        for (FieldDescriptor field : instanceDescriptor.getLinkFields()) {

            // TODO replace with properly poly-morphic code
            if (field instanceof InjectLinkFieldDescriptor) {
                InjectLinkFieldDescriptor linkField = (InjectLinkFieldDescriptor) field;
                if (ELLinkBuilder.evaluateCondition(linkField.getCondition(), entity, resource, instance)) {
                    URI uri = ELLinkBuilder.buildURI(linkField, entity, resource, instance, uriInfo, rmc);
                    linkField.setPropertyValue(instance, uri);
                }
            } else if (field instanceof InjectLinksFieldDescriptor) {

                InjectLinksFieldDescriptor linksField = (InjectLinksFieldDescriptor) field;
                List<Link> list = new ArrayList<>();
                for (InjectLinkFieldDescriptor linkField : linksField.getLinksToInject()) {
                    if (ELLinkBuilder.evaluateCondition(linkField.getCondition(), entity, resource, instance)) {
                       URI uri = ELLinkBuilder.buildURI(linkField, entity, resource, instance, uriInfo, rmc);
                       Link link = linkField.getLink(uri);
                       list.add(link);
                    }
                }
                List<ProvideLinkDescriptor> linkContributors = rlcc.getContributorsFor(instance.getClass());
                for (ProvideLinkDescriptor linkContributor : linkContributors) {
                    if (ELLinkBuilder.evaluateCondition(linkContributor.getCondition(),
                            entity, linkContributor.getResource(), instance)) {
                        URI uri = ELLinkBuilder.buildURI(linkContributor, entity, resource, instance, uriInfo, rmc);
                        Link link = linkContributor.getLink(uri);
                        list.add(link);
                    }
                }

                linksField.setPropertyValue(instance, list);
            }
        }

        // If entity is an array, collection, or map then process members
        Class<?> instanceClass = instance.getClass();
        if (instanceClass.isArray() && Object[].class.isAssignableFrom(instanceClass)) {
            Object array[] = (Object[]) instance;
            for (Object member : array) {
                processMember(entity, resource, member, processed, uriInfo, rmc, rlcc);
            }
        } else if (instance instanceof Iterable) {
            Iterable iterable = (Iterable) instance;
            for (Object member : iterable) {
                processMember(entity, resource, member, processed, uriInfo, rmc, rlcc);
            }
        } else if (instance instanceof Map) {
            Map map = (Map) instance;
            for (Object member : map.entrySet()) {
                processMember(entity, resource, member, processed, uriInfo, rmc, rlcc);
            }
        }

        // Recursively process all member fields
        for (FieldDescriptor member : instanceDescriptor.getNonLinkFields()) {

            if (fieldSuitableForIntrospection(member)) {
                processMember(entity, resource, member.getFieldValue(instance), processed, uriInfo, rmc, rlcc);
            }
        }

    }

    private boolean fieldSuitableForIntrospection(FieldDescriptor member) {
        return member.field == null
                || (!member.field.isSynthetic()
                    && !Modifier.isTransient(member.field.getModifiers())
                    && !member.field.getType().isPrimitive()
                    && member.field.getType() != String.class
                    && !member.field.isAnnotationPresent(InjectLinkNoFollow.class)
                    && !member.field.isAnnotationPresent(XmlTransient.class));
    }

    private void processMember(Object entity, Object resource, Object member, Set<Object> processed, UriInfo uriInfo,
            ResourceMappingContext rmc, ResourceLinkContributionContext rlcc) {
        if (member != null) {
            FieldProcessor<?> proc = new FieldProcessor(member.getClass());
            proc.processLinks(entity, resource, member, processed, uriInfo, rmc, rlcc);
        }
    }

}
