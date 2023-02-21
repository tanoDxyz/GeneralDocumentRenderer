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
package com.tanodxyz.itext722g.layout.renderer;


import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.io.font.constants.StandardFonts;
import com.tanodxyz.itext722g.io.logs.IoLogMessageConstant;
import com.tanodxyz.itext722g.io.util.TextUtil;
import com.tanodxyz.itext722g.kernel.font.PdfFontFactory;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.numbering.EnglishAlphabetNumbering;
import com.tanodxyz.itext722g.kernel.numbering.GreekAlphabetNumbering;
import com.tanodxyz.itext722g.kernel.numbering.RomanNumbering;
import com.tanodxyz.itext722g.kernel.pdf.tagging.StandardRoles;
import com.tanodxyz.itext722g.layout.element.Image;
import com.tanodxyz.itext722g.layout.element.Text;
import com.tanodxyz.itext722g.layout.layout.LayoutArea;
import com.tanodxyz.itext722g.layout.layout.LayoutContext;
import com.tanodxyz.itext722g.layout.layout.LayoutResult;
import com.tanodxyz.itext722g.layout.minmaxwidth.MinMaxWidth;
import com.tanodxyz.itext722g.layout.minmaxwidth.MinMaxWidthUtils;
import com.tanodxyz.itext722g.layout.properties.BaseDirection;
import com.tanodxyz.itext722g.layout.properties.IListSymbolFactory;
import com.tanodxyz.itext722g.layout.properties.ListNumberingType;
import com.tanodxyz.itext722g.layout.properties.ListSymbolPosition;
import com.tanodxyz.itext722g.layout.properties.Property;
import com.tanodxyz.itext722g.layout.properties.UnitValue;
import com.tanodxyz.itext722g.layout.tagging.LayoutTaggingHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListRenderer extends BlockRenderer {

    /**
     * Creates a ListRenderer from its corresponding layout object.
     *
     * @param modelElement the {@link  List} which this object should manage
     */
    public ListRenderer( com.tanodxyz.itext722g.layout.element.List modelElement) {
        super(modelElement);
    }

    @Override
    public LayoutResult layout(LayoutContext layoutContext) {
        LayoutResult errorResult = initializeListSymbols(layoutContext);
        if (errorResult != null) {
            return errorResult;
        }
        LayoutResult result = super.layout(layoutContext);
        // cannot place even the first ListItemRenderer
        if (Boolean.TRUE.equals(getPropertyAsBoolean(Property.FORCED_PLACEMENT)) && null != result.getCauseOfNothing()) {
            if (LayoutResult.FULL == result.getStatus()) {
                result = correctListSplitting(this, null, result.getCauseOfNothing(), result.getOccupiedArea());
            } else if (LayoutResult.PARTIAL == result.getStatus()) {
                result = correctListSplitting(result.getSplitRenderer(), result.getOverflowRenderer(), result.getCauseOfNothing(), result.getOccupiedArea());
            }
        }
        return result;
    }

    /**
     * Gets a new instance of this class to be used as a next renderer, after this renderer is used, if
     * {@link #layout(LayoutContext)} is called more than once.
     *
     * <p>
     * If a renderer overflows to the next area, iText uses this method to create a renderer
     * for the overflow part. So if one wants to extend {@link ListRenderer}, one should override
     * this method: otherwise the default method will be used and thus the default rather than the custom
     * renderer will be created.
     * @return new renderer instance
     */
    @Override
    public IRenderer getNextRenderer() {
        logWarningIfGetNextRendererNotOverridden(ListRenderer.class, this.getClass());
        return new ListRenderer(( com.tanodxyz.itext722g.layout.element.List) modelElement);
    }

    @Override
    protected AbstractRenderer createSplitRenderer(int layoutResult) {
        AbstractRenderer splitRenderer = super.createSplitRenderer(layoutResult);
        splitRenderer.addAllProperties(getOwnProperties());
        splitRenderer.setProperty(Property.LIST_SYMBOLS_INITIALIZED, Boolean.TRUE);
        return splitRenderer;
    }

    @Override
    protected AbstractRenderer createOverflowRenderer(int layoutResult) {
        AbstractRenderer overflowRenderer = super.createOverflowRenderer(layoutResult);
        overflowRenderer.addAllProperties(getOwnProperties());
        overflowRenderer.setProperty(Property.LIST_SYMBOLS_INITIALIZED, Boolean.TRUE);
        return overflowRenderer;
    }

    @Override
    public MinMaxWidth getMinMaxWidth() {
        LayoutResult errorResult = initializeListSymbols(new LayoutContext(new LayoutArea(1, new Rectangle(MinMaxWidthUtils.getInfWidth(), AbstractRenderer.INF))));
        if (errorResult != null) {
            return MinMaxWidthUtils.countDefaultMinMaxWidth(this);
        }
        return super.getMinMaxWidth();
    }

    protected IRenderer makeListSymbolRenderer(int index, IRenderer renderer) {
        IRenderer symbolRenderer = createListSymbolRenderer(index, renderer);
        // underlying should not be applied
        if (symbolRenderer != null) {
            symbolRenderer.setProperty(Property.UNDERLINE, false);
        }
        return symbolRenderer;
    }

    static Object getListItemOrListProperty(IRenderer listItem, IRenderer list, int propertyId) {
        return listItem.hasProperty(propertyId) ? listItem.<Object>getProperty(propertyId) : list.<Object>getProperty(propertyId);
    }

    private IRenderer createListSymbolRenderer(int index, IRenderer renderer) {
        Object defaultListSymbol = getListItemOrListProperty(renderer, this, Property.LIST_SYMBOL);
        if (defaultListSymbol instanceof Text) {
            return surroundTextBullet(new  TextRenderer((Text) defaultListSymbol));
        } else if (defaultListSymbol instanceof Image) {
            return new ImageRenderer((Image) defaultListSymbol);
        } else if (defaultListSymbol instanceof ListNumberingType) {
            ListNumberingType numberingType = (ListNumberingType) defaultListSymbol;
            String numberText;
            switch (numberingType) {
                case DECIMAL:
                    numberText = String.valueOf(index);
                    break;
                case DECIMAL_LEADING_ZERO:
                    numberText = (index < 10 ? "0" : "") + String.valueOf(index);
                    break;
                case ROMAN_LOWER:
                    numberText = RomanNumbering.toRomanLowerCase(index);
                    break;
                case ROMAN_UPPER:
                    numberText = RomanNumbering.toRomanUpperCase(index);
                    break;
                case ENGLISH_LOWER:
                    numberText = EnglishAlphabetNumbering.toLatinAlphabetNumberLowerCase(index);
                    break;
                case ENGLISH_UPPER:
                    numberText = EnglishAlphabetNumbering.toLatinAlphabetNumberUpperCase(index);
                    break;
                case GREEK_LOWER:
                    numberText = GreekAlphabetNumbering.toGreekAlphabetNumber(index, false, true);
                    break;
                case GREEK_UPPER:
                    numberText = GreekAlphabetNumbering.toGreekAlphabetNumber(index, true, true);
                    break;
                case ZAPF_DINGBATS_1:
                    numberText = TextUtil.charToString((char) (index + 171));
                    break;
                case ZAPF_DINGBATS_2:
                    numberText = TextUtil.charToString((char) (index + 181));
                    break;
                case ZAPF_DINGBATS_3:
                    numberText = TextUtil.charToString((char) (index + 191));
                    break;
                case ZAPF_DINGBATS_4:
                    numberText = TextUtil.charToString((char) (index + 201));
                    break;
                default:
                    throw new IllegalStateException();
            }
            Text textElement = new Text(getListItemOrListProperty(renderer, this, Property.LIST_SYMBOL_PRE_TEXT) + numberText + getListItemOrListProperty(renderer, this, Property.LIST_SYMBOL_POST_TEXT));
            IRenderer textRenderer;
            // Be careful. There is a workaround here. For Greek symbols we first set a dummy font with document=null
            // in order for the metrics to be taken into account correctly during layout.
            // Then on draw we set the correct font with actual document in order for the font objects to be created.
            if (numberingType == ListNumberingType.GREEK_LOWER || numberingType == ListNumberingType.GREEK_UPPER ||
                    numberingType == ListNumberingType.ZAPF_DINGBATS_1 || numberingType == ListNumberingType.ZAPF_DINGBATS_2 ||
                    numberingType == ListNumberingType.ZAPF_DINGBATS_3 || numberingType == ListNumberingType.ZAPF_DINGBATS_4) {

                final String constantFont = (numberingType == ListNumberingType.GREEK_LOWER || numberingType == ListNumberingType.GREEK_UPPER) ?
                        StandardFonts.SYMBOL : StandardFonts.ZAPFDINGBATS;

                textRenderer = new ConstantFontTextRenderer(textElement, constantFont);
                try {
                    textRenderer.setProperty(Property.FONT, PdfFontFactory.createFont(constantFont));
                } catch (IOException exc) {
                }
            } else {
                textRenderer = new  TextRenderer(textElement);
            }
           return surroundTextBullet(textRenderer);
        } else if (defaultListSymbol instanceof IListSymbolFactory) {
            return surroundTextBullet(((IListSymbolFactory) defaultListSymbol).createSymbol(index, this, renderer).createRendererSubTree());
        } else if (defaultListSymbol == null) {
            return null;
        } else {
            throw new IllegalStateException();
        }
    }


    // Wrap the bullet with a line because the direction (f.e. RTL) is processed on the LineRenderer level.
    private LineRenderer surroundTextBullet(IRenderer bulletRenderer) {
        LineRenderer lineRenderer = new LineRenderer();
        Text zeroWidthJoiner = new Text("\u200D");
        zeroWidthJoiner.getAccessibilityProperties().setRole(StandardRoles.ARTIFACT);
         TextRenderer zeroWidthJoinerRenderer = new  TextRenderer(zeroWidthJoiner);
        lineRenderer.addChild(zeroWidthJoinerRenderer);
        lineRenderer.addChild(bulletRenderer);
        lineRenderer.addChild(zeroWidthJoinerRenderer);
        return lineRenderer;
    }

    /**
         * Corrects split and overflow renderers when {@link  Property#FORCED_PLACEMENT} is applied.
     * <p>
     * We assume that {@link  Property#FORCED_PLACEMENT} is applied when the first
     * {@link  ListItemRenderer} cannot be fully layouted.
     * This means that the problem has occurred in one of the first list item renderer's children.
     * In that case we force the placement of all first item renderer's children before the one,
     * which was the cause of {@link com.tanodxyz.itext722g.layout.layout.LayoutResult#NOTHING}, including this child.
     * <p>
     * Notice that we do not expect {@link  Property#FORCED_PLACEMENT} to be applied
     * if we can render the first item renderer and strongly recommend not to set
     * {@link  Property#FORCED_PLACEMENT} manually.
     *
     * @param splitRenderer    the {@link IRenderer split renderer} before correction
     * @param overflowRenderer the {@link IRenderer overflow renderer} before correction
     * @param causeOfNothing   the renderer which has produced {@link LayoutResult#NOTHING}
     * @param occupiedArea     the area occupied by layout before correction
     * @return corrected {@link com.tanodxyz.itext722g.layout.layout.LayoutResult layout result}
     */
    private LayoutResult correctListSplitting(IRenderer splitRenderer, IRenderer overflowRenderer, IRenderer causeOfNothing, LayoutArea occupiedArea) {
        // the first not rendered child
        int firstNotRendered = splitRenderer.getChildRenderers().get(0).getChildRenderers().indexOf(causeOfNothing);

        if (-1 == firstNotRendered) {
            return new LayoutResult(null == overflowRenderer ? LayoutResult.FULL : LayoutResult.PARTIAL,
                    occupiedArea, splitRenderer, overflowRenderer, this);
        }

        // Notice that placed item is a son of the first ListItemRenderer (otherwise there would be now FORCED_PLACEMENT applied)
        IRenderer firstListItemRenderer = splitRenderer.getChildRenderers().get(0);

        ListRenderer newOverflowRenderer = (ListRenderer) createOverflowRenderer(LayoutResult.PARTIAL);
        newOverflowRenderer.deleteOwnProperty(Property.FORCED_PLACEMENT);
        // ListItemRenderer for not rendered children of firstListItemRenderer
        newOverflowRenderer.childRenderers.add(((ListItemRenderer) firstListItemRenderer).createOverflowRenderer(LayoutResult.PARTIAL));
        newOverflowRenderer.childRenderers.addAll(splitRenderer.getChildRenderers().subList(1, splitRenderer.getChildRenderers().size()));

        List<IRenderer> childrenStillRemainingToRender =
                new ArrayList<>(firstListItemRenderer.getChildRenderers().subList(firstNotRendered + 1, firstListItemRenderer.getChildRenderers().size()));

        // 'this' renderer will become split renderer
        splitRenderer.getChildRenderers().removeAll(splitRenderer.getChildRenderers().subList(1, splitRenderer.getChildRenderers().size()));

        if (0 != childrenStillRemainingToRender.size()) {
            newOverflowRenderer.getChildRenderers().get(0).getChildRenderers().addAll(childrenStillRemainingToRender);
            splitRenderer.getChildRenderers().get(0).getChildRenderers().removeAll(childrenStillRemainingToRender);
            newOverflowRenderer.getChildRenderers().get(0).setProperty(Property.MARGIN_LEFT, splitRenderer.getChildRenderers().get(0).<UnitValue>getProperty(Property.MARGIN_LEFT));
        } else {
            newOverflowRenderer.childRenderers.remove(0);
        }

        if (null != overflowRenderer) {
            newOverflowRenderer.childRenderers.addAll(overflowRenderer.getChildRenderers());
        }

        if (0 != newOverflowRenderer.childRenderers.size()) {
            return new LayoutResult(LayoutResult.PARTIAL, occupiedArea, splitRenderer, newOverflowRenderer, this);
        } else {
            return new LayoutResult(LayoutResult.FULL, occupiedArea, null, null, this);
        }
    }

    private LayoutResult initializeListSymbols(LayoutContext layoutContext) {
        if (!hasOwnProperty(Property.LIST_SYMBOLS_INITIALIZED)) {
            List<IRenderer> symbolRenderers = new ArrayList<>();
            int listItemNum = (int)this.<Integer>getProperty(Property.LIST_START, 1);
            for (int i = 0; i < childRenderers.size(); i++) {
                childRenderers.get(i).setParent(this);
                listItemNum = (childRenderers.get(i).<Integer>getProperty(Property.LIST_SYMBOL_ORDINAL_VALUE) != null) ? (int) childRenderers.get(i).<Integer>getProperty(Property.LIST_SYMBOL_ORDINAL_VALUE) : listItemNum;
                IRenderer currentSymbolRenderer = makeListSymbolRenderer(listItemNum, childRenderers.get(i));
                if (BaseDirection.RIGHT_TO_LEFT.equals(this.<BaseDirection>getProperty(Property.BASE_DIRECTION))) {
                    currentSymbolRenderer.setProperty(Property.BASE_DIRECTION, BaseDirection.RIGHT_TO_LEFT);
                }
                LayoutResult listSymbolLayoutResult = null;
                if (currentSymbolRenderer != null) {
                    ++listItemNum;
                    currentSymbolRenderer.setParent(childRenderers.get(i));
                    listSymbolLayoutResult = currentSymbolRenderer.layout(layoutContext);
                    currentSymbolRenderer.setParent(null);
                }
                childRenderers.get(i).setParent(null);
                boolean isForcedPlacement = Boolean.TRUE.equals(getPropertyAsBoolean(Property.FORCED_PLACEMENT));
                boolean listSymbolNotFit = listSymbolLayoutResult != null && listSymbolLayoutResult.getStatus() != LayoutResult.FULL;
                // TODO DEVSIX-1655: partially not fitting list symbol not shown at all, however this might be improved
                if (listSymbolNotFit && isForcedPlacement) {
                    currentSymbolRenderer = null;
                }
                symbolRenderers.add(currentSymbolRenderer);
                if (listSymbolNotFit && !isForcedPlacement) {
                    return new LayoutResult(LayoutResult.NOTHING, null, null, this, listSymbolLayoutResult.getCauseOfNothing());
                }
            }

            float maxSymbolWidth = 0;
            for (int i = 0; i < childRenderers.size(); i++) {
                IRenderer symbolRenderer = symbolRenderers.get(i);
                if (symbolRenderer != null) {
                    IRenderer listItemRenderer = childRenderers.get(i);
                    if ((ListSymbolPosition) getListItemOrListProperty(listItemRenderer, this, Property.LIST_SYMBOL_POSITION) != ListSymbolPosition.INSIDE) {
                        maxSymbolWidth = Math.max(maxSymbolWidth, symbolRenderer.getOccupiedArea().getBBox().getWidth());
                    }
                }
            }

            Float symbolIndent = this.getPropertyAsFloat(Property.LIST_SYMBOL_INDENT);
            listItemNum = 0;
            for (IRenderer childRenderer : childRenderers) {
                childRenderer.setParent(this);

                // Symbol indent's value should be summed with the margin's value
                boolean isRtl = BaseDirection.RIGHT_TO_LEFT ==
                        childRenderer.<BaseDirection>getProperty(Property.BASE_DIRECTION);
                int marginToSet = isRtl ? Property.MARGIN_RIGHT : Property.MARGIN_LEFT;
                childRenderer.deleteOwnProperty(marginToSet);
                UnitValue marginToSetUV =
                        childRenderer.<UnitValue>getProperty(marginToSet, UnitValue.createPointValue(0f));
                if (!marginToSetUV.isPointValue()) {
                    Logger logger = Logger.getLogger(ListRenderer.class.getName());
                    logger.log(Level.SEVERE,MessageFormatUtil.format(
                            IoLogMessageConstant.PROPERTY_IN_PERCENTS_NOT_SUPPORTED,
                            marginToSet));
                }
                float calculatedMargin = marginToSetUV.getValue();
                if ((ListSymbolPosition) getListItemOrListProperty(childRenderer, this, Property.LIST_SYMBOL_POSITION) == ListSymbolPosition.DEFAULT) {
                    calculatedMargin += maxSymbolWidth + (float) (symbolIndent != null ? symbolIndent : 0f);
                }
                childRenderer.setProperty(marginToSet, UnitValue.createPointValue(calculatedMargin));

                IRenderer symbolRenderer = symbolRenderers.get(listItemNum++);
                ((ListItemRenderer) childRenderer).addSymbolRenderer(symbolRenderer, maxSymbolWidth);
                if (symbolRenderer != null) {
                    LayoutTaggingHelper taggingHelper = this.<LayoutTaggingHelper>getProperty(Property.TAGGING_HELPER);
                    if (taggingHelper != null) {
                        if (symbolRenderer instanceof LineRenderer) {
                            taggingHelper.setRoleHint(symbolRenderer.getChildRenderers().get(1), StandardRoles.LBL);
                        } else {
                            taggingHelper.setRoleHint(symbolRenderer, StandardRoles.LBL);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static final class ConstantFontTextRenderer extends  TextRenderer {
        private String constantFontName;

        public ConstantFontTextRenderer(Text textElement, String font) {
            super(textElement);
            constantFontName = font;
        }

        @Override
        public void draw(DrawContext drawContext) {
            try {
                setProperty(Property.FONT, PdfFontFactory.createFont(constantFontName));
            } catch (IOException ignored) {
                // Do nothing
            }
            super.draw(drawContext);
        }
    }
}
