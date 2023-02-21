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
package com.tanodxyz.itext722g.svg.utils;


import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.geom.Vector;
import com.tanodxyz.itext722g.layout.properties.UnitValue;
import com.tanodxyz.itext722g.styledXmlParser.css.util.CssDimensionParsingUtils;
import com.tanodxyz.itext722g.styledXmlParser.css.util.CssTypesValidationUtils;
import com.tanodxyz.itext722g.svg.SvgConstants;
import com.tanodxyz.itext722g.svg.SvgConstants.Values;
import com.tanodxyz.itext722g.svg.exceptions.SvgExceptionMessageConstant;

public class SvgCoordinateUtils {

    /**
     * Converts relative coordinates to absolute ones. Assumes that relative coordinates are represented by
     * an array of coordinates with length proportional to the length of current coordinates array,
     * so that current coordinates array is applied in segments to the relative coordinates array
     *
     * @param relativeCoordinates the initial set of coordinates
     * @param currentCoordinates  an array representing the point relative to which the relativeCoordinates are defined
     * @return a String array of absolute coordinates, with the same length as the input array
     */
    public static String[] makeRelativeOperatorCoordinatesAbsolute(String[] relativeCoordinates,
            double[] currentCoordinates) {
        if (relativeCoordinates.length % currentCoordinates.length != 0) {
            throw new IllegalArgumentException(
                    SvgExceptionMessageConstant.COORDINATE_ARRAY_LENGTH_MUST_BY_DIVISIBLE_BY_CURRENT_COORDINATES_ARRAY_LENGTH);
        }
        String[] absoluteOperators = new String[relativeCoordinates.length];

        for (int i = 0; i < relativeCoordinates.length; ) {
            for (int j = 0; j < currentCoordinates.length; j++, i++) {
                double relativeDouble = Double.parseDouble(relativeCoordinates[i]);
                relativeDouble += currentCoordinates[j];
                absoluteOperators[i] = SvgCssUtils.convertDoubleToString(relativeDouble);
            }
        }

        return absoluteOperators;
    }

    /**
     * Calculate the angle between two vectors
     *
     * @param vectorA first vector
     * @param vectorB second vector
     * @return angle between vectors in radians units
     */
    public static double calculateAngleBetweenTwoVectors(Vector vectorA, Vector vectorB) {
        return Math.acos((double) vectorA.dot(vectorB) / ((double) vectorA.length() * (double) vectorB.length()));
    }

    /**
     * Returns absolute value for attribute in userSpaceOnUse coordinate system.
     *
     * @param attributeValue value of attribute.
     * @param defaultValue   default value.
     * @param start          start border for calculating percent value.
     * @param length         length for calculating percent value.
     * @param em             em value.
     * @param rem            rem value.
     * @return absolute value in the userSpaceOnUse coordinate system.
     */
    public static double getCoordinateForUserSpaceOnUse(String attributeValue, double defaultValue,
            double start, double length, float em, float rem) {
        double absoluteValue;
        final UnitValue unitValue = CssDimensionParsingUtils.parseLengthValueToPt(attributeValue, em, rem);
        if (unitValue == null) {
            absoluteValue = defaultValue;
        } else if (unitValue.getUnitType() == UnitValue.PERCENT) {
            absoluteValue = start + (length * unitValue.getValue() / 100);
        } else {
            absoluteValue = unitValue.getValue();
        }
        return absoluteValue;
    }

    /**
     * Returns a value relative to the object bounding box.
     * We should only call this method for attributes with coordinates relative to the object bounding rectangle.
     *
     * @param attributeValue attribute value to parse
     * @param defaultValue   this value will be returned if an error occurs while parsing the attribute value
     * @return if {@code attributeValue} is a percentage value, the given percentage of 1 will be returned.
     * And if it's a valid value with a number, the number will be extracted from that value.
     */
    public static double getCoordinateForObjectBoundingBox(String attributeValue, double defaultValue) {
        if (CssTypesValidationUtils.isPercentageValue(attributeValue)) {
            return CssDimensionParsingUtils.parseRelativeValue(attributeValue, 1);
        }
        if (CssTypesValidationUtils.isNumber(attributeValue)
                || CssTypesValidationUtils.isMetricValue(attributeValue)
                || CssTypesValidationUtils.isRelativeValue(attributeValue)) {
            // if there is incorrect value metric, then we do not need to parse the value
            int unitsPosition = CssDimensionParsingUtils.determinePositionBetweenValueAndUnit(attributeValue);
            if (unitsPosition > 0) {
                // We want to ignore the unit type how this is done in the "Google Chrome" approach
                // which treats the "abstract coordinate system" in the coordinate metric measure,
                // i.e. for value '0.5cm' the top/left of the object bounding box would be (1cm, 1cm),
                // for value '0.5em' the top/left of the object bounding box would be (1em, 1em) and etc.
                // no null pointer should be thrown as determine
                return CssDimensionParsingUtils.parseDouble(attributeValue.substring(0, unitsPosition))
                        .doubleValue();
            }
        }
        return defaultValue;
    }

