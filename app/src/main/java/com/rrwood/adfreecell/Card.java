package com.rrwood.adfreecell;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import static com.rrwood.adfreecell.CardSVGSuitValueInfo.CardSuit;
import static com.rrwood.adfreecell.CardSVGSuitValueInfo.CardSuitColour;
import static com.rrwood.adfreecell.CardSVGSuitValueInfo.CardAction;


public class Card {
    static final String TAG = "Card";
    static private Paint hilitePaint = null;
    static private Paint grayPaint = null;

    static {
        Card.hilitePaint = new Paint();
        Card.hilitePaint.setARGB(255, 240, 240, 0);

        ColorFilter filter = new LightingColorFilter(0xffdddddd, 0);
        Card.grayPaint = new Paint();
        Card.grayPaint.setColorFilter(filter);
    }

    private CardSuit cardSuit = null;
    private int cardVal = -1;

    private Rect cardRect = null;
    private SVGImage cardSVImage = null;
    private boolean cardIsMoving = false;
    private boolean cardIsSrcCard = false;
    private boolean cardIsDstCard = false;
    private Animator cardMotionAnimation = null;
    private Animator cardHiliteAnimation = null;
    private CardAction cardLastAction = CardAction.NO_ACTION;
    private int cardHiliteAlpha = 0;

    public Card(CardSuit cardSuit, int val, Drawable drawable) {
        this.cardSVImage = new SVGImage(drawable);
        this.cardRect = new Rect();
        this.cardSuit = cardSuit;
        this.cardVal = val;
    }

    public CardSuit getCardSuit() {
        return this.cardSuit;
    }

    public CardSuitColour getSuitColour() {
        return this.cardSuit.getColour();
    }

    public int getCardVal() {
        return this.cardVal;
    }

    public boolean isMoving() {
        return this.cardIsMoving;
    }

    public void setMoving(boolean moving) {
        this.cardIsMoving = moving;
    }

    public Animator getMotionAnimation() {
        return this.cardMotionAnimation;
    }

    public void setMotionAnimation(Animator animation) {
        this.cardMotionAnimation = animation;
    }

    public Animator getHiliteAnimation() {
        return this.cardHiliteAnimation;
    }

    public void setHiliteAnimation(Animator animation) {
        this.cardHiliteAnimation = animation;
    }

    public boolean isSrcCard() {
        return this.cardIsSrcCard;
    }

    public void setIsSrcCard(boolean hilite) {
        this.cardIsSrcCard = hilite;
    }

    public boolean isDstCard() {
        return this.cardIsDstCard;
    }

    public void setIsDstCard(boolean hilite) {
        this.cardIsDstCard = hilite;
    }

    public int getHeight() {
        return this.cardRect.bottom - this.cardRect.top;
    }

    public int getWidth() {
        return this.cardRect.right - this.cardRect.left;
    }

    public void moveTo(int left, int top) {
        this.setCardRect(left, top, left + this.getWidth(), top + this.getHeight());
    }

    public void drawCard(Canvas canvas) {
        if (this.cardIsDstCard) {
            Card.hilitePaint.setAlpha(this.cardHiliteAlpha);
            RectF hiliteRect = new RectF(this.cardRect);
            hiliteRect.inset(-5.0f, -5.0f);
            canvas.drawRoundRect(hiliteRect, 6.0f, 6.0f, Card.hilitePaint);
        }

        if (this.cardIsSrcCard) {
            this.cardSVImage.drawSelf(canvas, Card.grayPaint);
        }
        else {
            this.cardSVImage.drawSelf(canvas);
        }
    }

    public boolean contains(int x, int y) {
        return this.cardRect.contains(x, y);
    }

    public CardAction getLastAction() {
        return this.cardLastAction;
    }

    public void setLastAction(CardAction action) {
        this.cardLastAction = action;
    }

    public Rect getCardRect() {
        return new Rect(this.cardRect);
    }

    public void setCardRect(Rect cardRect) {
        this.setCardRect(cardRect.left, cardRect.top, cardRect.right, cardRect.bottom);
    }

    public void setCardRect(int left, int top, int right, int bottom) {
        Log.d(TAG,"setCardRect: Moving card, left=" + left);
        if (this.cardRect.left == left && this.cardRect.right == right && this.cardRect.top == top && this.cardRect.bottom == bottom) {
            return;
        }

        this.cardRect.set(left, top, right, bottom);
        this.cardSVImage.setRect(left, top, right, bottom);
    }

    public int getCardHiliteAlpha() {
        return this.cardHiliteAlpha;
    }

    public void setCardHiliteAlpha(int cardHiliteAlpha) {
        this.cardHiliteAlpha = cardHiliteAlpha;
    }
}