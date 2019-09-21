[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

JAXB Example
============

This example demonstrates how to use JAXB types with resource classes and the JAX-RS Client API.

The following JAXB types are supported and demonstrated:

-   JAXB classes annotated with @XmlRootElement
-   JAXB classes annotated with @XmlType
-   JAXBElement&lt;T&gt; where T is a JAXB xml type that may or may not be annotated with @XmlType
-   List&lt;T&gt;, Collection&lt;T&gt;, and T\[\] where T is a JAXB class annotated with @XmlRootElement or @XmlType

It also shows how to use @XmlHeader annotation to specify the header of the XML responses.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                                | Resource class           | HTTP methods
--------------------------------------- | ------------------------ | --------------
**_/jaxb/XmlRootElement_**              | JaxbResource             | GET, POST
**_/jaxb/XmlRootElementWithHeader_**    | JaxbResource             | GET
**_/jaxb/JAXBElement_**                 | JaxbResource             | GET, POST
**_/jaxb/XmlType_**                     | JaxbResource             | POST
**_/jaxb/collection/XmlRootElement_**   | JaxbCollectionResource   | GET, POST
**_/jaxb/collection/XmlType_**          | JaxbCollectionResource   | POST
**_/jaxb/array/XmlRootElement_**        | JaxbArrayResource        | GET, POST
**_/jaxb/array/XmlType_**               | JaxbArrayResource        | POST

JAXB classes annotated with @XmlRootElement may be produced and
consumed. JAXB classses annotated with @XmlType can only be consumed
hence why only the POST method is supported for the resources associated
with XmlType. This is because the JAXB requires enough information to
serialize the JAXB instance to a well-formed XML document. The same
rules apply to collections of such classes.

For the serialization of a Collection of JAXB type, or an array, Jersey
will automatically derive the XML root element of the XML document to
serialize from the XML name of the JAXB class annoated with
@XmlRootElement.

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container. You can access the application at:

-   <http://localhost:8080/jaxb/jaxb/XmlRootElement>
-   <http://localhost:8080/jaxb/jaxb/XmlRootElementWithHeader>
-   <http://localhost:8080/jaxb/jaxb/JAXBElement>
-   <http://localhost:8080/jaxb/jaxb/collection/XmlRootElement>
-   <http://localhost:8080/jaxb/jaxb/array/XmlRootElement>

or you can post an xml entity:

>    curl -v -X POST http://localhost:8080/jaxb/jaxb/XmlRootElement -H "Content-Type:application/xml" -d '
>    <jaxbXmlRootElement>
>        <value>xml root element</value>
>    </jaxbXmlRootElement>'

You can see test classes at `src/main/test` subdirectory for detailed information how to consume the service using JAX-RS Client API