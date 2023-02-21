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
package com.tanodxyz.itext722g.pdfa;

import com.tanodxyz.itext722g.io.logs.IoLogMessageConstant;
import com.tanodxyz.itext722g.kernel.font.PdfFont;
import com.tanodxyz.itext722g.kernel.pdf.DocumentProperties;
import com.tanodxyz.itext722g.kernel.pdf.IPdfPageFactory;
import com.tanodxyz.itext722g.kernel.pdf.IsoKey;
import com.tanodxyz.itext722g.kernel.pdf.PdfAConformanceLevel;
import com.tanodxyz.itext722g.kernel.pdf.PdfDictionary;
import com.tanodxyz.itext722g.kernel.pdf.PdfDocument;
import com.tanodxyz.itext722g.kernel.pdf.PdfName;
import com.tanodxyz.itext722g.kernel.pdf.PdfObject;
import com.tanodxyz.itext722g.kernel.pdf.PdfOutputIntent;
import com.tanodxyz.itext722g.kernel.pdf.PdfPage;
import com.tanodxyz.itext722g.kernel.pdf.PdfReader;
import com.tanodxyz.itext722g.kernel.pdf.PdfResources;
import com.tanodxyz.itext722g.kernel.pdf.PdfStream;
import com.tanodxyz.itext722g.kernel.pdf.PdfVersion;
import com.tanodxyz.itext722g.kernel.pdf.PdfWriter;
import com.tanodxyz.itext722g.kernel.pdf.PdfXrefTable;
import com.tanodxyz.itext722g.kernel.pdf.StampingProperties;
import com.tanodxyz.itext722g.kernel.pdf.canvas.CanvasGraphicsState;
import com.tanodxyz.itext722g.kernel.pdf.tagutils.TagStructureContext;
import com.tanodxyz.itext722g.kernel.xmp.XMPConst;
import com.tanodxyz.itext722g.kernel.xmp.XMPException;
import com.tanodxyz.itext722g.kernel.xmp.XMPMeta;
import com.tanodxyz.itext722g.kernel.xmp.XMPMetaFactory;
import com.tanodxyz.itext722g.kernel.xmp.XMPUtils;
import com.tanodxyz.itext722g.pdfa.checker.PdfA1Checker;
import com.tanodxyz.itext722g.pdfa.checker.PdfA2Checker;
import com.tanodxyz.itext722g.pdfa.checker.PdfA3Checker;
import com.tanodxyz.itext722g.pdfa.checker.PdfAChecker;
import com.tanodxyz.itext722g.pdfa.exceptions.PdfAConformanceException;
import com.tanodxyz.itext722g.pdfa.logs.PdfALogMessageConstant;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class extends {@link PdfDocument} and is in charge of creating files
 * that comply with the PDF/A standard.
 *
 * Client code is still responsible for making sure the file is actually PDF/A
 * compliant: multiple steps must be undertaken (depending on the
 * {@link PdfAConformanceLevel}) to ensure that the PDF/A standard is followed.
 *
 * This class will throw exceptions, mostly {@link PdfAConformanceException},
 * and thus refuse to output a PDF/A file if at any point the document does not
 * adhere to the PDF/A guidelines specified by the {@link PdfAConformanceLevel}.
 */
public class PdfADocument extends PdfDocument {


    private static IPdfPageFactory pdfAPageFactory = new PdfAPageFactory();

    protected PdfAChecker checker;

    private boolean alreadyLoggedThatObjectFlushingWasNotPerformed = false;

    private boolean alreadyLoggedThatPageFlushingWasNotPerformed = false;

    /**
     * Constructs a new PdfADocument for writing purposes, i.e. from scratch. A
     * PDF/A file has a conformance level, and must have an explicit output
     * intent.
     *
     * @param writer the {@link PdfWriter} object to write to
     * @param conformanceLevel the generation and strictness level of the PDF/A that must be followed.
     * @param outputIntent a {@link PdfOutputIntent}
     */
    public PdfADocument(PdfWriter writer, PdfAConformanceLevel conformanceLevel, PdfOutputIntent outputIntent) {
        this(writer, conformanceLevel, outputIntent, new DocumentProperties());
    }

