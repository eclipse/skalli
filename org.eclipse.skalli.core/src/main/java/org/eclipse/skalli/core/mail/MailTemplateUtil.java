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
package org.eclipse.skalli.core.mail;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.skalli.model.Project;

public class MailTemplateUtil {

    /**
     * contains velocity templates that can be used to send an automated
     * email when the project parent has changed.
     */
    public final static MailTemplate PARENT_CHANGED = new MailTemplateImpl(
            "org/eclipse/skalli/core/internal/mail/ParentChangedSubject.vm",
            "org/eclipse/skalli/core/internal/mail/ParentChangedBody.vm");

    // parameters that are passed to all velocity templates
    private final static String PARAMETER_PROJECT = "project";

    // set up the velocity engine once for this class
    private static VelocityEngine velocityEngine = new VelocityEngine();
    static {
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        try {
            velocityEngine.init(properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * private constructor, use only static methods of this utility class
     */
    private MailTemplateUtil() {
    }

    /**
     * returns the body of an email as a string parsed by the velocity engine. the velocity
     * template to be used is described by the mail template and the project instance can be used
     * by the template to show access relevant information.
     *
     * @param mailTemplate mail template
     * @param project this project can be used by the template
     * @return subject
     * @throws Exception exception that can be thrown by velocity engine
     */
    public static String getBody(MailTemplate mailTemplate, Project project) throws Exception {
        return processTemplate(mailTemplate.getBodyTemplate(), project);
    }

    /**
     * returns the subject of an email as a string parsed by the velocity engine. the velocity
     * template to be used is described by the mail template and the project instance can be used
     * by the template to show access relevant information.
     *
     * @param mailTemplate mail template
     * @param project this project can be used by the template
     * @return subject
     * @throws Exception exception that can be thrown by velocity engine
     */
    public static String getSubject(MailTemplate mailTemplate, Project project) throws Exception {
        return processTemplate(mailTemplate.getSubjectTemplate(), project);
    }

    /*
     * internal stuff
     */

    private static String processTemplate(String templateFile, Project project) throws Exception {
        Template template = velocityEngine.getTemplate(templateFile);
        VelocityContext context = new VelocityContext();
        context.put(PARAMETER_PROJECT, project);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    static class MailTemplateImpl implements MailTemplate {
        private String bodyTemplate;
        private String headerTemplate;

        private MailTemplateImpl(String headerTemplate, String bodyTemplate) {
            this.bodyTemplate = bodyTemplate;
            this.headerTemplate = headerTemplate;
        }

        @Override
        public String getBodyTemplate() {
            return bodyTemplate;
        }

        @Override
        public String getSubjectTemplate() {
            return headerTemplate;
        }
    }

}
