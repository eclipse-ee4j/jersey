[//]: # " Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. "
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

### Release Notes maven plugin

Runs for post-site mvn target, generates release notes for given template file, 
stores generated release notes in target/release-notes/{versionNumber}.html file (path of output file can be changed) 

### Input parameters:
 
 - releaseVersion - (String) - version to be used everywhere where it occurs. Like file name, replacement tag, release tag etc. 
 supplied in for of '2.29.1' 
 - githubApiUrl - (String) - short relative path to github api to which release notes shall be published (like eclipse-ee4j/jersey)
 - githubLogin - (String) - login of github user used to publish release notes to GitHub. Not used in dry run or not publish to GitHub modes.
 - githubToken -  (String) - token of github user while two factor authentication is used. 
                        Used to publish release notes to GitHub. Not used in dry run or not publish to GitHub modes.
 - githubPassword -  (String) - password of github user while simple authentication is used. 
                                         Used to publish release notes to GitHub. Not used in dry run or not publish to GitHub modes.
 - publishToGithub - (Boolean) - whether or not publish generated release notes directly to GitHub (false by default)
 - dryRun - (Boolean) - whether to modify anything (false) or not (true). True by default 
 - templateFilePath - (String) - template HTML file which is used to generate HTML release notes page
 - releaseDate - (String) - date of release (like 10-11-2019 or 10-NOV-2019 or any valid date format) 
 - releaseNotesFilePath - (String) - path for output file to be stored (default target/relese-notes/)