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
package com.tanodxyz.itext722g.svg.renderers.factories;

import com.tanodxyz.itext722g.svg.SvgConstants;
import com.tanodxyz.itext722g.svg.renderers.ISvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.CircleSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.ClipPathSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.DefsSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.EllipseSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.GroupSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.ImageSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.LineSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.LinearGradientSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.MarkerSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.PathSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.PatternSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.PolygonSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.PolylineSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.RectangleSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.StopSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.SvgTagSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.SymbolSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.TextLeafSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.TextSvgBranchRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.TextSvgTSpanBranchRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.UseSvgNodeRenderer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Contains the mapping of the default implementations, provided by this project for the standard SVG
 * tags as defined in the SVG Specification.
 */
class DefaultSvgNodeRendererMapper {

    private static final String CLIP_PATH_LC = SvgConstants.Tags.CLIP_PATH.toLowerCase();
    private static final String LINEAR_GRADIENT_LC = SvgConstants.Tags.LINEAR_GRADIENT.toLowerCase();
    private static final String TEXT_LEAF_LC = SvgConstants.Tags.TEXT_LEAF.toLowerCase();

    /**
     * Creates a new {@link DefaultSvgNodeRendererMapper} instance.
     */
    DefaultSvgNodeRendererMapper() {
    }

    private static final Map<String, ISvgNodeRendererCreator> mapping;
    private static final Collection<String> ignored;

