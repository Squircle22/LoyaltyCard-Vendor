package card.loyalty.loyaltycardvendor;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Sam on 22/04/2017.
 */

public class RecyclerClickListener extends RecyclerView.SimpleOnItemTouchListener {
    private static final String TAG = "RecyclerClickListener";

    interface OnRecyclerClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    private final OnRecyclerClickListener mListener;
    private final GestureDetectorCompat mGestureDetector;

    public RecyclerClickListener(Context context, final RecyclerView recyclerView, OnRecyclerClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress: starts");
                View childView = recyclerView.findChildViewUnder(e.getX(),e.getY());
                if (childView != null && mListener != null) {
                    Log.d(TAG, "onLongPress: calling listener.onLongClick");
                    mListener.onLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d(TAG, "onSingleTapConfirmed: starts");
                View childView = recyclerView.findChildViewUnder(e.getX(),e.getY());
                if (childView != null && mListener != null) {
                    Log.d(TAG, "onSingleTapConfirmed: calling listener.onClick");
                    mListener.onClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        Log.d(TAG, "onInterceptTouchEvent: starts");
        if (mGestureDetector != null) {
            boolean result = mGestureDetector.onTouchEvent(e);
            Log.d(TAG, "onInterceptTouchEvent: returned "+ result);
            return result;
        } else {
            Log.d(TAG, "onInterceptTouchEvent: returned: false");
            return false;
        }
    }
}
