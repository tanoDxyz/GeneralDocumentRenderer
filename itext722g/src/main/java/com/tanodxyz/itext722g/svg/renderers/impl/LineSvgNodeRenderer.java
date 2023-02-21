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
package com.tanodxyz.itext722g.svg.renderers.impl;


import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.geom.Vector;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvas;
import com.tanodxyz.itext722g.styledXmlParser.css.util.CssDimensionParsingUtils;
import com.tanodxyz.itext722g.svg.MarkerVertexType;
import com.tanodxyz.itext722g.svg.SvgConstants;
import com.tanodxyz.itext722g.svg.renderers.IMarkerCapable;
import com.tanodxyz.itext722g.svg.renderers.ISvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.SvgDrawContext;
import com.tanodxyz.itext722g.svg.utils.SvgCoordinateUtils;

import java.util.Map;

/**
 * {@link ISvgNodeRenderer} implementation for the &lt;line&gt; tag.
 */
public class LineSvgNodeRenderer extends AbstractSvgNodeRenderer implements IMarkerCapable {

    private float x1 = 0f;
    private float y1 = 0f;
    private float x2 = 0f;
    private float y2 = 0f;

    @Override
    public void doDraw(SvgDrawContext context) {
        PdfCanvas canvas = context.getCurrentCanvas();
        canvas.writeLiteral("% line\n");

        if (setParameterss()) {
            canvas.moveTo(x1, y1).lineTo(x2, y2);
        }
    }

    @Override
    public Rectangle getObjectBoundingBox(SvgDrawContext context) {
        if (setParameterss()) {
            float x = Math.min(x1, x2);
            float y = Math.min(y1, y2);

            float width = Math.abs(x1 - x2);
            float height = Math.abs(y1 - y2);

            return new Rectangle(x, y, width, height);
        } else {
            return null;
        }
    }

    @Override
    protected boolean canElementFill() {
        return false;
    }

    float getAttribute(Map<String, String> attributes, String key) {
        String value = attributes.get(key);
        if (value != null && !value.isEmpty()) {
            return CssDimensionParsingUtils.parseAbsoluteLength(attributes.get(key));
        }
        return 0;
    }

    @Override
    public ISvgNodeRenderer createDeepCopy() {
        LineSvgNodeRenderer copy = new LineSvgNodeRenderer();
        deepCopyAttributesAndStyles(copy);
        return copy;
    }

    @Override
    public void drawMarker(SvgDrawContext context, final MarkerVertexType markerVertexType) {
        String moveX = null;
        String moveY = null;
        if (MarkerVertexType.MARKER_START.equals(markerVertexType)) {
            moveX = this.attributesAndStyles.get(SvgConstants.Attributes.X1);
            moveY = this.attributesAndStyles.get(SvgConstants.Attributes.Y1);
        } else if (MarkerVertexType.MARKER_END.equals(markerVertexType)) {
            moveX = this.attributesAndStyles.get(SvgConstants.Attributes.X2);
            moveY = this.attributesAndStyles.get(SvgConstants.Attributes.Y2);
        }
        if (moveX != null && moveY != null) {
             MarkerSvgNodeRenderer.drawMarker(context, moveX, moveY, markerVertexType, this);
        }
    }

    @Override
    public double getAutoOrientAngle( MarkerSvgNodeRenderer marker, boolean reverse) {
        Vector v = new Vector(getAttribute(this.attributesAndStyles, SvgConstants.Attributes.X2) - getAttribute(
                this.attributesAndStyles, SvgConstants.Attributes.X1),
                getAttribute(this.attributesAndStyles, SvgConstants.Attributes.Y2) - getAttribute(
                        this.attributesAndStyles, SvgConstants.Attributes.Y1), 0f);
        Vector xAxis = new Vector(1, 0, 0);
        double rotAngle = SvgCoordinateUtils.calculateAngleBetweenTwoVectors(xAxis, v);
        return v.get(1) >= 0 && !reverse ? rotAngle : rotAngle * -1f;
    }

    private boolean setParameterss() {
        if (attributesAndStyles.size() > 0) {
            if (attributesAndStyles.containsKey(SvgConstants.Attributes.X1)) {
                this.x1 = getAttribute(attributesAndStyles, SvgConstants.Attributes.X1);
            }

            if (attributesAndStyles.containsKey(SvgConstants.Attributes.Y1)) {
                this.y1 = getAttribute(attributesAndStyles, SvgConstants.Attributes.Y1);
            }

            if (attributesAndStyles.containsKey(SvgConstants.Attributes.X2)) {
                this.x2 = getAttribute(attributesAndStyles, SvgConstants.Attributes.X2);
            }

            if (attributesAndStyles.containsKey(SvgConstants.Attributes.Y2)) {
                this.y2 = getAttribute(attributesAndStyles, SvgConstants.Attributes.Y2);
            }
            return true;
        }
        return false;
    }
}
