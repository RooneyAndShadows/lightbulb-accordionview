package com.github.rooneyandshadows.lightbulb.accordionview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.rooneyandshadows.lightbulb.commons.utils.DrawableUtils;
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils;
import com.github.rooneyandshadows.lightbulb.dialogs.base.BaseDialogFragment;
import com.github.rooneyandshadows.lightbulb.dialogs.dialog_alert.AlertDialog;
import com.github.rooneyandshadows.lightbulb.dialogs.dialog_alert.AlertDialogBuilder;
import com.github.rooneyandshadows.java.commons.string.StringUtils;
import com.github.rooneyandshadows.lightbulb.accordionview.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

@SuppressWarnings("unused")
public class AccordionView extends LinearLayout {
    private LinearLayout accordionView;
    private LinearLayout accordionHeaderContainer;
    private RelativeLayout contentContainer;
    private TextView headingTextView;
    private AppCompatImageButton additionalInfoButton;
    private AppCompatImageButton expandButton;
    private int animationDuration = 250;
    private int backgroundCornerRadius = 0;
    private int headingTextSize;
    private int additionalInfoDrawableColor;
    private int expandDrawableColor;
    private int backgroundColor;
    private int headingTextColor;
    private Drawable additionalInfoIcon;
    private Drawable expandIcon;
    private Drawable collapseIcon;
    private Drawable backgroundDrawable;
    private boolean expanded = false;
    private boolean expandable = true;
    private String headingText;
    private ExpansionListeners expandListeners;
    private AccordionAnimation anim;
    private ContentPositionType contentPosition;
    private AccordionAnimationType animationType;
    private AlertDialog dialog;
    private FragmentManager manager;
    private boolean dialogEnabled;
    private String DIALOG_TAG;
    private String dialogTitle;
    private String dialogMessage;
    private String dialogButtonText;

