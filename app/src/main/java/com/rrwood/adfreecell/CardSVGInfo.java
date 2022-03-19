package com.rrwood.adfreecell;

public class CardSVGInfo {

    static final CardSVGInfo[] CARD_SVG_LOAD_INFO = {
        new CardSVGInfo(R.drawable.ace_diamonds, Card.CardSuit.DIAMONDS, Card.CARD_VALUE_ACE),
        new CardSVGInfo(R.drawable.two_diamonds, Card.CardSuit.DIAMONDS, Card.CARD_VALUE_TWO),
        new CardSVGInfo(R.drawable.three_diamonds, Card.CardSuit.DIAMONDS, Card.CARD_VALUE_THREE),
        new CardSVGInfo(R.drawable.four_diamonds, Card.CardSuit.DIAMONDS, Card.CARD_VALUE_FOUR),
        new CardSVGInfo(R.drawable.five_diamonds, Card.CardSuit.DIAMONDS, Card.CARD_VALUE_FIVE),

        new CardSVGInfo(R.drawable.ace_clubs, Card.CardSuit.CLUBS, Card.CARD_VALUE_ACE),
        new CardSVGInfo(R.drawable.two_clubs, Card.CardSuit.CLUBS, Card.CARD_VALUE_TWO),
        new CardSVGInfo(R.drawable.three_clubs, Card.CardSuit.CLUBS, Card.CARD_VALUE_THREE),
        new CardSVGInfo(R.drawable.four_clubs, Card.CardSuit.CLUBS, Card.CARD_VALUE_FOUR),
        new CardSVGInfo(R.drawable.five_clubs, Card.CardSuit.CLUBS, Card.CARD_VALUE_FIVE),

        new CardSVGInfo(R.drawable.ace_hearts, Card.CardSuit.HEARTS, Card.CARD_VALUE_ACE),
        new CardSVGInfo(R.drawable.two_hearts, Card.CardSuit.HEARTS, Card.CARD_VALUE_TWO),
        new CardSVGInfo(R.drawable.three_hearts, Card.CardSuit.HEARTS, Card.CARD_VALUE_THREE),
        new CardSVGInfo(R.drawable.four_hearts, Card.CardSuit.HEARTS, Card.CARD_VALUE_FOUR),
        new CardSVGInfo(R.drawable.five_hearts, Card.CardSuit.HEARTS, Card.CARD_VALUE_FIVE),

        new CardSVGInfo(R.drawable.ace_spades, Card.CardSuit.SPADES, Card.CARD_VALUE_ACE),
        new CardSVGInfo(R.drawable.two_spades, Card.CardSuit.SPADES, Card.CARD_VALUE_TWO),
        new CardSVGInfo(R.drawable.three_spades, Card.CardSuit.SPADES, Card.CARD_VALUE_THREE),
        new CardSVGInfo(R.drawable.four_spades, Card.CardSuit.SPADES, Card.CARD_VALUE_FOUR),
        new CardSVGInfo(R.drawable.five_spades, Card.CardSuit.SPADES, Card.CARD_VALUE_FIVE),
    };


    int resourceID;
    Card.CardSuit cardSuit;
    int cardValue;

    CardSVGInfo(int resourceID, Card.CardSuit cardSuit, int cardValue) {
        this.resourceID = resourceID;
        this.cardSuit = cardSuit;
        this.cardValue = cardValue;
    }
}
