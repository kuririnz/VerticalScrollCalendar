package xyz.kuririnz.verticalcalendar.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by keisuke_kuribayashi on 2017/11/21.
 */
class SqureTextView(context: Context, atts: AttributeSet?): TextView(context, atts) {

    constructor(context: Context) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}