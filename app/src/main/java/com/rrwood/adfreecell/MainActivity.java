package com.rrwood.adfreecell;

import static com.rrwood.adfreecell.Card.CARD_VALUE_ACE;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.Window;
import android.content.pm.ActivityInfo;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;

import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MainActivity extends Activity implements View.OnLayoutChangeListener, View.OnTouchListener, ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    static class CardMove {
        public Card card = null;
        public CardStack fromStack = null;
        public CardStack toStack = null;

        public CardMove(Card c, CardStack from, CardStack to) {
            card = c;
            fromStack = from;
            toStack = to;
        }
    }


    private static final String TAG = "ROYDEBUG.MainActivity";

    private static final long DOUBLE_CLICK_MILLIS = 300;

    private static final int ANIM_MOVE_MIN = 500;
    private static final int ANIM_MOVE_DELTA = 250;
    private static final int ANIM_NEWGAME_DELTA = 20;
    private static final int ANIM_MOVE_MAX = 2000;

    static final int NUMFREECELLSTACKS = 4;
    static final int NUMACESTACKS = 4;
    static final int NUMGENERALSTACKS = 8;


    private GameView gameView = null;
    private final int naturalCardWidth = 224;
    private final int naturalCardHeight = 313;
    private int currentCardWidth = 0;
    private int currentCardHeight = 0;
    private int cardGridWidth = 0;
    private int cardGridHeight = 0;
    private int cardBorderSize = 0;

    private final Random random = new Random();

    private ArrayList<Card> cards = null;
    private HashMap<Animator, Card> movingCards = null;
    private ArrayList<Card> matchingCards = null;

    private ArrayList<CardStack> freecellStacks = null;
    private ArrayList<CardStack> aceStacks = null;
    private ArrayList<CardStack> generalStacks = null;
    private ArrayList<CardStack> allStacks = null;

    private boolean didInitialNewGame = false;

    private ArrayList<CardMove> cardMoves = null;

    private SVGImage restartSVGImage = null;
    private SVGImage undoSVGImage = null;


    private CardStack mSrcStack = null;
    private CardStack.CardStackType mSrcStackType = null;
    private Card mSrcCard = null;
    private int mSrcCardVal = -1;
    private Card.CardSuit mSrcCardSuit = null;
    private Card.CardSuitColour mSrcCardSuitColour = null;
    private CardStack dstStack = null;

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
        this.cardMoves = new ArrayList<CardMove>();

        // Set up the list of "matching" general cards
        this.matchingCards = new ArrayList<Card>();

        // Maintain a list of active animations and cards
        this.movingCards = new HashMap<Animator, Card>();

        // Create the cards but don't position or size them yet-- that happens in onLayoutChange, once we know the screen size
        this.cards = new ArrayList<>();
        for (CardSVGInfo cardSVGInfo : CardSVGInfo.CARD_SVG_LOAD_INFO){
            Drawable cardSVGDrawable = ResourcesCompat.getDrawable(res, cardSVGInfo.resourceID, null);
            Card card = new Card(cardSVGInfo.cardSuit, cardSVGInfo.cardValue, cardSVGDrawable);
            this.cards.add(card);
        }

        // Keep a list of ALL card stacks
        this.allStacks = new ArrayList<>();

        // Create the free-cell stacks and add them to the GameView (location will be set properly after GameView layout completes)
        this.freecellStacks = new ArrayList<>();
        Drawable blueCardDrawable = ResourcesCompat.getDrawable(res, R.drawable.blue_card_back_svg, null);

        for (int i = 0; i < NUMFREECELLSTACKS; i++) {
            CardStack cardStack = new CardStack(null, CardStack.CardStackType.FREECELLSTACK, blueCardDrawable);
            this.freecellStacks.add(cardStack);
            this.allStacks.add(cardStack);
            this.gameView.addCardStack(cardStack);
        }

        // Create the ace stacks (location will be set properly after GameView layout completes)
        // TODO: Add proper svg for each of the aces stacks
        this.aceStacks = new ArrayList<CardStack>();
        this.aceStacks.add(new CardStack(Card.CardSuit.CLUBS, CardStack.CardStackType.ACESTACK, blueCardDrawable));
        this.aceStacks.add(new CardStack(Card.CardSuit.DIAMONDS, CardStack.CardStackType.ACESTACK, blueCardDrawable));
        this.aceStacks.add(new CardStack(Card.CardSuit.SPADES, CardStack.CardStackType.ACESTACK, blueCardDrawable));
        this.aceStacks.add(new CardStack(Card.CardSuit.HEARTS, CardStack.CardStackType.ACESTACK, blueCardDrawable));

        for (CardStack cardStack : this.aceStacks) {
            this.allStacks.add(cardStack);
            this.gameView.addCardStack(cardStack);
        }

        // Create the general stacks (location will be set properly after GameView layout completes)
        this.generalStacks = new ArrayList<CardStack>();

        for (int i = 0; i < NUMGENERALSTACKS; i++) {
            CardStack cardStack = new CardStack(null, CardStack.CardStackType.GENERALSTACK, null);
            cardStack.pushCard(this.cards.get(i % this.cards.size()), false);
            this.generalStacks.add(cardStack);
            this.allStacks.add(cardStack);
            this.gameView.addCardStack(cardStack);
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


    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	            Log.d(TAG, "Menu item selected, id=R.id.action_settings");
	            return true;
	        case R.id.menu_action_2:
	        	Log.d(TAG, "Menu item selected, id=R.id.menu_action_2");
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.context_menu_action_1:
	        	Log.d(TAG, "Menu item selected, id=R.id.context_menu_action_1");
	            return true;
	        case R.id.context_menu_action_2:
	        	Log.d(TAG, "Menu item selected, id=R.id.context_menu_action_2");
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}


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
        cardMoves.clear();

        // Clear card selections
        clearSrcCardStack();
        clearDstCardStack();

        // Shuffle cards?
        if (shuffleCards) {
            shuffleCards(cards);
        }

        // Allocate cards to general stacks
        int stackNum = 0;
        int duration = ANIM_MOVE_MIN;

        for (Card card : cards) {
            card.moveTo(0, 0);
            moveCardToStack(card, null, generalStacks.get(stackNum), duration, false);
            stackNum = (stackNum + 1) % NUMGENERALSTACKS;
//        	duration = (duration + ANIM_MOVE_DELTA <= ANIM_MOVE_MAX) ? duration + ANIM_MOVE_DELTA : ANIM_MOVE_MAX;
            duration += ANIM_NEWGAME_DELTA;
        }

        // Force screen refresh
        gameView.postInvalidate();
    }


    private void clearDstCardStack() {
        if (dstStack != null) {
//            dstStack.setIsHilighted(false);
            dstStack = null;

            if (gameView != null) {
                gameView.postInvalidate();
            }
        }
    }


    private void selectDstCardStack(CardStack stack) {
        clearDstCardStack();

        if (stack != null) {
            dstStack = stack;
//            dstStack.setIsHilighted(true);

            if (gameView != null) {
                gameView.postInvalidate();
            }
        }
    }


    private void clearSrcCardStack() {
        // Clear any cards that are hilighted as matches for the current source card
        for (Card card : matchingCards) {
            card.setIsDstCard(false);

            Animator anim = card.getHiliteAnimation();

            if (anim != null) {
                anim.cancel();
                card.setHiliteAnimation(null);
            }
        }

        matchingCards.clear();


        // Now clear the source card
        if (mSrcCard != null) {
            mSrcCard.setIsSrcCard(false);
            mSrcCard.setLastAction(Card.CardAction.NO_ACTION);

            if (gameView != null) {
                gameView.postInvalidate();
            }
        }

        mSrcCardSuitColour = null;
        mSrcCardSuit = null;
        mSrcCardVal = -1;
        mSrcCard = null;
        mSrcStack = null;
        mSrcStackType = null;
    }


    private void selectSrcCardStack(CardStack stack) {
        clearSrcCardStack();

        if (stack != null && stack.topCard() != null) {
//			Log.d(TAG,"selectCardStack: Selecting stack " + stack);

            mSrcStack = stack;
            mSrcStackType = mSrcStack.getStackType();
            mSrcCard = mSrcStack.topCard();
            mSrcCardVal = mSrcCard.getCardVal();
            mSrcCardSuitColour = mSrcCard.getSuitColour();
            mSrcCardSuit = mSrcCard.getCardSuit();
            mSrcCard.setLastAction(Card.CardAction.MOUSE_DOWN);

            mSrcCard.setIsSrcCard(true);

            // Identify any cards that this source card can match
            for (CardStack tempStack : generalStacks) {
                ArrayList<Card> tempCards = tempStack.getCards();

                for (Card card : tempCards) {
                    if (cardsCanStack(mSrcCard, card)) {
                        card.setIsDstCard(true);

                        matchingCards.add(card);

                        ObjectAnimator animation = null;
                        animation = ObjectAnimator.ofObject(card, "hiliteAlpha", new IntEvaluator(), 0, 255);
                        animation.addUpdateListener(this);
                        animation.setDuration(500);
                        animation.setInterpolator(new DecelerateInterpolator());
                        animation.setRepeatCount(ValueAnimator.INFINITE);
                        animation.setRepeatMode(ValueAnimator.REVERSE);

                        card.setHiliteAnimation(animation);

                        animation.start();
                    }
                }
            }

            gameView.postInvalidate();
        }
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
        Card.CardSuit cardSuit = card.getCardSuit();
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


    private CardStack findDstGeneralStack(Card card) {
        CardStack generalStack = null;
        Card.CardSuitColour cardColour = card.getSuitColour();
        int cardVal = card.getCardVal();

        for (CardStack stack : generalStacks) {
            Card topCard = stack.topCard();

            if (topCard == null) {
                generalStack = stack;
                break;
            }

            Card.CardSuitColour topCardSuitColour = topCard.getSuitColour();
            int topCardVal = topCard.getCardVal();

            if (cardColour != topCardSuitColour && cardVal == topCardVal - 1) {
                generalStack = stack;
                break;
            }
        }

        return generalStack;
    }


    private CardStack findEmptyFreecellStack() {
        CardStack freecellStack = null;

        for (CardStack stack : freecellStacks) {
            if (stack.getNumCards() <= 0) {
                freecellStack = stack;

                break;
            }
        }

        return freecellStack;
    }


    private void undoMove() {
        int numMoves = cardMoves.size();

        if (numMoves > 0) {
            CardMove move = cardMoves.get(numMoves - 1);

            audioPlayer.play(this, R.raw.music_marimba_chord);

            // Move in the opposite direction!
            moveSingleCard(move.toStack, move.fromStack, 0, false);

            cardMoves.remove(numMoves - 1);
        }
    }


    private void moveSingleCard(CardStack srcStack, CardStack dstStack, int duration, boolean trackUndo) {
        if (srcStack == null) {
            return;
        }

        Card srcCard = srcStack.popCard();

        moveCardToStack(srcCard, srcStack, dstStack, duration, trackUndo);
    }


    private boolean cardsCanStack(Card topCard, Card bottomCard) {
        boolean cardsCanStack = false;

        if (topCard != null && bottomCard != null) {
            Card.CardSuitColour topSuitColour = topCard.getSuitColour();
            Card.CardSuitColour bottomSuitColour = bottomCard.getSuitColour();
            int topCardVal = topCard.getCardVal();
            int bottomCardVal = bottomCard.getCardVal();

            if (bottomSuitColour != topSuitColour && bottomCardVal == topCardVal + 1) {
                cardsCanStack = true;
            }
        }

        return cardsCanStack;
    }


    private void moveCardToStack(Card srcCard, CardStack srcStack, CardStack dstStack, int duration, boolean trackUndo) {
        if (srcCard == null || dstStack == null) {
            return;
        }

        if (duration <= 0) {
            duration = ANIM_MOVE_MIN;
        }

        Log.d(TAG,"moveCardToStack: Starting move of card = " + srcCard.getCardSuit().getName() + "," + srcCard.getCardVal());


        Animator currentAnimation = srcCard.getMotionAnimation();

        if (currentAnimation != null) {
            Log.d(TAG,"moveCardToStack: Cancelling current animation for card");

            currentAnimation.cancel();
        }

        Rect currentRect = srcCard.getRect();
        Point topLeft = dstStack.getNextCardLocation();
        Rect finalRect = new Rect(topLeft.x, topLeft.y, topLeft.x + srcCard.getWidth(), topLeft.y + srcCard.getHeight());

        dstStack.pushCard(srcCard, false);

        ObjectAnimator animation = null;
        animation = ObjectAnimator.ofObject(srcCard, "rect", new RectEvaluator(), currentRect, finalRect);
        animation.addUpdateListener(this);
        animation.addListener(this);
        animation.setDuration(duration);
        animation.setInterpolator(new OvershootInterpolator());

        srcCard.setMotionAnimation(animation);

        srcCard.setMoving(true);
        animation.start();
        movingCards.put(animation, srcCard);

        // Keep track of move so we can undo later
        if (trackUndo && srcStack != dstStack) {
            CardMove cardMove = new CardMove(srcCard, srcStack, dstStack);
            cardMoves.add(cardMove);
        }
    }


    private void cleanupCards() {
        boolean movedCard = false;
        int duration = ANIM_MOVE_MIN;

        do {
            movedCard = false;

            for (CardStack srcStack : allStacks) {
                if (srcStack.getStackType() == CardStack.CardStackType.ACESTACK) {
                    // Skip the ace stacks, of course
                    continue;
                }

                Card srcCard = srcStack.topCard();

                if (srcCard != null) {
                    CardStack dstStack = findDstAceStack(srcCard);

                    if (dstStack != null) {
                        moveSingleCard(srcStack, dstStack, duration, true);

                        movedCard = true;

                        duration = Math.min(duration + ANIM_MOVE_DELTA, ANIM_MOVE_MAX);
                    }
                }
            }
        } while (movedCard);
    }


    private boolean moveMultiCards(CardStack srcStack, CardStack dstStack) {
        // Sanity check

        if (srcStack == null || srcStack.getNumCards() <= 0 || dstStack == null || srcStack.getStackType() != CardStack.CardStackType.GENERALSTACK || dstStack.getStackType() != CardStack.CardStackType.GENERALSTACK) {
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

        ArrayList<Card> cardsToMove = new ArrayList<Card>();

        for (int i = 0; i < numCardsToMove; i++) {
            Card tmpCard = srcStack.popCard();
            cardsToMove.add(tmpCard);
        }

        int duration = ANIM_MOVE_MIN;

        for (int i = cardsToMove.size() - 1; i >= 0; i--) {
            Card tmpCard = cardsToMove.get(i);
            moveCardToStack(tmpCard, srcStack, dstStack, duration, true);

            duration = Math.min(duration + ANIM_MOVE_DELTA, ANIM_MOVE_MAX);
        }


        return numCardsToMove > 0;
    }


    private boolean doubleClickOccured(MotionEvent event) {
        boolean doubleClickDetected = false;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            long currentMillis = event.getEventTime();

            if (currentMillis < lastActionDownMillis + DOUBLE_CLICK_MILLIS) {
                doubleClickDetected = true;

                currentMillis = -1;
            }

            lastActionDownMillis = currentMillis;
        }

        return doubleClickDetected;
    }


    private void doDrag(MotionEvent event) {
        if (mSrcCard == null) {
            return;
        }

        int cardWidth = mSrcCard.getWidth();
        int cardHeight = mSrcCard.getHeight();
        int x = (int) event.getX() - cardWidth/2;
        int y = (int) event.getY() - cardHeight/2;

        mSrcCard.moveTo(x, y);
        mSrcCard.setMoving(true);
        mSrcCard.setLastAction(Card.CardAction.MOUSE_DRAG);

        gameView.postInvalidate();
    }


    /**
     * Determine if a card can move to a given CardStack
     *
     * @param card the Card to test
     * @param dstStack the CardStack to test
     * @return true if the Card is allowed to move to the CardStack, otherwise false
     */
    private boolean cardCanMoveToStack(Card card, CardStack dstStack) {
        boolean cardCanMove = false;

        if (card != null && dstStack != null) {
            CardStack.CardStackType dstStackType = dstStack.getStackType();
            Card dstCard = dstStack.topCard();
            Card.CardSuit dstSuit = dstStack.getSuit();
            Card.CardSuitColour dstColour = null;
            int dstCardVal = CARD_VALUE_ACE - 1;

            if (dstCard != null) {
                dstSuit = dstCard.getCardSuit();
                dstColour = dstSuit.getColour();
                dstCardVal = dstCard.getCardVal();
            }

            // Move to empty free-cell or empty general stack is okay
            if (dstCard == null && (dstStackType == CardStack.CardStackType.FREECELLSTACK || dstStackType == CardStack.CardStackType.GENERALSTACK)) {
                cardCanMove = true;
            }

            // Move to ace stack is okay if suits match and card order is correct
            else if (dstStackType == CardStack.CardStackType.ACESTACK && card.getCardSuit() == dstSuit && card.getCardVal() == dstCardVal + 1) {
                cardCanMove = true;
            }

            // If colours alternate and card order is correct, then move is okay
            else if (dstStackType == CardStack.CardStackType.GENERALSTACK && dstColour != card.getSuitColour() && dstCardVal == card.getCardVal() + 1) {
                cardCanMove = true;
            }
        }

        return cardCanMove;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG,"onTouch: event=" + MotionEvent.actionToString(event.getAction()));

        // Early processing for the icons (undo, new game, restart game)

        if (event.getAction() == MotionEvent.ACTION_DOWN && undoSVGImage.containsPt((int) event.getX(), (int) event.getY())) {
            gameView.playSoundEffect(SoundEffectConstants.CLICK);

            undoMove();

            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_DOWN && restartSVGImage.containsPt((int) event.getX(), (int) event.getY())) {
            doDialog();

            return true;
        }


        // Main processing for card-related events

        boolean handledEvent = false;

        // Figure out where the event occurred
        CardStack dstStack = findCardStack((int) event.getX(), (int) event.getY());

        // These booleans make things more readable below...
        boolean doubleClickDetected = doubleClickOccured(event);
        boolean movingBetweenStacks = (mSrcCard != null && mSrcStack != null  && dstStack != null && mSrcStack != dstStack);
        boolean srcAndDstAreGeneral = (movingBetweenStacks && mSrcStack.getStackType() == CardStack.CardStackType.GENERALSTACK && dstStack.getStackType() == CardStack.CardStackType.GENERALSTACK);
        boolean srcCardIsDragging = mSrcCard != null && mSrcCard.getLastAction() == Card.CardAction.MOUSE_DRAG;
        boolean srcCardWasClicked = mSrcCard != null && mSrcCard.getLastAction() == Card.CardAction.MOUSE_DOWN;

        if (dstStack == null && doubleClickDetected) {
            // Double-click in blank space means auto-move
            Log.d(TAG,"onTouch: Auto-move");
            clearSrcCardStack();
            clearDstCardStack();
            cleanupCards();
            handledEvent = true;
        }
        else if (dstStack == null  && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Clear selection
            Log.d(TAG,"onTouch: Clear selection");
            clearSrcCardStack();
            clearDstCardStack();

            // Don't eat this event-- if we do, then context menu won't show...
            // handledEvent = true;
        }
        else if (movingBetweenStacks && !srcAndDstAreGeneral  && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Move single card from one stack to another stack
            if (cardCanMoveToStack(mSrcCard, dstStack)) {
                Log.d(TAG,"onTouch: Move single card");
                moveSingleCard(mSrcStack, dstStack, 0, true);
                clearSrcCardStack();
                clearDstCardStack();
            }
            else {
                Log.d(TAG,"onTouch: Ignoring invalid single-card move");
                selectSrcCardStack(dstStack);
                clearDstCardStack();
            }

            handledEvent = true;
        }
        else if (srcAndDstAreGeneral && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Move from one general stack to another (i.e. possible run move)
            Log.d(TAG,"onTouch: Move between general stacks");
            if (moveMultiCards(mSrcStack, dstStack)) {
                clearSrcCardStack();
                clearDstCardStack();
            }
            else {
                selectSrcCardStack(dstStack);
                clearDstCardStack();
            }

            handledEvent = true;
        }
        else if (dstStack != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Select target card
            Log.d(TAG,"onTouch: Select card");
            selectSrcCardStack(dstStack);
            clearDstCardStack();

            handledEvent = true;
        }
        else if (srcCardWasClicked && event.getAction() == MotionEvent.ACTION_MOVE) {
            // If card was last clicked and user has dragged far enough, start moving

            // Initially this ignored movement anywhere within the card, but that was too extreme,
            // so just ignore motion in the middle of the card
            Rect tempRect = mSrcCard.getRect();
            int insetX = tempRect.width() / 4;
            int insetY = tempRect.height() / 4;
            tempRect.inset(insetX, insetY);
            if (!tempRect.contains((int) event.getX(), (int) event.getY())) {
                Log.d(TAG,"onTouch: Drag card begins");
                doDrag(event);
                selectDstCardStack(dstStack);
            }
            else {
                Log.d(TAG,"onTouch: Ignoring drag within card current location");
                clearDstCardStack();
            }

            handledEvent = true;
        }
        else if (srcCardIsDragging && event.getAction() == MotionEvent.ACTION_MOVE) {
            // If card is already dragging, continue dragging it
            Log.d(TAG,"onTouch: Drag card continues");
            doDrag(event);
            selectDstCardStack(dstStack);

            handledEvent = true;
        }
        else if (srcCardIsDragging && event.getAction() == MotionEvent.ACTION_UP) {
            // A dragged card was released, so move it to the new location or return it to its original stack
//			if (cardCanMoveToStack(mSrcCard, dstStack)) {

            // For debugging, allow drop of card anywhere
            if (dstStack != null && dstStack != mSrcStack) {

                Log.d(TAG,"onTouch: Stack drag-drop new location");
                moveSingleCard(mSrcStack, dstStack, 0, true);
                clearSrcCardStack();
                clearDstCardStack();
            }
            else {
                Log.d(TAG,"onTouch: Stack drag-drop old location");
                moveSingleCard(mSrcStack, mSrcStack, 0, true);
                clearDstCardStack();
            }

            handledEvent = true;
        }

        // Track mouse-up events or we will incorrectly allow drag/drop activity in code above
        if (mSrcCard != null && event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG,"onTouch: Noting mouse-up on selected card");
            mSrcCard.setLastAction(Card.CardAction.MOUSE_UP);

            handledEvent = true;
        }

        return handledEvent;
    }


    /**
     * Support loading a bitmap from the app's assets
     *
     * @param fileName is the name of the bitmap file to load
     * @param options is the Options object to use in loading the bitmap
     * @return the Bitmap object that contains the loaded bitmap or null if an error occurred while loading the bitmap
     */
    public Bitmap getBitmap(String fileName, BitmapFactory.Options options) {
        Bitmap bitmap = null;

        try {
            AssetManager assets = getAssets();
            InputStream istream = assets.open(fileName);
            bitmap = BitmapFactory.decodeStream(istream, null, options);
            istream.close();
        } catch (IOException ex) {
            Log.e(TAG, "loadBitmap: Error loading bitmap " + fileName, ex);
        }

        return bitmap;
    }

    public Bitmap getBitmap(String fileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return getBitmap(fileName, options);
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

        this.cardGridWidth = (viewWidth / NUMGENERALSTACKS + 1);
        this.cardGridHeight = (this.naturalCardHeight * this.cardGridWidth) / naturalCardWidth;
        this.cardBorderSize = this.cardGridWidth / 8;
        this.currentCardWidth = this.cardGridWidth - 2*this.cardBorderSize;
        this.currentCardHeight = (this.naturalCardHeight * this.currentCardWidth) / naturalCardWidth;

        // Position the restart and undo icons
        int actionsSize = this.currentCardHeight / 2;
        restartSVGImage.centerAt(viewWidthHalf, this.cardBorderSize + actionsSize / 2, actionsSize);
        undoSVGImage.centerAt(viewWidthHalf, this.cardBorderSize + (3 * actionsSize ) / 2, actionsSize);

        // Place the ace stacks
        for (int i = 0; i < NUMACESTACKS; i++) {
            int stackLeft = this.cardBorderSize + i * (this.currentCardWidth + this.cardBorderSize);
            int stackRight = stackLeft + this.currentCardWidth;
            int stackTop = this.cardBorderSize;
            int stackBottom = stackTop + this.currentCardHeight;
            CardStack cardStack = this.aceStacks.get(i);
            cardStack.setBaseRect(stackLeft, stackTop, stackRight, stackBottom);
        }

        // Place the free cells
        for (int i = 0; i < NUMFREECELLSTACKS; i++) {
            int stackLeft = viewWidth -  (i + 1) * (this.currentCardWidth + this.cardBorderSize);
            int stackRight = stackLeft + this.currentCardWidth;
            int stackTop = this.cardBorderSize;
            int stackBottom = stackTop + this.currentCardHeight;
            CardStack cardStack = freecellStacks.get(i);
            cardStack.setBaseRect(stackLeft, stackTop, stackRight, stackBottom);
        }

        // Place the general stacks
        for (int i = 0; i < NUMGENERALSTACKS; i++) {
            int stackLeft = i * this.cardGridWidth + this.cardBorderSize;
            int stackRight = stackLeft + this.currentCardWidth;
            int stackTop = this.cardGridHeight + this.cardBorderSize;
            int stackBottom = stackTop + this.currentCardHeight;
            CardStack cardStack = generalStacks.get(i);
            cardStack.setBaseRect(stackLeft, stackTop, stackRight, stackBottom);
        }

//        if (!didInitialNewGame) {
//            // Start a new game
//            startGame(true);
//
//            didInitialNewGame = true;
//        }
    }
}
