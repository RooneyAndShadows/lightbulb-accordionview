package com.github.rooneyandshadows.lightbulb.accordionview

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat

class AccordionGroupView(context: Context, attrs: AttributeSet?) : LinearLayoutCompat(context, attrs) {
    private var checkedId = -1
    private var protectFromCheckedChange = false
    private val onCheckedChangeListener: OnCheckedChangeListener? = null
    private val children: ArrayList<AccordionView>
        get() {
            val childCount = childCount
            val children = ArrayList<AccordionView>()
            for (i in 0 until childCount) children.add(getChildAt(i) as AccordionView)
            return children
        }

    init {
        isSaveEnabled = true
        orientation = VERTICAL
        readAttributes(context, attrs)
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
    override fun addView(child: View, index: Int) {
        if (child !is AccordionView) {
            Log.w(
                AccordionView::class.java.name,
                "Child view is ignored. Reason: Child views must be " + AccordionView::class.java.name
            )
            return
        }
        setupInternalCallbacks(child)
        super.addView(child, index)
    }

    @Override
    override fun addView(child: View, width: Int, height: Int) {
        if (child !is AccordionView) {
            Log.w(
                AccordionView::class.java.name,
                "Child view is ignored. Reason: Child views must be " + AccordionView::class.java.name
            )
            return
        }
        setupInternalCallbacks(child)
        super.addView(child, width, height)
    }

    @Override
    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        if (child !is AccordionView) {
            Log.w(
                AccordionView::class.java.name,
                "Child view is ignored. Reason: Child views must be " + AccordionView::class.java.name
            )
            return
        }
        setupInternalCallbacks(child)
        super.addView(child, params)
    }

    @Override
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child !is AccordionView) {
            Log.w(
                AccordionView::class.java.name,
                "Child view is ignored. Reason: Child views must be " + AccordionView::class.java.name
            )
            return
        }
        setupInternalCallbacks(child)
        super.addView(child, index, params)
    }

    @Override
    override fun onFinishInflate() {
        super.onFinishInflate()
        setCheckedIdInternally(
            newCheckedId = checkedId,
            animate = false,
            notifyChange = false
        )
    }

    @Override
    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val myState = SavedState(superState)
        myState.checkedId = checkedId
        return myState
    }

    @Override
    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        checkedId = savedState.checkedId
        val v = findViewById<AccordionView>(checkedId)
        if (v != null) setCheckedIdInternally(
            newCheckedId = checkedId,
            animate = false,
            notifyChange = false
        )
    }

    fun setCheckedId(newCheckId: Int) {
        setCheckedId(newCheckId, true)
    }

    fun setCheckedId(newCheckId: Int, animate: Boolean) {
        if (checkedId == newCheckId) return
        if (protectFromCheckedChange) return
        protectFromCheckedChange = true
        closeAllExceptFor(checkedId, animate)
        protectFromCheckedChange = false
        setCheckedIdInternally(newCheckId, animate, true)
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.AccordionGroupView, 0, 0)
        checkedId = try {
            a.getResourceId(
                R.styleable.AccordionGroupView_AVG_SelectedId,
                -1
            )
        } finally {
            a.recycle()
        }
    }

    private fun closeAllExceptFor(id: Int, animate: Boolean) {
        children.stream()
            .filter { checkableView: AccordionView ->
                if (!checkableView.isExpanded) return@filter false
                if (checkableView.id == -1) return@filter false
                checkableView.id != id
            }
            .forEach { checkableView: AccordionView -> checkableView.collapse(animate) }
    }

    private fun setupInternalCallbacks(targetView: AccordionView) {
        targetView.setOnGroupCheckedListener { accordion, expanded ->
            if (protectFromCheckedChange) return@setOnGroupCheckedListener
            protectFromCheckedChange = true
            var idToCheck = -1
            if (expanded) {
                idToCheck = accordion.id
                closeAllExceptFor(checkedId, true)
            }
            protectFromCheckedChange = false
            if (checkedId == idToCheck) return@setOnGroupCheckedListener
            setCheckedIdInternally(
                newCheckedId = idToCheck,
                animate = true,
                notifyChange = true
            )
        }
    }

    private fun setCheckedIdInternally(newCheckedId: Int, animate: Boolean, notifyChange: Boolean) {
        val previouslyCheckedId = checkedId
        val previousCheckedView = findViewById<AccordionView>(previouslyCheckedId)
        checkedId = newCheckedId
        if (newCheckedId == -1 && previousCheckedView != null) {
            protectFromCheckedChange = true
            if (previousCheckedView.isExpanded) previousCheckedView.collapse(animate)
            if (notifyChange && onCheckedChangeListener != null) onCheckedChangeListener.execute(
                checkedId,
                previousCheckedView
            )
            protectFromCheckedChange = false
        } else {
            val viewToCheck = findViewById<AccordionView>(checkedId)
            if (viewToCheck != null && !viewToCheck.isExpanded) viewToCheck.expand(animate)
            if (notifyChange && onCheckedChangeListener != null) onCheckedChangeListener.execute(checkedId, viewToCheck)
        }
    }

    interface OnCheckedChangeListener {
        fun execute(checkedId: Int, view: AccordionView?)
    }

    private class SavedState : BaseSavedState {
        var checkedId = 0

        internal constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            checkedId = `in`.readInt()
        }

        @Override
        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(checkedId)
        }

        @Override
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
}