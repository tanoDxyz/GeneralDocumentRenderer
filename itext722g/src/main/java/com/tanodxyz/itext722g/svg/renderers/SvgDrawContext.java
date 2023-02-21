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
package com.tanodxyz.itext722g.svg.renderers;


import com.tanodxyz.itext722g.kernel.geom.AffineTransform;
import com.tanodxyz.itext722g.kernel.geom.Matrix;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvas;
import com.tanodxyz.itext722g.layout.font.FontProvider;
import com.tanodxyz.itext722g.layout.font.FontSet;
import com.tanodxyz.itext722g.styledXmlParser.resolver.font.BasicFontProvider;
import com.tanodxyz.itext722g.styledXmlParser.resolver.resource.ResourceResolver;
import com.tanodxyz.itext722g.svg.css.SvgCssContext;
import com.tanodxyz.itext722g.svg.exceptions.SvgExceptionMessageConstant;
import com.tanodxyz.itext722g.svg.exceptions.SvgProcessingException;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

/**
 * The SvgDrawContext keeps a stack of {@link PdfCanvas} instances, which
 * represent all levels of XObjects that are added to the root canvas.
 */
public class SvgDrawContext {

    private final Map<String, ISvgNodeRenderer> namedObjects = new HashMap<>();
    private final Deque<PdfCanvas> canvases = new LinkedList<>();
    private final Deque<Rectangle> viewports = new LinkedList<>();
    private final Stack<String> useIds = new Stack<>();
    private final Stack<String> patternIds = new Stack<>();
    private final ResourceResolver resourceResolver;
    private final FontProvider fontProvider;
    private FontSet tempFonts;
    private SvgCssContext cssContext;

    private AffineTransform lastTextTransform = new AffineTransform();
    private float[] textMove = new float[]{0.0f, 0.0f};
    private float[] previousElementTextMove;

    /**
     * Create an instance of the context that is used to store information when converting SVG.
     *
     * @param resourceResolver instance of {@link ResourceResolver}
     * @param fontProvider instance of {@link FontProvider}
     */
    public SvgDrawContext(ResourceResolver resourceResolver, FontProvider fontProvider) {
        if (resourceResolver == null) {
            resourceResolver = new ResourceResolver(null);
        }
        this.resourceResolver = resourceResolver;
        if (fontProvider == null) {
            fontProvider = new BasicFontProvider();
        }
        this.fontProvider = fontProvider;
        cssContext = new SvgCssContext();
    }

    /**
     * Retrieves the current top of the stack, without modifying the stack.
     *
     * @return the current canvas that can be used for drawing operations.
     */
    public PdfCanvas getCurrentCanvas() {
        return canvases.getFirst();
    }

    /**
     * Retrieves the current top of the stack, thereby taking the current item
     * off the stack.
     *
     * @return the current canvas that can be used for drawing operations.
     */
    public PdfCanvas popCanvas() {
        PdfCanvas canvas = canvases.getFirst();
        canvases.removeFirst();
        return canvas;
    }


    /**
     * Adds a {@link PdfCanvas} to the stack (by definition its top), for use in
     * drawing operations.
     *
     * @param canvas the new top of the stack
     */
    public void pushCanvas(PdfCanvas canvas) {
        canvases.addFirst(canvas);
    }

    /**
     * Get the current size of the stack, signifying the nesting level of the
     * XObjects.
     *
     * @return the current size of the stack.
     */
    public int size() {
        return canvases.size();
    }

    /**
     * Adds a viewbox to the context.
     *
     * @param viewPort rectangle representing the current viewbox
     */
    public void addViewPort(Rectangle viewPort) {
        viewports.addFirst(viewPort);
    }

    /**
     * Get the current viewbox.
     *
     * @return the viewbox as it is currently set
     */
    public Rectangle getCurrentViewPort() {
        return viewports.getFirst();
    }

    /**
     * Get the viewbox which is the root viewport for the current document.
     *
     * @return root viewbox.
     */
    public Rectangle getRootViewPort() {
        return viewports.getLast();
    }

    /**
     * Remove the currently set view box.
     */
    public void removeCurrentViewPort() {
        if (this.viewports.size() > 0) {
            viewports.removeFirst();
        }
    }

    /**
     * Adds a named object to the draw context. These objects can then be referenced from a different tag.
     *
     * @param name        name of the object
     * @param namedObject object to be referenced
     */
    public void addNamedObject(String name, ISvgNodeRenderer namedObject) {
        if (namedObject == null) {
            throw new SvgProcessingException(SvgExceptionMessageConstant.NAMED_OBJECT_NULL);
        }

        if (name == null || name.isEmpty()) {
            throw new SvgProcessingException(SvgExceptionMessageConstant.NAMED_OBJECT_NAME_NULL_OR_EMPTY);
        }

        if (!this.namedObjects.containsKey(name)) {
            this.namedObjects.put(name, namedObject);
        }
    }

