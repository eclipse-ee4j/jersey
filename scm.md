[//]: # " Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. "
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

### Checking Out the Jersey Sources

#### Checking out Jersey 2.x sources

Jersey 2.x is based on a completely new code base that has been completely revamped.
As part of this effort, Jersey has moved from SVN to modern, distributed  GIT version control system.
This allowed us to how the project sources on [GitHub][jersey2gh]. The repository can be cloned
in a read-only mode by invoking:

```bash
git clone ssh://git@github.com:eclipse-ee4j/jersey.git
```

If you're only interested in reading the latest version of the sources and do not wish
to a) contribute code back to the repository or b) do not care about the history,
you can speed up the clone process by invoking:

```bash
git clone --depth 1 ssh://git@github.com:eclipse-ee4j/jersey.git
```
instead. This may speed up the clone process considerably.

#### Checking out Jersey 1.x sources

Old Jersey 1.x source code is hosted on [GitHub][jersey1gh] too. Read-only copy can be obtained by executing:

```bash
git clone ssh://git@github.com:jersey/jersey-1.x.git
```
Jersey 1.x code base has been frozen in terms of new features. Only bug fixes and other sustaining
patches are currently accepted in Jersey 1.x source code repository.

### Understanding Jersey 2.x Branches and Tags

Tag & Branch information:

<table>
    <tr>
        <th>Tag/Branch Name</th>
        <th>Details</th>
    </tr>
    <tr>
        <td>master</td>
        <td>This is effectively Jersey 2.x development branch. Development of new features happens here.</td>
    </tr>
    <tr>
        <td>2.28</td>
        <td>This is the Jersey 2.28 release tag. A sustaining branch for Jersey 2.28
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.25.1</td>
        <td>This is the Jersey 2.25.1 release tag. A sustaining branch for Jersey 2.25.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.25</td>
        <td>This is the Jersey 2.25 release tag. A sustaining branch for Jersey 2.25
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.24.1</td>
        <td>This is the Jersey 2.24.1 release tag. A sustaining branch for Jersey 2.24.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.24</td>
        <td>This is the Jersey 2.24 release tag. A sustaining branch for Jersey 2.24
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.23.2</td>
        <td>This is the Jersey 2.23.2 release tag. A sustaining branch for Jersey 2.23.2
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.23.1</td>
        <td>This is the Jersey 2.23.1 release tag. A sustaining branch for Jersey 2.23.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.23</td>
        <td>This is the Jersey 2.23 release tag. A sustaining branch for Jersey 2.23
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.22.2</td>
        <td>This is the Jersey 2.22.2 release tag. A sustaining branch for Jersey 2.22.r21
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.22.1</td>
        <td>This is the Jersey 2.22.1 release tag. A sustaining branch for Jersey 2.22.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.22</td>
        <td>This is the Jersey 2.22 release tag. A sustaining branch for Jersey 2.22
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.21.1</td>
        <td>This is the Jersey 2.21.1 release tag. A sustaining branch for Jersey 2.21.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.21</td>
        <td>This is the Jersey 2.21 release tag. A sustaining branch for Jersey 2.21
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.20</td>
        <td>This is the Jersey 2.20 release tag. A sustaining branch for Jersey 2.20
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.19</td>
        <td>This is the Jersey 2.19 release tag. A sustaining branch for Jersey 2.19
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.18</td>
        <td>This is the Jersey 2.18 release tag. A sustaining branch for Jersey 2.18
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.17</td>
        <td>This is the Jersey 2.17 release tag. A sustaining branch for Jersey 2.17
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.16</td>
        <td>This is the Jersey 2.16 release tag. A sustaining branch for Jersey 2.16
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.15</td>
        <td>This is the Jersey 2.15 release tag. A sustaining branch for Jersey 2.15
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.14</td>
        <td>This is the Jersey 2.14 release tag. A sustaining branch for Jersey 2.14
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.13</td>
        <td>This is the Jersey 2.13 release tag. A sustaining branch for Jersey 2.13
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.12</td>
        <td>This is the Jersey 2.12 release tag. A sustaining branch for Jersey 2.12
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.11</td>
        <td>This is the Jersey 2.11 release tag. A sustaining branch for Jersey 2.11
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.10.1</td>
        <td>This is the Jersey 2.10.1 release tag. A sustaining branch for Jersey 2.10.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.10</td>
        <td>This is the Jersey 2.10 release tag. A sustaining branch for Jersey 2.10
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.9.1</td>
        <td>This is the Jersey 2.9.1 release tag. A sustaining branch for Jersey 2.9.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.9</td>
        <td>This is the Jersey 2.9 release tag. A sustaining branch for Jersey 2.9
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.8</td>
        <td>This is the Jersey 2.8 release tag. A sustaining branch for Jersey 2.8
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.7</td>
        <td>This is the Jersey 2.7 release tag. A sustaining branch for Jersey 2.7
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.6</td>
        <td>This is the Jersey 2.6 release tag. A sustaining branch for Jersey 2.6
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.5.1</td>
        <td>This is the Jersey 2.5.1 release tag. A sustaining branch for Jersey 2.5.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.5</td>
        <td>This is the Jersey 2.5 release tag. A sustaining branch for Jersey 2.5
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.4.1</td>
        <td>This is the Jersey 2.4.1 release tag. A sustaining branch for Jersey 2.4.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.4</td>
        <td>This is the Jersey 2.4 release tag. A sustaining branch for Jersey 2.4
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.3.1</td>
        <td>This is the Jersey 2.3.1 release tag. A sustaining branch for Jersey 2.3.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.3</td>
        <td>This is the Jersey 2.3 release tag. A sustaining branch for Jersey 2.3
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.2</td>
        <td>This is the Jersey 2.2 release tag. A sustaining branch for Jersey 2.2
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.1</td>
        <td>This is the Jersey 2.1 release tag. A sustaining branch for Jersey 2.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.0.1</td>
        <td>This is the Jersey 2.0.1 release tag. A sustaining branch for Jersey 2.0.1
        release will be created from the tag if necessary.</td>
    </tr>
    <tr>
        <td>2.0</td>
        <td>This is the Jersey 2.0 release tag. A sustaining branch for Jersey 2.0
        release will be created from the tag if necessary.</td>
    </tr>
