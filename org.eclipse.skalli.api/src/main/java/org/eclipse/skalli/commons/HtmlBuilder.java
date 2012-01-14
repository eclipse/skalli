package org.eclipse.skalli.commons;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class HtmlBuilder {

    public static final String DEFAULT_TARGET = "_blank"; //$NON-NLS-1$
    public static final String STYLE_LEFT_MARGIN = "leftMargin"; //$NON-NLS-1$

    private StringBuilder sb;

    public HtmlBuilder() {
        this.sb = new StringBuilder();
    }

    public HtmlBuilder(int capacity) {
        this.sb = new StringBuilder(capacity);
    }

    public HtmlBuilder(String s) {
        this.sb = new StringBuilder(s);
    }

    public HtmlBuilder(StringBuilder sb) {
        this.sb = new StringBuilder(sb.toString());
    }

    public HtmlBuilder(CharSequence seq) {
        this.sb = new StringBuilder(seq);
    }

    public HtmlBuilder append(boolean b) {
        sb.append(b);
        return this;
    }

    public HtmlBuilder append(char c) {
        sb.append(c);
        return this;
    }

    public HtmlBuilder append(char[] str) {
        sb.append(str);
        return this;
    }

    public HtmlBuilder append(char[] str, int offset, int len) {
        sb.append(str, offset, len);
        return this;
    }

    public HtmlBuilder append(CharSequence s) {
        sb.append(s);
        return this;
    }

    public HtmlBuilder append(CharSequence s, int start, int end) {
        sb.append(s, start, end);
        return this;
    }

    public HtmlBuilder append(double d) {
        sb.append(d);
        return this;
    }

    public HtmlBuilder append(float f) {
        sb.append(f);
        return this;
    }

    public HtmlBuilder append(int i) {
        sb.append(i);
        return this;
    }

    public HtmlBuilder append(long lng) {
        sb.append(lng);
        return this;
    }

    public HtmlBuilder append(Object obj) {
        sb.append(obj);
        return this;
    }

    public HtmlBuilder append(String str) {
        sb.append(str);
        return this;
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendLineBreak() {
        sb.append("<br/>\n");
        return this;
    }

    public HtmlBuilder appendIcon(String icon) {
        return appendIcon(icon, null, null, null);
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendIcon(String icon, String alt, String styleClass, String style) {
        if (StringUtils.isNotBlank(icon)) {
            sb.append("<img src=\"").append(icon).append("\"");
            if(StringUtils.isNotBlank(alt)) {
                sb.append(" alt=\"").append(alt).append("\"");
            }
            appendStyle(styleClass, style);
            sb.append("/>");
        }
        return this;
    }

    public HtmlBuilder appendLink(String caption, String url) {
        appendLink(caption, url, DEFAULT_TARGET, null, null);
        return this;
    }

    public HtmlBuilder appendLink(String caption, URL url) {
        appendLink(caption, url, DEFAULT_TARGET, null, null);
        return this;
    }

    public HtmlBuilder appendLink(String caption, String url, String styleClass) {
        appendLink(caption, url, DEFAULT_TARGET, styleClass, null);
        return this;
    }

    public HtmlBuilder appendLink(String caption, URL url, String styleClass) {
        appendLink(caption, url, DEFAULT_TARGET, styleClass, null);
        return this;
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendLink(String caption, String url,
            String targetName, String styleClass, String style) {
        if (StringUtils.isNotBlank(url)) {
            sb.append("<a href=\"").append(url).append("\"");
            sb.append(" target=\"").append(StringUtils.isNotBlank(targetName)? targetName : DEFAULT_TARGET).append("\"");
            appendStyle(styleClass, style);
            sb.append(">");
        }
        if (StringUtils.isNotBlank(caption)) {
            sb.append(caption);
        } else if (StringUtils.isNotBlank(url)) {
            sb.append(url);
        } else {
            sb.append("<!-- -->");
        }
        if (StringUtils.isNotBlank(url)) {
            sb.append("</a>");
        }
        return this;
    }

    public HtmlBuilder appendLink(String caption, URL url,
            String targetName, String styleClass, String style) {
        appendLink(caption, url != null? url.toExternalForm() : null, targetName, styleClass, style);
        return this;
    }

    public HtmlBuilder appendIconizedLink(String icon, String caption, String url) {
        appendIcon(icon);
        appendLink(caption, url);
        return this;
    }

    public HtmlBuilder appendLinks(String firstCaption, String alternativeCaption, Collection<String> urls) {
        if (urls != null) {
            boolean isFirst = true;
            for (String url : urls) {
                if (StringUtils.isNotBlank(url)) {
                    String caption = null;
                    if (isFirst) {
                        caption = firstCaption;
                        if (StringUtils.isBlank(caption)) {
                            caption = alternativeCaption;
                        }
                    } else {
                        caption = alternativeCaption;
                        if (StringUtils.isBlank(caption)) {
                            caption = firstCaption;
                        }
                    }
                    String styleClass = isFirst? null : STYLE_LEFT_MARGIN;
                    appendLink(caption, url, styleClass);
                    isFirst = false;
                }
            }
        }
        return this;
    }

    public HtmlBuilder appendIconizedLinks(String icon, String firstCaption, String alternativeCaption, Collection<String> urls) {
        appendIcon(icon);
        appendLinks(firstCaption, alternativeCaption, urls);
        return this;
    }

    public HtmlBuilder appendLinks(Collection<Link> links) {
        if (links != null) {
            boolean isFirst = true;
            for (Link link : links) {
                String styleClass = isFirst? null : STYLE_LEFT_MARGIN;
                appendLink(link.getLabel(), link.getUrl(), styleClass);
                isFirst = false;
            }
        }
        return this;
    }

    public HtmlBuilder appendIconizedLinks(String icon, Collection<Link> links) {
        appendIcon(icon);
        appendLinks(links);
        return this;
    }

    public HtmlBuilder appendMailToLink(String link) {
        appendMailToLink(null, link, null);
        return this;
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendMailToLink(String icon, String link, String caption) {
        appendIcon(icon);
        if (StringUtils.isNotBlank(link)) {
            String urlEncoded = null;
            try {
                urlEncoded = URLEncoder.encode(link, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // should never happen, UTF-8 is available on all platforms
                throw new RuntimeException(e);
            }
            sb.append("<a href=\"mailto:").append(urlEncoded).append("\">");
            if (StringUtils.isBlank(caption)) {
                caption = link;
            }
            caption = StringEscapeUtils.escapeHtml(caption);
            sb.append(caption).append("</a>");
        }
        return this;
    }

    public HtmlBuilder appendHeader(String caption, int level) {
        return appendHeader(caption, level, null, null);
    }

    @SuppressWarnings("nls")
    public HtmlBuilder appendHeader(String caption, int level, String styleClass, String style) {
        level = Math.max(1, Math.min(6, level));
        sb.append("<h").append(level);
        appendStyle(styleClass, style);
        sb.append(">");
        if (StringUtils.isNotBlank(caption)) {
            sb.append(caption);
        } else {
            sb.append("<!-- -->");
        }
        sb.append("</h").append(level).append(">\n");
        return this;
    }


    @SuppressWarnings("nls")
    void appendStyle(String styleClass, String style) {
        if (StringUtils.isNotBlank(styleClass)) {
            sb.append(" class=\"").append(styleClass).append("\"");
        }
        if (StringUtils.isNotBlank(style)) {
            sb.append(" style=\"").append(style).append("\"");
        }
    }

    public int length() {
        return sb.length();
    }

    public void clear() {
        sb.setLength(0);
    }

    public StringBuilder asStringBuilder() {
        return sb;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
