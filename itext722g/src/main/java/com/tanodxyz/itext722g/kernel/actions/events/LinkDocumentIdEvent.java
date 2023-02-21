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
package com.tanodxyz.itext722g.kernel.actions.events;


import com.tanodxyz.itext722g.commons.actions.AbstractITextConfigurationEvent;
import com.tanodxyz.itext722g.commons.actions.AbstractProductProcessITextEvent;
import com.tanodxyz.itext722g.commons.actions.sequence.AbstractIdentifiableElement;
import com.tanodxyz.itext722g.commons.actions.sequence.SequenceId;
import com.tanodxyz.itext722g.commons.actions.sequence.SequenceIdManager;
import com.tanodxyz.itext722g.kernel.pdf.PdfDocument;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * An event allows to associated some {@link SequenceId} with {@link PdfDocument}.
 */
public final class LinkDocumentIdEvent extends AbstractITextConfigurationEvent {
    private final WeakReference<PdfDocument> document;
    private final WeakReference<SequenceId> sequenceId;

    /**
     * Creates a new instance of the event associating provided {@link PdfDocument} with the
     * appropriate {@link SequenceId}.
     *
     * @param document is a document
     * @param sequenceId is a general identifier to be associated with the document
     */
    public LinkDocumentIdEvent(PdfDocument document, SequenceId sequenceId) {
        super();
        this.document = new WeakReference<>(document);
        this.sequenceId = new WeakReference<>(sequenceId);
    }

    /**
     * Creates a new instance of the event associating provided {@link PdfDocument} with the
     * appropriate {@link AbstractIdentifiableElement}.
     *
     * @param document is a document
     * @param identifiableElement is an identifiable element to be associated with the document
     */
    public LinkDocumentIdEvent(PdfDocument document, AbstractIdentifiableElement identifiableElement) {
        this(document, identifiableElement == null ? null : SequenceIdManager.getSequenceId(identifiableElement));
    }

    /**
     * Defines an association between {@link PdfDocument} and {@link SequenceId}.
     */
    @Override
    public void doAction() {
        final SequenceId storedSequenceId = (SequenceId) sequenceId.get();
        final PdfDocument storedPdfDocument = (PdfDocument) document.get();

        if (storedSequenceId == null || storedPdfDocument == null) {
            return;
        }

        final List<AbstractProductProcessITextEvent> anonymousEvents = getEvents(storedSequenceId);

        if (anonymousEvents != null) {
            final SequenceId documentId = storedPdfDocument.getDocumentIdWrapper();
            for (final AbstractProductProcessITextEvent event : anonymousEvents) {
                final List<AbstractProductProcessITextEvent> storedEvents = getEvents(documentId);
                if (!storedEvents.contains(event)) {
                    addEvent(documentId, event);
                }
            }
        }
    }
}
