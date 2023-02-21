/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: Bruno Lowagie, Paulo Soares, et al.

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

package com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl;

import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.styledXmlParser.css.CommonCssConstants;
import com.tanodxyz.itext722g.styledXmlParser.css.CssDeclaration;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.IShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.util.CssTypesValidationUtils;
import com.tanodxyz.itext722g.styledXmlParser.css.validate.CssDeclarationValidationMaster;
import com.tanodxyz.itext722g.styledXmlParser.logs.StyledXmlParserLogMessageConstant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class GapShorthandResolver implements IShorthandResolver {

    private static final Logger LOGGER = Logger.getLogger(GapShorthandResolver.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssDeclaration> resolveShorthand(String shorthandExpression) {
        shorthandExpression = shorthandExpression.trim();
        if (CssTypesValidationUtils.isInitialOrInheritOrUnset(shorthandExpression)) {
            return Arrays.asList(
                    new CssDeclaration(CommonCssConstants.ROW_GAP, shorthandExpression),
                    new CssDeclaration(CommonCssConstants.COLUMN_GAP, shorthandExpression)
            );
        }
        if (CssTypesValidationUtils.containsInitialOrInheritOrUnset(shorthandExpression)) {
            return handleExpressionError(StyledXmlParserLogMessageConstant.INVALID_CSS_PROPERTY_DECLARATION, CommonCssConstants.GAP,
                    shorthandExpression);
        }
        if (shorthandExpression.isEmpty()) {
            return handleExpressionError(StyledXmlParserLogMessageConstant.SHORTHAND_PROPERTY_CANNOT_BE_EMPTY, CommonCssConstants.GAP,
                    shorthandExpression);
        }

        final String[] gapProps = shorthandExpression.split(" ");

        if (gapProps.length == 1) {
            return resolveGapWithTwoProperties(gapProps[0], gapProps[0]);
        } else if (gapProps.length == 2) {
            return resolveGapWithTwoProperties(gapProps[0], gapProps[1]);
        } else {
            return handleExpressionError(StyledXmlParserLogMessageConstant.INVALID_CSS_PROPERTY_DECLARATION, CommonCssConstants.GAP,
                    shorthandExpression);

        }
    }

    private List<CssDeclaration> resolveGapWithTwoProperties(String row, String column) {
        CssDeclaration rowGapDeclaration = new CssDeclaration(CommonCssConstants.ROW_GAP, row);
        if (!CssDeclarationValidationMaster.checkDeclaration(rowGapDeclaration)) {
            return handleExpressionError(StyledXmlParserLogMessageConstant.INVALID_CSS_PROPERTY_DECLARATION,
                    CommonCssConstants.ROW_GAP, row);
        }
        CssDeclaration columnGapDeclaration = new CssDeclaration(CommonCssConstants.COLUMN_GAP, column);
        if (!CssDeclarationValidationMaster.checkDeclaration(columnGapDeclaration)) {
            return handleExpressionError(StyledXmlParserLogMessageConstant.INVALID_CSS_PROPERTY_DECLARATION,
                    CommonCssConstants.COLUMN_GAP, column);
        }
        return Arrays.asList(rowGapDeclaration, columnGapDeclaration);
    }

    private static List<CssDeclaration> handleExpressionError(String logMessage, String attribute,
            String shorthandExpression) {
        LOGGER.warning(MessageFormatUtil.format(logMessage, attribute, shorthandExpression));
        return Collections.<CssDeclaration>emptyList();
    }
}
