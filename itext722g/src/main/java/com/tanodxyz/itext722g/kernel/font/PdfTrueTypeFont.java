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
package com.tanodxyz.itext722g.kernel.font;

import com.tanodxyz.itext722g.io.font.FontEncoding;
import com.tanodxyz.itext722g.io.font.FontNames;
import com.tanodxyz.itext722g.io.font.FontProgramFactory;
import com.tanodxyz.itext722g.io.font.TrueTypeFont;
import com.tanodxyz.itext722g.io.font.Type1Font;
import com.tanodxyz.itext722g.io.font.constants.StandardFonts;
import com.tanodxyz.itext722g.io.font.otf.Glyph;
import com.tanodxyz.itext722g.kernel.exceptions.KernelExceptionMessageConstant;
import com.tanodxyz.itext722g.kernel.exceptions.PdfException;
import com.tanodxyz.itext722g.kernel.pdf.PdfDictionary;
import com.tanodxyz.itext722g.kernel.pdf.PdfName;
import com.tanodxyz.itext722g.kernel.pdf.PdfStream;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Note. For TrueType FontNames.getStyle() is the same to Subfamily(). So, we shouldn't add style to /BaseFont.
 */
public class PdfTrueTypeFont extends PdfSimpleFont<TrueTypeFont> {


    PdfTrueTypeFont(TrueTypeFont ttf, String encoding, boolean embedded) {
        super();
        setFontProgram(ttf);
        this.embedded = embedded;
        FontNames fontNames = ttf.getFontNames();
        if (embedded && !fontNames.allowEmbedding()) {
            throw new PdfException("{0} cannot be embedded due to licensing restrictions.")
                    .setMessageParams(fontNames.getFontName());
        }
        if ((encoding == null || encoding.length() == 0) && ttf.isFontSpecific()) {
            encoding = FontEncoding.FONT_SPECIFIC;
        }
        if (encoding != null && FontEncoding.FONT_SPECIFIC.toLowerCase().equals(encoding.toLowerCase())) {
            fontEncoding = FontEncoding.createFontSpecificEncoding();
        } else {
            fontEncoding = FontEncoding.createFontEncoding(encoding);
        }
    }

    PdfTrueTypeFont(PdfDictionary fontDictionary) {
        super(fontDictionary);
        newFont = false;
        subset = false;
        fontEncoding = DocFontEncoding.createDocFontEncoding(fontDictionary.get(PdfName.Encoding), toUnicode);

        PdfName baseFontName = fontDictionary.getAsName(PdfName.BaseFont);
        // Section 9.6.3 (ISO-32000-1): A TrueType font dictionary may contain the same entries as a Type 1 font
        // dictionary (see Table 111), with these differences...
        // Section 9.6.2.2. (ISO-32000-1) associate standard fonts with Type1 fonts but there does not
        // seem to be a strict requirement on the subtype
        // Cases when a font with /TrueType subtype has base font which is one of the Standard 14 fonts
        // does not seem to be forbidden and it's handled by many PDF tools, so we handle it here as well
        if (baseFontName != null && StandardFonts.isStandardFont(baseFontName.getValue())
                && !fontDictionary.containsKey(PdfName.FontDescriptor) && !fontDictionary.containsKey(PdfName.Widths)) {
            try {
                fontProgram = FontProgramFactory.createFont(baseFontName.getValue(), true);
            } catch (IOException e) {
                throw new PdfException(KernelExceptionMessageConstant.IO_EXCEPTION_WHILE_CREATING_FONT, e);
            }
        } else {
            fontProgram = DocTrueTypeFont.createFontProgram(fontDictionary, fontEncoding, toUnicode);
        }

        embedded = fontProgram instanceof IDocFontProgram && ((IDocFontProgram) fontProgram).getFontFile() != null;
    }

    @Override
    public Glyph getGlyph(int unicode) {
        if (fontEncoding.canEncode(unicode)) {
            Glyph glyph = getFontProgram().getGlyph(fontEncoding.getUnicodeDifference(unicode));
            if (glyph == null && (glyph = notdefGlyphs.get(unicode)) == null) {
                final Glyph notdef = getFontProgram().getGlyphByCode(0);
                if (notdef != null) {
                    glyph = new Glyph(notdef, unicode);
                    notdefGlyphs.put(unicode, glyph);
                }
            }
            return glyph;
        }
        return null;
    }

