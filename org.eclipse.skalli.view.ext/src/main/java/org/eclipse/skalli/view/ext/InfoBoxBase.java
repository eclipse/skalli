/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.view.ext;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.HtmlBuilder;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;

public abstract class InfoBoxBase implements InfoBox {

    protected Clipboard clipboard;

    protected static final String STYLE_LABEL = "infolabel"; //$NON-NLS-1$
    protected static final String STYLE_LINK = "infolink"; //$NON-NLS-1$

    protected static final String HSPACE = "&nbsp;&nbsp;&nbsp;&nbsp;"; //$NON-NLS-1$
    protected static final String DEFAULT_TARGET = HtmlBuilder.DEFAULT_TARGET;

    private static final String LAST_MODIFIED = "last modified"; //$NON-NLS-1$

    protected void bindClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    protected void unbindClipboard(Clipboard clipboard) {
        this.clipboard = null;
    }

    protected void createLabel(Layout layout, String caption) {
        createLabel(layout, caption, STYLE_LABEL);
    }

    protected void createLabel(Layout layout, String caption, String styleName) {
        Label label = new Label(caption, Label.CONTENT_XHTML);
        if (StringUtils.isNotBlank(styleName)) {
            label.addStyleName(styleName);
        } else {
            label.addStyleName(STYLE_LABEL);
        }
        layout.addComponent(label);
    }

    protected void createLink(Layout layout, String caption, String url) {
        createLink(layout, caption, url, DEFAULT_TARGET, null);
    }

    protected void createLink(Layout layout, String caption, String url, String targetName, String styleName) {
        createLink(layout, caption, new ExternalResource(url), targetName, styleName);
    }

    protected void createLink(Layout layout, String caption, Resource resource) {
        createLink(layout, caption, resource, DEFAULT_TARGET, null);
    }

    protected void createLink(Layout layout, String caption, Resource resource, String targetName, String styleName) {
        Link link = new Link(caption, resource);
        if (StringUtils.isNotBlank(targetName)) {
            link.setTargetName(targetName);
        } else {
            link.setTargetName(DEFAULT_TARGET);
        }
        if (StringUtils.isNotBlank(styleName)) {
            link.addStyleName(styleName);
        } else {
            link.addStyleName(STYLE_LINK);
        }
        layout.addComponent(link);
    }

    protected void addLastModifiedInfo(Layout layout, String caption, String lastModified, String lastModifiedBy) {
        if (lastModified == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<span style=\"font-size:x-small\">");

        if (StringUtils.isNotBlank(caption)) {
            sb.append(caption);
        } else {
            sb.append(LAST_MODIFIED);
        }
        sb.append(" ");
        sb.append(lastModified);
        if (StringUtils.isNotBlank(lastModifiedBy)) {
            sb.append(" by ").append(lastModifiedBy);
        }
        sb.append("</span>");

        Label label = asLabel(sb.toString());
        label.setStyleName("light");
        label.addStyleName(STYLE_LABEL);
        layout.addComponent(label);
    }

    protected void addLastModifiedInfo(Layout layout, EntityBase entity) {
        addLastModifiedInfo(layout, null, entity);
    }

    protected void addLastModifiedInfo(Layout layout, String caption, EntityBase entity) {
        if (entity == null) {
            return;
        }
        addLastModifiedInfo(layout, caption, entity.getLastModified(), entity.getLastModifiedBy());
    }

    protected void addLastModifiedInfo(Layout layout, ExtensionEntityBase ext) {
        addLastModifiedInfo(layout, null, ext);
    }

    public Label asLabel(String s) {
        return asLabel(s, (String[]) null);
    }

    public Label asLabel(StringBuilder sb) {
        return asLabel(sb.toString(), (String[]) null);
    }

    public Label asLabel(HtmlBuilder sb) {
        return asLabel(sb.toString(), (String[]) null);
    }

    public Label asLabel(String s, String... styles) {
        Label label = new Label(s, Label.CONTENT_XHTML);
        if (styles != null) {
            for (String style : styles) {
                label.addStyleName(style);
            }
        }
        return label;
    }

    public Label asLabel(StringBuilder sb, String... styles) {
        return asLabel(sb.toString(), styles);
    }

    public Label asLabel(HtmlBuilder sb, String... styles) {
        return asLabel(sb.toString(), styles);
    }

    /**
     * Creates a link that copies a given text to the clipboard.<p>
     * Note: An info box that uses this method must bind to the {@link Clipboard} service:
     * <pre>
     * &lt;reference
     *   name="Clipboard"
     *   interface="org.eclipse.skalli.view.ext.Clipboard"
     *   bind="bindClipboard"
     *   unbind="unbindClipboard"
     *   cardinality="0..1"
     *   policy="dynamic" /&gt;
     * </pre>
     *
     * @param label   the label to display with the link.
     * @param textToClipboard   the text to copy to the clipboard when the
     * link is clicked.
     */
    @SuppressWarnings("nls")
    protected String copyToClipboardLink(String label, String textToClipboard) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>").append(label);
        if (clipboard != null) {
            sb.append(clipboard.copyToClipboardLink(textToClipboard));
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public List<String> getSupportedActions() {
        return Collections.emptyList();
    }

    @Override
    public void perform(String action, Project project, String userId) {
        //nothing to do here; no default action available
    }

}
