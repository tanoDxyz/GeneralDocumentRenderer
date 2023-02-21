/*
 * This class is based on the C# open source freeware library Clipper:
 * http://www.angusj.com/delphi/clipper.php
 * The original classes were distributed under the Boost Software License:
 *
 * Freeware for both open source and commercial applications
 * Copyright 2010-2014 Angus Johnson
 * Boost Software License - Version 1.0 - August 17th, 2003
 *
 * Permission is hereby granted, free of charge, to any person or organization
 * obtaining a copy of the software and accompanying documentation covered by
 * this license (the "Software") to use, reproduce, display, distribute,
 * execute, and transmit the Software, and to prepare derivative works of the
 * Software, and to permit third-parties to whom the Software is furnished to
 * do so, all subject to the following:
 *
 * The copyright notices in the Software and this entire statement, including
 * the above license grant, this restriction and the following disclaimer,
 * must be included in all copies of the Software, in whole or in part, and
 * all derivative works of the Software, unless such copies or derivative
 * works are solely in the form of machine-executable object code generated by
 * a source language processor.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.tanodxyz.itext722g.kernel.pdf.canvas.parser.clipper;

public interface IClipper {
    enum ClipType {
        INTERSECTION, UNION, DIFFERENCE, XOR
    }

    enum Direction {
        RIGHT_TO_LEFT, LEFT_TO_RIGHT
    };

    enum EndType {
        CLOSED_POLYGON, CLOSED_LINE, OPEN_BUTT, OPEN_SQUARE, OPEN_ROUND
    };

    enum JoinType {
        BEVEL, ROUND, MITER
    };

    enum PolyFillType {
        EVEN_ODD, NON_ZERO, POSITIVE, NEGATIVE
    };

    enum PolyType {
        SUBJECT, CLIP
    };

    interface IZFillCallback {
        void zFill(Point.LongPoint bot1, Point.LongPoint top1, Point.LongPoint bot2, Point.LongPoint top2, Point.LongPoint pt);
    };

    //InitOptions that can be passed to the constructor ...
    int REVERSE_SOLUTION = 1;

    int STRICTLY_SIMPLE = 2;

    int PRESERVE_COLINEAR = 4;

    boolean addPath(Path pg, PolyType polyType, boolean Closed);

    boolean addPaths(Paths ppg, PolyType polyType, boolean closed);

    void clear();

    boolean execute(ClipType clipType, Paths solution);

    boolean execute(ClipType clipType, Paths solution, PolyFillType subjFillType, PolyFillType clipFillType);

    boolean execute(ClipType clipType, PolyTree polytree);

    boolean execute(ClipType clipType, PolyTree polytree, PolyFillType subjFillType, PolyFillType clipFillType);
}
