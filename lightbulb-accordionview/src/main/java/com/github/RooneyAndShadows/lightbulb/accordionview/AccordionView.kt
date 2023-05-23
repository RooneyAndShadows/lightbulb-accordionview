package com.github.rooneyandshadows.lightbulb.accordionview

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.BindingAdapter
import com.github.rooneyandshadows.java.commons.string.StringUtils
import com.github.rooneyandshadows.lightbulb.accordionview.AccordionView.AccordionAnimationType.*
import com.github.rooneyandshadows.lightbulb.accordionview.animation.AccordionAnimation
import com.github.rooneyandshadows.lightbulb.accordionview.animation.AccordionShowHideAnimation
import com.github.rooneyandshadows.lightbulb.accordionview.animation.AccordionTransitionAnimation
import com.github.rooneyandshadows.lightbulb.commons.utils.DrawableUtils
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

class AccordionView(context: Context, attrs: AttributeSet?) : LinearLayoutCompat(context, attrs) {
    private val accordionHeader: LinearLayoutCompat by lazy {
        return@lazy findViewById(R.id.accordion_header)!!
    }
    private val contentContainer: RelativeLayout by lazy {
        return@lazy findViewById(R.id.accordion_content)!!
    }
    private val headingTextView: TextView by lazy {
        return@lazy findViewById(R.id.accordion_heading_text)!!
    }
    private val expandButton: AppCompatImageButton by lazy {
        return@lazy findViewById(R.id.accordion_expand_button)
    }
    private var animationDuration = 250
    private var backgroundCornerRadius = 0
    private var headingTextSize = 0
    private var expandDrawableColor = 0
    private var backgroundColor = 0
    private var headingTextColor = 0
    private var headingTextAppearance = -1
    private var expandIcon: Drawable? = null
    private var collapseIcon: Drawable? = null
    private var backgroundDrawable: Drawable? = null
    var isExpanded = false
        private set
    private var expandable = true
    private var expandOnHeadingClick = false
    private var headingText: String? = null
    private var expandListeners: ExpansionListeners? = null
    private var onGroupCheckedListener: ExpansionListeners? = null
    private var anim: AccordionAnimation? = null
    private var animationType: AccordionAnimationType = ANIM_NONE

    init {
        isSaveEnabled = true
        readAttributes(context, attrs)
        inflate(getContext(), R.layout.view_accordion_layout, this)
        orientation = VERTICAL
        setupClips()
        initializeHeader()
        initAnimation()
    }

