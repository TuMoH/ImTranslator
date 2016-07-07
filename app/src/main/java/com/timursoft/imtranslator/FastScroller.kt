package com.timursoft.imtranslator

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller

/**
 * Created by TuMoH on 28.06.2016.
 */
class FastScroller : VerticalRecyclerViewFastScroller {

    var listener: ((Int) -> Unit)? = null
    private var recyclerView: RecyclerView? = null

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    override fun setRecyclerView(recyclerView: RecyclerView) {
        super.setRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun scrollTo(scrollProgress: Float, fromTouch: Boolean) {
        val position = getPositionFromScrollProgress(scrollProgress)
        (recyclerView!!.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
        listener?.invoke(position)
    }

    private fun getPositionFromScrollProgress(scrollProgress: Float): Int {
        return (recyclerView!!.adapter.itemCount * scrollProgress).toInt()
    }

}
