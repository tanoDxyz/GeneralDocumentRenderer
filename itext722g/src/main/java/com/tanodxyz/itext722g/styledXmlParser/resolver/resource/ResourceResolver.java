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
package com.tanodxyz.itext722g.styledXmlParser.resolver.resource;


import com.tanodxyz.itext722g.commons.utils.Base64;
import com.tanodxyz.itext722g.commons.utils.MessageFormatUtil;
import com.tanodxyz.itext722g.io.image.ImageDataFactory;
import com.tanodxyz.itext722g.kernel.pdf.xobject.PdfFormXObject;
import com.tanodxyz.itext722g.kernel.pdf.xobject.PdfImageXObject;
import com.tanodxyz.itext722g.kernel.pdf.xobject.PdfXObject;
import com.tanodxyz.itext722g.styledXmlParser.logs.StyledXmlParserLogMessageConstant;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities class to resolve resources.
 */
public class ResourceResolver {

    /**
     * Identifier string used when loading in base64 images.
     */
    public static final String BASE64_IDENTIFIER = "base64";

    /**
     * Identifier string used to detect that the source is under data URI scheme.
     */
    public static final String DATA_SCHEMA_PREFIX = "data:";

    private static final Logger logger = Logger.getLogger(ResourceResolver.class.getName());

    /**
     * The {@link UriResolver} instance.
     */
    private  UriResolver uriResolver;

    /**
     * The {@link SimpleImageCache} instance.
     */
    private  SimpleImageCache imageCache;

    private IResourceRetriever retriever;

    /**
     * Creates a new {@link ResourceResolver} instance.
     * If {@code baseUri} is a string that represents an absolute URI with any schema except "file" - resources
     * url values will be resolved exactly as "new URL(baseUrl, uriString)". Otherwise base URI will be handled
     * as path in local file system.
     * <p>
     * If empty string or relative URI string is passed as base URI, then it will be resolved against current
     * working directory of this application instance.
     *
     * @param baseUri base URI against which all relative resource URIs will be resolved
     */
    public ResourceResolver(String baseUri) {
        this(baseUri, null);
    }

    /**
     * Creates a new {@link ResourceResolver} instance.
     * If {@code baseUri} is a string that represents an absolute URI with any schema except "file" - resources
     * url values will be resolved exactly as "new URL(baseUrl, uriString)". Otherwise base URI will be handled
     * as path in local file system.
     * <p>
     * If empty string or relative URI string is passed as base URI, then it will be resolved against current
     * working directory of this application instance.
     *
     * @param baseUri base URI against which all relative resource URIs will be resolved
     * @param retriever the resource retriever with the help of which data from resources will be retrieved
     */
    public ResourceResolver(String baseUri, IResourceRetriever retriever) {
        if (baseUri == null) {
            baseUri = "";
        }
        this.uriResolver = new  UriResolver(baseUri);
        this.imageCache = new  SimpleImageCache();

        if (retriever == null) {
            this.retriever = new DefaultResourceRetriever();
        } else {
            this.retriever = retriever;
        }
    }

    /**
     * Gets the resource retriever.
     *
     * The retriever is used to retrieve data from resources by URL.
     *
     * @return the resource retriever
     */
    public IResourceRetriever getRetriever() {
        return retriever;
    }

    /**
     * Sets the resource retriever.
     *
     * The retriever is used to retrieve data from resources by URL.
     *
     * @param retriever the resource retriever
     * @return the {@link ResourceResolver} instance
     */
    public ResourceResolver setRetriever(IResourceRetriever retriever) {
        this.retriever = retriever;
        return this;
    }

    /**
     * Retrieve image as either {@link PdfImageXObject}, or {@link PdfFormXObject}.
     *
     * @param src either link to file or base64 encoded stream
     * @return PdfXObject on success, otherwise null
     */
    public PdfXObject retrieveImage(String src) {
        if (src != null) {
            if (isContains64Mark(src)) {
                PdfXObject imageXObject = tryResolveBase64ImageSource(src);
                if (imageXObject != null) {
                    return imageXObject;
                }
            }

            PdfXObject imageXObject = tryResolveUrlImageSource(src);
            if (imageXObject != null) {
                return imageXObject;
            }
        }
        if (isDataSrc(src)) {
            logger.log(Level.SEVERE,MessageFormatUtil.format(
                    StyledXmlParserLogMessageConstant.UNABLE_TO_RETRIEVE_IMAGE_WITH_GIVEN_DATA_URI, src));
        } else {
            logger.log(Level.SEVERE,MessageFormatUtil.format(
                    StyledXmlParserLogMessageConstant.UNABLE_TO_RETRIEVE_IMAGE_WITH_GIVEN_BASE_URI,
                    uriResolver.getBaseUri(), src));
        }
        return null;
    }

