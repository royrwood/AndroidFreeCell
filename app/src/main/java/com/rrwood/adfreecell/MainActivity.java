package com.rrwood.adfreecell;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.Window;
import android.content.pm.ActivityInfo;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.rrwood.adfreecell.CardSVGSuitValueInfo.CARD_VALUE_ACE;
import static com.rrwood.adfreecell.CardSVGSuitValueInfo.CardSuit;
import static com.rrwood.adfreecell.CardSVGSuitValueInfo.CardSuitColour;
import static com.rrwood.adfreecell.CardSVGSuitValueInfo.CardAction;

import static com.rrwood.adfreecell.CardStack.CardStackType.FREECELLSTACK;
import static com.rrwood.adfreecell.CardStack.CardStackType.ACESTACK;
import static com.rrwood.adfreecell.CardStack.CardStackType.GENERALSTACK;



public class MainActivity extends Activity implements View.OnLayoutChangeListener, View.OnTouchListener, ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    static class CardMove {
        public ArrayList<Card> cards;
        public CardStack srcStack;
        public CardStack dstStack;

        public CardMove(CardStack srcStack, CardStack dstStack) {
            this.cards = new ArrayList<>();
            this.srcStack = srcStack;
            this.dstStack = dstStack;
        }
    }


    private static final String TAG = "ROYDEBUG.MainActivity";

    private static final long DOUBLE_CLICK_MILLIS = 300;

    private static final int ANIM_MOVE_MIN = 500;
    private static final int ANIM_MOVE_DELTA = 250;
    private static final int ANIM_NEWGAME_DELTA = 20;
    private static final int ANIM_MOVE_MAX = 2000;

    private final int NATURAL_SVG_CARD_WIDTH = 224;
    private final int NATURAL_SVG_CARD_HEIGHT = 313;

    static final int NUMFREECELLSTACKS = 4;
    static final int NUMACESTACKS = 4;
    static final int NUMGENERALSTACKS = 8;


    private GameView gameView = null;

    private final Random random = new Random();

    private ArrayList<Card> cards = null;
    private HashMap<Animator, Card> movingCards = null;

    private ArrayList<CardStack> freecellStacks = null;
    private ArrayList<CardStack> aceStacks = null;
    private ArrayList<CardStack> generalStacks = null;
    private ArrayList<CardStack> allStacks = null;

    private ArrayList<CardMove> cardMovesForUndo = null;

    private SVGImage restartSVGImage = null;
    private SVGImage undoSVGImage = null;

    private Card currentlySelectedCard = null;
    private int currentlySelectedCardVal = -1;
    private CardSuit currentlySelectedCardSuit = null;
    private CardSuitColour currentlySelectedCardSuitColour = null;
    private CardStack currentlySelectedCardStack = null;
    private CardStack.CardStackType currentlySelectedCardStackType = null;

    // TODO: DO I need this?
