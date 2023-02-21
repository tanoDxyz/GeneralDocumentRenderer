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


import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.svg.exceptions.SvgExceptionMessageConstant;
import com.tanodxyz.itext722g.svg.renderers.INoDrawSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.ISvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.SvgDrawContext;

public class DefsSvgNodeRenderer extends AbstractBranchSvgNodeRenderer implements INoDrawSvgNodeRenderer {
    @Override
    protected void doDraw(SvgDrawContext context) {
        throw new UnsupportedOperationException(SvgExceptionMessageConstant.DRAW_NO_DRAW);
    }

    @Override
    public ISvgNodeRenderer createDeepCopy() {
        DefsSvgNodeRenderer copy = new DefsSvgNodeRenderer();
        deepCopyAttributesAndStyles(copy);
        return copy;
    }

    @Override
    public Rectangle getObjectBoundingBox(SvgDrawContext context) {
        return null;
    }
}