    /**
     * Retrieve a resource as a byte array from a source that
     * can either be a link to a file, or a base64 encoded {@link String}.
     *
     * @param src either link to file or base64 encoded stream
     * @return byte[] on success, otherwise null
     */
    public byte[] retrieveBytesFromResource(String src) {
        byte[] bytes = retrieveBytesFromBase64Src(src);
        if (bytes != null) {
            return bytes;
        }

        try {
            URL url = uriResolver.resolveAgainstBaseUri(src);
            return retriever.getByteArrayByUrl(url);
        } catch (Exception e) {
            logger.log(Level.SEVERE,MessageFormatUtil.format(
                    StyledXmlParserLogMessageConstant.UNABLE_TO_RETRIEVE_STREAM_WITH_GIVEN_BASE_URI,
                    uriResolver.getBaseUri(), src), e);
            return null;
        }
    }

    /**
     * Retrieve the resource found in src as an InputStream
     *
     * @param src path to the resource
     * @return InputStream for the resource on success, otherwise null
     */
    public InputStream retrieveResourceAsInputStream(String src) {
        byte[] bytes = retrieveBytesFromBase64Src(src);
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }

        try {
            URL url = uriResolver.resolveAgainstBaseUri(src);
            return retriever.getInputStreamByUrl(url);
        } catch (Exception e) {
            logger.log(Level.SEVERE,MessageFormatUtil.format(
                    StyledXmlParserLogMessageConstant.UNABLE_TO_RETRIEVE_STREAM_WITH_GIVEN_BASE_URI,
                    uriResolver.getBaseUri(), src), e);
            return null;
        }
    }

    /**
     * Checks if source is under data URI scheme. (eg data:[&lt;media type&gt;][;base64],&lt;data&gt;).
     *
     * @param src string to test
     * @return true if source is under data URI scheme
     */
    public static boolean isDataSrc(String src) {
        return src != null && src.toLowerCase().startsWith(DATA_SCHEMA_PREFIX) && src.contains(",");
    }

    /**
     * Resolves a given URI against the base URI.
     *
     * @param uri the uri
     * @return the url
     * @throws MalformedURLException the malformed URL exception
     */
    public URL resolveAgainstBaseUri(String uri) throws MalformedURLException {
        return uriResolver.resolveAgainstBaseUri(uri);
    }

    /**
     * Resets the simple image cache.
     */
    public void resetCache() {
        imageCache.reset();
    }

    protected PdfXObject tryResolveBase64ImageSource(String src) {
        try {
            String fixedSrc = src.replaceAll("\\s", "");
            fixedSrc = fixedSrc.substring(fixedSrc.indexOf(BASE64_IDENTIFIER) + BASE64_IDENTIFIER.length() + 1);
            PdfXObject imageXObject = imageCache.getImage(fixedSrc);
            if (imageXObject == null) {
                imageXObject = new PdfImageXObject(ImageDataFactory.create(Base64.decode(fixedSrc)));
                imageCache.putImage(fixedSrc, imageXObject);
            }
            return imageXObject;
        } catch (Exception ignored) {
        }
        return null;
    }

    protected PdfXObject tryResolveUrlImageSource(String uri) {
        try {
            URL url = uriResolver.resolveAgainstBaseUri(uri);
            String imageResolvedSrc = url.toExternalForm();
            PdfXObject imageXObject = imageCache.getImage(imageResolvedSrc);
            if (imageXObject == null) {
                imageXObject = createImageByUrl(url);
                if (imageXObject != null) {
                    imageCache.putImage(imageResolvedSrc, imageXObject);
                }
            }
            return imageXObject;
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Create a iText XObject based on the image stored at the passed location.
     *
     * @param url location of the Image file.
     * @return {@link PdfXObject} containing the Image loaded in.
     * @throws Exception thrown if error occurred during fetching or constructing the image.
     */
    protected PdfXObject createImageByUrl(URL url) throws Exception {
        byte[] bytes = retriever.getByteArrayByUrl(url);
        return bytes == null ? null : new PdfImageXObject(ImageDataFactory.create(bytes));
    }

    private byte[] retrieveBytesFromBase64Src(String src) {
        if (isContains64Mark(src)) {
            try {
                String fixedSrc = src.replaceAll("\\s", "");
                fixedSrc = fixedSrc.substring(fixedSrc.indexOf(BASE64_IDENTIFIER) + BASE64_IDENTIFIER.length() + 1);
                return Base64.decode(fixedSrc);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Checks if string contains base64 mark.
     * It does not guarantee that src is a correct base64 data-string.
     *
     * @param src string to test
     * @return true if string contains base64 mark
     */
    private boolean isContains64Mark(String src) {
        return src.contains(BASE64_IDENTIFIER);
    }
}
