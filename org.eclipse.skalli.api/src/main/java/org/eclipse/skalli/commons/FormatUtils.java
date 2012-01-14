package org.eclipse.skalli.commons;

import org.apache.commons.lang.time.DateFormatUtils;

public class FormatUtils {

    public static String formatUTCWithMillis(long millis) {
        return DateFormatUtils.formatUTC(millis, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //$NON-NLS-1$
    }

}
