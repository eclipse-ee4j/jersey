[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Entity Data Filtering Dynamic and Configurable Query Parameters
===============================================================

This example demonstrates how to use entity filtering feature
dynamically with defining what should be sent to the other side based on
query parameters and how to apply the rules on domain classes as well as
on JAX-RS resource classes or JAX-RS resource methods.

The full description how Entity Data Filtering can be found in Jersey
User Guide, chapter [Entity Data
Filtering](https://jersey.java.net/documentation/latest/entity-filtering.html).
Sections relevant to this example (describing this exact example) are:

-   [Enabling and configuring Entity Filtering in your
    application](https://jersey.java.net/documentation/latest/entity-filtering.html#d0e13911)
-   [Entity Filtering based on dynamic and configurable query
    parameters](https://jersey.java.net/documentation/latest/entity-filtering.html#ef.selectable.annotations)

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                                     | Resource class   | HTTP methods   | Allowed values                                                               | Notes
------------------------------------------   | ---------------- | -------------- | ---------------------------------------------------------------------------- | ----------------------------------------------
**_/people/{id}?select={select}_**           |  PersonResource  |  GET           |  id = any value, select = field names from Person/Address/PhoneNumber class  |  Returns fields of Person/Address/PhoneNumber
**_/people/{id}/addresses?select={select}_** |  PersonResource  |  GET           |  id = any value, select = field names from Address/PhoneNumber class         |  Returns fields of Address/PhoneNumber

Application is based on Grizzly container (see `App`). Everything needed
(resources/providers) is registered in
`SelectableEntityFilteringApplication`.

Sample Response
---------------

Even though the same instance of, e.g. Person class, is used to create
response the given values of `select` query parameter are used to select
the fields that would be transferred over the wire. For
`people/1234?select=familyName,givenName` it looks like:

```javascript
{
   "familyName": "Dowd",
   "givenName": "Andrew"
}
```

And for `people/1234?select=familyName,givenName,addresses.phoneNumber.number` it looks like:

```javascript
{
   "addresses":[
      {
         "phoneNumber":{
            "number": "867-5309"
         }
      }
   ],
   "familyName": "Dowd",
   "givenName": "Andrew"
}
```

Running the Example
-------------------

Run the example as follows:

>     mvn clean package exec:java

This deploys current example using Grizzly. You can access the
application at:

-   <http://localhost:8080/people/1234>
-   <http://localhost:8080/people/1234?select=familyName,givenName>
-   <http://localhost:8080/people/1234?select=region,addresses.region>
-   <http://localhost:8080/people/1234?select=familyName,givenName,addresses.phoneNumber.number>

Using Jackson instead of MOXy
-----------------------------

This examples uses by default Entity Data Filtering feature together
with MOXy. To switch MOXy JSON provider to Jackson (2.x) JSON provider
simply

-   comment registration of MOXy ContextResolver, and\
     `register(new MoxyJsonConfig().setFormattedOutput(true).resolver())`
-   uncomment registration of JacksonFeature\
     `register(JacksonFeature.class)`

in `SelectableEntityFilteringApplication` class.