    /**
     * Get a named object based on its name. If the name isn't listed, this method will return null.
     *
     * @param name name of the object you want to reference
     * @return the referenced object
     */
    public ISvgNodeRenderer getNamedObject(String name) {
        return this.namedObjects.get(name);
    }

    /**
     * Gets the ResourceResolver to be used during the drawing operations.
     *
     * @return resource resolver instance
     */
    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    /**
     * * Adds a number of named object to the draw context. These objects can then be referenced from a different tag.
     *
     * @param namedObjects Map containing the named objects keyed to their ID strings
     */
    public void addNamedObjects(Map<String, ISvgNodeRenderer> namedObjects) {
        this.namedObjects.putAll(namedObjects);
    }

    /**
     * Gets the FontProvider to be used during the drawing operations.
     *
     * @return font provider instance
     */
    public FontProvider getFontProvider() {
        return fontProvider;
    }

    /**
     * Gets list of temporary fonts from @font-face.
     *
     * @return font set instance
     */
    public FontSet getTempFonts() {
        return tempFonts;
    }

    /**
     * Sets the FontSet.
     *
     * @param tempFonts font set to be used during drawing operations
     */
    public void setTempFonts(FontSet tempFonts) {
        this.tempFonts = tempFonts;
    }

    /**
     * Returns true when this id has been used before
     *
     * @param elementId element id to check
     * @return true if id has been encountered before through a use element
     */
    public boolean isIdUsedByUseTagBefore(String elementId) {
        return this.useIds.contains(elementId);
    }

    /**
     * Adds an ID that has been referenced by a use element.
     *
     * @param elementId referenced element ID
     */
    public void addUsedId(String elementId) {
        this.useIds.push(elementId);
    }

    /**
     * Removes an ID that has been referenced by a use element.
     *
     * @param elementId referenced element ID
     */
    public void removeUsedId(String elementId) {
        this.useIds.pop();
    }

    /**
     * Get the text transformation that was last applied
     * @return {@link AffineTransform} representing the last text transformation
     */
    public AffineTransform getLastTextTransform() {
        if (lastTextTransform == null) {
            lastTextTransform = new AffineTransform();
        }
        return this.lastTextTransform;
    }

    /**
     * Set the last text transformation
     * @param newTransform last text transformation
     */
    public void setLastTextTransform(AffineTransform newTransform) {
        this.lastTextTransform = newTransform;
    }

    /**
     * Get the stored current text move
     * @return [horizontal text move, vertical text move]
     */
    public float[] getTextMove() {
        return textMove;
    }

    /**
     * Reset the stored text move to [0f,0f]
     */
    public void resetTextMove() {
        textMove = new float[]{0.0f, 0.0f};
    }

    /**
     * Increment the stored text move
     * @param additionalMoveX horizontal value to add
     * @param additionalMoveY vertical value to add
     */
    public void addTextMove(float additionalMoveX, float additionalMoveY) {
        textMove[0] += additionalMoveX;
        textMove[1] += additionalMoveY;
    }

    /**
     * Get the current canvas transformation
     * @return the {@link AffineTransform} representing the current canvas transformation
     */
    public AffineTransform getCurrentCanvasTransform() {
        Matrix currentTransform = getCurrentCanvas().getGraphicsState().getCtm();
        if (currentTransform != null) {
            return new AffineTransform(currentTransform.get(0), currentTransform.get(1),
                    currentTransform.get(3), currentTransform.get(4), currentTransform.get(6), currentTransform.get(7));
        }
        return new AffineTransform();
    }

    /**
     * Gets the SVG CSS context.
     *
     * @return the SVG CSS context
     */
    public SvgCssContext getCssContext() {
        return cssContext;
    }

    /**
     * Sets the SVG CSS context.
     *
     * @param cssContext the SVG CSS context
     */
    public void setCssContext(SvgCssContext cssContext) {
        this.cssContext = cssContext;
    }

    /**
     * Add pattern id to stack. Check if the id is already in the stack.
     * If it is, then return {@code false} and not add, if it is not - add and return {@code true}.
     *
     * @param patternId pattern id
     * @return {@code true} if pattern id was not on the stack and was pushed; {@code false} if it is on the stack
     */
    public boolean pushPatternId(String patternId) {
        if (this.patternIds.contains(patternId)) {
            return false;
        } else {
            this.patternIds.push(patternId);
            return true;
        }
    }

    /**
     * Pops the last template id from the stack.
     */
    public void popPatternId() {
        this.patternIds.pop();
    }

    public void setPreviousElementTextMove(float[] previousElementTextMove) {
        this.previousElementTextMove = previousElementTextMove;
    }

    public float[] getPreviousElementTextMove() {
        return previousElementTextMove;
    }
}
