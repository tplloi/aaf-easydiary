package me.blog.korn123.easydiary.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.createThumbnailGlideOptions
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemGalleryBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.FILE_URI_PREFIX
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_SMALL
import java.io.File
import java.util.*

class GalleryAdapter(
        val activity: Activity,
        private val listPostcard: List<AttachedPhoto>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>(), SectionedAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(activity, ItemGalleryBinding.inflate(activity.layoutInflater, parent, false), this)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bindTo(listPostcard[position])
    }

    override fun getItemCount() = listPostcard.size

    @SuppressLint("DefaultLocale")
    @NonNull
    override fun getSectionName(position: Int): String {
        return String.format("%d. %s", position + 1, listPostcard[position].file.name)
    }

    fun onItemHolderClick(itemHolder: GalleryViewHolder) {
        onItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.adapterPosition, itemHolder.itemId)
    }

    fun onItemCheckedChange(position: Int, isChecked: Boolean) {
        listPostcard[position].isItemChecked = isChecked
    }

    class GalleryViewHolder(
            val activity: Activity, private val itemGalleryBinding: ItemGalleryBinding, val adapter: GalleryAdapter
    ) : RecyclerView.ViewHolder(itemGalleryBinding.root), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        init {
            itemGalleryBinding.run {
                activity.updateAppViews(root)
                FontUtils.setFontsTypeface(activity, null, imageContainer)
                root.setOnClickListener(this@GalleryViewHolder)
                checkItem.setOnCheckedChangeListener(this@GalleryViewHolder)
            }
        }

        fun bindTo(attachedPhoto: AttachedPhoto) {
            val timeStampView = itemGalleryBinding.createdDate
            timeStampView.setTextSize(TypedValue.COMPLEX_UNIT_PX, activity.dpToPixelFloatValue(10F))
            itemGalleryBinding.checkItem.isChecked = attachedPhoto.isItemChecked
            timeStampView.text = when (attachedPhoto.currentTimeMillis) {
                0L -> GUIDE_MESSAGE
                else -> DateUtils.getDateTimeStringForceFormatting(attachedPhoto.currentTimeMillis, activity)
            }

            activity.run {
                val point =  getDefaultDisplay()
                val spanCount = if (activity.isLandScape()) config.postcardSpanCountLandscape else config.postcardSpanCountPortrait
                val targetX = point.x / spanCount
                itemGalleryBinding.imageContainer.layoutParams.height = targetX
                itemGalleryBinding.imageview.layoutParams.height = targetX
                itemGalleryBinding.imageview.scaleType = ImageView.ScaleType.CENTER
                Glide.with(itemGalleryBinding.imageview.context)
                        .load(attachedPhoto.file)
                        .apply(createThumbnailGlideOptions(targetX * PHOTO_CORNER_RADIUS_SCALE_FACTOR_SMALL))
                        .into(itemGalleryBinding.imageview)
            }
        }

        override fun onClick(p0: View?) {
            adapter.onItemHolderClick(this)
        }

        override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
            adapter.onItemCheckedChange(this.adapterPosition, p1)
        }
    }

    data class AttachedPhoto(val file: File, var isItemChecked: Boolean, val currentTimeMillis: Long)

    companion object {
        const val GUIDE_MESSAGE = "No information"
        const val POSTCARD_DATE_FORMAT = "yyyyMMddHHmmss"
    }
}