    static {
        Map<String, ISvgNodeRendererCreator> result = new HashMap<>();
        result.put(SvgConstants.Tags.CIRCLE, () -> new CircleSvgNodeRenderer());
        result.put(SvgConstants.Tags.CLIP_PATH, () -> new ClipPathSvgNodeRenderer());
        result.put(SvgConstants.Tags.DEFS, () -> new DefsSvgNodeRenderer());
        result.put(SvgConstants.Tags.ELLIPSE, () -> new EllipseSvgNodeRenderer());
        result.put(SvgConstants.Tags.G, () -> new GroupSvgNodeRenderer());
        result.put(SvgConstants.Tags.IMAGE, () -> new ImageSvgNodeRenderer());
        result.put(SvgConstants.Tags.LINE, () -> new LineSvgNodeRenderer());
        result.put(SvgConstants.Tags.LINEAR_GRADIENT, () -> new LinearGradientSvgNodeRenderer());
        result.put(SvgConstants.Tags.MARKER, () -> new MarkerSvgNodeRenderer());
        result.put(SvgConstants.Tags.PATTERN, () -> new PatternSvgNodeRenderer());
        result.put(SvgConstants.Tags.PATH, () -> new PathSvgNodeRenderer());
        result.put(SvgConstants.Tags.POLYGON, () -> new PolygonSvgNodeRenderer());
        result.put(SvgConstants.Tags.POLYLINE, () -> new PolylineSvgNodeRenderer());
        result.put(SvgConstants.Tags.RECT, () -> new RectangleSvgNodeRenderer());
        result.put(SvgConstants.Tags.STOP, () -> new StopSvgNodeRenderer());
        result.put(SvgConstants.Tags.SVG, () -> new SvgTagSvgNodeRenderer());
        result.put(SvgConstants.Tags.SYMBOL, () -> new SymbolSvgNodeRenderer());
        result.put(SvgConstants.Tags.TEXT, () -> new TextSvgBranchRenderer());
        result.put(SvgConstants.Tags.TSPAN, () -> new TextSvgTSpanBranchRenderer());
        result.put(SvgConstants.Tags.USE, () -> new UseSvgNodeRenderer());
        result.put(SvgConstants.Tags.TEXT_LEAF, () -> new TextLeafSvgNodeRenderer());

        // TODO: DEVSIX-3923 remove normalization (.toLowerCase)
        result.put(CLIP_PATH_LC, () -> new ClipPathSvgNodeRenderer());
        result.put(LINEAR_GRADIENT_LC, () -> new LinearGradientSvgNodeRenderer());
        result.put(TEXT_LEAF_LC, () -> new TextLeafSvgNodeRenderer());

        mapping = Collections.unmodifiableMap(result);

        // Not supported tags as of yet
        Collection<String> ignoredTags = new HashSet<>();

        ignoredTags.add(SvgConstants.Tags.A);
        ignoredTags.add(SvgConstants.Tags.ALT_GLYPH);
        ignoredTags.add(SvgConstants.Tags.ALT_GLYPH_DEF);
        ignoredTags.add(SvgConstants.Tags.ALT_GLYPH_ITEM);

        ignoredTags.add(SvgConstants.Tags.COLOR_PROFILE);

        ignoredTags.add(SvgConstants.Tags.DESC);

        ignoredTags.add(SvgConstants.Tags.FE_BLEND);
        ignoredTags.add(SvgConstants.Tags.FE_COLOR_MATRIX);
        ignoredTags.add(SvgConstants.Tags.FE_COMPONENT_TRANSFER);
        ignoredTags.add(SvgConstants.Tags.FE_COMPOSITE);
        ignoredTags.add(SvgConstants.Tags.FE_COMVOLVE_MATRIX);
        ignoredTags.add(SvgConstants.Tags.FE_DIFFUSE_LIGHTING);
        ignoredTags.add(SvgConstants.Tags.FE_DISPLACEMENT_MAP);
        ignoredTags.add(SvgConstants.Tags.FE_DISTANT_LIGHT);
        ignoredTags.add(SvgConstants.Tags.FE_FLOOD);
        ignoredTags.add(SvgConstants.Tags.FE_FUNC_A);
        ignoredTags.add(SvgConstants.Tags.FE_FUNC_B);
        ignoredTags.add(SvgConstants.Tags.FE_FUNC_G);
        ignoredTags.add(SvgConstants.Tags.FE_FUNC_R);
        ignoredTags.add(SvgConstants.Tags.FE_GAUSSIAN_BLUR);
        ignoredTags.add(SvgConstants.Tags.FE_IMAGE);
        ignoredTags.add(SvgConstants.Tags.FE_MERGE);
        ignoredTags.add(SvgConstants.Tags.FE_MERGE_NODE);
        ignoredTags.add(SvgConstants.Tags.FE_MORPHOLOGY);
        ignoredTags.add(SvgConstants.Tags.FE_OFFSET);
        ignoredTags.add(SvgConstants.Tags.FE_POINT_LIGHT);
        ignoredTags.add(SvgConstants.Tags.FE_SPECULAR_LIGHTING);
        ignoredTags.add(SvgConstants.Tags.FE_SPOTLIGHT);
        ignoredTags.add(SvgConstants.Tags.FE_TILE);
        ignoredTags.add(SvgConstants.Tags.FE_TURBULENCE);
        ignoredTags.add(SvgConstants.Tags.FILTER);
        ignoredTags.add(SvgConstants.Tags.FONT);
        ignoredTags.add(SvgConstants.Tags.FONT_FACE);
        ignoredTags.add(SvgConstants.Tags.FONT_FACE_FORMAT);
        ignoredTags.add(SvgConstants.Tags.FONT_FACE_NAME);
        ignoredTags.add(SvgConstants.Tags.FONT_FACE_SRC);
        ignoredTags.add(SvgConstants.Tags.FONT_FACE_URI);
        ignoredTags.add(SvgConstants.Tags.FOREIGN_OBJECT);

        ignoredTags.add(SvgConstants.Tags.GLYPH);
        ignoredTags.add(SvgConstants.Tags.GLYPH_REF);

        ignoredTags.add(SvgConstants.Tags.HKERN);

        ignoredTags.add(SvgConstants.Tags.MASK);
        ignoredTags.add(SvgConstants.Tags.METADATA);
        ignoredTags.add(SvgConstants.Tags.MISSING_GLYPH);

        ignoredTags.add(SvgConstants.Tags.RADIAL_GRADIENT);

        ignoredTags.add(SvgConstants.Tags.STYLE);

        ignoredTags.add(SvgConstants.Tags.TITLE);

        ignored = Collections.unmodifiableCollection(ignoredTags);
    }

    /**
     * Gets the default SVG tags mapping.
     *
     * @return the default SVG tags mapping
     */
    Map<String, ISvgNodeRendererCreator> getMapping() {
        return mapping;
    }

    /**
     * Gets the default ignored SVG tags.
     * @return default ignored SVG tags
     */
    Collection<String> getIgnoredTags() {
        return ignored;
    }

    /**
     * Represents a function, which creates {@link ISvgNodeRenderer} instance.
     */
    @FunctionalInterface
    public interface ISvgNodeRendererCreator {
        /**
         * Creates an {@link ISvgNodeRenderer} instance.
         * @return {@link ISvgNodeRenderer} instance.
         */
        ISvgNodeRenderer create();
    }
}
