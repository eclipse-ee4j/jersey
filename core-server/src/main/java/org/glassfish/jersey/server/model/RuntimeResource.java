/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.glassfish.jersey.uri.PathPattern;

/**
 * Runtime resource is a group of {@link Resource resources} with the same {@link Resource#getPath() path}
 * regular expression. Runtime resource is constructed from {@link Resource resources} creating
 * the {@link ResourceModel resource model}.
 * <p/>
 * Runtime resource can have child runtime resources which are groups of child resources of all resources constructing this
 * runtime resource.
 * <p/>
 * The following example shows how Runtime resource structure is built from Resource model:
 * <pre>
 * &#064;Path("{foo}")
 * public class TemplateResourceFoo {
 *     &#064;GET
 *     &#064;Path("child")
 *     public String getFoo() {...}
 *
 *     &#064;Path("{x}")
 *     &#064;GET
 *     public String getX() {...}
 *
 *     &#064;Path("{y}")
 *     &#064;POST
 *     public String postY(String entity) {...}
 * }
 *
 * &#064;Path("{bar}")
 * public class TemplateResourceBar {
 *     &#064;Path("{z}")
 *     &#064;PUT
 *     public String putZ(String entity) {...}
 * }
 * </pre>
 *
 * Will be represented by RuntimeResources:
 * <table border="1">
 * <tr>
 * <th>line</th>
 * <th>RuntimeResource regex</th>
 * <th>Grouped Resources (paths)</th>
 * <th>Parent RuntimeResource (line)</th>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>"/([^/]+?)"</td>
 * <td>Resource("{foo}"), Resource("{bar}")</td>
 * <td>no parent</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>"child"</td>
 * <td>Child Resource("child")</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>"/([^/]+?)"</td>
 * <td>Child Resource("{x}"), Child Resource("{y}"), Child Resource("{z}")</td>
 * <td>1</td>
 * </tr>
 * </table>
 *
 * @author Miroslav Fuksa
 */
public class RuntimeResource implements ResourceModelComponent {
    /**
     * Runtime Resource builder.
     */
    static class Builder {
        private final List<Resource> resources;
        private final String regex;
        private final List<RuntimeResource.Builder> childRuntimeResourceBuilders;


        /**
         * Create new {@link RuntimeResource runtime resource} builder instance.
         *
         * @param resources                    List of resources with same regex that creates a RuntimeResource.
         * @param childRuntimeResourceBuilders List of builders of child runtime resources that belong runtime resource.
         * @param regex                        Path regular expression.
         */
        public Builder(List<Resource> resources, List<Builder> childRuntimeResourceBuilders, String regex) {
            this.childRuntimeResourceBuilders = childRuntimeResourceBuilders;
            this.resources = resources;
            this.regex = regex;
        }


        /**
         * Build new RuntimeResource from this builder.
         *
         * @param parent Parent runtime resource.
         * @return New RuntimeResource instance.
         */
        public RuntimeResource build(RuntimeResource parent) {
            return new RuntimeResource(resources, childRuntimeResourceBuilders, parent, regex);
        }
    }

    /**
     * Comparator of RuntimeResources based on rules respecting resource matching algorithm.
     */
    public static final Comparator<RuntimeResource> COMPARATOR = new Comparator<RuntimeResource>() {
        @Override
        public int compare(RuntimeResource o1, RuntimeResource o2) {
            final int cmp = PathPattern.COMPARATOR.compare(o1.getPathPattern(), o2.getPathPattern());
            if (cmp == 0) {
                // quaternary key sorting those derived from
                // sub-resource methods ahead of those derived from sub-resource locators
                final int locatorCmp = o1.resourceLocators.size() - o2.resourceLocators.size();

                // compare the regexes if still equal
                return (locatorCmp == 0) ? o2.regex.compareTo(o1.regex) : locatorCmp;
            } else {
                return cmp;
            }
        }
    };

    private final String regex;
    private final List<ResourceMethod> resourceMethods;
    private final List<ResourceMethod> resourceLocators;
    private final List<RuntimeResource> childRuntimeResources;
    private final List<Resource> resources;

