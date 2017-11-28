package xyz.kuririnz.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import xyz.kuririnz.verticalcalendar.VerticalCalendar

class CalendarActivity : AppCompatActivity() {

    private lateinit var verticalCalendar: VerticalCalendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        verticalCalendar = findViewById(R.id.CustomCalendarView)
        verticalCalendar.setSelectItemListener { _, _ ->
            println("setSelectItemListener event success")
        }
    }
}