//    private CardStack dstStack = null;

    private AudioPlayer audioPlayer = null;

    private long lastActionDownMillis = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"onCreate: Starting onCreate");

        // Set screen prefs
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Create the main GameView
        gameView = new GameView(this);
        gameView.addOnLayoutChangeListener(this);
        setContentView(gameView);
        registerForContextMenu(gameView);

        // Listen for input touch events
        gameView.setOnTouchListener(this);

        // Note display size; this is just for information since it is not necessarily the same as the view size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Point screenSize = new Point(dm.widthPixels, dm.heightPixels);
        Log.d(TAG,"onCreate: Screen size = (" + screenSize.x + "," + screenSize.y + ")");

        // We need to access our app resources to get the SVG drawables
        Resources res = this.getApplicationContext().getResources();

        // Set up the restart icon
        Drawable restartDrawable = ResourcesCompat.getDrawable(res, R.drawable.redo_arrow_svg, null);
        this.restartSVGImage = new SVGImage(restartDrawable, true);
        this.gameView.addSVImage(this.restartSVGImage);

        // Set up the undo icon
        Drawable undoDrawable = ResourcesCompat.getDrawable(res, R.drawable.undo_arrow_svg, null);
        this.undoSVGImage = new SVGImage(undoDrawable, true);
        this.gameView.addSVImage(this.undoSVGImage);

        // Set up the audio player
        this.audioPlayer = new AudioPlayer();

        // Set up the list of card moves (used during undo)
        this.cardMovesForUndo = new ArrayList<CardMove>();

        // Maintain a list of active animations and cards
        this.movingCards = new HashMap<Animator, Card>();

        // Create the cards but don't position or size them yet-- that happens in onLayoutChange, once we know the screen size
        this.cards = new ArrayList<>();
        for (CardSVGSuitValueInfo cardSVGSuitValueInfo : CardSVGSuitValueInfo.CARD_SVG_LOAD_INFO){
            Drawable cardSVGDrawable = ResourcesCompat.getDrawable(res, cardSVGSuitValueInfo.resourceID, null);
            Card card = new Card(cardSVGSuitValueInfo.cardSuit, cardSVGSuitValueInfo.cardValue, cardSVGDrawable);
            this.cards.add(card);
        }

        // Keep a list of ALL card stacks
        this.allStacks = new ArrayList<>();

        // Create the free-cell stacks and add them to the GameView (location will be set properly after GameView layout completes)
        this.freecellStacks = new ArrayList<>();
        Drawable blueCardDrawable = ResourcesCompat.getDrawable(res, R.drawable.blue_card_back_svg, null);

        for (int i = 0; i < NUMFREECELLSTACKS; i++) {
            CardStack cardStack = new CardStack(null, FREECELLSTACK, blueCardDrawable);
            this.freecellStacks.add(cardStack);
            this.allStacks.add(cardStack);
            this.gameView.addCardStack(cardStack);
        }

        // Create the ace stacks (location will be set properly after GameView layout completes)
        Drawable clubsCardDrawable = ResourcesCompat.getDrawable(res, R.drawable.face_clubs, null);
        Drawable diamondsCardDrawable = ResourcesCompat.getDrawable(res, R.drawable.face_diamonds, null);
        Drawable spadesCardDrawable = ResourcesCompat.getDrawable(res, R.drawable.face_spades, null);
        Drawable heartsCardDrawable = ResourcesCompat.getDrawable(res, R.drawable.face_hearts, null);

        this.aceStacks = new ArrayList<CardStack>();
        this.aceStacks.add(new CardStack(CardSuit.CLUBS, ACESTACK, clubsCardDrawable));
        this.aceStacks.add(new CardStack(CardSuit.DIAMONDS, ACESTACK, diamondsCardDrawable));
        this.aceStacks.add(new CardStack(CardSuit.SPADES, ACESTACK, spadesCardDrawable));
        this.aceStacks.add(new CardStack(CardSuit.HEARTS, ACESTACK, heartsCardDrawable));

        for (CardStack cardStack : this.aceStacks) {
            this.allStacks.add(cardStack);
            this.gameView.addCardStack(cardStack);
        }

        // Create the general stacks (location will be set properly after GameView layout completes)
        this.generalStacks = new ArrayList<CardStack>();
        for (int i = 0; i < NUMGENERALSTACKS; i++) {
            CardStack cardStack = new CardStack(null, GENERALSTACK, null);
            this.generalStacks.add(cardStack);
            this.allStacks.add(cardStack);
            this.gameView.addCardStack(cardStack);
        }

        // Set up initial card positions
        shuffleCards(cards);

        int stackIndex = 0;
        for (Card card : cards) {
            CardStack cardStack = this.generalStacks.get(stackIndex);
            cardStack.pushCard(card, false);
            stackIndex = (stackIndex + 1) % NUMGENERALSTACKS;
        }

        Log.d(TAG,"onCreate: Finished onCreate");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (audioPlayer != null) {
            audioPlayer.stop();
        }
    }


