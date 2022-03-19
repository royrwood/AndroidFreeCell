package com.rrwood.adfreecell;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;


/**
 * A class representing a playing card
 */
public class Card {
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


    public Card(CardSuit cardSuit, int val, Drawable drawable) {
        this.cardSVImage = new SVGImage(drawable);
        this.cardRect = new Rect();
        this.cardSuit = cardSuit;
        this.cardVal = val;
    }

    public CardSuit getCardSuit() {
        return cardSuit;
    }

    public CardSuitColour getSuitColour() {
        return cardSuit.getColour();
    }

    public int getCardVal() {
        return cardVal;
    }

    public boolean isMoving() {
        return cardIsMoving;
    }

    public void setMoving(boolean moving) {
        cardIsMoving = moving;
    }

    public Animator getMotionAnimation() {
        return cardMotionAnimation;
    }

    public void setMotionAnimation(Animator animation) {
        cardMotionAnimation = animation;
    }

    public Animator getHiliteAnimation() {
        return cardHiliteAnimation;
    }

    public void setHiliteAnimation(Animator animation) {
        cardHiliteAnimation = animation;
    }

    public boolean isSrcCard() {
        return cardIsSrcCard;
    }

    public void setIsSrcCard(boolean hilite) {
        cardIsSrcCard = hilite;
    }

    public boolean isDstCard() {
        return cardIsDstCard;
    }

    public void setIsDstCard(boolean hilite) {
        cardIsDstCard = hilite;
    }

    public int getHeight() {
        return this.cardRect.bottom - this.cardRect.top;
    }

    public int getWidth() {
        return this.cardRect.right - this.cardRect.left;
    }

    public void moveTo(int x, int y) {
        cardRect.offsetTo(x, y);
    }

    public Rect getRect() {
        return new Rect(cardRect);
    }

    public void setRect(int cardLeft, int cardTop, int cardRight, int cardBottom) {
        if (this.cardRect.left == cardLeft && this.cardRect.right == cardTop && this.cardRect.top == cardRight && this.cardRect.bottom == cardBottom) {
            return;
        }

        this.cardRect.set(cardLeft, cardTop, cardRight, cardBottom);
        this.cardSVImage.setRect(cardLeft, cardTop, cardRight, cardBottom);
    }

    public void drawCard(Canvas canvas) {
        this.cardSVImage.drawSelf(canvas);
    }

    public boolean contains(int x, int y) {
        return cardRect.contains(x, y);
    }

    public CardAction getLastAction() {
        return cardLastAction;
    }

    public void setLastAction(CardAction action) {
        cardLastAction = action;
    }

    public int getHiliteAlpha() {
        return cardHiliteAlpha;
    }

    public void setHiliteAlpha(int alpha) {
        cardHiliteAlpha = alpha;
    }
}