    private final RuntimeResource parent;
    private final PathPattern pathPattern;


    private RuntimeResource(List<Resource> resources,
                            List<Builder> childRuntimeResourceBuilders,
                            RuntimeResource parent,
                            String regex) {
        this.parent = parent;
        this.pathPattern = resources.get(0).getPathPattern();

        this.resources = new ArrayList<>(resources);

        this.regex = regex;
        this.resourceMethods = new ArrayList<>();
        this.resourceLocators = new ArrayList<>();
        this.childRuntimeResources = new ArrayList<>();
        for (Builder childRuntimeResourceBuilder : childRuntimeResourceBuilders) {
            this.childRuntimeResources.add(childRuntimeResourceBuilder.build(this));
        }
        Collections.sort(this.childRuntimeResources, COMPARATOR);

        for (final Resource res : this.resources) {
            this.resourceMethods.addAll(res.getResourceMethods());

            final ResourceMethod resourceLocator = res.getResourceLocator();
            if (resourceLocator != null) {
                this.resourceLocators.add(resourceLocator);
            }
        }
    }

    /**
     * Get child runtime resources of this resource.
     *
     * @return List of child runtime resource.
     */
    public List<RuntimeResource> getChildRuntimeResources() {
        return childRuntimeResources;
    }

    /**
     * Get regular expression of path pattern of this runtime resource.
     *
     * @return Matching regular expression.
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Get resource methods (excluding resource locators) of all {@link Resource resources} of this runtime resource.
     *
     * @return List of resource methods.
     */
    public List<ResourceMethod> getResourceMethods() {
        return resourceMethods;
    }

    /**
     * Get resource locators of all {@link Resource resources} of this runtime resource.
     * <p/>
     * Note that valid RuntimeResource should have only one resource locator. This method is used for validation purposes.
     *
     * @return List of resource locators.
     */
    public List<ResourceMethod> getResourceLocators() {
        return resourceLocators;
    }

    /**
     * Return the resource locator of this resource.
     *
     * @return Resource locator of this runtime resource.
     */
    public ResourceMethod getResourceLocator() {
        if (resourceLocators.size() >= 1) {
            return resourceLocators.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get parent of this runtime resource.
     *
     * @return Parent runtime resource if this runtime resource is a child resource, null otherwise.
     */
    public RuntimeResource getParent() {
        return parent;
    }

    /**
     * Get path pattern for matching purposes.
     *
     * @return Path pattern.
     */
    public PathPattern getPathPattern() {
        return pathPattern;
    }

    /**
     * Get full regular expression of this runtime resource prefixed by regular expression of parent if present.
     *
     * @return Full resource regular expression.
     */
    public String getFullPathRegex() {
        if (parent == null) {
            return regex;
        } else {
            return parent.getRegex() + regex;
        }
    }

    /**
     * Return parent {@link Resource resources} of {@link Resource resources} from this runtime resource. The returned list
     * is ordered so that the position of the parent resource in the returned list is the same as position of its child resource
     * in list returned by {@link #getResources()}. Simply said the order of lists returned
     * from {@code getParentResources()} and {@link #getResources()} from parent-child point of view is the same. If the resource
     * has no parent then the element {@code null} is in the list.
     *
     * @return Parent resource list with resources if this runtime resource is child resource or {@code null} elements if
     *         this runtime resource is the parent resource.
     */
    public List<Resource> getParentResources() {
        return resources.stream().map(child -> (child == null) ? null : child.getParent()).collect(Collectors.toList());
    }

    /**
     * Get resources creating this runtime resource.
     *
     * @return List of resources with same path regular expression which this resource is based on.
     */
    public List<Resource> getResources() {
        return resources;
    }

    @Override
    public void accept(ResourceModelVisitor visitor) {
        visitor.visitRuntimeResource(this);
    }

    @Override
    public List<? extends ResourceModelComponent> getComponents() {
        return getChildRuntimeResources();
    }
}

