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


import com.tanodxyz.itext722g.commons.actions.EventManager;
import com.tanodxyz.itext722g.commons.actions.sequence.AbstractIdentifiableElement;
import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.io.logs.IoLogMessageConstant;
import com.tanodxyz.itext722g.kernel.actions.events.LinkDocumentIdEvent;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.pdf.PdfDocument;
import com.tanodxyz.itext722g.layout.IPropertyContainer;
import com.tanodxyz.itext722g.layout.layout.LayoutArea;
import com.tanodxyz.itext722g.layout.layout.LayoutContext;
import com.tanodxyz.itext722g.layout.layout.LayoutPosition;
import com.tanodxyz.itext722g.layout.layout.LayoutResult;
import com.tanodxyz.itext722g.layout.layout.PositionedLayoutContext;
import com.tanodxyz.itext722g.layout.layout.RootLayoutArea;
import com.tanodxyz.itext722g.layout.logs.LayoutLogMessageConstant;
import com.tanodxyz.itext722g.layout.margincollapse.MarginsCollapseHandler;
import com.tanodxyz.itext722g.layout.margincollapse.MarginsCollapseInfo;
import com.tanodxyz.itext722g.layout.properties.ClearPropertyValue;
import com.tanodxyz.itext722g.layout.properties.Property;
import com.tanodxyz.itext722g.layout.tagging.LayoutTaggingHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public abstract class RootRenderer extends AbstractRenderer {

    protected boolean immediateFlush = true;
    protected RootLayoutArea currentArea;
    protected List<IRenderer> waitingDrawingElements = new ArrayList<>();
    List<Rectangle> floatRendererAreas;
    private IRenderer keepWithNextHangingRenderer;
    private LayoutResult keepWithNextHangingRendererLayoutResult;
    private MarginsCollapseHandler marginsCollapseHandler;
    private LayoutArea initialCurrentArea;
    private List<IRenderer> waitingNextPageRenderers = new ArrayList<>();
    private boolean floatOverflowedCompletely = false;

    public void addChild(IRenderer renderer) {
        LayoutTaggingHelper taggingHelper = this.<LayoutTaggingHelper>getProperty(Property.TAGGING_HELPER);
        if (taggingHelper != null) {
            LayoutTaggingHelper.addTreeHints(taggingHelper, renderer);
        }

        // Some positioned renderers might have been fetched from non-positioned child and added to this renderer,
        // so we use this generic mechanism of determining which renderers have been just added.
        int numberOfChildRenderers = childRenderers.size();
        int numberOfPositionedChildRenderers = positionedRenderers.size();
        super.addChild(renderer);
        List<IRenderer> addedRenderers = new ArrayList<>(1);
        List<IRenderer> addedPositionedRenderers = new ArrayList<>(1);
        while (childRenderers.size() > numberOfChildRenderers) {
            addedRenderers.add(childRenderers.get(numberOfChildRenderers));
            childRenderers.remove(numberOfChildRenderers);
        }
        while (positionedRenderers.size() > numberOfPositionedChildRenderers) {
            addedPositionedRenderers.add(positionedRenderers.get(numberOfPositionedChildRenderers));
            positionedRenderers.remove(numberOfPositionedChildRenderers);
        }

        boolean marginsCollapsingEnabled = Boolean.TRUE.equals(getPropertyAsBoolean(Property.COLLAPSING_MARGINS));
        if (currentArea == null) {
            updateCurrentAndInitialArea(null);
            if (marginsCollapsingEnabled) {
                marginsCollapseHandler = new MarginsCollapseHandler(this, null);
            }
        }

        // Static layout
        for (int i = 0; currentArea != null && i < addedRenderers.size(); i++) {
              RootRendererAreaStateHandler rootRendererStateHandler = new   RootRendererAreaStateHandler();

            renderer = addedRenderers.get(i);
            boolean rendererIsFloat = FloatingHelper.isRendererFloating(renderer);
            boolean clearanceOverflowsToNextPage = FloatingHelper.isClearanceApplied(waitingNextPageRenderers, renderer.<ClearPropertyValue>getProperty(Property.CLEAR));
            if (rendererIsFloat && (floatOverflowedCompletely || clearanceOverflowsToNextPage)) {
                waitingNextPageRenderers.add(renderer);
                floatOverflowedCompletely = true;
                continue;
            }

            processWaitingKeepWithNextElement(renderer);

            List<IRenderer> resultRenderers = new ArrayList<>();
            LayoutResult result = null;

            MarginsCollapseInfo childMarginsInfo = null;
            if (marginsCollapsingEnabled && currentArea != null && renderer != null) {
                childMarginsInfo = marginsCollapseHandler.startChildMarginsHandling(renderer, currentArea.getBBox());
            }
            while (clearanceOverflowsToNextPage || currentArea != null && renderer != null
                    && (result = renderer.setParent(this)
                    .layout(new LayoutContext(currentArea.clone(), childMarginsInfo, floatRendererAreas))).getStatus() != LayoutResult.FULL) {
                boolean currentAreaNeedsToBeUpdated = false;
                if (clearanceOverflowsToNextPage) {
                    result = new LayoutResult(LayoutResult.NOTHING, null, null, renderer);
                    currentAreaNeedsToBeUpdated = true;
                }
                if (result.getStatus() == LayoutResult.PARTIAL) {
                    if (rendererIsFloat) {
                        waitingNextPageRenderers.add(result.getOverflowRenderer());
                        break;
                    } else {
                        processRenderer(result.getSplitRenderer(), resultRenderers);
                        if (!rootRendererStateHandler.attemptGoForwardToStoredNextState(this)) {
                            currentAreaNeedsToBeUpdated = true;
                        }
                    }
                } else if (result.getStatus() == LayoutResult.NOTHING && !clearanceOverflowsToNextPage) {
                    if (result.getOverflowRenderer() instanceof ImageRenderer) {
                        float imgHeight = ((ImageRenderer) result.getOverflowRenderer()).getOccupiedArea().getBBox().getHeight();
                        if (!floatRendererAreas.isEmpty()
                                || currentArea.getBBox().getHeight() < imgHeight && !currentArea.isEmptyArea()) {
                            if (rendererIsFloat) {
                                waitingNextPageRenderers.add(result.getOverflowRenderer());
                                floatOverflowedCompletely = true;
                                break;
                            }
                            currentAreaNeedsToBeUpdated = true;
                        } else {
                            ((ImageRenderer) result.getOverflowRenderer()).autoScale(currentArea);
                            result.getOverflowRenderer().setProperty(Property.FORCED_PLACEMENT, true);
                            Logger logger = Logger.getLogger(RootRenderer.class.getName());
                            logger.warning(MessageFormatUtil.format(LayoutLogMessageConstant.ELEMENT_DOES_NOT_FIT_AREA, ""));
                        }
                    } else {
                        if (currentArea.isEmptyArea() && result.getAreaBreak() == null) {
                            boolean keepTogetherChanged = tryDisableKeepTogether(result,
                                    rendererIsFloat, rootRendererStateHandler);

                            boolean areKeepTogetherAndForcedPlacementBothNotChanged = !keepTogetherChanged;
                            if (areKeepTogetherAndForcedPlacementBothNotChanged) {
                                areKeepTogetherAndForcedPlacementBothNotChanged =
                                        !updateForcedPlacement(renderer, result.getOverflowRenderer());
                            }

                            if (areKeepTogetherAndForcedPlacementBothNotChanged) {
                                // FORCED_PLACEMENT was already set to the renderer and
                                // LogMessageConstant.ELEMENT_DOES_NOT_FIT_AREA message was logged.
                                // This else-clause should never be hit, otherwise there is a bug in FORCED_PLACEMENT implementation.
                                assert false;

                                // Still handling this case in order to avoid nasty infinite loops.
                                break;
                            }
                        } else {
                            rootRendererStateHandler.storePreviousState(this);
                            if (!rootRendererStateHandler.attemptGoForwardToStoredNextState(this)) {
                                if (rendererIsFloat) {
                                    waitingNextPageRenderers.add(result.getOverflowRenderer());
                                    floatOverflowedCompletely = true;
                                    break;
                                }
                                currentAreaNeedsToBeUpdated = true;
                            }
                        }
                    }
                }

                renderer = result.getOverflowRenderer();

                if (marginsCollapsingEnabled) {
                    marginsCollapseHandler.endChildMarginsHandling(currentArea.getBBox());
                }
                if (currentAreaNeedsToBeUpdated) {
                    updateCurrentAndInitialArea(result);
                }
                if (marginsCollapsingEnabled) {
                    marginsCollapseHandler = new MarginsCollapseHandler(this, null);
                    childMarginsInfo = marginsCollapseHandler.startChildMarginsHandling(renderer, currentArea.getBBox());
                }

                clearanceOverflowsToNextPage = clearanceOverflowsToNextPage
                        && FloatingHelper.isClearanceApplied(waitingNextPageRenderers, renderer.<ClearPropertyValue>getProperty(Property.CLEAR));
            }
            if (marginsCollapsingEnabled) {
                marginsCollapseHandler.endChildMarginsHandling(currentArea.getBBox());
            }

            if (null != result && null != result.getSplitRenderer()) {
                renderer = result.getSplitRenderer();
            }

            // Keep renderer until next element is added for future keep with next adjustments
            if (renderer != null && result != null) {
                if (Boolean.TRUE.equals(renderer.<Boolean>getProperty(Property.KEEP_WITH_NEXT))) {
                    if (Boolean.TRUE.equals(renderer.<Boolean>getProperty(Property.FORCED_PLACEMENT))) {
                        Logger logger = Logger.getLogger(RootRenderer.class.getName());
                        logger.warning(IoLogMessageConstant.ELEMENT_WAS_FORCE_PLACED_KEEP_WITH_NEXT_WILL_BE_IGNORED);
                        shrinkCurrentAreaAndProcessRenderer(renderer, resultRenderers, result);
                    } else {
                        keepWithNextHangingRenderer = renderer;
                        keepWithNextHangingRendererLayoutResult = result;
                    }
                } else if (result.getStatus() != LayoutResult.NOTHING) {
                    shrinkCurrentAreaAndProcessRenderer(renderer, resultRenderers, result);
                }
            }
        }

        for (int i = 0; i < addedPositionedRenderers.size(); i++) {
            positionedRenderers.add(addedPositionedRenderers.get(i));
            renderer = positionedRenderers.get(positionedRenderers.size() - 1);
            Integer positionedPageNumber = renderer.<Integer>getProperty(Property.PAGE_NUMBER);
            if (positionedPageNumber == null) {
                positionedPageNumber = currentArea.getPageNumber();
            }

            LayoutArea layoutArea;
            // For position=absolute, if none of the top, bottom, left, right properties are provided,
            // the content should be displayed in the flow of the current content, not overlapping it.
            // The behavior is just if it would be statically positioned except it does not affect other elements
            if (Integer.valueOf(LayoutPosition.ABSOLUTE).equals(renderer.<Integer>getProperty(Property.POSITION)) && AbstractRenderer.noAbsolutePositionInfo(renderer)) {
                layoutArea = new LayoutArea((int) positionedPageNumber, currentArea.getBBox().clone());
            } else {
                layoutArea = new LayoutArea((int) positionedPageNumber, initialCurrentArea.getBBox().clone());
            }
            Rectangle fullBbox = layoutArea.getBBox().clone();
            preparePositionedRendererAndAreaForLayout(renderer, fullBbox, layoutArea.getBBox());
            renderer.layout(new PositionedLayoutContext(new LayoutArea(layoutArea.getPageNumber(), fullBbox), layoutArea));

            if (immediateFlush) {
                flushSingleRenderer(renderer);
                positionedRenderers.remove(positionedRenderers.size() - 1);
            }
        }
    }

    /**
     * Draws (flushes) the content.
     *
     * @see #draw(  DrawContext)
     */
    public void flush() {
        for (IRenderer resultRenderer : childRenderers) {
            flushSingleRenderer(resultRenderer);
        }
        for (IRenderer resultRenderer : positionedRenderers) {
            flushSingleRenderer(resultRenderer);
        }
        childRenderers.clear();
        positionedRenderers.clear();
    }

    /**
     * This method correctly closes the {@link RootRenderer} instance.
     * There might be hanging elements, like in case of {@link Property#KEEP_WITH_NEXT} is set to true
     * and when no consequent element has been added. This method addresses such situations.
     */
    public void close() {
        addAllWaitingNextPageRenderers();
        if (keepWithNextHangingRenderer != null) {
            keepWithNextHangingRenderer.setProperty(Property.KEEP_WITH_NEXT, false);
            IRenderer rendererToBeAdded = keepWithNextHangingRenderer;
            keepWithNextHangingRenderer = null;
            addChild(rendererToBeAdded);
        }
        if (!immediateFlush) {
            flush();
        }
        flushWaitingDrawingElements(true);
        LayoutTaggingHelper taggingHelper = this.<LayoutTaggingHelper>getProperty(Property.TAGGING_HELPER);
        if (taggingHelper != null) {
            taggingHelper.releaseAllHints();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutResult layout(LayoutContext layoutContext) {
        throw new IllegalStateException("Layout is not supported for root renderers.");
    }

    public LayoutArea getCurrentArea() {
        if (currentArea == null) {
            updateCurrentAndInitialArea(null);
        }
        return currentArea;
    }

    protected abstract void flushSingleRenderer(IRenderer resultRenderer);

    protected abstract LayoutArea updateCurrentArea(LayoutResult overflowResult);

    protected void shrinkCurrentAreaAndProcessRenderer(IRenderer renderer, List<IRenderer> resultRenderers, LayoutResult result) {
        if (currentArea != null) {
            float resultRendererHeight = result.getOccupiedArea().getBBox().getHeight();
            currentArea.getBBox().setHeight(currentArea.getBBox().getHeight() - resultRendererHeight);
            if (currentArea.isEmptyArea() && (resultRendererHeight > 0 || FloatingHelper.isRendererFloating(renderer))) {
                currentArea.setEmptyArea(false);
            }
            processRenderer(renderer, resultRenderers);
        }

        if (!immediateFlush) {
            childRenderers.addAll(resultRenderers);
        }
    }

    protected void flushWaitingDrawingElements() {
        flushWaitingDrawingElements(true);
    }

    void flushWaitingDrawingElements(boolean force) {
        Set<IRenderer> flushedElements = new HashSet<>();
        for (int i = 0; i < waitingDrawingElements.size(); ++i) {
            IRenderer waitingDrawingElement = waitingDrawingElements.get(i);
            // TODO Remove checking occupied area to be not null when DEVSIX-1655 is resolved.
            if (force || (null != waitingDrawingElement.getOccupiedArea() && waitingDrawingElement.getOccupiedArea().getPageNumber() < currentArea.getPageNumber())) {
                flushSingleRenderer(waitingDrawingElement);
                flushedElements.add(waitingDrawingElement);
            } else if (null == waitingDrawingElement.getOccupiedArea()) {
                flushedElements.add(waitingDrawingElement);
            }
        }
        waitingDrawingElements.removeAll(flushedElements);
    }

    final void linkRenderToDocument(IRenderer renderer, PdfDocument pdfDocument) {
        if (renderer == null) {
            return;
        }
        final IPropertyContainer container = renderer.getModelElement();
        if (container instanceof AbstractIdentifiableElement) {
            EventManager.getInstance().onEvent(
                    new LinkDocumentIdEvent(pdfDocument, (AbstractIdentifiableElement) container)
            );
        }
        final List<IRenderer> children = renderer.getChildRenderers();
        if (children != null) {
            for (IRenderer child : children) {
                linkRenderToDocument(child, pdfDocument);
            }
        }
    }

    private void processRenderer(IRenderer renderer, List<IRenderer> resultRenderers) {
        alignChildHorizontally(renderer, currentArea.getBBox());
        if (immediateFlush) {
            flushSingleRenderer(renderer);
        } else {
            resultRenderers.add(renderer);
        }
    }

    private void processWaitingKeepWithNextElement(IRenderer renderer) {
        if (keepWithNextHangingRenderer != null) {
            LayoutArea rest = currentArea.clone();
            rest.getBBox().setHeight(rest.getBBox().getHeight() - keepWithNextHangingRendererLayoutResult.getOccupiedArea().getBBox().getHeight());
            boolean ableToProcessKeepWithNext = false;
            if (renderer.setParent(this).layout(new LayoutContext(rest)).getStatus() != LayoutResult.NOTHING) {
                // The area break will not be introduced and we are safe to place everything as is
                shrinkCurrentAreaAndProcessRenderer(keepWithNextHangingRenderer, new ArrayList<IRenderer>(), keepWithNextHangingRendererLayoutResult);
                ableToProcessKeepWithNext = true;
            } else {
                float originalElementHeight = keepWithNextHangingRendererLayoutResult.getOccupiedArea().getBBox().getHeight();
                List<Float> trySplitHeightPoints = new ArrayList<>();
                float delta = 35;
                for (int i = 1; i <= 5 && originalElementHeight - delta * i > originalElementHeight / 2; i++) {
                    trySplitHeightPoints.add(originalElementHeight - delta * i);
                }
                for (int i = 0; i < trySplitHeightPoints.size() && !ableToProcessKeepWithNext; i++) {
                    float curElementSplitHeight = trySplitHeightPoints.get(i);
                    RootLayoutArea firstElementSplitLayoutArea = (RootLayoutArea) currentArea.clone();
                    firstElementSplitLayoutArea.getBBox().setHeight(curElementSplitHeight).
                            moveUp(currentArea.getBBox().getHeight() - curElementSplitHeight);
                    LayoutResult firstElementSplitLayoutResult = keepWithNextHangingRenderer.setParent(this).layout(new LayoutContext(firstElementSplitLayoutArea.clone()));
                    if (firstElementSplitLayoutResult.getStatus() == LayoutResult.PARTIAL) {
                        RootLayoutArea storedArea = currentArea;
                        updateCurrentAndInitialArea(firstElementSplitLayoutResult);
                        LayoutResult firstElementOverflowLayoutResult = firstElementSplitLayoutResult.getOverflowRenderer().layout(new LayoutContext(currentArea.clone()));
                        if (firstElementOverflowLayoutResult.getStatus() == LayoutResult.FULL) {
                            LayoutArea secondElementLayoutArea = currentArea.clone();
                            secondElementLayoutArea.getBBox().setHeight(secondElementLayoutArea.getBBox().getHeight() - firstElementOverflowLayoutResult.getOccupiedArea().getBBox().getHeight());
                            LayoutResult secondElementLayoutResult = renderer.setParent(this).layout(new LayoutContext(secondElementLayoutArea));
                            if (secondElementLayoutResult.getStatus() != LayoutResult.NOTHING) {
                                ableToProcessKeepWithNext = true;

                                currentArea = firstElementSplitLayoutArea;
                                shrinkCurrentAreaAndProcessRenderer(firstElementSplitLayoutResult.getSplitRenderer(), new ArrayList<IRenderer>(), firstElementSplitLayoutResult);
                                updateCurrentAndInitialArea(firstElementSplitLayoutResult);
                                shrinkCurrentAreaAndProcessRenderer(firstElementSplitLayoutResult.getOverflowRenderer(), new ArrayList<IRenderer>(), firstElementOverflowLayoutResult);
                            }
                        }
                        if (!ableToProcessKeepWithNext) {
                            currentArea = storedArea;
                        }
                    }
                }
            }
            if (!ableToProcessKeepWithNext && !currentArea.isEmptyArea()) {
                RootLayoutArea storedArea = currentArea;
                updateCurrentAndInitialArea(null);
                LayoutResult firstElementLayoutResult = keepWithNextHangingRenderer.setParent(this).layout(new LayoutContext(currentArea.clone()));
                if (firstElementLayoutResult.getStatus() == LayoutResult.FULL) {
                    LayoutArea secondElementLayoutArea = currentArea.clone();
                    secondElementLayoutArea.getBBox().setHeight(secondElementLayoutArea.getBBox().getHeight() - firstElementLayoutResult.getOccupiedArea().getBBox().getHeight());
                    LayoutResult secondElementLayoutResult = renderer.setParent(this).layout(new LayoutContext(secondElementLayoutArea));
                    if (secondElementLayoutResult.getStatus() != LayoutResult.NOTHING) {
                        ableToProcessKeepWithNext = true;
                        shrinkCurrentAreaAndProcessRenderer(keepWithNextHangingRenderer, new ArrayList<IRenderer>(), keepWithNextHangingRendererLayoutResult);
                    }
                }
                if (!ableToProcessKeepWithNext) {
                    currentArea = storedArea;
                }
            }
            if (!ableToProcessKeepWithNext) {
                Logger logger = Logger.getLogger(RootRenderer.class.getName());
                logger.warning(IoLogMessageConstant.RENDERER_WAS_NOT_ABLE_TO_PROCESS_KEEP_WITH_NEXT);
                shrinkCurrentAreaAndProcessRenderer(keepWithNextHangingRenderer, new ArrayList<IRenderer>(), keepWithNextHangingRendererLayoutResult);
            }
            keepWithNextHangingRenderer = null;
            keepWithNextHangingRendererLayoutResult = null;
        }
    }

    private void updateCurrentAndInitialArea(LayoutResult overflowResult) {
        floatRendererAreas = new ArrayList<>();
        updateCurrentArea(overflowResult);
        initialCurrentArea = currentArea == null ? null : currentArea.clone();
        // TODO how bout currentArea == null ?
        addWaitingNextPageRenderers();
    }

    private void addAllWaitingNextPageRenderers() {
        boolean marginsCollapsingEnabled = Boolean.TRUE.equals(getPropertyAsBoolean(Property.COLLAPSING_MARGINS));
        while (!waitingNextPageRenderers.isEmpty()) {
            if (marginsCollapsingEnabled) {
                marginsCollapseHandler = new MarginsCollapseHandler(this, null);
            }
            updateCurrentAndInitialArea(null);
        }
    }

    private void addWaitingNextPageRenderers() {
        floatOverflowedCompletely = false;
        List<IRenderer> waitingFloatRenderers = new ArrayList<>(waitingNextPageRenderers);
        waitingNextPageRenderers.clear();
        for (IRenderer renderer : waitingFloatRenderers) {
            addChild(renderer);
        }
    }

    private boolean updateForcedPlacement(IRenderer currentRenderer, IRenderer overflowRenderer) {
        if (Boolean.TRUE.equals(currentRenderer.<Boolean>getProperty(Property.FORCED_PLACEMENT))) {
            return false;
        } else {
            overflowRenderer.setProperty(Property.FORCED_PLACEMENT, true);
            Logger logger = Logger.getLogger(RootRenderer.class.getName());

            logger.warning(MessageFormatUtil.format(LayoutLogMessageConstant.ELEMENT_DOES_NOT_FIT_AREA, ""));

            return true;
        }
    }

    private boolean tryDisableKeepTogether(LayoutResult result,
                                           boolean rendererIsFloat,   RootRendererAreaStateHandler rootRendererStateHandler) {
        IRenderer toDisableKeepTogether = null;

        // looking for the most outer keep together element
        IRenderer current = result.getCauseOfNothing();
        while (current != null) {
            if (Boolean.TRUE.equals(current.<Boolean>getProperty(Property.KEEP_TOGETHER))) {
                toDisableKeepTogether = current;
            }
            current = current.getParent();
        }

        if (toDisableKeepTogether == null) {
            return false;
        }

        // Ideally the disabling of keep together property should be done on the renderers layer,
        // but due to the problem with renderers tree (parent links from causeOfNothing
        // may not lead to overflowRenderer) such approach does not work now. So we
        // disabling keep together on the models layer.
        toDisableKeepTogether.getModelElement().setProperty(Property.KEEP_TOGETHER, false);
        Logger logger = Logger.getLogger(RootRenderer.class.getName());

        logger.warning(MessageFormatUtil.format(
                LayoutLogMessageConstant.ELEMENT_DOES_NOT_FIT_AREA,
                "KeepTogether property will be ignored."));

        if (!rendererIsFloat) {
            rootRendererStateHandler.attemptGoBackToStoredPreviousStateAndStoreNextState(this);
        }
        return true;
    }
}