//    @Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    // Handle item selection
//	    switch (item.getItemId()) {
//	        case R.id.action_settings:
//	            Log.d(TAG, "Menu item selected, id=R.id.action_settings");
//	            return true;
//	        case R.id.menu_action_2:
//	        	Log.d(TAG, "Menu item selected, id=R.id.menu_action_2");
//	            return true;
//	        default:
//	            return super.onOptionsItemSelected(item);
//	    }
//	}
//
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//	    super.onCreateContextMenu(menu, v, menuInfo);
//	    MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(R.menu.context_menu, menu);
//	}
//
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//	    switch (item.getItemId()) {
//	        case R.id.context_menu_action_1:
//	        	Log.d(TAG, "Menu item selected, id=R.id.context_menu_action_1");
//	            return true;
//	        case R.id.context_menu_action_2:
//	        	Log.d(TAG, "Menu item selected, id=R.id.context_menu_action_2");
//	            return true;
//	        default:
//	            return super.onContextItemSelected(item);
//	    }
//	}


	private void doDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setMessage("End Game?");
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setPositiveButton("Restart Game", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				Log.d(TAG, "doDialog: 'Restart Game' button was clicked");
				startGame(false);
			}
		});
		alertDialogBuilder.setNeutralButton("New Game", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				Log.d(TAG, "doDialog: 'New Game' button was clicked");
				startGame(true);
			}
		});
		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				Log.d(TAG, "doDialog: 'Cancel' button was clicked");
			}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();
	}


    private void shuffleCards(ArrayList<Card> cards) {
        int numCards = cards.size();

        for (int cardIndex = numCards - 1; cardIndex >= 0; cardIndex--) {
            int swapIndex = random.nextInt(cardIndex + 1);

            Card temp = cards.get(cardIndex);
            cards.set(cardIndex, cards.get(swapIndex));
            cards.set(swapIndex, temp);
        }
    }


    private void startGame(boolean shuffleCards) {
        // Stop animations
        for (Map.Entry<Animator, Card> entry : movingCards.entrySet()) {
            Animator anim = entry.getKey();
            Card card = entry.getValue();

            if (anim != null) {
                anim.cancel();
            }

            if (card != null) {
                card.setMoving(false);
                card.setMotionAnimation(null);
            }
        }

        movingCards.clear();

        // Remove all cards from all stacks
        for (CardStack stack : allStacks) {
            stack.removeAllCards();
        }

        // Clear undo stack
        cardMovesForUndo.clear();

        // Clear card selections
        clearSrcCardStack();
//        clearDstCardStack();

        // Shuffle cards?
        if (shuffleCards) {
            shuffleCards(cards);
        }

        // Allocate cards to general stacks
        int stackNum = 0;
        int duration = ANIM_MOVE_MIN;

        for (Card card : cards) {
            card.moveTo(0, 0);
            moveCardToStack(card, generalStacks.get(stackNum), duration, null);
            stackNum = (stackNum + 1) % NUMGENERALSTACKS;
//        	duration = (duration + ANIM_MOVE_DELTA <= ANIM_MOVE_MAX) ? duration + ANIM_MOVE_DELTA : ANIM_MOVE_MAX;
            duration += ANIM_NEWGAME_DELTA;
        }

        // Force screen refresh
        gameView.postInvalidate();
    }


//    private void clearDstCardStack() {
//        if (dstStack != null) {
//            dstStack.setIsHighlighted(false);
//            dstStack = null;
//
//            if (gameView != null) {
//                gameView.postInvalidate();
//            }
//        }
//    }
//
//
//    private void selectDstCardStack(CardStack stack) {
//        clearDstCardStack();
//
//        if (stack != null) {
//            dstStack = stack;
//            dstStack.setIsHighlighted(true);
//
//            if (gameView != null) {
//                gameView.postInvalidate();
//            }
//        }
//    }


    private void clearSrcCardStack() {
        // Clear any cards that are hilighted as matches for the current source card
        for (Card card : this.cards) {
            if (card.isDstCard()) {
                card.setIsDstCard(false);

                Animator anim = card.getHighlightAnimation();

                if (anim != null) {
                    anim.cancel();
                    card.setHighlightAnimation(null);
                }
            }
        }

        // Now clear the source card
        if (this.currentlySelectedCard != null) {
            this.currentlySelectedCard.setIsSrcCard(false);
            this.currentlySelectedCard.setLastAction(CardAction.NO_ACTION);

            this.gameView.postInvalidate();
        }

        this.currentlySelectedCardSuitColour = null;
        this.currentlySelectedCardSuit = null;
        this.currentlySelectedCardVal = -1;
        this.currentlySelectedCard = null;
        this.currentlySelectedCardStack = null;
        this.currentlySelectedCardStackType = null;
    }


    private void selectSrcCardStack(CardStack targetStack) {
        clearSrcCardStack();

        Card targetCard = (targetStack != null) ? targetStack.topCard() : null;

        if (targetStack == null || targetCard == null) {
            return;
        }

        // Set the card state
        targetCard.setIsSrcCard(true);
        targetCard.setLastAction(CardAction.MOUSE_DOWN);

        // And remember this stack/card
        this.currentlySelectedCardStack = targetStack;
        this.currentlySelectedCardStackType = this.currentlySelectedCardStack.getStackType();
        this.currentlySelectedCard = targetCard;
        this.currentlySelectedCardVal = targetCard.getCardVal();
        this.currentlySelectedCardSuitColour = targetCard.getSuitColour();
        this.currentlySelectedCardSuit = targetCard.getCardSuit();

        // Identify any cards that this source card can match
        for (CardStack tempStack : generalStacks) {
            ArrayList<Card> tempCards = tempStack.getCards();

            for (Card card : tempCards) {
                if (cardsCanStack(targetCard, card)) {
                    card.setIsDstCard(true);

                    ObjectAnimator animation = ObjectAnimator.ofObject(card, "cardHighlightAlpha", new IntEvaluator(), 0, 255);
                    animation.addUpdateListener(this);
                    animation.setDuration(500);
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.setRepeatCount(ValueAnimator.INFINITE);
                    animation.setRepeatMode(ValueAnimator.REVERSE);

                    card.setHighlightAnimation(animation);

                    animation.start();
                }
            }
        }

        this.gameView.postInvalidate();
    }

    private CardStack findCardStack(int x, int y) {
        CardStack targetStack = null;

        for (CardStack c : allStacks) {
            if (c.containsPoint(x, y)) {
                targetStack = c;
                break;
            }
        }

        return targetStack;
    }


    private CardStack findDstAceStack(Card card) {
        CardStack aceStack = null;
        CardSuit cardSuit = card.getCardSuit();
        int cardVal = card.getCardVal();

        for (CardStack stack : aceStacks) {
            if (cardSuit == stack.getSuit()) {
                Card topCard = stack.topCard();
                int topCardVal = CARD_VALUE_ACE - 1;

                if (topCard != null) {
                    topCardVal = topCard.getCardVal();
                }

                if (cardVal == topCardVal + 1) {
                    aceStack = stack;
                }

                break;
            }
        }

        return aceStack;
    }


