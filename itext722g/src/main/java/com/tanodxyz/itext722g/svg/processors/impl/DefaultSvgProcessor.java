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
package com.tanodxyz.itext722g.svg.processors.impl;


import com.tanodxyz.itext722g.styledXmlParser.css.ICssResolver;
import com.tanodxyz.itext722g.styledXmlParser.jsoup.nodes.Element;
import com.tanodxyz.itext722g.styledXmlParser.jsoup.parser.Tag;
import com.tanodxyz.itext722g.styledXmlParser.node.IElementNode;
import com.tanodxyz.itext722g.styledXmlParser.node.INode;
import com.tanodxyz.itext722g.styledXmlParser.node.ITextNode;
import com.tanodxyz.itext722g.styledXmlParser.node.impl.jsoup.node.JsoupElementNode;
import com.tanodxyz.itext722g.svg.SvgConstants;
import com.tanodxyz.itext722g.svg.SvgConstants.Tags;
import com.tanodxyz.itext722g.svg.css.impl.SvgStyleResolver;
import com.tanodxyz.itext722g.svg.exceptions.SvgExceptionMessageConstant;
import com.tanodxyz.itext722g.svg.exceptions.SvgProcessingException;
import com.tanodxyz.itext722g.svg.processors.ISvgConverterProperties;
import com.tanodxyz.itext722g.svg.processors.ISvgProcessor;
import com.tanodxyz.itext722g.svg.processors.ISvgProcessorResult;
import com.tanodxyz.itext722g.svg.processors.impl.font.SvgFontProcessor;
import com.tanodxyz.itext722g.svg.renderers.IBranchSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.INoDrawSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.ISvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.factories.DefaultSvgNodeRendererFactory;
import com.tanodxyz.itext722g.svg.renderers.factories.ISvgNodeRendererFactory;
import com.tanodxyz.itext722g.svg.renderers.impl.DefsSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.ISvgTextNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.LinearGradientSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.StopSvgNodeRenderer;
import com.tanodxyz.itext722g.svg.renderers.impl.TextSvgBranchRenderer;
import com.tanodxyz.itext722g.svg.utils.SvgTextUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Default implementation of {@link ISvgProcessor}.
 * This implementation traverses the {@link INode} tree depth-first,
 * using a stack to recreate a tree of {@link ISvgNodeRenderer} with the same structure.
 */
public class DefaultSvgProcessor implements ISvgProcessor {

    private ProcessorState processorState;

    private ICssResolver cssResolver;
    private ISvgNodeRendererFactory rendererFactory;
    private Map<String, ISvgNodeRenderer> namedObjects;
    private  SvgProcessorContext context;

    /**
     * Instantiates a DefaultSvgProcessor object.
     */
    public DefaultSvgProcessor() {
    }

    @Override
    public ISvgProcessorResult process(INode root, ISvgConverterProperties converterProps) throws SvgProcessingException {
        if (root == null) {
            throw new SvgProcessingException(SvgExceptionMessageConstant.I_NODE_ROOT_IS_NULL);
        }
        if (converterProps == null) {
            converterProps = new SvgConverterProperties();
        }
        //Setup processorState
        performSetup(root, converterProps);

        //Find root
        IElementNode svgRoot = findFirstElement(root, SvgConstants.Tags.SVG);

        if (svgRoot != null) {
            //Iterate over children
            executeDepthFirstTraversal(svgRoot);
            ISvgNodeRenderer rootSvgRenderer = createResultAndClean();
            return new  SvgProcessorResult(namedObjects, rootSvgRenderer, context);
        } else {
            throw new SvgProcessingException(SvgExceptionMessageConstant.NO_ROOT);
        }
    }

    /**
     * Load in configuration, set initial processorState and create/fill-in context of the processor
     *
     * @param converterProps that contains configuration properties and operations
     */
    void performSetup(INode root, ISvgConverterProperties converterProps) {
        processorState = new  ProcessorState();
        if (converterProps.getRendererFactory() != null) {
            rendererFactory = converterProps.getRendererFactory();
        } else {
            rendererFactory = new DefaultSvgNodeRendererFactory();
        }
        context = new  SvgProcessorContext(converterProps);
        cssResolver = new SvgStyleResolver(root, context);
        new SvgFontProcessor(context).addFontFaceFonts(cssResolver);
        //TODO DEVSIX-2264
        namedObjects = new HashMap<>();
    }

    /**
     * Start the depth-first traversal of the INode tree, pushing the results on the stack
     *
     * @param startingNode node to start on
     */
    void executeDepthFirstTraversal(INode startingNode) {
        //Create and push rootNode
        if (startingNode instanceof IElementNode && !rendererFactory.isTagIgnored((IElementNode) startingNode)) {
            IElementNode rootElementNode = (IElementNode) startingNode;

            ISvgNodeRenderer startingRenderer = rendererFactory.createSvgNodeRendererForTag(rootElementNode, null);
            if (startingRenderer != null) {
                Map<String, String> attributesAndStyles = cssResolver.resolveStyles(startingNode, context.getCssContext());
                rootElementNode.setStyles(attributesAndStyles);
                startingRenderer.setAttributesAndStyles(attributesAndStyles);
                processorState.push(startingRenderer);
                for (INode rootChild : startingNode.childNodes()) {
                    visit(rootChild);
                }
            }
        }
    }

