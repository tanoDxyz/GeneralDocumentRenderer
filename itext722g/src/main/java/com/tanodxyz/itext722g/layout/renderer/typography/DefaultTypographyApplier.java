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
package com.tanodxyz.itext722g.layout.renderer.typography;


import com.tanodxyz.itext722g.commons.actions.contexts.IMetaInfo;
import com.tanodxyz.itext722g.commons.actions.sequence.SequenceId;
import com.tanodxyz.itext722g.io.font.FontProgram;
import com.tanodxyz.itext722g.io.font.TrueTypeFont;
import com.tanodxyz.itext722g.io.font.otf.GlyphLine;
import com.tanodxyz.itext722g.io.logs.IoLogMessageConstant;
import com.tanodxyz.itext722g.layout.properties.BaseDirection;
import com.tanodxyz.itext722g.layout.renderer.CharacterUtils;
import com.tanodxyz.itext722g.layout.renderer.LineRenderer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class DefaultTypographyApplier extends AbstractTypographyApplier {

    private static final Logger LOGGER = Logger.getLogger(DefaultTypographyApplier.class.getName());

    public DefaultTypographyApplier() {
    }

    @Override
    public boolean isPdfCalligraphInstance() {
        return false;
    }

    @Override
    public boolean applyOtfScript(TrueTypeFont font, GlyphLine glyphLine, CharacterUtils.UnicodeScript script, Object configurator,
                                  SequenceId id, IMetaInfo metaInfo) {
        LOGGER.warning(IoLogMessageConstant.TYPOGRAPHY_NOT_FOUND);
        return super.applyOtfScript(font, glyphLine, script, configurator, id, metaInfo);
    }

    @Override
    public Collection<CharacterUtils.UnicodeScript> getSupportedScripts() {
        LOGGER.warning(IoLogMessageConstant.TYPOGRAPHY_NOT_FOUND);
        return super.getSupportedScripts();
    }

    @Override
    public Collection<CharacterUtils.UnicodeScript> getSupportedScripts(Object configurator) {
        LOGGER.warning(IoLogMessageConstant.TYPOGRAPHY_NOT_FOUND);
        return super.getSupportedScripts(configurator);
    }

    @Override
    public boolean applyKerning(FontProgram fontProgram, GlyphLine text, SequenceId sequenceId, IMetaInfo metaInfo) {
        LOGGER.warning(IoLogMessageConstant.TYPOGRAPHY_NOT_FOUND);
        return super.applyKerning(fontProgram, text, sequenceId, metaInfo);
    }

    @Override
    public byte[] getBidiLevels(BaseDirection baseDirection, int[] unicodeIds, SequenceId sequenceId,
                                IMetaInfo metaInfo) {
        LOGGER.warning(IoLogMessageConstant.TYPOGRAPHY_NOT_FOUND);
        return super.getBidiLevels(baseDirection, unicodeIds, sequenceId, metaInfo);
    }

    @Override
    public int[] reorderLine(List<LineRenderer.RendererGlyph> line, byte[] lineLevels, byte[] levels) {
        LOGGER.warning(IoLogMessageConstant.TYPOGRAPHY_NOT_FOUND);
        return super.reorderLine(line, lineLevels, levels);
    }

    @Override
    public List<Integer> getPossibleBreaks(String str) {
        LOGGER.warning(IoLogMessageConstant.TYPOGRAPHY_NOT_FOUND);
        return super.getPossibleBreaks(str);
    }

    @Override
    public Map<String, byte[]> loadShippedFonts() throws IOException {
        LOGGER.warning(IoLogMessageConstant.TYPOGRAPHY_NOT_FOUND);
        return super.loadShippedFonts();
    }
}