//    private CardStack findDstGeneralStack(Card card) {
//        CardStack generalStack = null;
//        CardSuitColour cardColour = card.getSuitColour();
//        int cardVal = card.getCardVal();
//
//        for (CardStack stack : generalStacks) {
//            Card topCard = stack.topCard();
//
//            if (topCard == null) {
//                generalStack = stack;
//                break;
//            }
//
//            CardSuitColour topCardSuitColour = topCard.getSuitColour();
//            int topCardVal = topCard.getCardVal();
//
//            if (cardColour != topCardSuitColour && cardVal == topCardVal - 1) {
//                generalStack = stack;
//                break;
//            }
//        }
//
//        return generalStack;
//    }
//
//
//    private CardStack findEmptyFreecellStack() {
//        CardStack freecellStack = null;
//
//        for (CardStack stack : freecellStacks) {
//            if (stack.getNumCards() <= 0) {
//                freecellStack = stack;
//
//                break;
//            }
//        }
//
//        return freecellStack;
//    }


    private void undoMove() {
        int numMoves = cardMovesForUndo.size();

        if (numMoves > 0) {
            CardMove cardMove = cardMovesForUndo.get(numMoves - 1);

            audioPlayer.play(this, R.raw.music_marimba_chord);

            int duration = ANIM_MOVE_MIN;

            for (Card card: cardMove.cards) {
                // Move in the opposite direction!
                cardMove.dstStack.popCard();
                moveCardToStack(card, cardMove.srcStack, duration, null);
                duration = Math.min(duration + ANIM_MOVE_DELTA, ANIM_MOVE_MAX);
            }

            cardMovesForUndo.remove(numMoves - 1);
        }
    }


    private boolean cardsCanStack(Card topCard, Card bottomCard) {
        boolean cardsCanStack = false;

        if (topCard != null && bottomCard != null) {
            CardSuitColour topSuitColour = topCard.getSuitColour();
            CardSuitColour bottomSuitColour = bottomCard.getSuitColour();
            int topCardVal = topCard.getCardVal();
            int bottomCardVal = bottomCard.getCardVal();

            if (bottomSuitColour != topSuitColour && bottomCardVal == topCardVal + 1) {
                cardsCanStack = true;
            }
        }

        return cardsCanStack;
    }


    private void moveCardToStack(Card srcCard, CardStack dstStack, int duration, CardMove undoCardMove) {
        if (duration <= 0) {
            duration = ANIM_MOVE_MIN;
        }

        Log.d(TAG,"moveCardToStack: Starting move of card = " + srcCard.getCardSuit().getName() + "," + srcCard.getCardVal());

        Animator currentAnimation = srcCard.getMotionAnimation();

        if (currentAnimation != null) {
            Log.d(TAG,"moveCardToStack: Cancelling current animation for card");

            currentAnimation.cancel();
        }

        Rect currentRect = srcCard.getCardRect();
        Point topLeft = dstStack.getNextCardLocation();
        Rect finalRect = new Rect(topLeft.x, topLeft.y, topLeft.x + srcCard.getWidth(), topLeft.y + srcCard.getHeight());

        dstStack.pushCard(srcCard, false);

        ObjectAnimator animation = ObjectAnimator.ofObject(srcCard, "cardRect", new RectEvaluator(), currentRect, finalRect);
        animation.addUpdateListener(this);
        animation.addListener(this);
        animation.setDuration(duration);
        animation.setInterpolator(new OvershootInterpolator());

        srcCard.setMotionAnimation(animation);

        srcCard.setMoving(true);
        animation.start();
        movingCards.put(animation, srcCard);

        // Keep track of move so we can undo later
        if (undoCardMove != null) {
            undoCardMove.cards.add(srcCard);
        }
    }


    private void cleanupCards() {
        boolean movedCard = true;
        int duration = ANIM_MOVE_MIN;

        while (movedCard) {
            movedCard = false;

            for (CardStack srcStack : allStacks) {
                Card srcCard = srcStack.topCard();

                if (srcCard == null || srcStack.getStackType() == ACESTACK) {
                    continue;
                }

                CardStack dstStack = findDstAceStack(srcCard);

                if (dstStack == null) {
                    continue;
                }

                moveTopCardFromSrcStackToDstStack(srcStack, dstStack, duration, true);

                movedCard = true;

                duration = Math.min(duration + ANIM_MOVE_DELTA, ANIM_MOVE_MAX);
            }
        }
    }


    private void moveTopCardFromSrcStackToDstStack(CardStack srcStack, CardStack dstStack, int duration, boolean trackUndo) {
        Card srcCard = srcStack.popCard();

        // Keep track of move so we can undo later
        if (trackUndo && srcStack != dstStack) {
            CardMove cardMove = new CardMove(srcStack, dstStack);
            this.cardMovesForUndo.add(cardMove);
            moveCardToStack(srcCard, dstStack, duration, cardMove);
        }
        else {
            moveCardToStack(srcCard, dstStack, duration, null);
        }
    }


    private boolean moveMultiCards(CardStack srcStack, CardStack dstStack) {
        // Sanity check

        if (srcStack == null || srcStack.getNumCards() <= 0 || dstStack == null || srcStack.getStackType() != GENERALSTACK || dstStack.getStackType() != GENERALSTACK) {
            return false;
        }


        // Figure out how many empty cells we have to work with; don't count the dest stack!

        int numEmptyFreecells = 0;
        int numEmptyGenerals = 0;

        for (CardStack stack : generalStacks) {
            if (stack != dstStack && stack.getNumCards() <= 0) {
                numEmptyGenerals++;
            }
        }

        for (CardStack stack : freecellStacks) {
            if (stack != dstStack && stack.getNumCards() <= 0) {
                numEmptyFreecells++;
            }
        }

        Log.d(TAG,"moveStack: numEmptyFreecells=" + numEmptyFreecells + ", numEmptyGenerals=" + numEmptyGenerals);


        ArrayList<Card> srcCards = srcStack.getCards();
        Card dstCard = dstStack.topCard();
        int srcStackNumCards = srcCards.size();
        int srcStackRunLength = 1;

        // Figure out the length of the run on the source stack

        for (int i = srcStackNumCards - 1; i > 0; i--) {
            Card topCard = srcCards.get(i);
            Card bottomCard = srcCards.get(i - 1);

            // Hit limit wrt destination?
            if (cardsCanStack(topCard, dstCard)) {
                break;
            }
            else if (cardsCanStack(topCard, bottomCard)) {
                srcStackRunLength++;
            }
            else {
                break;
            }
        }

        Log.d(TAG,"moveStack: srcStackRunLength=" + srcStackRunLength);


        // So, how many cards can we actually move?

        int numCardsToMove = srcStackRunLength;
        int numEmptyCells = numEmptyFreecells + numEmptyGenerals;

        if (numCardsToMove > numEmptyCells + 1) {
            Log.d(TAG,"moveStack: clipped numCardsToMove due to empty cell count");
            numCardsToMove = numEmptyCells + 1;
        }

        Log.d(TAG,"moveStack: numCardsToMove=" + numCardsToMove);


        // If destination is empty or runs match, then move the cards

        Card cardToMove = srcCards.get(srcStackNumCards - numCardsToMove);

        if (dstCard != null && !cardsCanStack(cardToMove, dstCard)) {
            Log.d(TAG,"moveStack: Runs do not connect");
            return false;
        }

        Log.d(TAG,"moveStack: Runs connect, moving cards...");

        // Remove the cards from the source stack

        ArrayList<Card> cardsToMove = new ArrayList<>();

        for (int i = 0; i < numCardsToMove; i++) {
            Card tmpCard = srcStack.popCard();
            cardsToMove.add(tmpCard);
        }

        int duration = ANIM_MOVE_MIN;
        CardMove cardMove = new CardMove(srcStack, dstStack);
        this.cardMovesForUndo.add(cardMove);

        for (int i = cardsToMove.size() - 1; i >= 0; i--) {
            Card tmpCard = cardsToMove.get(i);
            moveCardToStack(tmpCard, dstStack, duration, cardMove);

            duration = Math.min(duration + ANIM_MOVE_DELTA, ANIM_MOVE_MAX);
        }


        return numCardsToMove > 0;
    }


    private boolean doubleClickOccurred(MotionEvent event) {
        boolean doubleClickDetected = false;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            long currentMillis = event.getEventTime();

            if (currentMillis < this.lastActionDownMillis + DOUBLE_CLICK_MILLIS) {
                doubleClickDetected = true;

                currentMillis = -1;
            }

            this.lastActionDownMillis = currentMillis;
        }

        return doubleClickDetected;
    }


    private void doDrag(MotionEvent event) {
        if (this.currentlySelectedCard == null) {
            return;
        }

        int cardWidth = this.currentlySelectedCard.getWidth();
        int cardHeight = this.currentlySelectedCard.getHeight();
        int x = (int) event.getX() - cardWidth/2;
        int y = (int) event.getY() - cardHeight/2;

        this.currentlySelectedCard.setMoving(true);
        this.currentlySelectedCard.moveTo(x, y);
        this.currentlySelectedCard.setLastAction(CardAction.MOUSE_DRAG);

        gameView.postInvalidate();
    }


    private boolean cardCanMoveToStack(Card card, CardStack dstStack) {
        boolean cardCanMove = false;

        if (card != null && dstStack != null) {
            CardStack.CardStackType dstStackType = dstStack.getStackType();
            Card dstCard = dstStack.topCard();
            CardSuit dstSuit = dstStack.getSuit();
            CardSuitColour dstColour = null;
            int dstCardVal = CARD_VALUE_ACE - 1;

            if (dstCard != null) {
                dstSuit = dstCard.getCardSuit();
                dstColour = dstSuit.getColour();
                dstCardVal = dstCard.getCardVal();
            }

            // Move to empty free-cell or empty general stack is okay
            if (dstCard == null && (dstStackType == FREECELLSTACK || dstStackType == GENERALSTACK)) {
                cardCanMove = true;
            }

            // Move to ace stack is okay if suits match and card order is correct
            else if (dstStackType == ACESTACK && card.getCardSuit() == dstSuit && card.getCardVal() == dstCardVal + 1) {
                cardCanMove = true;
            }

            // If colours alternate and card order is correct, then move is okay
            else if (dstStackType == GENERALSTACK && dstColour != card.getSuitColour() && dstCardVal == card.getCardVal() + 1) {
                cardCanMove = true;
            }
        }

        return cardCanMove;
    }


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        // Since a change has occurred, trigger a redraw of the entire GameView
        gameView.postInvalidate();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        Card card = movingCards.get(animation);

        if (card != null) {
            Log.d(TAG,"onAnimationEnd: Ended move of card = " + card.getCardSuit().getName() + "," + card.getCardVal());
            card.setMoving(false);
            card.setMotionAnimation(null);
        }

        movingCards.remove(animation);
    }

    @Override
    public void onAnimationRepeat(Animator anim) {
        // Pass
    }

    @Override
    public void onAnimationStart(Animator anim) {
        // Pass
    }

    @Override
    public void onAnimationCancel(Animator anim) {
        // Pass
    }

    @Override
    // Receive notification of changes in the position/size of the GameView
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        Log.d(TAG,"onLayoutChange: (left,top,right,bottom) = (" + left + "," + top + "," + right + "," + bottom + ")");
        Log.d(TAG,"onLayoutChange: (oldLeft,oldTop,oldRight,oldBottom) = (" + oldLeft + "," + oldTop + "," + oldRight + "," + oldBottom + ")");

        int viewHeight = bottom - top;
        int viewWidth = right - left;
        int viewWidthHalf = viewWidth / 2;

        int cardGridWidth = (viewWidth / NUMGENERALSTACKS + 1);
        int cardGridHeight = (this.NATURAL_SVG_CARD_HEIGHT * cardGridWidth) / NATURAL_SVG_CARD_WIDTH;
        int cardBorderSize = cardGridWidth / 8;
        int currentCardWidth = cardGridWidth - 2 * cardBorderSize;
        int currentCardHeight = (this.NATURAL_SVG_CARD_HEIGHT * currentCardWidth) / NATURAL_SVG_CARD_WIDTH;
