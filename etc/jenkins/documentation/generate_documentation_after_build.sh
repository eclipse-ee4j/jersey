#!/bin/bash -lex

#
# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
#

#Environment paths for Jenkins

#TOOLS_PREFIX=/opt/tools
#JAVA_PREFIX=/opt/tools/java/oracle
#MVN_HOME=/opt/tools/apache-maven/latest
#JAVA_HOME=/opt/tools/java/oracle/jdk-8/latest
#PATH=/opt/tools/apache-maven/latest/bin:/opt/tools/java/oracle/jdk-8/latest/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# IMPORTANT - modify environmen.sh before running the script.
source environment.sh

#Script REQUIRES Curl, Perl, Python, Git and Maven (3.6.0) to be installed on running host.
#Configures and moves documentation to proper locations,
#Generates release notes by milestone (same as version number)
#Moves release notes to proper location
#Sends release notes to GitHub (rest api)
#Commits all modifications to proper repositories (web and src-web)


DIRS="$RELEASE_VERSION"
if $UPDATE_LATEST;
then
    DIRS="latest $DIRS"
fi

#export PATH=/opt/csw/bin:$PATH
WEB_DIR=$WORKSPACE/target/jersey-web
WEB_SRC_DIR=$WORKSPACE/target/jersey-web-src


function copyDocs {
    APIDOCS_DIR=$WEB_DIR/apidocs/$1
    DOCS_DIR=$WEB_DIR/documentation/$1

    #
    # API docs
    #
    if test ! -e $APIDOCS_DIR ; then
        mkdir -p $APIDOCS_DIR
    fi
    cd $APIDOCS_DIR

    rm -rf jersey || true
    cp -R $WORKSPACE/target/site/apidocs ./jersey

    #
    # user guide
    #
    rm -rf $DOCS_DIR || true
    mkdir -p $DOCS_DIR
    cp -r $WORKSPACE/docs/target/docbook/index/* $DOCS_DIR
    rm $DOCS_DIR/*.fo || true
}

if test -e $WEB_DIR ; then
    rm -rf $WEB_DIR
fi

# would couse shallow reject: git clone --depth 1 $WEBSITE_URL $WEB_DIR
git clone $WEBSITE_URL $WEB_DIR
git clone $WEBSITE_SOURCE_REPO $WEB_SRC_DIR

cd $WEB_DIR

for dir in $DIRS; do
    copyDocs $dir
done

cd $WEB_DIR

git config --local user.email "jersey-bot@eclipse.org"
git config --local user.name "jersey-bot"
git add -A .
git diff --cached --exit-code || git commit -m "[jenkins] automatic javadoc and documentation update [$RELEASE_VERSION @ $BRANCH_SPECIFIER]"

# [1] update site
mvn versions:set -DnewVersion=$RELEASE_VERSION -DgenerateBackupPoms=false -f $WEB_SRC_DIR/pom.xml

# [2] update scm.md
perl -0777 -i' ' -pe 's@(<td>master</td>.*?</tr>)@$1\n'\
'    <tr>\n'\
'        <td>'$RELEASE_VERSION'</td>\n'\
'        <td>This is the Jersey '$RELEASE_VERSION' release tag. A sustaining branch for Jersey '$RELEASE_VERSION'\n'\
'        release will be created from the tag if necessary.</td>\n'\
'    </tr>@igs' \
$WEB_SRC_DIR/src/site/markdown/scm.md

### [] run post-site
##mvn clean post-site -f $WEB_SRC_DIR/pom.xml

mvn clean site -f $WEB_SRC_DIR/pom.xml
cp -r $WEB_SRC_DIR/target/site/* .
cp -a $WEB_SRC_DIR/src/main/templates/release-note.html release-notes/${RELEASE_VERSION}.html

# [] add pull requests to the release notes
MILESTONE_ID=$(curl 'https://api.github.com/repos/eclipse-ee4j/jersey/milestones' | python -c "
import json,sys,os
obj=json.load(sys.stdin)
[sys.stdout.write(str(a['number']) + os.linesep if a['title']=='"$RELEASE_VERSION"' else '') for a in obj]
")

PULL_REQUESTS=""
[ "$MILESTONE_ID" -eq "$MILESTONE_ID" ] && PULL_REQUESTS=$(curl 'https://api.github.com/repos/eclipse-ee4j/jersey/issues?state=closed&milestone='$MILESTONE_ID | python -c "
import json,sys,os;
obj=json.load(sys.stdin);
output = ''
for a in obj:
    if 'pull_request' in a:
        output += '    <li>[<a href=\'{0}\'>Pull {1}</a>] - {2}</li>\n'.format(str(a['pull_request']['html_url']), str(a['number']), str(a['title']))
    else:
        output += '    <li>[<a href=\'{0}\'>Issue {1}</a>] - {2}</li>\n'.format(str(a['html_url']), str(a['number']), str(a['title']))
if output != '':
    print '<h2>Issues and Pull Requests</h2>\n<ul>\n{}</ul>'.format(output)
")

if [ "$PULL_REQUESTS" != "" ]; then
  perl -0777 -i' ' -pe 's|<!-- PULL_REQUESTS_PLACEHOLDER -->|'"$(printf '%s' "$PULL_REQUESTS" | sed -e 's/[\|$@&]/\\&/g')"'|igs' release-notes/$RELEASE_VERSION.html
fi

# [] update the template with the current release
perl -0777 -i' ' -pe 's@(<h2>Previous releases</h2>.*?<ul>)@$1\n'\
'    <li><a href="'$RELEASE_VERSION'.html">Jersey '$RELEASE_VERSION' Release Notes</a></li>@igs' \
$WEB_SRC_DIR/src/main/templates/release-note.html

git add -A .
git diff --cached --exit-code || git commit -m "[jenkins] automatic project-info and release notes update [$RELEASE_VERSION @ $BRANCH_SPECIFIER]"

GITHUB_RELEASE_NOTES='{
  	"tag_name": "'"$RELEASE_VERSION"'",
  	"name": "'"$RELEASE_VERSION"'",
  	"body": "'"$(printf '%s' "$PULL_REQUESTS" | sed -e 's/["]/\\&/g' | perl -pe 's/\n/\\n/')"'"
  }'

echo "Github release notes: $GITHUB_RELEASE_NOTES"

if [ "$DRY_RUN" = "false" ]; then

  echo "Publishing release on Github"
  curl -u "${USER_NAME}:${USER_TOKEN}" -X POST -vvv --data "$GITHUB_RELEASE_NOTES" \
      -H "Content-type: application/json" https://api.github.com/repos/eclipse-ee4j/jersey/releases

  echo "Pushing Web sources to $WEBSITE_URL"
  #git push origin master
else
  echo "Dry run .. not pushing to the master"
  #git push origin master --dry-run
fi
