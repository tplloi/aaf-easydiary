package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_daily_symbol.view.*
import kotlinx.android.synthetic.main.viewholder_daily_symbol.view.*
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.Leisure
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import java.util.*

class DailySymbolViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {
    fun bindTo(leisure: Leisure) {
        itemView.date.text = leisure.date
        itemView.dayOfMonth.text = leisure.dayOfMonth
        itemView.dayOfWeek.text = leisure.dayOfWeekStr
        itemView.dayOfWeek.setTextColor(when (leisure.dayOfWeekNum) {
            Calendar.SATURDAY -> Color.BLUE
            Calendar.SUNDAY -> Color.RED
            else -> Color.BLACK
        })
        val pair = EasyDiaryDbHelper.readDiaryByDateString(leisure.dateString).partition { item ->
            activity.config.selectedSymbols.split(",").find { it.toInt() == item.weather } != null
        }

        when (pair.first.isEmpty()) {
            true -> itemView.noItemMessage.visibility = View.VISIBLE
            false -> itemView.noItemMessage.visibility = View.GONE
        }

        itemView.symbolFlexbox.removeAllViews()
        (activity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater).let {
            pair.first.map { diary ->
                val symbolCard = it.inflate(R.layout.layout_daily_symbol, null)
                FlavorUtils.initWeatherView(activity, symbolCard.dailySymbol, diary.weather)
                itemView.symbolFlexbox.addView(symbolCard)
            }
        }
    }
}