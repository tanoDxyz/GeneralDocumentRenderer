/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.tanodxyz.itext722g.svg.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class that facilitates parsing values from CSS.
 */
// TODO DEVSIX-2266

public final class SvgCssUtils {

    private SvgCssUtils() {}

    /**
     * Splits a given String into a list of substrings.
     * The string is split up by commas and whitespace characters (\t, \n, \r, \f).
     *
     * @param value the string to be split
     * @return a list containing the split strings, an empty list if the value is null or empty
     */
    public static List<String> splitValueList(String value) {
        List<String> result = new ArrayList<>();

        if (value != null && value.length() > 0) {
            value = value.trim();

            String[] list = value.split("\\s*(,|\\s)\\s*");
            result.addAll(Arrays.asList(list));
        }

        return result;
    }

    /**
     * Converts a float to a String.
     *
     * @param value to be converted float value
     * @return the value in a String representation
     */
    public static String convertFloatToString(float value) {
        return String.valueOf(value);
    }

    /**
     * Converts a double to a String.
     *
     * @param value to be converted double value
     * @return the value in a String representation
     */
    public static String convertDoubleToString(double value) {
        return String.valueOf(value);
    }
}
