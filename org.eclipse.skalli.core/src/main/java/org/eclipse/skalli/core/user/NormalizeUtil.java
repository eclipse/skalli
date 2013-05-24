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
package org.eclipse.skalli.core.user;

import java.text.Normalizer;

public class NormalizeUtil {

    @SuppressWarnings("nls")
    public static String normalize(String s) {
        if (s == null) {
            return null;
        }
        s = s.replaceAll("ä", "ae");
        s = s.replaceAll("ö", "oe");
        s = s.replaceAll("ü", "ue");
        s = s.replaceAll("Ä", "Ae");
        s = s.replaceAll("Ö", "Oe");
        s = s.replaceAll("Ü", "Ue");
        s = s.replaceAll("ß", "ss");
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

}
