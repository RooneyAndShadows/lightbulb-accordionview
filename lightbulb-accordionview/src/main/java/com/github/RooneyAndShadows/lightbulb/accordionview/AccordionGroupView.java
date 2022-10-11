package com.github.rooneyandshadows.lightbulb.accordionview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

@SuppressWarnings("unused")
public class AccordionGroupView extends LinearLayoutCompat {
    private int checkedId = -1;
    private boolean protectFromCheckedChange = false;
    private OnCheckedChangeListener onCheckedChangeListener;

    public AccordionGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        readAttributes(context, attrs);
        initView();
    }

    private void initView() {
        setOrientation(VERTICAL);
    }

    @Override
    public void addView(View child, int index) {
        if (!(child instanceof AccordionView)) {
            Log.w(AccordionView.class.getName(), "Child view is ignored. Reason: Child views must be " + AccordionView.class.getName());
            return;
        }
        setupInternalCallbacks((AccordionView) child);
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (!(child instanceof AccordionView)) {
            Log.w(AccordionView.class.getName(), "Child view is ignored. Reason: Child views must be " + AccordionView.class.getName());
            return;
        }
        setupInternalCallbacks((AccordionView) child);
        super.addView(child, width, height);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (!(child instanceof AccordionView)) {
            Log.w(AccordionView.class.getName(), "Child view is ignored. Reason: Child views must be " + AccordionView.class.getName());
            return;
        }
        setupInternalCallbacks((AccordionView) child);
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof AccordionView)) {
            Log.w(AccordionView.class.getName(), "Child view is ignored. Reason: Child views must be " + AccordionView.class.getName());
            return;
        }
        setupInternalCallbacks((AccordionView) child);
        super.addView(child, index, params);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setCheckedIdInternally(checkedId, false, false);
    }

    //@Override
    //protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    //    dispatchFreezeSelfOnly(container);
    //}

    //@Override
    //protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    //    dispatchThawSelfOnly(container);
    //}

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState myState = new SavedState(superState);
        myState.checkedId = checkedId;
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        checkedId = savedState.checkedId;
        AccordionView v = findViewById(checkedId);
        if (v != null)
            setCheckedIdInternally(checkedId, false, false);
    }

    public void setCheckedId(int newCheckId) {
        setCheckedId(newCheckId, true);
    }

    public void setCheckedId(int newCheckId, boolean animate) {
        if (checkedId == newCheckId)
            return;
        if (protectFromCheckedChange)
            return;
        protectFromCheckedChange = true;
        closeAllExceptFor(checkedId, animate);
        protectFromCheckedChange = false;
        setCheckedIdInternally(newCheckId, animate, true);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AccordionGroupView, 0, 0);
        try {
            checkedId = a.getResourceId(R.styleable.AccordionGroupView_AVG_SelectedId, -1);
        } finally {
            a.recycle();
        }
    }

    private void closeAllExceptFor(int id, boolean animate) {
        getChildren().stream()
                .filter(checkableView -> {
                    if (!checkableView.isExpanded())
                        return false;
                    if (checkableView.getId() == -1)
                        return false;
                    return checkableView.getId() != id;
                })
                .forEach(checkableView -> checkableView.collapse(animate));
    }

    private void setupInternalCallbacks(AccordionView targetView) {
        targetView.setOnGroupCheckedListener((view, expanded) -> {
            if (protectFromCheckedChange)
                return;
            protectFromCheckedChange = true;
            int idToCheck = -1;
            if (expanded) {
                idToCheck = view.getId();
                closeAllExceptFor(checkedId, true);
            }
            protectFromCheckedChange = false;
            if (checkedId == idToCheck)
                return;
            setCheckedIdInternally(idToCheck, true, true);
        });
    }

    private void setCheckedIdInternally(int newCheckedId, boolean animate, boolean notifyChange) {
        int previouslyCheckedId = this.checkedId;
        AccordionView previousCheckedView = findViewById(previouslyCheckedId);
        this.checkedId = newCheckedId;
        if (newCheckedId == -1 && previousCheckedView != null) {
            protectFromCheckedChange = true;
            if (previousCheckedView.isExpanded())
                previousCheckedView.collapse(animate);
            if (notifyChange && onCheckedChangeListener != null)
                onCheckedChangeListener.execute(checkedId, previousCheckedView);
            protectFromCheckedChange = false;
        } else {
            AccordionView viewToCheck = findViewById(checkedId);
            if (viewToCheck != null && !viewToCheck.isExpanded())
                viewToCheck.expand(animate);
            if (notifyChange && onCheckedChangeListener != null)
                onCheckedChangeListener.execute(checkedId, viewToCheck);
        }
    }

    private ArrayList<AccordionView> getChildren() {
        int childCount = getChildCount();
        ArrayList<AccordionView> children = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            children.add((AccordionView) getChildAt(i));
        }
        return children;
    }

    public interface OnCheckedChangeListener {
        void execute(int checkedId, AccordionView view);
    }

    private static class SavedState extends BaseSavedState {
        private int checkedId;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            checkedId = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(checkedId);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}