package org.eclipse.skalli.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("nls")
public class PatternUtil {

    public static String adjustProjectName(String projectName) {

        Pattern pattern = Pattern.compile("[\\s]");
        Matcher matcher = pattern.matcher(projectName);

        return matcher.replaceAll("_");
    }
}