    /**
     * Constructs a new PdfADocument for writing purposes, i.e. from scratch. A
     * PDF/A file has a conformance level, and must have an explicit output
     * intent.
     *
     * @param writer the {@link PdfWriter} object to write to
     * @param conformanceLevel the generation and strictness level of the PDF/A that must be followed.
     * @param outputIntent a {@link PdfOutputIntent}
     * @param properties a {@link DocumentProperties}
     */
    public PdfADocument(PdfWriter writer, PdfAConformanceLevel conformanceLevel, PdfOutputIntent outputIntent, DocumentProperties properties) {
        super(writer, properties);
        setChecker(conformanceLevel);
        addOutputIntent(outputIntent);
    }

    /**
     * Opens a PDF/A document in the stamping mode.
     *
     * @param reader PDF reader.
     * @param writer PDF writer.
     */
    public PdfADocument(PdfReader reader, PdfWriter writer) {
        this(reader, writer, new StampingProperties());
    }

    /**
     * Open a PDF/A document in stamping mode.
     *
     * @param reader PDF reader.
     * @param writer PDF writer.
     * @param properties properties of the stamping process
     */
    public PdfADocument(PdfReader reader, PdfWriter writer, StampingProperties properties) {
        super(reader, writer, properties);

        PdfAConformanceLevel conformanceLevel = reader.getPdfAConformanceLevel();
        if (conformanceLevel == null) {
            throw new PdfAConformanceException(PdfAConformanceException.DOCUMENT_TO_READ_FROM_SHALL_BE_A_PDFA_CONFORMANT_FILE_WITH_VALID_XMP_METADATA);
        }

        setChecker(conformanceLevel);
    }

    @Override
    public void checkIsoConformance(Object obj, IsoKey key) {
        checkIsoConformance(obj, key, null, null);
    }

    @Override
    public void checkIsoConformance(Object obj, IsoKey key, PdfResources resources, PdfStream contentStream) {
        CanvasGraphicsState gState;
        PdfDictionary currentColorSpaces = null;
        if (resources != null) {
            currentColorSpaces = resources.getPdfObject().getAsDictionary(PdfName.ColorSpace);
        }
        switch (key) {
            case CANVAS_STACK:
                checker.checkCanvasStack((char) obj);
                break;
            case PDF_OBJECT:
                checker.checkPdfObject((PdfObject) obj);
                break;
            case RENDERING_INTENT:
                checker.checkRenderingIntent((PdfName) obj);
                break;
            case INLINE_IMAGE:
                checker.checkInlineImage((PdfStream) obj, currentColorSpaces);
                break;
            case EXTENDED_GRAPHICS_STATE:
                gState = (CanvasGraphicsState) obj;
                checker.checkExtGState(gState, contentStream);
                break;
            case FILL_COLOR:
                gState = (CanvasGraphicsState) obj;
                checker.checkColor(gState.getFillColor(), currentColorSpaces, true, contentStream);
                break;
            case PAGE:
                checker.checkSinglePage((PdfPage) obj);
                break;
            case STROKE_COLOR:
                gState = (CanvasGraphicsState) obj;
                checker.checkColor(gState.getStrokeColor(), currentColorSpaces, false, contentStream);
                break;
            case TAG_STRUCTURE_ELEMENT:
                checker.checkTagStructureElement((PdfObject) obj);
                break;
            case FONT_GLYPHS:
                checker.checkFontGlyphs(((CanvasGraphicsState) obj).getFont(), contentStream);
                break;
            case XREF_TABLE:
                checker.checkXrefTable((PdfXrefTable) obj);
        }
    }

    /**
     * Gets the PdfAConformanceLevel set in the constructor or in the metadata
     * of the {@link PdfReader}.
     *
     * @return a {@link PdfAConformanceLevel}
     */
    public PdfAConformanceLevel getConformanceLevel() {
        return checker.getConformanceLevel();
    }

