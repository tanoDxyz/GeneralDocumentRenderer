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
package com.tanodxyz.itext722g.layout.layout;

import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.layout.margincollapse.MarginsCollapseInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the context for content {@link com.itextpdf.layout.renderer.IRenderer#layout(LayoutContext) layouting}.
 */
public class LayoutContext {

    /**
     * The {@link LayoutArea} for the content to be placed on.
     */
    protected LayoutArea area;

    /**
     * The info about margins collapsing.
     */
    protected MarginsCollapseInfo marginsCollapseInfo;

    /**
     * The list of {@link Rectangle} objects.
     */
    protected List<Rectangle> floatRendererAreas = new ArrayList<>();

    /**
     * Indicates whether the height is clipped or not.
     */
    protected boolean clippedHeight = false;

    /**
     * Creates the layout context.
     *
     * @param area for the content to be placed on
     */
    public LayoutContext(LayoutArea area) {
        this.area = area;
    }

    /**
     * Creates the layout context.
     *
     * @param area for the content to be placed on
     * @param marginsCollapseInfo the info about margins collapsing
     */
    public LayoutContext(LayoutArea area, MarginsCollapseInfo marginsCollapseInfo) {
        this.area = area;
        this.marginsCollapseInfo = marginsCollapseInfo;
    }

    /**
     * Creates the layout context.
     *
     * @param area for the content to be placed on
     * @param marginsCollapseInfo the info about margins collapsing
     * @param floatedRendererAreas list of {@link Rectangle} objects
     */
    public LayoutContext(LayoutArea area, MarginsCollapseInfo marginsCollapseInfo, List<Rectangle> floatedRendererAreas) {
        this(area, marginsCollapseInfo);
        if (floatedRendererAreas != null) {
            this.floatRendererAreas = floatedRendererAreas;
        }
    }

    /**
     * Creates the layout context.
     *
     * @param area for the content to be placed on
     * @param clippedHeight indicates whether the height is clipped or not
     */
    public LayoutContext(LayoutArea area, boolean clippedHeight) {
        this(area);
        this.clippedHeight = clippedHeight;
    }

    /**
     * Creates the layout context.
     *
     * @param area for the content to be placed on
     * @param marginsCollapseInfo the info about margins collapsing
     * @param floatedRendererAreas list of {@link Rectangle} objects
     * @param clippedHeight indicates whether the height is clipped or not
     */
    public LayoutContext(LayoutArea area, MarginsCollapseInfo marginsCollapseInfo, List<Rectangle> floatedRendererAreas, boolean clippedHeight) {
        this(area, marginsCollapseInfo);
        if (floatedRendererAreas != null) {
            this.floatRendererAreas = floatedRendererAreas;
        }
        this.clippedHeight = clippedHeight;
    }

    /**
     * Gets the {@link LayoutArea area} the content to be placed on.
     *
     * @return the area for content layouting.
     */
    public LayoutArea getArea() {
        return area;
    }

    /**
     * Gets info about margins collapsing.
     *
     * @return the info about margins collapsing
     */
    public MarginsCollapseInfo getMarginsCollapseInfo() {
        return marginsCollapseInfo;
    }

    /**
     * Gets list of {@link Rectangle} objects.
     *
     * @return list of {@link Rectangle} objects
     */
    public List<Rectangle> getFloatRendererAreas() {
        return floatRendererAreas;
    }

    /**
     * Indicates whether the layout area's height is clipped or not.
     *
     * @return whether the layout area's height is clipped or not.
     */
    public boolean isClippedHeight() {
        return clippedHeight;
    }

    /**
     * Defines whether the layout area's height is clipped or not.
     *
     * @param clippedHeight indicates whether the height is clipped or not.
     */
    public void setClippedHeight(boolean clippedHeight) {
        this.clippedHeight = clippedHeight;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return area.toString();
    }
}
