/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jersey_ejb.resources;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.examples.jersey_ejb.entities.Message;
import org.glassfish.jersey.examples.jersey_ejb.entities.MessageListWriter;
import org.glassfish.jersey.examples.jersey_ejb.entities.MessageWriter;
import org.glassfish.jersey.examples.jersey_ejb.exceptions.NotFoundExceptionMapper;

/**
 * Main application class.
 *
 * @author Jonathan Benoit
 */
@ApplicationPath("/app/*")
public class MyApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register root resources/providers
        classes.add(MessageBoardRootResource.class);
        classes.add(MessageBoardResourceBean.class);
        classes.add(MessageHolderSingletonBean.class);
        classes.add(NotFoundExceptionMapper.class);
        classes.add(MessageWriter.class);
        classes.add(MessageListWriter.class);
        classes.add(Message.class);
        return classes;
    }
}