    public AccordionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        readAttributes(context, attrs);
        accordionView = (LinearLayout) inflate(getContext(), R.layout.view_accordion_layout, this);
        initView();
    }

    public void setExpandListeners(ExpansionListeners expandListeners) {
        this.expandListeners = expandListeners;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
        invalidate();
    }

    public void setDialogMessage(String dialogMessage) {
        this.dialogMessage = dialogMessage;
        invalidate();
    }

    public void setDialogButtonText(String dialogButtonText) {
        this.dialogButtonText = dialogButtonText;
        invalidate();
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public void setBackgroundCornerRadius(int backgroundCornerRadius) {
        this.backgroundCornerRadius = backgroundCornerRadius;
        invalidate();
    }

    public void setBackground(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public void setBackground(Drawable backgroundDrawable) {
        this.backgroundDrawable = backgroundDrawable;
        invalidate();
    }

    public void setHeadingText(String headingText) {
        this.headingText = headingText;
        headingTextView.setText(headingText);
    }

    @BindingAdapter("accordionHeadingText")
    public static void setHeadingText(AccordionView view, String text) {
        view.setHeadingText(text);
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
        invalidate();
    }

    public void expand(boolean animated) {
        AccordionView view = this;
        if (expandable && expanded || (animated && anim.hasRunningAnimation()))
            return;
        if (animated)
            anim.expand(animationDuration);
        else
            contentContainer.setVisibility(VISIBLE);
        expanded = true;
        expandButton.setImageDrawable(collapseIcon);
        if (expandListeners != null)
            expandListeners.onExpanded(view);
    }

    public void collapse(boolean animated) {
        AccordionView view = this;
        if (expandable && !expanded || (animated && anim.hasRunningAnimation()))
            return;
        if (animated)
            anim.collapse(animationDuration);
        else
            contentContainer.setVisibility(GONE);
        expanded = false;
        expandButton.setImageDrawable(expandIcon);
        if (expandListeners != null)
            expandListeners.onCollapsed(view);
    }

    private void initView() {
        selectChildren();
        setupHeader();
        initAnimation();
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AccordionView, 0, 0);
        try {
            DIALOG_TAG = a.getString(R.styleable.AccordionView_AV_DialogTag);
            dialogTitle = a.getString(R.styleable.AccordionView_AV_DialogTitle);
            dialogMessage = a.getString(R.styleable.AccordionView_AV_DialogMessage);
            headingText = a.getString(R.styleable.AccordionView_AV_HeadingText);
            dialogButtonText = a.getString(R.styleable.AccordionView_AV_DialogButtonText);
            if (StringUtils.isNullOrEmptyString(headingText))
                headingText = "HEADING";
            if (StringUtils.isNullOrEmptyString(DIALOG_TAG))
                DIALOG_TAG = "ACCORDION_TOOLTIP_TAG";
            if (StringUtils.isNullOrEmptyString(dialogTitle))
                dialogTitle = "Title";
            if (StringUtils.isNullOrEmptyString(dialogMessage))
                dialogMessage = "Message";
            contentPosition = ContentPositionType.valueOf(a.getInt(R.styleable.AccordionView_AV_ContentPosition, 1));
            animationType = AccordionAnimationType.valueOf(a.getInt(R.styleable.AccordionView_AV_AnimationType, 1));
            additionalInfoDrawableColor = a.getColor(R.styleable.AccordionView_AV_InfoIconColor, ResourceUtils.getColorByAttribute(getContext(), R.attr.colorOnSurface));
            expandDrawableColor = a.getColor(R.styleable.AccordionView_AV_ExpandIconColor, ResourceUtils.getColorByAttribute(getContext(), R.attr.colorOnSurface));
            headingTextColor = a.getColor(R.styleable.AccordionView_AV_HeadingTextColor, ResourceUtils.getColorByAttribute(getContext(), R.attr.colorOnSurface));
            backgroundColor = a.getColor(R.styleable.AccordionView_AV_BackgroundColor, ResourceUtils.getColorByAttribute(getContext(), R.attr.colorSurface));
            headingTextSize = a.getDimensionPixelSize(R.styleable.AccordionView_AV_HeadingTextSize, ResourceUtils.getDimenPxById(context, R.dimen.av_heading_text_size));
            backgroundDrawable = a.getDrawable(R.styleable.AccordionView_AV_BackgroundDrawable);
            animationDuration = a.getInteger(R.styleable.AccordionView_AV_AnimationDuration, animationDuration);
            backgroundCornerRadius = a.getInteger(R.styleable.AccordionView_AV_BackgroundCornerRadius, backgroundCornerRadius);
            expanded = a.getBoolean(R.styleable.AccordionView_AV_Expanded, expanded);
            expandable = a.getBoolean(R.styleable.AccordionView_AV_Expandable, expandable);
            dialogEnabled = a.getBoolean(R.styleable.AccordionView_AV_DialogEnabled, dialogEnabled);
        } finally {
            a.recycle();
        }
    }

    private void selectChildren() {
        accordionHeaderContainer = accordionView.findViewWithTag("accordion_header_container");
        RelativeLayout accordionBelowContent = accordionView.findViewWithTag("content_container_below");
        RelativeLayout accordionDefaultContent = accordionView.findViewWithTag("content_container_default");
        headingTextView = accordionView.findViewWithTag("accordionHeadingText");
        additionalInfoButton = accordionView.findViewWithTag("accordionInformationButton");
        expandButton = accordionView.findViewWithTag("accordionExpandButton");
        switch (contentPosition) {
            case BELOW_HEADER:
                contentContainer = accordionBelowContent;
                break;
            default:
                contentContainer = accordionDefaultContent;
                break;
        }
    }

    private void setupHeader() {
        setClipChildren(false);
        setClipToPadding(false);
        headingTextView.setText(headingText);
        headingTextView.setTextColor(headingTextColor);
        headingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, headingTextSize);
        expandIcon = ResourceUtils.getDrawable(getContext(), R.drawable.icon_expand);
        collapseIcon = ResourceUtils.getDrawable(getContext(), R.drawable.icon_collapse);
        expandIcon.setTint(expandDrawableColor);
        collapseIcon.setTint(expandDrawableColor);
        setupHeaderBackground();
        setupInitialExpandState();
        if (isInEditMode())
            return;
        initializeInformationButton();
        initializeExpandButton();
    }

    private void initializeInformationButton() {
        if (!dialogEnabled) {
            additionalInfoButton.setVisibility(GONE);
            return;
        }
        manager = ((FragmentActivity) getContext()).getSupportFragmentManager();
        dialog = new AlertDialogBuilder(manager, DIALOG_TAG)
                .withDialogType(BaseDialogFragment.DialogTypes.BOTTOM_SHEET)
                .withTitle(dialogTitle)
                .withMessage(dialogMessage)
                .withCancelOnClickOutsude(true)
                .withPositiveButton(new BaseDialogFragment.DialogButtonConfiguration(dialogButtonText, true, true), null)
                .buildDialog();
        additionalInfoIcon = ResourceUtils.getDrawable(getContext(), R.drawable.icon_info);
        additionalInfoIcon.setTint(additionalInfoDrawableColor);
        additionalInfoButton.setImageDrawable(additionalInfoIcon);
        additionalInfoButton.setOnClickListener(view -> dialog.show());
    }

    private void setupHeaderBackground() {
        if (backgroundDrawable != null)
            accordionHeaderContainer.setBackground(backgroundDrawable);
        else
            accordionHeaderContainer.setBackground(DrawableUtils.getRoundedShapeWithColor(backgroundColor, backgroundCornerRadius));
    }

    private void setupInitialExpandState() {
        if (expanded)
            if (contentContainer.getVisibility() == GONE)
                contentContainer.setVisibility(VISIBLE);
        if (!expanded)
            if (contentContainer.getVisibility() == VISIBLE)
                contentContainer.setVisibility(VISIBLE);
    }

    private void initializeExpandButton() {
        if (expanded) {
            if (expandable)
                expandButton.setImageDrawable(collapseIcon);
        } else {
            if (expandable)
                expandButton.setImageDrawable(expandIcon);
        }
        if (expandable) {
            expandButton.setOnClickListener(view -> {
                if (expanded) {
                    collapse(true);
                } else {
                    expand(true);
                }
            });
        } else {
            expandButton.setVisibility(GONE);
        }
    }

    private void initAnimation() {
        switch (animationType) {
            case ANIM_NONE:
                anim = new AccordionShowHideAnimation(contentContainer);
                break;
            case ANIM_HEIGHT_TRANSITION:
                anim = new AccordionTransitionAnimation(contentContainer);
                break;
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (contentContainer != null) { //check if is initial inflation
            contentContainer.addView(child, index, params);
        } else
            super.addView(child, index, params);
    }

    // UNCOMMENT FOR SAVE STATE ONLY ON ROOT VIEW WITHOUT CHILD VIEWS
    /*@Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }*/

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState myState = new SavedState(superState);
        myState.title = this.dialogTitle;
        myState.subtitle = this.dialogMessage;
        myState.buttonText = this.dialogButtonText;
        myState.expanded = this.expanded;
        myState.backgroundColor = this.backgroundColor;
        myState.cornerRadius = this.backgroundCornerRadius;
        myState.headingTextSize = this.headingTextSize;
        myState.animationDuration = this.animationDuration;
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.dialogTitle = savedState.title;
        this.dialogMessage = savedState.subtitle;
        this.dialogButtonText = savedState.buttonText;
        this.expanded = savedState.expanded;
        this.backgroundColor = savedState.backgroundColor;
        this.backgroundCornerRadius = savedState.cornerRadius;
        this.headingTextSize = savedState.headingTextSize;
        this.animationDuration = savedState.animationDuration;
        initView();
    }

    private static class SavedState extends BaseSavedState {
        private String title;
        private String subtitle;
        private String buttonText;
        private boolean expanded;
        private int cornerRadius;
        private int headingTextSize;
        private int backgroundColor;
        private int animationDuration;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            title = in.readString();
            subtitle = in.readString();
            buttonText = in.readString();
            cornerRadius = in.readInt();
            headingTextSize = in.readInt();
            backgroundColor = in.readInt();
            animationDuration = in.readInt();
            expanded = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(title);
            out.writeString(subtitle);
            out.writeString(buttonText);
            out.writeInt(cornerRadius);
            out.writeInt(headingTextSize);
            out.writeInt(backgroundColor);
            out.writeInt(animationDuration);
            out.writeInt(expanded ? 1 : 0);
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

    public interface ExpansionListeners {
        void onExpanded(AccordionView view);

        void onCollapsed(AccordionView view);
    }

    private enum ContentPositionType {
        INSIDE_HEADER(1),
        BELOW_HEADER(2);

        private int value;
        private static SparseArray<ContentPositionType> map = new SparseArray<>();

        ContentPositionType(int value) {
            this.value = value;
        }

        static {
            for (ContentPositionType contentPosition : ContentPositionType.values()) {
                map.put(contentPosition.value, contentPosition);
            }
        }

        public static ContentPositionType valueOf(int contentPosition) {
            return map.get(contentPosition);
        }

        public int getValue() {
            return value;
        }
    }

    private enum AccordionAnimationType {
        ANIM_NONE(1),
        ANIM_HEIGHT_TRANSITION(2);

        private int value;
        private static SparseArray<AccordionAnimationType> map = new SparseArray<>();

        AccordionAnimationType(int value) {
            this.value = value;
        }

        static {
            for (AccordionAnimationType animation : AccordionAnimationType.values()) {
                map.put(animation.value, animation);
            }
        }

        public static AccordionAnimationType valueOf(int animation) {
            return map.get(animation);
        }

        public int getValue() {
            return value;
        }
    }
}