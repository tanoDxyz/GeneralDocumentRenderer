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


import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.kernel.colors.Color;
import com.tanodxyz.itext722g.kernel.colors.PatternColor;
import com.tanodxyz.itext722g.kernel.geom.AffineTransform;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.pdf.PdfArray;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvas;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfPatternCanvas;
import com.tanodxyz.itext722g.kernel.pdf.colorspace.PdfPattern;
import com.tanodxyz.itext722g.svg.SvgConstants;
import com.tanodxyz.itext722g.svg.SvgConstants.Attributes;
import com.tanodxyz.itext722g.svg.SvgConstants.Values;
import com.tanodxyz.itext722g.svg.logs.SvgLogMessageConstant;
import com.tanodxyz.itext722g.svg.renderers.ISvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.ISvgPaintServer;
import com.tanodxyz.itext722g.svg.renderers.SvgDrawContext;
import com.tanodxyz.itext722g.svg.utils.SvgCoordinateUtils;
import com.tanodxyz.itext722g.svg.utils.TransformUtils;

import java.util.logging.Logger;


/**
 * Implementation for the svg &lt;pattern&gt; tag.
 */
public class PatternSvgNodeRenderer extends AbstractBranchSvgNodeRenderer implements ISvgPaintServer {

    private static final Logger LOGGER = Logger.getLogger(PatternSvgNodeRenderer.class.getName());

    private static final double CONVERT_COEFF = 0.75;

    @Override
    public ISvgNodeRenderer createDeepCopy() {
        final PatternSvgNodeRenderer copy = new PatternSvgNodeRenderer();
        deepCopyAttributesAndStyles(copy);
        deepCopyChildren(copy);
        return copy;
    }

    @Override
    public Color createColor(SvgDrawContext context, Rectangle objectBoundingBox, float objectBoundingBoxMargin,
                             float parentOpacity) {
        if (objectBoundingBox == null) {
            return null;
        }
        if (!context.pushPatternId(getAttribute(Attributes.ID))) {
            // this means that pattern is cycled
            return null;
        }
        try {
            PdfPattern.Tiling tilingPattern = createTilingPattern(context, objectBoundingBox);
            drawPatternContent(context, tilingPattern);
            return (tilingPattern == null) ? null : new PatternColor(tilingPattern);
        } finally {
            context.popPatternId();
        }
    }

    @Override
    public Rectangle getObjectBoundingBox(SvgDrawContext context) {
        return null;
    }

    private PdfPattern.Tiling createTilingPattern(SvgDrawContext context,
                                                  Rectangle objectBoundingBox) {
        final boolean isObjectBoundingBoxInPatternUnits = isObjectBoundingBoxInPatternUnits();
        final boolean isObjectBoundingBoxInPatternContentUnits = isObjectBoundingBoxInPatternContentUnits();

        // evaluate pattern rectangle on target pattern units
        Rectangle originalPatternRectangle = calculateOriginalPatternRectangle(
                context, isObjectBoundingBoxInPatternUnits);

        // get xStep and yStep on target pattern units
        double xStep = originalPatternRectangle.getWidth();
        double yStep = originalPatternRectangle.getHeight();

        if (!xStepYStepAreValid(xStep, yStep)) {
            return null;
        }

        // we have to consider transforming an element that use pattern in corresponding  with SVG logic
        final AffineTransform patternMatrixTransform = context.getCurrentCanvasTransform();

        patternMatrixTransform.concatenate(getPatternTransform());

        if (isObjectBoundingBoxInPatternUnits) {
            patternMatrixTransform.concatenate(getTransformToUserSpaceOnUse(objectBoundingBox));
        }

        patternMatrixTransform.translate(originalPatternRectangle.getX(), originalPatternRectangle.getY());

        final float[] viewBoxValues = getViewBoxValues();
        Rectangle bbox;
        if (viewBoxValues.length < VIEWBOX_VALUES_NUMBER) {
            if (isObjectBoundingBoxInPatternUnits != isObjectBoundingBoxInPatternContentUnits) {
                // If pattern units are not the same as pattern content units, then we need to scale
                // the resulted space into a space to draw pattern content. The pattern rectangle origin
                // is already in place, but measures should be adjusted.
                double scaleX, scaleY;
                if (isObjectBoundingBoxInPatternContentUnits) {
                    scaleX = objectBoundingBox.getWidth() / CONVERT_COEFF;
                    scaleY = objectBoundingBox.getHeight() / CONVERT_COEFF;
                } else {
                    scaleX = CONVERT_COEFF / objectBoundingBox.getWidth();
                    scaleY = CONVERT_COEFF / objectBoundingBox.getHeight();
                }
                patternMatrixTransform.scale(scaleX, scaleY);
                xStep /= scaleX;
                yStep /= scaleY;
            }
            bbox = new Rectangle(0F, 0F, (float) xStep, (float) yStep);
        } else {
            if (isViewBoxInvalid(viewBoxValues)) {
                return null;
            }

            // Here we revert scaling to the object's bounding box coordinate system
            // to keep the aspect ratio of the original viewport of the pattern.
            if (isObjectBoundingBoxInPatternUnits) {
                double scaleX = CONVERT_COEFF / objectBoundingBox.getWidth();
                double scaleY = CONVERT_COEFF / objectBoundingBox.getHeight();
                patternMatrixTransform.scale(scaleX, scaleY);
                xStep /= scaleX;
                yStep /= scaleY;
            }

            Rectangle viewBox = new Rectangle(viewBoxValues[0], viewBoxValues[1], viewBoxValues[2], viewBoxValues[3]);
            Rectangle appliedViewBox = calculateAppliedViewBox(viewBox, xStep, yStep);

            patternMatrixTransform.translate(appliedViewBox.getX(), appliedViewBox.getY());

            double scaleX = (double) appliedViewBox.getWidth() / (double) viewBox.getWidth();
            double scaleY = (double) appliedViewBox.getHeight() / (double) viewBox.getHeight();
            patternMatrixTransform.scale(scaleX, scaleY);
            xStep /= scaleX;
            yStep /= scaleY;

            patternMatrixTransform.translate(-viewBox.getX(), -viewBox.getY());

            double bboxXOriginal = viewBox.getX() - appliedViewBox.getX() / scaleX;
            double bboxYOriginal = viewBox.getY() - appliedViewBox.getY() / scaleY;
            bbox = new Rectangle((float) bboxXOriginal, (float) bboxYOriginal, (float) xStep, (float) yStep);
        }

        return createColoredTilingPatternInstance(patternMatrixTransform, bbox, xStep, yStep);
    }

