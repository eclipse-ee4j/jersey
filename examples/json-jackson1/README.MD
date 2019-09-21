[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Jackson (1.x) JAX-RS JSON Provider Example
==========================================

This example demonstrates how to produce/consume JSON representations
from Java objects. This applies not only to JAXB beans, as shown in the
`json-from-jaxb` example but also to ordinary, un-annotated, POJOs.

This example hosts three simple read-only resources: One provides an
example of using a Jackson JSON provider (registered by the feature
`Jackson1Feature` in the `MyApplication` class) instead of using JAXB
(Object-&gt;JAXB-&gt;JSON) which has some limitations (e.g. empty arrays
in JAXB beans). For this resource the JSON representation is produced by
the Jackson JAX-RS provider, while the XML representation is generated
by JAXB as usual. The second web resource is based on a simple
un-annotated POJO and provides only JSON-based representations: JSON and
JSON with padding (JSONP). The third resource depicts how Jackson and
JAXB annotations could be mixed together within a single POJO.

Contents
--------

The "empty array" web resource is implemented by the
`org.glassfish.jersey.examples.jackson1.EmptyArrayResource` class.

The "non JAXB" web resource is implemented by the
`org.glassfish.jersey.examples.jackson1.NonJaxbBeanResource` class.

Both resources use the default Jackson mapper configuration to serialize JSON
data out and depicts the corner cases, where the Jersey internal, StAX
based, JSON processing can not be utilized.

The `org.glassfish.jersey.examples.jackson1.CombinedAnnotationResource`
class is used to show how JAXB and Jackson annotations could be combined
together in one Java bean for JSON serialization.

See the `org.glassfish.jersey.examples.jackson1.MyObjectMapperProvider` class
where Jackson specific options are used to set up the desired JSON
serialization configuration.

The mapping of the URI path space is presented in the following table:

URI path                     | Resource class               | HTTP method
---------------------------- | ---------------------------- | -------------
**_/emptyArrayResource_**    | EmptyArrayResource           | GET
**_/nonJaxbResource_**       | NonJaxbBeanResource          | GET
**_/combinedAnnotations_**   | CombinedAnnotationResource   | GET

To use Jackson specific configuration options, one can implement a
`ContextResolver<ObjectMapper>` provider. For an example of such an
implementation, see the `MyObjectMapperProvider` class.

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/)

A [WADL description](http://wadl.java.net/#spec) may be accessed at the URL:

-   `http://localhost:8080/jackson1/application.wadl`

The three resources are available at

-   <http://localhost:8080/jackson1/emptyArrayResource>
-   <http://localhost:8080/jackson1/nonJaxbResource>
-   <http://localhost:8080/jackson1/combinedAnnotations>

To easily obtain the different output types available, [cURL](http://curl.haxx.se/) can be used as follows:

Obtain the JSON output of EmptyArrayResource (use `-HAccept:application/xml` for XML output):

>     curl -HAccept:application/json http://localhost:8080/jackson1/emptyArrayResource

Obtain the JSON output of NonJaxbBeanResource (use `-HAccept:application/javascript` for JSONP output):

>     curl -HAccept:application/json http://localhost:8080/jackson1/nonJaxbResource

Obtain the JSON output of CombinedAnnotationResource:

>     curl -HAccept:application/json http://localhost:8080/jackson1/combinedAnnotations
