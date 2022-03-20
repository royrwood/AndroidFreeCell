package com.rrwood.adfreecell;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class CardStack {
    enum CardStackType {
        FREECELLSTACK, ACESTACK, GENERALSTACK
    }

    private static final String TAG = "ROYDEBUG.CardStack";

    private SVGImage emptyStackSVImage = null;
    private Card.CardSuit stackSuit = null;
    private CardStackType stackType = null;
    private ArrayList<Card> cards = null;
    private Rect baseRect = null;
    private Rect fullRect = null;
    private int cardVertOffset = 0;
    private boolean isHilighted = false;
    private Paint hilitePaint = null;


    public CardStack(Card.CardSuit suit, CardStackType stackType, Drawable emptyStackDrawable) {
        this.stackSuit = suit;
        this.stackType = stackType;
        this.cards = new ArrayList<>();
        this.baseRect = new Rect();
        this.fullRect = new Rect();

        if (emptyStackDrawable != null) {
            this.emptyStackSVImage = new SVGImage(emptyStackDrawable);
        }

        this.hilitePaint = new Paint();
        this.hilitePaint.setARGB(255, 240, 240, 0);
    }


    public boolean isHilighted() {
        return this.isHilighted;
    }

    public void setIsHilighted(boolean isHilighted) {
        this.isHilighted = isHilighted;
    }

    public Rect getRect() {
        return new Rect(fullRect);
    }

    public Card.CardSuit getSuit() {
        return this.stackSuit;
    }

    public CardStackType getStackType() {
        return this.stackType;
    }

    public ArrayList<Card> getCards() {
        return this.cards;
    }

    public int getNumCards() {
        return this.cards.size();
    }

    public void setVertOffset(int vertOffset) {
        this.cardVertOffset = vertOffset;

        calcFullRect();
    }

    public void setBaseRect(int stackLeft, int stackTop, int stackRight, int stackBottom) {
        if (this.baseRect.left == stackLeft && this.baseRect.right == stackRight && this.baseRect.top == stackTop && this.baseRect.bottom == stackBottom) {
            return;
        }

        this.baseRect.set(stackLeft, stackTop, stackRight, stackBottom);

        if (this.emptyStackSVImage != null) {
            this.emptyStackSVImage.setRect(stackLeft, stackTop, stackRight, stackBottom);
        }

        int cardTop = stackTop;
        int cardHeight = stackBottom - stackTop;
        for (Card card : this.cards) {
            card.setCardRect(stackLeft, cardTop, stackRight, cardTop + cardHeight);
            cardTop += this.cardVertOffset;
        }

        calcFullRect();
    }

    void calcFullRect() {
        int numCards = this.cards.size();

        if (numCards > 0) {
            Card finalCard = this.cards.get(numCards - 1);
            int finalCardBottom = this.baseRect.top + this.cardVertOffset * (numCards - 1) + finalCard.getHeight();
            this.fullRect.set(this.baseRect.left, this.baseRect.top, this.baseRect.right, finalCardBottom);
        }
        else {
            this.fullRect.set(this.baseRect.left, this.baseRect.top, this.baseRect.right, this.baseRect.bottom);
        }
    }

    public void drawStaticCards(Canvas canvas) {
        if (this.isHilighted()) {
            RectF hiliteRect = new RectF(this.fullRect.left - 3, this.fullRect.top - 3, this.fullRect.right + 3, this.fullRect.bottom + 3);
            this.hilitePaint.setAlpha(255);
            canvas.drawRoundRect(hiliteRect, 6.0f, 6.0f, this.hilitePaint);
        }

        // Always draw empty stack image, since a card could be in motion and not yet landed on the stack
        if (this.emptyStackSVImage != null) {
            this.emptyStackSVImage.drawSelf(canvas);
        }
        else {
            Paint outLinePaint = new Paint();
            outLinePaint.setColor(Color.BLACK);
            outLinePaint.setStyle(Paint.Style.STROKE);
            outLinePaint.setStrokeWidth(2);

            RectF insetRect = new RectF(this.baseRect);
            insetRect.inset(5.0f, 5.0f);
            canvas.drawRoundRect(insetRect, 6.0f, 6.0f, outLinePaint);
        }

        for (Card card : this.cards) {
            if (!card.isMoving()) {
                card.drawCard(canvas);
            }
        }
    }

    public void drawMovingCards(Canvas canvas) {
        for (Card card : this.cards) {
            if (card.isMoving()) {
                card.drawCard(canvas);
            }
        }
    }

    public Point getLeftTop() {
        return new Point(this.fullRect.left, this.fullRect.top);
    }

    public boolean containsPoint(int x, int y) {
        return this.fullRect.contains(x, y);
    }

    public Point getNextCardLocation() {
        int cardTop = this.baseRect.top + this.cardVertOffset * this.cards.size();
        return new Point(this.baseRect.left, cardTop);
    }

    public void pushCard(Card card, boolean updateCardLocation) {
        if (updateCardLocation) {
            // Set the destination of the card and let it handle the animation movement
            int cardTop = this.baseRect.top + this.cardVertOffset * this.cards.size();
            card.moveTo(this.baseRect.left, cardTop);
        }

        this.cards.add(card);

        calcFullRect();
    }

    public Card popCard() {
        Card topCard = null;
        int numCards = this.cards.size();

        if (numCards > 0) {
            topCard = this.cards.remove(numCards - 1);
        }

        calcFullRect();

        return topCard;
    }


    public Card topCard() {
        Card topCard = null;
        int numCards = this.cards.size();

        if (numCards > 0) {
            topCard = this.cards.get(numCards - 1);
        }

        return topCard;
    }


    public void removeAllCards() {
        this.cards.clear();

        calcFullRect();
    }


//    public void setLeftTop(int x, int y) {
//        this.emptyStackSVImage.moveTo(x, y);
//        this.emptyRect.offsetTo(x, y);
//
//        for (Card c : this.cards) {
//            c.moveTo(x, y);
//            y += this.cardVertOffset;
//        }
//    }
}