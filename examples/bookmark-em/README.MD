[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Bookmark EM Example
===================

This example demonstrates how to use JPA in the backend. The example is
based on bookmark example from the [RESTful Web
Services](http://www.oreilly.com/catalog/9780596529260/) book, which was
inspired by the [http://del.icio.us/](http://del.icio.us/v1) web
service.

A bookmark web application is presented that is capable of maintaining
users and their bookmarks.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                                 | Resource class      | HTTP methods                                          | Notes
---------------------------------------- | ------------------- | ----------------------------------------------------- | --------------------------------------------------------
**_/users_**                             | UsersResource       | GET                                                   | Returns a list of users.
**_/users/{userid}_**                    | UserResource        | GET, PUT (used also for creating new users), DELETE   | Returns user details
**_/users/{userid}/bookmarks_**          | BookmarksResource   | GET, POST                                             | Returns a list of bookmarks for actual user resource.
**_/users/{userid}/bookmarks/{bmid}_**   | BookmarkResource    | GET, PUT, DELETE                                      | Returns bookmark uri and a long and short description.

Running the Example
-------------------

Bookmark example runs on Glassfish 3.1 application server
([https://javaee.github.io/glassfish/](https://javaee.github.io/glassfish/)) and needs a running JavaDB
(<http://www.oracle.com/technetwork/java/javadb/overview/index.html>)
instance for underlying data (it comes along with GlassFish 3.1).

Presuming, you have installed Glassfish 3.1. `AS_HOME` variable should
point to your glassfish installation directory.

#### Building And Starting The Bookmark Service

You then build and run the example by

    mvn package
    $AS_HOME/bin/asadmin start-domain
    $AS_HOME/bin/asadmin start-database
    $AS_HOME/bin/asadmin deploy target/bookmark-em.war

#### Test Client Running

    mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.external.ExternalTestContainerFactory -Djersey.config.test.logging.enable=true -Djersey.config.test.logging.dumpEntity=true -Djersey.config.test.container.port=8080 -Ptest

Links
-----

<http://java.net/projects/bpcatalog/sources/svn/content/trunk/ee5/docs/persistence/webonlyapp.html?raw=true>
