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
import com.tanodxyz.itext722g.commons.actions.EventManager;
import com.tanodxyz.itext722g.commons.actions.confirmations.ConfirmEvent;
import com.tanodxyz.itext722g.commons.actions.confirmations.ConfirmedEventWrapper;
import com.tanodxyz.itext722g.commons.actions.confirmations.EventConfirmationType;
import com.tanodxyz.itext722g.commons.actions.data.ProductData;
import com.tanodxyz.itext722g.commons.actions.processors.ITextProductEventProcessor;
import com.tanodxyz.itext722g.commons.actions.producer.ProducerBuilder;
import com.tanodxyz.itext722g.commons.actions.sequence.SequenceId;
import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.kernel.actions.data.ITextCoreProductData;
import com.tanodxyz.itext722g.kernel.logs.KernelLogMessageConstant;
import com.tanodxyz.itext722g.kernel.pdf.PdfDocument;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Class represents events notifying that {@link PdfDocument} was flushed.
 */
public final class FlushPdfDocumentEvent extends AbstractITextConfigurationEvent {
    private static final Logger LOGGER = Logger.getLogger(FlushPdfDocumentEvent.class.getName());

    private final WeakReference<PdfDocument> document;

    /**
     * Creates a new instance of the flushing event.
     *
     * @param document is a document to be flushed
     */
    public FlushPdfDocumentEvent(PdfDocument document) {
        super();
        this.document = new WeakReference<>(document);
    }

    /**
     * Prepares document for flushing.
     */
    @Override
    protected void doAction() {
        final PdfDocument pdfDocument = (PdfDocument) document.get();
        if (pdfDocument == null) {
            return;
        }
        List<AbstractProductProcessITextEvent> events = getEvents(pdfDocument.getDocumentIdWrapper());

        if (events == null || events.isEmpty()) {
            final ProductData productData = ITextCoreProductData.getInstance();
            final String noEventProducer = "iText\u00ae \u00a9" + productData.getSinceCopyrightYear() + "-"
                    + productData.getToCopyrightYear() + " iText Group NV (no registered products)";
            pdfDocument.getDocumentInfo().setProducer(noEventProducer);
            return;
        }

        final Set<String> products = new HashSet<>();
        for (final AbstractProductProcessITextEvent event : events) {
            pdfDocument.getFingerPrint().registerProduct(event.getProductData());
            if (event.getConfirmationType() == EventConfirmationType.ON_CLOSE) {
                EventManager.getInstance().onEvent(new ConfirmEvent(pdfDocument.getDocumentIdWrapper(), event));
            }
            products.add(event.getProductName());
        }

        for (final String product : products) {
            final ITextProductEventProcessor processor = getActiveProcessor(product);
            if (processor == null) {
                LOGGER.warning(MessageFormatUtil.format(KernelLogMessageConstant.UNKNOWN_PRODUCT_INVOLVED, product));
            }
        }

        final String oldProducer = pdfDocument.getDocumentInfo().getProducer();
        final String newProducer =
                ProducerBuilder.modifyProducer(getConfirmedEvents(pdfDocument.getDocumentIdWrapper()), oldProducer);
        pdfDocument.getDocumentInfo().setProducer(newProducer);
    }

    private List<ConfirmedEventWrapper> getConfirmedEvents(SequenceId sequenceId) {
        final List<AbstractProductProcessITextEvent> events = getEvents(sequenceId);
        final List<ConfirmedEventWrapper> confirmedEvents = new ArrayList<>();
        for (AbstractProductProcessITextEvent event : events) {
            if (event instanceof ConfirmedEventWrapper) {
                confirmedEvents.add((ConfirmedEventWrapper) event);
            } else {
                LOGGER.warning(MessageFormatUtil.format(KernelLogMessageConstant.UNCONFIRMED_EVENT,
                        event.getProductName(), event.getEventType()));
            }
        }
        return confirmedEvents;
    }
}
