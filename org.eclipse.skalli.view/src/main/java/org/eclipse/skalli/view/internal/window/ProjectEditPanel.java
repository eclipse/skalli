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
package org.eclipse.skalli.view.internal.window;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.issues.Issues;
import org.eclipse.skalli.services.issues.IssuesService;
import org.eclipse.skalli.services.project.ProjectNode;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.eclipse.skalli.services.user.UserUtils;
import org.eclipse.skalli.view.ext.ExtensionFormService;
import org.eclipse.skalli.view.ext.Navigator;
import org.eclipse.skalli.view.ext.ProjectEditContext;
import org.eclipse.skalli.view.ext.ProjectEditMode;
import org.eclipse.skalli.view.internal.application.ProjectApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ProjectEditPanel extends Panel implements Issuer {

    private static final long serialVersionUID = 2377962084815410728L;

    private static final Logger LOG = LoggerFactory.getLogger(ProjectEditPanel.class);

    private static final String STYLE_EDIT_PROJECT = "prjedt"; //$NON-NLS-1$
    private static final String STYLE_EDIT_PROJECT_LAYOUT = "prjedt-layout"; //$NON-NLS-1$
    private static final String STYLE_EDIT_PROJECT_BUTTONS = "prjedt-buttons"; //$NON-NLS-1$
    private static final String STYLE_EDIT_PROJECT_ERROR = "prjedt-errorLabel"; //$NON-NLS-1$
    private static final String STYLE_ISSUES = "prjedt-issues"; //$NON-NLS-1$

    private static final String HEADER = "header_"; //$NON-NLS-1$
    private static final String FOOTER = "footer_"; //$NON-NLS-1$
    private static final String BUTTON_OK = "button_ok"; //$NON-NLS-1$
    private static final String BUTTON_CANCEL = "button_cancel"; //$NON-NLS-1$
    private static final String BUTTON_EXPAND_ALL = "button_expand_all"; //$NON-NLS-1$
    private static final String BUTTON_COLLAPSE_ALL = "button_collapse_all"; //$NON-NLS-1$
    private static final String BUTTON_VALIDATE = "button_validate"; //$NON-NLS-1$

    private static final String PANEL_WIDTH = "600px"; //$NON-NLS-1$
    private static final String CONFIRM_POPUP_WIDTH = "350px"; //$NON-NLS-1$

    private final ThemeResource ICON_BUTTON_OK = new ThemeResource("icons/button/ok.png"); //$NON-NLS-1$
    private final ThemeResource ICON_BUTTON_CANCEL = new ThemeResource("icons/button/cancel.png"); //$NON-NLS-1$
    private final ThemeResource ICON_BUTTON_EXPAND_ALL = new ThemeResource("icons/button/openall.png"); //$NON-NLS-1$
    private final ThemeResource ICON_BUTTON_COLLAPSE_ALL = new ThemeResource("icons/button/closeall.png"); //$NON-NLS-1$
    private final ThemeResource ICON_BUTTON_VALIDATE = new ThemeResource("icons/button/validate.png"); //$NON-NLS-1$

    private static final String WARN_EXTENSION_DISABLED = "Extension <i>{0}</i> has been disabled";
    private static final String WARN_EXTENSION_INHERITED = "Extension <i>{0}</i> is inherited from parent project <i>{1}</i>";

    private Project project;
    private ProjectTemplate projectTemplate;
    private ProjectEditMode mode;

    private Project modifiedProject;

    private TreeSet<ProjectEditPanelEntry> entries;
    private Set<String> selectableExtensions;
    private Map<String, String> displayNames;

    private ProjectApplication application;
    private Navigator navigator;

    private Label headerLabel;
    private Label footerLabel;
    private Button headerCheckButton;
    private Button footerCheckButton;

    private CssLayout indicatorArea;
    private ProgressIndicator progressIndicator;
    private ProgressThread progressThread;
    private ValidatorThread validatorThread;

    private Issues persistedIssues;

    /**
     * Creates a project edit view for
     * <ul>
     *   <li>browsing of an existing project in readonly mode ({@link ProjectEditMode#VIEW_PROJECT})</li>
     *   <li>editing of an existing project ({@link ProjectEditMode#EDIT_PROJECT})</li>
     *   <li>editing of a new project ({@link ProjectEditMode#NEW_PROJECT})</li>
     * </ul>
     *
     * @param application  the application.
     * @param navigator  the navigator used to leave the dialog.
     * @param project  the project to browse or modify, or a freshly created project.
     * (see {@link ProjectService#createProject(String, String)}).
     * @param mode  one of {@link ProjectEditMode#VIEW_PROJECT}, {@link ProjectEditMode#EDIT_PROJECT} or
     * {@link ProjectEditMode#NEW_PROJECT}.
     */
    public ProjectEditPanel(ProjectApplication application, Navigator navigator, Project project, ProjectEditMode mode)
    {
        this.application = application;
        this.navigator = navigator;
        this.mode = mode;
        this.project = project;

        // If the project has been persisted before, do not modify the
        // cached project instance, but modify a copy loaded directly from storage.
        if (ProjectEditMode.NEW_PROJECT.equals(mode)) {
            modifiedProject = project;
        } else {
            ProjectService service = Services.getRequiredService(ProjectService.class);
            modifiedProject = service.loadEntity(Project.class, project.getUuid());
        }

        ProjectTemplateService templateService = Services.getRequiredService(ProjectTemplateService.class);
        projectTemplate = templateService.getProjectTemplateById(project.getProjectTemplateId());

        initializeSelectableExtensions();
        loadPersistedIssues();
        initializePanelEntries();

        setSizeFull();
        setStyleName(STYLE_EDIT_PROJECT);
        renderContent((VerticalLayout) getContent());
    }

    /**
     * Loads persisted issues for the project.
     */
    private void loadPersistedIssues() {
        if (modifiedProject.getUuid() == null) {
            LOG.info("New project, no issues available. Skipping loading of issues.");
            return;
        }

        IssuesService issuesService = Services.getService(IssuesService.class);
        if (issuesService == null) {
            LOG.warn("No issue service available. Skipping loading of issues.");
            return;
        }

        persistedIssues = issuesService.loadEntity(Issues.class, modifiedProject.getUuid());
    }

    /**
     * Initializes the {@link #selectableExtensions} field:
     * Calls {@link ProjectTemplateService#getSelectableExtensions(ProjectTemplate, Project)} to determine
     * all extensions that
     * <ul>
     * <li>can work with the given template (see {@link ExtensionService#getProjectTemplateIdservice()})</li>
     * <li>are not in the template's exclude list (if an exclude list is specified,
     * see {@link ProjectTemplate#getExcludedExtensions()})</li>
     * <li>are in the templates include list (if an include list is specified,
     * see {@link ProjectTemplate#getIncludedExtensions()})</li>
     * </ul>
     * If all checks succeed, the extension's class name is added to <code>selectableExtensions</code>.
     */
    private void initializeSelectableExtensions() {
        ProjectTemplateService projectTemplateService = Services.getRequiredService(ProjectTemplateService.class);
        selectableExtensions = new HashSet<String>();
        for (Class<? extends ExtensionEntityBase> extension : projectTemplateService.getSelectableExtensions(
                projectTemplate, modifiedProject)) {
            selectableExtensions.add(extension.getName());
        }
    }

    /**
     * Creates and initializes the panels.
     * Iterates over all available {@link ExtensionFormService} and creates a {@link ProjectEditPanelEntry}
     * for each form service that is supported by the project template of the project to edit.
     */
    @SuppressWarnings("rawtypes")
    private void initializePanelEntries() {
        entries = new TreeSet<ProjectEditPanelEntry>(new ProjectEditPanelComparator(projectTemplate));
        displayNames = new HashMap<String, String>();
        Context context = new Context(application, projectTemplate, mode);
        Iterator<ExtensionFormService> extensionFormFactories = Services.getServiceIterator(ExtensionFormService.class);
        while (extensionFormFactories.hasNext()) {
            ExtensionFormService extensionFormFactory = extensionFormFactories.next();
            String extensionClassName = extensionFormFactory.getExtensionClass().getName();
            if (selectableExtensions.contains(extensionClassName)) {
                ProjectEditPanelEntry entry = new ProjectEditPanelEntry(
                        modifiedProject, extensionFormFactory, context, application);
                entry.setWidth(PANEL_WIDTH);
                entries.add(entry);
                displayNames.put(extensionClassName, entry.getDisplayName());
            }
        }
    }

    void renderContent(VerticalLayout content) {
        content.setStyleName(STYLE_EDIT_PROJECT_LAYOUT);
        headerCheckButton = renderButtons(content, true);
        renderProgessIndicator(content);
        headerLabel = renderMessageArea(content);
        renderPanels(content);
        footerLabel = renderMessageArea(content);
        footerCheckButton = renderButtons(content, false);
        renderPersistedIssues();
    }

    private void renderProgessIndicator(VerticalLayout layout) {
        indicatorArea = new CssLayout();
        indicatorArea.setVisible(false);
        indicatorArea.setMargin(true);
        indicatorArea.setWidth(PANEL_WIDTH);
        indicatorArea.addComponent(new Label("<strong>Project is checked for issues</strong>", Label.CONTENT_XHTML));
        progressIndicator = new ProgressIndicator();
        progressIndicator.setWidth("300px");
        progressIndicator.setIndeterminate(false);
        indicatorArea.addComponent(progressIndicator);
        layout.addComponent(indicatorArea);
        layout.setComponentAlignment(indicatorArea, Alignment.MIDDLE_CENTER);
    }

    private Label renderMessageArea(VerticalLayout layout) {
        CssLayout messageArea = new CssLayout();
        messageArea.setMargin(true);
        messageArea.setWidth(PANEL_WIDTH);
        Label label = new Label("", Label.CONTENT_XHTML); //$NON-NLS-1$
        label.addStyleName(STYLE_ISSUES);
        label.setVisible(false);
        messageArea.addComponent(label);
        layout.addComponent(messageArea);
        layout.setComponentAlignment(messageArea, Alignment.MIDDLE_CENTER);
        return label;
    }

    private void setMessage(Label label, String message) {
        if (StringUtils.isNotEmpty(message)) {
            label.setValue(message);
            label.setVisible(true);
        } else {
            label.setVisible(false);
        }
        label.requestRepaint();
    }

    private void setMessage(String message) {
        setMessage(headerLabel, message);
        setMessage(footerLabel, message);
    }

    private void setMessage(SortedSet<Issue> issues, Map<String, String> displayNames) {
        String message = Issue.asHTMLList(null, issues, displayNames);
        setMessage(headerLabel, message);
        setMessage(footerLabel, message);
    }

    /**
     * Renders a OK/Cancel/Validate/Expand All/Collapse All button bar.
     */
    @SuppressWarnings("serial")
    private Button renderButtons(VerticalLayout layout, boolean header) {
        CssLayout buttons = new CssLayout();
        buttons.addStyleName(STYLE_EDIT_PROJECT_BUTTONS);
        String prefix = header ? HEADER : FOOTER;

        Button okButton = new Button("OK");
        okButton.setIcon(ICON_BUTTON_OK);
        okButton.setDescription("Save the modified project");
        okButton.addStyleName(prefix + BUTTON_OK);
        okButton.addListener(new OKButtonListener());
        buttons.addComponent(okButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setIcon(ICON_BUTTON_CANCEL);
        cancelButton.setDescription("Discard all changes to the project");
        cancelButton.addStyleName(prefix + BUTTON_CANCEL);
        cancelButton.addListener(new CancelButtonListener());
        buttons.addComponent(cancelButton);

        Button checkButton = new Button("Check");
        checkButton.setIcon(ICON_BUTTON_VALIDATE);
        checkButton.setDescription("Checks the modified project for issues without saving it");
        checkButton.addStyleName(prefix + BUTTON_VALIDATE);
        checkButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                validateModifiedProject();
            }
        });
        buttons.addComponent(checkButton);

        Button expandAllButton = new Button("Expand All");
        expandAllButton.setIcon(ICON_BUTTON_EXPAND_ALL);
        expandAllButton.setDescription("Expand all panels");
        expandAllButton.addStyleName(prefix + BUTTON_EXPAND_ALL);
        expandAllButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                expandAllPanels();
            }
        });
        buttons.addComponent(expandAllButton);

        Button collapseAllButton = new Button("Collapse All");
        collapseAllButton.setIcon(ICON_BUTTON_COLLAPSE_ALL);
        collapseAllButton.setDescription("Collapse all panels");
        collapseAllButton.addStyleName(prefix + BUTTON_COLLAPSE_ALL);
        collapseAllButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                collapseAllPanels();
            }
        });
        buttons.addComponent(collapseAllButton);

        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
        return checkButton;
    }

    /**
     * Renders the panels in the order defined by the comparator of {@link #entries}.
     */
    private void renderPanels(VerticalLayout layout) {
        for (ProjectEditPanelEntry entry : entries) {
            layout.addComponent(entry);
            layout.setComponentAlignment(entry, Alignment.MIDDLE_CENTER);
        }
    }

    private void collapseAllPanels() {
        for (ProjectEditPanelEntry entry : entries) {
            entry.collapse();
        }
    }

    private void expandAllPanels() {
        for (ProjectEditPanelEntry entry : entries) {
            entry.expand();
        }
    }

    /**
     * Validates the modified project with {@link Severity#INFO} and renders the result.
     */
    private void validateModifiedProject() {
        headerCheckButton.setEnabled(false);
        footerCheckButton.setEnabled(false);
        commitForms();
        renderIssues(null, false);
        progressIndicator.setValue(0f);
        indicatorArea.setVisible(true);
        progressThread = new ProgressThread();
        validatorThread = new ValidatorThread();
        progressThread.start();
        validatorThread.start();
    }

    private void updateProgressIndicator() {
        if (validatorThread != null && validatorThread.isFinished()) {
            indicatorArea.setVisible(false);
            if (persistedIssues != null) {
                persistedIssues.addLatestDuration(validatorThread.getDuration());
            }
            progressThread.interrupt();
            validatorThread.interrupt();
            progressThread = null;
            validatorThread = null;
            headerCheckButton.setEnabled(true);
            footerCheckButton.setEnabled(true);
        } else if (progressThread != null) {
            progressIndicator.setValue(progressThread.progress());
        } else {
            progressIndicator.setValue(0f);
        }
    }

    private class ProgressThread extends Thread {
        private static final long SLEEPING_TIME = 300L; // 1 seconds milliseconds
        private static final long UNKNOWN_AVERAGE_DURATION = 30000L; // 10 seconds

        private long elapsedTime;
        private long averageDuration;

        @Override
        public void run() {
            averageDuration = UNKNOWN_AVERAGE_DURATION;
            if (persistedIssues != null && persistedIssues.getAverageDuration() > 0) {
                averageDuration = persistedIssues.getAverageDuration();
            }

            for (; elapsedTime < averageDuration; elapsedTime += SLEEPING_TIME) {
                try {
                    Thread.sleep(SLEEPING_TIME);
                } catch (InterruptedException e) {
                    return;
                }
                synchronized (getApplication()) {
                    updateProgressIndicator();
                }
            }
        }

        private float progress() {
            return Math.min((float) elapsedTime / averageDuration, 0.95f); // never return 100%
        }
    }

    private class ValidatorThread extends Thread {
        private long startTime;
        private long duration = -1L;

        @Override
        public void run() {
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            startTime = System.currentTimeMillis();
            SortedSet<Issue> issues = projectService.validate(modifiedProject, Severity.INFO);
            synchronized (getApplication()) {
                renderIssues(issues, false);
                if (issues.size() == 0) {
                    getWindow().showNotification("No Issues Found");
                }
                duration = System.currentTimeMillis() - startTime;
                updateProgressIndicator();
            }
        }

        public boolean isFinished() {
            return duration >= 0;
        }

        public long getDuration() {
            return duration;
        }
    }

    /**
     * Renders the persisted validation issues. If the persisted issues are
     * stale, render a corresponding message instead.
     */
    private void renderPersistedIssues() {
        if (persistedIssues != null) {
            if (persistedIssues.isStale()) {
                setMessage("<ul><li class=\"STALE\">" +
                        "No information about issues available. Use the <b>Validate</b> button " +
                        "above to validate the project now.</li></ul>");
            } else {
                renderIssues(persistedIssues.getIssues(), false);
            }
        }
    }

    /**
     * Renders validation issues given by a <code>ValidationException</code>
     * merged with the persisted issues (if any).
     *
     * @param e  the validation exception to render.
     */
    private void renderIssues(ValidationException e) {
        TreeSet<Issue> issues = new TreeSet<Issue>(e.getIssues());
        renderIssues(issues, true);
    }

    /**
     * Renders the given validation issues.
     *
     * If <code>collapseValid</code> is set all panels without issues will be collapsed
     * to focus the view of the user to panels with issues.
     */
    private void renderIssues(SortedSet<Issue> issues, boolean collapseValid) {
        Map<String, SortedSet<Issue>> sortedIssues = new HashMap<String, SortedSet<Issue>>();
        sortIssuesByExtension(issues, sortedIssues);

        // render issues that are related to an extension
        for (ProjectEditPanelEntry entry : entries) {
            String extensionName = entry.getExtensionClassName();
            SortedSet<Issue> extensionIssues = sortedIssues.get(extensionName);
            entry.showIssues(extensionIssues, collapseValid);
        }

        // render issues in the message areas
        setMessage(issues, displayNames);
    }

    /**
     * Sorts the issues by extension and adds them to <code>extensionIssues</code> according to the
     * extension they belong to.
     */
    private void sortIssuesByExtension(SortedSet<Issue> issues, Map<String, SortedSet<Issue>> extensionIssues) {
        if (issues != null) {
            for (Issue issue : issues) {
                Class<? extends ExtensionEntityBase> extension = issue.getExtension();
                if (extension != null) {
                    String extensionName = extension.getName();
                    addIssue(extensionIssues, extensionName, issue);
                }
            }
        }
    }

    private void addIssue(Map<String, SortedSet<Issue>> issues, String extensionName, Issue issue) {
        SortedSet<Issue> set = issues.get(extensionName);
        if (set == null) {
            set = new TreeSet<Issue>();
        }
        set.add(issue);
        issues.put(extensionName, set);
    }

    /**
     * Commits the forms of all enabled panels, so that fresh input from the Vaadin
     * fields is copied to the modified project. Note, this method does not persist
     * the modified project!
     */
    private void commitForms() {
        for (ProjectEditPanelEntry entry : entries) {
            if (entry.isEnabled()) {
                try {
                    entry.commit();
                } catch (InvalidValueException e) {
                    // we do not support Vaadin validation (see DefaultProjectFieldFactory),
                    // but if something bad happens, we at least should log it
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Persists the current modified project and removes persisted issues.
     *
     * Sets the current system time as {@link Project#setRegistered(long) registration time},
     * if a new project is saved for the first time.
     *
     * @throws ValidationException if there are {@link org.eclipse.skalli.model.ext.Severity#FATAL fatal}
     * validation issues.
     */
    private void commit() throws ValidationException {
        if (ProjectEditMode.NEW_PROJECT.equals(mode)) {
            modifiedProject.setRegistered(System.currentTimeMillis());
        }
        commitModifiedProject();
        clearPersistedIssues();
    }

    private void commitModifiedProject() throws ValidationException {
        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        projectService.persist(modifiedProject, application.getLoggedInUser());
    }

    /**
     * Removes persisted issues for this project, but marks the corresponding
     * {@link Issues} entry as stale.
     */
    private void clearPersistedIssues() throws ValidationException {
        IssuesService issuesService = Services.getService(IssuesService.class);
        if (issuesService != null) {
            Issues emptyIssues = new Issues(modifiedProject.getUuid());
            emptyIssues.setStale(true);
            issuesService.persist(emptyIssues, application.getLoggedInUser());
        }
    }

    private class OKButtonListener implements Button.ClickListener {
        private static final long serialVersionUID = 6531396291087032954L;

        @Override
        public void buttonClick(ClickEvent event) {
            try {
                commitForms();
                List<String> dataLossWarnings = getDataLossWarnings();
                List<String> confirmationWarnings = getConfirmationWarnings();
                if (dataLossWarnings.isEmpty() && confirmationWarnings.isEmpty()) {
                    doCommit();
                } else {
                    ConfirmPopup popup = new ConfirmPopup(dataLossWarnings, confirmationWarnings,
                            new ConfirmPopup.OnConfirmation() {
                                @Override
                                public void onConfirmation(boolean confirmed) {
                                    if (confirmed) {
                                        doCommit();
                                    }
                                }
                            });
                    getWindow().addWindow(popup);
                }
            } catch (RuntimeException e) {
                // If something bad happens in a validator, in the form commit or
                // while persisting the project, we log the incident and render the exception
                renderException(e);
                LOG.error(e.getMessage(), e);
            }
        }

        private void renderException(Throwable t) {
            StringBuilder sb = new StringBuilder();
            sb.append("<strong class=\"").append(STYLE_EDIT_PROJECT_ERROR).append("\">"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("Failed to commit changes. An internal error occured. See log for details.");
            sb.append("</strong>"); //$NON-NLS-1$
            setMessage(sb.toString());
        }

        private void doCommit() {
            try {
                commit();
                application.refresh(modifiedProject);
                application.refresh((Project) project.getParentEntity());
                application.refresh((Project) modifiedProject.getParentEntity());
                if (!modifiedProject.isDeleted()) {
                    refreshSubProjects();
                    navigator.navigateProjectView(modifiedProject);
                }
                else {
                    navigator.navigateWelcomeView();
                }
            } catch (ValidationException e) {
                renderIssues(e);
                LOG.debug(e.getMessage(), e);
            }
        }

        private void refreshSubProjects() {
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            ProjectNode node = projectService.getProjectNode(modifiedProject.getUuid(), null);
            refreshNode(projectService, node);
        }

        private void refreshNode(ProjectService projectService, ProjectNode node) {
            for (ProjectNode child : node.getSubProjects()) {
                application.refresh(child.getProject());
                refreshNode(projectService, child);
            }
        }

        private List<String> getDataLossWarnings() {
            List<String> warnings = new ArrayList<String>();
            for (ExtensionEntityBase extension : project.getAllExtensions()) {
                Class<? extends ExtensionEntityBase> extensionClass = extension.getClass();
                if (modifiedProject.isInherited(extensionClass) && !project.isInherited(extensionClass)) {
                    warnings.add(asWarning(WARN_EXTENSION_INHERITED, extension));
                }
                else if (modifiedProject.getExtension(extensionClass) == null) {
                    warnings.add(asWarning(WARN_EXTENSION_DISABLED, extension));
                }
            }
            return warnings;
        }

        private List<String> getConfirmationWarnings() {
            User modifier = UserUtils.getUser(application.getLoggedInUser());
            List<String> warnings = new ArrayList<String>();
            for (ExtensionEntityBase extension : project.getAllExtensions()) {
                Class<? extends ExtensionEntityBase> extensionClass = extension.getClass();
                ExtensionService<?> extensionService = ExtensionServices.getExtensionService(extensionClass);
                if (extensionService != null) {
                    warnings.addAll(extensionService.getConfirmationWarnings(project, modifiedProject, modifier));
                }
            }
            return warnings;
        }

        private String asWarning(String pattern, ExtensionEntityBase extension) {
            String displayName = displayNames.get(extension.getClass().getName());
            if (pattern == WARN_EXTENSION_DISABLED) {
                return MessageFormat.format(pattern, displayName);
            }
            return MessageFormat.format(pattern, displayName,
                    ((Project) extension.getExtensibleEntity()).getName());
        }
    }

    private static class ConfirmPopup extends Window implements Button.ClickListener {
        private static final long serialVersionUID = 6695547216798144892L;

        public interface OnConfirmation {
            public void onConfirmation(boolean confirmed);
        }

        private OnConfirmation callback;
        private Button yes = new Button("Yes", this);
        private Button no = new Button("No", this);

        public ConfirmPopup(List<String> dataLossWarnings, List<String> confirmationWarnings, OnConfirmation callback) {
            super("Confirmation of Changes");
            setModal(true);
            setWidth(CONFIRM_POPUP_WIDTH);
            this.callback = callback;

            StringBuilder sb = new StringBuilder();
            append(sb, "The following changes will <strong>remove data permanently</strong> from the project:",
                    dataLossWarnings);
            append(sb, "The following changes might not be your intention:", confirmationWarnings);
            sb.append("<p>Continue anyway?</p>");
            Label content = new Label(sb.toString(), Label.CONTENT_XHTML);
            addComponent(content);

            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            hl.addComponent(yes);
            hl.addComponent(no);
            addComponent(hl);
        }

        @SuppressWarnings("nls")
        private void append(StringBuilder sb, String title, List<String> messages) {
            if (messages.size() > 0) {
                sb.append("<p>").append(title).append("</p>");
                sb.append("<p><ul>");
                for (String message : messages) {
                    sb.append("<li>").append(message).append("</li>");
                }
                sb.append("</ul></p>");
            }
        }

        @Override
        public void buttonClick(ClickEvent event) {
            if (getParent() != null) {
                ((Window) getParent()).removeWindow(this);
            }
            callback.onConfirmation(event.getSource() == yes);
        }
    }

    private class CancelButtonListener implements Button.ClickListener {
        private static final long serialVersionUID = 4567366927195161150L;

        @Override
        public void buttonClick(ClickEvent event) {
            if (mode.equals(ProjectEditMode.EDIT_PROJECT)) {
                navigator.navigateProjectView(project);
            }
            else if (mode.equals(ProjectEditMode.NEW_PROJECT)) {
                navigator.navigateWelcomeView();
            }
        }
    }

    private class Context implements ProjectEditContext {
        private final ProjectApplication application;
        private final ProjectTemplate projectTemplate;
        private final ProjectEditMode mode;

        public Context(ProjectApplication application, ProjectTemplate projectTemplate, ProjectEditMode mode) {
            this.projectTemplate = projectTemplate;
            this.application = application;
            this.mode = mode;
        }

        @Override
        public ProjectTemplate getProjectTemplate() {
            return projectTemplate;
        }

        @Override
        public boolean isAdministrator() {
            return GroupUtils.isAdministrator(application.getLoggedInUser());
        }

        @Override
        public ProjectEditMode getProjectEditMode() {
            return mode;
        }

        @Override
        public void onPropertyChanged(String propertyId, Object newValue) {
            for (ProjectEditPanelEntry entry : entries) {
                entry.onPropertyChanged(propertyId, newValue);
            }
        }
    }

    private static class ProjectEditPanelComparator implements Comparator<ProjectEditPanelEntry> {
        private ProjectTemplate projectTemplate;

        public ProjectEditPanelComparator(ProjectTemplate projectTemplate) {
            this.projectTemplate = projectTemplate;
        }

        @Override
        public int compare(ProjectEditPanelEntry o1, ProjectEditPanelEntry o2) {
            int result = Float.compare(getRank(o1), getRank(o2));
            if (result == 0) {
                // same rank: compare states (required < visible < enabled
                result = compareStates(o1, o2);
                if (result == 0) {
                    // same rank and state: order captions alphabetically
                    result = o1.getDisplayName().compareTo(o2.getDisplayName());
                }
            }
            return result;
        }

        private float getRank(ProjectEditPanelEntry o) {
            float rank = projectTemplate.getRank(o.getExtensionClassName());
            if (rank < 0) {
                rank = o.getRank();
                if (rank < 0) {
                    rank = Float.MAX_VALUE;
                }
            }
            return rank;
        }

        private int compareStates(ProjectEditPanelEntry o1, ProjectEditPanelEntry o2) {
            String ext1 = o1.getExtensionClassName();
            String ext2 = o2.getExtensionClassName();
            int result = compareStates(projectTemplate.isVisible(ext1), projectTemplate.isVisible(ext2));
            if (result == 0) {
                result = compareStates(projectTemplate.isEnabled(ext1), projectTemplate.isEnabled(ext2));
            }
            return result;
        }

        private int compareStates(boolean state1, boolean state2) {
            if (state1) {
                return state2 ? 0 : -1;
            } else {
                return state2 ? 1 : 0;
            }
        }
    }
}
