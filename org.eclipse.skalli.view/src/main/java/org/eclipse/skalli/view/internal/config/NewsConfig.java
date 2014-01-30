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
package org.eclipse.skalli.view.internal.config;

import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("news")
public class NewsConfig {

    static class Messages {
        @XStreamImplicit(itemFieldName="message")
        private List<String> messages;

        public List<String> getMessages() {
            if (messages == null) {
                return Collections.emptyList();
            }
            return messages;
        }
    }

    private String url;

    private Messages alerts;
    private Messages messages;

    // do not remove: required by xstream
    public NewsConfig() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getAlerts() {
        if (alerts == null) {
            return Collections.emptyList();
        }
        return alerts.getMessages();
    }

    public List<String> getMessages() {
        if (messages == null) {
            return Collections.emptyList();
        }
        return messages.getMessages();
    }

    public boolean isMessageDefined() {
        return getAlerts().size() > 0 || getMessages().size() > 0;
    }

    public boolean isAlertsDefined() {
        return getAlerts().size() > 0;
    }
}
