package com.tanodxyz.itext722g.styledXmlParser;


import com.tanodxyz.itext722g.styledXmlParser.css.CssRuleSet;
import com.tanodxyz.itext722g.styledXmlParser.css.selector.CssSelectorComparator;

import java.util.Comparator;


/**
 * Comparator class used to sort CSS rule set objects.
 */
public class CssRuleSetComparator implements Comparator<CssRuleSet> {

    /** The selector comparator. */
    private CssSelectorComparator selectorComparator = new CssSelectorComparator();

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(CssRuleSet o1, CssRuleSet o2) {
        return selectorComparator.compare(o1.getSelector(), o2.getSelector());
    }
}
