[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Bookstore
=========

This example demonstrates how to connect JSP pages to resources. The
example is copied from the
[Bookstore](http://stapler.kohsuke.org/getting-started.html) example
presented by the [Stapler](http://stapler.kohsuke.org/) project that
provides a way to staple URIs to Java classes to build RESTful Web
applications such as [Hudson](http://java.net/projects/hudson/).

A bookstore Web application is presented that is capable of presenting
books, CDs and tracks of CDs.

Contents
--------

The example consists of four web resources implemented by the following:

`org.glassfish.jersey.examples.bookstore.webapp.resource.Bookstore`

The bookstore resource that returns a list of items, either CDs
or Books. The resource dynamically references a Book or CD resource
using the getItem method that is annotated with Path. The Book and
CD resource both inherit from the Item class and thus the resources
can be managed polymorphically.

`org.glassfish.jersey.examples.bookstore.webapp.resource.Book`

The book resource that has a title and an author.

`org.glassfish.jersey.examples.bookstore.webapp.resource.CD`

The CD resource that has a title, author and a list of tracks. The
resource dynamically references the Track resource using the
getTrack method that is annotated with Path.

`org.glassfish.jersey.examples.bookstore.webapp.resource.Track`

The Tracks resource that has a name and the length of the track.

The mapping of the URI path space is presented in the following table:


URI path                            | Resource class   | HTTP methods   | Notes
----------------------------------- | ---------------- | -------------- | ------------------------------------------------------------------------------
**_/_**                             | Bookstore        | GET            | webapp/resource/Bookstore/index.jsp
**_/count_**                        | Bookstore        | GET            | webapp/resource/Bookstore/count.jsp
**_/time_**                         | Bookstore        | GET            | webapp/resource/Bookstore/time.jsp
**_/items/{itemid}_**               | Book, CD         | GET            | webapp/resource/Book/index.jsp, webapp/resource/CD/index.jsp
**_/items/{itemid}/tracks/{num}_**  | Track            | GET            | webapp/resource/Track/index.jsp

Running the Example
-------------------

Bookstore example runs on Glassfish 4.0 application server
([https://javaee.github.io/glassfish/](https://javaee.github.io/glassfish/)), which can be
run as an embedded container or Jetty.

If you are working with Jersey GlassFish update center module installed
into your existing GlassFish instance, you will need to follow
instructions at [the module README file](../../README.html) in order to
deploy the example.

Otherwise, you can run the example using embedded GlassFish as follows:

Build and deploy the project by executing the following command from the
project directory

>   mvn clean package embedded-glassfish:run (TODO does not work at the moment)

or you can run the example using Jetty as follows:

>   mvn clean package jetty:run

Go to the URL:

<http://localhost:8080/bookstore-webapp/>

#### Test Client Running

>   mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.external.ExternalTestContainerFactory -Djersey.config.test.logging.enable=true -Djersey.config.test.logging.dumpEntity=true -Djersey.config.test.container.port=8080 -Dmaven.test.skip=false

or

>   mvn test -Prun-external-tests

How it works
------------

This example shows how to support polymorphism of resources and JSP
pages. Hence it is possible to add another resource, such as a DVD
resource with associated JSP pages, which extends Item without having to
change the logic of Bookstore or the existing JSP pages.

JSP pages are associated with resource classes. Such JSP pages are
resolved by converting the fully qualified class name of the resource
class into a path and appending last path segment of the request URI
path to that path. For example, when a GET is performed on the URI path
"/" then the path to the JSP page is
"/org/glassfish/jersey/examples/bookstore/webapp/resource/Bookstore/",
and in this case since the last path segment is empty "index.jsp" is
appended to the path. Then the request us forwarded to the JSP page at
that path. Similarly when a GET is performed on the URI path "/count"
then the path to the JSP page is
"//org/glassfish/jersey/examples/bookstore/webapp/resource/Bookstore/count.jsp".

The JSP variable "it" is set to the instance of Bookstore so that the
index.jsp, or count.jsp, has access to the Bookstore instance as a Java
bean.

If a resource class inherits from another resource class then it will
automatically inherit the JSPs from the super class.

A JSP page may also include JSPs using the inheritance mechanism, for
example the index.jsp page associated with the Book resource class
includes a footer.jsp page whose location is specified by the super
class, Item.
