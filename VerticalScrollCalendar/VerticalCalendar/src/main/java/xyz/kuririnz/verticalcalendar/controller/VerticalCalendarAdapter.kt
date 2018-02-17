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
import xyz.kuririnz.verticalcalendar.view.SqureTextView
import java.util.*

/**
 * Recycler Adapter by Vertical Calender
 */
class VerticalCalendarAdapter(c: Context
                              , numOfMonth: Int
                              , itemClickListener: ((index: Int, dt: Date) -> Unit)) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class cellType (val type: Int) {
        cellHeader(0),
        cellWeekday(1),
        cellDate(2),
    }

    /**
     * selected status of enum class
     */
    enum class SelectStatus{
        None,
        First,
        Second,
        Cancel,
    }

    private val context = c
    private val numOfMonth = numOfMonth
    private val onClickItem: ((index: Int, dt: Date) -> Unit) ? = itemClickListener
    private lateinit var dayOfMonthList : MutableList<Int>
    private lateinit var dayOfWeekList: MutableList<Int>
    private lateinit var nextDayOfWeekList: MutableList<Int>
    private var selectState = SelectStatus.None
    private var selectRange = Array<Date?>(2, {null})
    private var calendar = GregorianCalendar()

    /**
     *
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        var currentDateIndex = 0
        var currentMonthIndex = 0
        for (count in 0..(numOfMonth - 1)) {
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
        if (currentDateIndex == 1) { return }

        var tmpCalendar = calendar.clone() as GregorianCalendar
        tmpCalendar.add(Calendar.MONTH, currentMonthIndex)
        if (currentDateIndex > 1) {
            var cellHolder = holder as CalendarCellViewHolder
            val adjustDate = currentDateIndex - 2 - dayOfWeekList[currentMonthIndex]
            // target month is true
            cellHolder.dayText.isEnabled = adjustDate >= 0 && (tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 1) >= adjustDate
            // set text params color / string
            tmpCalendar.add(Calendar.DAY_OF_MONTH, adjustDate)
            cellHolder.dayText.text = tmpCalendar.get(Calendar.DAY_OF_MONTH).toString()
            cellHolder.targetDate = tmpCalendar.time
            val colorList = when (tmpCalendar.get(Calendar.DAY_OF_WEEK)) {
                1 -> { ContextCompat.getColorStateList(context, R.color.vc_holidaycell_bg_selector) }
                7 -> { ContextCompat.getColorStateList(context, R.color.vc_saturdaycell_bg_selector) }
                else -> { ContextCompat.getColorStateList(context, R.color.vc_commoncell_text_selector) }
            }

            for (dt in selectRange) {
                if (dt != null && dt.time == tmpCalendar.timeInMillis) {
                    cellHolder.dayText.isActivated = true
                    break
                } else if (dt == selectRange[1]) {
                    cellHolder.dayText.isActivated = false
                }
            }

            cellHolder.toLeftRange.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            cellHolder.toRightRange.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            if (selectState == SelectStatus.Second) {
                val curMillSec = tmpCalendar.timeInMillis
                when {
                    selectRange[0]!!.time < curMillSec && selectRange[1]!!.time > curMillSec -> {
                        cellHolder.toLeftRange.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                        cellHolder.toRightRange.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                    }
                    selectRange[0]!!.time == curMillSec -> {
                        cellHolder.toRightRange.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                    }
                    selectRange[1]!!.time == curMillSec -> {
                        cellHolder.toLeftRange.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))

                    }
                }
            }

            cellHolder.dayText.setTextColor(colorList)
            // set cell tapped event
            cellHolder.dayText.setOnClickListener(ClickDateOnClickListener(position, tmpCalendar.time))
        } else {
            // Month Text Process
            tmpCalendar.add(Calendar.DAY_OF_MONTH, currentDateIndex)
            var cellHolder = holder as CalendarHeaderViewHolder
            cellHolder.header.text = DateFormat.format("yyyy年MM月", tmpCalendar)
        }
    }

    override fun getItemCount(): Int {
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val nextMonthCalendar:GregorianCalendar = calendar.clone() as GregorianCalendar
        nextMonthCalendar.add(Calendar.MONTH, 1)

        val nextDayOfWeek = 8 - nextMonthCalendar.get(Calendar.DAY_OF_WEEK)
        var dayCount: Int = (calendar.getActualMaximum(Calendar.DATE) + 2) + (calendar.get(Calendar.DAY_OF_WEEK) - 1) + (if (nextDayOfWeek == 7) 0 else nextDayOfWeek)
        dayOfMonthList = mutableListOf(calendar.getActualMaximum(Calendar.DATE) + 2)
        dayOfWeekList = mutableListOf(calendar.get(Calendar.DAY_OF_WEEK) - 1)
        nextDayOfWeekList = mutableListOf(if (nextDayOfWeek == 7) 0 else nextDayOfWeek)
        for(i in 1..(numOfMonth - 1)) {
            val tmpCalendar:GregorianCalendar = calendar.clone() as GregorianCalendar
            val tmpNextCalendar:GregorianCalendar = nextMonthCalendar.clone() as GregorianCalendar
            tmpCalendar.add(Calendar.MONTH, i)
            tmpNextCalendar.add(Calendar.MONTH, i)
            val lastDate = tmpCalendar.getActualMaximum(Calendar.DATE) + 2
            val firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - 1
            val nextDayOfWeek = 8 - tmpNextCalendar.get(Calendar.DAY_OF_WEEK)
            dayOfMonthList.add(lastDate)
            dayOfWeekList.add(firstDay)
            nextDayOfWeekList.add(if (nextDayOfWeek == 7) 0 else nextDayOfWeek)
            dayCount += lastDate + firstDay + if (nextDayOfWeek == 7) 0 else nextDayOfWeek
        }
        return dayCount
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        if (viewType == cellType.cellWeekday.type)
            return WeekdayViewHolder(inflater.inflate(R.layout.recycler_item_weekday, parent, false))
        else if (viewType == cellType.cellHeader.type)
            return CalendarHeaderViewHolder(inflater.inflate(R.layout.recycler_item_header, parent, false))
        else
            return CalendarCellViewHolder(inflater.inflate(R.layout.recycler_item_cell, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        var currentDateIndex = 0
        for (count in 0..(numOfMonth - 1)) {
            val sumMonthDateCount = dayOfMonthList.filterIndexed { index, _ -> index < count + 1 }.sum()
            val sumPrevDayOfWeek = dayOfWeekList.filterIndexed { index, _ -> index < count + 1 }.sum()
            val sumNextDayOfWeek = nextDayOfWeekList.filterIndexed { index, _ -> index < count + 1 }.sum()

            if (sumMonthDateCount + sumPrevDayOfWeek + sumNextDayOfWeek > position) {
                currentDateIndex = position - dayOfMonthList.filterIndexed { index, _ -> index < count }.sum() - dayOfWeekList.filterIndexed { index, _ -> index < count }.sum() - nextDayOfWeekList.filterIndexed { index, _ -> index < count }.sum()
                break
            }
        }

        when(currentDateIndex) {
            0 -> { return cellType.cellHeader.type }
            1 -> { return cellType.cellWeekday.type }
            else -> { return cellType.cellDate.type }
        }
        return super.getItemViewType(position)
    }

    // OnDateCell Click Item Listener
    inner class ClickDateOnClickListener(position: Int, currentDate: Date) : View.OnClickListener {
        val index = position
        val dt = currentDate
        override fun onClick(v: View?) {
            if (!v!!.isEnabled) return
            v.isActivated = !v.isActivated

            when(selectState) {
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
                    selectRange = Array(2, {null})
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
    class CalendarCellViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val dayText: SqureTextView = v.findViewById(R.id.vc_item_date_text)
        val toLeftRange: View = v.findViewById(R.id.vc_item_left_image)
        val toRightRange: View = v.findViewById(R.id.vc_item_right_image)
        var targetDate = Date()
    }

    /**
     * Month Header recycler cell holder
     */
    class CalendarHeaderViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val header: TextView = v.findViewById(R.id.CalendarMonthText)
    }

    /**
     * Week Row recycler cell holder
     */
    class WeekdayViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val sunday: TextView = v.findViewById(R.id.CalendarSunday)
        val monday: TextView = v.findViewById(R.id.CalendarMonday)
        val tuesday: TextView = v.findViewById(R.id.CalendarTuesday)
        val wednesday: TextView = v.findViewById(R.id.CalendarWednesday)
        val thursday: TextView = v.findViewById(R.id.CalendarThursday)
        val friday: TextView = v.findViewById(R.id.CalendarFriday)
        val saturday: TextView = v.findViewById(R.id.CalendarSaturday)
    }
}