    /**
     * Returns the viewBox received after scaling and displacement given preserveAspectRatio.
     *
     * @param viewBox         parsed viewBox rectangle. It should be a valid {@link Rectangle}
     * @param currentViewPort current element view port. It should be a valid {@link Rectangle}
     * @param align           the alignment value that indicates whether to force uniform scaling
     *                        and, if so, the alignment method to use in case the aspect ratio of
     *                        the viewBox doesn't match the aspect ratio of the viewport. If align
     *                        is {@code null} or align is invalid (i.e. not in the predefined list),
     *                        then the default logic with align = "xMidYMid", and meetOrSlice = "meet" would be used
     * @param meetOrSlice     the way to scale the viewBox. If meetOrSlice is not {@code null} and invalid,
     *                        then the default logic with align = "xMidYMid"
     *                        and meetOrSlice = "meet" would be used, if meetOrSlice is {@code null}
     *                        then default "meet" value would be used with the specified align
     * @return the applied viewBox {@link Rectangle}
     */
    public static Rectangle applyViewBox(Rectangle viewBox, Rectangle currentViewPort, String align,
            String meetOrSlice) {
        if (currentViewPort == null) {
            throw new IllegalArgumentException(SvgExceptionMessageConstant.CURRENT_VIEWPORT_IS_NULL);
        }

        if (viewBox == null || viewBox.getWidth() <= 0 || viewBox.getHeight() <= 0) {
            throw new IllegalArgumentException(SvgExceptionMessageConstant.VIEWBOX_IS_INCORRECT);
        }

        if (align == null || (
                meetOrSlice != null && !Values.MEET.equals(meetOrSlice) && !Values.SLICE.equals(meetOrSlice)
        )) {
            return applyViewBox(viewBox, currentViewPort, Values.XMID_YMID, Values.MEET);
        }

        double scaleWidth;
        double scaleHeight;
        if (Values.NONE.equalsIgnoreCase(align)) {
            scaleWidth = (double) currentViewPort.getWidth() / (double) viewBox.getWidth();
            scaleHeight = (double) currentViewPort.getHeight() / (double) viewBox.getHeight();
        } else {
            double scale = getScaleWidthHeight(viewBox, currentViewPort, meetOrSlice);
            scaleWidth = scale;
            scaleHeight = scale;
        }

        // apply scale
        Rectangle appliedViewBox = new Rectangle(viewBox.getX(), viewBox.getY(),
                (float) ((double) viewBox.getWidth() * scaleWidth),
                (float) ((double) viewBox.getHeight() * scaleHeight));

        double minXOffset = (double) currentViewPort.getX() - (double) appliedViewBox.getX();
        double minYOffset = (double) currentViewPort.getY() - (double) appliedViewBox.getY();

        double midXOffset = (double) currentViewPort.getX() + ((double) currentViewPort.getWidth() / 2)
                - ((double) appliedViewBox.getX() + ((double) appliedViewBox.getWidth() / 2));
        double midYOffset = (double) currentViewPort.getY() + ((double) currentViewPort.getHeight() / 2)
                - ((double) appliedViewBox.getY() + ((double) appliedViewBox.getHeight() / 2));

        double maxXOffset = (double) currentViewPort.getX() + (double) currentViewPort.getWidth()
                - ((double) appliedViewBox.getX() + (double) appliedViewBox.getWidth());
        double maxYOffset = (double) currentViewPort.getY() + (double) currentViewPort.getHeight()
                - ((double) appliedViewBox.getY() + (double) appliedViewBox.getHeight());
        
        double xOffset;
        double yOffset;

        switch (align.toLowerCase()) {
            case SvgConstants.Values.NONE:
            case SvgConstants.Values.XMIN_YMIN:
                xOffset = minXOffset;
                yOffset = minYOffset;
                break;
            case SvgConstants.Values.XMIN_YMID:
                xOffset = minXOffset;
                yOffset = midYOffset;
                break;
            case SvgConstants.Values.XMIN_YMAX:
                xOffset = minXOffset;
                yOffset = maxYOffset;
                break;
            case SvgConstants.Values.XMID_YMIN:
                xOffset = midXOffset;
                yOffset = minYOffset;
                break;
            case SvgConstants.Values.XMID_YMAX:
                xOffset = midXOffset;
                yOffset = maxYOffset;
                break;
            case SvgConstants.Values.XMAX_YMIN:
                xOffset = maxXOffset;
                yOffset = minYOffset;
                break;
            case SvgConstants.Values.XMAX_YMID:
                xOffset = maxXOffset;
                yOffset = midYOffset;
                break;
            case SvgConstants.Values.XMAX_YMAX:
                xOffset = maxXOffset;
                yOffset = maxYOffset;
                break;
            case SvgConstants.Values.XMID_YMID:
                xOffset = midXOffset;
                yOffset = midYOffset;
                break;
            default:
                return applyViewBox(viewBox, currentViewPort, Values.XMID_YMID, Values.MEET);
        }

        // apply offset
        appliedViewBox.moveRight((float) xOffset);
        appliedViewBox.moveUp((float) yOffset);

        return appliedViewBox;
    }

    private static double getScaleWidthHeight(Rectangle viewBox, Rectangle currentViewPort,
            String meetOrSlice) {
        double scaleWidth = (double) currentViewPort.getWidth() / (double) viewBox.getWidth();
        double scaleHeight = (double) currentViewPort.getHeight() / (double) viewBox.getHeight();
        if (Values.SLICE.equalsIgnoreCase(meetOrSlice)) {
            return Math.max(scaleWidth, scaleHeight);
        } else if (Values.MEET.equalsIgnoreCase(meetOrSlice) || meetOrSlice == null) {
            return Math.min(scaleWidth, scaleHeight);
        } else {
            // This code should be unreachable. We check for incorrect cases
            // in the applyViewBox method and instead use the default implementation (xMidYMid meet).
            throw new IllegalStateException(
                    SvgExceptionMessageConstant.MEET_OR_SLICE_ARGUMENT_IS_INCORRECT);
        }
    }
}
