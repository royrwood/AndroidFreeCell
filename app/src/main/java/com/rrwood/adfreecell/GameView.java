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
        Log.d(TAG,"onDraw: Redrawing...");

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


/*
@Override
public void onDraw (Canvas canvas) {
    // Clear background
    canvas.drawARGB(0xFF, 0x00, 0x80, 0x00);

    // Draw any icons
    for (Icon icon : mIcons) {
        Bitmap bitmap = icon.getBitmap();
        Point pt = icon.getPt();

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, pt.x, pt.y, null);
        }
    }


    // Clear list of moving cards (they must be drawn after all the static cards!)
    mMovingCards.clear();

    // Draw each stack of cards
    for (CardStack cardStack : mCardStacks) {
        Point p = cardStack.getLeftTop();
        Bitmap bitmap = cardStack.getBitmap();
        ArrayList<Card> cards = cardStack.getCards();

        // Hilight stack if necessary
        if (cardStack.isHilighted()) {
            mRectF.set(cardStack.getRect());
            mRectF.inset(-3.0f, -3.0f);

            mYellowPaint.setAlpha(255);
            canvas.drawRoundRect(mRectF, 6.0f, 6.0f, mYellowPaint);
        }

        canvas.drawBitmap(bitmap, p.x, p.y, null);

        for (Card card : cards) {
            // Moving cards are drawn later, over top of all static cards
            if (card.isMoving()) {
                mMovingCards.add(card);
            }
            else {
                drawCard(card, canvas);
            }
        }
    }

    // Draw cards that are in motion last so they are on top of everything else
    for (Card c : mMovingCards) {
        drawCard(c, canvas);
    }
}


    private void drawCard(Card card, Canvas canvas) {
        Point p = card.getLeftTop();
        boolean isSrcCard = card.isSrcCard();
        boolean isDstCard = card.isDstCard();
        Bitmap bitmap = card.getBitmap();

        if (isDstCard) {
            int alpha = card.getHiliteAlpha();
            mYellowPaint.setAlpha(alpha);
            mRectF.set(card.getRect());
            mRectF.inset(-5.0f, -5.0f);
            canvas.drawRoundRect(mRectF, 6.0f, 6.0f, mYellowPaint);
        }

        if (isSrcCard) {
            canvas.drawBitmap(bitmap, p.x, p.y, mGrayPaint);
//			canvas.drawBitmap(bitmap, p.x, p.y, null);
        }
        else {
            canvas.drawBitmap(bitmap, p.x, p.y, null);
//			canvas.drawBitmap(bitmap, p.x, p.y, mLightPaint);
        }
    }
 */