[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

MOXy Example
============

What is MOXy
------------

This example demonstrates that you can utilize MOXy extensions when
dealing with XML representation when developing a Jersey based RESTful
Web application

MOXy is EclipseLink's Object to XML Mapping services. MOXy allows for a
POJO object model to be mapped to an XML schema. The Java Architecture
for XML Binding (JAXB) provides a Java standard for object XML mapping
(OXM). MOXy supports JAXB, as well as providing its' own native API and
integration with Web Services.

MOXy XML Path Mapping Extension
-------------------------------

The MOXy extension shown in this example is described on the Eclipse
Wiki site at
<http://wiki.eclipse.org/EclipseLink/Examples/MOXy/GettingStarted/MOXyExtensions>.
It allows you to specify path based mapping via @XmlPath annotation.

If the MOXy extension was not used, the XML represenation of the
customer data would look like follows:

```xml
<customer>
  <name>Jane Doe</name>
  <address>
    <city>My Town</city>
    <street>123 Any Street</street>
  </address>
  <phoneNumbers type="work">613-555-1111</phoneNumbers>
  <phoneNumbers type="cell">613-555-2222</phoneNumbers>
</customer>
```

By adding `@org.eclipse.persistence.oxm.annotations.XmlPath` annotation
to the bean definition classes, you will get the following XML
representation instead:

```xml
<customer>
  <personal-info>
    <name>Jane Doe</name>
  </personal-info>
  <contact-info>
    <address>
      <city>My Town</city>
      <street>123 Any Street</street>
    </address>
    <phone-number type="work">613-555-1111</phone-number>
    <phone-number type="cell">613-555-2222</phone-number>
  </contact-info>
</customer>
```

XML Path expressions used are:

-   personal-info/name/text()
-   contact-info/address
-   contact-info/phone-number

Please check out the source code and the wiki page linked above for the
detailed information on the XML Path mapping feature.

Replacing Implicit JAXB Runtime With MOXy
-----------------------------------------

Since MOXy is a JAXB implementation, the example still utilizes the
standard Jersey JAXB message body reader/writer providers. To make
Jersey use MOXy runtime, you just need to put a `jaxb.properties` file
into the Java package containing your JAXB beans. The file should have
the following content:

    javax.xml.bind.context.factory=org.eclipse.persistence.jaxb.JAXBContextFactory

Concrete file used in this example is placed under

    src/main/resources/com/sun/jersey/samples/moxy/beans/jaxb.properties

Contents
--------

The example consists of a single REST Resource represented by the
following Java class:

    org.glassfish.jersey.examples.xmlmoxy.CustomerResource

A resource class that maintains a single customer data.

The mapping of the URI path space is presented in the following table:

URI path         | Resource class     | HTTP methods
---------------- | ------------------ | --------------
**_/customer_**  | CustomerResource   | GET, PUT

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container. You can access the application at:

-   [http://localhost:8080/moxy/customer](http://localhost:8080/xml-moxy/customer)
