package com.rrwood.adfreecell;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class GameView extends View {
    private final String TAG = "ROYDEBUG.GameView";

    // The card stacks (general, aces, freecells)
    private final ArrayList<CardStack> cardStacks = new ArrayList<>();

    // The icons (undo, redo, etc)
    private ArrayList<SVGImage> SVGImages = null;

//    private Paint grayPaint = null;
//    private Paint yellowPaint = null;
//    private Paint lightPaint = null;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initGameView();
    }

    public GameView(Context context) {
        super(context);

        initGameView();
    }

    private void initGameView() {
        SVGImages = new ArrayList<>();

//        grayPaint = new Paint();
////        ColorFilter filter = new LightingColorFilter(Color.LTGRAY, 0);
//        ColorFilter filter = new LightingColorFilter(0xffdddddd, 0);
//        grayPaint.setColorFilter(filter);
//
//        yellowPaint = new Paint();
//        yellowPaint.setARGB(255, 240, 240, 0);
//
//        lightPaint = new Paint();
////        mLowPaint.setARGB(0x1f, 0x00, 0x00, 0xdf);
//        ColorFilter lowFilter = new LightingColorFilter(0xffffffff, 0x005f5f5f);
//        lightPaint.setColorFilter(lowFilter);

        Log.d(TAG, "GameView: (w,h) = (" + this.getWidth() + "," + this.getHeight() + ")");
    }

    public void addCardStack(CardStack stack) {
        cardStacks.add(stack);
    }

    public void addSVImage(SVGImage SVGImage) {
        SVGImages.add(SVGImage);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: (w,h) = (" + w + "," + h + ", (oldw,oldh) = (" + oldw + "," + oldh + ")");
    }

    @Override
    public void onDraw (Canvas canvas) {
        // Clear background
        canvas.drawARGB(0xFF, 0x00, 0x80, 0x00);

        // Draw any icons
        for (SVGImage SVGImage : SVGImages) {
            SVGImage.drawSelf(canvas);
        }

        // Draw static cards first, then moving cards on top of everything else
        for (CardStack cardStack : cardStacks) {
            cardStack.drawStaticCards(canvas);
        }

        for (CardStack cardStack : cardStacks) {
            cardStack.drawMovingCards(canvas);
        }

//            Point p = cardStack.getLeftTop();
//            ArrayList<Card> cards = cardStack.getCards();
//
//            // Hilight stack if necessary
//            if (cardStack.isHilighted()) {
//                cardRect.set(cardStack.getRect());
//                cardRect.inset(-3.0f, -3.0f);
//
//                yellowPaint.setAlpha(255);
//                canvas.drawRoundRect(cardRect, 6.0f, 6.0f, yellowPaint);
//            }

//            for (Card card : cards) {
//                // Moving cards are drawn later, over top of all static cards
//                if (card.isMoving()) {
//                    movingCards.add(card);
//                } else {
//                    drawCard(card, canvas);
//                }
//            }

//        for (Card c : movingCards) {
//            drawCard(c, canvas);
//        }
    }
}