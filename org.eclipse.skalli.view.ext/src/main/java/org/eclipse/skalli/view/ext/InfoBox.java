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
package org.eclipse.skalli.view.ext;

import java.util.List;

import org.eclipse.skalli.model.Project;

import com.vaadin.ui.Component;

/**
 * Extension point for info boxes on the project details page.
 * Implementations must be registered as OSGi service component.
 * <p>
 * IMPORTANT NOTES:<br>
 * <ul>
 * <li>There is only one instance of a <code>ProjectInfoBox</code> per implementing class, i.e.
 * info boxes must be stateless and thread safe.</li>
 * <li>An implementation of <code>ProjectInfoBox</code> must not have Vaadin components/layouts
 * as instance variables. Otherwise there is the risk of <tt>Out-Of-Sync</tt> errors.</li>
 * </ul>
 */
public interface InfoBox extends IconProvider {

    /** The infobox should appear in the left column of a project's detail page. */
    public static final int COLUMN_WEST = 1;

    /** The infobox should appear in the right column of a project's detail page. */
    public static final int COLUMN_EAST = 2;

    /**
     * Action attribute that triggers a refresh of the infobox content. For example, if
     * the infobox shows content of a remote system, the action should synchronize the
     * content of the infobox with the remote system.
     *
     * Usage: <tt>/projects/&lt;projectId&gt;/infoboxes?action=refresh</tt> or
     * <tt>/projects/&lt;projectId&gt;/infoboxes/&lt;shortName&gt;?action=refresh</tt>, respectively.
     *
     * @see #perform(String, Project, String)
     */
    public static final String REFRESH_ACTION = "refresh"; //$NON-NLS-1$

    /**
     * Returns the caption of the info box.
     */
    public String getCaption();

    /**
     * Returns the rank of the info box in relation to the other info
     * boxes in the column. Smaller rank means the info box should
     * appear above all other info boxes with larger rank. Info boxes
     * with the same rank are arranged in alphabetical order of to their
     * captions.
     *
     * @return the rank of the info box, i.e. a positive float number greater 1.0.
     */
    public float getPositionWeight();

    /**
     * Specifies whether the info box prefers the left ("west") or right ("east") column
     * on the project details page.
     *
     * @return either {@link #COLUMN_WEST} or {@link #COLUMN_EAST}.
     */
    public int getPreferredColumn();

    /**
     * Returns <code>true</code>, if the info box is visible and the user
     * requesting the info box is allowed to see it.
     *
     * Info boxes that can be switched on/off in the project edit dialog should
     * check whether a corresponding extension is attached to the <code>project</code>
     * and return <code>false</code> if there is no such extension available.
     *
     * @param project  the project for which the info box is to be rendered.
     * @param userId  the unique identifier of the user viewing the project details page.
     *
     * @return <code>true</code>, if the info box is visible.
     */
    public boolean isVisible(Project project, String userId);

    /**
     * Returns the Vaadin component to be rendered inside the info box panel,
     * or <code>null</code> if the info box has nothing to display and should
     * therefore not be rendered at all.
     *
     * @param project  the project for which the component is to be created.
     * @param util  context information for the creation of the component.
     */
    public Component getContent(Project project, ExtensionUtil util);

    /**
    * Returns a short name for the info box or null.
    * If the short name is not null it must be unique among all info boxes in the system.
    * Consumers should treed a null short name as if it does not require his conditions.
    *
    * One possible Consumer is a Project filer, which  perform actions only to those Infoxes which have the requested name.
    */
    public String getShortName();

    /**
     * Performs a given action on a project related to this info box.
     * For example, an info box might trigger a refresh of remote content that it displays.
     * This method is called before rendering of the project's detail page for URLs of the
     * form <tt>/projects/&lt;projectId&gt;/infoboxes?action=&lt;action&gt;</tt>
     * and <tt>/projects/&lt;projectId&gt;/infoboxes/&lt;shortName>&gt;action=&lt;action&gt;</tt>, respectively.
     * @param action the action to perform
     * @param project the project for which to perform the action
     * @param userId the unique identifier of the user that triggered the action
     */
    public void perform(String action, Project project, String userId);

    /**
     * Returns the actions supported by this infobox.
     * @return  a list of actions, or an empty list.
     */
    public List<String> getSupportedActions();

}
