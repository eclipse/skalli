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
package org.eclipse.skalli.core.validation;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.scheduler.Schedule;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("validation")
public class ValidationConfig {

    private Schedule schedule;
    private Severity minSeverity;
    private ValidationAction action;
    private String userId;
    private String entityType;

    public ValidationConfig() {
    }

    public ValidationConfig(ValidationConfig config) {
        minSeverity = config.getMinSeverity();
        action = config.getAction();
        userId = config.getUserId();
        entityType = config.getEntityType();
        schedule = config.getSchedule();
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Severity getMinSeverity() {
        if (minSeverity == null) {
            minSeverity = Severity.INFO;
        }
        return minSeverity;
    }

    public void setMinSeverity(Severity minSeverity) {
        this.minSeverity = minSeverity;
    }

    public ValidationAction getAction() {
        return action;
    }

    public void setAction(ValidationAction action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(action != null ? action : "<no action>").append(' ');
        sb.append(getMinSeverity()).append(' ');
        sb.append(StringUtils.isNotBlank(userId) ? userId : "<default user>").append(' ');
        sb.append(StringUtils.isNotBlank(entityType) ? entityType : "<all entity types>");
        return sb.toString();
    }
}
