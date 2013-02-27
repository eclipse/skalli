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
package org.eclipse.skalli.model.ext.maven.internal.config;

import org.eclipse.skalli.services.scheduler.Schedule;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("mavenResolver")
public class MavenResolverConfig {

    private Schedule schedule;
    private String userId;

    // do not remove: required by xstream
    public MavenResolverConfig() {
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

}
