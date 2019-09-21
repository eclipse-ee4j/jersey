/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.examples.entityfiltering.filtering.ProjectDetailedView;

/**
 * Project entity class. Fields {@code tasks} and {@code users} are available only in detailed view (defined via
 * {@link ProjectDetailedView}).
 *
 * @author Michal Gajdos
 */
@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
@XmlRootElement
public class Project {

    private Long id;

    private String name;

    private String description;

    @ProjectDetailedView
    private List<Task> tasks;

    @ProjectDetailedView
    private List<User> users;

    public Project() {
    }

    public Project(final Long id, final String name, final String description) {
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

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(final List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(final List<User> users) {
        this.users = users;
    }
}