    /**
     * Extract result from internal processorState and clean up afterwards
     *
     * @return Root renderer of the processed SVG
     */
    private ISvgNodeRenderer createResultAndClean() {
        return processorState.pop();
    }

    /**
     * Recursive visit of the object tree, depth-first, processing the visited node and calling visit on its children.
     * Visit responsibilities for element nodes:
     * - Assign styles(CSS and attributes) to element
     * - Create Renderer based on element
     * - push and pop renderer to stack
     * Visit responsibilities for text nodes
     * - add text to parent object
     *
     * @param node INode to visit
     */
    private void visit(INode node) {
        if (node instanceof IElementNode) {
            IElementNode element = (IElementNode) node;

            if (!rendererFactory.isTagIgnored(element)) {
                ISvgNodeRenderer parentRenderer = processorState.top();
                ISvgNodeRenderer renderer = rendererFactory.createSvgNodeRendererForTag(element, parentRenderer);
                if (renderer != null) {
                    final Map<String, String> styles = cssResolver.resolveStyles(node, context.getCssContext());
                    // For inheritance
                    element.setStyles(styles);
                    // For drawing operations
                    renderer.setAttributesAndStyles(styles);

                    String attribute = renderer.getAttribute(SvgConstants.Attributes.ID);
                    if (attribute != null) {
                        namedObjects.put(attribute, renderer);
                    }

                    if (renderer instanceof StopSvgNodeRenderer) {
                        if (parentRenderer instanceof LinearGradientSvgNodeRenderer) {
                            // It is necessary to add StopSvgNodeRenderer only as a child of LinearGradientSvgNodeRenderer,
                            // because StopSvgNodeRenderer performs an auxiliary function and should not be drawn at all
                            ((LinearGradientSvgNodeRenderer) parentRenderer).addChild(renderer);
                        }
                    }
                    // DefsSvgNodeRenderer should not have parental relationship with any renderer, it only serves as a storage
                    else if (!(renderer instanceof INoDrawSvgNodeRenderer) && !(parentRenderer instanceof DefsSvgNodeRenderer)) {
                        if (parentRenderer instanceof IBranchSvgNodeRenderer) {
                            ((IBranchSvgNodeRenderer) parentRenderer).addChild(renderer);
                        } else if (parentRenderer instanceof TextSvgBranchRenderer && renderer instanceof ISvgTextNodeRenderer) {
                            // Text branch node renderers only accept ISvgTextNodeRenderers
                            ((TextSvgBranchRenderer) parentRenderer).addChild((ISvgTextNodeRenderer) renderer);
                        }
                    }

                    processorState.push(renderer);
                }

                for (INode childNode : element.childNodes()) {
                    visit(childNode);
                }

                if (renderer != null) {
                    processorState.pop();
                }
            }
        } else if (processAsText(node)) {
            processText((ITextNode) node);
        }
    }

    /**
     * Check if this node is a text node that needs to be processed by the parent
     *
     * @param node node to check
     * @return true if the node should be processed as text, false otherwise
     */
    private boolean processAsText(INode node) {
        return node instanceof ITextNode;
    }

    /**
     * Process the text contained in the text-node
     *
     * @param textNode node containing text to process
     */
    private void processText(ITextNode textNode) {
        ISvgNodeRenderer parentRenderer = this.processorState.top();

        if (parentRenderer instanceof TextSvgBranchRenderer) {
            String wholeText = textNode.wholeText();
            if (!"".equals(wholeText) && !SvgTextUtil.isOnlyWhiteSpace(wholeText)) {
                final IElementNode textLeafElement = new JsoupElementNode(new Element(Tag.valueOf(Tags.TEXT_LEAF), ""));
                ISvgTextNodeRenderer textLeaf = (ISvgTextNodeRenderer) this.rendererFactory
                        .createSvgNodeRendererForTag(textLeafElement, parentRenderer);
                textLeaf.setParent(parentRenderer);
                textLeaf.setAttribute(SvgConstants.Attributes.TEXT_CONTENT, wholeText);
                ((TextSvgBranchRenderer) parentRenderer).addChild(textLeaf);
            }
        }
    }

    /**
     * Find the first element in the node-tree that corresponds with the passed tag-name. Search is performed depth-first
     *
     * @param node    root-node to start with
     * @param tagName name of the tag that needs to be fonund
     * @return IElementNode
     */
    IElementNode findFirstElement(INode node, String tagName) {
        LinkedList<INode> q = new LinkedList<>();
        q.add(node);

        while (!q.isEmpty()) {
            INode currentNode = q.getFirst();
            q.removeFirst();

            if (currentNode == null) {
                return null;
            }

            if (currentNode instanceof IElementNode && ((IElementNode) currentNode).name() != null && ((IElementNode) currentNode).name().equals(tagName)) {
                return (IElementNode) currentNode;
            }

            for (INode child : currentNode.childNodes()) {
                if (child instanceof IElementNode) {
                    q.add(child);
                }
            }
        }

        return null;
    }
}
