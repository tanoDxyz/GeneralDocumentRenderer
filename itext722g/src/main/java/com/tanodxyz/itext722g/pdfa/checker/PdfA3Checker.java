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
package com.tanodxyz.itext722g.pdfa.checker;


import com.tanodxyz.itext722g.kernel.pdf.PdfAConformanceLevel;
import com.tanodxyz.itext722g.kernel.pdf.PdfDictionary;
import com.tanodxyz.itext722g.kernel.pdf.PdfName;
import com.tanodxyz.itext722g.kernel.pdf.PdfObject;
import com.tanodxyz.itext722g.kernel.pdf.PdfStream;
import com.tanodxyz.itext722g.pdfa.exceptions.PdfAConformanceException;
import com.tanodxyz.itext722g.pdfa.logs.PdfAConformanceLogMessageConstant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * PdfA3Checker defines the requirements of the PDF/A-3 standard and contains a
 * number of methods that override the implementations of its superclass
 * {@link PdfA2Checker}.
 * 
 * The specification implemented by this class is ISO 19005-3
 */
public class PdfA3Checker extends PdfA2Checker {
    protected static final Set<PdfName> allowedAFRelationships = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(
                    PdfName.Source,
                    PdfName.Data,
                    PdfName.Alternative,
                    PdfName.Supplement,
                    PdfName.Unspecified)));

    /**
     * Creates a PdfA3Checker with the required conformance level
     * 
     * @param conformanceLevel the required conformance level, <code>a</code> or
     * <code>u</code> or <code>b</code>
     */
    public PdfA3Checker(PdfAConformanceLevel conformanceLevel) {
        super(conformanceLevel);
    }

    @Override
    protected void checkFileSpec(PdfDictionary fileSpec) {
        PdfName relationship = fileSpec.getAsName(PdfName.AFRelationship);
        if (relationship == null || !allowedAFRelationships.contains(relationship)) {
            throw new PdfAConformanceException(PdfAConformanceException.FILE_SPECIFICATION_DICTIONARY_SHALL_CONTAIN_ONE_OF_THE_PREDEFINED_AFRELATIONSHIP_KEYS);
        }

        if (fileSpec.containsKey(PdfName.EF)) {
            if (!fileSpec.containsKey(PdfName.F) || !fileSpec.containsKey(PdfName.UF) || !fileSpec.containsKey(PdfName.Desc)) {
                throw new PdfAConformanceException(PdfAConformanceException.FILE_SPECIFICATION_DICTIONARY_SHALL_CONTAIN_F_KEY_AND_UF_KEY);
            }


            PdfDictionary ef = fileSpec.getAsDictionary(PdfName.EF);
            PdfStream embeddedFile = ef.getAsStream(PdfName.F);
            if (embeddedFile == null) {
                throw new PdfAConformanceException(PdfAConformanceException.EF_KEY_OF_FILE_SPECIFICATION_DICTIONARY_SHALL_CONTAIN_DICTIONARY_WITH_VALID_F_KEY);
            }

            if (!embeddedFile.containsKey(PdfName.Subtype)) {
                throw new PdfAConformanceException(PdfAConformanceException.MIME_TYPE_SHALL_BE_SPECIFIED_USING_THE_SUBTYPE_KEY_OF_THE_FILE_SPECIFICATION_STREAM_DICTIONARY);
            }

            if (embeddedFile.containsKey(PdfName.Params)) {
                PdfObject params = embeddedFile.get(PdfName.Params);
                if (!params.isDictionary()) {
                    throw new PdfAConformanceException(PdfAConformanceException.EMBEDDED_FILE_SHALL_CONTAIN_PARAMS_KEY_WITH_DICTIONARY_AS_VALUE);
                }
                if (((PdfDictionary)params).getAsString(PdfName.ModDate) == null) {
                    throw new PdfAConformanceException(PdfAConformanceException.EMBEDDED_FILE_SHALL_CONTAIN_PARAMS_KEY_WITH_VALID_MODDATE_KEY);
                }
            } else {
                Logger logger = Logger.getLogger(PdfAChecker.class.getName());
                logger.warning(PdfAConformanceLogMessageConstant.EMBEDDED_FILE_SHOULD_CONTAIN_PARAMS_KEY);
            }
        }
    }
}
