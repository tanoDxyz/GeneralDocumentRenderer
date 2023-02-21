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
package com.tanodxyz.itext722g.layout.renderer.objectfit;


import com.tanodxyz.itext722g.layout.properties.ObjectFit;

/**
 * The class represents results of calculating of rendered image size
 * after applying of the {@link ObjectFit} property.
 */
public class ObjectFitApplyingResult {
    private double renderedImageWidth;
    private double renderedImageHeight;
    private boolean imageCuttingRequired;

    /**
     * Creates a new instance of the class with default values.
     */
    public ObjectFitApplyingResult() {
    }

    /**
     * Creates a new instance of the class.
     *
     * @param renderedImageWidth   is a width of the image to render
     * @param renderedImageHeight  is a height of the image to render
     * @param imageCuttingRequired is a flag showing if rendered image should be clipped
     *                             as its size is greater than size of the image container
     */
    public ObjectFitApplyingResult(double renderedImageWidth, double renderedImageHeight,
            boolean imageCuttingRequired) {
        this.renderedImageWidth = renderedImageWidth;
        this.renderedImageHeight = renderedImageHeight;
        this.imageCuttingRequired = imageCuttingRequired;
    }


    /**
     * Getter for width of rendered image.
     *
     * @return width of rendered image
     */
    public double getRenderedImageWidth() {
        return renderedImageWidth;
    }

    /**
     * Setter for width of rendered image.
     *
     * @param renderedImageWidth is a new width of rendered image
     */
    public void setRenderedImageWidth(double renderedImageWidth) {
        this.renderedImageWidth = renderedImageWidth;
    }

    /**
     * Getter for height of rendered image.
     *
     * @return height of rendered image
     */
    public double getRenderedImageHeight() {
        return renderedImageHeight;
    }

    /**
     * Setter for height of rendered image.
     *
     * @param renderedImageHeight is a new height of rendered image
     */
    public void setRenderedImageHeight(double renderedImageHeight) {
        this.renderedImageHeight = renderedImageHeight;
    }

    /**
     * Getter for a boolean value showing if at least one dimension of rendered image
     * is greater than expected image size. If true then image will be shown partially
     *
     * @return true if the image need to be cutting during rendering and false otherwise
     */
    public boolean isImageCuttingRequired() {
        return imageCuttingRequired;
    }

    /**
     * Setter for a boolean value showing if at least one dimension of rendered image
     * is greater than expected image size. If true then image will be shown partially
     *
     * @param imageCuttingRequired is a new value of the cutting-required flag
     */
    public void setImageCuttingRequired(boolean imageCuttingRequired) {
        this.imageCuttingRequired = imageCuttingRequired;
    }
}
