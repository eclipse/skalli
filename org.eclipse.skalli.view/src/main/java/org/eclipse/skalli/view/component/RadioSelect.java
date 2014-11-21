/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.view.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.skalli.commons.HtmlUtils;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class RadioSelect extends CustomField implements Button.ClickListener {

    private static final long serialVersionUID = 9062812566241582377L;

    private static final ThemeResource SELECTED = new ThemeResource("icons/button/radio_selected.png"); //$NON-NLS-1$
    private static final ThemeResource UNSELECTED = new ThemeResource("icons/button/radio_unselected.png"); //$NON-NLS-1$

    private final List<Entry> entries;
    private List<Button> radios;
    private int selected;

    private Layout layout;
    private GridLayout entriesGrid;

    public RadioSelect(String caption, Collection<Entry> entries) {
        setCaption(caption);
        this.entries = new ArrayList<Entry>(entries);
        this.selected = 0;

        layout = new CssLayout() {

            private static final long serialVersionUID = 3319052685837527947L;

            @Override
            protected String getCss(Component c) {
                if (c instanceof Button) {
                    return "margin-top:10px;margin-right:15px"; //$NON-NLS-1$
                }
                if (c instanceof Label) {
                    return "margin-bottom:15px"; //$NON-NLS-1$
                }
                return "margin-top:3px"; //$NON-NLS-1$
            }
        };
        layout.setMargin(true);
        layout.setSizeFull();

        if (entries != null && entries.size() > 0) {
            entriesGrid = new GridLayout(2, entries.size());
            entriesGrid.setSizeFull();
            //entriesGrid.setSpacing(true);
            layout.addComponent(entriesGrid);
            renderEntries();
        }

        setCompositionRoot(layout);
    }

    private void renderEntries() {
        int row = 0;
        radios = new ArrayList<Button>(entries.size());
        for (Entry entry : entries) {
            Button b = new Button();
            b.setIcon(row == selected ? SELECTED : UNSELECTED);
            b.setStyleName(Button.STYLE_LINK);
            b.addListener((Button.ClickListener) this);
            entriesGrid.addComponent(b, 0, row);
            radios.add(b);

            StringBuilder sb = new StringBuilder();
            sb.append("<span style=\"font-weight:bold\">"); //$NON-NLS-1$
            sb.append(StringEscapeUtils.escapeHtml(entry.getCaption()));
            sb.append("</span><br>"); //$NON-NLS-1$
            sb.append("<span style=\"white-space:normal\">"); //$NON-NLS-1$
            sb.append(HtmlUtils.clean(entry.getDescription()));
            sb.append("</span>"); //$NON-NLS-1$

            CssLayout css = new CssLayout() {

                private static final long serialVersionUID = 7370808823922141846L;

                @Override
                protected String getCss(Component c) {
                    return "margin-bottom:10px;margin-left:3px"; //$NON-NLS-1$
                }
            };
            css.setSizeFull();
            Label comment = new Label(sb.toString(), Label.CONTENT_XHTML);
            css.addComponent(comment);

            entriesGrid.addComponent(css, 1, row);
            entriesGrid.setColumnExpandRatio(1, 1.0f);
            ++row;
        }
    }

    @Override
    public void buttonClick(ClickEvent event) {
        radios.get(selected).setIcon(UNSELECTED);
        Button b = event.getButton();
        if (UNSELECTED.equals(b.getIcon())) {
            for (int i = 0; i < radios.size(); ++i) {
                if (b.equals(radios.get(i))) {
                    selected = i;
                }
            }
            b.setIcon(SELECTED);
        }
    }

    public String getSelected() {
        return entries.get(selected).getId();
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    public static class Entry implements Comparable<Entry> {
        private String id;
        private String caption;
        private String description;
        private float rank;

        public Entry(String id, String caption, String description, float rank) {
            super();
            this.id = id;
            this.caption = caption;
            this.description = description;
            this.rank = rank;
        }

        public String getId() {
            return id;
        }

        public String getCaption() {
            return caption;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public int compareTo(Entry o) {
            int res = Float.compare(rank, o.rank);
            if (res == 0) {
                res = caption.compareTo(o.caption);
                if (res == 0) {
                    res = id.compareTo(o.id);
                }
            }
            return res;
        }
    }

}
