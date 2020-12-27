package code.theducation.music.views;

import android.graphics.Rect;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.zhanghai.android.fastscroll.FastScroller;

public class ScrollingViewOnApplyWindowInsetsListener implements View.OnApplyWindowInsetsListener {

    @NonNull
    private final Rect mPadding = new Rect();
    @Nullable
    private final FastScroller mFastScroller;

    public ScrollingViewOnApplyWindowInsetsListener(
            @Nullable View view, @Nullable FastScroller fastScroller) {
        if (view != null) {
            mPadding.set(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom());
        }
        mFastScroller = fastScroller;
    }

    public ScrollingViewOnApplyWindowInsetsListener() {
        this(null, null);
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets insets) {
        view.setPadding(
                mPadding.left + insets.getSystemWindowInsetLeft(),
                mPadding.top,
                mPadding.right + insets.getSystemWindowInsetRight(),
                mPadding.bottom + insets.getSystemWindowInsetBottom());
        if (mFastScroller != null) {
            mFastScroller.setPadding(
                    insets.getSystemWindowInsetLeft(),
                    0,
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom());
        }
        return insets;
    }
}
