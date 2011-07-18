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
package org.eclipse.skalli.core.internal.mail;

public interface MailTemplate {

    /**
     * @return path in classpath for the body template (velocity).
     */
    public String getBodyTemplate();

    /**
     * @return path in classpath for the subject template (velocity).
     */
    public String getSubjectTemplate();

}
