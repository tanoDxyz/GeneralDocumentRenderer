package com.tanodxyz.itext722g;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BitmapExt {
    private final Bitmap bitmap;

    public BitmapExt(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void recycle() {
        try {
            this.bitmap.recycle();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public boolean isBlackAndWhite() {
        boolean isBlackAndWhite = true;
        final int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int pixel : pixels) {
            if (pixel != Color.WHITE && pixel != Color.BLACK) {
                isBlackAndWhite = false;
                break;
            }
        }
        return isBlackAndWhite;
    }

    public int[] getPixels() {
        return new int[bitmap.getWidth() * bitmap.getHeight()];
    }
}
