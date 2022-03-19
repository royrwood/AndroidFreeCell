package com.rrwood.adfreecell;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Point;
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
//    private boolean isHilighted = false;


    public CardStack(Card.CardSuit suit, CardStackType stackType, Drawable emptyStackDrawable) {
        this.stackSuit = suit;
        this.stackType = stackType;
        this.cards = new ArrayList<>();
        this.baseRect = new Rect();
        this.fullRect = new Rect();

        if (emptyStackDrawable != null) {
            this.emptyStackSVImage = new SVGImage(emptyStackDrawable);
        }
    }


//    public boolean isHilighted() {
//        return this.isHilighted;
//    }
//
//    public void setIsHilighted(boolean isHilighted) {
//        this.isHilighted = isHilighted;
//    }

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
    }

    public void setBaseRect(int stackLeft, int stackTop, int stackRight, int stackBottom) {
        if (this.baseRect.left == stackLeft && this.baseRect.right == stackRight && this.baseRect.top == stackTop && this.baseRect.bottom == stackBottom) {
            return;
        }

        this.baseRect.set(stackLeft, stackTop, stackRight, stackBottom);

        if (this.emptyStackSVImage != null) {
            this.emptyStackSVImage.setRect(stackLeft, stackTop, stackRight, stackBottom);
        }

        calcFullRect();

        int cardTop = stackTop;
        int cardHeight = stackBottom - stackTop;
        for (Card card : this.cards) {
            card.setRect(stackLeft, cardTop, stackRight, cardTop + cardHeight);
            cardTop += this.cardVertOffset;
        }
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
        int numCards = this.cards.size();

        if (numCards == 0) {
            if (this.emptyStackSVImage != null) {
                this.emptyStackSVImage.drawSelf(canvas);
            }
        }
        else {
            for (Card card : this.cards) {
                if (!card.isMoving()) {
                    card.drawCard(canvas);
                }
            }
        }
    }

    public void drawMovingCards(Canvas canvas) {

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