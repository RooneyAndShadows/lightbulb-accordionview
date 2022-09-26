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

import com.github.rooneyandshadows.java.commons.string.StringUtils;
import com.github.rooneyandshadows.lightbulb.accordionview.animation.AccordionAnimation;
import com.github.rooneyandshadows.lightbulb.accordionview.animation.AccordionShowHideAnimation;
import com.github.rooneyandshadows.lightbulb.accordionview.animation.AccordionTransitionAnimation;
import com.github.rooneyandshadows.lightbulb.commons.utils.DrawableUtils;
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils;
import com.github.rooneyandshadows.lightbulb.dialogs.base.BaseDialogFragment;
import com.github.rooneyandshadows.lightbulb.dialogs.dialog_alert.AlertDialog;
import com.github.rooneyandshadows.lightbulb.dialogs.dialog_alert.AlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

@SuppressWarnings("unused")
public class AccordionView extends LinearLayoutCompat {
    private LinearLayoutCompat accordionHeaderContainer;
    private ConstraintLayout accordionHeader;
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
    private int headingTextAppearance = -1;
    private Drawable expandIcon;
    private Drawable collapseIcon;
    private Drawable backgroundDrawable;
    private boolean expanded = false;
    private boolean expandable = true;
    private boolean expandOnHeadingClick = false;
    private String headingText;
    private ExpansionListeners expandListeners;
    private ExpansionListeners onGroupCheckedListener;
    private AccordionAnimation anim;
    private ContentPositionType contentPosition;
    private AccordionAnimationType animationType;
    private AlertDialog dialog;
    private boolean dialogEnabled;
    private String DIALOG_TAG;
    private String dialogTitle;
    private String dialogMessage;
    private String dialogButtonText;
    private boolean inflated;

