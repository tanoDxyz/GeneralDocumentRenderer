package com.tanodxyz.generaldocumentrenderer.pdfparsing;

import com.tanodxyz.itext722g.kernel.geom.LineSegment;
import com.tanodxyz.itext722g.kernel.geom.Matrix;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.geom.Vector;
import com.tanodxyz.itext722g.kernel.pdf.canvas.CanvasTag;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.EventType;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.data.ClippingPathInfo;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.data.IEventData;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.data.PathRenderInfo;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.listener.DefaultTextChunkLocationComparator;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.listener.ITextChunkLocation;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.listener.TextChunk;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.listener.TextChunkLocationBasedComparator;
import com.tanodxyz.itext722g.kernel.pdf.canvas.parser.listener.TextChunkLocationDefaultImp;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContentProcessor implements ITextExtractionStrategy {

    /**
     * set to true for debugging
     */
    private static boolean DUMP_STATE = false;
    /**
     * this field indicates single line text and it's pageRelativeBounds
     */
    public HashMap<String, ArrayList<TextChunk>> textAndBoundsArray = new HashMap<>(2000);
    public ArrayList<TextRenderInfo> allTextRenderInfos = new ArrayList<>(2000);
    public ArrayList<ImageRenderInfo> allImageRenderInfos = new ArrayList<>(2000);
    public ArrayList<PathRenderInfo> allRenderPathsInfos = new ArrayList<>(2000);
    public ArrayList<ClippingPathInfo> allClipPathInfos = new ArrayList<>(2000);

    private StringBuilder singleLineText = new StringBuilder();
    /**
     * a summary of all found text
     */
    private final List<TextChunk> locationalResult = new ArrayList<>();

    private final LocationTextExtractionStrategy.ITextChunkLocationStrategy tclStrat;

    private boolean useActualText = false;

    private boolean rightToLeftRunDirection = false;

    private TextRenderInfo lastTextRenderInfo;

    /**
     * Creates a new text extraction renderer.
     */
    public ContentProcessor() {
        this(new LocationTextExtractionStrategy.ITextChunkLocationStrategyImpl());
    }

    /**
     * Creates a new text extraction renderer, with a custom strategy for
     * creating new TextChunkLocation objects based on the input of the
     * TextRenderInfo.
     *
     * @param strat the custom strategy
     */
    public ContentProcessor(LocationTextExtractionStrategy.ITextChunkLocationStrategy strat) {
        tclStrat = strat;
    }

    /**
     * Changes the behavior of text extraction so that if the parameter is set to {@code true},
     * /ActualText marked content property will be used instead of raw decoded bytes.
     * Beware: the logic is not stable yet.
     *
     * @param useActualText true to use /ActualText, false otherwise
     * @return this object
     */
    public ContentProcessor setUseActualText(boolean useActualText) {
        this.useActualText = useActualText;
        return this;
    }

    /**
     * Sets if text flows from left to right or from right to left.
     * Call this method with <code>true</code> argument for extracting Arabic, Hebrew or other
     * text with right-to-left writing direction.
     *
     * @param rightToLeftRunDirection value specifying whether the direction should be right to left
     * @return this object
     */
    public ContentProcessor setRightToLeftRunDirection(boolean rightToLeftRunDirection) {
        this.rightToLeftRunDirection = rightToLeftRunDirection;
        return this;
    }

    /**
     * Gets the value of the property which determines if /ActualText will be used when extracting
     * the text
     *
     * @return true if /ActualText value is used, false otherwise
     */
    public boolean isUseActualText() {
        return useActualText;
    }

    @Override
    public void eventOccurred(IEventData data, EventType type) {
        if (type.equals(EventType.RENDER_TEXT)) {
            TextRenderInfo renderInfo = (TextRenderInfo) data;
            renderInfo.preserveGraphicsState();
            allTextRenderInfos.add(renderInfo);
            LineSegment segment = renderInfo.getBaseline();
            if (renderInfo.getRise() != 0) {
                // remove the rise from the baseline - we do this because the text from a super/subscript render operations should probably be considered as part of the baseline of the text the super/sub is relative to
                Matrix riseOffsetTransform = new Matrix(0, -renderInfo.getRise());
                segment = segment.transformBy(riseOffsetTransform);
            }

            if (useActualText) {
                CanvasTag lastTagWithActualText = lastTextRenderInfo != null
                        ? findLastTagWithActualText(lastTextRenderInfo.getCanvasTagHierarchy())
                        : null;
                if (lastTagWithActualText != null && lastTagWithActualText == findLastTagWithActualText(renderInfo.getCanvasTagHierarchy())) {
                    // Merge two text pieces, assume they will be in the same line
                    TextChunk lastTextChunk = locationalResult.get(locationalResult.size() - 1);
                    Vector mergedStart = new Vector(Math.min(lastTextChunk.getLocation().getStartLocation().get(0), segment.getStartPoint().get(0)),
                            Math.min(lastTextChunk.getLocation().getStartLocation().get(1), segment.getStartPoint().get(1)),
                            Math.min(lastTextChunk.getLocation().getStartLocation().get(2), segment.getStartPoint().get(2)));
                    Vector mergedEnd = new Vector(Math.max(lastTextChunk.getLocation().getEndLocation().get(0), segment.getEndPoint().get(0)),
                            Math.max(lastTextChunk.getLocation().getEndLocation().get(1), segment.getEndPoint().get(1)),
                            Math.max(lastTextChunk.getLocation().getEndLocation().get(2), segment.getEndPoint().get(2)));
                    TextChunk merged = new TextChunk(lastTextChunk.getText(), tclStrat.createLocation(renderInfo,
                            new LineSegment(mergedStart, mergedEnd)));
                    merged.renderInfo = renderInfo;
                    locationalResult.set(locationalResult.size() - 1, merged);
                } else {
                    String actualText = renderInfo.getActualText();
                    TextChunk tc = new TextChunk(actualText != null ? actualText : renderInfo.getText(),
                            tclStrat.createLocation(renderInfo, segment));
                    tc.renderInfo = renderInfo;
                    locationalResult.add(tc);
                }
            } else {
                TextChunk tc = new TextChunk(renderInfo.getText(), tclStrat.createLocation(renderInfo, segment));
                tc.renderInfo = renderInfo;
                locationalResult.add(tc);
            }

            lastTextRenderInfo = renderInfo;
        } else if (type.equals(EventType.RENDER_IMAGE)) {
            ImageRenderInfo imri = (ImageRenderInfo) data;
            imri.preserveGraphicsState();
            allImageRenderInfos.add(imri);
        } else if ((type.equals(EventType.RENDER_PATH))) {
            PathRenderInfo pri = (PathRenderInfo) data;
            pri.preserveGraphicsState();
            allRenderPathsInfos.add(pri);
        } else if (type.equals(EventType.CLIP_PATH_CHANGED)) {
            ClippingPathInfo cpi = (ClippingPathInfo) data;
            cpi.preserveGraphicsState();
            allClipPathInfos.add(cpi);
        }
    }

    @Override
    public Set<EventType> getSupportedEvents() {
        return null;
    }

    public void processContent() {
        getResultantText();
    }

    @Override
    public String getResultantText() {
        if (DUMP_STATE) dumpState();

        List<TextChunk> textChunks = new ArrayList<>(locationalResult);
        sortWithMarks(textChunks);
        StringBuilder sb = new StringBuilder();
        TextChunk lastChunk = null;
        singleLineText = new StringBuilder(20000);
        textAndBoundsArray = new HashMap<>(20000);
        ArrayList<TextChunk> singleLineTextChunks = new ArrayList<>(2000);
        for (TextChunk chunk : textChunks) {
            if (lastChunk == null) {
                sb.append(chunk.text);
                singleLineText.append(chunk.text);
                singleLineTextChunks.add(chunk);
            } else {
                if (chunk.sameLine(lastChunk)) {
                    // we only insert a blank space if the trailing character of the previous string wasn't a space, and the leading character of the current string isn't a space
                    if (isChunkAtWordBoundary(chunk, lastChunk) &&
                            !startsWithSpace(chunk.text) && !endsWithSpace(lastChunk.text)) {
                        sb.append(' ');
                        singleLineText.append(' ');
                    }
                    singleLineText.append(chunk.text);
                    singleLineTextChunks.add(chunk);
                    sb.append(chunk.text);
                } else {
                    sb.append('\n');
                    singleLineText.append('\n');
                    textAndBoundsArray.put(singleLineText.toString(), singleLineTextChunks);
                    singleLineText = new StringBuilder();
                    singleLineTextChunks = new ArrayList<>(2000);
                    singleLineText.append(chunk.text);
                    singleLineTextChunks.add(chunk);
                    sb.append(chunk.text);
                }
            }
            lastChunk = chunk;
        }

        if (!singleLineText.toString().isEmpty()) {
            textAndBoundsArray.put(singleLineText.toString(), singleLineTextChunks);
        }
        return sb.toString();
    }

    public static void printLocation(TextChunk chunk) {
        ITextChunkLocation location = chunk.location;
        System.out.println("Bako: TextRenderText = " + chunk.renderInfo.getText());
        System.out.println("Bako: llx = " + location.getStartLocation().get(0));
        System.out.println("Bako: lly = " + location.getStartLocation().get(1));
        System.out.println("Bako: urx = " + location.getEndLocation().get(0));
        System.out.println("Bako: ury = " + location.getEndLocation().get(1));
        System.out.println();
        System.out.println("Bako: left = " + chunk.renderInfo.rectangle().getLeft());
        System.out.println("Bako: top = " + chunk.renderInfo.rectangle().getTop());
        System.out.println("Bako: right = " + chunk.renderInfo.rectangle().getRight());
        System.out.println("Bako: bottom = " + chunk.renderInfo.rectangle().getBottom());
        System.out.println();
        System.out.println("Bako: setting manual coordinates ");
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle = rectangle.setBbox(location.getStartLocation().get(0), location.getStartLocation().get(1),
                location.getEndLocation().get(0), location.getEndLocation().get(1));
        System.out.println("Bako: left = " + rectangle.getLeft());
        System.out.println("Bako: top = " + rectangle.getTop());
        System.out.println("Bako: right = " + rectangle.getRight());
        System.out.println("Bako: bottom = " + rectangle.getBottom());
        System.out.println();
    }


    /**
     * Determines if a space character should be inserted between a previous chunk and the current chunk.
     * This method is exposed as a callback so subclasses can fine time the algorithm for determining whether a space should be inserted or not.
     * By default, this method will insert a space if the there is a gap of more than half the font space character widthSpec between the end of the
     * previous chunk and the beginning of the current chunk.  It will also indicate that a space is needed if the starting point of the new chunk
     * appears *before* the end of the previous chunk (i.e. overlapping text).
     *
     * @param chunk         the new chunk being evaluated
     * @param previousChunk the chunk that appeared immediately before the current chunk
     * @return true if the two chunks represent different words (i.e. should have a space between them).  False otherwise.
     */
    protected boolean isChunkAtWordBoundary(TextChunk chunk, TextChunk previousChunk) {
        return chunk.getLocation().isAtWordBoundary(previousChunk.getLocation());
    }

    /**
     * Checks if the string starts with a space character, false if the string is empty or starts with a non-space character.
     *
     * @param str the string to be checked
     * @return true if the string starts with a space character, false if the string is empty or starts with a non-space character
     */
    private boolean startsWithSpace(String str) {
        return str.length() != 0 && str.charAt(0) == ' ';
    }

    /**
     * Checks if the string ends with a space character, false if the string is empty or ends with a non-space character
     *
     * @param str the string to be checked
     * @return true if the string ends with a space character, false if the string is empty or ends with a non-space character
     */
    private boolean endsWithSpace(String str) {
        return str.length() != 0 && str.charAt(str.length() - 1) == ' ';
    }

    /**
     * Used for debugging only
     */
    private void dumpState() {
        for (TextChunk location : locationalResult) {
            location.printDiagnostics();
            System.out.println();
        }
    }

    private CanvasTag findLastTagWithActualText(List<CanvasTag> canvasTagHierarchy) {
        CanvasTag lastActualText = null;
        for (CanvasTag tag : canvasTagHierarchy) {
            if (tag.getActualText() != null) {
                lastActualText = tag;
                break;
            }
        }
        return lastActualText;
    }

    private void sortWithMarks(List<TextChunk> textChunks) {
        Map<TextChunk, LocationTextExtractionStrategy.TextChunkMarks> marks = new HashMap<>();
        List<TextChunk> toSort = new ArrayList<>();

        for (int markInd = 0; markInd < textChunks.size(); markInd++) {
            ITextChunkLocation location = textChunks.get(markInd).getLocation();
            if (location.getStartLocation().equals(location.getEndLocation())) {
                boolean foundBaseToAttachTo = false;
                for (int baseInd = 0; baseInd < textChunks.size(); baseInd++) {
                    if (markInd != baseInd) {
                        ITextChunkLocation baseLocation = textChunks.get(baseInd).getLocation();
                        if (!baseLocation.getStartLocation().equals(baseLocation.getEndLocation()) && TextChunkLocationDefaultImp.containsMark(baseLocation, location)) {
                            LocationTextExtractionStrategy.TextChunkMarks currentMarks = marks.get(textChunks.get(baseInd));
                            if (currentMarks == null) {
                                currentMarks = new LocationTextExtractionStrategy.TextChunkMarks();
                                marks.put(textChunks.get(baseInd), currentMarks);
                            }

                            if (markInd < baseInd) {
                                currentMarks.preceding.add(textChunks.get(markInd));
                            } else {
                                currentMarks.succeeding.add(textChunks.get(markInd));
                            }

                            foundBaseToAttachTo = true;
                            break;
                        }
                    }
                }

                if (!foundBaseToAttachTo) {
                    toSort.add(textChunks.get(markInd));
                }
            } else {
                toSort.add(textChunks.get(markInd));
            }
        }

        Collections.sort(toSort, new TextChunkLocationBasedComparator(new DefaultTextChunkLocationComparator(!rightToLeftRunDirection)));

        textChunks.clear();

        for (TextChunk current : toSort) {
            LocationTextExtractionStrategy.TextChunkMarks currentMarks = marks.get(current);
            if (currentMarks != null) {
                if (!rightToLeftRunDirection) {
                    for (int j = 0; j < currentMarks.preceding.size(); j++) {
                        textChunks.add(currentMarks.preceding.get(j));
                    }
                } else {
                    for (int j = currentMarks.succeeding.size() - 1; j >= 0; j--) {
                        textChunks.add(currentMarks.succeeding.get(j));
                    }
                }
            }
            textChunks.add(current);
            if (currentMarks != null) {
                if (!rightToLeftRunDirection) {
                    for (int j = 0; j < currentMarks.succeeding.size(); j++) {
                        textChunks.add(currentMarks.succeeding.get(j));
                    }
                } else {
                    for (int j = currentMarks.preceding.size() - 1; j >= 0; j--) {
                        textChunks.add(currentMarks.preceding.get(j));
                    }
                }
            }
        }
    }

    public List<TextRenderInfo> getResultantTextRenderInfos() {
        return allTextRenderInfos;
    }

    public interface ITextChunkLocationStrategy {
        ITextChunkLocation createLocation(TextRenderInfo renderInfo, LineSegment baseline);
    }

    private static class TextChunkMarks {
        List<TextChunk> preceding = new ArrayList<>();
        List<TextChunk> succeeding = new ArrayList<>();
    }

    public static final class ITextChunkLocationStrategyImpl
            implements LocationTextExtractionStrategy.ITextChunkLocationStrategy {

        @Override
        public ITextChunkLocation createLocation(TextRenderInfo renderInfo, LineSegment baseline) {
            return new TextChunkLocationDefaultImp(baseline.getStartPoint(), baseline.getEndPoint(),
                    renderInfo.getSingleSpaceWidth());
        }
    }


}
