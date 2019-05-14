package xyz.kuririnz.verticalcalendar

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import xyz.kuririnz.verticalcalendar.controller.CalendarSpansizeLookUp
import xyz.kuririnz.verticalcalendar.controller.VerticalCalendarAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Vertical Calendar View
 */
class VerticalCalendar(context: Context
                       , attributeSet: AttributeSet?
                       , defStyleAttr: Int = 0) : RecyclerView(context, attributeSet, defStyleAttr) {

    private val c = context
    private var selectItem: ((start: Date?, end: Date?) -> Unit)? = null
    private var startDate = Date()
    private var numOfMonth:Int = 0
    private var selectState = SelectStatus.None
    private var selectInfo = Array<Date?>(2) {null}
    private val calendarAdapter: VerticalCalendarAdapter by lazy {
        VerticalCalendarAdapter(c, numOfMonth) {index, dt ->
            println("process of Vertical Calendar cell Tapped num is $index")
            when(selectState) {
                SelectStatus.None -> { selectInfo[0] = dt }
                SelectStatus.First -> { selectInfo[1] = dt }
                SelectStatus.Second -> { selectInfo.drop(2) }
            }
            this.selectItem?.invoke(selectInfo[0], selectInfo[1])
        }
    }
    private val lm: GridLayoutManager by lazy { GridLayoutManager(context, 7) }

    // Constructor

    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, 0) {
        // load from xml resource
        attributeSet?.let {
            val attrs = context.theme.obtainStyledAttributes(it, R.styleable.VerticalCalendar, 0, 0)

            try {
                this.numOfMonth = attrs.getInt(R.styleable.VerticalCalendar_numOfMonth, 6)
                attrs.getString(R.styleable.VerticalCalendar_startDate).let {
                    startDateStr: String ->
                    val df = SimpleDateFormat("yyyy/MM")
                    this.startDate = df.parse(startDateStr)
                    println("process of Vertical Calendar cell Tapped num is $startDate")
                }
            } finally {
                attrs.recycle()
            }
        }

        // initialize to GridLayoutManager
        lm.spanSizeLookup = CalendarSpansizeLookUp(this.numOfMonth)

        // set require data
        this.layoutManager = lm
        this.adapter = calendarAdapter
    }

    constructor(context: Context): this(context, null)

    /**
     * set item select event
     */
    fun setSelectItemListener(selectItemListener: ((start: Date?, end: Date?) -> Unit)) {
        this.selectItem = selectItemListener
    }

    companion object {
        /**
         * selected status of enum class
         */
        enum class SelectStatus{
            None,
            First,
            Second,
        }
    }
}