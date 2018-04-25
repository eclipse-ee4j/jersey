/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.aggregator;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.KeyStroke;

/**
 * TODO: javadoc.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class RemoveSelectedListItemsAction extends AbstractAction {

    private final JList list;
    private final DefaultListModel listModel;

    public RemoveSelectedListItemsAction(JList list, DefaultListModel model) {
        if (list == null || model == null) {
            throw new NullPointerException("Bound JList component and it's model must not be null.");
        }

        this.list = list;
        this.listModel = model;

        putValue(NAME, "Delete");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        removeSelectedListItems();
    }

    private void removeSelectedListItems() {
        Object[] selectedValues = list.getSelectedValues();
        boolean itemsSelected = selectedValues.length > 0;

        if (itemsSelected && confirmRemove()) {
            for (Object selectedValue : selectedValues) {
                listModel.removeElement(selectedValue);
            }
        }
    }

    private boolean confirmRemove() {
        // E.g. JOptionPane-Confirm-Dialog
        return true;
    }
}
