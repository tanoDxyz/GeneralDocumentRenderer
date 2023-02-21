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
package com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand;


import com.tanodxyz.itext722g.styledXmlParser.css.CommonCssConstants;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BackgroundPositionShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BackgroundShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BorderBottomShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BorderColorShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BorderLeftShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BorderRadiusShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BorderRightShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BorderShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BorderStyleShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BorderTopShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.BorderWidthShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.FlexFlowShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.FlexShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.FontShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.GapShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.ListStyleShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.MarginShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.OutlineShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.PaddingShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.PlaceItemsShorthandResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.shorthand.impl.TextDecorationShorthandResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory for creating ShorthandResolver objects.
 */
public class ShorthandResolverFactory {
    
    /** The map of shorthand resolvers. */
    private static final Map<String, IShorthandResolver> shorthandResolvers;
    static {
        shorthandResolvers = new HashMap<>();
        shorthandResolvers.put(CommonCssConstants.BACKGROUND, new BackgroundShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BACKGROUND_POSITION, new BackgroundPositionShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BORDER, new BorderShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BORDER_BOTTOM, new BorderBottomShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BORDER_COLOR, new BorderColorShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BORDER_LEFT, new BorderLeftShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BORDER_RADIUS, new BorderRadiusShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BORDER_RIGHT, new BorderRightShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BORDER_STYLE, new BorderStyleShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BORDER_TOP, new BorderTopShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.BORDER_WIDTH, new BorderWidthShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.FONT, new FontShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.LIST_STYLE, new ListStyleShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.MARGIN, new MarginShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.OUTLINE, new OutlineShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.PADDING, new PaddingShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.TEXT_DECORATION, new TextDecorationShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.FLEX, new FlexShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.FLEX_FLOW, new FlexFlowShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.GAP, new GapShorthandResolver());
        shorthandResolvers.put(CommonCssConstants.PLACE_ITEMS, new PlaceItemsShorthandResolver());
    }

    /**
     * Gets a shorthand resolver.
     *
     * @param shorthandProperty the property
     * @return the shorthand resolver
     */
    public static IShorthandResolver getShorthandResolver(String shorthandProperty) {
        return shorthandResolvers.get(shorthandProperty);
    }
}
