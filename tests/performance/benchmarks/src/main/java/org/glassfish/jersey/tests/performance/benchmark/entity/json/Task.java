/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.benchmark.entity.json;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Task entity class. Fields {@code project} and {@code user} are available only in detailed view (defined via
 * {@link TaskDetailedView}).
 *
 * @author Michal Gajdos
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@XmlRootElement
public class Task {

    private Long id;

    private String name;

    private String description;

    @TaskDetailedView
    private Project project;

    @TaskDetailedView
    private User user;

    public Task() {
    }

    public Task(final Long id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }
}