    public AccordionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        readAttributes(context, attrs);
        inflate(getContext(), R.layout.view_accordion_layout, this);
        inflated = true;
        initView();
    }

    @Override
    public void setClipChildren(boolean clipChildren) {
        super.setClipChildren(clipChildren);
        if (inflated)
            setupClips();
    }

    @Override
    public void setClipToPadding(boolean clipToPadding) {
        super.setClipToPadding(clipToPadding);
        if (inflated)
            setupClips();
    }

    void setOnGroupCheckedListener(ExpansionListeners listener) {
        onGroupCheckedListener = listener;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpandListeners(ExpansionListeners expandListeners) {
        this.expandListeners = expandListeners;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
        dialog.setTitle(dialogTitle);
    }

    public void setDialogMessage(String dialogMessage) {
        this.dialogMessage = dialogMessage;
        dialog.setMessage(dialogMessage);
    }

    public void setDialogButtonText(String dialogButtonText) {
        this.dialogButtonText = dialogButtonText;
        initializeInformationButton();
    }

    public void setHeadingTextColor(int headingTextColor) {
        this.headingTextColor = headingTextColor;
        headingTextView.setTextColor(headingTextColor);
    }

    public void setHeadingTextSize(int headingTextSize) {
        this.headingTextSize = headingTextSize;
        headingTextView.setTextSize(headingTextSize);
    }

    public void setHeadingTextAppearance(int headingTextAppearance) {
        this.headingTextAppearance = headingTextAppearance;
        headingTextView.setTextAppearance(headingTextAppearance);
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public void setBackgroundCornerRadius(int backgroundCornerRadius) {
        this.backgroundCornerRadius = backgroundCornerRadius;
        setupHeader();
    }

    public void setBackground(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        setupHeader();
    }

    public void setBackground(Drawable backgroundDrawable) {
        this.backgroundDrawable = backgroundDrawable;
        setupHeader();
    }

    public void setExpandOnHeadingClick(boolean expandOnHeadingClick) {
        this.expandOnHeadingClick = expandOnHeadingClick;
        setupHeader();
    }

    public void setHeadingText(String headingText) {
        this.headingText = headingText;
        headingTextView.setText(headingText);
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
        initializeExpandButton();
    }

    public void setContentPosition(ContentPositionType contentPosition) {
        ViewGroup oldContainer = contentContainer;
        this.contentPosition = contentPosition;
        RelativeLayout accordionBelowContent = findViewById(R.id.content_container_below);
        RelativeLayout accordionDefaultContent = findViewById(R.id.content_container_default);
        if (this.contentPosition == ContentPositionType.BELOW_HEADER)
            contentContainer = accordionBelowContent;
        else contentContainer = accordionDefaultContent;
        if (oldContainer != contentContainer)
            while (oldContainer.getChildCount() > 0) {
                contentContainer.addView(oldContainer.getChildAt(0));
                oldContainer.removeViewAt(0);
            }
        setupInitialExpandState();
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
            expandListeners.execute(view, true);
        if (onGroupCheckedListener != null)
            onGroupCheckedListener.execute(view, true);
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
            onGroupCheckedListener.execute(view, false);
        if (onGroupCheckedListener != null)
            onGroupCheckedListener.execute(view, false);
    }

    @BindingAdapter("accordionHeadingText")
    public static void setHeadingText(AccordionView view, String text) {
        view.setHeadingText(text);
    }

    private void initView() {
        setOrientation(VERTICAL);
        selectChildren();
        setupClips();
        initializeHeader();
        initAnimation();
    }

    private void setupClips() {
        RelativeLayout accordionBelowContent = findViewById(R.id.content_container_below);
        RelativeLayout accordionDefaultContent = findViewById(R.id.content_container_default);
        accordionBelowContent.setClipToPadding(getClipToPadding());
        accordionDefaultContent.setClipToPadding(getClipToPadding());
        accordionHeaderContainer.setClipToPadding(getClipToPadding());
        accordionHeader.setClipToPadding(getClipToPadding());
        accordionBelowContent.setClipChildren(getClipChildren());
        accordionDefaultContent.setClipChildren(getClipChildren());
        accordionHeaderContainer.setClipChildren(getClipChildren());
        accordionHeader.setClipChildren(getClipChildren());
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
                headingText = ResourceUtils.getPhrase(context, R.string.av_heading_default_text);
            if (StringUtils.isNullOrEmptyString(DIALOG_TAG))
                DIALOG_TAG = "ACCORDION_TOOLTIP_TAG";
            if (StringUtils.isNullOrEmptyString(dialogTitle))
                dialogTitle = ResourceUtils.getPhrase(context, R.string.av_default_dialog_title_text);
            if (StringUtils.isNullOrEmptyString(dialogMessage))
                dialogMessage = ResourceUtils.getPhrase(context, R.string.av_default_dialog_message_text);
            contentPosition = ContentPositionType.valueOf(a.getInt(R.styleable.AccordionView_AV_ContentPosition, 1));
            animationType = AccordionAnimationType.valueOf(a.getInt(R.styleable.AccordionView_AV_AnimationType, 1));
            additionalInfoDrawableColor = a.getColor(R.styleable.AccordionView_AV_InfoIconColor, ResourceUtils.getColorByAttribute(getContext(), R.attr.colorOnSurface));
            expandDrawableColor = a.getColor(R.styleable.AccordionView_AV_ExpandIconColor, ResourceUtils.getColorByAttribute(getContext(), R.attr.colorOnSurface));
            headingTextColor = a.getColor(R.styleable.AccordionView_AV_HeadingTextColor, -1);
            backgroundColor = a.getColor(R.styleable.AccordionView_AV_BackgroundColor, ResourceUtils.getColorByAttribute(getContext(), R.attr.colorSurface));
            headingTextSize = a.getDimensionPixelSize(R.styleable.AccordionView_AV_HeadingTextSize, -1);
            headingTextAppearance = a.getResourceId(R.styleable.AccordionView_AV_HeadingTextAppearance, R.style.Accordion_HeadingTextAppearance);
            backgroundDrawable = a.getDrawable(R.styleable.AccordionView_AV_BackgroundDrawable);
            animationDuration = a.getInteger(R.styleable.AccordionView_AV_AnimationDuration, animationDuration);
            backgroundCornerRadius = a.getInteger(R.styleable.AccordionView_AV_BackgroundCornerRadius, backgroundCornerRadius);
            expanded = a.getBoolean(R.styleable.AccordionView_AV_Expanded, expanded);
            expandable = a.getBoolean(R.styleable.AccordionView_AV_Expandable, expandable);
            expandOnHeadingClick = a.getBoolean(R.styleable.AccordionView_AV_ExpandOnHeadingClick, expandOnHeadingClick);
            dialogEnabled = a.getBoolean(R.styleable.AccordionView_AV_DialogEnabled, dialogEnabled);
        } finally {
            a.recycle();
        }
    }

    private void selectChildren() {
        accordionHeaderContainer = findViewById(R.id.accordion_header_container);
        accordionHeader = findViewById(R.id.accordion_header);
        RelativeLayout accordionBelowContent = findViewById(R.id.content_container_below);
        RelativeLayout accordionDefaultContent = findViewById(R.id.content_container_default);
        headingTextView = findViewById(R.id.accordionHeadingText);
        additionalInfoButton = findViewById(R.id.accordionInformationButton);
        expandButton = findViewById(R.id.accordionExpandButton);
        if (contentPosition == ContentPositionType.BELOW_HEADER)
            contentContainer = accordionBelowContent;
        else contentContainer = accordionDefaultContent;
    }

    private void initializeHeader() {
        headingTextView.setText(headingText);
        headingTextView.setTextAppearance(headingTextAppearance);
        if (headingTextColor != -1)
            headingTextView.setTextColor(headingTextColor);
        if (headingTextSize != -1)
            headingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, headingTextSize);
        expandIcon = ResourceUtils.getDrawable(getContext(), R.drawable.icon_expand);
        collapseIcon = ResourceUtils.getDrawable(getContext(), R.drawable.icon_collapse);
        expandIcon.setTint(expandDrawableColor);
        collapseIcon.setTint(expandDrawableColor);
        setupHeader();
        setupInitialExpandState();
        if (isInEditMode())
            return;
        initializeInformationButton();
        initializeExpandButton();
        measureWithButtons();
    }

    private void measureWithButtons() {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) accordionHeader.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        expandButton.setVisibility(VISIBLE);
        additionalInfoButton.setVisibility(VISIBLE);
        accordionHeader.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        accordionHeader.setMinHeight(accordionHeader.getMeasuredHeight());
    }

    private void initializeInformationButton() {
        if (!dialogEnabled) {
            additionalInfoButton.setBackground(null);
            additionalInfoButton.setVisibility(GONE);
            return;
        }
        additionalInfoButton.setBackgroundResource(R.drawable.accordion_heading_button_background);
        FragmentManager manager = ((FragmentActivity) getContext()).getSupportFragmentManager();
        dialog = new AlertDialogBuilder(manager, DIALOG_TAG)
                .withDialogType(BaseDialogFragment.DialogTypes.BOTTOM_SHEET)
                .withTitle(dialogTitle)
                .withMessage(dialogMessage)
                .withCancelOnClickOutsude(true)
                .withPositiveButton(new BaseDialogFragment.DialogButtonConfiguration(dialogButtonText, true, true), null)
                .buildDialog();
        Drawable additionalInfoIcon = ResourceUtils.getDrawable(getContext(), R.drawable.icon_info);
        additionalInfoIcon.setTint(additionalInfoDrawableColor);
        additionalInfoButton.setImageDrawable(additionalInfoIcon);
        additionalInfoButton.setOnClickListener(view -> dialog.show());
    }

    private void setupHeader() {
        accordionHeaderContainer.setBackground(backgroundDrawable != null ? backgroundDrawable : DrawableUtils.getRoundedCornersDrawable(backgroundColor, backgroundCornerRadius));
        if (expandOnHeadingClick && expandable) {
            accordionHeader.setClickable(true);
            accordionHeader.setFocusable(true);
            accordionHeader.setOnClickListener(view -> {
                if (expanded) collapse(true);
                else expand(true);
            });
        } else {
            accordionHeader.setOnClickListener(null);
        }
    }

    private void setupInitialExpandState() {
        if (expanded)
            if (contentContainer.getVisibility() == GONE)
                contentContainer.setVisibility(VISIBLE);
        if (!expanded)
            if (contentContainer.getVisibility() == VISIBLE)
                contentContainer.setVisibility(GONE);
    }

    private void initializeExpandButton() {
        if (expanded) {
            if (expandable) expandButton.setImageDrawable(collapseIcon);
        } else {
            if (expandable) expandButton.setImageDrawable(expandIcon);
        }
        if (expandable) {
            expandButton.setBackgroundResource(R.drawable.accordion_heading_button_background);
            expandButton.setVisibility(VISIBLE);
            expandButton.setOnClickListener(view -> {
                if (expanded) collapse(true);
                else expand(true);
            });
        } else {
            expandButton.setBackground(null);
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

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

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
        myState.headingTextColor = this.headingTextColor;
        myState.headingTextAppearance = this.headingTextAppearance;
        myState.animationDuration = this.animationDuration;
        myState.visibility = this.getVisibility();
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
        this.headingTextColor = savedState.headingTextColor;
        this.headingTextAppearance = savedState.headingTextAppearance;
        this.animationDuration = savedState.animationDuration;
        this.setVisibility(savedState.visibility);
        initView();
    }

    private static class SavedState extends BaseSavedState {
        private String title;
        private String subtitle;
        private String buttonText;
        private boolean expanded;
        private int cornerRadius;
        private int headingTextSize;
        private int headingTextColor;
        private int headingTextAppearance;
        private int backgroundColor;
        private int animationDuration;
        private int visibility;

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
            headingTextColor = in.readInt();
            headingTextAppearance = in.readInt();
            backgroundColor = in.readInt();
            animationDuration = in.readInt();
            visibility = in.readInt();
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
            out.writeInt(headingTextColor);
            out.writeInt(headingTextAppearance);
            out.writeInt(backgroundColor);
            out.writeInt(animationDuration);
            out.writeInt(visibility);
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
        public void execute(AccordionView view, boolean expanded);
    }

    public enum ContentPositionType {
        INSIDE_HEADER(1),
        BELOW_HEADER(2);

        private final int value;
        private static final SparseArray<ContentPositionType> map = new SparseArray<>();

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

    public enum AccordionAnimationType {
        ANIM_NONE(1),
        ANIM_HEIGHT_TRANSITION(2);

        private final int value;
        private static final SparseArray<AccordionAnimationType> map = new SparseArray<>();

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