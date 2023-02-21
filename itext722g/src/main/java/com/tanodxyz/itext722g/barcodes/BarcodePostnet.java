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
package com.tanodxyz.itext722g.barcodes;


import android.graphics.Bitmap;

import com.tanodxyz.itext722g.BitmapExt;
import com.tanodxyz.itext722g.kernel.colors.Color;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.pdf.PdfDocument;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvas;


public class BarcodePostnet extends Barcode1D {

    public static int TYPE_POSTNET = 1;
    public static int TYPE_PLANET = 2;

    /**
     * The bars for each character.
     */
    private static final byte[][] BARS =
            {
                    {1, 1, 0, 0, 0},
                    {0, 0, 0, 1, 1},
                    {0, 0, 1, 0, 1},
                    {0, 0, 1, 1, 0},
                    {0, 1, 0, 0, 1},
                    {0, 1, 0, 1, 0},
                    {0, 1, 1, 0, 0},
                    {1, 0, 0, 0, 1},
                    {1, 0, 0, 1, 0},
                    {1, 0, 1, 0, 0}
            };

    public BarcodePostnet(PdfDocument document) {
        super(document);
        // distance between bars
        n = 72f / 22f;
        // bar width
        x = 0.02f * 72f;
        // height of the tall bars
        barHeight = 0.125f * 72f;
        // height of the short bars
        size = 0.05f * 72f;
        // type of code
        codeType = TYPE_POSTNET;
    }

    /** Creates the bars for Postnet.
     * @param text the code to be created without checksum
     * @return the bars
     */
    public static byte[] getBarsPostnet(String text) {
        int total = 0;
        for (int k = text.length() - 1; k >= 0; --k) {
            int n = text.charAt(k) - '0';
            total += n;
        }
        text += (char)(((10 - (total % 10)) % 10) + '0');
        byte[] bars = new byte[text.length() * 5 + 2];
        bars[0] = 1;
        bars[bars.length - 1] = 1;
        for (int k = 0; k < text.length(); ++k) {
            int c = text.charAt(k) - '0';
            System.arraycopy(BARS[c], 0, bars, k * 5 + 1, 5);
        }
        return bars;
    }

    @Override
    public Rectangle getBarcodeSize() {
        float width = ((code.length() + 1) * 5 + 1) * n + x;
        return new Rectangle(width, barHeight);
    }

    @Override
    public void fitWidth(float width) {
        byte[] bars = getBarsPostnet(code);
        float currentWidth = getBarcodeSize().getWidth();
        x *= width / currentWidth;
        n = (width - x) / (bars.length - 1);
    }

    @Override
    public Rectangle placeBarcode(PdfCanvas canvas, Color barColor, Color textColor) {
        if (barColor != null)
            canvas.setFillColor(barColor);
        byte[] bars = getBarsPostnet(code);
        byte flip = 1;
        if (codeType == TYPE_PLANET) {
            flip = 0;
            bars[0] = 0;
            bars[bars.length - 1] = 0;
        }
        float startX = 0;
        for (int k = 0; k < bars.length; ++k) {
            canvas.rectangle(startX, 0, x - inkSpreading, bars[k] == flip ? barHeight : size);
            startX += n;
        }
        canvas.fill();
        return getBarcodeSize();
    }

    @Override
    public BitmapExt createAwtImage(int foreground, int background) {
        int f = foreground;
        int g = background;
        int barWidth = (int)x;
        if (barWidth <= 0)
            barWidth = 1;
        int barDistance = (int)n;
        if (barDistance <= barWidth)
            barDistance = barWidth + 1;
        int barShort = (int)size;
        if (barShort <= 0)
            barShort = 1;
        int barTall = (int)barHeight;
        if (barTall <= barShort)
            barTall = barShort + 1;
        int width = ((code.length() + 1) * 5 + 1) * barDistance + barWidth;
        int[] pix = new int[width * barTall];
        byte[] bars = getBarsPostnet(code);
        byte flip = 1;
        if (codeType == TYPE_PLANET) {
            flip = 0;
            bars[0] = 0;
            bars[bars.length - 1] = 0;
        }
        int idx = 0;
        for (int k = 0; k < bars.length; ++k) {
            boolean dot = (bars[k] == flip);
            for (int j = 0; j < barDistance; ++j) {
                pix[idx + j] = ((dot && j < barWidth) ? f : g);
            }
            idx += barDistance;
        }
        int limit = width * (barTall - barShort);
        for (int k = width; k < limit; k += width)
            System.arraycopy(pix, 0, pix, k, width);
        idx = limit;
        for (int k = 0; k < bars.length; ++k) {
            for (int j = 0; j < barDistance; ++j) {
                pix[idx + j] = ((j < barWidth) ? f : g);
            }
            idx += barDistance;
        }
        for (int k = limit + width; k < pix.length; k += width)
            System.arraycopy(pix, limit, pix, k, width);
        Bitmap bitmap = Bitmap.createBitmap(pix, 0, width, width, barTall, Bitmap.Config.ARGB_8888);
        return new BitmapExt(bitmap);
    }
}
