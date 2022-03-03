package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemDdayAddBinding
import me.blog.korn123.easydiary.databinding.ItemDdayBinding
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.models.DDay

class DDayAdapter(
    val activity: Activity,
    private val dDayItems: MutableList<DDay>

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType == 0) {
            true -> DDayViewHolder(ItemDdayBinding.inflate(activity.layoutInflater))
            false -> DDayAddViewHolder(ItemDdayAddBinding.inflate(activity.layoutInflater))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dDayItems.size == position.plus(1)) {
            true -> 1
            false -> 0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (dDayItems.size == position.plus(1)) {
            true -> (holder as DDayAddViewHolder).bindTo(dDayItems[position])
            false -> (holder as DDayViewHolder).bindTo(dDayItems[position])
        }
    }

    inner class DDayViewHolder(private val itemDDayBinding: ItemDdayBinding) : RecyclerView.ViewHolder(itemDDayBinding.root) {

        init {
            activity.run {
                initTextSize(itemDDayBinding.root)
                updateTextColors(itemDDayBinding.root)
                updateAppViews(itemDDayBinding.root)
                updateCardViewPolicy(itemDDayBinding.root)
                FontUtils.setFontsTypeface(this, this.assets, null, itemDDayBinding.root)
            }
        }

        fun bindTo(dDay: DDay) {
            itemDDayBinding.run {
                title.text = dDay.title
                targetDate.text = "2022.03.03"
                remainDays.text = "+100"
            }
        }
    }

    inner class DDayAddViewHolder(private val itemDDayAddBinding: ItemDdayAddBinding) : RecyclerView.ViewHolder(itemDDayAddBinding.root) {

        init {
            activity.run {
                initTextSize(itemDDayAddBinding.root)
                updateTextColors(itemDDayAddBinding.root)
                updateAppViews(itemDDayAddBinding.root)
                updateCardViewPolicy(itemDDayAddBinding.root)
                FontUtils.setFontsTypeface(this, this.assets, null, itemDDayAddBinding.root)
            }
        }

        fun bindTo(dDay: DDay) {
            itemDDayAddBinding.run {}
        }
    }

    override fun getItemCount(): Int = dDayItems.size
}