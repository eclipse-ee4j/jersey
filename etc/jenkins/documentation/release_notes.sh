#!/bin/bash
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

# IMPORTANT - modify environmen.sh before running the script.
source environment.sh

#Script REQUIRES Curl, Perl and Python to be present in environment

#Generates and sends release notes to GitHub. Release notes are generated by milestone which corresponds to version name.
#if it is required to generate release notes by label, script shall be modified accordingly

WEB_DIR=$WORKSPACE/target/jersey-web
WEB_SRC_DIR=$WORKSPACE/target/jersey-web-src

cd $WEB_DIR

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

GITHUB_RELEASE_NOTES='{
  	"tag_name": "'"$RELEASE_VERSION"'",
  	"name": "'"$RELEASE_VERSION"'",
  	"body": "'"$(printf '%s' "$PULL_REQUESTS" | sed -e 's/["]/\\&/g' | perl -pe 's/\n/\\n/')"'"
  }'

echo "Github release notes: $GITHUB_RELEASE_NOTES"

  echo "Publishing release on Github"
  curl -u "${USER_NAME}:${USER_TOKEN}" -X POST -vvv --data "$GITHUB_RELEASE_NOTES" \
      -H "Content-type: application/json" https://api.github.com/repos/eclipse-ee4j/jersey/releases

  echo "Pushing Web sources to $WEBSITE_URL"
