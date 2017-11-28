package xyz.kuririnz.verticalcalendar.controller

import android.support.v7.widget.GridLayoutManager
import java.util.*

/**
 * GridLayoutManager Option by Vertical Calendar
 */
class CalendarSpansizeLookUp(): GridLayoutManager.SpanSizeLookup() {

    var calendar: GregorianCalendar = GregorianCalendar()
    lateinit var dayOfMonthList : MutableList<Int>
    lateinit var dayOfWeekList: MutableList<Int>
    lateinit var nextDayOfWeekList: MutableList<Int>
    var displayMonth : Int = 0

    constructor(displayMonth: Int) : this() {
        this.displayMonth = displayMonth
        this.calendar.set(Calendar.DAY_OF_MONTH, 1)
        this.calendar.set(Calendar.HOUR, 0)
        this.calendar.set(Calendar.MINUTE, 0)
        this.calendar.set(Calendar.SECOND, 0)
        this.calendar.set(Calendar.MILLISECOND, 0)

        val nextMonthCalendar:GregorianCalendar = calendar.clone() as GregorianCalendar
        nextMonthCalendar.add(Calendar.MONTH, 1)
        val nextDayOfWeek = 8 - nextMonthCalendar.get(Calendar.DAY_OF_WEEK)

        dayOfMonthList = mutableListOf(calendar.getActualMaximum(Calendar.DATE) + 2)
        dayOfWeekList = mutableListOf(calendar.get(Calendar.DAY_OF_WEEK) - 1)
        nextDayOfWeekList = mutableListOf(if (nextDayOfWeek == 7) 0 else nextDayOfWeek)
        for(i in 1..(displayMonth - 1)) {
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
        }
    }

    override fun getSpanSize(position: Int): Int {
        var currentDateIndex = 0
        for (count in 0..(displayMonth - 1)) {
            val sumMonthDateCount = dayOfMonthList.filterIndexed { index, _ -> index < count + 1 }.sum()
            val sumPrevDayOfWeek = dayOfWeekList.filterIndexed { index, _ -> index < count + 1 }.sum()
            val sumNextDayOfWeek = nextDayOfWeekList.filterIndexed { index, _ -> index < count + 1 }.sum()

            if (sumMonthDateCount + sumPrevDayOfWeek + sumNextDayOfWeek > position) {
                currentDateIndex = position - dayOfMonthList.filterIndexed { index, _ -> index < count }.sum() - dayOfWeekList.filterIndexed { index, _ -> index < count }.sum() - nextDayOfWeekList.filterIndexed { index, _ -> index < count }.sum()
                break
            }
        }
        if (currentDateIndex > 1) {
            return 1
        } else {
            return 7
        }
    }
}