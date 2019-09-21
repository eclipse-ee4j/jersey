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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity-store utility class. Class creates a sample instance of each entity.
 *
 * @author Michal Gajdos
 */
@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
public final class EntityStore {

    private static final Map<Long, Project> PROJECTS = new LinkedHashMap<>();
    private static final Map<Long, User> USERS = new LinkedHashMap<>();
    private static final Map<Long, Task> TASKS = new LinkedHashMap<>();

    static {
        // Projects.
        final Project project = createProject("Jersey", "Jersey is the open source (see NOTICE.md for license "
                + "information) JAX-RS 2.1 (JSR 370) production quality Reference Implementation for building RESTful Web "
                + "services.");

        // Users.
        final User robot = createUser("Jersey Robot", "very@secret.com");

        // Tasks.
        final Task filtering = createTask("ENT_FLT", "Entity Data Filtering");
        final Task oauth = createTask("OAUTH", "OAuth 1 + 2");

        // Project -> Users, Tasks.
        add(project, robot);
        filtering.setProject(project);
        oauth.setProject(project);

        // Users -> Projects, Tasks.
        add(robot, project);
        filtering.setUser(robot);
        oauth.setUser(robot);

        // Tasks -> Projects, Users.
        add(filtering, project);
        add(oauth, project);
        add(filtering, robot);
        add(oauth, robot);
    }

    public static void add(final Project project, final User user) {
        user.getProjects().add(project);
    }

    public static void add(final User user, final Project project) {
        project.getUsers().add(user);
    }

    public static void add(final Task task, final User user) {
        user.getTasks().add(task);
    }

    public static void add(final Task task, final Project project) {
        project.getTasks().add(task);
    }

    public static Project createProject(final String name, final String description) {
        return createProject(name, description, null, null);
    }

    public static Project createProject(final String name, final String description, final List<User> users,
                                        final List<Task> tasks) {
        final Project project = new Project(PROJECTS.size() + 1L, name, description);

        project.setTasks(tasks == null ? new ArrayList<Task>() : tasks);
        project.setUsers(users == null ? new ArrayList<User>() : users);
        PROJECTS.put(project.getId(), project);

        return project;
    }

    public static User createUser(final String name, final String email) {
        return createUser(name, email, null, null);
    }

    public static User createUser(final String name, final String email, final List<Project> projects, final List<Task> tasks) {
        final User user = new User(USERS.size() + 1L, name, email);

        user.setProjects(projects == null ? new ArrayList<Project>() : projects);
        user.setTasks(tasks == null ? new ArrayList<Task>() : tasks);
        USERS.put(user.getId(), user);

        return user;
    }

    public static Task createTask(final String name, final String description) {
        return createTask(name, description, null, null);
    }

    public static Task createTask(final String name, final String description, final Project project, final User user) {
        final Task task = new Task(TASKS.size() + 1L, name, description);

        task.setProject(project);
        task.setUser(user);
        TASKS.put(task.getId(), task);

        return task;
    }

    public static Project getProject(final Long id) {
        return PROJECTS.get(id);
    }

    public static User getUser(final Long id) {
        return USERS.get(id);
    }

    public static Task getTask(final Long id) {
        return TASKS.get(id);
    }

    public static List<Project> getProjects() {
        return new ArrayList<>(PROJECTS.values());
    }

    public static List<User> getUsers() {
        return new ArrayList<>(USERS.values());
    }

    public static List<Task> getTasks() {
        return new ArrayList<>(TASKS.values());
    }

    /**
     * Prevent instantiation.
     */
    private EntityStore() {
    }
}
