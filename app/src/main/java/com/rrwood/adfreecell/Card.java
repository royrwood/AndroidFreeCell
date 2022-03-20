package com.rrwood.adfreecell;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;


/**
 * A class representing a playing card
 */
public class Card {
    static final String TAG = "Card";

    static final String SUIT_NAME_CLUBS = "Clubs";
    static final String SUIT_NAME_HEARTS = "Hearts";
    static final String SUIT_NAME_DIAMONDS = "Diamonds";
    static final String SUIT_NAME_SPADES = "Spades";

    static final int CARD_VALUE_ACE = 1;
    static final int CARD_VALUE_TWO = 2;
    static final int CARD_VALUE_THREE = 3;
    static final int CARD_VALUE_FOUR = 4;
    static final int CARD_VALUE_FIVE = 5;
    static final int CARD_VALUE_SIX = 6;
    static final int CARD_VALUE_SEVEN = 7;
    static final int CARD_VALUE_EIGHT = 8;
    static final int CARD_VALUE_NINE = 9;
    static final int CARD_VALUE_TEN = 10;
    static final int CARD_VALUE_JACK = 11;
    static final int CARD_VALUE_QUEEN = 12;
    static final int CARD_VALUE_KING = 13;

    static final int NUM_CARDS_PER_SUIT = 13;


    enum CardSuitColour {
        BLACK, RED;
    }

    enum CardSuit {
        CLUBS(CardSuitColour.BLACK, Card.SUIT_NAME_CLUBS),
        DIAMONDS(CardSuitColour.RED, Card.SUIT_NAME_DIAMONDS),
        SPADES(CardSuitColour.BLACK, Card.SUIT_NAME_SPADES),
        HEARTS(CardSuitColour.RED, Card.SUIT_NAME_HEARTS);

        private final CardSuitColour colour;
        private final String name;

        CardSuit(CardSuitColour colour, String name) {
            this.colour = colour;
            this.name = name;
        }

        public CardSuitColour getColour() {
            return colour;
        }

        public boolean sameColour(CardSuit otherSuit) {
            return (colour == otherSuit.getColour());
        }

        public String getName() {
            return name;
        }
    }

    enum CardAction {
        NO_ACTION, MOUSE_DOWN, MOUSE_UP, MOUSE_DRAG
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
    private Paint hilitePaint = null;
    private Paint grayPaint = null;

    public Card(CardSuit cardSuit, int val, Drawable drawable) {
        this.cardSVImage = new SVGImage(drawable);
        this.cardRect = new Rect();
        this.cardSuit = cardSuit;
        this.cardVal = val;

        this.hilitePaint = new Paint();
        this.hilitePaint.setARGB(255, 240, 240, 0);

        ColorFilter filter = new LightingColorFilter(0xffdddddd, 0);
        this.grayPaint = new Paint();
        this.grayPaint.setColorFilter(filter);
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
            this.hilitePaint.setAlpha(this.cardHiliteAlpha);
            RectF hiliteRect = new RectF(this.cardRect);
            hiliteRect.inset(-5.0f, -5.0f);
            canvas.drawRoundRect(hiliteRect, 6.0f, 6.0f, this.hilitePaint);
        }

        if (this.cardIsSrcCard) {
            this.cardSVImage.drawSelf(canvas, this.grayPaint);
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