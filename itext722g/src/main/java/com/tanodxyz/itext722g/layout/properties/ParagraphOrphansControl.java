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
package com.tanodxyz.itext722g.layout.properties;

import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.io.logs.IoLogMessageConstant;
import com.tanodxyz.itext722g.layout.element.Paragraph;
import com.tanodxyz.itext722g.layout.renderer.ParagraphRenderer;

import java.util.logging.Logger;


/**
 * A specialized class holding configurable parameters related to {@link Paragraph}'s
 * orphans restrictions. This class is meant to be used as the value for the {@link Property#ORPHANS_CONTROL} key.
 */
public class ParagraphOrphansControl {
    private int minOrphans;

    /**
     * Creates a {@link ParagraphOrphansControl} instance with a specified orphans limitation.
     *
     * @param minOrphans minimal number of paragraph's lines to remain on an area before an area break.
     */
    public ParagraphOrphansControl(int minOrphans) {
        this.minOrphans = minOrphans;
    }

    /**
     * Sets parameter that defines orphans restrictions.
     *
     * @param minOrphans minimal number of paragraph's lines to remain on an area before an area break.
     * @return this {@link ParagraphOrphansControl} instance
     */
    public ParagraphOrphansControl setMinAllowedOrphans(int minOrphans) {
        this.minOrphans = minOrphans;
        return this;
    }

    /**
     * Gets minimal number of paragraph's lines to remain on an area before a split.
     *
     * @return minimal number of paragraph's lines to remain on an area before an area break.
     */
    public int getMinOrphans() {
        return minOrphans;
    }

    /**
     * Writes a log message reporting that orphans constraint is violated.
     *
     * This method is to be overridden if violation scenarios need to be handled in some other way.
     *
     * @param renderer a renderer processing orphans
     * @param message  {@link String} explaining the reason for violation
     */
    public void handleViolatedOrphans(ParagraphRenderer renderer, String message) {
        Logger logger = Logger.getLogger(ParagraphOrphansControl.class.getName());
        if (renderer.getOccupiedArea() != null && renderer.getLines() != null) {
            int pageNumber = renderer.getOccupiedArea().getPageNumber();
            String warnText = MessageFormatUtil.format(IoLogMessageConstant.ORPHANS_CONSTRAINT_VIOLATED, pageNumber,
                    minOrphans, renderer.getLines().size(), message);
            logger.warning(warnText);
        } else {
            logger.warning(IoLogMessageConstant.PREMATURE_CALL_OF_HANDLE_VIOLATION_METHOD);
        }
    }
}
