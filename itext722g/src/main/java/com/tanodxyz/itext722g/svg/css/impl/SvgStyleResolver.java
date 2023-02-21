/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

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
package com.tanodxyz.itext722g.svg.css.impl;


import com.tanodxyz.itext722g.io.util.DecimalFormatUtil;
import com.tanodxyz.itext722g.io.util.ResourceUtil;
import com.tanodxyz.itext722g.styledXmlParser.css.CommonCssConstants;
import com.tanodxyz.itext722g.styledXmlParser.css.CssDeclaration;
import com.tanodxyz.itext722g.styledXmlParser.css.CssFontFaceRule;
import com.tanodxyz.itext722g.styledXmlParser.css.CssStatement;
import com.tanodxyz.itext722g.styledXmlParser.css.CssStyleSheet;
import com.tanodxyz.itext722g.styledXmlParser.css.ICssResolver;
import com.tanodxyz.itext722g.styledXmlParser.css.media.CssMediaRule;
import com.tanodxyz.itext722g.styledXmlParser.css.media.MediaDeviceDescription;
import com.tanodxyz.itext722g.styledXmlParser.css.parse.CssRuleSetParser;
import com.tanodxyz.itext722g.styledXmlParser.css.parse.CssStyleSheetParser;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.AbstractCssContext;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.CssDefaults;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.CssInheritance;
import com.tanodxyz.itext722g.styledXmlParser.css.resolve.IStyleInheritance;
import com.tanodxyz.itext722g.styledXmlParser.css.util.CssDimensionParsingUtils;
import com.tanodxyz.itext722g.styledXmlParser.css.util.CssTypesValidationUtils;
import com.tanodxyz.itext722g.styledXmlParser.css.util.CssUtils;
import com.tanodxyz.itext722g.styledXmlParser.logs.StyledXmlParserLogMessageConstant;
import com.tanodxyz.itext722g.styledXmlParser.node.IAttribute;
import com.tanodxyz.itext722g.styledXmlParser.node.IDataNode;
import com.tanodxyz.itext722g.styledXmlParser.node.IElementNode;
import com.tanodxyz.itext722g.styledXmlParser.node.INode;
import com.tanodxyz.itext722g.styledXmlParser.node.IStylesContainer;
import com.tanodxyz.itext722g.styledXmlParser.node.ITextNode;
import com.tanodxyz.itext722g.styledXmlParser.resolver.resource.ResourceResolver;
import com.tanodxyz.itext722g.styledXmlParser.util.StyleUtil;
import com.tanodxyz.itext722g.svg.SvgConstants;
import com.tanodxyz.itext722g.svg.SvgConstants.Tags;
import com.tanodxyz.itext722g.svg.css.SvgCssContext;
import com.tanodxyz.itext722g.svg.exceptions.SvgProcessingException;
import com.tanodxyz.itext722g.svg.logs.SvgLogMessageConstant;
import com.tanodxyz.itext722g.svg.processors.impl.SvgProcessorContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of SVG`s styles and attribute resolver .
 */
public class SvgStyleResolver implements ICssResolver {
    // It is necessary to cast parameters asList method to IStyleInheritance to C# compiler understand which types is used
    public static final Set<IStyleInheritance> INHERITANCE_RULES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList((IStyleInheritance) new CssInheritance(), (IStyleInheritance) new SvgAttributeInheritance())));

    // TODO: DEVSIX-3923 remove normalization (.toLowerCase)
    private static final String[] ELEMENTS_INHERITING_PARENT_STYLES = new String[]{
            Tags.MARKER, Tags.LINEAR_GRADIENT, Tags.LINEAR_GRADIENT.toLowerCase(), Tags.PATTERN
    };

    private static final float DEFAULT_FONT_SIZE = CssDimensionParsingUtils.parseAbsoluteFontSize(
            CssDefaults.getDefaultValue(SvgConstants.Attributes.FONT_SIZE));

    private static final Logger LOGGER = Logger.getLogger(SvgStyleResolver.class.getName());

    private CssStyleSheet css;
    private static final String DEFAULT_CSS_PATH = "default.css";
    private boolean isFirstSvgElement = true;

    /**
     * The device description.
     */
    private MediaDeviceDescription deviceDescription;

    /**
     * The list of fonts.
     */
    private final List<CssFontFaceRule> fonts = new ArrayList<>();

    /**
     * The resource resolver
     */
    private final ResourceResolver resourceResolver;

    /**
     * Creates a {@link SvgStyleResolver} with a given default CSS.
     *
     * @param defaultCssStream the default CSS
     * @param context the processor context
     * @throws IOException if any input/output issue occurs
     */
    public SvgStyleResolver(InputStream defaultCssStream, SvgProcessorContext context) throws IOException {
        this.css = CssStyleSheetParser.parse(defaultCssStream);
        this.resourceResolver = context.getResourceResolver();
    }

    /**
     * Creates a {@link SvgStyleResolver}.
     *
     * @param context the processor context
     */
    public SvgStyleResolver(SvgProcessorContext context) {
        try (InputStream defaultCss = ResourceUtil.getResourceStream(DEFAULT_CSS_PATH)) {
            this.css = CssStyleSheetParser.parse(defaultCss);
        } catch (IOException e) {
            LOGGER.warning(SvgLogMessageConstant.ERROR_INITIALIZING_DEFAULT_CSS + " " +e);
            this.css = new CssStyleSheet();
        }
        this.resourceResolver = context.getResourceResolver();
    }

    /**
     * Creates a {@link SvgStyleResolver}. This constructor will instantiate its internal
     * style sheet and it will collect the css declarations from the provided node.
     *
     * @param rootNode node to collect css from
     * @param context the processor context
     */
    public SvgStyleResolver(INode rootNode, SvgProcessorContext context) {
        // TODO DEVSIX-2060. Fetch default styles first.
        this.deviceDescription = context.getDeviceDescription();
        this.resourceResolver = context.getResourceResolver();
        collectCssDeclarations(rootNode, this.resourceResolver);
        collectFonts();
    }

    public static void resolveFontSizeStyle(Map<String, String> styles, SvgCssContext cssContext, String parentFontSizeStr) {
        String elementFontSize = styles.get(SvgConstants.Attributes.FONT_SIZE);
        String resolvedFontSize;
        if (CssTypesValidationUtils.isNegativeValue(elementFontSize)) {
            elementFontSize = parentFontSizeStr;
        }

        if (CssTypesValidationUtils.isRelativeValue(elementFontSize) || CommonCssConstants.LARGER.equals(elementFontSize)
                || CommonCssConstants.SMALLER.equals(elementFontSize)) {
            float baseFontSize;
            if (CssTypesValidationUtils.isRemValue(elementFontSize)) {
                baseFontSize = cssContext == null ? DEFAULT_FONT_SIZE : cssContext.getRootFontSize();
            } else if (parentFontSizeStr == null) {
                baseFontSize = CssDimensionParsingUtils.parseAbsoluteFontSize(
                        CssDefaults.getDefaultValue(SvgConstants.Attributes.FONT_SIZE));
            } else {
                baseFontSize = CssDimensionParsingUtils.parseAbsoluteLength(parentFontSizeStr);
            }

            final float absoluteFontSize = CssDimensionParsingUtils.parseRelativeFontSize(elementFontSize, baseFontSize);
            // Format to 4 decimal places to prevent differences between Java and C#
            resolvedFontSize = DecimalFormatUtil.formatNumber(absoluteFontSize, "0.####");
        } else if (elementFontSize == null){
            resolvedFontSize = DecimalFormatUtil.formatNumber(DEFAULT_FONT_SIZE, "0.####");
        } else {
            resolvedFontSize = DecimalFormatUtil.formatNumber(CssDimensionParsingUtils.parseAbsoluteFontSize(elementFontSize), "0.####");
        }
        styles.put(SvgConstants.Attributes.FONT_SIZE, resolvedFontSize + CommonCssConstants.PT);
    }

    public static boolean isElementNested(IElementNode element, String parentElementNameForSearch) {
        if (!(element.parentNode() instanceof IElementNode)) {
            return false;
        }
        final IElementNode parentElement = (IElementNode) element.parentNode();
        if (parentElement == null) {
            return false;
        }
        if (parentElement.name() != null && parentElement.name().equals(parentElementNameForSearch)) {
            return true;
        }

        return isElementNested(parentElement, parentElementNameForSearch);
    }

    @Override
    public Map<String, String> resolveStyles(INode element, AbstractCssContext context) {
        if (context instanceof SvgCssContext) {
            return resolveStyles(element, (SvgCssContext) context);
        }
        throw new SvgProcessingException(SvgLogMessageConstant.CUSTOM_ABSTRACT_CSS_CONTEXT_NOT_SUPPORTED);
    }

    /**
     * Resolves node styles without inheritance of parent element styles.
     *
     * @param node the node
     * @param cssContext the CSS context (RootFontSize, etc.)
     * @return the map containing the resolved styles that are defined in the body of the element
     */
    public Map<String, String> resolveNativeStyles(INode node, AbstractCssContext cssContext) {
        final Map<String, String> styles = new HashMap<>();
        // Load in from collected style sheets
        final List<CssDeclaration> styleSheetDeclarations = css.getCssDeclarations(node,
                MediaDeviceDescription.createDefault());
        for (CssDeclaration ssd : styleSheetDeclarations) {
            styles.put(ssd.getProperty(), ssd.getExpression());
        }

        // Load in attributes declarations
        if (node instanceof IElementNode) {
            IElementNode eNode = (IElementNode) node;
            for (IAttribute attr : eNode.getAttributes()) {
                processAttribute(attr, styles);
            }
        }
        return styles;
    }

    private static boolean onlyNativeStylesShouldBeResolved(IElementNode element) {
        for (final String elementInheritingParentStyles : ELEMENTS_INHERITING_PARENT_STYLES) {
            if (elementInheritingParentStyles.equals(element.name())
                    || SvgStyleResolver.isElementNested(element, elementInheritingParentStyles)) {
                return false;
            }
        }
        return SvgStyleResolver.isElementNested(element, Tags.DEFS);
    }

    private Map<String, String> resolveStyles(INode element, SvgCssContext context) {
        // Resolves node styles without inheritance of parent element styles
        Map<String, String> styles = resolveNativeStyles(element, context);
        if (element instanceof IElementNode && SvgStyleResolver.onlyNativeStylesShouldBeResolved((IElementNode) element)) {
            return styles;
        }

        String parentFontSizeStr = null;
        // Load in and merge inherited styles from parent
        if (element.parentNode() instanceof IStylesContainer) {
            final IStylesContainer parentNode = (IStylesContainer) element.parentNode();
            Map<String, String> parentStyles = parentNode.getStyles();

            if (parentStyles == null && !(parentNode instanceof IElementNode)) {
                LOGGER.log(Level.SEVERE,StyledXmlParserLogMessageConstant.ERROR_RESOLVING_PARENT_STYLES);
            }

            if (parentStyles != null) {
                parentFontSizeStr = parentStyles.get(SvgConstants.Attributes.FONT_SIZE);
                for (Map.Entry<String, String> entry : parentStyles.entrySet()) {
                    styles = StyleUtil.mergeParentStyleDeclaration(styles, entry.getKey(), entry.getValue(),
                            parentFontSizeStr, INHERITANCE_RULES);
                }
            }
        }

        SvgStyleResolver.resolveFontSizeStyle(styles, context, parentFontSizeStr);

        // Set root font size
        final boolean isSvgElement = element instanceof IElementNode
                && SvgConstants.Tags.SVG.equals(((IElementNode) element).name());
        if (isFirstSvgElement && isSvgElement) {
            isFirstSvgElement = false;
            final String rootFontSize = styles.get(SvgConstants.Attributes.FONT_SIZE);
            if (rootFontSize != null) {
                context.setRootFontSize(styles.get(SvgConstants.Attributes.FONT_SIZE));
            }
        }

        return styles;
    }

    /**
     * Resolves the full path of link href attribute,
     * thanks to the resource resolver.
     *
     * @param attr the attribute to process
     * @param attributesMap the element styles map
     */
    private void processXLink(final IAttribute attr, final Map<String, String> attributesMap) {
        String xlinkValue = attr.getValue();
        if (!isStartedWithHash(xlinkValue) && !ResourceResolver.isDataSrc(xlinkValue)) {
            try {
                xlinkValue = this.resourceResolver.resolveAgainstBaseUri(attr.getValue()).toExternalForm();
            } catch (MalformedURLException mue) {
                LOGGER.log(Level.SEVERE,StyledXmlParserLogMessageConstant.UNABLE_TO_RESOLVE_IMAGE_URL, mue);
            }
        }
        attributesMap.put(attr.getKey(), xlinkValue);
    }

    /**
     * Checks if string starts with #.
     *
     * @param s the test string
     * @return true if the string starts with #, otherwise false
     */
    private boolean isStartedWithHash(String s) {
        return s != null && s.startsWith("#");
    }

    private void collectCssDeclarations(INode rootNode, ResourceResolver resourceResolver) {
        this.css = new CssStyleSheet();
        LinkedList<INode> q = new LinkedList<>();
        if (rootNode != null) {
            q.add(rootNode);
        }
        while (!q.isEmpty()) {
            INode currentNode = q.pop();
            if (currentNode instanceof IElementNode) {
                IElementNode headChildElement = (IElementNode) currentNode;
                if (SvgConstants.Tags.STYLE.equals(headChildElement.name())) {
                    // XML parser will parse style tag contents as text nodes
                    if (!currentNode.childNodes().isEmpty() && (currentNode.childNodes().get(0) instanceof IDataNode ||
                            currentNode.childNodes().get(0) instanceof ITextNode)) {
                        String styleData;
                        if (currentNode.childNodes().get(0) instanceof IDataNode) {
                            styleData = ((IDataNode) currentNode.childNodes().get(0)).getWholeData();
                        } else {
                            styleData = ((ITextNode) currentNode.childNodes().get(0)).wholeText();
                        }
                        CssStyleSheet styleSheet = CssStyleSheetParser.parse(styleData);
                        // TODO (DEVSIX-2263): media query wrap
                        // styleSheet = wrapStyleSheetInMediaQueryIfNecessary(headChildElement, styleSheet);
                        this.css.appendCssStyleSheet(styleSheet);
                    }

                } else if (CssUtils.isStyleSheetLink(headChildElement)) {
                    String styleSheetUri = headChildElement.getAttribute(SvgConstants.Attributes.HREF);
                    try (InputStream stream = resourceResolver.retrieveResourceAsInputStream(styleSheetUri)) {
                        if (stream != null) {
                            CssStyleSheet styleSheet = CssStyleSheetParser.parse(stream,
                                    resourceResolver.resolveAgainstBaseUri(styleSheetUri).toExternalForm());
                            this.css.appendCssStyleSheet(styleSheet);
                        }
                    } catch (Exception exc) {
                        LOGGER.severe(StyledXmlParserLogMessageConstant.UNABLE_TO_PROCESS_EXTERNAL_CSS_FILE + " "+ exc);
                    }
                }
            }
            for (INode child : currentNode.childNodes()) {
                if (child instanceof IElementNode) {
                    q.add(child);
                }
            }
        }
    }

    /**
     * Gets the list of fonts.
     *
     * @return the list of {@link CssFontFaceRule} instances
     */
    public List<CssFontFaceRule> getFonts() {
        return new ArrayList<>(fonts);
    }

    /**
     * Collects fonts from the style sheet.
     */
    private void collectFonts() {
        for (CssStatement cssStatement : css.getStatements()) {
            collectFonts(cssStatement);
        }
    }

    /**
     * Collects fonts from a {@link CssStatement}.
     *
     * @param cssStatement the CSS statement
     */
    private void collectFonts(CssStatement cssStatement) {
        if (cssStatement instanceof CssFontFaceRule) {
            fonts.add((CssFontFaceRule) cssStatement);
        } else if (cssStatement instanceof CssMediaRule &&
                ((CssMediaRule) cssStatement).matchMediaDevice(deviceDescription)) {
            for (CssStatement cssSubStatement : ((CssMediaRule) cssStatement).getStatements()) {
                collectFonts(cssSubStatement);
            }
        }
    }

    private void processAttribute(IAttribute attr, Map<String, String> styles) {
        //Style attribute needs to be parsed further
        switch (attr.getKey()) {
            case SvgConstants.Attributes.STYLE:
                Map<String, String> parsed = parseStylesFromStyleAttribute(attr.getValue());
                for (Map.Entry<String, String> style : parsed.entrySet()) {
                    styles.put(style.getKey(), style.getValue());
                }
                break;
            case SvgConstants.Attributes.XLINK_HREF:
                processXLink(attr, styles);
                break;
            default:
                styles.put(attr.getKey(), attr.getValue());
        }
    }

    private Map<String, String> parseStylesFromStyleAttribute(String style) {
        Map<String, String> parsed = new HashMap<>();
        List<CssDeclaration> declarations = CssRuleSetParser.parsePropertyDeclarations(style);
        for (CssDeclaration declaration : declarations) {
            parsed.put(declaration.getProperty(), declaration.getExpression());
        }
        return parsed;
    }

}
