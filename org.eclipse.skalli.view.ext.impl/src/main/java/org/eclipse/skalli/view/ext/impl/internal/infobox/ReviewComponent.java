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
package org.eclipse.skalli.view.ext.impl.internal.infobox;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ext.misc.ProjectRating;
import org.eclipse.skalli.model.ext.misc.ProjectRatingStyle;
import org.eclipse.skalli.model.ext.misc.ReviewEntry;
import org.eclipse.skalli.model.ext.misc.ReviewProjectExt;
import org.eclipse.skalli.view.ext.ExtensionUtil;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ReviewComponent extends CustomComponent {

    private static final long serialVersionUID = 5778729327534523225L;

    private final ThemeResource ICON_THUMB_UP = new ThemeResource("icons/rating/thumb-up.png"); //$NON-NLS-1$
    private final ThemeResource ICON_THUMB_DOWN = new ThemeResource("icons/rating/thumb-down.png"); //$NON-NLS-1$
    private final ThemeResource ICON_FACE_CRYING = new ThemeResource("icons/rating/face-crying.png"); //$NON-NLS-1$
    private final ThemeResource ICON_FACE_SAD = new ThemeResource("icons/rating/face-sad.png"); //$NON-NLS-1$
    private final ThemeResource ICON_FACE_PLAIN = new ThemeResource("icons/rating/face-plain.png"); //$NON-NLS-1$
    private final ThemeResource ICON_FACE_SMILE = new ThemeResource("icons/rating/face-smile.png"); //$NON-NLS-1$
    private final ThemeResource ICON_FACE_SMILE_BIG = new ThemeResource("icons/rating/face-smile-big.png"); //$NON-NLS-1$

    private final ThemeResource ICON_BUTTON_OK = new ThemeResource("icons/button/ok.png"); //$NON-NLS-1$
    private final ThemeResource ICON_BUTTON_CANCEL = new ThemeResource("icons/button/cancel.png"); //$NON-NLS-1$

    private static final int MILLIS_PER_SECOND = 1000;
    private static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    private static final int MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    private static final int MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

    private static final String HSPACE = "&nbsp;&nbsp;&nbsp;&nbsp;"; //$NON-NLS-1$

    private Project project;
    private ReviewProjectExt extension;
    private List<ReviewEntry> reviews;
    private int size;
    private int defaultPageLength;
    private int maxPageLength;
    private int currentPageLength;
    private boolean showAll;
    private ExtensionUtil util;
    private ProjectRatingStyle ratingStyle;

    private Layout layout;
    private GridLayout reviewGrid;
    private Button morelessButton;
    private Button nextButton;
    private Button prevButton;
    private CssLayout reviewButtons;
    private Window reviewPopup;
    private Label ratioLabel;

    private int currentPage;
    private int lastPage;

    @SuppressWarnings("serial")
    public ReviewComponent(Project project, int defaultPageLength, int maxPageLength, ExtensionUtil util) {
        this.project = project;
        this.defaultPageLength = defaultPageLength;
        this.maxPageLength = maxPageLength;
        this.util = util;

        extension = project.getExtension(ReviewProjectExt.class);
        if (extension == null) {
            extension = new ReviewProjectExt();
            extension.setExtensibleEntity(project);
            project.addExtension(extension);
        }
        reviews = extension.getReviews();
        ratingStyle = extension.getRatingStyle();

        showAll = false;
        size = reviews.size();
        currentPage = 0;
        currentPageLength = defaultPageLength;
        lastPage = size / maxPageLength;

        layout = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c instanceof Button) {
                    return "padding-top:3px;padding-right:15px"; //$NON-NLS-1$
                }
                if (c instanceof Label) {
                    return "padding-bottom:10px"; //$NON-NLS-1$
                }
                return "padding-top:3px"; //$NON-NLS-1$
            }
        };
        layout.setSizeFull();

        paintReviewButtons();
        paintReviewList();

        setCompositionRoot(layout);
    }

    private void paintReviewList() {
        if (size > 0) {
            painRatioLabel();
            paintReviewGrid();
            paintPageButtons();
        }
    }

    private void painRatioLabel() {
        String ratioLabelValue = null;
        if (ProjectRatingStyle.TWO_STATES.equals(ratingStyle)) {
            ratioLabelValue = "<span style=\"font-weight:bold\">" //$NON-NLS-1$
                    + extension.getRecommendedRatio() + " of " + extension.getNumberVotes()
                    + " users recommend this project"
                    + "</span>"; //$NON-NLS-1$
        } else if (ProjectRatingStyle.FIVE_STATES.equals(ratingStyle)) {
            ratioLabelValue = "<span style=\"font-weight:bold\">" //$NON-NLS-1$
                    + "Average rating of this project by " + extension.getNumberVotes()
                    + " users: "
                    + getDescription(extension.getAverageRating()) + "</span>"; //$NON-NLS-1$
        }
        if (ratioLabel == null) {
            ratioLabel = new Label(ratioLabelValue, Label.CONTENT_XHTML);
            layout.addComponent(ratioLabel);
        } else {
            ratioLabel.setValue(ratioLabelValue);
        }
    }

    private void paintReviewGrid() {
        if (reviewGrid == null) {
            reviewGrid = new GridLayout(2, currentPageLength);
            reviewGrid.setSizeFull();
            reviewGrid.setSpacing(true);
            layout.addComponent(reviewGrid);
        } else {
            reviewGrid.removeAllComponents();
        }
        int rows = reviewGrid.getRows();
        if (rows != currentPageLength) {
            reviewGrid.setRows(Math.min(size, currentPageLength));
        }

        int row = 0;
        List<ReviewEntry> latestReviews = getLatestReviews(currentPage, currentPageLength);
        for (ReviewEntry review : latestReviews) {
            ProjectRating rating = review.getRating();
            Embedded e = new Embedded(null, getIcon(rating));
            e.setDescription(getDescription(rating));
            e.setWidth("22px"); //$NON-NLS-1$
            e.setHeight("22px"); //$NON-NLS-1$
            reviewGrid.addComponent(e, 0, row);

            StringBuilder sb = new StringBuilder();
            sb.append("<span style=\"white-space:normal\">"); //$NON-NLS-1$
            sb.append(HtmlUtils.clean(review.getComment()));
            sb.append("<br>"); //$NON-NLS-1$
            sb.append("<span style=\"font-size:x-small\">").append(" posted by "); //$NON-NLS-1$
            sb.append(StringEscapeUtils.escapeHtml(review.getVoter()));
            sb.append(" "); //$NON-NLS-1$

            long deltaMillis = System.currentTimeMillis() - review.getTimestamp();
            long deltaDays = deltaMillis / MILLIS_PER_DAY;
            if (deltaDays > 0) {
                sb.append(deltaDays);
                sb.append(" days ago");
            } else {
                long deltaHours = deltaMillis / MILLIS_PER_HOUR;
                if (deltaHours > 0) {
                    sb.append(deltaHours).append(" hours ago");
                } else {
                    long deltaMinutes = deltaMillis / MILLIS_PER_MINUTE;
                    if (deltaMinutes > 0) {
                        sb.append(deltaMinutes).append(" minutes ago");
                    } else {
                        sb.append(" just now");
                    }
                }
                sb.append("</span></span>"); //$NON-NLS-1$
            }
            CssLayout css = new CssLayout();
            css.setSizeFull();
            Label comment = new Label(sb.toString(), Label.CONTENT_XHTML);
            comment.setSizeUndefined();
            css.addComponent(comment);
            reviewGrid.addComponent(css, 1, row);
            reviewGrid.setColumnExpandRatio(1, 1.0f);
            ++row;
        }
    }

    private ThemeResource getIcon(ProjectRating rating) {
        ThemeResource icon = null;
        switch (rating) {
        case UP:
            icon = ICON_THUMB_UP;
            break;
        case DOWN:
            icon = ICON_THUMB_DOWN;
            break;
        case FACE_CRYING:
            icon = ICON_FACE_CRYING;
            break;
        case FACE_SAD:
            icon = ICON_FACE_SAD;
            break;
        case FACE_PLAIN:
            icon = ICON_FACE_PLAIN;
            break;
        case FACE_SMILE:
            icon = ICON_FACE_SMILE;
            break;
        case FACE_SMILE_BIG:
            icon = ICON_FACE_SMILE_BIG;
            break;
        }
        return icon;
    }

    private String getDescription(ProjectRating rating) {
        String description = null;
        switch (rating) {
        case UP:
            description = "Recommended";
            break;
        case DOWN:
            description = "Not Recommended";
            break;
        case FACE_CRYING:
            description = "Lousy!";
            break;
        case FACE_SAD:
            description = "Poor";
            break;
        case FACE_PLAIN:
            description = "Average";
            break;
        case FACE_SMILE:
            description = "Good";
            break;
        case FACE_SMILE_BIG:
            description = "Excellent!";
            break;
        }
        return description;
    }

    private String getRatingQuestion() {
        switch (ratingStyle) {
        case TWO_STATES:
            return "Would you recommend this project?";
        case FIVE_STATES:
            return "How would you rate this project?";
        }
        return null;
    }

    private String getReviewButtonsHeight() {
        switch (ratingStyle) {
        case TWO_STATES:
            return "80px"; //$NON-NLS-1$
        case FIVE_STATES:
            return "70px"; //$NON-NLS-1$
        }
        return "0px"; //$NON-NLS-1$
    }

    private String getReviewComment(ProjectRating rating) {
        String comment = null;
        switch (rating) {
        case UP:
            comment = "I recommend this project!";
            break;
        case DOWN:
            comment = "I do not recommend this project!";
            break;
        case FACE_CRYING:
            comment = "I think this project is lousy!";
            break;
        case FACE_SAD:
            comment = "I think this project is poor!";
            break;
        case FACE_PLAIN:
            comment = "I think this project is average!";
            break;
        case FACE_SMILE:
            comment = "I think this project is good!";
            break;
        case FACE_SMILE_BIG:
            comment = "I think this project is excellent!";
            break;
        }
        return comment;
    }

    private String getReviewCommentQuestion(ProjectRating rating) {
        String question = null;
        switch (rating) {
        case UP:
            question = "Why do you recommend this project?";
            break;
        case DOWN:
            question = "Why do you not recommend this project?";
            break;
        case FACE_CRYING:
            question = "Why do you think this project is lousy?";
            break;
        case FACE_SAD:
            question = "Why do you think this project is poor?";
            break;
        case FACE_PLAIN:
            question = "Why do you think this project is average?";
            break;
        case FACE_SMILE:
            question = "Why do you think this project is good?";
            break;
        case FACE_SMILE_BIG:
            question = "Why do you think this project is excellent?";
            break;
        }
        return question;
    }

    private void paintPageButtons() {
        paintMoreLessButton();
        paintPrevNextButtons();
        paintButtonStates();
    }

    @SuppressWarnings({ "deprecation", "serial" })
    private void paintMoreLessButton() {
        if (morelessButton == null) {
            morelessButton = new Button();
            morelessButton.setStyleName(Button.STYLE_LINK);
            morelessButton.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    if (showAll) {
                        showAll = false;
                        currentPage = 0;
                        currentPageLength = defaultPageLength;
                        lastPage = size / currentPageLength;
                        paintButtonStates();
                    } else {
                        showAll = true;
                        currentPage = 0;
                        currentPageLength = maxPageLength;
                        lastPage = size / currentPageLength;
                        paintButtonStates();
                    }
                    paintReviewGrid();
                }
            });
            layout.addComponent(morelessButton);
        }
    }

    @SuppressWarnings({ "serial", "deprecation" })
    private void paintPrevNextButtons() {
        if (prevButton == null) {
            prevButton = new Button();
            prevButton.setStyleName(Button.STYLE_LINK);
            prevButton.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    --currentPage;
                    paintReviewGrid();
                    paintButtonStates();
                }
            });
            layout.addComponent(prevButton);
        }
        if (nextButton == null) {
            nextButton = new Button();
            nextButton.setStyleName(Button.STYLE_LINK);
            nextButton.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    ++currentPage;
                    paintReviewGrid();
                    paintButtonStates();
                }
            });
            layout.addComponent(nextButton);
        }
    }

    private void paintButtonStates() {
        String caption = showAll ? "Show Latest Reviews" : "Show All Reviews";
        morelessButton.setCaption(caption);

        if (showAll || size > currentPageLength) {
            morelessButton.setEnabled(true);
        } else {
            morelessButton.setEnabled(false);
        }

        if (showAll && size > currentPageLength) {
            nextButton.setVisible(true);
            nextButton.setCaption("Next " + currentPageLength + " Reviews");

            prevButton.setVisible(true);
            prevButton.setCaption("Previous " + currentPageLength + " Reviews");

            if (currentPage == lastPage || size <= currentPageLength) {
                nextButton.setEnabled(false);
            } else {
                nextButton.setEnabled(true);
            }
            if (currentPage == 0 || size <= currentPageLength) {
                prevButton.setEnabled(false);
            } else {
                prevButton.setEnabled(true);
            }
        } else {
            nextButton.setVisible(false);
            prevButton.setVisible(false);
        }
    }

    @SuppressWarnings("serial")
    private void paintReviewButtons() {
        reviewButtons = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c instanceof HorizontalLayout) {
                    return "padding-left: 15px; padding-top: 10px;"; //$NON-NLS-1$
                } else {
                    return StringUtils.EMPTY;
                }
            }
        };
        reviewButtons.setWidth("300px"); //$NON-NLS-1$
        reviewButtons.setHeight(getReviewButtonsHeight());

        Label label = new Label("<b>" + getRatingQuestion() + "</b>", Label.CONTENT_XHTML); //$NON-NLS-1$ //$NON-NLS-2$
        reviewButtons.addComponent(label);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeUndefined();

        final String separatorLabelCaption = "<b>" + HSPACE + "or" + HSPACE + "</b>"; //$NON-NLS-1$ //$NON-NLS-3$
        ProjectRating[] ratings = ProjectRating.getRatings(ratingStyle);
        int i = 0;
        for (ProjectRating rating : ratings) {
            if (i > 0) {
                Label separatorLabel = new Label(separatorLabelCaption, Label.CONTENT_XHTML);
                hl.addComponent(separatorLabel);
                hl.setComponentAlignment(separatorLabel, Alignment.MIDDLE_LEFT);
            }
            paintReviewButton(hl, rating);
            ++i;
        }

        reviewButtons.addComponent(hl);
        layout.addComponent(reviewButtons);
    }

    @SuppressWarnings({ "serial", "deprecation" })
    private void paintReviewButton(HorizontalLayout hl, final ProjectRating rating) {
        Button btn = new Button();
        if (util.getLoggedInUser() != null) {
            btn.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    reviewPopup = createReviewWindow(rating);
                    getWindow().addWindow(reviewPopup);
                }
            });
            btn.setDescription(getDescription(rating));
        } else {
            btn.setEnabled(false);
            btn.setDescription("Login to rate this project.");
        }
        btn.setStyleName(Button.STYLE_LINK);
        btn.setIcon(getIcon(rating));
        hl.addComponent(btn);
    }

    @SuppressWarnings("serial")
    private Window createReviewWindow(final ProjectRating rating) {
        final Window subwindow = new Window("Rate and Review");
        subwindow.setModal(true);
        subwindow.setWidth("420px"); //$NON-NLS-1$
        subwindow.setHeight("320px"); //$NON-NLS-1$

        VerticalLayout vl = (VerticalLayout) subwindow.getContent();
        vl.setSpacing(true);
        vl.setSizeFull();

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeUndefined();

        Embedded icon = new Embedded(null, getIcon(rating));
        Label iconLabel = new Label("<b>" + HSPACE + getReviewComment(rating) + "</b>", Label.CONTENT_XHTML); //$NON-NLS-1$ //$NON-NLS-2$
        String captionTextField = getReviewCommentQuestion(rating);
        hl.addComponent(icon);
        hl.addComponent(iconLabel);
        hl.setComponentAlignment(iconLabel, Alignment.MIDDLE_LEFT);
        vl.addComponent(hl);

        final TextField editor = new TextField(captionTextField);
        editor.setRows(3);
        editor.setColumns(30);
        editor.setImmediate(true);
        vl.addComponent(editor);

        final User user = util.getLoggedInUser();
        final ArrayList<String> userSelects = new ArrayList<String>(2);
        userSelects.add("I want to vote as " + user.getDisplayName());
        if (extension.getAllowAnonymous()) {
            userSelects.add("I want to vote as Anonymous!");
        }
        final OptionGroup userSelect = new OptionGroup(null, userSelects);
        userSelect.setNullSelectionAllowed(false);
        userSelect.select(userSelects.get(0));
        vl.addComponent(userSelect);

        CssLayout css = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                return "margin-left:5px;margin-right:5px;margin-top:10px"; //$NON-NLS-1$
            }
        };

        Button okButton = new Button("OK");
        okButton.setIcon(ICON_BUTTON_OK);
        okButton.setDescription("Commit changes");
        okButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                String comment = (String) editor.getValue();
                if (StringUtils.isBlank(comment)) {
                    comment = "No Comment";
                }
                ((Window) subwindow.getParent()).removeWindow(subwindow);
                String userName = "Anonymous";
                if (userSelects.get(0).equals(userSelect.getValue())) {
                    userName = user.getDisplayName();
                }
                ReviewEntry review = new ReviewEntry(rating, comment, userName, System.currentTimeMillis());
                extension.addReview(review);
                util.persist(project);
                reviews = extension.getReviews();
                size = reviews.size();
                currentPage = 0;
                lastPage = size / currentPageLength;
                paintReviewList();
            }
        });
        css.addComponent(okButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setIcon(ICON_BUTTON_CANCEL);
        cancelButton.setDescription("Discard changes");
        cancelButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                ((Window) subwindow.getParent()).removeWindow(subwindow);
            }
        });
        css.addComponent(cancelButton);

        vl.addComponent(css);
        vl.setComponentAlignment(css, Alignment.MIDDLE_CENTER);

        return subwindow;
    }

    private List<ReviewEntry> getLatestReviews(int currentPage, int len) {
        int first = size - currentPage * currentPageLength - 1;
        if (first >= size) {
            first = size - 1;
        }
        int last = first - len + 1;
        if (last < 0) {
            last = 0;
        }
        ArrayList<ReviewEntry> result = new ArrayList<ReviewEntry>();
        for (int i = first; i >= last; --i) {
            result.add(reviews.get(i));
        }
        return result;
    }
}