    private Rectangle calculateAppliedViewBox(Rectangle viewBox, double xStep, double yStep) {
        String[] preserveAspectRatio = retrieveAlignAndMeet();
        Rectangle patternRect = new Rectangle(0f, 0f, (float) xStep, (float) yStep);
        return SvgCoordinateUtils.applyViewBox(viewBox, patternRect, preserveAspectRatio[0], preserveAspectRatio[1]);
    }

    private void drawPatternContent(SvgDrawContext context, PdfPattern.Tiling pattern) {
        if (pattern == null) {
            return;
        }
        final PdfCanvas patternCanvas = new PdfPatternCanvas(pattern,
                context.getCurrentCanvas().getDocument());
        context.pushCanvas(patternCanvas);
        try {
            for (final ISvgNodeRenderer renderer : this.getChildren()) {
                renderer.draw(context);
            }
        } finally {
            context.popCanvas();
        }
    }

    private Rectangle calculateOriginalPatternRectangle(SvgDrawContext context,
                                                        boolean isObjectBoundingBoxInPatternUnits) {
        double xOffset, yOffset, xStep, yStep;
        if (isObjectBoundingBoxInPatternUnits) {
            xOffset = SvgCoordinateUtils.getCoordinateForObjectBoundingBox(
                    getAttribute(Attributes.X), 0) * CONVERT_COEFF;
            yOffset = SvgCoordinateUtils.getCoordinateForObjectBoundingBox(
                    getAttribute(Attributes.Y), 0) * CONVERT_COEFF;
            xStep = SvgCoordinateUtils.getCoordinateForObjectBoundingBox(
                    getAttribute(Attributes.WIDTH), 0) * CONVERT_COEFF;
            yStep = SvgCoordinateUtils.getCoordinateForObjectBoundingBox(
                    getAttribute(Attributes.HEIGHT), 0) * CONVERT_COEFF;
        } else {
            final Rectangle currentViewPort = context.getCurrentViewPort();
            final double viewPortX = currentViewPort.getX();
            final double viewPortY = currentViewPort.getY();
            final double viewPortWidth = currentViewPort.getWidth();
            final double viewPortHeight = currentViewPort.getHeight();
            final float em = getCurrentFontSize();
            final float rem = context.getCssContext().getRootFontSize();
            // get pattern coordinates in userSpaceOnUse coordinate system
            xOffset = SvgCoordinateUtils.getCoordinateForUserSpaceOnUse(
                    getAttribute(Attributes.X), viewPortX, viewPortX, viewPortWidth, em, rem);
            yOffset = SvgCoordinateUtils.getCoordinateForUserSpaceOnUse(
                    getAttribute(Attributes.Y), viewPortY, viewPortY, viewPortHeight, em, rem);
            xStep = SvgCoordinateUtils.getCoordinateForUserSpaceOnUse(
                    getAttribute(Attributes.WIDTH), viewPortX, viewPortX, viewPortWidth, em, rem);
            yStep = SvgCoordinateUtils.getCoordinateForUserSpaceOnUse(
                    getAttribute(Attributes.HEIGHT), viewPortY, viewPortY, viewPortHeight, em, rem);
        }
        return new Rectangle((float) xOffset, (float) yOffset, (float) xStep, (float) yStep);
    }

