package com.example.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.jobs.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MCardViewBehavior extends CoordinatorLayout.Behavior<MaterialCardView> {
    private Rect tmpRect;
    private static final ThreadLocal<Matrix> matrix = new ThreadLocal<>();
    private static final ThreadLocal<RectF> rectF = new ThreadLocal<>();

    public MCardViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.MCardViewBehavior);
        a.recycle();
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull MaterialCardView child, int layoutDirection) {
        final List<View> dependencies = parent.getDependencies(child);
        for (int i = 0, count = dependencies.size(); i < count; i++) {
            final View dependency = dependencies.get(i);
            if (dependency instanceof AppBarLayout) {
                if (updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child)) {
                    break;
                }
            }
        }
        // Now let the CoordinatorLayout lay out the FAB
        parent.onLayoutChild(child, layoutDirection);
        return true;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull MaterialCardView child, @NonNull View dependency) {
        if (dependency instanceof AppBarLayout) {
            // If we're depending on an AppBarLayout we will show/hide it automatically
            // if the FAB is anchored to the AppBarLayout
            updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child);
        }
        return false;
    }

    private boolean updateFabVisibilityForAppBarLayout(
            CoordinatorLayout parent,
            @NonNull AppBarLayout appBarLayout,
            @NonNull MaterialCardView child) {

        if (tmpRect == null) {
            tmpRect = new Rect();
        }

        // First, let's get the visible rect of the dependency
        final Rect rect = tmpRect;
        getDescendantRect(parent, appBarLayout, rect);
        if (rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
            // If the anchor's bottom is below the seam, we'll animate our FAB out
            child.setVisibility(View.GONE);
        } else {
            // Else, we'll animate our FAB back in
            child.setVisibility(View.VISIBLE);
        }
        return true;
    }

    public static void getDescendantRect(
            @NonNull ViewGroup parent, @NonNull View descendant, @NonNull Rect out) {
        out.set(0, 0, descendant.getWidth(), descendant.getHeight());
        offsetDescendantRect(parent, descendant, out);
    }

    public static void offsetDescendantRect(
            @NonNull ViewGroup parent, @NonNull View descendant, @NonNull Rect rect) {
        Matrix m = matrix.get();
        if (m == null) {
            m = new Matrix();
            matrix.set(m);
        } else {
            m.reset();
        }

        offsetDescendantMatrix(parent, descendant, m);

        RectF rectF = MCardViewBehavior.rectF.get();
        if (rectF == null) {
            rectF = new RectF();
            rectF.set(rectF);
        }
        rectF.set(rect);
        m.mapRect(rectF);
        rect.set(
                (int) (rectF.left + 0.5f),
                (int) (rectF.top + 0.5f),
                (int) (rectF.right + 0.5f),
                (int) (rectF.bottom + 0.5f));
    }

    private static void offsetDescendantMatrix(
            ViewParent target, @NonNull View view, @NonNull Matrix m) {
        final ViewParent parent = view.getParent();
        if (parent instanceof View && parent != target) {
            final View vp = (View) parent;
            offsetDescendantMatrix(target, vp, m);
            m.preTranslate(-vp.getScrollX(), -vp.getScrollY());
        }

        m.preTranslate(view.getLeft(), view.getTop());

        if (!view.getMatrix().isIdentity()) {
            m.preConcat(view.getMatrix());
        }
    }

}
