package com.tanodxyz.itext722g.styledXmlParser;

import com.tanodxyz.itext722g.io.exceptions.IOException;
import com.tanodxyz.itext722g.styledXmlParser.node.IDocumentNode;

import java.io.InputStream;

/**
 * Interface for the XML parsing operations that accept XML and return a document node.
 */
public interface IXmlParser {

    /**
     * Parses XML provided as an {@code InputStream} and an encoding.
     *
     * @param XmlStream the Xml stream
     * @param charset the character set. If {@code null} then parser should detect encoding from stream.
     * @return a document node
     * @throws IOException Signals that an I/O exception has occurred.
     */
     IDocumentNode parse(InputStream XmlStream, String charset) throws IOException, java.io.IOException;

    /**
     * Parses XML provided as a {@code String}.
     *
     * @param Xml the Xml string
     * @return a document node
     */
     IDocumentNode parse(String Xml);

}
