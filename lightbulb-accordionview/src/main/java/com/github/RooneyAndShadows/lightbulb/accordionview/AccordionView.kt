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
    private val stateExpanded = intArrayOf(R.attr.av_state_expanded)
    private val stateCollapsed = intArrayOf(-R.attr.av_state_expanded)
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
    private var headingTextSize = 0
    private var expandDrawableColor: Int = 0
        set(value) {
            field = value
            expandIcon?.setTint(field)
        }
    private var headingTextColor = 0
    private var headingTextAppearance = -1
    private var expandIcon: Drawable? = null
        set(value) {
            field = value
            field?.apply {
                setTint(expandDrawableColor)
            }
        }
    private var expandable = true
    private var expandOnHeadingClick = false
    private var headingText: String? = null
    private var expandListeners: OnExpandedChangeListener? = null
    private var onGroupCheckedListener: OnExpandedChangeListener? = null
    private var anim: AccordionAnimation? = null
    private var animationType: AccordionAnimationType = ANIM_NONE
    private var inflated = false
    var isExpanded = false
        private set(value) {
            if (field == value) return
            field = value
            refreshDrawableState()
        }

    init {
        isSaveEnabled = true
        orientation = VERTICAL
        readAttributes(context, attrs)
        inflate(getContext(), R.layout.view_accordion_layout, this)
        setupClips()
        initializeHeader()
        initAnimation()
        inflated = true
    }

    @Override
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (!inflated) super.addView(child, index, params)
        else contentContainer.addView(child, index, params)
    }

    @Override
    override fun setClipChildren(clipChildren: Boolean) {
        super.setClipChildren(clipChildren)
        if (!inflated) return
        setupClips()
    }

    @Override
    override fun setClipToPadding(clipToPadding: Boolean) {
        super.setClipToPadding(clipToPadding)
        if (!inflated) return
        setupClips()
    }

    @Override
    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isExpanded) {
            mergeDrawableStates(drawableState, stateExpanded)
        }
        return drawableState
    }

    fun setOnGroupCheckedListener(listener: OnExpandedChangeListener?) {
        onGroupCheckedListener = listener
    }

    fun setExpandListeners(expandListeners: OnExpandedChangeListener?) {
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

    fun setExpandOnHeadingClick(expandOnHeadingClick: Boolean) {
        this.expandOnHeadingClick = expandOnHeadingClick
        syncExpandButton()
    }

    fun setHeadingText(headingText: String?) {
        this.headingText = headingText
        headingTextView.text = headingText
    }

    fun setExpandable(expandable: Boolean) {
        this.expandable = expandable
        syncExpandButton()
    }

    fun expand(animated: Boolean) {
        if (expandable && isExpanded || animated && anim!!.hasRunningAnimation()) return
        if (animated) anim!!.expand(animationDuration) else contentContainer.visibility = VISIBLE
        isExpanded = true
        expandButton.animate().setDuration(animationDuration.toLong()).rotation(180f).start()
        if (expandListeners != null) expandListeners!!.execute(this, true)
        if (onGroupCheckedListener != null) onGroupCheckedListener!!.execute(this, true)
    }

    fun collapse(animated: Boolean) {
        if (expandable && !isExpanded || animated && anim!!.hasRunningAnimation()) return
        if (animated) anim!!.collapse(animationDuration) else contentContainer.visibility = GONE
        isExpanded = false
        expandButton.animate().setDuration(animationDuration.toLong()).rotation(0f).start()
        if (expandListeners != null) onGroupCheckedListener!!.execute(this, false)
        if (onGroupCheckedListener != null) onGroupCheckedListener!!.execute(this, false)
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
                headingText = getString(R.styleable.AccordionView_av_heading_text)
                if (StringUtils.isNullOrEmptyString(headingText)) headingText =
                    ResourceUtils.getPhrase(context, R.string.av_heading_default_text)
                animationType = AccordionAnimationType.valueOf(getInt(R.styleable.AccordionView_av_animation, 1))
                expandDrawableColor = getColor(
                    R.styleable.AccordionView_av_expand_icon_color,
                    ResourceUtils.getColorByAttribute(getContext(), R.attr.colorOnSurface)
                )
                headingTextColor = getColor(R.styleable.AccordionView_av_heading_text_color, -1)
                headingTextSize = getDimensionPixelSize(R.styleable.AccordionView_av_heading_text_Size, -1)
                headingTextAppearance = getResourceId(
                    R.styleable.AccordionView_av_heading_text_appearance,
                    R.style.AccordionHeadingTextAppearance
                )
                animationDuration = getInteger(R.styleable.AccordionView_av_animation_duration, animationDuration)
                isExpanded = getBoolean(R.styleable.AccordionView_av_expanded, isExpanded)
                expandable = getBoolean(R.styleable.AccordionView_av_expandable, expandable)
                expandOnHeadingClick = getBoolean(R.styleable.AccordionView_av_expand_on_heading_click, expandOnHeadingClick)
            } finally {
                recycle()
            }
        }
    }

    private fun syncExpandButton() {
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

    private fun initializeHeader() {
        headingTextView.text = headingText
        headingTextView.setTextAppearance(headingTextAppearance)
        if (headingTextColor != -1) headingTextView.setTextColor(headingTextColor)
        if (headingTextSize != -1) headingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, headingTextSize.toFloat())
        setupInitialExpandState()
        initializeExpandButton()
        syncExpandButton()
    }

    private fun setupInitialExpandState() {
        if (isExpanded) {
            expandButton.rotation = 180F
            if (contentContainer.visibility == GONE) contentContainer.visibility = VISIBLE

        }
        if (!isExpanded) {
            expandButton.rotation = 0F
            if (contentContainer.visibility == VISIBLE) contentContainer.visibility = GONE
        }
    }

    private fun initializeExpandButton() {
        expandIcon = ResourceUtils.getDrawable(context, R.drawable.av_icon_expand)
        expandIcon!!.setTint(expandDrawableColor)
        expandButton.apply {
            if (expandable) {
                expandButton.setImageDrawable(expandIcon)
                expandButton.setBackgroundResource(R.drawable.av_heading_button_background)
                expandButton.setOnClickListener { if (isExpanded) collapse(true) else expand(true) }
                expandButton.visibility = VISIBLE
                return@apply
            }
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
        headingTextSize = savedState.headingTextSize
        headingTextColor = savedState.headingTextColor
        headingTextAppearance = savedState.headingTextAppearance
        animationDuration = savedState.animationDuration
        this.visibility = savedState.visibility
    }

    fun interface OnExpandedChangeListener {
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