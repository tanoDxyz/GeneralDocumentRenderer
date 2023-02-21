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


import com.tanodxyz.itext722g.io.font.FontProgram;
import com.tanodxyz.itext722g.kernel.font.PdfFont;
import com.tanodxyz.itext722g.kernel.geom.Point;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvas;
import com.tanodxyz.itext722g.layout.properties.RenderingMode;
import com.tanodxyz.itext722g.layout.renderer.TextRenderer;
import com.tanodxyz.itext722g.svg.SvgConstants;
import com.tanodxyz.itext722g.svg.renderers.ISvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.SvgDrawContext;
import com.tanodxyz.itext722g.svg.utils.SvgTextUtil;
import com.tanodxyz.itext722g.svg.utils.TextRectangle;

/**
 * {@link ISvgNodeRenderer} implementation for drawing text to a canvas.
 */
public class TextLeafSvgNodeRenderer extends AbstractSvgNodeRenderer implements ISvgTextNodeRenderer {

    @Override
    public ISvgNodeRenderer createDeepCopy() {
        TextLeafSvgNodeRenderer copy = new TextLeafSvgNodeRenderer();
        deepCopyAttributesAndStyles(copy);
        return copy;
    }


    @Override
    public float getTextContentLength(float parentFontSize, PdfFont font) {
        float contentLength = 0.0f;
        if (font != null && this.attributesAndStyles != null && this.attributesAndStyles.containsKey(SvgConstants.Attributes.TEXT_CONTENT)) {
            // Use own font-size declaration if it is present, parent's otherwise
            final float fontSize = SvgTextUtil.resolveFontSize(this, parentFontSize);
            final String content = this.attributesAndStyles.get(SvgConstants.Attributes.TEXT_CONTENT);
            contentLength = font.getWidth(content, fontSize);
        }
        return contentLength;
    }

    @Override
    public float[] getRelativeTranslation() {
        return new float[]{0.0f, 0.0f};
    }

    @Override
    public boolean containsRelativeMove() {
        return false; //Leaf text elements do not contain any kind of transformation
    }

    @Override
    public boolean containsAbsolutePositionChange() {
        return false; //Leaf text elements do not contain any kind of transformation
    }

    @Override
    public float[][] getAbsolutePositionChanges() {
        float[] part = new float[]{0f};
        return new float[][]{part, part};
    }

    @Override
    public TextRectangle getTextRectangle(SvgDrawContext context, Point basePoint) {
        if (getParent() instanceof  TextSvgBranchRenderer && basePoint != null) {
            final float parentFontSize = ((AbstractSvgNodeRenderer) getParent()).getCurrentFontSize();
            final PdfFont parentFont = (( TextSvgBranchRenderer) getParent()).getFont();
            final float textLength = getTextContentLength(parentFontSize, parentFont);
            final float[] fontAscenderDescenderFromMetrics = TextRenderer
                    .calculateAscenderDescender(parentFont, RenderingMode.HTML_MODE);
            final float fontAscender = fontAscenderDescenderFromMetrics[0] / FontProgram.UNITS_NORMALIZATION * parentFontSize;
            final float fontDescender = fontAscenderDescenderFromMetrics[1] / FontProgram.UNITS_NORMALIZATION * parentFontSize;
            // TextRenderer#calculateAscenderDescender returns fontDescender as a negative value so we should subtract this value
            final float textHeight = fontAscender - fontDescender;
            return new TextRectangle((float) basePoint.getX(), (float) basePoint.getY() - fontAscender, textLength,
                    textHeight, (float) basePoint.getY());
        } else {
            return null;
        }
    }

    @Override
    public Rectangle getObjectBoundingBox(SvgDrawContext context) {
        return null;
    }

    @Override
    protected void doDraw(SvgDrawContext context) {
        if (this.attributesAndStyles != null && this.attributesAndStyles.containsKey(SvgConstants.Attributes.TEXT_CONTENT)) {
            PdfCanvas currentCanvas = context.getCurrentCanvas();
            //TODO(DEVSIX-2507): Support for glyph by glyph handling of x, y and rotate
            if (context.getPreviousElementTextMove() == null) {
                currentCanvas.moveText(context.getTextMove()[0], context.getTextMove()[1]);
            } else {
                currentCanvas.moveText(context.getPreviousElementTextMove()[0],
                        context.getPreviousElementTextMove()[1]);
            }
            currentCanvas.showText(this.attributesAndStyles.get(SvgConstants.Attributes.TEXT_CONTENT));
        }
    }

    @Override
    protected boolean canElementFill() {
        return false;
    }

}
