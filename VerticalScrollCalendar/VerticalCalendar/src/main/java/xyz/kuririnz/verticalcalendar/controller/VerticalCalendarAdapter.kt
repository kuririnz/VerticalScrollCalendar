package xyz.kuririnz.verticalcalendar.controller

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import xyz.kuririnz.verticalcalendar.R
import xyz.kuririnz.verticalcalendar.VerticalCalendar.Companion.SelectStatus
import xyz.kuririnz.verticalcalendar.view.SqureTextView
import java.util.*

/**
 * Recycler Adapter by Vertical Calender
 */
class VerticalCalendarAdapter(private val c: Context
                              , private val numOfMonth: Int
                              , itemClickListener: ((index: Int, dt: Date) -> Unit)) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class CellType { Header, Weekday, Date, }

    private val onClickItem: ((index: Int, dt: Date) -> Unit)? = itemClickListener
    private lateinit var dayOfMonthList: MutableList<Int>
    private lateinit var dayOfWeekList: MutableList<Int>
    private lateinit var nextDayOfWeekList: MutableList<Int>
    private var selectState = SelectStatus.None
    private var selectRange = Array<Date?>(2) { null }
    private var calendar = GregorianCalendar()

    /**
     *
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        var currentDateIndex = 0
        var currentMonthIndex = 0
        for (count in 0 until numOfMonth) {
            val sumMonthDateCount = dayOfMonthList.filterIndexed { index, _ -> index < count + 1 }.sum()
            val sumPrevDayOfWeek = dayOfWeekList.filterIndexed { index, _ -> index < count + 1 }.sum()
            val sumNextDayOfWeek = nextDayOfWeekList.filterIndexed { index, _ -> index < count + 1 }.sum()

            if (sumMonthDateCount + sumPrevDayOfWeek + sumNextDayOfWeek > position) {
                currentMonthIndex = count
                currentDateIndex = position - dayOfMonthList.filterIndexed { index, _ -> index < count }.sum() - dayOfWeekList.filterIndexed { index, _ -> index < count }.sum() - nextDayOfWeekList.filterIndexed { index, _ -> index < count }.sum()
                break
            }
        }

        // process end in the case of week cell
        if (currentDateIndex == 1) {
            return
        }

        val tmpCalendar = calendar.clone() as GregorianCalendar
        tmpCalendar.add(Calendar.MONTH, currentMonthIndex)
        if (currentDateIndex > 1) {
            val cellHolder = holder as CalendarCellViewHolder
            val adjustDate = currentDateIndex - 2 - dayOfWeekList[currentMonthIndex]
            // target month is true
            cellHolder.dayText.isEnabled = adjustDate >= 0 && (tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 1) >= adjustDate
            // set text params color / string
            tmpCalendar.add(Calendar.DAY_OF_MONTH, adjustDate)
            cellHolder.dayText.text = tmpCalendar.get(Calendar.DAY_OF_MONTH).toString()
            cellHolder.targetDate = tmpCalendar.time
            val colorList = when (tmpCalendar.get(Calendar.DAY_OF_WEEK)) {
                1 -> ContextCompat.getColorStateList(c, R.color.vc_holidaycell_bg_selector)
                7 -> ContextCompat.getColorStateList(c, R.color.vc_saturdaycell_bg_selector)
                else -> ContextCompat.getColorStateList(c, R.color.vc_commoncell_text_selector)
            }

            for (dt in selectRange) {
                if (dt != null && dt.time == tmpCalendar.timeInMillis) {
                    cellHolder.dayText.isActivated = true
                    break
                } else if (dt == selectRange[1]) {
                    cellHolder.dayText.isActivated = false
                }
            }

            cellHolder.toLeftRange.setBackgroundColor(ContextCompat.getColor(c, android.R.color.transparent))
            cellHolder.toRightRange.setBackgroundColor(ContextCompat.getColor(c, android.R.color.transparent))
            if (selectState == SelectStatus.Second) {
                val curMillSec = tmpCalendar.timeInMillis
                when {
                    selectRange[0]!!.time < curMillSec && selectRange[1]!!.time > curMillSec -> {
                        cellHolder.toLeftRange.setBackgroundColor(ContextCompat.getColor(c, android.R.color.holo_orange_light))
                        cellHolder.toRightRange.setBackgroundColor(ContextCompat.getColor(c, android.R.color.holo_orange_light))
                    }
                    selectRange[0]!!.time == curMillSec -> {
                        cellHolder.toRightRange.setBackgroundColor(ContextCompat.getColor(c, android.R.color.holo_orange_light))
                    }
                    selectRange[1]!!.time == curMillSec -> {
                        cellHolder.toLeftRange.setBackgroundColor(ContextCompat.getColor(c, android.R.color.holo_orange_light))

                    }
                }
            }

            cellHolder.dayText.setTextColor(colorList)
            // set cell tapped event
            cellHolder.dayText.setOnClickListener(ClickDateOnClickListener(position, tmpCalendar.time))
        } else {
            // Month Text Process
            tmpCalendar.add(Calendar.DAY_OF_MONTH, currentDateIndex)
            val cellHolder = holder as CalendarHeaderViewHolder
            cellHolder.header.text = DateFormat.format("yyyy年MM月", tmpCalendar)
        }
    }

    override fun getItemCount(): Int {
        calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nextMonthCalendar: GregorianCalendar = calendar.clone() as GregorianCalendar
        nextMonthCalendar.add(Calendar.MONTH, 1)

        var nextDayOfWeek = 8 - nextMonthCalendar.get(Calendar.DAY_OF_WEEK)
        var dayCount: Int = (calendar.getActualMaximum(Calendar.DATE) + 2) + (calendar.get(Calendar.DAY_OF_WEEK) - 1) + (if (nextDayOfWeek == 7) 0 else nextDayOfWeek)
        dayOfMonthList = mutableListOf(calendar.getActualMaximum(Calendar.DATE) + 2)
        dayOfWeekList = mutableListOf(calendar.get(Calendar.DAY_OF_WEEK) - 1)
        nextDayOfWeekList = mutableListOf(if (nextDayOfWeek == 7) 0 else nextDayOfWeek)
        for (i in 1 until numOfMonth) {
            val tmpCalendar: GregorianCalendar = calendar.clone() as GregorianCalendar
            val tmpNextCalendar: GregorianCalendar = nextMonthCalendar.clone() as GregorianCalendar
            tmpCalendar.add(Calendar.MONTH, i)
            tmpNextCalendar.add(Calendar.MONTH, i)
            val lastDate = tmpCalendar.getActualMaximum(Calendar.DATE) + 2
            val firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - 1
            nextDayOfWeek = 8 - tmpNextCalendar.get(Calendar.DAY_OF_WEEK)
            dayOfMonthList.add(lastDate)
            dayOfWeekList.add(firstDay)
            nextDayOfWeekList.add(if (nextDayOfWeek == 7) 0 else nextDayOfWeek)
            dayCount += lastDate + firstDay + if (nextDayOfWeek == 7) 0 else nextDayOfWeek
        }
        return dayCount
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(c)
        return when (CellType.values()[viewType]) {
            CellType.Weekday -> WeekdayViewHolder(inflater.inflate(R.layout.recycler_item_weekday, parent, false))
            CellType.Header -> CalendarHeaderViewHolder(inflater.inflate(R.layout.recycler_item_header, parent, false))
            CellType.Date -> CalendarCellViewHolder(inflater.inflate(R.layout.recycler_item_cell, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        var currentDateIndex = 0
        for (count in 0 until numOfMonth) {
            val sumMonthDateCount = dayOfMonthList.filterIndexed { index, _ -> index < count + 1 }.sum()
            val sumPrevDayOfWeek = dayOfWeekList.filterIndexed { index, _ -> index < count + 1 }.sum()
            val sumNextDayOfWeek = nextDayOfWeekList.filterIndexed { index, _ -> index < count + 1 }.sum()

            if (sumMonthDateCount + sumPrevDayOfWeek + sumNextDayOfWeek > position) {
                currentDateIndex = position - dayOfMonthList.filterIndexed { index, _ -> index < count }.sum()
                -dayOfWeekList.filterIndexed { index, _ -> index < count }.sum()
                -nextDayOfWeekList.filterIndexed { index, _ -> index < count }.sum()
                break
            }
        }

        return CellType.values()[currentDateIndex].ordinal
    }

    // OnDateCell Click Item Listener
    inner class ClickDateOnClickListener(position: Int, currentDate: Date) : View.OnClickListener {
        private val index = position
        private val dt = currentDate
        override fun onClick(v: View?) {
            v?.let { it.isActivated = !it.isActivated } ?: return

            when (selectState) {
                SelectStatus.None -> {
                    selectState = SelectStatus.First
                    selectRange[0] = dt
                }
                SelectStatus.First -> {
                    selectState = SelectStatus.Second
                    selectRange[1] = dt
                    selectRange.sort()
                    notifyDataSetChanged()
                }
                SelectStatus.Second -> {
                    selectState = SelectStatus.None
                    selectRange = Array(2) { null }
                    notifyDataSetChanged()
                }
            }

            // callback method call
            onClickItem?.invoke(index, dt)
        }
    }

    /**
     * common date recycler cell holder
     */
    class CalendarCellViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val dayText: SqureTextView = v.findViewById(R.id.vc_item_date_text)
        val toLeftRange: View = v.findViewById(R.id.vc_item_left_image)
        val toRightRange: View = v.findViewById(R.id.vc_item_right_image)
        var targetDate = Date()
    }

    /**
     * Month Header recycler cell holder
     */
    class CalendarHeaderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val header: TextView = v.findViewById(R.id.CalendarMonthText)
    }

    /**
     * Week Row recycler cell holder
     */
    class WeekdayViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val sunday: TextView = v.findViewById(R.id.CalendarSunday)
        val monday: TextView = v.findViewById(R.id.CalendarMonday)
        val tuesday: TextView = v.findViewById(R.id.CalendarTuesday)
        val wednesday: TextView = v.findViewById(R.id.CalendarWednesday)
        val thursday: TextView = v.findViewById(R.id.CalendarThursday)
        val friday: TextView = v.findViewById(R.id.CalendarFriday)
        val saturday: TextView = v.findViewById(R.id.CalendarSaturday)
    }
}