</table>

In order to check out a branch in git, you simply need to issue
`git checkout <branch name>` (e.g. `git checkout master`).

If you're interested in working with an existing tag, you'll first need to issue
`git fetch --tags` in order to obtain the tag references.  After successful completion
of this command, you can issue `git checkout <tag name>`. Note that when doing so, you'll
get a message about being in a detached state - this is normal and nothing to worry about.
All fetched tags can be listed using `git tag -l` In general, we keep our tag names
inline with the released version.  For example, if you wanted to checkout the tag
for Jersey 2.0, the tag name would be *2.0* This convention is consistent for
all branches/versions/releases.

### Submitting Patches and Contribute Code

Contributing to Jersey project can be done in various ways: bug fixes, enhancements, new features,
or even whole new extension modules. In general, all contributions must comply with following
requirements:

*   All contributors must sign the [Oracle Contributor Agreement][oca].

*   Any new contribution must be associated with an existing [Jersey issue][jersey-jira].
    If no existing issue has been yet opened for the problem your contribution attempts to solve,
    please open a new one.

*   All bug fixes must be accompanied with a new unit test (or a set of unit tests) that
    reproduce the fixed issue.

*   New large feature contributions must be accompanied with:

    *   A patch for [Jersey User Guide][jug-sources] that is written in DocBook 5. Either a
        new chapter or a new section to existing chapter must be provided as appropriate.

    *   At least one new [example][jersey-examples] demonstrating the feature.

For small patches (minor bug fixes, correction of typos in documentation etc.), linking a
[gist][gist] that contains your patch with details on what you\'re resolving and how you\'ve
resolved it is most convenient approach. Alternatively, especially in case of larger contributions
or more significant changes in code, please follow the process of opening a new
[GitHub pull request][gpr].

### GIT Tips and Tricks for Developers

First, for anyone not familiar with Git, before attempting to work with the repository,
we highly recommend reading the [Git tutorial][gitorial].

When collaborating, before you push your changes to the remote repository, it's best
to issue `git pull --rebase` This will 'replay' any changes that have occurred in the
remote repository since your last pull on top of your current work.  If you don't do this,
Git will perform a merge for you, however, the result of the commit will look like
you've touched files that you haven't.  This is fine, but it generally raises a few eyebrows
and makes code reviews of any patches slightly more complicated. As usual, more complications
means more time spent in review.

There are times when you may need to move changes back and forth between branches.
In cases where the code bases are very similar, you can use
`git cherry-pick <SHA1 of the commit to pick and apply>` to do this quickly.



[gist]: https://gist.github.com/
[gitorial]: http://schacon.github.com/git/gittutorial.html
[gpr]: https://help.github.com/articles/using-pull-requests
[oca]: http://www.oracle.com/technetwork/community/oca-486395.html

[jersey-jira]: https://github.com/eclipse-ee4j/jersey/issues
[jersey-examples]: https://github.com/eclipse-ee4j/jersey/tree/master/examples/
[jersey2gh]: http://github.com/eclipse-ee4j/jersey/
[jug-sources]: https://github.com/eclipse-ee4j/jersey/tree/master/docs/src/main/docbook/
