package org.eclipse.skalli.commons;

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class URLUtils {

    public static List<URL> asURLs(Enumeration<URL> u) {
        List<URL> ret = new LinkedList<URL>();
        if (u != null) {
            while (u.hasMoreElements()) {
                URL url = u.nextElement();
                ret.add(url);
            }
        }
        return ret;
    }
}
