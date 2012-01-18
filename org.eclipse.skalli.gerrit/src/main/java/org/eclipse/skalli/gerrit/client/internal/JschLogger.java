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
package org.eclipse.skalli.gerrit.client.internal;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Logger;

public class JschLogger implements Logger {

    private static org.slf4j.Logger LOG = LoggerFactory.getLogger(JschLogger.class);

    @Override
    public boolean isEnabled(int level) {
        return true;
    }

    @Override
    public void log(int level, String message) {
        switch (level) {
        case 0: //DEBUG
            LOG.debug(message);
            break;
        case 1: //INFO
            LOG.info(message);
            break;
        case 2: //WARN
            LOG.warn(message);
            break;
        case 3: //ERROR
        case 4: //FATAL
            LOG.error(message);
            break;
        default:
            LOG.info(message);
            break;
        }
    }

}