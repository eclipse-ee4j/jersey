/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.e2e.json.entity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.tests.e2e.json.JsonTestHelper;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings({"UnusedDeclaration", "RedundantIfStatement"})
@XmlRootElement
public class TreeModel {

    @SuppressWarnings({"UnusedDeclaration", "StringEquality", "RedundantIfStatement"})
    public static class Node {

        @XmlElement
        public String label;
        @XmlElement
        public boolean expanded;
        @XmlElement
        public List<Node> children;

        public Node() {
            this("dummy node", null);
        }

        public Node(String label) {
            this(label, null);
        }

        public Node(String label, Collection<Node> children) {
            this.label = label;
            if (!JsonTestHelper.isCollectionEmpty(children)) {
                this.children = new LinkedList<Node>();
                this.children.addAll(children);
                expanded = true;
            }
        }

        @Override
        public int hashCode() {
            int result = 13;
            result += 17 * label.hashCode();
            if (!JsonTestHelper.isCollectionEmpty(children)) {
                for (Node n : children) {
                    result = 5 + 17 * n.hashCode();
                }
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) {
                return false;
            }
            final Node other = (Node) obj;
            if (this.label != other.label && (this.label == null || !this.label.equals(other.label))) {
                return false;
            }
            if ((this.children != other.children
                    && JsonTestHelper.isCollectionEmpty(this.children) != JsonTestHelper.isCollectionEmpty(other.children))
                    && (this.children == null || !this.children.equals(other.children))) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            String result = "(" + label + ":";
            if (!JsonTestHelper.isCollectionEmpty(children)) {
                for (Node n : children) {
                    result += n.toString();
                }
                return result + ")";
            } else {
                return result + "0 children)";
            }
        }
    }

    @XmlElement
    public Node root;

    public TreeModel() {
    }

    public TreeModel(Node root) {
        this.root = root;
    }

    public static Object createTestInstance() {
        TreeModel instance = new TreeModel();
        instance.root = new Node();
        return instance;
    }

    @Override
    public int hashCode() {
        if (null != root) {
            return 7 + root.hashCode();
        } else {
            return 7;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TreeModel)) {
            return false;
        }
        final TreeModel other = (TreeModel) obj;
        if (this.root != other.root && (this.root == null || !this.root.equals(other.root))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return (null != root) ? root.toString() : "(NULL_ROOT)";
    }
}
