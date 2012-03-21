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
package org.eclipse.skalli.commands;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class CommandProviderImpl implements CommandProvider {

    private static final String COMMAND = "skalli"; //$NON-NLS-1$
    private static final String COMMAND_STORAGE = "storage"; //$NON-NLS-1$

    private static final String OPTION_LIST = "-list"; //$NON-NLS-1$
    private static final String OPTION_ADD = "-add"; //$NON-NLS-1$
    private static final String OPTION_REMOVE = "-remove"; //$NON-NLS-1$

    private static final String OPTION_COPY = "-copy"; //$NON-NLS-1$

    private static final String DESCRIPTION_ADMIN = "maintain set of user with administrative permissions";

    private static final String DESCRIPTION_STORAGE = "maintain data storage";
    private static String HELP_STORAGE = "\t" + COMMAND + " " + COMMAND_STORAGE + //$NON-NLS-1$ //$NON-NLS-2$
            " -copy <sourceType> <destinationType> <category>" + //$NON-NLS-1$
            " - " + DESCRIPTION_STORAGE + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

    private static String HELP = "\n---Skalli---\n" + HELP_STORAGE; //$NON-NLS-1$

    private static String INVALID = "Invalid command invocation.\n" + HELP; //$NON-NLS-1$

    @Override
    public String getHelp() {
        return HELP;
    }

    public void _skalli(CommandInterpreter intr) {
        try {
            String command = intr.nextArgument();
            if (StringUtils.equalsIgnoreCase(command, COMMAND_STORAGE)) {
                String arg = intr.nextArgument();
                if (StringUtils.isBlank(arg) || OPTION_COPY.compareToIgnoreCase(arg) != 0) {
                    intr.println(INVALID);
                } else {
                    String sourceType = intr.nextArgument();
                    String destinationType = intr.nextArgument();
                    String category = intr.nextArgument();
                    if (StringUtils.isBlank(sourceType) || StringUtils.isBlank(destinationType)
                            || StringUtils.isBlank(category)) {
                        intr.println(INVALID);
                    } else {
                        StorageCommand.copy(sourceType, destinationType, category, intr);
                    }
                }
            } else {
                // command not valid or null, print help
                intr.println(HELP);
            }
        } catch (Exception e) {
            intr.printStackTrace(e);
        }
    }

}
