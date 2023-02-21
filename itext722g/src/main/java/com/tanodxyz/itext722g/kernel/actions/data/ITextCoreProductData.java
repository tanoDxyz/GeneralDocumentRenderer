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
package com.tanodxyz.itext722g.kernel.actions.data;


import com.tanodxyz.itext722g.commons.actions.ProductNameConstant;
import com.tanodxyz.itext722g.commons.actions.data.ProductData;

/**
 * Stores an instance of {@link ProductData} related to iText core module.
 */
public final class ITextCoreProductData {
    private static final String CORE_PUBLIC_PRODUCT_NAME = "Core";
    private static final String CORE_VERSION = "7.2.2";
    private static final int CORE_COPYRIGHT_SINCE = 2000;
    private static final int CORE_COPYRIGHT_TO = 2022;

    private static final ProductData ITEXT_PRODUCT_DATA = new ProductData(CORE_PUBLIC_PRODUCT_NAME,
            ProductNameConstant.ITEXT_CORE, CORE_VERSION, CORE_COPYRIGHT_SINCE, CORE_COPYRIGHT_TO);

    private ITextCoreProductData() {
        // Empty constructor.
    }

    /**
     * Getter for an instance of {@link ProductData} related to iText core module.
     *
     * @return iText core product description
     */
    public static ProductData getInstance() {
        return ITEXT_PRODUCT_DATA;
    }
}
