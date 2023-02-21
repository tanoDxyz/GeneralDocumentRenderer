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
package com.tanodxyz.itext722g.forms.fields;


import com.tanodxyz.itext722g.commons.utils.Base64;
import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.forms.PdfAcroForm;
import com.tanodxyz.itext722g.forms.exceptions.FormsExceptionMessageConstant;
import com.tanodxyz.itext722g.forms.fields.borders.FormBorderFactory;
import com.tanodxyz.itext722g.forms.util.DrawingUtil;
import com.tanodxyz.itext722g.io.font.FontProgram;
import com.tanodxyz.itext722g.io.font.PdfEncodings;
import com.tanodxyz.itext722g.io.font.constants.StandardFonts;
import com.tanodxyz.itext722g.io.image.ImageData;
import com.tanodxyz.itext722g.io.image.ImageDataFactory;
import com.tanodxyz.itext722g.io.logs.IoLogMessageConstant;
import com.tanodxyz.itext722g.io.source.OutputStream;
import com.tanodxyz.itext722g.io.source.PdfTokenizer;
import com.tanodxyz.itext722g.io.source.RandomAccessFileOrArray;
import com.tanodxyz.itext722g.io.source.RandomAccessSourceFactory;
import com.tanodxyz.itext722g.kernel.colors.Color;
import com.tanodxyz.itext722g.kernel.colors.ColorConstants;
import com.tanodxyz.itext722g.kernel.colors.DeviceCmyk;
import com.tanodxyz.itext722g.kernel.colors.DeviceGray;
import com.tanodxyz.itext722g.kernel.colors.DeviceRgb;
import com.tanodxyz.itext722g.kernel.exceptions.PdfException;
import com.tanodxyz.itext722g.kernel.font.PdfFont;
import com.tanodxyz.itext722g.kernel.font.PdfFontFactory;
import com.tanodxyz.itext722g.kernel.geom.Matrix;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.pdf.PdfAConformanceLevel;
import com.tanodxyz.itext722g.kernel.pdf.PdfArray;
import com.tanodxyz.itext722g.kernel.pdf.PdfDictionary;
import com.tanodxyz.itext722g.kernel.pdf.PdfDocument;
import com.tanodxyz.itext722g.kernel.pdf.PdfIndirectReference;
import com.tanodxyz.itext722g.kernel.pdf.PdfName;
import com.tanodxyz.itext722g.kernel.pdf.PdfNumber;
import com.tanodxyz.itext722g.kernel.pdf.PdfObject;
import com.tanodxyz.itext722g.kernel.pdf.PdfObjectWrapper;
import com.tanodxyz.itext722g.kernel.pdf.PdfOutputStream;
import com.tanodxyz.itext722g.kernel.pdf.PdfPage;
import com.tanodxyz.itext722g.kernel.pdf.PdfResources;
import com.tanodxyz.itext722g.kernel.pdf.PdfStream;
import com.tanodxyz.itext722g.kernel.pdf.PdfString;
import com.tanodxyz.itext722g.kernel.pdf.action.PdfAction;
import com.tanodxyz.itext722g.kernel.pdf.annot.PdfAnnotation;
import com.tanodxyz.itext722g.kernel.pdf.annot.PdfWidgetAnnotation;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvas;
import com.tanodxyz.itext722g.kernel.pdf.xobject.PdfFormXObject;
import com.tanodxyz.itext722g.kernel.pdf.xobject.PdfImageXObject;
import com.tanodxyz.itext722g.layout.Canvas;
import com.tanodxyz.itext722g.layout.Style;
import com.tanodxyz.itext722g.layout.borders.Border;
import com.tanodxyz.itext722g.layout.element.Div;
import com.tanodxyz.itext722g.layout.element.Paragraph;
import com.tanodxyz.itext722g.layout.element.Text;
import com.tanodxyz.itext722g.layout.layout.LayoutArea;
import com.tanodxyz.itext722g.layout.layout.LayoutContext;
import com.tanodxyz.itext722g.layout.layout.LayoutResult;
import com.tanodxyz.itext722g.layout.properties.BoxSizingPropertyValue;
import com.tanodxyz.itext722g.layout.properties.Leading;
import com.tanodxyz.itext722g.layout.properties.OverflowPropertyValue;
import com.tanodxyz.itext722g.layout.properties.Property;
import com.tanodxyz.itext722g.layout.properties.TextAlignment;
import com.tanodxyz.itext722g.layout.properties.TransparentColor;
import com.tanodxyz.itext722g.layout.properties.VerticalAlignment;
import com.tanodxyz.itext722g.layout.renderer.IRenderer;
import com.tanodxyz.itext722g.layout.renderer.MetaInfoContainer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a single field or field group in an {@link PdfAcroForm
 * AcroForm}.
 * <p>
 * To be able to be wrapped with this {@link PdfObjectWrapper} the {@link PdfObject}
 * must be indirect.
 */
public class PdfFormField extends PdfObjectWrapper<PdfDictionary> {

    /**
     * Flag that designates, if set, that the field can contain multiple lines
     * of text.
     */
    public static final int FF_MULTILINE = makeFieldFlag(13);

    /**
     * Flag that designates, if set, that the field's contents must be obfuscated.
     */
    public static final int FF_PASSWORD = makeFieldFlag(14);

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;

    /**
     * A field with the symbol check
     */
    public static final int TYPE_CHECK = 1;
    /**
     * A field with the symbol circle
     */
    public static final int TYPE_CIRCLE = 2;
    /**
     * A field with the symbol cross
     */
    public static final int TYPE_CROSS = 3;
    /**
     * A field with the symbol diamond
     */
    public static final int TYPE_DIAMOND = 4;
    /**
     * A field with the symbol square
     */
    public static final int TYPE_SQUARE = 5;
    /**
     * A field with the symbol star
     */
    public static final int TYPE_STAR = 6;

    public static final int HIDDEN = 1;
    public static final int VISIBLE_BUT_DOES_NOT_PRINT = 2;
    public static final int HIDDEN_BUT_PRINTABLE = 3;
    public static final int VISIBLE = 4;

    public static final int FF_READ_ONLY = makeFieldFlag(1);
    public static final int FF_REQUIRED = makeFieldFlag(2);
    public static final int FF_NO_EXPORT = makeFieldFlag(3);
    
    /**
     * Default padding X offset
     */
    static final float X_OFFSET = 2;

    /**
     * Size of text in form fields when font size is not explicitly set.
     */
    static final int DEFAULT_FONT_SIZE = 12;

    /**
     * Minimal size of text in form fields
     */
    static final int MIN_FONT_SIZE = 4;

    /**
     * Index of font value in default appearance element
     */
    static final int DA_FONT = 0;

    /**
     * Index of font size value in default appearance element
     */
    static final int DA_SIZE = 1;

    /**
     * Index of color value in default appearance element
     */
    static final int DA_COLOR = 2;

    private static final String[] CHECKBOX_TYPE_ZAPFDINGBATS_CODE = {"4", "l", "8", "u", "n", "H"};
    
    protected String text;
    protected ImageData img;
    protected PdfFont font;
    protected float fontSize = -1;
    protected Color color;
    protected int checkType;
    protected float borderWidth = 1;
    protected Color backgroundColor;
    protected Color borderColor;
    protected int rotation = 0;
    protected PdfFormXObject form;
    protected PdfAConformanceLevel pdfAConformanceLevel;

    /**
     * Creates a form field as a wrapper object around a {@link PdfDictionary}.
     * This {@link PdfDictionary} must be an indirect object.
     *
     * @param pdfObject the dictionary to be wrapped, must have an indirect reference.
     */
    public PdfFormField(PdfDictionary pdfObject) {
        super(pdfObject);
        ensureObjectIsAddedToDocument(pdfObject);
        setForbidRelease();
        retrieveStyles();
    }

    /**
     * Creates a minimal {@link PdfFormField}.
     *
     * @param pdfDocument The document
     */
    protected PdfFormField(PdfDocument pdfDocument) {
        this((PdfDictionary) new PdfDictionary().makeIndirect(pdfDocument));
        PdfName formType = getFormType();
        if (formType != null) {
            put(PdfName.FT, formType);
        }
    }

    /**
     * Creates a form field as a parent of a {@link PdfWidgetAnnotation}.
     *
     * @param widget      The widget which will be a kid of the {@link PdfFormField}
     * @param pdfDocument The document
     */
    protected PdfFormField(PdfWidgetAnnotation widget, PdfDocument pdfDocument) {
        this((PdfDictionary) new PdfDictionary().makeIndirect(pdfDocument));
        widget.makeIndirect(pdfDocument);
        addKid(widget);
        put(PdfName.FT, getFormType());
    }

    /**
     * Makes a field flag by bit position. Bit positions are numbered 1 to 32.
     * But position 0 corresponds to flag 1, position 3 corresponds to flag 4 etc.
     *
     * @param bitPosition bit position of a flag in range 1 to 32 from the pdf specification.
     * @return corresponding field flag.
     */
    public static int makeFieldFlag(int bitPosition) {
        return (1 << (bitPosition - 1));
    }

    /**
     * Creates an empty form field without a predefined set of layout or
     * behavior.
     *
     * @param doc the {@link PdfDocument} to create the field in
     * @return a new {@link PdfFormField}
     */
    public static PdfFormField createEmptyField(PdfDocument doc) {
        return createEmptyField(doc, null);
    }

    /**
     * Creates an empty form field without a predefined set of layout or
     * behavior.
     *
     * @param doc                  the {@link PdfDocument} to create the field in
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfFormField}
     */
    public static PdfFormField createEmptyField(PdfDocument doc, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfFormField field = new PdfFormField(doc);
        field.pdfAConformanceLevel = pdfAConformanceLevel;
        return field;
    }

    /**
     * Creates an empty {@link PdfButtonFormField button form field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc   the {@link PdfDocument} to create the button field in
     * @param rect  the location on the page for the button
     * @param flags an <code>int</code>, containing a set of binary behavioral
     *              flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *              flags you require.
     * @return a new {@link PdfButtonFormField}
     */
    public static PdfButtonFormField createButton(PdfDocument doc, Rectangle rect, int flags) {
        return createButton(doc, rect, flags, null);
    }

    /**
     * Creates an empty {@link PdfButtonFormField button form field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc                  the {@link PdfDocument} to create the button field in
     * @param rect                 the location on the page for the button
     * @param flags                an <code>int</code>, containing a set of binary behavioral
     *                             flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *                             flags you require.
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfButtonFormField}
     */
    public static PdfButtonFormField createButton(PdfDocument doc, Rectangle rect, int flags, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
        PdfButtonFormField field = new PdfButtonFormField(annot, doc);
        field.pdfAConformanceLevel = pdfAConformanceLevel;
        if (null != pdfAConformanceLevel) {
            annot.setFlag(PdfAnnotation.PRINT);
        }
        field.setFieldFlags(flags);
        return field;
    }

    /**
     * Creates an empty {@link PdfButtonFormField button form field} with custom
     * behavior and layout.
     *
     * @param doc   the {@link PdfDocument} to create the button field in
     * @param flags an <code>int</code>, containing a set of binary behavioral
     *              flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *              flags you require.
     * @return a new {@link PdfButtonFormField}
     */
    public static PdfButtonFormField createButton(PdfDocument doc, int flags) {
        return createButton(doc, flags, null);
    }

    /**
     * Creates an empty {@link PdfButtonFormField button form field} with custom
     * behavior and layout.
     *
     * @param doc                  the {@link PdfDocument} to create the button field in
     * @param flags                an <code>int</code>, containing a set of binary behavioral
     *                             flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *                             flags you require.
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfButtonFormField}
     */
    public static PdfButtonFormField createButton(PdfDocument doc, int flags, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfButtonFormField field = new PdfButtonFormField(doc);
        field.pdfAConformanceLevel = pdfAConformanceLevel;
        field.setFieldFlags(flags);
        return field;
    }


    /**
     * Creates an empty {@link PdfTextFormField text form field}.
     *
     * @param doc the {@link PdfDocument} to create the text field in
     * @return a new {@link PdfTextFormField}
     */
    public static PdfTextFormField createText(PdfDocument doc) {
        return createText(doc, (PdfAConformanceLevel) null);
    }

    /**
     * Creates an empty {@link PdfTextFormField text form field}.
     *
     * @param doc                  the {@link PdfDocument} to create the text field in
     * @param pdfAConformanceLevel the desired {@link PdfAConformanceLevel} of the field. Must match the conformance
     *                             level of the {@link PdfDocument} this field will eventually be added into
     * @return a new {@link PdfTextFormField}
     */
    public static PdfTextFormField createText(PdfDocument doc, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfTextFormField textFormField = new PdfTextFormField(doc);
        textFormField.pdfAConformanceLevel = pdfAConformanceLevel;
        return textFormField;
    }

