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
package com.tanodxyz.itext722g.layout.borders;

import com.tanodxyz.itext722g.kernel.colors.Color;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvas;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvasConstants;

/**
 * Draws a border with rounded dots around the element it's been set to. For square dots see {@link DottedBorder}.
 */
public class RoundDotsBorder extends Border {

    /**
     * The modifier to be applied on the width to have the initial gap size
     */
    private static final float GAP_MODIFIER = 2.5f;

    /**
     * Creates a RoundDotsBorder with the specified wit?dth and sets the color to black.
     *
     * @param width width of the border
     */
    public RoundDotsBorder(float width) {
        super(width);
    }

    /**
     * Creates a RoundDotsBorder with the specified wit?dth and the specified color.
     *
     * @param color color of the border
     * @param width width of the border
     */
    public RoundDotsBorder(Color color, float width) {
        super(color, width);
    }

    /**
     * Creates a RoundDotsBorder with the specified width, color and opacity.
     *
     * @param color   color of the border
     * @param width   width of the border
     * @param opacity width of the border
     */
    public RoundDotsBorder(Color color, float width, float opacity) {
        super(color, width, opacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() {
        return ROUND_DOTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(PdfCanvas canvas, float x1, float y1, float x2, float y2, Side defaultSide, float borderWidthBefore, float borderWidthAfter) {
        float initialGap = width * GAP_MODIFIER;
        float dx = x2 - x1;
        float dy = y2 - y1;
        double borderLength = Math.sqrt(dx * dx + dy * dy);
        float adjustedGap = super.getDotsGap(borderLength, initialGap);

        float[] startingPoints = getStartingPointsForBorderSide(x1, y1, x2, y2, defaultSide);
        x1 = startingPoints[0];
        y1 = startingPoints[1];
        x2 = startingPoints[2];
        y2 = startingPoints[3];

        canvas.saveState()
                .setStrokeColor(transparentColor.getColor())
                .setLineWidth(width)
                .setLineCapStyle(PdfCanvasConstants.LineCapStyle.ROUND);
        transparentColor.applyStrokeTransparency(canvas);
        canvas.setLineDash(0, adjustedGap, adjustedGap/2)
                .moveTo(x1, y1).lineTo(x2, y2)
                .stroke()
                .restoreState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawCellBorder(PdfCanvas canvas, float x1, float y1, float x2, float y2, Side defaultSide) {
        float initialGap = width * GAP_MODIFIER;
        float dx = x2 - x1;
        float dy = y2 - y1;
        double borderLength = Math.sqrt(dx * dx + dy * dy);
        float adjustedGap = super.getDotsGap(borderLength, initialGap);
        boolean isHorizontal = false;
        if (Math.abs(y2 - y1) < 0.0005f) {
            isHorizontal = true;
        }

        if (isHorizontal) {
            x2 -= width;
        }
        canvas.saveState();
        canvas.setStrokeColor(transparentColor.getColor());
        transparentColor.applyStrokeTransparency(canvas);
        canvas.setLineWidth(width);
        canvas.setLineCapStyle(PdfCanvasConstants.LineCapStyle.ROUND);

        canvas.setLineDash(0, adjustedGap, adjustedGap / 2)
                .moveTo(x1, y1).lineTo(x2, y2)
                .stroke();
        canvas.restoreState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(PdfCanvas canvas, float x1, float y1, float x2, float y2, float horizontalRadius1, float verticalRadius1, float horizontalRadius2, float verticalRadius2, Side defaultSide, float borderWidthBefore, float borderWidthAfter) {
        float initialGap = width * GAP_MODIFIER;
        float dx = x2 - x1;
        float dy = y2 - y1;
        double borderLength = Math.sqrt(dx * dx + dy * dy);
        float adjustedGap = super.getDotsGap(borderLength, initialGap);

        canvas
                .saveState()
                .setStrokeColor(transparentColor.getColor());
        transparentColor.applyStrokeTransparency(canvas);
        canvas
                .setLineWidth(width)
                .setLineCapStyle(PdfCanvasConstants.LineCapStyle.ROUND)
                .setLineDash(0, adjustedGap, adjustedGap / 2);

        Rectangle boundingRectangle = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        float[] horizontalRadii = new float[]{horizontalRadius1, horizontalRadius2};
        float[] verticalRadii = new float[]{verticalRadius1, verticalRadius2};

        drawDiscontinuousBorders(canvas, boundingRectangle, horizontalRadii, verticalRadii, defaultSide, borderWidthBefore, borderWidthAfter);
    }
}
