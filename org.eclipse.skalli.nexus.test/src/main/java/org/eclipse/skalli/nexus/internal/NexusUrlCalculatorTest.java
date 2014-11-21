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
package org.eclipse.skalli.nexus.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.skalli.nexus.NexusClientException;
import org.eclipse.skalli.nexus.internal.config.NexusConfig;
import org.junit.Test;

/**
 *
 */
public class NexusUrlCalculatorTest {

    @Test
    public void testGetNexusUrl_case1() throws NexusClientException, MalformedURLException {
        NexusConfig nexusConfig = new NexusConfig();
        nexusConfig.setUrl("http://mynexus:8081/nexus/");
        nexusConfig.setDomain("repositories");
        nexusConfig.setTarget("build.milestones");

        NexusUrlCalculator calc = new NexusUrlCalculator(nexusConfig, "com.sap.ldi.demo.helloworld",
                "com.sap.ldi.demo.helloworld");
        assertThat(
                calc.getNexusUrl(0, 100),
                is(new URL(
                        "http://mynexus:8081/nexus/service/local/data_index/repositories/build.milestones/content?g=com.sap.ldi.demo.helloworld&a=com.sap.ldi.demo.helloworld&from=0&count=100")));
    }

    @Test
    public void testGetNexusUrl_case2() throws NexusClientException, MalformedURLException {

        NexusConfig nexusConfig = new NexusConfig();
        nexusConfig.setUrl("http://nexus/");
        nexusConfig.setDomain("foo");
        nexusConfig.setTarget("build.snapshot");

        NexusUrlCalculator calc = new NexusUrlCalculator(nexusConfig, "com.sap.demo",
                "com.sap.demo.helloworld");
        assertThat(
                calc.getNexusUrl(100, 999),
                is(new URL(
                        "http://nexus/service/local/data_index/foo/build.snapshot/content?g=com.sap.demo&a=com.sap.demo.helloworld&from=100&count=999")));
    }
}