    @Override
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        contentContainer.addView(child, index, params)
    }

    @Override
    override fun setClipChildren(clipChildren: Boolean) {
        super.setClipChildren(clipChildren)
        setupClips()
    }

    @Override
    override fun setClipToPadding(clipToPadding: Boolean) {
        super.setClipToPadding(clipToPadding)
        setupClips()
    }

    @Override
    override fun setBackground(backgroundDrawable: Drawable) {
        this.backgroundDrawable = backgroundDrawable
        setupHeader()
    }

    fun setOnGroupCheckedListener(listener: ExpansionListeners?) {
        onGroupCheckedListener = listener
    }

    fun setExpandListeners(expandListeners: ExpansionListeners?) {
        this.expandListeners = expandListeners
    }

    fun setHeadingTextColor(headingTextColor: Int) {
        this.headingTextColor = headingTextColor
        headingTextView.setTextColor(headingTextColor)
    }

    fun setHeadingTextSize(headingTextSize: Int) {
        this.headingTextSize = headingTextSize
        headingTextView.textSize = headingTextSize.toFloat()
    }

    fun setHeadingTextAppearance(headingTextAppearance: Int) {
        this.headingTextAppearance = headingTextAppearance
        headingTextView.setTextAppearance(headingTextAppearance)
    }

    fun setAnimationDuration(animationDuration: Int) {
        this.animationDuration = animationDuration
    }

    fun setBackgroundCornerRadius(backgroundCornerRadius: Int) {
        this.backgroundCornerRadius = backgroundCornerRadius
        setupHeader()
    }

    fun setBackground(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        setupHeader()
    }

    fun setExpandOnHeadingClick(expandOnHeadingClick: Boolean) {
        this.expandOnHeadingClick = expandOnHeadingClick
        setupHeader()
    }

    fun setHeadingText(headingText: String?) {
        this.headingText = headingText
        headingTextView.text = headingText
    }

    fun setExpandable(expandable: Boolean) {
        this.expandable = expandable
        initializeExpandButton()
    }

    fun expand(animated: Boolean) {
        val view = this
        if (expandable && isExpanded || animated && anim!!.hasRunningAnimation()) return
        if (animated) anim!!.expand(animationDuration) else contentContainer.visibility = VISIBLE
        isExpanded = true
        expandButton.setImageDrawable(collapseIcon)
        if (expandListeners != null) expandListeners!!.execute(view, true)
        if (onGroupCheckedListener != null) onGroupCheckedListener!!.execute(view, true)
    }

    fun collapse(animated: Boolean) {
        val view = this
        if (expandable && !isExpanded || animated && anim!!.hasRunningAnimation()) return
        if (animated) anim!!.collapse(animationDuration) else contentContainer.visibility = GONE
        isExpanded = false
        expandButton.setImageDrawable(expandIcon)
        if (expandListeners != null) onGroupCheckedListener!!.execute(view, false)
        if (onGroupCheckedListener != null) onGroupCheckedListener!!.execute(view, false)
    }

    private fun setupClips() {
        contentContainer.clipToPadding = clipToPadding
        accordionHeader.clipToPadding = clipToPadding
        contentContainer.clipChildren = clipChildren
        accordionHeader.clipChildren = clipChildren
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?) {
        val attr = context.theme.obtainStyledAttributes(attrs, R.styleable.AccordionView, 0, 0)
        attr.apply {
            try {
                headingText = getString(R.styleable.AccordionView_AV_HeadingText)
                if (StringUtils.isNullOrEmptyString(headingText)) headingText =
                    ResourceUtils.getPhrase(context, R.string.av_heading_default_text)
                animationType = AccordionAnimationType.valueOf(getInt(R.styleable.AccordionView_AV_AnimationType, 1))
                expandDrawableColor = getColor(
                    R.styleable.AccordionView_AV_ExpandIconColor,
                    ResourceUtils.getColorByAttribute(getContext(), R.attr.colorOnSurface)
                )
                headingTextColor = getColor(R.styleable.AccordionView_AV_HeadingTextColor, -1)
                backgroundColor = getColor(
                    R.styleable.AccordionView_AV_BackgroundColor,
                    ResourceUtils.getColorByAttribute(getContext(), R.attr.colorSurface)
                )
                headingTextSize = getDimensionPixelSize(R.styleable.AccordionView_AV_HeadingTextSize, -1)
                headingTextAppearance = getResourceId(
                    R.styleable.AccordionView_AV_HeadingTextAppearance,
                    R.style.AccordionHeadingTextAppearance
                )
                backgroundDrawable = getDrawable(R.styleable.AccordionView_AV_BackgroundDrawable)
                animationDuration = getInteger(R.styleable.AccordionView_AV_AnimationDuration, animationDuration)
                backgroundCornerRadius = getInteger(
                    R.styleable.AccordionView_AV_BackgroundCornerRadius,
                    backgroundCornerRadius
                )
                isExpanded = getBoolean(R.styleable.AccordionView_AV_Expanded, isExpanded)
                expandable = getBoolean(R.styleable.AccordionView_AV_Expandable, expandable)
                expandOnHeadingClick = getBoolean(R.styleable.AccordionView_AV_ExpandOnHeadingClick, expandOnHeadingClick)
            } finally {
                recycle()
            }
        }
    }

    private fun initializeHeader() {
        headingTextView.text = headingText
        headingTextView.setTextAppearance(headingTextAppearance)
        if (headingTextColor != -1) headingTextView.setTextColor(headingTextColor)
        if (headingTextSize != -1) headingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, headingTextSize.toFloat())
        expandIcon = ResourceUtils.getDrawable(context, R.drawable.accordion_icon_expand)
        collapseIcon = ResourceUtils.getDrawable(context, R.drawable.accordion_icon_collapse)
        expandIcon!!.setTint(expandDrawableColor)
        collapseIcon!!.setTint(expandDrawableColor)
        setupHeader()
        setupInitialExpandState()
        if (isInEditMode) return
        initializeExpandButton()
    }

    private fun setupHeader() {
        background = if (backgroundDrawable != null) backgroundDrawable else DrawableUtils.getRoundedCornersDrawable(
                backgroundColor,
                backgroundCornerRadius
            )
        if (expandOnHeadingClick && expandable) {
            accordionHeader.isClickable = true
            accordionHeader.isFocusable = true
            accordionHeader.setOnClickListener {
                if (isExpanded) collapse(true)
                else expand(true)
            }
        } else {
            accordionHeader.setOnClickListener(null)
        }
    }

    private fun setupInitialExpandState() {
        if (isExpanded) if (contentContainer.visibility == GONE) contentContainer.visibility = VISIBLE
        if (!isExpanded) if (contentContainer.visibility == VISIBLE) contentContainer.visibility = GONE
    }

    private fun initializeExpandButton() {
        if (isExpanded) {
            if (expandable) expandButton.setImageDrawable(collapseIcon)
        } else {
            if (expandable) expandButton.setImageDrawable(expandIcon)
        }
        if (expandable) {
            expandButton.setBackgroundResource(R.drawable.accordion_heading_button_background)
            expandButton.visibility = VISIBLE
            expandButton.setOnClickListener { view: View? -> if (isExpanded) collapse(true) else expand(true) }
        } else {
            expandButton.background = null
            expandButton.visibility = GONE
        }
    }

    private fun initAnimation() {
        anim = when (animationType) {
            ANIM_NONE -> AccordionShowHideAnimation(contentContainer)
            ANIM_HEIGHT_TRANSITION -> AccordionTransitionAnimation(contentContainer)
        }
    }

    //@Override
    //protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    //    dispatchFreezeSelfOnly(container);
    //}
    //@Override
    //protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    //    dispatchThawSelfOnly(container);
    //}
    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val myState = SavedState(superState)
        myState.expanded = isExpanded
        myState.backgroundColor = backgroundColor
        myState.cornerRadius = backgroundCornerRadius
        myState.headingTextSize = headingTextSize
        myState.headingTextColor = headingTextColor
        myState.headingTextAppearance = headingTextAppearance
        myState.animationDuration = animationDuration
        myState.visibility = this.visibility
        return myState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        isExpanded = savedState.expanded
        backgroundColor = savedState.backgroundColor
        backgroundCornerRadius = savedState.cornerRadius
        headingTextSize = savedState.headingTextSize
        headingTextColor = savedState.headingTextColor
        headingTextAppearance = savedState.headingTextAppearance
        animationDuration = savedState.animationDuration
        this.visibility = savedState.visibility
    }

    fun interface ExpansionListeners {
        fun execute(view: AccordionView, expanded: Boolean)
    }

    private class SavedState : BaseSavedState {
        var expanded = false
        var cornerRadius = 0
        var headingTextSize = 0
        var headingTextColor = 0
        var headingTextAppearance = 0
        var backgroundColor = 0
        var animationDuration = 0
        var visibility = 0

        internal constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            cornerRadius = `in`.readInt()
            headingTextSize = `in`.readInt()
            headingTextColor = `in`.readInt()
            headingTextAppearance = `in`.readInt()
            backgroundColor = `in`.readInt()
            animationDuration = `in`.readInt()
            visibility = `in`.readInt()
            expanded = `in`.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(cornerRadius)
            out.writeInt(headingTextSize)
            out.writeInt(headingTextColor)
            out.writeInt(headingTextAppearance)
            out.writeInt(backgroundColor)
            out.writeInt(animationDuration)
            out.writeInt(visibility)
            out.writeInt(if (expanded) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    enum class AccordionAnimationType(val value: Int) {
        ANIM_NONE(1),
        ANIM_HEIGHT_TRANSITION(2);

        companion object {
            fun valueOf(value: Int) = values().first { it.value == value }
        }
    }

    companion object {
        @BindingAdapter("accordionHeadingText")
        fun setHeadingText(view: AccordionView, text: String?) {
            view.setHeadingText(text)
        }
    }
}