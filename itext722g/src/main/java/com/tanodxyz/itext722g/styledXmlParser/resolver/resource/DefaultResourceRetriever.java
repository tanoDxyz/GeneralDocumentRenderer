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
package com.tanodxyz.itext722g.styledXmlParser.resolver.resource;


import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.io.util.StreamUtil;
import com.tanodxyz.itext722g.io.util.UrlUtil;
import com.tanodxyz.itext722g.styledXmlParser.exceptions.ReadingByteLimitException;
import com.tanodxyz.itext722g.styledXmlParser.logs.StyledXmlParserLogMessageConstant;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Default implementation of the {@link IResourceRetriever} interface, which can set a limit
 * on the size of retrieved resources using input stream with a limit on the number of bytes read.
 */
public class DefaultResourceRetriever implements IResourceRetriever {
    private static final Logger logger = Logger.getLogger(DefaultResourceRetriever.class.getName());

    private long resourceSizeByteLimit;

    /**
     * Creates a new {@link DefaultResourceRetriever} instance.
     * The limit on the size of retrieved resources is by default equal to {@link Long#MAX_VALUE} bytes.
     */
    public DefaultResourceRetriever() {
        resourceSizeByteLimit = Long.MAX_VALUE;
    }

    /**
     * Gets the resource size byte limit.
     *
     * The resourceSizeByteLimit is used to create input stream with a limit on the number of bytes read.
     *
     * @return the resource size byte limit
     */
    public long getResourceSizeByteLimit() {
        return resourceSizeByteLimit;
    }

    /**
     * Sets the resource size byte limit.
     *
     * The resourceSizeByteLimit is used to create input stream with a limit on the number of bytes read.
     *
     * @param resourceSizeByteLimit the resource size byte limit
     * @return the {@link IResourceRetriever} instance
     */
    public  IResourceRetriever setResourceSizeByteLimit(long resourceSizeByteLimit) {
        this.resourceSizeByteLimit = resourceSizeByteLimit;
        return this;
    }

    /**
     * Gets the input stream with current limit on the number of bytes read,
     * that connect with source URL for retrieving data from that connection.
     *
     * @param url the source URL
     * @return the limited input stream or null if the URL was filtered
     */
    public InputStream getInputStreamByUrl(URL url) throws IOException {
        if (!urlFilter(url)) {
            logger.warning(
                    MessageFormatUtil.format(StyledXmlParserLogMessageConstant.RESOURCE_WITH_GIVEN_URL_WAS_FILTERED_OUT,
                            url));
            return null;
        }
        return new  LimitedInputStream(UrlUtil.getInputStreamOfFinalConnection(url), resourceSizeByteLimit);
    }

    /**
     * Gets the byte array that are retrieved from the source URL.
     *
     * @param url the source URL
     * @return the byte array or null if the retrieving failed or the
     * URL was filtered or the resourceSizeByteLimit was violated
     */
    public byte[] getByteArrayByUrl(URL url) throws IOException {
        try (InputStream stream = getInputStreamByUrl(url)){
            if (stream == null) {
                return null;
            }

            return StreamUtil.inputStreamToArray(stream);
        } catch (ReadingByteLimitException ex) {
            logger.warning(MessageFormatUtil.format(
                    StyledXmlParserLogMessageConstant.UNABLE_TO_RETRIEVE_RESOURCE_WITH_GIVEN_RESOURCE_SIZE_BYTE_LIMIT,
                    url, resourceSizeByteLimit));
        }
        return null;
    }

    /**
     * Method for filtering resources by URL.
     *
     * The default implementation allows for all URLs. Override this method if want to set filtering.
     *
     * @param url the source URL
     * @return true if the resource can be retrieved and false otherwise
     */
    protected boolean urlFilter(URL url) {
        return true;
    }
}
