[//]: # " Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved. "
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

Jersey (Java8) WebApp Example Archetype.
==========================================================

This module defines skeleton maven project (archetype) for developing new Java SE-based and Servlet-based examples for Jersey.

Instructions
------------

- install this archetype into your local repository, `maven-archetype-plugin` updates your local `archetype-catalog.xml` 
which is located in `.m2` directory.

>     mvn clean install

- run generation your new project using:

>     mvn archetype:generate -DarchetypeCatalog=local

- maven provides you a set of your local installed maven archetypes, choose this one
`org.glassfish.jersey.archetypes:jersey-example-java8-webapp`

- fill in all input fields properly, e.g.

```
groupId: org.glassfish.jersey.examples (default value)
artifactId: my-example
version: Just use a release version - e.g. "2.20"
package: Use org.glassfish.jersey.examples.my-example
projectAuthor: Use "Name Surname (name.surname at mycompany.com)"
projectDescription: "My New Example using Jersey."
projectName: "My Jersey Example"
```

- confirm the project generation and feel free to start coding!
