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
package com.tanodxyz.itext722g.forms.xfdf;

import com.tanodxyz.itext722g.kernel.pdf.PdfName;
import com.tanodxyz.itext722g.kernel.pdf.PdfString;

/**
 * Represent Action tag in xfdf document structure.
 * Content model: ( URI | Launch | GoTo | GoToR | Named ).
 * Attributes: none.
 * For more details see paragraph 6.5.1 in Xfdf specification.
 */
public class ActionObject {


    /**
     * Type of inner action element. Possible values: {@link PdfName#URI}, {@link PdfName#Launch}, {@link PdfName#GoTo},
     * {@link PdfName#GoToR}, {@link PdfName#Named}.
     */
    private PdfName type;

    /**
     * Represents Name, required attribute of URI element. For more details see paragraph 6.5.30 in Xfdf specification.
     */
    private PdfString uri;

    /**
     * Represents IsMap, optional attribute of URI element. For more details see paragraph 6.5.30 in Xfdf specification.
     */
    private boolean isMap;

    /**
     * Represents Name, required attribute of Named element. For more details see paragraph 6.5.24 in Xfdf specification.
     */
    private PdfName nameAction;

    /**
     * Represents OriginalName required attribute of File inner element of GoToR or Launch element.
     * Corresponds to F key in go-to action or launch dictionaries.
     * For more details see paragraphs 6.5.11, 6.5.23 in Xfdf specification.
     */
    private String fileOriginalName;

    /**
     * Represents NewWindow, optional attribute of Launch element. For more details see paragraph 6.5.23 in Xfdf specification.
     */
    private boolean isNewWindow;

    /**
     * Represents Dest, inner element of link, GoTo, and GoToR elements.
     * Corresponds to Dest key in link annotation dictionary.
     * For more details see paragraph 6.5.10 in Xfdf specification.
     */
    private DestObject destination;

    public ActionObject(PdfName type) {
        this.type = type;
    }


    /**
     * Returns the type of inner action element. Possible values: {@link PdfName#URI}, {@link PdfName#Launch},
     * {@link PdfName#GoTo}, {@link PdfName#GoToR}, {@link PdfName#Named}.
     *
     * @return {@link PdfName} type of inner action element
     */
    public PdfName getType() {
        return type;
    }

    /**
     * Sets the type of inner action element. Possible values: {@link PdfName#URI}, {@link PdfName#Launch},
     * {@link PdfName#GoTo}, {@link PdfName#GoToR}, {@link PdfName#Named}.
     *
     * @param type {@link PdfName} type of inner action object
     * @return current {@link ActionObject}
     */
    public ActionObject setType(PdfName type) {
        this.type = type;
        return this;
    }

    /**
     * Gets the string value of URI elements. Corresponds to Name, required attribute of URI element.
     * For more details see paragraph 6.5.30 in Xfdf specification.
     *
     * @return {@link PdfString} value of URI element
     */
    public PdfString getUri() {
        return uri;
    }

    /**
     * Sets the string value of URI element. Corresponds to Name, required attribute of URI element.
     * For more details see paragraph 6.5.30 in Xfdf specification.
     *
     * @param uri {@link PdfString} value to be set to URI element
     * @return current {@link ActionObject}
     */
    public ActionObject setUri(PdfString uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Gets IsMap, optional attribute of URI element. For more details see paragraph 6.5.30 in Xfdf specification.
     *
     * @return boolean indicating if URI element is a map
     */
    public boolean isMap() {
        return isMap;
    }

    /**
     * Sets IsMap, optional attribute of URI element. For more details see paragraph 6.5.30 in Xfdf specification.
     *
     * @param map boolean indicating if URI element is a map
     * @return current {@link ActionObject}
     */
    public ActionObject setMap(boolean map) {
        isMap = map;
        return this;
    }

    /**
     * Gets the value of Name, required attribute of Named element. For more details see paragraph 6.5.24 in Xfdf specification.
     *
     * @return {@link PdfName} value of Name attribute of a named action element
     */
    public PdfName getNameAction() {
        return nameAction;
    }

    /**
     * Sets the value of Name, required attribute of Named element. For more details see paragraph 6.5.24 in Xfdf specification.
     *
     * @param nameAction {@link PdfName} value to be set to Name attribute of a named action element
     * @return current {@link ActionObject}
     */
    public ActionObject setNameAction(PdfName nameAction) {
        this.nameAction = nameAction;
        return this;
    }

    /**
     * Gets the string value of OriginalName, required attribute of File inner element of GoToR or Launch element.
     * Corresponds to F key in go-to action or launch dictionaries.
     * For more details see paragraphs 6.5.11, 6.5.23 in Xfdf specification.
     *
     * @return {@link String} value of OriginalName attribute of current action object
     */
    public String getFileOriginalName() {
        return fileOriginalName;
    }

    /**
     * Sets the string value of OriginalName, required attribute of File inner element of GoToR or Launch element.
     * Corresponds to F key in go-to action or launch dictionaries.
     * For more details see paragraphs 6.5.11, 6.5.23 in Xfdf specification.
     *
     * @param fileOriginalName {@link String} value of OriginalName attribute of action object
     * @return current {@link ActionObject}
     */
    public ActionObject setFileOriginalName(String fileOriginalName) {
        this.fileOriginalName = fileOriginalName;
        return this;
    }

    /**
     * Gets the boolean value of NewWindow, optional attribute of Launch element. For more details see paragraph 6.5.23 in Xfdf specification.
     *
     * @return boolean indicating if current Launch action element should be opened in a new window
     */
    public boolean isNewWindow() {
        return isNewWindow;
    }

    /**
     * Sets the boolean value of NewWindow, optional attribute of Launch element. For more details see paragraph 6.5.23 in Xfdf specification.
     *
     * @param newWindow boolean indicating if current Launch action element should be opened in a new window
     * @return current {@link ActionObject}
     */
    public ActionObject setNewWindow(boolean newWindow) {
        isNewWindow = newWindow;
        return this;
    }

    /**
     * Gets Dest, inner element of link, GoTo, and GoToR elements.
     * Corresponds to Dest key in link annotation dictionary.
     * For more details see paragraph 6.5.10 in Xfdf specification.
     *
     * @return {@link DestObject} destination attribute of current action element
     */
    public   DestObject getDestination() {
        return destination;
    }

    /**
     * Sets Dest, inner element of link, GoTo, and GoToR elements.
     * Corresponds to Dest key in link annotation dictionary.
     * For more details see paragraph 6.5.10 in Xfdf specification.
     *
     * @param destination {@link DestObject} destination attribute of the action element
     * @return current {@link ActionObject}
     */
    public ActionObject setDestination(  DestObject destination) {
        this.destination = destination;
        return this;
    }
}
