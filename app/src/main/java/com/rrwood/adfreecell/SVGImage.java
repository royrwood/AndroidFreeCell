package com.rrwood.adfreecell;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.graphics.drawable.DrawableCompat;

public class SVGImage {
    private Drawable drawable = null;
    private Bitmap bitmap = null;
    private final Rect rect = new Rect();


    public SVGImage(Drawable drawable) {
        this.drawable = drawable;
    }

    public int getWidth() {
        return this.rect.right - this.rect.left;
    }

    public int getHeight() {
        return this.rect.bottom - this.rect.top;
    }

    public Rect getRect() {
        return new Rect(rect);
    }

    public Point getLeftTop() {
        return new Point(rect.left, rect.top);
    }

    public boolean containsPt(int x, int y) {
        return rect.contains(x, y);
    }

    public void centerAt(int x, int y, int sideLength) {
        int left = x - sideLength / 2;
        int right = left + sideLength;
        int top = y - sideLength / 2;
        int bottom = top + sideLength;

        this.setRect(left, top, right, bottom);
    }

    public void setRect(int left, int top, int right, int bottom) {
        int prevWidth = this.rect.right - this.rect.left;
        int prevHeight = this.rect.bottom - this.rect.top;
        int newWidth = right - left;
        int newHeight = bottom - top;

        this.rect.set(left, top, right, bottom);

        if (this.drawable != null && (prevWidth != newWidth || prevHeight != newHeight)) {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                drawable = (DrawableCompat.wrap(drawable)).mutate();
//            }
            Bitmap tempBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(tempBitmap);
            drawable.setBounds(0, 0, newWidth, newHeight);
            drawable.draw(tempCanvas);
            this.bitmap = tempBitmap;
        }
    }

    public void moveTo(int x, int y) {
        this.rect.offsetTo(x, y);
    }

    public void drawSelf(Canvas canvas) {
        if (this.bitmap != null) {
            canvas.drawBitmap(this.bitmap, this.rect.left, this.rect.top, null);
        }
    }
}
