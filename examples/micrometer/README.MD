[//]: # " Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Public License v. 2.0, which is available at "
[//]: # " http://www.eclipse.org/legal/epl-2.0. "
[//]: # "  "
[//]: # " This Source Code may also be made available under the following Secondary "
[//]: # " Licenses when the conditions for such availability set forth in the "
[//]: # " Eclipse Public License v. 2.0 are satisfied: GNU General Public License, "
[//]: # " version 2 with the GNU Classpath Exception, which is available at "
[//]: # " https://www.gnu.org/software/classpath/license.html. "
[//]: # "  "
[//]: # " SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 "

jersey-micrometer-webapp
==========================================================

This example demonstrates basics of Micrometer Jersey integration

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                                   | Resource class            | HTTP methods
------------------------------------------ | ------------------------- | --------------
**_/micro/meter_**                            | JerseyResource            | GET
**_/micro/metrics_**                            | JerseyResource            | GET
**_/micro/metrics/metrics_**                            | JerseyResource            | GET

Sample Response
---------------

```javascript
--- (micro/meter)
Hello World!
---- (micro/metrics)   
Listing available meters: http.shared.metrics;
---- (micro/metric/metrics)
Overall requests counts: 9, total time (millis): 35.799483
```


Running the Example
-------------------

Run the example using [Grizzly](https://javaee.github.io/grizzly/) container as follows:

>     mvn clean compile exec:java

- <http://localhost:8080/micro/meter>
- after few request to the main page go to the url
- - <http://localhost:8080/micro/metrics>
- and see the list of available meters
- then go to the 
- - <http://localhost:8080/micro/metrics/metrics>
- and see statistics for the micro/meter page