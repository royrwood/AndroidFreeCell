package com.rrwood.adfreecell;

public class CardSVGInfo {

    static final CardSVGInfo[] CARD_SVG_LOAD_INFO = {
        new CardSVGInfo(R.drawable.two_of_diamonds_svg, Card.CardSuit.DIAMONDS, Card.CARD_VALUE_TWO),
        new CardSVGInfo(R.drawable.three_of_diamonds_svg, Card.CardSuit.DIAMONDS, Card.CARD_VALUE_THREE),

        new CardSVGInfo(R.drawable.two_of_clubs_svg, Card.CardSuit.CLUBS, Card.CARD_VALUE_TWO),
        new CardSVGInfo(R.drawable.three_of_clubs_svg, Card.CardSuit.CLUBS, Card.CARD_VALUE_THREE),

        new CardSVGInfo(R.drawable.two_of_hearts_svg, Card.CardSuit.HEARTS, Card.CARD_VALUE_TWO),
        new CardSVGInfo(R.drawable.three_of_hearts_svg, Card.CardSuit.HEARTS, Card.CARD_VALUE_THREE),

        new CardSVGInfo(R.drawable.two_of_spades_svg, Card.CardSuit.SPADES, Card.CARD_VALUE_TWO),
        new CardSVGInfo(R.drawable.three_of_spades_svg, Card.CardSuit.SPADES, Card.CARD_VALUE_THREE),
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
