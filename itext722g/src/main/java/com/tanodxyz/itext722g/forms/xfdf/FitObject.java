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
package com.tanodxyz.itext722g.forms.xfdf;

import com.tanodxyz.itext722g.forms.exceptions.XfdfException;
import com.tanodxyz.itext722g.kernel.pdf.PdfObject;

/**
 * Represent Fit, a child of the Dest element.
 * Content model: none.
 * Attributes: depends of type of Fit (FitH, FitB, FitV etc.).
 * For more details see paragraphs 6.5.13-6.5.19, 6.6.23 in Xfdf specification.
 */
public class FitObject {

    /**
     * Represents the page displayed by current Fit element.
     * Attribute of Fit, FitB, FitBH, FitBV, FitH, FitR, FitV, XYZ elements.
     */
    private PdfObject page;

    /**
     * Vertical coordinate positioned at the top edge of the window.
     */
    private float top;

    /**
     * Vertical coordinate positioned at the bottom edge of the window.
     */
    private float bottom;

    /**
     * Horizontal coordinate positioned at the left edge of the window.
     */
    private float left;

    /**
     * Horizontal coordinate positioned at the right edge of the window.
     */
    private float right;

    /**
     * Corresponds to the zoom object in the destination syntax.
     * Attribute of XYZ object.
     */
    private float zoom;

    public FitObject(PdfObject page) {
        if(page == null) {
            throw new XfdfException(XfdfException.PAGE_IS_MISSING);
        }
        this.page = page;
    }

    /**
     * Gets the PdfObject representing the page displayed by current Fit element.
     * Attribute of Fit, FitB, FitBH, FitBV, FitH, FitR, FitV, XYZ elements.
     *
     * @return {@link PdfObject page} of the current Fit element
     */
    public PdfObject getPage() {
        return page;
    }

    /**
     * Gets a float vertical coordinate positioned at the top edge of the window.
     *
     * @return top vertical coordinate
     */
    public float getTop() {
        return top;
    }

    /**
     * Sets a float vertical coordinate positioned at the top edge of the window.
     * @param top vertical coordinate value
     * @return current {@link FitObject fit object}
     */
    public FitObject setTop(float top) {
        this.top = top;
        return this;
    }

    /**
     * Gets a float horizontal coordinate positioned at the left edge of the window.
     *
     * @return left horizontal coordinate
     */
    public float getLeft() {
        return left;
    }

    /**
     * Sets a float horizontal coordinate positioned at the left edge of the window.
     * @param left horizontal coordinate value
     * @return current {@link FitObject fit object}
     */
    public FitObject setLeft(float left) {
        this.left = left;
        return this;
    }

    /**
     * Gets a float vertical coordinate positioned at the bottom edge of the window.
     *
     * @return bottom vertical coordinate
     */
    public float getBottom() {
        return bottom;
    }

    /**
     * Sets a float vertical coordinate positioned at the bottom edge of the window.
     *
     * @param bottom vertical coordinate value
     * @return current {@link FitObject fit object}
     */
    public FitObject setBottom(float bottom) {
        this.bottom = bottom;
        return this;
    }

    /**
     * Gets a float horizontal coordinate positioned at the right edge of the window.
     *
     * @return right horizontal coordinate
     */
    public float getRight() {
        return right;
    }

    /**
     * Sets a float horizontal coordinate positioned at the right edge of the window.
     *
     * @param right horizontal coordinate
     * @return current {@link FitObject fit object}
     */
    public FitObject setRight(float right) {
        this.right = right;
        return this;
    }

    /**
     * Gets a float representing the zoom ratio.
     * Attribute of XYZ object.
     *
     * @return zoom ratio value
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Sets a float representing the zoom ratio.
     * Attribute of XYZ object.
     *
     * @param zoom ratio value
     * @return current {@link FitObject fit object}
     */
    public FitObject setZoom(float zoom) {
        this.zoom = zoom;
        return this;
    }
}