    @Override
    public boolean containsGlyph(int unicode) {
        if (fontEncoding.isFontSpecific()) {
            return fontProgram.getGlyphByCode(unicode) != null;
        } else {
            return fontEncoding.canEncode(unicode)
                    && getFontProgram().getGlyph(fontEncoding.getUnicodeDifference(unicode)) != null;
        }
    }

    @Override
    public void flush() {
        if (isFlushed()) {
            return;
        }
        ensureUnderlyingObjectHasIndirectReference();
        if (newFont) {
            PdfName subtype;
            String fontName;
            if (((TrueTypeFont) getFontProgram()).isCff()) {
                subtype = PdfName.Type1;
                fontName = fontProgram.getFontNames().getFontName();
            } else {
                subtype = PdfName.TrueType;
                fontName = updateSubsetPrefix(fontProgram.getFontNames().getFontName(), subset, embedded);
            }
            flushFontData(fontName, subtype);
        }
        super.flush();
    }

    @Override
    public boolean isBuiltWith(String fontProgram, String encoding) {
        // Now Identity-H is default for true type fonts. However, in case of Identity-H the method from
        // PdfType0Font would be triggered, hence we need to return false there.
        return null != encoding && !"".equals(encoding) && super.isBuiltWith(fontProgram, encoding);
    }

    @Override
    protected void addFontStream(PdfDictionary fontDescriptor) {
        if (embedded) {
            PdfName fontFileName;
            PdfStream fontStream;
            if (fontProgram instanceof IDocFontProgram) {
                fontFileName = ((IDocFontProgram) fontProgram).getFontFileName();
                fontStream = ((IDocFontProgram) fontProgram).getFontFile();
            } else if (((TrueTypeFont) getFontProgram()).isCff()) {
                fontFileName = PdfName.FontFile3;
                try {
                    byte[] fontStreamBytes = ((TrueTypeFont) getFontProgram()).getFontStreamBytes();
                    fontStream = getPdfFontStream(fontStreamBytes, new int[]{fontStreamBytes.length});
                    fontStream.put(PdfName.Subtype, new PdfName("Type1C"));
                } catch (PdfException e) {
                    Logger logger = Logger.getLogger(PdfTrueTypeFont.class.getName());
                    logger.log(Level.SEVERE,e.getMessage());
                    fontStream = null;
                }
            } else {
                fontFileName = PdfName.FontFile2;
                SortedSet<Integer> glyphs = new TreeSet<>();
                for (int k = 0; k < usedGlyphs.length; k++) {
                    if (usedGlyphs[k] != 0) {
                        int uni = fontEncoding.getUnicode(k);
                        Glyph glyph = uni > -1 ? fontProgram.getGlyph(uni) : fontProgram.getGlyphByCode(k);
                        if (glyph != null) {
                            glyphs.add(glyph.getCode());
                        }
                    }
                }
                ((TrueTypeFont) getFontProgram()).updateUsedGlyphs(glyphs, subset, subsetRanges);
                try {
                    byte[] fontStreamBytes;
                    //getDirectoryOffset() > 0 means ttc, which shall be subset anyway.
                    if (subset || ((TrueTypeFont) getFontProgram()).getDirectoryOffset() > 0) {
                        fontStreamBytes = ((TrueTypeFont) getFontProgram()).getSubset(glyphs, subset);
                    } else {
                        fontStreamBytes = ((TrueTypeFont) getFontProgram()).getFontStreamBytes();
                    }
                    fontStream = getPdfFontStream(fontStreamBytes, new int[]{fontStreamBytes.length});
                } catch (PdfException e) {
                    Logger logger = Logger.getLogger(PdfTrueTypeFont.class.getName());
                    logger.log(Level.SEVERE,e.getMessage());
                    fontStream = null;
                }
            }
            if (fontStream != null) {
                fontDescriptor.put(fontFileName, fontStream);
                if (fontStream.getIndirectReference() != null) {
                    fontStream.flush();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isBuiltInFont() {
        return fontProgram instanceof Type1Font && ((Type1Font) fontProgram).isBuiltInFont();
    }
}
