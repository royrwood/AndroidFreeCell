package com.rrwood.adfreecell;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class GameView extends View {
    private final String TAG = "ROYDEBUG.GameView";

    // The card stacks (general, aces, freecells)
    private final ArrayList<CardStack> cardStacks = new ArrayList<>();

    // The icons (undo, redo, etc)
    private ArrayList<SVGImage> SVGImages = null;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initGameView();
    }

    public GameView(Context context) {
        super(context);

        initGameView();
    }

    private void initGameView() {
        this.SVGImages = new ArrayList<>();

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
        for (SVGImage SVGImage : this.SVGImages) {
            SVGImage.drawSelf(canvas);
        }

        // Draw static cards first, then moving cards on top of everything else
        for (CardStack cardStack : this.cardStacks) {
            cardStack.drawStaticCards(canvas);
        }

        for (CardStack cardStack : this.cardStacks) {
            cardStack.drawMovingCards(canvas);
        }
    }
}
