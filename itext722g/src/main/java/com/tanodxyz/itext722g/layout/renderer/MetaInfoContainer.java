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
package com.tanodxyz.itext722g.layout.renderer;


import com.tanodxyz.itext722g.commons.actions.contexts.IMetaInfo;

/**
 * Class to store metaInfo that will be used for layout renderers.
 */
public class MetaInfoContainer {

    private final IMetaInfo metaInfo;

    /**
     * Creates MetaInfoContainer instance with provided meta info.
     *
     * @param metaInfo the meta info
     */
    public MetaInfoContainer(IMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * Return the IMetaInfo object.
     *
     * @return returns IMetaInfo
     */
    IMetaInfo getMetaInfo() {
        return metaInfo;
    }
}
