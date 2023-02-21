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
package com.tanodxyz.itext722g.styledXmlParser.jsoup.select;


import com.tanodxyz.itext722g.styledXmlParser.jsoup.helper.Validate;
import com.tanodxyz.itext722g.styledXmlParser.jsoup.nodes.Element;
import com.tanodxyz.itext722g.styledXmlParser.jsoup.nodes.Node;

/**
 * Depth-first node traversor. Use to iterate through all nodes under and including the specified root node.
 * <p>
 * This implementation does not use recursion, so a deep DOM does not risk blowing the stack.
 */
public class NodeTraversor {
    /**
     * Start a depth-first traverse of the root and all of its descendants.
     * @param visitor Node visitor.
     * @param root the root node point to traverse.
     */
    public static void traverse( NodeVisitor visitor, Node root) {
        Node node = root;
        Node parent; // remember parent to find nodes that get replaced in .head
        int depth = 0;
        
        while (node != null) {
            parent = node.parentNode();
            visitor.head(node, depth); // visit current node
            if (parent != null && !node.hasParent()) // must have been replaced; find replacement
                node = parent.childNode(node.siblingIndex()); // replace ditches parent but keeps sibling index

            if (node.childNodeSize() > 0) { // descend
                node = node.childNode(0);
                depth++;
            } else {
                while (true) {
                    assert node != null; // as depth > 0, will have parent
                    if (!(node.nextSibling() == null && depth > 0)) break;
                    visitor.tail(node, depth); // when no more siblings, ascend
                    node = node.parentNode();
                    depth--;
                }
                visitor.tail(node, depth);
                if (node == root)
                    break;
                node = node.nextSibling();
            }
        }
    }

    /**
     * Start a depth-first traverse of all elements.
     * @param visitor Node visitor.
     * @param elements Elements to filter.
     */
    public static void traverse( NodeVisitor visitor, Elements elements) {
        Validate.notNull(visitor);
        Validate.notNull(elements);
        for (Element el : elements)
            traverse(visitor, el);
    }

    /**
     * Start a depth-first filtering of the root and all of its descendants.
     * @param filter Node visitor.
     * @param root the root node point to traverse.
     * @return The filter result of the root node, or {@link NodeFilter.FilterResult#STOP}.
     */
    public static NodeFilter.FilterResult filter(NodeFilter filter, Node root) {
        Node node = root;
        int depth = 0;

        while (node != null) {
            NodeFilter.FilterResult result = filter.head(node, depth);
            if (result == NodeFilter.FilterResult.STOP)
                return result;
            // Descend into child nodes:
            if (result == NodeFilter.FilterResult.CONTINUE && node.childNodeSize() > 0) {
                node = node.childNode(0);
                ++depth;
                continue;
            }
            // No siblings, move upwards:
            while (true) {
                assert node != null; // depth > 0, so has parent
                if (!(node.nextSibling() == null && depth > 0)) break;
                // 'tail' current node:
                if (result == NodeFilter.FilterResult.CONTINUE || result == NodeFilter.FilterResult.SKIP_CHILDREN) {
                    result = filter.tail(node, depth);
                    if (result == NodeFilter.FilterResult.STOP)
                        return result;
                }
                Node prev = node; // In case we need to remove it below.
                node = node.parentNode();
                depth--;
                if (result == NodeFilter.FilterResult.REMOVE)
                    prev.remove(); // Remove AFTER finding parent.
                result = NodeFilter.FilterResult.CONTINUE; // Parent was not pruned.
            }
            // 'tail' current node, then proceed with siblings:
            if (result == NodeFilter.FilterResult.CONTINUE || result == NodeFilter.FilterResult.SKIP_CHILDREN) {
                result = filter.tail(node, depth);
                if (result == NodeFilter.FilterResult.STOP)
                    return result;
            }
            if (node == root)
                return result;
            Node prev = node; // In case we need to remove it below.
            node = node.nextSibling();
            if (result == NodeFilter.FilterResult.REMOVE)
                prev.remove(); // Remove AFTER finding sibling.
        }
        // root == null?
        return NodeFilter.FilterResult.CONTINUE;
    }

    /**
     * Start a depth-first filtering of all elements.
     * @param filter Node filter.
     * @param elements Elements to filter.
     */
    public static void filter(NodeFilter filter, Elements elements) {
        Validate.notNull(filter);
        Validate.notNull(elements);
        for (Element el : elements)
            if (filter(filter, el) == NodeFilter.FilterResult.STOP)
                break;
    }
}