    private boolean isObjectBoundingBoxInPatternUnits() {
        String patternUnits = getAttribute(Attributes.PATTERN_UNITS);
        if (patternUnits == null) {
            patternUnits = getAttribute(Attributes.PATTERN_UNITS.toLowerCase());
        }
        if (Values.USER_SPACE_ON_USE.equals(patternUnits)) {
            return false;
        } else if (patternUnits != null && !Values.OBJECT_BOUNDING_BOX.equals(patternUnits)) {
            Logger.getLogger(this.getClass().getName()).warning(MessageFormatUtil.format(
                    SvgLogMessageConstant.PATTERN_INVALID_PATTERN_UNITS_LOG, patternUnits));
        }
        return true;
    }

    private boolean isObjectBoundingBoxInPatternContentUnits() {
        String patternContentUnits = getAttribute(Attributes.PATTERN_CONTENT_UNITS);
        if (patternContentUnits == null) {
            patternContentUnits = getAttribute(Attributes.PATTERN_CONTENT_UNITS.toLowerCase());
        }
        if (Values.OBJECT_BOUNDING_BOX.equals(patternContentUnits)) {
            return true;
        } else if (patternContentUnits != null && !Values.USER_SPACE_ON_USE
                .equals(patternContentUnits)) {
            Logger.getLogger(this.getClass().getName()).warning(MessageFormatUtil.format(
                    SvgLogMessageConstant.PATTERN_INVALID_PATTERN_CONTENT_UNITS_LOG, patternContentUnits));
        }
        return false;
    }

    private static PdfPattern.Tiling createColoredTilingPatternInstance(AffineTransform patternAffineTransform,
                                                                        Rectangle bbox, double xStep, double yStep) {
        PdfPattern.Tiling coloredTilingPattern = new PdfPattern.Tiling(bbox, (float) xStep, (float) yStep,
                true);
        setPatternMatrix(coloredTilingPattern, patternAffineTransform);
        return coloredTilingPattern;
    }

    private static void setPatternMatrix(PdfPattern.Tiling pattern, AffineTransform affineTransform) {
        if (!affineTransform.isIdentity()) {
            final double[] patternMatrix = new double[6];
            affineTransform.getMatrix(patternMatrix);
            pattern.setMatrix(new PdfArray(patternMatrix));
        }
    }

    private static AffineTransform getTransformToUserSpaceOnUse(Rectangle objectBoundingBox) {
        AffineTransform transform = new AffineTransform();
        transform.translate(objectBoundingBox.getX(), objectBoundingBox.getY());
        transform.scale(objectBoundingBox.getWidth() / CONVERT_COEFF,
                objectBoundingBox.getHeight() / CONVERT_COEFF);
        return transform;
    }

    private static boolean xStepYStepAreValid(double xStep, double yStep) {
        if (xStep < 0 || yStep < 0) {
            LOGGER.warning(MessageFormatUtil
                    .format(SvgLogMessageConstant.PATTERN_WIDTH_OR_HEIGHT_IS_NEGATIVE));

            return false;
        } else if (xStep == 0 || yStep == 0) {
            LOGGER.info(MessageFormatUtil
                    .format(SvgLogMessageConstant.PATTERN_WIDTH_OR_HEIGHT_IS_ZERO));

            return false;
        } else {
            return true;
        }
    }

    private static boolean isViewBoxInvalid(float[] viewBoxValues) {
        // if viewBox width or height is zero we should disable rendering
        // of the element (according to the viewBox documentation)
        if (viewBoxValues[2] == 0 || viewBoxValues[3] == 0) {
            LOGGER.info(MessageFormatUtil
                    .format(SvgLogMessageConstant.VIEWBOX_WIDTH_OR_HEIGHT_IS_ZERO));

            return true;
        } else {
            return false;
        }
    }

    private AffineTransform getPatternTransform() {
        String patternTransform = getAttribute(SvgConstants.Attributes.PATTERN_TRANSFORM);
        if (patternTransform == null) {
            patternTransform = getAttribute(SvgConstants.Attributes.PATTERN_TRANSFORM.toLowerCase());
        }
        if (patternTransform != null && !patternTransform.isEmpty()) {
            return TransformUtils.parseTransform(patternTransform);
        }
        return new AffineTransform();
    }
}
