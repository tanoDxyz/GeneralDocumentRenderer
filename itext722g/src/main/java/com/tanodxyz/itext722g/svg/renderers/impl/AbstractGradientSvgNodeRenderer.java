/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tanodxyz.itext722g.svg.renderers.impl;


import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.kernel.colors.gradients.GradientSpreadMethod;
import com.tanodxyz.itext722g.kernel.geom.AffineTransform;
import com.tanodxyz.itext722g.svg.SvgConstants.Attributes;
import com.tanodxyz.itext722g.svg.SvgConstants.Values;
import com.tanodxyz.itext722g.svg.exceptions.SvgExceptionMessageConstant;
import com.tanodxyz.itext722g.svg.logs.SvgLogMessageConstant;
import com.tanodxyz.itext722g.svg.renderers.ISvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.ISvgPaintServer;
import com.tanodxyz.itext722g.svg.renderers.SvgDrawContext;
import com.tanodxyz.itext722g.svg.utils.TransformUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@link ISvgNodeRenderer} abstract implementation for gradient tags
 * (&lt;linearGradient&gt;, &lt;radialGradient&gt;).
 */
public abstract class AbstractGradientSvgNodeRenderer extends AbstractBranchSvgNodeRenderer implements
        ISvgPaintServer {

    @Override
    protected void doDraw(SvgDrawContext context) {
        throw new UnsupportedOperationException(SvgExceptionMessageConstant.DRAW_NO_DRAW);
    }

    /**
     * Checks whether the gradient units values are on user space on use or object bounding box
     *
     * @return {@code false} if the 'gradientUnits' value of the gradient tag equals
     * to 'userSpaceOnUse', otherwise {@code true}
     */
    protected boolean isObjectBoundingBoxUnits() {
        String gradientUnits = getAttribute(Attributes.GRADIENT_UNITS);
        // TODO: DEVSIX-3923 remove normalization (.toLowerCase)
        if (gradientUnits == null) {
            getAttribute(Attributes.GRADIENT_UNITS.toLowerCase());
        }
        if (Values.USER_SPACE_ON_USE.equals(gradientUnits)) {
            return false;
        } else if (gradientUnits != null && !Values.OBJECT_BOUNDING_BOX.equals(gradientUnits)) {
            Logger.getLogger(this.getClass().getName()).warning(MessageFormatUtil.format(
                    SvgLogMessageConstant.GRADIENT_INVALID_GRADIENT_UNITS_LOG, gradientUnits));
        }
        return true;
    }

    /**
     * Evaluates the 'gradientTransform' transformations
     * @return an {@link AffineTransform} object representing the specified gradient transformation
     */
    protected AffineTransform getGradientTransform() {
        String gradientTransform = getAttribute(Attributes.GRADIENT_TRANSFORM);
        // TODO: DEVSIX-3923 remove normalization (.toLowerCase)
        if (gradientTransform == null) {
            gradientTransform = getAttribute(Attributes.GRADIENT_TRANSFORM.toLowerCase());
        }
        if (gradientTransform != null && !gradientTransform.isEmpty()) {
            return TransformUtils.parseTransform(gradientTransform);
        }
        return null;
    }

    /**
     * Construct a list of child stop renderers
     * @return a list of {@link StopSvgNodeRenderer} elements that represents the child stop values
     */
    protected List< StopSvgNodeRenderer> getChildStopRenderers() {
        List< StopSvgNodeRenderer> stopRenderers = new ArrayList<>();
        for (ISvgNodeRenderer child : getChildren()) {
            if (child instanceof  StopSvgNodeRenderer) {
                stopRenderers.add(( StopSvgNodeRenderer) child);
            }
        }
        return stopRenderers;
    }

    /**
     * Parses the gradient spread method
     * @return the parsed {@link GradientSpreadMethod} specified in the gradient
     */
    protected GradientSpreadMethod parseSpreadMethod() {
        String spreadMethodValue = getAttribute(Attributes.SPREAD_METHOD);
        if (spreadMethodValue == null) {
            spreadMethodValue = getAttribute(Attributes.SPREAD_METHOD.toLowerCase());
        }
        if (spreadMethodValue == null) {
            // returning svg default spread method
            return GradientSpreadMethod.PAD;
        }
        switch (spreadMethodValue) {
            case Values.SPREAD_METHOD_PAD:
                return GradientSpreadMethod.PAD;
            case Values.SPREAD_METHOD_REFLECT:
                return GradientSpreadMethod.REFLECT;
            case Values.SPREAD_METHOD_REPEAT:
                return GradientSpreadMethod.REPEAT;
            default:
                Logger.getLogger(this.getClass().getName()).warning(MessageFormatUtil.format(
                        SvgLogMessageConstant.GRADIENT_INVALID_SPREAD_METHOD_LOG, spreadMethodValue));
                return GradientSpreadMethod.PAD;
        }
    }
}
