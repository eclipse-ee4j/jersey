[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Declarative Hyperlinking Example
================================

This example demonstrates how to use the declarative hyperlink
annotations.

Contents
--------

The example consists of one resource class and one representation class
along with a couple of model classes:

`org.glassfish.jersey.examples.linking.resources.ItemResource`

A resource class that produces a XML response to an HTTP GET.

`com.sun.jersey.samples.linking.representation.ItemRepresentation`

A JAXB representation class used to produce XML. This class also
contains @Link and @Ref annotations to produce hyperlinks in the
XML document.

The mapping of the URI path space is presented in the following table:

URI path               | Resource class   | HTTP methods
---------------------- | ---------------- | --------------
**_/items/{index}_**   | ItemResource     | GET

Sample Response
---------------

You can access resources of this application also using curl:

>     curl -v http://localhost:8080/items/1

In this example you should see an XML snippet with a next and a previous
link.

```xml
<item>
    <name>Item 1</name>
    <link href="http://localhost:8080/items/1" rel="self"/>
    <links>
        <link href="http://localhost:8080/items/2" rel="next"/>
        <link href="http://localhost:8080/items/0" rel="prev"/>
    </links>
</item>
```

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys current example using Grizzly. You can access the
application at:

-   <http://localhost:8080/items/0>\
-   <http://localhost:8080/items/1>\
-   <http://localhost:8080/items/2>\
-   <http://localhost:8080/items/3>\
-   <http://localhost:8080/items/4>\

Notice how the first has a self link, a next link but no previous link,
the second, third and fourth have all three and the last one only has a
self link and prev link.

Observe the HTTP headers included in the responses (e.g. via `curl -v`).
Notice that there is a HTTP Link header for next and previous when
appropriate.