//        int generalStackHorizSpacing = (viewWidth - NUMGENERALSTACKS * currentCardWidth) / (NUMGENERALSTACKS + 1);

        // Position the restart and undo icons
        int actionsSize = currentCardHeight / 2;
        restartSVGImage.centerAt(viewWidthHalf, cardBorderSize + actionsSize / 2, actionsSize);
        undoSVGImage.centerAt(viewWidthHalf, cardBorderSize + (3 * actionsSize ) / 2, actionsSize);

        // Place the ace stacks
        for (int i = 0; i < NUMACESTACKS; i++) {
            int stackLeft = cardBorderSize + i * (currentCardWidth + cardBorderSize);
            int stackRight = stackLeft + currentCardWidth;
            int stackTop = cardBorderSize;
            int stackBottom = stackTop + currentCardHeight;
            CardStack cardStack = this.aceStacks.get(i);
            cardStack.setBaseRect(stackLeft, stackTop, stackRight, stackBottom);
        }

        // Place the free cells
        for (int i = 0; i < NUMFREECELLSTACKS; i++) {
            int stackLeft = viewWidth -  (i + 1) * (currentCardWidth + cardBorderSize);
            int stackRight = stackLeft + currentCardWidth;
            int stackTop = cardBorderSize;
            int stackBottom = stackTop + currentCardHeight;
            CardStack cardStack = freecellStacks.get(i);
            cardStack.setBaseRect(stackLeft, stackTop, stackRight, stackBottom);
        }

        // Place the general stacks
        for (int i = 0; i < NUMGENERALSTACKS; i++) {
            int stackLeft = i * cardGridWidth + cardBorderSize;
            int stackRight = stackLeft + currentCardWidth;
            int stackTop = cardGridHeight + cardBorderSize;
            int stackBottom = stackTop + currentCardHeight;
            CardStack cardStack = generalStacks.get(i);
            cardStack.setVertOffset(currentCardHeight / 4);
            cardStack.setBaseRect(stackLeft, stackTop, stackRight, stackBottom);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG,"onTouch: event=" + MotionEvent.actionToString(event.getAction()));

        // Early processing for the undo/restart icons
        if (event.getAction() == MotionEvent.ACTION_DOWN && this.undoSVGImage.containsPt((int) event.getX(), (int) event.getY())) {
            this.gameView.playSoundEffect(SoundEffectConstants.CLICK);
            undoMove();
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_DOWN && this.restartSVGImage.containsPt((int) event.getX(), (int) event.getY())) {
            doDialog();
            return true;
        }

        // Main processing for card-related events

        boolean handledEvent = false;

        // Figure out where the event occurred
        CardStack targetStack = findCardStack((int) event.getX(), (int) event.getY());

        // These booleans make things more readable below...
        boolean doubleClickDetected = doubleClickOccurred(event);
        boolean srcCardSelected = this.currentlySelectedCard != null;
        boolean movingBetweenStacks = (this.currentlySelectedCard != null && this.currentlySelectedCardStack != null  && targetStack != null && this.currentlySelectedCardStack != targetStack);
        boolean srcAndDstAreGeneral = (movingBetweenStacks && this.currentlySelectedCardStack.getStackType() == GENERALSTACK && targetStack.getStackType() == GENERALSTACK);
        boolean srcCardIsDragging = this.currentlySelectedCard != null && this.currentlySelectedCard.getLastAction() == CardAction.MOUSE_DRAG;
        boolean srcCardWasClicked = this.currentlySelectedCard != null && this.currentlySelectedCard.getLastAction() == CardAction.MOUSE_DOWN;

        if (targetStack == null && doubleClickDetected) {
            // Double-click in blank space means auto-move
            Log.d(TAG,"onTouch: Auto-move");
            clearSrcCardStack();
//            clearDstCardStack();
            cleanupCards();
            handledEvent = true;
        }
        else if (targetStack == null  && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Clear selection
            Log.d(TAG,"onTouch: Clear selection");
            clearSrcCardStack();
//            clearDstCardStack();

            // Don't eat this event-- if we do, then context menu won't show...
            // handledEvent = true;
        }
        else if (movingBetweenStacks && !srcAndDstAreGeneral  && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Move single card from one stack to another stack
            if (cardCanMoveToStack(this.currentlySelectedCard, targetStack)) {
                Log.d(TAG,"onTouch: Move single card");
                moveTopCardFromSrcStackToDstStack(this.currentlySelectedCardStack, targetStack, 0, true);
                clearSrcCardStack();
//                clearDstCardStack();
            }
            else {
                Log.d(TAG,"onTouch: Ignoring invalid single-card move");
                selectSrcCardStack(targetStack);
//                clearDstCardStack();
            }

            handledEvent = true;
        }
        else if (srcAndDstAreGeneral && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Move from one general stack to another (i.e. possible run move)
            Log.d(TAG,"onTouch: Move between general stacks");
            if (moveMultiCards(this.currentlySelectedCardStack, targetStack)) {
                clearSrcCardStack();
            }
            else {
                selectSrcCardStack(targetStack);
            }
//            clearDstCardStack();
            handledEvent = true;
        }
        else if (targetStack != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Select target card
            Log.d(TAG,"onTouch: Select card");
            selectSrcCardStack(targetStack);
//            clearDstCardStack();

            handledEvent = true;
        }
        else if (srcCardWasClicked && event.getAction() == MotionEvent.ACTION_MOVE) {
            // If card was last clicked and user has dragged far enough, start moving

            // Initially this ignored movement anywhere within the card, but that was too extreme,
            // so just ignore motion in the middle of the card
            Rect tempRect = this.currentlySelectedCard.getCardRect();
            int insetX = tempRect.width() / 4;
            int insetY = tempRect.height() / 4;
            tempRect.inset(insetX, insetY);
            if (!tempRect.contains((int) event.getX(), (int) event.getY())) {
                Log.d(TAG,"onTouch: Drag card begins");
                doDrag(event);

//                if (targetStack != this.currentlySelectedCardStack) {
//                    selectDstCardStack(targetStack);
//                }
            }
            else {
                Log.d(TAG,"onTouch: Ignoring drag within card current location");
//                clearDstCardStack();
            }

            handledEvent = true;
        }
        else if (srcCardIsDragging && event.getAction() == MotionEvent.ACTION_MOVE) {
            // If card is already dragging, continue dragging it
            Log.d(TAG,"onTouch: Drag card continues");
            doDrag(event);

//            if (targetStack != this.currentlySelectedCardStack) {
//                selectDstCardStack(targetStack);
//            }

            handledEvent = true;
        }
        else if (srcCardIsDragging && event.getAction() == MotionEvent.ACTION_UP) {
            // A dragged card was released, so move it to the new location or return it to its original stack
			if (cardCanMoveToStack(this.currentlySelectedCard, targetStack)) {
                Log.d(TAG,"onTouch: Stack drag-drop new location");
                moveTopCardFromSrcStackToDstStack(this.currentlySelectedCardStack, targetStack, 0, true);
                clearSrcCardStack();
            }
            else {
                Log.d(TAG,"onTouch: Stack drag-drop old location");
                // Sneaky-- source and dest are the same stack, since we really just want the animation effect as card is moved back into place
                moveTopCardFromSrcStackToDstStack(this.currentlySelectedCardStack, this.currentlySelectedCardStack, 0, true);
            }

//            clearDstCardStack();

            handledEvent = true;
        }

        // Track mouse-up events or we will incorrectly allow drag/drop activity in code above
        if (this.currentlySelectedCard != null && event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG,"onTouch: Noting mouse-up on selected card");
            this.currentlySelectedCard.setLastAction(CardAction.MOUSE_UP);

            handledEvent = true;
        }

        return handledEvent;
    }
}
