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

    // A list of all the card stacks that need drawing
    private final ArrayList<CardStack> cardStacks = new ArrayList<>();

    // A list of cards that are moving and need drawing after all the card stacks
    private ArrayList<Card> movingCards = null;

    // A list of icons to draw
    private ArrayList<SVGImage> SVGImages = null;

    // A Paint used for highlighting cards
    private Paint grayPaint = null;
    private Paint yellowPaint = null;
    private Paint lightPaint = null;

    // Temp rect used when drawing (lint complains if we create this during drawing)
    private final RectF cardRect = new RectF();


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initGameView();
    }

    public GameView(Context context) {
        super(context);

        initGameView();
    }

    private void initGameView() {
        movingCards = new ArrayList<>();

        SVGImages = new ArrayList<>();

        grayPaint = new Paint();
//        ColorFilter filter = new LightingColorFilter(Color.LTGRAY, 0);
        ColorFilter filter = new LightingColorFilter(0xffdddddd, 0);
        grayPaint.setColorFilter(filter);

        yellowPaint = new Paint();
        yellowPaint.setARGB(255, 240, 240, 0);

        lightPaint = new Paint();
//        mLowPaint.setARGB(0x1f, 0x00, 0x00, 0xdf);
        ColorFilter lowFilter = new LightingColorFilter(0xffffffff, 0x005f5f5f);
        lightPaint.setColorFilter(lowFilter);

        Log.d(TAG, "GameView: (w,h) = (" + this.getWidth() + "," + this.getHeight() + ")");
    }


    /**
     * Add a CardStack to the list of card stacks to draw during screen updates
     *
     * @param stack the CardStack to track
     */
    public void addCardStack(CardStack stack) {
        cardStacks.add(stack);
    }


    /**
     * Add an icon to the display
     *
     * @param SVGImage the Icon to add
     */
    public void addIcon(SVGImage SVGImage) {
        SVGImages.add(SVGImage);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: (w,h) = (" + w + "," + h + ", (oldw,oldh) = (" + oldw + "," + oldh + ")");
    }


    /**
     * Draw the view and all the CardStacks attached to it
     */
    @Override
    public void onDraw (Canvas canvas) {
        // Clear background
        canvas.drawARGB(0xFF, 0x00, 0x80, 0x00);


        // Draw any icons
        for (SVGImage SVGImage : SVGImages) {
            SVGImage.drawSelf(canvas);
        }


        // Clear list of moving cards (they must be drawn after all the static cards!)
        movingCards.clear();

        // Draw each stack of cards

        for (CardStack cardStack : cardStacks) {
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

            cardStack.drawStaticCards(canvas);

//            for (Card card : cards) {
//                // Moving cards are drawn later, over top of all static cards
//                if (card.isMoving()) {
//                    movingCards.add(card);
//                } else {
//                    drawCard(card, canvas);
//                }
//            }
        }

        // Draw cards that are in motion last so they are on top of everything else
//        for (Card c : movingCards) {
//            drawCard(c, canvas);
//        }


//        Context context = this.getContext();
//        Resources res = context.getResources();
//        Drawable myDrawable = ResourcesCompat.getDrawable(res, R.drawable.restart_icon_256x256, null);
//        if (myDrawable != null) {
//            Log.d(TAG, "Drawing Drawable test_png");
//            int indent = 20;
////            int width = myDrawable.getIntrinsicWidth();
//            int width = 64;
////            int height = myDrawable.getIntrinsicHeight();
//            int height = 64;
//            myDrawable.setBounds(indent, indent, indent + width, indent + height);
//            myDrawable.setAlpha(255);
//            myDrawable.invalidateSelf();
//            myDrawable.draw(canvas);
//        }


//        Context context = this.getContext();
//        Resources res = context.getResources();
////        Drawable myDrawable = ResourcesCompat.getDrawable(res, R.drawable.undo_svg, null);
//        Drawable myDrawable = ResourcesCompat.getDrawable(res, R.drawable.clubs_ace_svg, null);
//        if (myDrawable != null) {
//            Log.d(TAG, "Drawing Drawable clubs_ace_svg");
//            int indent = 20;
//            int width = myDrawable.getIntrinsicWidth();
//            int height = myDrawable.getIntrinsicHeight();
//            int left = indent;
//            int right = left + width;
//            for (int i = 0;i < 4; i++) {
//                myDrawable.setBounds(left, indent, right, indent + height / (i + 1));
//                myDrawable.draw(canvas);
//                left = right + indent;
//                right = left + width / (i + 2);
//            }


//            Log.d(TAG, "Drawing Drawable bitmap");
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                myDrawable = (DrawableCompat.wrap(myDrawable)).mutate();
//            }
//            Bitmap bitmap = Bitmap.createBitmap(myDrawable.getIntrinsicWidth(), myDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//            Canvas tempCanvas = new Canvas(bitmap);
//            myDrawable.setBounds(0, 0, tempCanvas.getWidth(), tempCanvas.getHeight());
//            myDrawable.draw(canvas);
//            canvas.drawBitmap(bitmap, 20, 20, null);

//            private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
//                Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
//                        vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(bitmap);
//                vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//                vectorDrawable.draw(canvas);
//                Log.e(TAG, "getBitmap: 1");
//                return bitmap;
//            }
    }


//    private void drawCard(Card card, Canvas canvas) {
//        Point p = card.getLeftTop();
//        boolean isSrcCard = card.isSrcCard();
//        boolean isDstCard = card.isDstCard();
//        Bitmap bitmap = card.getBitmap();
//
//        if (isDstCard) {
//            int alpha = card.getHiliteAlpha();
//            yellowPaint.setAlpha(alpha);
//            cardRect.set(card.getRect());
//            cardRect.inset(-5.0f, -5.0f);
//            canvas.drawRoundRect(cardRect, 6.0f, 6.0f, yellowPaint);
//        }
//
//        if (isSrcCard) {
//            canvas.drawBitmap(bitmap, p.x, p.y, grayPaint);
////			canvas.drawBitmap(bitmap, p.x, p.y, null);
//        }
//        else {
//            canvas.drawBitmap(bitmap, p.x, p.y, null);
////			canvas.drawBitmap(bitmap, p.x, p.y, mLightPaint);
//        }
//    }
}