    void logThatPdfAPageFlushingWasNotPerformed() {
        if (!alreadyLoggedThatPageFlushingWasNotPerformed) {
            alreadyLoggedThatPageFlushingWasNotPerformed = true;
            // This log message will be printed once for one instance of the document.
            Logger.getLogger(PdfADocument.class.getName()).warning(PdfALogMessageConstant.PDFA_PAGE_FLUSHING_WAS_NOT_PERFORMED);
        }
    }

    @Override
    protected void addCustomMetadataExtensions(XMPMeta xmpMeta) {
        if (this.isTagged()) {
            try {
                if (xmpMeta.getPropertyInteger(XMPConst.NS_PDFUA_ID, XMPConst.PART) != null) {
                    XMPMeta taggedExtensionMeta = XMPMetaFactory.parseFromString(PdfAXMPUtil.PDF_UA_EXTENSION);
                    XMPUtils.appendProperties(taggedExtensionMeta, xmpMeta, true, false);
                }
            } catch (XMPException exc) {
                Logger logger = Logger.getLogger(PdfADocument.class.getName());
                logger.log(Level.SEVERE,IoLogMessageConstant.EXCEPTION_WHILE_UPDATING_XMPMETADATA, exc);
            }
        }
    }

    @Override
    protected void updateXmpMetadata() {
        try {
            XMPMeta xmpMeta = updateDefaultXmpMetadata();
            xmpMeta.setProperty(XMPConst.NS_PDFA_ID, XMPConst.PART, checker.getConformanceLevel().getPart());
            xmpMeta.setProperty(XMPConst.NS_PDFA_ID, XMPConst.CONFORMANCE, checker.getConformanceLevel().getConformance());
            addCustomMetadataExtensions(xmpMeta);
            setXmpMetadata(xmpMeta);
        } catch (XMPException e) {
            Logger logger = Logger.getLogger(PdfADocument.class.getName());
            logger.log(Level.SEVERE,IoLogMessageConstant.EXCEPTION_WHILE_UPDATING_XMPMETADATA, e);
        }
    }

    @Override
    protected void checkIsoConformance() {
        checker.checkDocument(catalog);
    }

    @Override
    protected void flushObject(PdfObject pdfObject, boolean canBeInObjStm) throws IOException {
        markObjectAsMustBeFlushed(pdfObject);
        if (isClosing || checker.objectIsChecked(pdfObject)) {
            super.flushObject(pdfObject, canBeInObjStm);
        } else if (!alreadyLoggedThatObjectFlushingWasNotPerformed) {
            alreadyLoggedThatObjectFlushingWasNotPerformed = true;
            // This log message will be printed once for one instance of the document.
            Logger.getLogger(PdfADocument.class.getName()).warning(PdfALogMessageConstant.PDFA_OBJECT_FLUSHING_WAS_NOT_PERFORMED);
        }
    }

    @Override
    protected void flushFonts() {
        for (PdfFont pdfFont : getDocumentFonts()) {
            checker.checkFont(pdfFont);
        }
        super.flushFonts();
    }

    protected void setChecker(PdfAConformanceLevel conformanceLevel) {
        switch (conformanceLevel.getPart()) {
            case "1":
                checker = new PdfA1Checker(conformanceLevel);
                break;
            case "2":
                checker = new PdfA2Checker(conformanceLevel);
                break;
            case "3":
                checker = new PdfA3Checker(conformanceLevel);
                break;
        }
    }

    protected void initTagStructureContext() {
        tagStructureContext = new TagStructureContext(this, getPdfVersionForPdfA(checker.getConformanceLevel()));
    }

    @Override
    protected IPdfPageFactory getPageFactory() {
        return pdfAPageFactory;
    }

    boolean isClosing() {
        return isClosing;
    }

    private static PdfVersion getPdfVersionForPdfA(PdfAConformanceLevel conformanceLevel) {
        PdfVersion version;
        switch (conformanceLevel.getPart()) {
            case "1":
                version = PdfVersion.PDF_1_4;
                break;
            case "2":
                version = PdfVersion.PDF_1_7;
                break;
            case "3":
                version = PdfVersion.PDF_1_7;
                break;
            default:
                version = PdfVersion.PDF_1_4;
                break;
        }
        return version;
    }
}
