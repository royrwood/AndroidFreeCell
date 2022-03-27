package com.rrwood.adfreecell;

public class CardSVGSuitValueInfo {
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

    enum CardAction {
        NO_ACTION, MOUSE_DOWN, MOUSE_UP, MOUSE_DRAG
    }

    enum CardSuitColour {
        BLACK, RED;
    }

    enum CardSuit {
        CLUBS(CardSuitColour.BLACK, SUIT_NAME_CLUBS),
        DIAMONDS(CardSuitColour.RED, SUIT_NAME_DIAMONDS),
        SPADES(CardSuitColour.BLACK, SUIT_NAME_SPADES),
        HEARTS(CardSuitColour.RED, SUIT_NAME_HEARTS);

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

    static final CardSVGSuitValueInfo[] CARD_SVG_LOAD_INFO = {
        new CardSVGSuitValueInfo(R.drawable.ace_diamonds, CardSuit.DIAMONDS, CARD_VALUE_ACE),
        new CardSVGSuitValueInfo(R.drawable.two_diamonds, CardSuit.DIAMONDS, CARD_VALUE_TWO),
        new CardSVGSuitValueInfo(R.drawable.three_diamonds, CardSuit.DIAMONDS, CARD_VALUE_THREE),
        new CardSVGSuitValueInfo(R.drawable.four_diamonds, CardSuit.DIAMONDS, CARD_VALUE_FOUR),
        new CardSVGSuitValueInfo(R.drawable.five_diamonds, CardSuit.DIAMONDS, CARD_VALUE_FIVE),
        new CardSVGSuitValueInfo(R.drawable.six_diamonds, CardSuit.DIAMONDS, CARD_VALUE_SIX),
        new CardSVGSuitValueInfo(R.drawable.seven_diamonds, CardSuit.DIAMONDS, CARD_VALUE_SEVEN),
        new CardSVGSuitValueInfo(R.drawable.eight_diamonds, CardSuit.DIAMONDS, CARD_VALUE_EIGHT),
        new CardSVGSuitValueInfo(R.drawable.nine_diamonds, CardSuit.DIAMONDS, CARD_VALUE_NINE),
        new CardSVGSuitValueInfo(R.drawable.ten_diamonds, CardSuit.DIAMONDS, CARD_VALUE_TEN),
        new CardSVGSuitValueInfo(R.drawable.jack_diamonds, CardSuit.DIAMONDS, CARD_VALUE_JACK),
        new CardSVGSuitValueInfo(R.drawable.queen_diamonds, CardSuit.DIAMONDS, CARD_VALUE_QUEEN),
        new CardSVGSuitValueInfo(R.drawable.king_diamonds, CardSuit.DIAMONDS, CARD_VALUE_KING),

        new CardSVGSuitValueInfo(R.drawable.ace_clubs, CardSuit.CLUBS, CARD_VALUE_ACE),
        new CardSVGSuitValueInfo(R.drawable.two_clubs, CardSuit.CLUBS, CARD_VALUE_TWO),
        new CardSVGSuitValueInfo(R.drawable.three_clubs, CardSuit.CLUBS, CARD_VALUE_THREE),
        new CardSVGSuitValueInfo(R.drawable.four_clubs, CardSuit.CLUBS, CARD_VALUE_FOUR),
        new CardSVGSuitValueInfo(R.drawable.five_clubs, CardSuit.CLUBS, CARD_VALUE_FIVE),
        new CardSVGSuitValueInfo(R.drawable.six_clubs, CardSuit.CLUBS, CARD_VALUE_SIX),
        new CardSVGSuitValueInfo(R.drawable.seven_clubs, CardSuit.CLUBS, CARD_VALUE_SEVEN),
        new CardSVGSuitValueInfo(R.drawable.eight_clubs, CardSuit.CLUBS, CARD_VALUE_EIGHT),
        new CardSVGSuitValueInfo(R.drawable.nine_clubs, CardSuit.CLUBS, CARD_VALUE_NINE),
        new CardSVGSuitValueInfo(R.drawable.ten_clubs, CardSuit.CLUBS, CARD_VALUE_TEN),
        new CardSVGSuitValueInfo(R.drawable.jack_clubs, CardSuit.DIAMONDS, CARD_VALUE_JACK),
        new CardSVGSuitValueInfo(R.drawable.queen_clubs, CardSuit.DIAMONDS, CARD_VALUE_QUEEN),
        new CardSVGSuitValueInfo(R.drawable.king_clubs, CardSuit.DIAMONDS, CARD_VALUE_KING),

        new CardSVGSuitValueInfo(R.drawable.ace_hearts, CardSuit.HEARTS, CARD_VALUE_ACE),
        new CardSVGSuitValueInfo(R.drawable.two_hearts, CardSuit.HEARTS, CARD_VALUE_TWO),
        new CardSVGSuitValueInfo(R.drawable.three_hearts, CardSuit.HEARTS, CARD_VALUE_THREE),
        new CardSVGSuitValueInfo(R.drawable.four_hearts, CardSuit.HEARTS, CARD_VALUE_FOUR),
        new CardSVGSuitValueInfo(R.drawable.five_hearts, CardSuit.HEARTS, CARD_VALUE_FIVE),
        new CardSVGSuitValueInfo(R.drawable.six_hearts, CardSuit.HEARTS, CARD_VALUE_SIX),
        new CardSVGSuitValueInfo(R.drawable.seven_hearts, CardSuit.HEARTS, CARD_VALUE_SEVEN),
        new CardSVGSuitValueInfo(R.drawable.eight_hearts, CardSuit.HEARTS, CARD_VALUE_EIGHT),
        new CardSVGSuitValueInfo(R.drawable.nine_hearts, CardSuit.HEARTS, CARD_VALUE_NINE),
        new CardSVGSuitValueInfo(R.drawable.ten_hearts, CardSuit.HEARTS, CARD_VALUE_TEN),
        new CardSVGSuitValueInfo(R.drawable.jack_hearts, CardSuit.DIAMONDS, CARD_VALUE_JACK),
        new CardSVGSuitValueInfo(R.drawable.queen_hearts, CardSuit.DIAMONDS, CARD_VALUE_QUEEN),
        new CardSVGSuitValueInfo(R.drawable.king_hearts, CardSuit.DIAMONDS, CARD_VALUE_KING),

        new CardSVGSuitValueInfo(R.drawable.ace_spades, CardSuit.SPADES, CARD_VALUE_ACE),
        new CardSVGSuitValueInfo(R.drawable.two_spades, CardSuit.SPADES, CARD_VALUE_TWO),
        new CardSVGSuitValueInfo(R.drawable.three_spades, CardSuit.SPADES, CARD_VALUE_THREE),
        new CardSVGSuitValueInfo(R.drawable.four_spades, CardSuit.SPADES, CARD_VALUE_FOUR),
        new CardSVGSuitValueInfo(R.drawable.five_spades, CardSuit.SPADES, CARD_VALUE_FIVE),
        new CardSVGSuitValueInfo(R.drawable.six_spades, CardSuit.SPADES, CARD_VALUE_SIX),
        new CardSVGSuitValueInfo(R.drawable.seven_spades, CardSuit.SPADES, CARD_VALUE_SEVEN),
        new CardSVGSuitValueInfo(R.drawable.eight_spades, CardSuit.SPADES, CARD_VALUE_EIGHT),
        new CardSVGSuitValueInfo(R.drawable.nine_spades, CardSuit.SPADES, CARD_VALUE_NINE),
        new CardSVGSuitValueInfo(R.drawable.ten_spades, CardSuit.SPADES, CARD_VALUE_TEN),
        new CardSVGSuitValueInfo(R.drawable.jack_spades, CardSuit.DIAMONDS, CARD_VALUE_JACK),
        new CardSVGSuitValueInfo(R.drawable.queen_spades, CardSuit.DIAMONDS, CARD_VALUE_QUEEN),
        new CardSVGSuitValueInfo(R.drawable.king_spades, CardSuit.DIAMONDS, CARD_VALUE_KING),
    };


    int resourceID;
    CardSuit cardSuit;
    int cardValue;

    CardSVGSuitValueInfo(int resourceID, CardSuit cardSuit, int cardValue) {
        this.resourceID = resourceID;
        this.cardSuit = cardSuit;
        this.cardValue = cardValue;
    }
}