    /**
     * Creates an empty {@link PdfTextFormField text form field}.
     *
     * @param doc  the {@link PdfDocument} to create the text field in
     * @param rect the location on the page for the text field
     * @return a new {@link PdfTextFormField}
     */
    public static PdfTextFormField createText(PdfDocument doc, Rectangle rect) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
        return new PdfTextFormField(annot, doc);
    }

    /**
     * Creates a named {@link PdfTextFormField text form field} with an initial
     * value, and the form's default font specified in
     * {@link PdfAcroForm#getDefaultResources}.
     *
     * @param doc  the {@link PdfDocument} to create the text field in
     * @param rect the location on the page for the text field
     * @param name the name of the form field
     * @return a new {@link PdfTextFormField}
     */
    public static  PdfTextFormField createText(PdfDocument doc, Rectangle rect, String name) {
        return createText(doc, rect, name, "");
    }

    /**
     * Creates a named {@link PdfTextFormField text form field} with an initial
     * value, and the form's default font specified in
     * {@link PdfAcroForm#getDefaultResources}.
     *
     * @param doc   the {@link PdfDocument} to create the text field in
     * @param rect  the location on the page for the text field
     * @param name  the name of the form field
     * @param value the initial value
     * @return a new {@link PdfTextFormField}
     */
    public static PdfTextFormField createText(PdfDocument doc, Rectangle rect, String name, String value) {
        return createText(doc, rect, name, value, null, -1);
    }

    /**
     * Creates a named {@link PdfTextFormField text form field} with an initial
     * value, with a specified font and font size.
     *
     * @param doc      the {@link PdfDocument} to create the text field in
     * @param rect     the location on the page for the text field
     * @param name     the name of the form field
     * @param value    the initial value
     * @param font     a {@link PdfFont}
     * @param fontSize the size of the font
     * @return a new {@link PdfTextFormField}
     */
    public static  PdfTextFormField createText(PdfDocument doc, Rectangle rect, String name, String value, PdfFont font, float fontSize) {
        return createText(doc, rect, name, value, font, fontSize, false);
    }

    /**
     * Creates a named {@link PdfTextFormField text form field} with an initial
     * value, with a specified font and font size.
     *
     * @param doc       the {@link PdfDocument} to create the text field in
     * @param rect      the location on the page for the text field
     * @param name      the name of the form field
     * @param value     the initial value
     * @param font      a {@link PdfFont}
     * @param fontSize  the size of the font
     * @param multiline true for multiline text field
     * @return a new {@link PdfTextFormField}
     */
    public static  PdfTextFormField createText(PdfDocument doc, Rectangle rect, String name, String value, PdfFont font, float fontSize, boolean multiline) {
        return createText(doc, rect, name, value, font, fontSize, multiline, null);
    }

    /**
     * Creates a named {@link PdfTextFormField text form field} with an initial
     * value, with a specified font and font size.
     *
     * @param doc                  the {@link PdfDocument} to create the text field in
     * @param rect                 the location on the page for the text field
     * @param name                 the name of the form field
     * @param value                the initial value
     * @param font                 a {@link PdfFont}
     * @param fontSize             the size of the font
     * @param multiline            true for multiline text field
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfTextFormField}
     */
    public static  PdfTextFormField createText(PdfDocument doc, Rectangle rect, String name, String value, PdfFont font, float fontSize, boolean multiline, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
         PdfTextFormField field = new  PdfTextFormField(annot, doc);

        field.pdfAConformanceLevel = pdfAConformanceLevel;
        if (null != pdfAConformanceLevel) {
            annot.setFlag(PdfAnnotation.PRINT);
        }

        ((PdfFormField) field).updateFontAndFontSize(font, fontSize);
        field.setMultiline(multiline);
        field.setFieldName(name);
        field.setValue(value);

        return field;
    }

    /**
     * Creates a named {@link PdfTextFormField multilined text form field} with an initial
     * value, with a specified font and font size.
     *
     * @param doc      the {@link PdfDocument} to create the text field in
     * @param rect     the location on the page for the text field
     * @param name     the name of the form field
     * @param value    the initial value
     * @param font     a {@link PdfFont}
     * @param fontSize the size of the font
     * @return a new {@link PdfTextFormField}
     */
    public static  PdfTextFormField createMultilineText(PdfDocument doc, Rectangle rect, String name, String value, PdfFont font, float fontSize) {
        return createText(doc, rect, name, value, font, fontSize, true);
    }

    /**
     * Creates a named {@link PdfTextFormField multiline text form field} with an initial
     * value, and the form's default font specified in
     * {@link PdfAcroForm#getDefaultResources}.
     *
     * @param doc   the {@link PdfDocument} to create the text field in
     * @param rect  the location on the page for the text field
     * @param name  the name of the form field
     * @param value the initial value
     * @return a new {@link PdfTextFormField}
     */
    public static  PdfTextFormField createMultilineText(PdfDocument doc, Rectangle rect, String name, String value) {
        return createText(doc, rect, name, value, null, -1, true);
    }

    /**
     * Creates an empty {@link PdfChoiceFormField choice form field}.
     *
     * @param doc   the {@link PdfDocument} to create the choice field in
     * @param flags an <code>int</code>, containing a set of binary behavioral
     *              flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *              flags you require.
     * @return a new {@link PdfChoiceFormField}
     */
    public static PdfChoiceFormField createChoice(PdfDocument doc, int flags) {
        return createChoice(doc, flags, null);
    }

    /**
     * Creates an empty {@link PdfChoiceFormField choice form field}.
     *
     * @param doc                  the {@link PdfDocument} to create the choice field in
     * @param flags                an <code>int</code>, containing a set of binary behavioral
     *                             flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *                             flags you require.
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfChoiceFormField}
     */
    public static PdfChoiceFormField createChoice(PdfDocument doc, int flags, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfChoiceFormField field = new PdfChoiceFormField(doc);
        field.pdfAConformanceLevel = pdfAConformanceLevel;
        field.setFieldFlags(flags);
        return field;
    }

    /**
     * Creates an empty {@link PdfChoiceFormField choice form field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc   the {@link PdfDocument} to create the choice field in
     * @param rect  the location on the page for the choice field
     * @param flags an <code>int</code>, containing a set of binary behavioral
     *              flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *              flags you require.
     * @return a new {@link PdfChoiceFormField}
     */
    public static PdfChoiceFormField createChoice(PdfDocument doc, Rectangle rect, int flags) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
        PdfChoiceFormField field = new PdfChoiceFormField(annot, doc);
        field.setFieldFlags(flags);
        return field;
    }

    /**
     * Creates a {@link PdfChoiceFormField choice form field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc     the {@link PdfDocument} to create the choice field in
     * @param rect    the location on the page for the choice field
     * @param name    the name of the form field
     * @param value   the initial value
     * @param options an array of {@link PdfString} objects that each represent
     *                the 'on' state of one of the choices.
     * @param flags   an <code>int</code>, containing a set of binary behavioral
     *                flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *                flags you require.
     * @return a new {@link PdfChoiceFormField}
     */
    public static PdfChoiceFormField createChoice(PdfDocument doc, Rectangle rect, String name, String value, PdfArray options, int flags) {
        return createChoice(doc, rect, name, value, null, -1, options, flags);
    }

    /**
     * Creates a {@link PdfChoiceFormField choice form field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc                  the {@link PdfDocument} to create the choice field in
     * @param rect                 the location on the page for the choice field
     * @param name                 the name of the form field
     * @param value                the initial value
     * @param options              an array of {@link PdfString} objects that each represent
     *                             the 'on' state of one of the choices.
     * @param flags                an <code>int</code>, containing a set of binary behavioral
     *                             flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *                             flags you require.
     * @param font                 the desired font to be used when displaying the text
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfChoiceFormField}
     */
    public static PdfChoiceFormField createChoice(PdfDocument doc, Rectangle rect, String name, String value, PdfArray options, int flags, PdfFont font, PdfAConformanceLevel pdfAConformanceLevel) {
        return createChoice(doc, rect, name, value, font, (float) DEFAULT_FONT_SIZE, options, flags, pdfAConformanceLevel);
    }

    /**
     * Creates a {@link PdfChoiceFormField choice form field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc      the {@link PdfDocument} to create the choice field in
     * @param rect     the location on the page for the choice field
     * @param name     the name of the form field
     * @param value    the initial value
     * @param font     a {@link PdfFont}
     * @param fontSize the size of the font
     * @param options  an array of {@link PdfString} objects that each represent
     *                 the 'on' state of one of the choices.
     * @param flags    an <code>int</code>, containing a set of binary behavioral
     *                 flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *                 flags you require.
     * @return a new {@link PdfChoiceFormField}
     */
    public static PdfChoiceFormField createChoice(PdfDocument doc, Rectangle rect, String name, String value, PdfFont font, float fontSize, PdfArray options, int flags) {
        return createChoice(doc, rect, name, value, font, fontSize, options, flags, null);
    }

    /**
     * Creates a {@link PdfChoiceFormField choice form field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc                  the {@link PdfDocument} to create the choice field in
     * @param rect                 the location on the page for the choice field
     * @param name                 the name of the form field
     * @param value                the initial value
     * @param font                 a {@link PdfFont}
     * @param fontSize             the size of the font
     * @param options              an array of {@link PdfString} objects that each represent
     *                             the 'on' state of one of the choices.
     * @param flags                an <code>int</code>, containing a set of binary behavioral
     *                             flags. Do binary <code>OR</code> on this <code>int</code> to set the
     *                             flags you require.
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfChoiceFormField}
     */
    public static PdfChoiceFormField createChoice(PdfDocument doc, Rectangle rect, String name, String value, PdfFont font, float fontSize, PdfArray options, int flags, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
        PdfFormField field = new PdfChoiceFormField(annot, doc);
        field.pdfAConformanceLevel = pdfAConformanceLevel;
        if (null != pdfAConformanceLevel) {
            annot.setFlag(PdfAnnotation.PRINT);
        }

        field.updateFontAndFontSize(font, fontSize);
        field.put(PdfName.Opt, options);
        field.setFieldFlags(flags);
        field.setFieldName(name);
        ((PdfChoiceFormField) field).setListSelected(new String[] {value}, false);
        if ((flags & PdfChoiceFormField.FF_COMBO) == 0) {
            value = optionsArrayToString(options);
        }

        PdfFormXObject xObject = new PdfFormXObject(new Rectangle(0, 0, rect.getWidth(), rect.getHeight()));
        field.drawChoiceAppearance(rect, field.fontSize, value, xObject, 0);
        annot.setNormalAppearance(xObject.getPdfObject());

        return (PdfChoiceFormField) field;
    }

    /**
     * Creates an empty {@link PdfSignatureFormField signature form field}.
     *
     * @param doc the {@link PdfDocument} to create the signature field in
     * @return a new {@link PdfSignatureFormField}
     */
    public static  PdfSignatureFormField createSignature(PdfDocument doc) {
        return createSignature(doc, (PdfAConformanceLevel) null);
    }

    /**
     * Creates an empty {@link PdfSignatureFormField signature form field}.
     *
     * @param doc                  the {@link PdfDocument} to create the signature field in
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfSignatureFormField}
     */
    public static  PdfSignatureFormField createSignature(PdfDocument doc, PdfAConformanceLevel pdfAConformanceLevel) {
         PdfSignatureFormField signatureFormField = new  PdfSignatureFormField(doc);
        signatureFormField.pdfAConformanceLevel = pdfAConformanceLevel;
        return signatureFormField;
    }

    /**
     * Creates an empty {@link PdfSignatureFormField signature form field}.
     *
     * @param doc  the {@link PdfDocument} to create the signature field in
     * @param rect the location on the page for the signature field
     * @return a new {@link PdfSignatureFormField}
     */
    public static  PdfSignatureFormField createSignature(PdfDocument doc, Rectangle rect) {
        return createSignature(doc, rect, null);
    }

    /**
     * Creates an empty {@link PdfSignatureFormField signature form field}.
     *
     * @param doc                  the {@link PdfDocument} to create the signature field in
     * @param rect                 the location on the page for the signature field
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfSignatureFormField}
     */
    public static  PdfSignatureFormField createSignature(PdfDocument doc, Rectangle rect, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
         PdfSignatureFormField signatureFormField = new  PdfSignatureFormField(annot, doc);
        signatureFormField.pdfAConformanceLevel = pdfAConformanceLevel;
        if (null != pdfAConformanceLevel) {
            annot.setFlag(PdfAnnotation.PRINT);
        }
        return signatureFormField;
    }

    /**
     * Creates a {@link PdfButtonFormField radio group form field}.
     *
     * @param doc   the {@link PdfDocument} to create the radio group in
     * @param name  the name of the form field
     * @param value the initial value
     * @return a new {@link PdfButtonFormField radio group}
     */
    public static PdfButtonFormField createRadioGroup(PdfDocument doc, String name, String value) {
        return createRadioGroup(doc, name, value, null);
    }

    /**
     * Creates a {@link PdfButtonFormField radio group form field}.
     *
     * @param doc                  the {@link PdfDocument} to create the radio group in
     * @param name                 the name of the form field
     * @param value                the initial value
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfButtonFormField radio group}
     */
    public static PdfButtonFormField createRadioGroup(PdfDocument doc, String name, String value, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfButtonFormField radio = createButton(doc, PdfButtonFormField.FF_RADIO);
        radio.setFieldName(name);
        radio.put(PdfName.V, new PdfName(value));
        radio.pdfAConformanceLevel = pdfAConformanceLevel;
        return radio;
    }

    /**
     * Creates a generic {@link PdfFormField} that is added to a radio group.
     *
     * @param doc        the {@link PdfDocument} to create the radio group in
     * @param rect       the location on the page for the field
     * @param radioGroup the radio button group that this field should belong to
     * @param value      the initial value
     * @return a new {@link PdfFormField}
     * @see #createRadioGroup(PdfDocument, java.lang.String, java.lang.String)
     */
    public static PdfFormField createRadioButton(PdfDocument doc, Rectangle rect, PdfButtonFormField radioGroup, String value) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
        PdfFormField radio = new PdfButtonFormField(annot, doc);

        String name = radioGroup.getValue().toString().substring(1);
        if (name.equals(value)) {
            annot.setAppearanceState(new PdfName(value));
        } else {
            annot.setAppearanceState(new PdfName("Off"));
        }
        radio.drawRadioAppearance(rect.getWidth(), rect.getHeight(), value);
        radioGroup.addKid(radio);
        return radio;
    }

    /**
     * Creates a generic {@link PdfFormField} that is added to a radio group.
     *
     * @param doc                  the {@link PdfDocument} to create the radio group in
     * @param rect                 the location on the page for the field
     * @param radioGroup           the radio button group that this field should belong to
     * @param value                the initial value
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfFormField}
     * @see #createRadioGroup(PdfDocument, java.lang.String, java.lang.String)
     */
    public static PdfFormField createRadioButton(PdfDocument doc, Rectangle rect, PdfButtonFormField radioGroup, String value, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
        PdfFormField radio = new PdfButtonFormField(annot, doc);
        radio.pdfAConformanceLevel = pdfAConformanceLevel;
        if (null != pdfAConformanceLevel) {
            annot.setFlag(PdfAnnotation.PRINT);
        }

        String name = radioGroup.getValue().toString().substring(1);
        if (name.equals(value)) {
            annot.setAppearanceState(new PdfName(value));
        } else {
            annot.setAppearanceState(new PdfName("Off"));
        }
        radio.drawRadioAppearance(rect.getWidth(), rect.getHeight(), value);

        radioGroup.addKid(radio);
        return radio;
    }

    /**
     * Creates a {@link PdfButtonFormField} as a push button without data.
     *
     * @param doc     the {@link PdfDocument} to create the radio group in
     * @param rect    the location on the page for the field
     * @param name    the name of the form field
     * @param caption the text to display on the button
     * @return a new {@link PdfButtonFormField}
     */
    public static PdfButtonFormField createPushButton(PdfDocument doc, Rectangle rect, String name, String caption) {
        PdfButtonFormField field;
        try {
            field = createPushButton(doc, rect, name, caption, PdfFontFactory.createFont(), (float) DEFAULT_FONT_SIZE);
        } catch (IOException e) {
            throw new PdfException(e);
        }
        return field;
    }

    /**
     * Creates a {@link PdfButtonFormField} as a push button without data, with
     * its caption in a custom font.
     *
     * @param doc      the {@link PdfDocument} to create the radio group in
     * @param rect     the location on the page for the field
     * @param name     the name of the form field
     * @param caption  the text to display on the button
     * @param font     a {@link PdfFont}
     * @param fontSize the size of the font
     * @return a new {@link PdfButtonFormField}
     */
    public static PdfButtonFormField createPushButton(PdfDocument doc, Rectangle rect, String name, String caption, PdfFont font, float fontSize) {
        return createPushButton(doc, rect, name, caption, font, fontSize, null);
    }

    /**
     * Creates a {@link PdfButtonFormField} as a push button without data, with
     * its caption in a custom font.
     *
     * @param doc                  the {@link PdfDocument} to create the radio group in
     * @param rect                 the location on the page for the field
     * @param name                 the name of the form field
     * @param caption              the text to display on the button
     * @param font                 a {@link PdfFont}
     * @param fontSize             the size of the font
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfButtonFormField}
     */
    public static PdfButtonFormField createPushButton(PdfDocument doc, Rectangle rect, String name, String caption, PdfFont font, float fontSize, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
        PdfButtonFormField field = new PdfButtonFormField(annot, doc);
        field.pdfAConformanceLevel = pdfAConformanceLevel;
        if (null != pdfAConformanceLevel) {
            annot.setFlag(PdfAnnotation.PRINT);
        }
        field.setPushButton(true);
        field.setFieldName(name);
        field.text = caption;
        ((PdfFormField) field).updateFontAndFontSize(font, fontSize);
        field.backgroundColor = ColorConstants.LIGHT_GRAY;

        PdfFormXObject xObject = field.drawPushButtonAppearance(rect.getWidth(), rect.getHeight(), caption, font, fontSize);
        annot.setNormalAppearance(xObject.getPdfObject());

        PdfDictionary mk = new PdfDictionary();
        mk.put(PdfName.CA, new PdfString(caption));
        mk.put(PdfName.BG, new PdfArray(field.backgroundColor.getColorValue()));
        annot.setAppearanceCharacteristics(mk);

        if (pdfAConformanceLevel != null) {
            createPushButtonAppearanceState(annot.getPdfObject());
        }

        return field;
    }

    /**
     * Creates a {@link PdfButtonFormField} as a checkbox.
     *
     * @param doc   the {@link PdfDocument} to create the radio group in
     * @param rect  the location on the page for the field
     * @param name  the name of the form field
     * @param value the initial value
     * @return a new {@link PdfButtonFormField checkbox}
     */
    public static PdfButtonFormField createCheckBox(PdfDocument doc, Rectangle rect, String name, String value) {
        return createCheckBox(doc, rect, name, value, TYPE_CROSS);
    }

    /**
     * Creates a {@link PdfButtonFormField} as a checkbox.
     *
     * @param doc       the {@link PdfDocument} to create the radio group in
     * @param rect      the location on the page for the field
     * @param name      the name of the form field
     * @param value     the initial value
     * @param checkType the type of checkbox graphic to use.
     * @return a new {@link PdfButtonFormField checkbox}
     */
    public static PdfButtonFormField createCheckBox(PdfDocument doc, Rectangle rect, String name, String value, int checkType) {
        return createCheckBox(doc, rect, name, value, checkType, null);
    }

    /**
     * Creates a {@link PdfButtonFormField} as a checkbox. Check symbol will fit rectangle.
     * You may set font and font size after creation.
     *
     * @param doc                  the {@link PdfDocument} to create the radio group in
     * @param rect                 the location on the page for the field
     * @param name                 the name of the form field
     * @param value                the initial value
     * @param checkType            the type of checkbox graphic to use.
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfButtonFormField checkbox}
     */
    public static PdfButtonFormField createCheckBox(PdfDocument doc, Rectangle rect, String name, String value, int checkType, PdfAConformanceLevel pdfAConformanceLevel) {
        PdfWidgetAnnotation annot = new PdfWidgetAnnotation(rect);
        PdfButtonFormField check = new PdfButtonFormField(annot, doc);
        check.pdfAConformanceLevel = pdfAConformanceLevel;
        check.setFontSize(0);
        check.setCheckType(checkType);
        check.setFieldName(name);
        check.put(PdfName.V, new PdfName(value));
        annot.setAppearanceState(new PdfName(value));

        if (pdfAConformanceLevel != null) {
            check.drawPdfA2CheckAppearance(rect.getWidth(), rect.getHeight(), "Off".equals(value) ? "Yes" : value, checkType);
            annot.setFlag(PdfAnnotation.PRINT);
        } else {
            check.drawCheckAppearance(rect.getWidth(), rect.getHeight(), "Off".equals(value) ? "Yes" : value);
        }

        return check;
    }

    /**
     * Creates a {@link PdfChoiceFormField combobox} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc     the {@link PdfDocument} to create the combobox in
     * @param rect    the location on the page for the combobox
     * @param name    the name of the form field
     * @param value   the initial value
     * @param options a two-dimensional array of Strings which will be converted
     *                to a PdfArray.
     * @return a new {@link PdfChoiceFormField} as a combobox
     */
    public static PdfChoiceFormField createComboBox(PdfDocument doc, Rectangle rect, String name, String value, String[][] options) {
        try {
            return createComboBox(doc, rect, name, value, options, PdfFontFactory.createFont(), null);
        } catch (IOException e) {
            throw new PdfException(e);
        }
    }

    /**
     * Creates a {@link PdfChoiceFormField combobox} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc                  the {@link PdfDocument} to create the combobox in
     * @param rect                 the location on the page for the combobox
     * @param name                 the name of the form field
     * @param value                the initial value
     * @param options              a two-dimensional array of Strings which will be converted
     *                             to a PdfArray.
     * @param font                 the desired font to be used when displaying the text
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfChoiceFormField} as a combobox
     */
    public static PdfChoiceFormField createComboBox(PdfDocument doc, Rectangle rect, String name, String value, String[][] options, PdfFont font, PdfAConformanceLevel pdfAConformanceLevel) {
        return createChoice(doc, rect, name, value, processOptions(options), PdfChoiceFormField.FF_COMBO, font, pdfAConformanceLevel);
    }

    /**
     * Creates a {@link PdfChoiceFormField combobox} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc     the {@link PdfDocument} to create the combobox in
     * @param rect    the location on the page for the combobox
     * @param name    the name of the form field
     * @param value   the initial value
     * @param options an array of Strings which will be converted to a PdfArray.
     * @return a new {@link PdfChoiceFormField} as a combobox
     */
    public static PdfChoiceFormField createComboBox(PdfDocument doc, Rectangle rect, String name, String value, String[] options) {
        return createComboBox(doc, rect, name, value, options, null, null);
    }

    /**
     * Creates a {@link PdfChoiceFormField combobox} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc                  the {@link PdfDocument} to create the combobox in
     * @param rect                 the location on the page for the combobox
     * @param name                 the name of the form field
     * @param value                the initial value
     * @param options              an array of Strings which will be converted to a PdfArray.
     * @param font                 the desired font to be used when displaying the text
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfChoiceFormField} as a combobox
     */
    public static PdfChoiceFormField createComboBox(PdfDocument doc, Rectangle rect, String name, String value, String[] options, PdfFont font, PdfAConformanceLevel pdfAConformanceLevel) {
        return createChoice(doc, rect, name, value, processOptions(options), PdfChoiceFormField.FF_COMBO, font, pdfAConformanceLevel);
    }

    /**
     * Creates a {@link PdfChoiceFormField list field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc     the {@link PdfDocument} to create the choice field in
     * @param rect    the location on the page for the choice field
     * @param name    the name of the form field
     * @param value   the initial value
     * @param options a two-dimensional array of Strings which will be converted
     *                to a PdfArray.
     * @return a new {@link PdfChoiceFormField} as a list field
     */
    public static PdfChoiceFormField createList(PdfDocument doc, Rectangle rect, String name, String value, String[][] options) {
        return createList(doc, rect, name, value, options, null, null);
    }

    /**
     * Creates a {@link PdfChoiceFormField list field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc                  the {@link PdfDocument} to create the choice field in
     * @param rect                 the location on the page for the choice field
     * @param name                 the name of the form field
     * @param value                the initial value
     * @param options              a two-dimensional array of Strings which will be converted
     *                             to a PdfArray.
     * @param font                 the desired font to be used when displaying the text
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfChoiceFormField} as a list field
     */
    public static PdfChoiceFormField createList(PdfDocument doc, Rectangle rect, String name, String value, String[][] options, PdfFont font, PdfAConformanceLevel pdfAConformanceLevel) {
        return createChoice(doc, rect, name, value, processOptions(options), 0, font, pdfAConformanceLevel);
    }

    /**
     * Creates a {@link PdfChoiceFormField list field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc     the {@link PdfDocument} to create the list field in
     * @param rect    the location on the page for the list field
     * @param name    the name of the form field
     * @param value   the initial value
     * @param options an array of Strings which will be converted to a PdfArray.
     * @return a new {@link PdfChoiceFormField} as a list field
     */
    public static PdfChoiceFormField createList(PdfDocument doc, Rectangle rect, String name, String value, String[] options) {
        return createList(doc, rect, name, value, options, null, null);
    }

    /**
     * Creates a {@link PdfChoiceFormField list field} with custom
     * behavior and layout, on a specified location.
     *
     * @param doc                  the {@link PdfDocument} to create the list field in
     * @param rect                 the location on the page for the list field
     * @param name                 the name of the form field
     * @param value                the initial value
     * @param options              an array of Strings which will be converted to a PdfArray.
     * @param font                 the desired font to be used when displaying the text
     * @param pdfAConformanceLevel the {@link PdfAConformanceLevel} of the document. {@code} null if it's no PDF/A document
     * @return a new {@link PdfChoiceFormField} as a list field
     */
    public static PdfChoiceFormField createList(PdfDocument doc, Rectangle rect, String name, String value, String[] options, PdfFont font, PdfAConformanceLevel pdfAConformanceLevel) {
        return createChoice(doc, rect, name, value, processOptions(options), 0, font, pdfAConformanceLevel);
    }

    /**
     * Creates a (subtype of) {@link PdfFormField} object. The type of the object
     * depends on the <code>FT</code> entry in the <code>pdfObject</code> parameter.
     *
     * @param pdfObject assumed to be either a {@link PdfDictionary}, or a
     *                  {@link PdfIndirectReference} to a {@link PdfDictionary}
     * @param document  the {@link PdfDocument} to create the field in
     * @return a new {@link PdfFormField}, or <code>null</code> if
     * <code>pdfObject</code> does not contain a <code>FT</code> entry
     */
    public static PdfFormField makeFormField(PdfObject pdfObject, PdfDocument document) {
        if (pdfObject.isDictionary()) {
            PdfFormField field;
            PdfDictionary dictionary = (PdfDictionary) pdfObject;
            PdfName formType = dictionary.getAsName(PdfName.FT);
            if (PdfName.Tx.equals(formType)) {
                field = new  PdfTextFormField(dictionary);
            } else if (PdfName.Btn.equals(formType)) {
                field = new PdfButtonFormField(dictionary);
            } else if (PdfName.Ch.equals(formType)) {
                field = new PdfChoiceFormField(dictionary);
            } else if (PdfName.Sig.equals(formType)) {
                field = new  PdfSignatureFormField(dictionary);
            } else {
                field = new PdfFormField(dictionary);
            }
            field.makeIndirect(document);

            if (document != null && document.getReader() != null && document.getReader().getPdfAConformanceLevel() != null) {
                field.pdfAConformanceLevel = document.getReader().getPdfAConformanceLevel();
            }
            return field;
        }

        return null;
    }

    /**
     * Returns the type of the parent form field, or of the wrapped
     * &lt;PdfDictionary&gt; object.
     *
     * @return the form type, as a {@link PdfName}
     */
    public PdfName getFormType() {
        PdfName formType = getPdfObject().getAsName(PdfName.FT);
        if (formType == null) {
            return getTypeFromParent(getPdfObject());
        }
        return formType;
    }

    /**
     * Sets a value to the field and generating field appearance if needed.
     *
     * @param value of the field
     * @return the field
     */
    public PdfFormField setValue(String value) {
        PdfName formType = getFormType();
        boolean autoGenerateAppearance = !(PdfName.Btn.equals(formType) && getFieldFlag(PdfButtonFormField.FF_RADIO));
        return setValue(value, autoGenerateAppearance);
    }

    /**
     * Sets a value to the field and generates field appearance if needed.
     *
     * @param value              of the field
     * @param generateAppearance if false, appearance won't be regenerated
     * @return the field
     */
    public PdfFormField setValue(String value, boolean generateAppearance) {
        PdfName formType = getFormType();
        if (formType == null || !PdfName.Btn.equals(formType)) {
            PdfArray kids = getKids();
            if (kids != null) {
                for (PdfObject kid: kids) {
                    if (kid.isDictionary() && ((PdfDictionary) kid).getAsString(PdfName.T) != null) {
                        PdfFormField field = new PdfFormField((PdfDictionary) kid);
                        field.setValue(value);
                        if (field.getDefaultAppearance() == null) {
                            field.font = this.font;
                            field.fontSize = this.fontSize;
                            field.color = this.color;
                        }
                    }
                }
            }
            if (PdfName.Ch.equals(formType)) {
                if (this instanceof PdfChoiceFormField) {
                    ((PdfChoiceFormField) this).setListSelected(new String[] {value}, false);
                } else {
                    PdfChoiceFormField choice = new PdfChoiceFormField(this.getPdfObject());
                    choice.setListSelected(new String[] {value}, false);
                }
            } else {
                put(PdfName.V, new PdfString(value, PdfEncodings.UNICODE_BIG));
            }
        } else if (PdfName.Btn.equals(formType)) {
            if (getFieldFlag(PdfButtonFormField.FF_PUSH_BUTTON)) {
                try {
                    img = ImageDataFactory.create(Base64.decode(value));
                } catch (Exception e) {
                    text = value;
                }
            } else {
                put(PdfName.V, new PdfName(value));
                for (PdfWidgetAnnotation widget : getWidgets()) {
                    List<String> states = Arrays
                            .asList(new PdfFormField(widget.getPdfObject()).getAppearanceStates());
                    if (states.contains(value)) {
                        widget.setAppearanceState(new PdfName(value));
                    } else {
                        widget.setAppearanceState(new PdfName("Off"));
                    }
                }
            }
        }

        if (generateAppearance) {
            regenerateField();
        }

        this.setModified();
        return this;
    }

    /**
     * Set text field value with given font and size
     *
     * @param value    text value
     * @param font     a {@link PdfFont}
     * @param fontSize the size of the font
     * @return the edited field
     */
    public PdfFormField setValue(String value, PdfFont font, float fontSize) {
        updateFontAndFontSize(font, fontSize);
        return setValue(value);
    }

    private void updateFontAndFontSize(PdfFont font, float fontSize) {
        if (font == null) {
            font = getDocument().getDefaultFont();
        }
        this.font = font;
        if (fontSize < 0) {
            fontSize = DEFAULT_FONT_SIZE;
        }
        this.fontSize = fontSize;
    }

    /**
     * Sets the field value and the display string. The display string
     * is used to build the appearance.
     *
     * @param value   the field value
     * @param display the string that is used for the appearance. If <CODE>null</CODE>
     *                the <CODE>value</CODE> parameter will be used
     * @return the edited field
     */
    public PdfFormField setValue(String value, String display) {
        if (display == null) {
            return setValue(value);
        }
        setValue(display, true);
        PdfName formType = getFormType();
        if (PdfName.Btn.equals(formType)) {
            if ((getFieldFlags() & PdfButtonFormField.FF_PUSH_BUTTON) != 0) {
                text = value;
            } else {
                put(PdfName.V, new PdfName(value));
            }
        } else {
            put(PdfName.V, new PdfString(value, PdfEncodings.UNICODE_BIG));
        }
        return this;
    }

    /**
     * Sets a parent {@link PdfFormField} for the current object.
     *
     * @param parent another form field that this field belongs to, usually a group field
     * @return the edited field
     */
    public PdfFormField setParent(PdfFormField parent) {
        return put(PdfName.Parent, parent.getPdfObject());
    }

    /**
     * Gets the parent dictionary.
     *
     * @return another form field that this field belongs to, usually a group field
     */
    public PdfDictionary getParent() {
        return getPdfObject().getAsDictionary(PdfName.Parent);
    }

    /**
     * Gets the kids of this object.
     *
     * @return contents of the dictionary's <code>Kids</code> property, as a {@link PdfArray}
     */
    public PdfArray getKids() {
        return getPdfObject().getAsArray(PdfName.Kids);
    }

    /**
     * Adds a new kid to the <code>Kids</code> array property from a
     * {@link PdfFormField}. Also sets the kid's <code>Parent</code> property to this object.
     *
     * @param kid a new {@link PdfFormField} entry for the field's <code>Kids</code> array property
     * @return the edited field
     */
    public PdfFormField addKid(PdfFormField kid) {
        kid.setParent(this);
        PdfArray kids = getKids();
        if (kids == null) {
            kids = new PdfArray();
        }
        kids.add(kid.getPdfObject());

        return put(PdfName.Kids, kids);
    }

    /**
     * Adds a new kid to the <code>Kids</code> array property from a
     * {@link PdfWidgetAnnotation}. Also sets the kid's <code>Parent</code> property to this object.
     *
     * @param kid a new {@link PdfWidgetAnnotation} entry for the field's <code>Kids</code> array property
     * @return the edited field
     */
    public PdfFormField addKid(PdfWidgetAnnotation kid) {
        kid.setParent(getPdfObject());
        PdfArray kids = getKids();
        if (kids == null) {
            kids = new PdfArray();
        }
        kids.add(kid.getPdfObject());
        return put(PdfName.Kids, kids);
    }

    /**
     * Changes the name of the field to the specified value.
     *
     * @param name the new field name, as a String
     * @return the edited field
     */
    public PdfFormField setFieldName(String name) {
        return put(PdfName.T, new PdfString(name));
    }

    /**
     * Gets the current field name.
     *
     * @return the current field name, as a {@link PdfString}
     */
    public PdfString getFieldName() {
        String parentName = "";
        PdfDictionary parent = getParent();
        if (parent != null) {
            PdfFormField parentField = PdfFormField.makeFormField(getParent(), getDocument());
            PdfString pName = parentField.getFieldName();
            if (pName != null) {
                parentName = pName.toUnicodeString() + ".";
            }
        }
        PdfString name = getPdfObject().getAsString(PdfName.T);
        if (name != null) {
            name = new PdfString(parentName + name.toUnicodeString(), PdfEncodings.UNICODE_BIG);
        }
        return name;
    }

    /**
     * Changes the alternate name of the field to the specified value. The
     * alternate is a descriptive name to be used by status messages etc.
     *
     * @param name the new alternate name, as a String
     * @return the edited field
     */
    public PdfFormField setAlternativeName(String name) {
        return put(PdfName.TU, new PdfString(name));
    }

    /**
     * Gets the current alternate name. The alternate is a descriptive name to
     * be used by status messages etc.
     *
     * @return the current alternate name, as a {@link PdfString}
     */
    public PdfString getAlternativeName() {
        return getPdfObject().getAsString(PdfName.TU);
    }

    /**
     * Changes the mapping name of the field to the specified value. The
     * mapping name can be used when exporting the form data in the document.
     *
     * @param name the new alternate name, as a String
     * @return the edited field
     */
    public PdfFormField setMappingName(String name) {
        return put(PdfName.TM, new PdfString(name));
    }

    /**
     * Gets the current mapping name. The mapping name can be used when
     * exporting the form data in the document.
     *
     * @return the current mapping name, as a {@link PdfString}
     */
    public PdfString getMappingName() {
        return getPdfObject().getAsString(PdfName.TM);
    }

    /**
     * Checks whether a certain flag, or any of a combination of flags, is set
     * for this form field.
     *
     * @param flag an <code>int</code> interpreted as a series of a binary flags
     * @return true if any of the flags specified in the parameter is also set
     * in the form field.
     */
    public boolean getFieldFlag(int flag) {
        return (getFieldFlags() & flag) != 0;
    }

    /**
     * Adds a flag, or combination of flags, for the form field. This method is
     * intended to be used one flag at a time, but this is not technically
     * enforced. To <em>replace</em> the current value, use
     * {@link #setFieldFlags(int)}.
     *
     * @param flag an <code>int</code> interpreted as a series of a binary flags
     * @return the edited field
     */
    public PdfFormField setFieldFlag(int flag) {
        return setFieldFlag(flag, true);
    }

    /**
     * Adds or removes a flag, or combination of flags, for the form field. This
     * method is intended to be used one flag at a time, but this is not
     * technically enforced. To <em>replace</em> the current value, use
     * {@link #setFieldFlags(int)}.
     *
     * @param flag  an <code>int</code> interpreted as a series of a binary flags
     * @param value if <code>true</code>, adds the flag(s). if <code>false</code>,
     *              removes the flag(s).
     * @return the edited field
     */
    public PdfFormField setFieldFlag(int flag, boolean value) {
        int flags = getFieldFlags();

        if (value) {
            flags |= flag;
        } else {
            flags &= ~flag;
        }

        return setFieldFlags(flags);
    }

    /**
     * If true, the field can contain multiple lines of text; if false, the field's text is restricted to a single line.
     *
     * @return whether the field can span over multiple lines.
     */
    public boolean isMultiline() {
        return getFieldFlag(FF_MULTILINE);
    }

    /**
     * If true, the field is intended for entering a secure password that should not be echoed visibly to the screen.
     * Characters typed from the keyboard should instead be echoed in some unreadable form, such as asterisks or bullet characters.
     *
     * @return whether or not the contents of the field must be obfuscated
     */
    public boolean isPassword() {
        return getFieldFlag(FF_PASSWORD);
    }

    /**
     * Sets a flag, or combination of flags, for the form field. This method
     * <em>replaces</em> the previous value. Compare with {@link #setFieldFlag(int)}
     * which <em>adds</em> a flag to the existing flags.
     *
     * @param flags an <code>int</code> interpreted as a series of a binary flags
     * @return the edited field
     */
    public PdfFormField setFieldFlags(int flags) {
        int oldFlags = getFieldFlags();
        put(PdfName.Ff, new PdfNumber(flags));
        if (((oldFlags ^ flags) &  PdfTextFormField.FF_COMB) != 0
                && PdfName.Tx.equals(getFormType()) && new  PdfTextFormField(getPdfObject()).getMaxLen() != 0)
            regenerateField();
        return this;
    }

    /**
     * Gets the current list of PDF form field flags.
     *
     * @return the current list of flags, encoded as an <code>int</code>
     */
    public int getFieldFlags() {
        PdfNumber f = getPdfObject().getAsNumber(PdfName.Ff);
        if (f != null) {
            return f.intValue();
        } else {
            PdfDictionary parent = getParent();
            if (parent != null) {
                return new PdfFormField(parent).getFieldFlags();
            } else {
                return 0;
            }
        }
    }

    /**
     * Gets the current value contained in the form field.
     *
     * @return the current value, as a {@link PdfObject}
     */
    public PdfObject getValue() {
        if(getPdfObject().get(PdfName.T) == null && getParent() != null) {
            return getParent().get(PdfName.V);
        }
        return getPdfObject().get(PdfName.V);
    }

    /**
     * Gets the current value contained in the form field.
     *
     * @return the current value, as a {@link String}
     */
    public String getValueAsString() {
        PdfObject value = getValue();
        if (value == null) {
            return "";
        } else if (value instanceof PdfStream) {
            return new String(((PdfStream) value).getBytes(), StandardCharsets.UTF_8);
        } else if (value instanceof PdfName) {
            return ((PdfName) value).getValue();
        } else if (value instanceof PdfString) {
            return ((PdfString) value).toUnicodeString();
        } else {
            return "";
        }
    }

    /**
     * Sets the default fallback value for the form field.
     *
     * @param value the default value
     * @return the edited field
     */
    public PdfFormField setDefaultValue(PdfObject value) {
        return put(PdfName.DV, value);
    }

    /**
     * Gets the default fallback value for the form field.
     *
     * @return the default value
     */
    public PdfObject getDefaultValue() {
        return getPdfObject().get(PdfName.DV);
    }

    /**
     * Sets an additional action for the form field.
     *
     * @param key    the dictionary key to use for storing the action
     * @param action the action
     * @return the edited field
     */
    public PdfFormField setAdditionalAction(PdfName key, PdfAction action) {
        PdfAction.setAdditionalAction(this, key, action);
        return this;
    }

    /**
     * Gets the currently additional action dictionary for the form field.
     *
     * @return the additional action dictionary
     */
    public PdfDictionary getAdditionalAction() {
        return getPdfObject().getAsDictionary(PdfName.AA);
    }

    /**
     * Sets options for the form field. Only to be used for checkboxes and radio buttons.
     *
     * @param options an array of {@link PdfString} objects that each represent
     *                the 'on' state of one of the choices.
     * @return the edited field
     */
    public PdfFormField setOptions(PdfArray options) {
        return put(PdfName.Opt, options);
    }

    /**
     * Gets options for the form field. Should only return usable values for
     * checkboxes and radio buttons.
     *
     * @return the options, as an {@link PdfArray} of {@link PdfString} objects
     */
    public PdfArray getOptions() {
        return getPdfObject().getAsArray(PdfName.Opt);
    }

    /**
     * Gets all {@link PdfWidgetAnnotation} that this form field and its
     * {@link #getKids() kids} refer to.
     *
     * @return a list of {@link PdfWidgetAnnotation}
     */
    public List<PdfWidgetAnnotation> getWidgets() {
        List<PdfWidgetAnnotation> widgets = new ArrayList<>();

        PdfName subType = getPdfObject().getAsName(PdfName.Subtype);
        if (subType != null && subType.equals(PdfName.Widget)) {
            widgets.add((PdfWidgetAnnotation) PdfAnnotation.makeAnnotation(getPdfObject()));
        }

        PdfArray kids = getKids();
        if (kids != null) {
            for (int i = 0; i < kids.size(); i++) {
                PdfObject kid = kids.get(i);
                subType = ((PdfDictionary) kid).getAsName(PdfName.Subtype);
                if (subType != null && subType.equals(PdfName.Widget)) {
                    widgets.add((PdfWidgetAnnotation) PdfAnnotation.makeAnnotation(kid));
                }
            }
        }

        return widgets;
    }

    /**
     * Gets default appearance string containing a sequence of valid page-content graphics or text state operators that
     * define such properties as the field's text size and color.
     *
     * @return the default appearance graphics, as a {@link PdfString}
     */
    public PdfString getDefaultAppearance() {
        PdfString defaultAppearance = getPdfObject().getAsString(PdfName.DA);
        if (defaultAppearance == null) {
            PdfDictionary parent = getParent();
            if (parent != null) {
                //If this is not merged form field we should get default appearance from the parent which actually is a
                //form field dictionary
                if (parent.containsKey(PdfName.FT)) {
                    defaultAppearance = parent.getAsString(PdfName.DA);
                }
            }
        }
        // DA is an inherited key, therefore AcroForm shall be checked if there is no parent or no DA in parent.
        if (defaultAppearance == null) {
            defaultAppearance = (PdfString) getAcroFormKey(PdfName.DA, PdfObject.STRING);
        }
        return defaultAppearance;
    }

    /**
     * Updates DA for Variable text, Push button and choice form fields.
     * The resources required for DA will be put to AcroForm's DR.
     * Note, for other form field types DA will be removed.
     */
    public void updateDefaultAppearance() {
        if (hasDefaultAppearance()) {
            assert this.font != null;

            PdfDictionary defaultResources = (PdfDictionary) getAcroFormObject(PdfName.DR, PdfObject.DICTIONARY);
            if (defaultResources == null) {
                // ensure that AcroForm dictionary exist.
                addAcroFormToCatalog();
                defaultResources = new PdfDictionary();
                putAcroFormObject(PdfName.DR, defaultResources);
            }
            PdfDictionary fontResources = defaultResources.getAsDictionary(PdfName.Font);
            if (fontResources == null) {
                fontResources = new PdfDictionary();
                defaultResources.put(PdfName.Font, fontResources);
            }
            PdfName fontName = getFontNameFromDR(fontResources, this.font.getPdfObject());
            if (fontName == null) {
                fontName = getUniqueFontNameForDR(fontResources);
                fontResources.put(fontName, this.font.getPdfObject());
                fontResources.setModified();
            }

            put(PdfName.DA, generateDefaultAppearance(fontName, fontSize, color));
            // Font from DR may not be added to document through PdfResource.
            getDocument().addFont(this.font);
        } else {
            getPdfObject().remove(PdfName.DA);
            setModified();
        }
    }

    /**
     * Gets a code specifying the form of quadding (justification) to be used in displaying the text:
     * 0 Left-justified
     * 1 Centered
     * 2 Right-justified
     *
     * @return the current justification attribute
     */
    public Integer getJustification() {
        Integer justification = getPdfObject().getAsInt(PdfName.Q);
        if (justification == null && getParent() != null) {
            justification = getParent().getAsInt(PdfName.Q);
        }
        return justification;
    }

    /**
     * Sets a code specifying the form of quadding (justification) to be used in displaying the text:
     * 0 Left-justified
     * 1 Centered
     * 2 Right-justified
     *
     * @param justification the value to set the justification attribute to
     * @return the edited field
     */
    public PdfFormField setJustification(int justification) {
        put(PdfName.Q, new PdfNumber(justification));
        regenerateField();
        return this;
    }

    /**
     * Gets a default style string, as described in "Rich Text Strings" section of Pdf spec.
     *
     * @return the default style, as a {@link PdfString}
     */
    public PdfString getDefaultStyle() {
        return getPdfObject().getAsString(PdfName.DS);
    }

    /**
     * Sets a default style string, as described in "Rich Text Strings" section of Pdf spec.
     *
     * @param defaultStyleString a new default style for the form field
     * @return the edited field
     */
    public PdfFormField setDefaultStyle(PdfString defaultStyleString) {
        put(PdfName.DS, defaultStyleString);
        return this;
    }

    /**
     * Gets a rich text string, as described in "Rich Text Strings" section of Pdf spec.
     * May be either {@link PdfStream} or {@link PdfString}.
     *
     * @return the current rich text value
     */
    public PdfObject getRichText() {
        return getPdfObject().get(PdfName.RV);
    }

    /**
     * Sets a rich text string, as described in "Rich Text Strings" section of Pdf spec.
     * May be either {@link PdfStream} or {@link PdfString}.
     *
     * @param richText a new rich text value
     * @return The edited PdfFormField
     */
    public PdfFormField setRichText(PdfObject richText) {
        put(PdfName.RV, richText);
        return this;
    }

    /**
     * Gets the current fontSize of the form field.
     *
     * @return the current fontSize
     */
    public float getFontSize() {
        return fontSize;
    }


    /**
     * Gets the current font of the form field.
     *
     * @return the current {@link PdfFont font}
     */
    public PdfFont getFont() {
        return font;
    }

    /**
     * Gets the current color of the form field.
     *
     * @return the current {@link Color color}
     */
    public Color getColor() {
        return color;
    }

    /**
     * Basic setter for the <code>font</code> property. Regenerates the field
     * appearance after setting the new value.
     * Note that the font will be added to the document so ensure that the font is embedded
     * if it's a pdf/a document.
     *
     * @param font The new font to be set
     * @return The edited PdfFormField
     */
    public PdfFormField setFont(PdfFont font) {
        updateFontAndFontSize(font, this.fontSize);
        regenerateField();
        return this;
    }

    /**
     * Basic setter for the <code>fontSize</code> property. Regenerates the
     * field appearance after setting the new value.
     *
     * @param fontSize The new font size to be set
     * @return The edited PdfFormField
     */
    public PdfFormField setFontSize(float fontSize) {
        updateFontAndFontSize(this.font, fontSize);
        regenerateField();
        return this;
    }

    /**
     * Basic setter for the <code>fontSize</code> property. Regenerates the
     * field appearance after setting the new value.
     *
     * @param fontSize The new font size to be set
     * @return The edited PdfFormField
     */
    public PdfFormField setFontSize(int fontSize) {
        setFontSize((float) fontSize);
        return this;
    }

    /**
     * Combined setter for the <code>font</code> and <code>fontSize</code>
     * properties. Regenerates the field appearance after setting the new value.
     *
     * @param font     The new font to be set
     * @param fontSize The new font size to be set
     * @return The edited PdfFormField
     */
    public PdfFormField setFontAndSize(PdfFont font, float fontSize) {
        updateFontAndFontSize(font, fontSize);
        regenerateField();
        return this;
    }

    /**
     * Basic setter for the <code>backgroundColor</code> property. Regenerates
     * the field appearance after setting the new value.
     *
     * @param backgroundColor The new color to be set or {@code null} if no background needed
     * @return The edited PdfFormField
     */
    public PdfFormField setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        PdfDictionary mk;
        List<PdfWidgetAnnotation> kids = getWidgets();
        for (PdfWidgetAnnotation kid : kids) {
            mk = kid.getAppearanceCharacteristics();
            if (mk == null) {
                mk = new PdfDictionary();
            }
            if (backgroundColor == null) {
                mk.remove(PdfName.BG);
            } else {
                mk.put(PdfName.BG, new PdfArray(backgroundColor.getColorValue()));
            }
            kid.setAppearanceCharacteristics(mk);
        }
        regenerateField();
        return this;
    }

    /**
     * Basic setter for the <code>degRotation</code> property. Regenerates
     * the field appearance after setting the new value.
     *
     * @param degRotation The new degRotation to be set
     * @return The edited PdfFormField
     */
    public PdfFormField setRotation(int degRotation) {
        if (degRotation % 90 != 0) {
            throw new IllegalArgumentException("degRotation.must.be.a.multiple.of.90");
        } else {
            degRotation %= 360;
            if (degRotation < 0) {
                degRotation += 360;
            }

            this.rotation = degRotation;
        }
        PdfDictionary mk = getWidgets().get(0).getAppearanceCharacteristics();
        if (mk == null) {
            mk = new PdfDictionary();
            this.put(PdfName.MK, mk);
        }
        mk.put(PdfName.R, new PdfNumber(degRotation));

        this.rotation = degRotation;
        regenerateField();
        return this;
    }

    /**
     * Sets the action on all {@link PdfWidgetAnnotation widgets} of this form field.
     *
     * @param action The action
     * @return The edited field
     */
    public PdfFormField setAction(PdfAction action) {
        List<PdfWidgetAnnotation> widgets = getWidgets();
        if (widgets != null) {
            for (PdfWidgetAnnotation widget : widgets) {
                widget.setAction(action);
            }
        }
        return this;
    }

    /**
     * Changes the type of graphical marker used to mark a checkbox as 'on'.
     * Notice that in order to complete the change one should call
     * {@link #regenerateField() regenerateField} method
     *
     * @param checkType the new checkbox marker
     * @return The edited field
     */
    public PdfFormField setCheckType(int checkType) {
        if (checkType < TYPE_CHECK || checkType > TYPE_STAR) {
            checkType = TYPE_CROSS;
        }
        this.checkType = checkType;
        text = CHECKBOX_TYPE_ZAPFDINGBATS_CODE[checkType - 1];
        if (pdfAConformanceLevel != null) {
            return this;
        }
        try {
            font = PdfFontFactory.createFont(StandardFonts.ZAPFDINGBATS);
        } catch (IOException e) {
            throw new PdfException(e);
        }
        return this;
    }

    /**
     * Set the visibility flags of the form field annotation
     * Options are: HIDDEN, HIDDEN_BUT_PRINTABLE, VISIBLE, VISIBLE_BUT_DOES_NOT_PRINT
     *
     * @param visibility visibility option
     * @return The edited field
     */
    public PdfFormField setVisibility(int visibility) {
        switch (visibility) {
            case HIDDEN:
                put(PdfName.F, new PdfNumber(PdfAnnotation.PRINT | PdfAnnotation.HIDDEN));
                break;
            case VISIBLE_BUT_DOES_NOT_PRINT:
                break;
            case HIDDEN_BUT_PRINTABLE:
                put(PdfName.F, new PdfNumber(PdfAnnotation.PRINT | PdfAnnotation.NO_VIEW));
                break;
            case VISIBLE:
            default:
                put(PdfName.F, new PdfNumber(PdfAnnotation.PRINT));
                break;
        }
        return this;
    }

    /**
     * This method regenerates appearance stream of the field. Use it if you
     * changed any field parameters and didn't use setValue method which
     * generates appearance by itself.
     *
     * @return whether or not the regeneration was successful.
     */
    public boolean regenerateField() {
        boolean result = true;
        updateDefaultAppearance();
        for (PdfWidgetAnnotation widget: getWidgets()) {
            PdfFormField field = new PdfFormField(widget.getPdfObject());
            copyParamsToKids(field);
            result &= field.regenerateWidget(this.getValueAsString());
        }
        return result;
    }



    /**
     * Gets the border width for the field.
     *
     * @return the current border width.
     */
    public float getBorderWidth() {
        PdfDictionary bs = getWidgets().get(0).getBorderStyle();
        if (bs != null) {
            PdfNumber w = bs.getAsNumber(PdfName.W);
            if (w != null) {
                borderWidth = w.floatValue();
            }
        }
        return borderWidth;
    }

    /**
     * Sets the border width for the field.
     *
     * @param borderWidth The new border width.
     * @return The edited field
     */
    public PdfFormField setBorderWidth(float borderWidth) {
        PdfDictionary bs = getWidgets().get(0).getBorderStyle();
        if (bs == null) {
            bs = new PdfDictionary();
            put(PdfName.BS, bs);
        }
        bs.put(PdfName.W, new PdfNumber(borderWidth));
        this.borderWidth = borderWidth;
        regenerateField();
        return this;
    }

    /**
     * Sets the border style for the field.
     *
     * @param style the new border style.
     * @return the edited field
     */
    public PdfFormField setBorderStyle(PdfDictionary style) {
        getWidgets().get(0).setBorderStyle(style);
        regenerateField();
        return this;
    }

    /**
     * Sets the Border Color.
     *
     * @param color the new value for the Border Color
     * @return the edited field
     */
    public PdfFormField setBorderColor(Color color) {
        borderColor = color;
        PdfDictionary mk;
        List<PdfWidgetAnnotation> kids = getWidgets();
        for (PdfWidgetAnnotation kid : kids) {
            mk = kid.getAppearanceCharacteristics();
            if (mk == null) {
                mk = new PdfDictionary();
            }
            if (borderColor == null) {
                mk.remove(PdfName.BC);
            } else {
                mk.put(PdfName.BC, new PdfArray(borderColor.getColorValue()));
            }
            kid.setAppearanceCharacteristics(mk);
        }
        regenerateField();
        return this;
    }

    /**
     * Sets the text color.
     *
     * @param color the new value for the Color
     * @return the edited field
     */
    public PdfFormField setColor(Color color) {
        this.color = color;
        regenerateField();
        return this;
    }

    /**
     * Sets the ReadOnly flag, specifying whether or not the field can be changed.
     *
     * @param readOnly if <code>true</code>, then the field cannot be changed.
     * @return the edited field
     */
    public PdfFormField setReadOnly(boolean readOnly) {
        return setFieldFlag(FF_READ_ONLY, readOnly);
    }

    /**
     * Gets the ReadOnly flag, specifying whether or not the field can be changed.
     *
     * @return <code>true</code> if the field cannot be changed.
     */
    public boolean isReadOnly() {
        return getFieldFlag(FF_READ_ONLY);
    }

    /**
     * Sets the Required flag, specifying whether or not the field must be filled in.
     *
     * @param required if <code>true</code>, then the field must be filled in.
     * @return the edited field
     */
    public PdfFormField setRequired(boolean required) {
        return setFieldFlag(FF_REQUIRED, required);
    }

    /**
     * Gets the Required flag, specifying whether or not the field must be filled in.
     *
     * @return <code>true</code> if the field must be filled in.
     */
    public boolean isRequired() {
        return getFieldFlag(FF_REQUIRED);
    }

    /**
     * Sets the NoExport flag, specifying whether or not exporting is forbidden.
     *
     * @param noExport if <code>true</code>, then exporting is <em>forbidden</em>
     * @return the edited field
     */
    public PdfFormField setNoExport(boolean noExport) {
        return setFieldFlag(FF_NO_EXPORT, noExport);
    }

    /**
     * Gets the NoExport attribute.
     *
     * @return whether exporting the value following a form action is forbidden.
     */
    public boolean isNoExport() {
        return getFieldFlag(FF_NO_EXPORT);
    }

    /**
     * Specifies on which page the form field's widget must be shown.
     *
     * @param pageNum the page number
     * @return the edited field
     */
    public PdfFormField setPage(int pageNum) {
        List<PdfWidgetAnnotation> widgets = getWidgets();
        if (widgets.size() > 0) {
            PdfAnnotation annot = widgets.get(0);
            if (annot != null) {
                annot.setPage(getDocument().getPage(pageNum));
            }
        }
        return this;
    }

    /**
     * Gets the appearance state names.
     *
     * @return an array of Strings containing the names of the appearance states
     */
    public String[] getAppearanceStates() {
        Set<String> names = new LinkedHashSet<>();
        PdfString stringOpt = getPdfObject().getAsString(PdfName.Opt);
        if (stringOpt != null) {
            names.add(stringOpt.toUnicodeString());
        } else {
            PdfArray arrayOpt = getPdfObject().getAsArray(PdfName.Opt);
            if (arrayOpt != null) {
                for (PdfObject pdfObject : arrayOpt) {
                    PdfString valStr = null;
                    if (pdfObject.isArray()) {
                        valStr = ((PdfArray) pdfObject).getAsString(1);
                    } else if (pdfObject.isString()) {
                        valStr = (PdfString) pdfObject;
                    }
                    if (valStr != null) {
                        names.add(valStr.toUnicodeString());
                    }
                }
            }
        }

        PdfDictionary dic = getPdfObject();
        dic = dic.getAsDictionary(PdfName.AP);
        if (dic != null) {
            dic = dic.getAsDictionary(PdfName.N);
            if (dic != null) {
                for (PdfName state : dic.keySet()) {
                    names.add(state.getValue());
                }
            }
        }

        PdfArray kids = getKids();
        if (kids != null) {
            for (PdfObject kid : kids) {
                PdfFormField fld = new PdfFormField((PdfDictionary) kid);
                String[] states = fld.getAppearanceStates();
                Collections.addAll(names, states);
            }
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * Sets an appearance for (the widgets related to) the form field.
     *
     * @param appearanceType   the type of appearance stream to be added
     *                         <ul>
     *                         <li> PdfName.N: normal appearance
     *                         <li> PdfName.R: rollover appearance
     *                         <li> PdfName.D: down appearance
     *                         </ul>
     * @param appearanceState  the state of the form field that needs to be true
     *                         for the appearance to be used. Differentiates between several streams
     *                         of the same type.
     * @param appearanceStream the appearance instructions, as a {@link PdfStream}
     * @return the edited field
     */
    public PdfFormField setAppearance(PdfName appearanceType, String appearanceState, PdfStream appearanceStream) {
        PdfWidgetAnnotation widget = getWidgets().get(0);
        PdfDictionary dic;
        if (widget != null) {
            dic = widget.getPdfObject();
        } else {
            dic = getPdfObject();
        }
        PdfDictionary ap = dic.getAsDictionary(PdfName.AP);
        if (ap != null) {
            PdfDictionary appearanceDictionary = ap.getAsDictionary(appearanceType);
            if (appearanceDictionary == null) {
                ap.put(appearanceType, appearanceStream);
            } else {
                appearanceDictionary.put(new PdfName(appearanceState), appearanceStream);
            }
        }

        return this;
    }

    /**
     * Sets zero font size which will be interpreted as auto-size according to ISO 32000-1, 12.7.3.3.
     *
     * @return the edited field
     */
    public PdfFormField setFontSizeAutoScale() {
        this.fontSize = 0;
        regenerateField();
        return this;
    }

    /**
     * Inserts the value into the {@link PdfDictionary} of this field and associates it with the specified key.
     * If the key is already present in this field dictionary,
     * this method will override the old value with the specified one.
     *
     * @param key  key to insert or to override
     * @param value the value to associate with the specified key
     * @return this {@link PdfFormField} instance
     */
    public PdfFormField put(PdfName key, PdfObject value) {
        getPdfObject().put(key, value);
        setModified();
        return this;
    }

    /**
     * Removes the specified key from the {@link PdfDictionary} of this field.
     *
     * @param key key to be removed
     * @return this {@link PdfFormField} instance
     */
    public PdfFormField remove(PdfName key) {
        getPdfObject().remove(key);
        setModified();
        return this;
    }

    /**
     * Releases underlying pdf object and other pdf entities used by wrapper.
     * This method should be called instead of direct call to {@link PdfObject#release()} if the wrapper is used.
     */
    public void release() {
        unsetForbidRelease();
        getPdfObject().release();
    }

    @Override
    protected boolean isWrappedObjectMustBeIndirect() {
        return true;
    }

    /**
     * Gets the {@link PdfDocument} that owns that form field.
     *
     * @return the {@link PdfDocument} that owns that form field.
     */
    protected PdfDocument getDocument() {
        return getPdfObject().getIndirectReference().getDocument();
    }

    /**
     * Gets a {@link Rectangle} that matches the current size and position of this form field.
     *
     * @param field current form field.
     * @return a {@link Rectangle} that matches the current size and position of this form field.
     */
    protected Rectangle getRect(PdfDictionary field) {
        PdfArray rect = field.getAsArray(PdfName.Rect);
        if (rect == null) {
            PdfArray kids = field.getAsArray(PdfName.Kids);
            if (kids == null) {
                throw new PdfException(FormsExceptionMessageConstant.WRONG_FORM_FIELD_ADD_ANNOTATION_TO_THE_FIELD);
            }
            rect = ((PdfDictionary) kids.get(0)).getAsArray(PdfName.Rect);
        }

        return rect != null ? rect.toRectangle() : null;
    }

    /**
     * Convert {@link String} multidimensional array of combo box or list options to {@link PdfArray}.
     *
     * @param options Two-dimensional array of options.
     * @return a {@link PdfArray} that contains all the options.
     */
    protected static PdfArray processOptions(String[][] options) {
        PdfArray array = new PdfArray();
        for (String[] option : options) {
            PdfArray subArray = new PdfArray(new PdfString(option[0], PdfEncodings.UNICODE_BIG));
            subArray.add(new PdfString(option[1], PdfEncodings.UNICODE_BIG));
            array.add(subArray);
        }
        return array;
    }

    /**
     * Convert {@link String} array of combo box or list options to {@link PdfArray}.
     *
     * @param options array of options.
     * @return a {@link PdfArray} that contains all the options.
     */
    protected static PdfArray processOptions(String[] options) {
        PdfArray array = new PdfArray();
        for (String option : options) {
            array.add(new PdfString(option, PdfEncodings.UNICODE_BIG));
        }
        return array;
    }

    protected static Object[] splitDAelements(String da) {
        PdfTokenizer tk = new PdfTokenizer(new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(PdfEncodings.convertToBytes(da, null))));
        List<String> stack = new ArrayList<>();
        Object[] ret = new Object[3];
        try {
            while (tk.nextToken()) {
                if (tk.getTokenType() == PdfTokenizer.TokenType.Comment)
                    continue;
                if (tk.getTokenType() == PdfTokenizer.TokenType.Other) {
                    switch (tk.getStringValue()) {
                        case "Tf":
                            if (stack.size() >= 2) {
                                ret[DA_FONT] = stack.get(stack.size() - 2);
                                ret[DA_SIZE] = new Float(stack.get(stack.size() - 1));
                            }
                            break;
                        case "g":
                            if (stack.size() >= 1) {
                                float gray = new Float(stack.get(stack.size() - 1));
                                if (gray != 0) {
                                    ret[DA_COLOR] = new DeviceGray(gray);
                                }
                            }
                            break;
                        case "rg":
                            if (stack.size() >= 3) {
                                float red = new Float(stack.get(stack.size() - 3));
                                float green = new Float(stack.get(stack.size() - 2));
                                float blue = new Float(stack.get(stack.size() - 1));
                                ret[DA_COLOR] = new DeviceRgb(red, green, blue);
                            }
                            break;
                        case "k":
                            if (stack.size() >= 4) {
                                float cyan = new Float(stack.get(stack.size() - 4));
                                float magenta = new Float(stack.get(stack.size() - 3));
                                float yellow = new Float(stack.get(stack.size() - 2));
                                float black = new Float(stack.get(stack.size() - 1));
                                ret[DA_COLOR] = new DeviceCmyk(cyan, magenta, yellow, black);
                            }
                            break;
                        default:
                            stack.clear();
                            break;
                    }
                } else {
                    stack.add(tk.getStringValue());
                }
            }
        } catch (Exception ignored) {

        }
        return ret;
    }

    /**
     * Draws the visual appearance of text in a form field.
     *
     * @param rect       The location on the page for the list field
     * @param font       a {@link PdfFont}
     * @param fontSize   The size of the font
     * @param value      The initial value
     * @param appearance The appearance
     */
    protected void drawTextAppearance(Rectangle rect, PdfFont font, float fontSize, String value, PdfFormXObject appearance) {
        PdfStream stream = (PdfStream) new PdfStream().makeIndirect(getDocument());
        PdfResources resources = appearance.getResources();
        PdfCanvas canvas = new PdfCanvas(stream, resources, getDocument());

        float height = rect.getHeight();
        float width = rect.getWidth();
        PdfFormXObject xObject = new PdfFormXObject(new Rectangle(0, 0, width, height));
        drawBorder(canvas, xObject, width, height);
        if (isPassword()) {
            value = obfuscatePassword(value);
        }

        canvas.
                beginVariableText().
                saveState().
                endPath();

        TextAlignment textAlignment = convertJustificationToTextAlignment();
        float x = 0;
        if (textAlignment == TextAlignment.RIGHT) {
            x = rect.getWidth();
        } else if (textAlignment == TextAlignment.CENTER) {
            x = rect.getWidth() / 2;
        }

        Canvas modelCanvas = new Canvas(canvas, new Rectangle(0, -height, 0, 2 * height));
        modelCanvas.setProperty(Property.APPEARANCE_STREAM_LAYOUT, true);

        setMetaInfoToCanvas(modelCanvas);

        Style paragraphStyle = new Style().setFont(font).setFontSize(fontSize);
        paragraphStyle.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1));
        if (color != null)
            paragraphStyle.setProperty(Property.FONT_COLOR, new TransparentColor(color));

        int maxLen = new  PdfTextFormField(getPdfObject()).getMaxLen();
        // check if /Comb has been set
        if (this.getFieldFlag( PdfTextFormField.FF_COMB) && 0 != maxLen) {
            float widthPerCharacter = width / maxLen;
            int numberOfCharacters = Math.min(maxLen, value.length());

            int start;
            switch (textAlignment) {
                case RIGHT:
                    start = (maxLen - numberOfCharacters);
                    break;
                case CENTER:
                    start = (maxLen - numberOfCharacters) / 2;
                    break;
                default:
                    start = 0;
            }
            float startOffset = widthPerCharacter * (start + 0.5f);
            for (int i = 0; i < numberOfCharacters; i++) {
                modelCanvas.showTextAligned(new Paragraph(value.substring(i, i + 1)).addStyle(paragraphStyle),
                        startOffset + widthPerCharacter * i, rect.getHeight() / 2, TextAlignment.CENTER, VerticalAlignment.MIDDLE);
            }
        } else {
            if (this.getFieldFlag( PdfTextFormField.FF_COMB)) {
                Logger logger = Logger.getLogger(PdfFormField.class.getName());
                logger.log(Level.SEVERE,MessageFormatUtil.format(IoLogMessageConstant.COMB_FLAG_MAY_BE_SET_ONLY_IF_MAXLEN_IS_PRESENT));
            }
            modelCanvas.showTextAligned(createParagraphForTextFieldValue(value).addStyle(paragraphStyle).setPaddings(0, X_OFFSET, 0, X_OFFSET),
                    x, rect.getHeight() / 2, textAlignment, VerticalAlignment.MIDDLE);
        }
        canvas.
                restoreState().
                endVariableText();

        appearance.getPdfObject().setData(stream.getBytes());
    }

    protected void drawMultiLineTextAppearance(Rectangle rect, PdfFont font, String value, PdfFormXObject appearance) {
        PdfStream stream = (PdfStream) new PdfStream().makeIndirect(getDocument());
        PdfResources resources = appearance.getResources();
        PdfCanvas canvas = new PdfCanvas(stream, resources, getDocument());

        float width = rect.getWidth();
        float height = rect.getHeight();

        drawBorder(canvas, appearance, width, height);
        canvas.beginVariableText();

        Rectangle areaRect = new Rectangle(0, 0, width, height);
        Canvas modelCanvas = new Canvas(canvas, areaRect);
        modelCanvas.setProperty(Property.APPEARANCE_STREAM_LAYOUT, true);

        setMetaInfoToCanvas(modelCanvas);

        Paragraph paragraph = createParagraphForTextFieldValue(value).setFont(font)
                .setMargin(0)
                .setPadding(3)
                .setMultipliedLeading(1);
        if (fontSize == 0) {
            paragraph.setFontSize(approximateFontSizeToFitMultiLine(paragraph, areaRect, modelCanvas.getRenderer()));
        } else {
            paragraph.setFontSize(fontSize);
        }
        paragraph.setProperty(Property.FORCED_PLACEMENT, true);
        paragraph.setTextAlignment(convertJustificationToTextAlignment());

        if (color != null) {
            paragraph.setFontColor(color);
        }
        // here we subtract an epsilon to make sure that element won't be split but overflown
        paragraph.setHeight(height - 0.00001f);
        paragraph.setProperty(Property.BOX_SIZING, BoxSizingPropertyValue.BORDER_BOX);
        paragraph.setProperty(Property.OVERFLOW_X, OverflowPropertyValue.FIT);
        paragraph.setProperty(Property.OVERFLOW_Y, OverflowPropertyValue.HIDDEN);
        modelCanvas.add(paragraph);
        canvas.endVariableText();

        appearance.getPdfObject().setData(stream.getBytes());
    }


    /**
     * Draws the visual appearance of Choice box in a form field.
     *
     * @param rect       The location on the page for the list field
     * @param value      The initial value
     * @param appearance The appearance
     */
    private void drawChoiceAppearance(Rectangle rect, float fontSize, String value, PdfFormXObject appearance, int topIndex) {
        PdfStream stream = (PdfStream) new PdfStream().makeIndirect(getDocument());
        PdfResources resources = appearance.getResources();
        PdfCanvas canvas = new PdfCanvas(stream, resources, getDocument());

        float width = rect.getWidth();
        float height = rect.getHeight();
        float widthBorder = 6.0f;
        float heightBorder = 2.0f;

        List<String> strings = font.splitString(value, fontSize, width - widthBorder);

        drawBorder(canvas, appearance, width, height);
        canvas.
                beginVariableText().
                saveState().
                rectangle(3, 3, width - widthBorder, height - heightBorder).
                clip().
                endPath();

        Canvas modelCanvas = new Canvas(canvas, new Rectangle(3, 0, Math.max(0, width - widthBorder), Math.max(0, height - heightBorder)));
        modelCanvas.setProperty(Property.APPEARANCE_STREAM_LAYOUT, true);

        setMetaInfoToCanvas(modelCanvas);

        Div div = new Div();
        if(getFieldFlag(PdfChoiceFormField.FF_COMBO)) {
            div.setVerticalAlignment(VerticalAlignment.MIDDLE);
        }
        div.setHeight(Math.max(0, height - heightBorder));
        for (int index = 0; index < strings.size(); index++) {
            Boolean isFull = modelCanvas.getRenderer().getPropertyAsBoolean(Property.FULL);
            if (Boolean.TRUE.equals(isFull)) {
                break;
            }

            Paragraph paragraph = new Paragraph(strings.get(index)).setFont(font).setFontSize(fontSize).setMargins(0, 0, 0, 0).setMultipliedLeading(1);
            paragraph.setProperty(Property.FORCED_PLACEMENT, true);
            paragraph.setTextAlignment(convertJustificationToTextAlignment());

            if (color != null) {
                paragraph.setFontColor(color);
            }
            if (!this.getFieldFlag(PdfChoiceFormField.FF_COMBO)) {
                PdfArray indices = getPdfObject().getAsArray(PdfName.I);
                if (indices == null && this.getKids() == null && this.getParent() != null) {
                    indices = this.getParent().getAsArray(PdfName.I);
                }
                if (indices != null && indices.size() > 0) {
                    for (PdfObject ind : indices) {
                        if (!ind.isNumber())
                            continue;
                        if (((PdfNumber) ind).getValue() == index + topIndex) {
                            paragraph.setBackgroundColor(new DeviceRgb(10, 36, 106));
                            paragraph.setFontColor(ColorConstants.LIGHT_GRAY);
                        }
                    }
                }
            }
            div.add(paragraph);
        }
        modelCanvas.add(div);
        canvas.
                restoreState().
                endVariableText();

        appearance.getPdfObject().setData(stream.getBytes());
    }

    /**
     * Draws a border using the borderWidth and borderColor of the form field.
     *
     * @param canvas  The {@link PdfCanvas} on which to draw
     * @param xObject The PdfFormXObject
     * @param width   The width of the rectangle to draw
     * @param height  The height of the rectangle to draw
     */
    protected void drawBorder(PdfCanvas canvas, PdfFormXObject xObject, float width, float height) {
        canvas.saveState();
        float borderWidth = getBorderWidth();
        PdfDictionary bs = getWidgets().get(0).getBorderStyle();
        if (borderWidth < 0) {
            borderWidth = 0;
        }

        if (backgroundColor != null) {
            canvas
                    .setFillColor(backgroundColor)
                    .rectangle(0, 0, width, height)
                    .fill();
        }

        if (borderWidth > 0 && borderColor != null) {
            borderWidth = Math.max(1, borderWidth);
            canvas
                    .setStrokeColor(borderColor)
                    .setLineWidth(borderWidth);
            Border border = FormBorderFactory.getBorder(bs, borderWidth, borderColor, backgroundColor);
            if (border != null) {
                float borderWidthX2 = borderWidth + borderWidth;
                border.draw(canvas, new Rectangle(borderWidth, borderWidth,
                        width - borderWidthX2, height - borderWidthX2));
            } else {
                canvas
                        .rectangle(0, 0, width, height)
                        .stroke();
            }
        }

        applyRotation(xObject, height, width);
        canvas.restoreState();
    }

    protected void drawRadioBorder(PdfCanvas canvas, PdfFormXObject xObject, float width, float height) {
        canvas.saveState();
        float borderWidth = getBorderWidth();
        float cx = width / 2;
        float cy = height / 2;
        if (borderWidth < 0) {
            borderWidth = 0;
        }

        float r = (Math.min(width, height) - borderWidth) / 2;

        if (backgroundColor != null) {
            canvas.
                    setFillColor(backgroundColor).
                    circle(cx, cy, r + borderWidth / 2).
                    fill();
        }

        if (borderWidth > 0 && borderColor != null) {
            borderWidth = Math.max(1, borderWidth);
            canvas.
                    setStrokeColor(borderColor).
                    setLineWidth(borderWidth).
                    circle(cx, cy, r).
                    stroke();
        }

        applyRotation(xObject, height, width);
        canvas.restoreState();
    }

    /**
     * Draws the appearance of a radio button with a specified value.
     *
     * @param width  the width of the radio button to draw
     * @param height the height of the radio button to draw
     * @param value  the value of the button
     */
    protected void drawRadioAppearance(float width, float height, String value) {
        Rectangle rect = new Rectangle(0, 0, width, height);
        PdfWidgetAnnotation widget = getWidgets().get(0);
        widget.setNormalAppearance(new PdfDictionary());

        //On state
        PdfFormXObject xObjectOn = new PdfFormXObject(rect);
        if (value != null) {
            PdfStream streamOn = (PdfStream) new PdfStream().makeIndirect(getDocument());
            PdfCanvas canvasOn = new PdfCanvas(streamOn, new PdfResources(), getDocument());

            drawRadioBorder(canvasOn, xObjectOn, width, height);
            drawRadioField(canvasOn, width, height, true);

            xObjectOn.getPdfObject().getOutputStream().writeBytes(streamOn.getBytes());
            widget.getNormalAppearanceObject().put(new PdfName(value), xObjectOn.getPdfObject());
        }

        //Off state
        PdfStream streamOff = (PdfStream) new PdfStream().makeIndirect(getDocument());
        PdfCanvas canvasOff = new PdfCanvas(streamOff, new PdfResources(), getDocument());
        PdfFormXObject xObjectOff = new PdfFormXObject(rect);

        drawRadioBorder(canvasOff, xObjectOff, width, height);

        xObjectOff.getPdfObject().getOutputStream().writeBytes(streamOff.getBytes());
        widget.getNormalAppearanceObject().put(new PdfName("Off"), xObjectOff.getPdfObject());

        if (pdfAConformanceLevel != null
                && ("2".equals(pdfAConformanceLevel.getPart()) || "3".equals(pdfAConformanceLevel.getPart()))) {
            xObjectOn.getResources();
            xObjectOff.getResources();
        }
    }

    /**
     * Draws a radio button.
     *
     * @param canvas the {@link PdfCanvas} on which to draw
     * @param width  the width of the radio button to draw
     * @param height the height of the radio button to draw
     * @param on     required to be <code>true</code> for fulfilling the drawing operation
     */
    protected void drawRadioField(PdfCanvas canvas, float width, float height, boolean on) {
        canvas.saveState();
        if (on) {
            canvas.resetFillColorRgb();
            DrawingUtil.drawCircle(canvas, width / 2, height / 2, Math.min(width, height) / 4);
        }
        canvas.restoreState();
    }

    /**
     * Draws the appearance of a checkbox with a specified state value.
     *
     * @param width       the width of the checkbox to draw
     * @param height      the height of the checkbox to draw
     * @param onStateName the state of the form field that will be drawn
     */
    protected void drawCheckAppearance(float width, float height, String onStateName) {
        Rectangle rect = new Rectangle(0, 0, width, height);

        PdfStream streamOn = (PdfStream) new PdfStream().makeIndirect(getDocument());
        PdfCanvas canvasOn = new PdfCanvas(streamOn, new PdfResources(), getDocument());
        PdfFormXObject xObjectOn = new PdfFormXObject(rect);
        drawBorder(canvasOn, xObjectOn, width, height);
        drawCheckBox(canvasOn, width, height, fontSize);
        xObjectOn.getPdfObject().getOutputStream().writeBytes(streamOn.getBytes());
        xObjectOn.getResources().addFont(getDocument(), getFont());


        PdfStream streamOff = (PdfStream) new PdfStream().makeIndirect(getDocument());
        PdfCanvas canvasOff = new PdfCanvas(streamOff, new PdfResources(), getDocument());
        PdfFormXObject xObjectOff = new PdfFormXObject(rect);
        drawBorder(canvasOff, xObjectOff, width, height);
        xObjectOff.getPdfObject().getOutputStream().writeBytes(streamOff.getBytes());
        xObjectOff.getResources().addFont(getDocument(), getFont());

        PdfDictionary normalAppearance = new PdfDictionary();
        normalAppearance.put(new PdfName(onStateName), xObjectOn.getPdfObject());
        normalAppearance.put(new PdfName("Off"), xObjectOff.getPdfObject());

        PdfDictionary mk = new PdfDictionary();
        mk.put(PdfName.CA, new PdfString(text));

        PdfWidgetAnnotation widget = getWidgets().get(0);
        widget.put(PdfName.MK, mk);
        widget.setNormalAppearance(normalAppearance);
    }

    /**
     * Draws PDF/A-2 compliant check appearance.
     * Actually it's just PdfA check appearance. According to corrigendum there is no difference between them
     *
     * @param width       width of the checkbox
     * @param height      height of the checkbox
     * @param onStateName name that corresponds to the "On" state of the checkbox
     * @param checkType   the type that determines how the checkbox will look like. Allowed values are {@link PdfFormField#TYPE_CHECK},
     *                    {@link PdfFormField#TYPE_CIRCLE}, {@link PdfFormField#TYPE_CROSS}, {@link PdfFormField#TYPE_DIAMOND},
     *                    {@link PdfFormField#TYPE_SQUARE}, {@link PdfFormField#TYPE_STAR}
     */
    protected void drawPdfA2CheckAppearance(float width, float height, String onStateName, int checkType) {
        this.checkType = checkType;
        Rectangle rect = new Rectangle(0, 0, width, height);

        PdfStream streamOn = (PdfStream) new PdfStream().makeIndirect(getDocument());
        PdfCanvas canvasOn = new PdfCanvas(streamOn, new PdfResources(), getDocument());
        PdfFormXObject xObjectOn = new PdfFormXObject(rect);
        xObjectOn.getResources();

        drawBorder(canvasOn, xObjectOn, width, height);
        drawPdfACheckBox(canvasOn, width, height, true);
        xObjectOn.getPdfObject().getOutputStream().writeBytes(streamOn.getBytes());

        PdfStream streamOff = (PdfStream) new PdfStream().makeIndirect(getDocument());
        PdfCanvas canvasOff = new PdfCanvas(streamOff, new PdfResources(), getDocument());
        PdfFormXObject xObjectOff = new PdfFormXObject(rect);
        xObjectOff.getResources();

        drawBorder(canvasOff, xObjectOff, width, height);
        xObjectOff.getPdfObject().getOutputStream().writeBytes(streamOff.getBytes());

        PdfDictionary normalAppearance = new PdfDictionary();
        normalAppearance.put(new PdfName(onStateName), xObjectOn.getPdfObject());
        normalAppearance.put(new PdfName("Off"), xObjectOff.getPdfObject());

        PdfDictionary mk = new PdfDictionary();
        mk.put(PdfName.CA, new PdfString(text));

        PdfWidgetAnnotation widget = getWidgets().get(0);
        widget.put(PdfName.MK, mk);
        widget.setNormalAppearance(normalAppearance);
    }

    /**
     * Draws the appearance for a push button.
     *
     * @param width    the width of the pushbutton
     * @param height   the width of the pushbutton
     * @param text     the text to display on the button
     * @param font     a {@link PdfFont}
     * @param fontSize the size of the font
     * @return a new {@link PdfFormXObject}
     */
    protected PdfFormXObject drawPushButtonAppearance(float width, float height, String text,
                                                      PdfFont font, float fontSize) {
        PdfStream stream = (PdfStream) new PdfStream().makeIndirect(getDocument());
        PdfCanvas canvas = new PdfCanvas(stream, new PdfResources(), getDocument());

        PdfFormXObject xObject = new PdfFormXObject(new Rectangle(0, 0, width, height));
        drawBorder(canvas, xObject, width, height);

        if (img != null) {
            PdfImageXObject imgXObj = new PdfImageXObject(img);
            canvas.addXObjectWithTransformationMatrix(imgXObj, width - borderWidth, 0, 0, height - borderWidth,
                    borderWidth / 2, borderWidth / 2);
            xObject.getResources().addImage(imgXObj);
        } else if (form != null) {
            canvas.addXObjectWithTransformationMatrix(form, (height - borderWidth) / form.getHeight(), 0, 0,
                    (height - borderWidth) / form.getHeight(), borderWidth / 2, borderWidth / 2);
            xObject.getResources().addForm(form);
        } else {
            drawButton(canvas, 0, 0, width, height, text, font, fontSize);
            xObject.getResources().addFont(getDocument(), font);
        }
        xObject.getPdfObject().getOutputStream().writeBytes(stream.getBytes());

        return xObject;
    }

    /**
     * Performs the low-level drawing operations to draw a button object.
     *
     * @param canvas   the {@link PdfCanvas} of the page to draw on.
     * @param x        will be ignored, according to spec it shall be 0
     * @param y        will be ignored, according to spec it shall be 0
     * @param width    the width of the button
     * @param height   the width of the button
     * @param text     the text to display on the button
     * @param font     a {@link PdfFont}
     * @param fontSize the size of the font
     */
    protected void drawButton(PdfCanvas canvas, float x, float y, float width, float height, String text, PdfFont font, float fontSize) {
        if (color == null) {
            color = ColorConstants.BLACK;
        }
        if (text == null) {
            text = "";
        }

        Paragraph paragraph = new Paragraph(text).setFont(font).setFontSize(fontSize).setMargin(0).setMultipliedLeading(1).
                setVerticalAlignment(VerticalAlignment.MIDDLE);
        Canvas modelCanvas = new Canvas(canvas, new Rectangle(0, -height, width, 2 * height));
        modelCanvas.setProperty(Property.APPEARANCE_STREAM_LAYOUT, true);

        setMetaInfoToCanvas(modelCanvas);

        modelCanvas.showTextAligned(paragraph, width / 2, height / 2, TextAlignment.CENTER, VerticalAlignment.MIDDLE);
    }

    /**
     * Performs the low-level drawing operations to draw a checkbox object.
     *
     * @param canvas   the {@link PdfCanvas} of the page to draw on.
     * @param width    the width of the button
     * @param height   the width of the button
     * @param fontSize the size of the font
     */
    protected void drawCheckBox(PdfCanvas canvas, float width, float height, float fontSize) {
        if (checkType == TYPE_CROSS) {
            DrawingUtil.drawCross(canvas, width, height, borderWidth);
            return;
        }
        PdfFont ufont = getFont();
        if (fontSize <= 0) {
            // there is no min font size for checkbox, however we can't set 0, because it means auto size.
            fontSize = approximateFontSizeToFitSingleLine(ufont, new Rectangle(width, height), text, 0.1f);
        }
        // PdfFont gets all width in 1000 normalized units
        canvas.
                beginText().
                setFontAndSize(ufont, fontSize).
                resetFillColorRgb().
                setTextMatrix((width - ufont.getWidth(text, fontSize)) / 2, (height - ufont.getAscent(text, fontSize)) / 2).
                showText(text).
                endText();
    }

    protected void drawPdfACheckBox(PdfCanvas canvas, float width, float height, boolean on) {
        if (!on) {
            return;
        }
        switch (checkType) {
            case TYPE_CHECK:
                DrawingUtil.drawPdfACheck(canvas, width, height);
                break;
            case TYPE_CIRCLE:
                DrawingUtil.drawPdfACircle(canvas, width, height);
                break;
            case TYPE_CROSS:
                DrawingUtil.drawPdfACross(canvas, width, height);
                break;
            case TYPE_DIAMOND:
                DrawingUtil.drawPdfADiamond(canvas, width, height);
                break;
            case TYPE_SQUARE:
                DrawingUtil.drawPdfASquare(canvas, width, height);
                break;
            case TYPE_STAR:
                DrawingUtil.drawPdfAStar(canvas, width, height);
                break;
        }
    }

    static void setMetaInfoToCanvas(Canvas canvas) {
        MetaInfoContainer metaInfo = FormsMetaInfoStaticContainer.getMetaInfoForLayout();
        if (metaInfo != null) {
            canvas.setProperty(Property.META_INFO, metaInfo);
        }
    }

    private String getRadioButtonValue() {
        for (String state : getAppearanceStates()) {
            if (!"Off".equals(state)) {
                return state;
            }
        }
        return null;
    }

    private float getFontSize(PdfArray bBox, String value) {
        assert !isMultiline();
        if (this.fontSize == 0) {
            if (bBox == null || value == null || value.isEmpty()) {
                return DEFAULT_FONT_SIZE;
            } else {
                return approximateFontSizeToFitSingleLine(this.font, bBox.toRectangle(), value, MIN_FONT_SIZE);
            }
        }
        return this.fontSize;
    }

    private float approximateFontSizeToFitMultiLine(Paragraph paragraph, Rectangle rect, IRenderer parentRenderer) {
        IRenderer renderer = paragraph.createRendererSubTree().setParent(parentRenderer);
        LayoutContext layoutContext = new LayoutContext(new LayoutArea(1, rect));
        float lFontSize = MIN_FONT_SIZE, rFontSize = DEFAULT_FONT_SIZE;

        paragraph.setFontSize(DEFAULT_FONT_SIZE);
        if (renderer.layout(layoutContext).getStatus() != LayoutResult.FULL) {
            final int numberOfIterations = 6;
            for (int i = 0; i < numberOfIterations; i++) {
                float mFontSize = (lFontSize + rFontSize) / 2;
                paragraph.setFontSize(mFontSize);
                LayoutResult result = renderer.layout(layoutContext);
                if (result.getStatus() == LayoutResult.FULL) {
                    lFontSize = mFontSize;
                } else {
                    rFontSize = mFontSize;
                }
            }
        } else {
            lFontSize = DEFAULT_FONT_SIZE;
        }
        return lFontSize;
    }

    // For text field that value shall be min 4, for checkbox there is no min value.
    private float approximateFontSizeToFitSingleLine(PdfFont localFont, Rectangle bBox, String value, float minValue) {
        float fs;
        float height = bBox.getHeight() - borderWidth * 2;
        int[] fontBbox = localFont.getFontProgram().getFontMetrics().getBbox();
        fs = height / (fontBbox[2] - fontBbox[1]) * FontProgram.UNITS_NORMALIZATION;

        float baseWidth = localFont.getWidth(value, 1);
        if (baseWidth != 0) {
            float availableWidth = Math.max(bBox.getWidth() - borderWidth * 2, 0);
            // This constant is taken based on what was the resultant padding in previous version of this algorithm in case border width was zero.
            float absMaxPadding = 4f;
            // relative value is quite big in order to preserve visible padding on small field sizes. This constant is taken arbitrary, based on visual similarity to Acrobat behaviour.
            float relativePaddingForSmallSizes = 0.15f;
            // with current constants, if availableWidth is less than ~26 points, padding will be made relative
            if (availableWidth * relativePaddingForSmallSizes < absMaxPadding) {
                availableWidth -= availableWidth * relativePaddingForSmallSizes * 2;
            } else {
                availableWidth -= absMaxPadding * 2;
            }
            fs = Math.min(fs, availableWidth / baseWidth);
        }
        return Math.max(fs, minValue);
    }

    /**
     * Calculate the necessary height offset after applying field rotation
     * so that the origin of the bounding box is the lower left corner with respect to the field text.
     *
     * @param bBox             bounding box rectangle before rotation
     * @param pageRotation     rotation of the page
     * @param relFieldRotation rotation of the field relative to the page
     * @return translation value for height
     */
    private float calculateTranslationHeightAfterFieldRot(Rectangle bBox, double pageRotation,
                                                          double relFieldRotation) {
        if (relFieldRotation == 0) {
            return 0.0f;
        }
        if (pageRotation == 0) {
            if (relFieldRotation == Math.PI / 2) {
                return bBox.getHeight();
            }
            if (relFieldRotation == Math.PI) {
                return bBox.getHeight();
            }

        }
        if (pageRotation == -Math.PI / 2) {
            if (relFieldRotation == -Math.PI / 2) {
                return bBox.getWidth() - bBox.getHeight();
            }
            if (relFieldRotation == Math.PI / 2) {
                return bBox.getHeight();
            }
            if (relFieldRotation == Math.PI) {
                return bBox.getWidth();
            }

        }
        if (pageRotation == -Math.PI) {
            if (relFieldRotation == -1 * Math.PI) {
                return bBox.getHeight();
            }
            if (relFieldRotation == -1 * Math.PI / 2) {
                return bBox.getHeight() - bBox.getWidth();
            }

            if (relFieldRotation == Math.PI / 2) {
                return bBox.getWidth();
            }
        }
        if (pageRotation == -3 * Math.PI / 2) {
            if (relFieldRotation == -3 * Math.PI / 2) {
                return bBox.getWidth();
            }
            if (relFieldRotation == -Math.PI) {
                return bBox.getWidth();
            }
        }

        return 0.0f;
    }

    /**
     * Calculate the necessary width offset after applying field rotation
     * so that the origin of the bounding box is the lower left corner with respect to the field text.
     *
     * @param bBox             bounding box rectangle before rotation
     * @param pageRotation     rotation of the page
     * @param relFieldRotation rotation of the field relative to the page
     * @return translation value for width
     */
    private float calculateTranslationWidthAfterFieldRot(Rectangle bBox, double pageRotation,
                                                         double relFieldRotation) {
        if (relFieldRotation == 0) {
            return 0.0f;
        }
        if (pageRotation == 0 && (relFieldRotation == Math.PI || relFieldRotation == 3 * Math.PI / 2)) {
            return bBox.getWidth();
        }
        if (pageRotation == -Math.PI / 2) {
            if (relFieldRotation == -Math.PI / 2 || relFieldRotation == Math.PI) {
                return bBox.getHeight();
            }
        }

        if (pageRotation == -Math.PI) {
            if (relFieldRotation == -1 * Math.PI) {
                return bBox.getWidth();
            }
            if (relFieldRotation == -1 * Math.PI / 2) {
                return bBox.getHeight();
            }
            if (relFieldRotation == Math.PI / 2) {
                return -1 * (bBox.getHeight() - bBox.getWidth());
            }
        }
        if (pageRotation == -3 * Math.PI / 2) {
            if (relFieldRotation == -3 * Math.PI / 2) {
                return -1 * (bBox.getWidth() - bBox.getHeight());
            }
            if (relFieldRotation == -Math.PI) {
                return bBox.getHeight();
            }
            if (relFieldRotation == -Math.PI / 2) {
                return bBox.getWidth();
            }
        }
        return 0.0f;
    }

    private boolean hasDefaultAppearance() {
        PdfName type = getFormType();
        return type == PdfName.Tx
                || type == PdfName.Ch
                || (type == PdfName.Btn && (getFieldFlags() & PdfButtonFormField.FF_PUSH_BUTTON) != 0);
    }

    private PdfName getUniqueFontNameForDR(PdfDictionary fontResources) {
        int indexer = 1;
        Set<PdfName> fontNames = fontResources.keySet();
        PdfName uniqueName;
        do {
            uniqueName = new PdfName("F" + indexer++);
        } while (fontNames.contains(uniqueName));
        return uniqueName;
    }

    private PdfName getFontNameFromDR(PdfDictionary fontResources, PdfObject font) {
        for (Map.Entry<PdfName, PdfObject> drFont : fontResources.entrySet()) {
            if (drFont.getValue() == font) {
                return drFont.getKey();
            }
        }
        return null;
    }

    private PdfObject getAcroFormObject(PdfName key, int type) {
        PdfObject acroFormObject = null;
        PdfDictionary acroFormDictionary = getDocument().getCatalog().getPdfObject().getAsDictionary(PdfName.AcroForm);
        if (acroFormDictionary != null) {
            acroFormObject = acroFormDictionary.get(key);
        }
        return (acroFormObject != null && acroFormObject.getType() == type) ? acroFormObject : null;
    }

    /**
     * Puts object directly to AcroForm dictionary.
     * It works much faster than consequent invocation of {@link PdfAcroForm#getAcroForm(PdfDocument, boolean)}
     * and {@link PdfAcroForm#getPdfObject()}.
     * <p>
     * Note, this method assume that Catalog already has AcroForm object.
     * {@link #addAcroFormToCatalog()} should be called explicitly.
     *
     * @param acroFormKey    the key of the object.
     * @param acroFormObject the object to add.
     */
    private void putAcroFormObject(PdfName acroFormKey, PdfObject acroFormObject) {
        getDocument().getCatalog().getPdfObject().getAsDictionary(PdfName.AcroForm).put(acroFormKey, acroFormObject);
    }

    private void addAcroFormToCatalog() {
        if (getDocument().getCatalog().getPdfObject().getAsDictionary(PdfName.AcroForm) == null) {
            PdfDictionary acroform = new PdfDictionary();
            acroform.makeIndirect(getDocument());
            // PdfName.Fields is the only required key.
            acroform.put(PdfName.Fields, new PdfArray());
            getDocument().getCatalog().put(PdfName.AcroForm, acroform);
        }
    }

    private PdfObject getAcroFormKey(PdfName key, int type) {
        PdfObject acroFormKey = null;
        PdfDocument document = getDocument();
        if (document != null) {
            PdfDictionary acroFormDictionary = document.getCatalog().getPdfObject().getAsDictionary(PdfName.AcroForm);
            if (acroFormDictionary != null) {
                acroFormKey = acroFormDictionary.get(key);
            }
        }
        return (acroFormKey != null && acroFormKey.getType() == type) ? acroFormKey : null;
    }

    private TextAlignment convertJustificationToTextAlignment() {
        Integer justification = getJustification();
        if (justification == null) {
            justification = 0;
        }
        TextAlignment textAlignment = TextAlignment.LEFT;
        if (justification == ALIGN_RIGHT) {
            textAlignment = TextAlignment.RIGHT;
        } else if (justification == ALIGN_CENTER) {
            textAlignment = TextAlignment.CENTER;
        }
        return textAlignment;
    }

    private PdfName getTypeFromParent(PdfDictionary field) {
        PdfDictionary parent = field.getAsDictionary(PdfName.Parent);
        PdfName formType = field.getAsName(PdfName.FT);
        if (parent != null) {
            formType = parent.getAsName(PdfName.FT);
            if (formType == null) {
                formType = getTypeFromParent(parent);
            }
        }
        return formType;
    }

    private String obfuscatePassword(String text) {
        char[] pchar = new char[text.length()];
        for (int i = 0; i < text.length(); i++)
            pchar[i] = '*';
        return new String(pchar);
    }

    private void applyRotation(PdfFormXObject xObject, float height, float width) {
        switch (rotation) {
            case 90:
                xObject.put(PdfName.Matrix, new PdfArray(new float[]{0, 1, -1, 0, height, 0}));
                break;
            case 180:
                xObject.put(PdfName.Matrix, new PdfArray(new float[]{-1, 0, 0, -1, width, height}));
                break;
            case 270:
                xObject.put(PdfName.Matrix, new PdfArray(new float[]{0, -1, 1, 0, 0, width}));
                break;
        }
    }

    private PdfObject getValueFromAppearance(PdfObject appearanceDict, PdfName key) {
        if (appearanceDict instanceof PdfDictionary) {
            return ((PdfDictionary) appearanceDict).get(key);
        }
        return null;
    }

    private void retrieveStyles() {
        // For now we retrieve styles only in case of merged widget with the field,
        // for one field might contain several widgets with their own different styles
        // and it's unclear how to handle it with the way iText processes fields with multiple widgets currently.
        PdfName subType = getPdfObject().getAsName(PdfName.Subtype);
        if (subType != null && subType.equals(PdfName.Widget)) {
            PdfDictionary appearanceCharacteristics = getPdfObject().getAsDictionary(PdfName.MK);
            if (appearanceCharacteristics != null) {
                backgroundColor = appearancePropToColor(appearanceCharacteristics, PdfName.BG);
                Color extractedBorderColor = appearancePropToColor(appearanceCharacteristics, PdfName.BC);
                if (extractedBorderColor != null)
                    borderColor = extractedBorderColor;
            }
        }
        PdfString defaultAppearance = getDefaultAppearance();
        if (defaultAppearance != null) {
            Object[] fontData = splitDAelements(defaultAppearance.getValue());
            if (fontData[DA_SIZE] != null && fontData[DA_FONT] != null) {
                color = (Color) fontData[DA_COLOR];
                fontSize = (float) fontData[DA_SIZE];
                font = resolveFontName((String) fontData[DA_FONT]);
            }
        }

        updateFontAndFontSize(this.font, this.fontSize);
    }

    private PdfFont resolveFontName(String fontName) {
        PdfDictionary defaultResources = (PdfDictionary) getAcroFormObject(PdfName.DR, PdfObject.DICTIONARY);
        PdfDictionary defaultFontDic = defaultResources != null ? defaultResources.getAsDictionary(PdfName.Font) : null;
        if (fontName != null && defaultFontDic != null) {
            PdfDictionary daFontDict = defaultFontDic.getAsDictionary(new PdfName(fontName));
            if (daFontDict != null) {
                return getDocument().getFont(daFontDict);
            }
        }
        return null;
    }

    private Color appearancePropToColor(PdfDictionary appearanceCharacteristics, PdfName property) {
        PdfArray colorData = appearanceCharacteristics.getAsArray(property);
        if (colorData != null) {
            float[] backgroundFloat = new float[colorData.size()];
            for (int i = 0; i < colorData.size(); i++)
                backgroundFloat[i] = colorData.getAsNumber(i).floatValue();
            switch (colorData.size()) {
                case 0:
                    return null;
                case 1:
                    return new DeviceGray(backgroundFloat[0]);
                case 3:
                    return new DeviceRgb(backgroundFloat[0], backgroundFloat[1], backgroundFloat[2]);
                case 4:
                    return new DeviceCmyk(backgroundFloat[0], backgroundFloat[1], backgroundFloat[2], backgroundFloat[3]);
            }
        }
        return null;
    }

    private void regeneratePushButtonField() {
        PdfDictionary widget = getPdfObject();
        PdfFormXObject appearance;
        Rectangle rect = getRect(widget);
        PdfDictionary apDic = widget.getAsDictionary(PdfName.AP);

        if (apDic == null) {
            put(PdfName.AP, apDic = new PdfDictionary());
        }
        appearance = drawPushButtonAppearance(rect.getWidth(), rect.getHeight(), this.text,
                this.font, getFontSize(widget.getAsArray(PdfName.Rect), this.text));

        apDic.put(PdfName.N, appearance.getPdfObject());

        if (pdfAConformanceLevel != null) {
            createPushButtonAppearanceState(widget);
        }
    }

    private void regenerateRadioButtonField() {
        Rectangle rect = getRect(getPdfObject());
        String value = getRadioButtonValue();
        if (rect != null && !"".equals(value)) {
            drawRadioAppearance(rect.getWidth(), rect.getHeight(), value);
        }
    }

    private void regenerateCheckboxField(String value) {
        Rectangle rect = getRect(getPdfObject());
        setCheckType(checkType);

        PdfWidgetAnnotation widget = (PdfWidgetAnnotation) PdfAnnotation.makeAnnotation(getPdfObject());

        if (pdfAConformanceLevel != null) {
            drawPdfA2CheckAppearance(rect.getWidth(), rect.getHeight(), "Off".equals(value) ? "Yes" : value, checkType);
            widget.setFlag(PdfAnnotation.PRINT);
        } else {
            drawCheckAppearance(rect.getWidth(), rect.getHeight(), "Off".equals(value) ? "Yes" : value);
        }

        if (widget.getNormalAppearanceObject() != null && widget.getNormalAppearanceObject().containsKey(new PdfName(value))) {
            widget.setAppearanceState(new PdfName(value));
        } else {
            widget.setAppearanceState(new PdfName("Off"));
        }
    }

    private boolean regenerateTextAndChoiceField(String value, PdfName type) {
        PdfPage page = PdfWidgetAnnotation.makeAnnotation(getPdfObject()).getPage();
        PdfArray bBox = getPdfObject().getAsArray(PdfName.Rect);

        //Apply Page rotation
        int pageRotation = 0;
        if (page != null) {
            pageRotation = page.getRotation();
            //Clockwise, so negative
            pageRotation *= -1;
        }
        PdfArray matrix;
        if (pageRotation % 90 == 0) {
            //Cast angle to [-360, 360]
            double angle = pageRotation % 360;
            //Get angle in radians
            angle = degreeToRadians(angle);
            Rectangle initialBboxRectangle = bBox.toRectangle();
            //rotate the bounding box
            Rectangle rect = initialBboxRectangle.clone();
            //Calculate origin offset
            double translationWidth = 0;
            double translationHeight = 0;
            if (angle >= -1 * Math.PI && angle <= -1 * Math.PI / 2) {
                translationWidth = rect.getWidth();
            }
            if (angle <= -1 * Math.PI) {
                translationHeight = rect.getHeight();
            }

            //Store rotation and translation in the matrix
            matrix = new PdfArray(new double[]{Math.cos(angle), -Math.sin(angle), Math.sin(angle), Math.cos(angle), translationWidth, translationHeight});
            //If the angle is a multiple of 90 and not a multiple of 180, height and width of the bounding box need to be switched
            if (angle % (Math.PI / 2) == 0 && angle % (Math.PI) != 0) {
                rect.setWidth(initialBboxRectangle.getHeight());
                rect.setHeight(initialBboxRectangle.getWidth());
            }
            // Adapt origin
            rect.setX(rect.getX() + (float) translationWidth);
            rect.setY(rect.getY() + (float) translationHeight);
            //Copy Bounding box
            bBox = new PdfArray(rect);
        } else {
            //Avoid NPE when handling corrupt pdfs
            Logger logger = Logger.getLogger(PdfFormField.class.getName());
            logger.log(Level.SEVERE,IoLogMessageConstant.INCORRECT_PAGEROTATION);
            matrix = new PdfArray(new double[]{1, 0, 0, 1, 0, 0});
        }
        //Apply field rotation
        float fieldRotation = 0;
        if (this.getPdfObject().getAsDictionary(PdfName.MK) != null
                && this.getPdfObject().getAsDictionary(PdfName.MK).get(PdfName.R) != null) {
            fieldRotation = (float) this.getPdfObject().getAsDictionary(PdfName.MK).getAsFloat(PdfName.R);
            //Get relative field rotation
            fieldRotation += pageRotation;
        }
        if (fieldRotation % 90 == 0) {
            Rectangle initialBboxRectangle = bBox.toRectangle();
            //Cast angle to [-360, 360]
            double angle = fieldRotation % 360;
            //Get angle in radians
            angle = degreeToRadians(angle);
            //Calculate origin offset
            double translationWidth = calculateTranslationWidthAfterFieldRot(initialBboxRectangle, degreeToRadians(pageRotation), angle);
            double translationHeight = calculateTranslationHeightAfterFieldRot(initialBboxRectangle, degreeToRadians(pageRotation), angle);

            //Concatenate rotation and translation into the matrix
            Matrix currentMatrix = new Matrix(matrix.getAsNumber(0).floatValue(), matrix.getAsNumber(1).floatValue(), matrix.getAsNumber(2).floatValue(), matrix.getAsNumber(3).floatValue(), matrix.getAsNumber(4).floatValue(), matrix.getAsNumber(5).floatValue());
            Matrix toConcatenate = new Matrix((float) Math.cos(angle), (float) (-Math.sin(angle)), (float) (Math.sin(angle)), (float) (Math.cos(angle)), (float) translationWidth, (float) translationHeight);
            currentMatrix = currentMatrix.multiply(toConcatenate);
            matrix = new PdfArray(new float[]{currentMatrix.get(0), currentMatrix.get(1), currentMatrix.get(3), currentMatrix.get(4), currentMatrix.get(6), currentMatrix.get(7)});

            //Construct bounding box
            Rectangle rect = initialBboxRectangle.clone();
            //If the angle is a multiple of 90 and not a multiple of 180, height and width of the bounding box need to be switched
            if (angle % (Math.PI / 2) == 0 && angle % (Math.PI) != 0) {
                rect.setWidth(initialBboxRectangle.getHeight());
                rect.setHeight(initialBboxRectangle.getWidth());
            }
            rect.setX(rect.getX() + (float) translationWidth);
            rect.setY(rect.getY() + (float) translationHeight);
            //Copy Bounding box
            bBox =  new PdfArray(rect);
        }
        //Create appearance
        Rectangle bboxRectangle = bBox.toRectangle();
        PdfFormXObject appearance = new PdfFormXObject(new Rectangle(0, 0, bboxRectangle.getWidth(), bboxRectangle.getHeight()));
        appearance.put(PdfName.Matrix, matrix);
        //Create text appearance
        if (PdfName.Tx.equals(type)) {
            if (isMultiline()) {
                drawMultiLineTextAppearance(bboxRectangle, this.font, value, appearance);
            } else {
                drawTextAppearance(bboxRectangle, this.font, getFontSize(bBox, value), value, appearance);
            }
        } else {
            int topIndex = 0;
            if (!getFieldFlag(PdfChoiceFormField.FF_COMBO)) {
                PdfNumber topIndexNum = this.getPdfObject().getAsNumber(PdfName.TI);
                if (topIndexNum == null && this.getParent() != null) {
                    topIndexNum = this.getParent().getAsNumber(PdfName.TI);
                }
                PdfArray options = getOptions();
                if (null == options && this.getParent() != null) {
                    options = this.getParent().getAsArray(PdfName.Opt);
                }
                if (null != options) {
                    topIndex = null != topIndexNum ? topIndexNum.intValue() : 0;
                    PdfArray visibleOptions = topIndex > 0
                            ? new PdfArray(options.subList(topIndex, options.size())) : (PdfArray) options.clone();
                    value = optionsArrayToString(visibleOptions);
                }
            }
            drawChoiceAppearance(bboxRectangle, getFontSize(bBox, value), value, appearance, topIndex);
        }
        PdfDictionary ap = new PdfDictionary();
        ap.put(PdfName.N, appearance.getPdfObject());
        ap.setModified();
        put(PdfName.AP, ap);

        return true;
    }

    private void copyParamsToKids(PdfFormField child) {
        if (child.checkType <= 0 || child.checkType > 5) {
            child.checkType = this.checkType;
        }
        if (child.getDefaultAppearance() == null) {
            child.font = this.font;
            child.fontSize = this.fontSize;
        }
        if (child.color == null) {
            child.color = this.color;
        }
        if (child.text == null) {
            child.text = this.text;
        }
        if (child.img == null) {
            child.img = this.img;
        }
        if (child.borderWidth == 1) {
            child.borderWidth = this.borderWidth;
        }
        if (child.backgroundColor == null) {
            child.backgroundColor = this.backgroundColor;
        }
        if (child.borderColor == null) {
            child.borderColor = this.borderColor;
        }
        if (child.rotation == 0) {
            child.rotation = this.rotation;
        }
        if (child.pdfAConformanceLevel == null) {
            child.pdfAConformanceLevel = this.pdfAConformanceLevel;
        }
        if (child.form == null) {
            child.form = this.form;
        }
    }

    private boolean regenerateWidget(String value) {
        PdfName type = getFormType();

        if (PdfName.Tx.equals(type) || PdfName.Ch.equals(type)) {
            return regenerateTextAndChoiceField(value, type);
        } else if (PdfName.Btn.equals(type)) {
            if (getFieldFlag(PdfButtonFormField.FF_PUSH_BUTTON)) {
                regeneratePushButtonField();
            } else if (getFieldFlag(PdfButtonFormField.FF_RADIO)) {
                regenerateRadioButtonField();
            } else {
                regenerateCheckboxField(value);
            }
            return true;
        }
        return false;
    }

    private static String optionsArrayToString(PdfArray options) {
        StringBuilder sb = new StringBuilder();
        for (PdfObject obj : options) {
            if (obj.isString()) {
                sb.append(((PdfString) obj).toUnicodeString()).append('\n');
            } else if (obj.isArray()) {
                PdfObject element = ((PdfArray) obj).get(1);
                if (element.isString()) {
                    sb.append(((PdfString) element).toUnicodeString()).append('\n');
                }
            } else {
                sb.append('\n');
            }
        }
        // last '\n'
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static double degreeToRadians(double angle) {
        return Math.PI * angle / 180.0;
    }

    private static PdfString generateDefaultAppearance(PdfName font, float fontSize, Color textColor) {
        assert font != null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfOutputStream pdfStream = new PdfOutputStream(new OutputStream<>(output));
        final byte[] g = new byte[]{(byte) 'g'};
        final byte[] rg = new byte[]{(byte) 'r', (byte) 'g'};
        final byte[] k = new byte[]{(byte) 'k'};
        final byte[] Tf = new byte[]{(byte) 'T', (byte) 'f'};

        pdfStream.write(font)
                .writeSpace()
                .writeFloat(fontSize).writeSpace()
                .writeBytes(Tf);

        if (textColor != null) {
            if (textColor instanceof DeviceGray) {
                pdfStream.writeSpace()
                        .writeFloats(textColor.getColorValue())
                        .writeSpace()
                        .writeBytes(g);
            } else if (textColor instanceof DeviceRgb) {
                pdfStream.writeSpace()
                        .writeFloats(textColor.getColorValue())
                        .writeSpace()
                        .writeBytes(rg);
            } else if (textColor instanceof DeviceCmyk) {
                pdfStream.writeSpace()
                        .writeFloats(textColor.getColorValue())
                        .writeSpace()
                        .writeBytes(k);
            } else {
                Logger logger = Logger.getLogger(PdfFormField.class.getName());
                logger.log(Level.SEVERE,IoLogMessageConstant.UNSUPPORTED_COLOR_IN_DA);
            }
        }
        return new PdfString(output.toByteArray());
    }

    private static boolean isWidgetAnnotation(PdfDictionary pdfObject) {
        return pdfObject != null && PdfName.Widget.equals(pdfObject.getAsName(PdfName.Subtype));
    }

    private static void createPushButtonAppearanceState(PdfDictionary widget) {
        PdfDictionary appearances = widget.getAsDictionary(PdfName.AP);
        PdfStream normalAppearanceStream = appearances.getAsStream(PdfName.N);
        if (normalAppearanceStream != null) {
            PdfName stateName = widget.getAsName(PdfName.AS);
            if (stateName == null) {
                stateName = new PdfName("push");
            }
            widget.put(PdfName.AS, stateName);
            PdfDictionary normalAppearance = new PdfDictionary();
            normalAppearance.put(stateName, normalAppearanceStream);
            appearances.put(PdfName.N, normalAppearance);
        }
    }

    private static Paragraph createParagraphForTextFieldValue(String value) {
        Text text = new Text(value);
        text.setNextRenderer(new FormFieldValueNonTrimmingTextRenderer(text));
        return new Paragraph(text);
    }
}
