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

import com.tanodxyz.itext722g.kernel.geom.AffineTransform;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.pdf.PdfArray;
import com.tanodxyz.itext722g.kernel.pdf.PdfName;
import com.tanodxyz.itext722g.kernel.pdf.PdfStream;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvas;
import com.tanodxyz.itext722g.svg.exceptions.SvgExceptionMessageConstant;
import com.tanodxyz.itext722g.svg.exceptions.SvgProcessingException;
import com.tanodxyz.itext722g.svg.renderers.ISvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.SvgDrawContext;

import java.util.Map;

/**
 * Root renderer responsible for applying the initial axis-flipping transform
 */
public class PdfRootSvgNodeRenderer implements ISvgNodeRenderer {

    ISvgNodeRenderer subTreeRoot;

    /**
     * Creates a {@link PdfRootSvgNodeRenderer} instance.
     * @param subTreeRoot root of the subtree
     */
    public PdfRootSvgNodeRenderer(ISvgNodeRenderer subTreeRoot){
        this.subTreeRoot = subTreeRoot;
        subTreeRoot.setParent(this);
    }

    @Override
    public void setParent(ISvgNodeRenderer parent) {
        // TODO DEVSIX-2283
    }

    @Override
    public ISvgNodeRenderer getParent() {
        // TODO DEVSIX-2283
        return null;
    }

    @Override
    public void draw(SvgDrawContext context) {
        //Set viewport and transformation for pdf-context
        context.addViewPort(this.calculateViewPort(context));
        PdfCanvas currentCanvas = context.getCurrentCanvas();
        currentCanvas.concatMatrix(this.calculateTransformation(context));
        currentCanvas.writeLiteral("% svg root\n");
        subTreeRoot.draw(context);
    }

    @Override
    public void setAttributesAndStyles(Map<String, String> attributesAndStyles) {

    }

    @Override
    public String getAttribute(String key) {
        return null;
    }

    @Override
    public void setAttribute(String key, String value) {

    }

    @Override
    public Map<String, String> getAttributeMapCopy() {
        return null;
    }

    @Override
    public Rectangle getObjectBoundingBox(SvgDrawContext context) {
        return null;
    }

    AffineTransform calculateTransformation(SvgDrawContext context){
        Rectangle viewPort = context.getCurrentViewPort();
        float horizontal = viewPort.getX();
        float vertical = viewPort.getY() + viewPort.getHeight();
        // flip coordinate space vertically and translate on the y axis with the viewport height
        AffineTransform transform = AffineTransform.getTranslateInstance(0,0); //Identity-transform
        transform.concatenate(AffineTransform.getTranslateInstance(horizontal,vertical));
        transform.concatenate(new AffineTransform(1,0,0,-1,0,0));

        return transform;
    }

    Rectangle calculateViewPort(SvgDrawContext context){
        float portX = 0f;
        float portY = 0f;
        float portWidth = 0f;
        float portHeight = 0f;

        PdfStream contentStream = context.getCurrentCanvas().getContentStream();

        if ( ! contentStream.containsKey(PdfName.BBox) ) {
            throw new SvgProcessingException(SvgExceptionMessageConstant.ROOT_SVG_NO_BBOX);
        }

        PdfArray bboxArray = contentStream.getAsArray(PdfName.BBox);

        portX = bboxArray.getAsNumber(0).floatValue();
        portY = bboxArray.getAsNumber(1).floatValue();
        portWidth = bboxArray.getAsNumber(2).floatValue() - portX;
        portHeight = bboxArray.getAsNumber(3).floatValue() - portY;

        return new Rectangle(portX, portY, portWidth, portHeight);
    }

    @Override
    public ISvgNodeRenderer createDeepCopy() {
        PdfRootSvgNodeRenderer copy = new PdfRootSvgNodeRenderer(subTreeRoot.createDeepCopy());
        return copy;
    }

}
