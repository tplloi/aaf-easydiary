package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.extensions.*
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_dev.*
import kotlinx.android.synthetic.main.dialog_alarm.view.*
import kotlinx.android.synthetic.main.layout_settings_schedule.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.SpacesItemDecoration
import me.blog.korn123.easydiary.adapters.AlarmAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Alarm
import java.util.*
import kotlin.math.pow

class SettingsScheduleFragment() : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mRootView: ViewGroup
    private lateinit var mAlarmAdapter: AlarmAdapter
    private var mAlarmList: ArrayList<Alarm> = arrayListOf()
    private val mActivity: Activity
        get() = activity!!


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.layout_settings_schedule, container, false) as ViewGroup
        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateFragmentUI(mRootView)

        mAlarmAdapter = AlarmAdapter(
                mActivity,
                mAlarmList,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    openAlarmDialog(EasyDiaryDbHelper.duplicateAlarm(mAlarmList[position]), mAlarmList[position])
                }
        )

        alarmRecyclerView.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(mActivity, 1)
            addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            adapter = mAlarmAdapter
        }

        initProperties()
        bindEvent()
        updateAlarmList()
    }

    override fun onPause() {
        super.onPause()
        EasyDiaryUtils.changeDrawableIconColor(mActivity, android.R.color.white, R.drawable.delete_w)
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mRootView)
        EasyDiaryUtils.changeDrawableIconColor(mActivity, mActivity.config.textColor, R.drawable.delete_w)
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun openAlarmDialog(temporaryAlarm: Alarm, storedAlarm: Alarm? = null) {
        mActivity.run {
            var rootView: View? = null
            var alertDialog: AlertDialog? = null
            val builder = AlertDialog.Builder(this).apply {
                setCancelable(false)
                setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    // update alarm schedule
                    if (temporaryAlarm.isEnabled) {
                        scheduleNextAlarm(temporaryAlarm, true)
                    } else {
                        cancelAlarmClock(temporaryAlarm)
                    }

                    // save alarm
                    temporaryAlarm.label = rootView?.alarmDescription?.text.toString()
                    EasyDiaryDbHelper.updateAlarm(temporaryAlarm)
                    alertDialog?.dismiss()
                    updateAlarmList()
                }
                setNegativeButton(getString(android.R.string.cancel)) { _, _ -> alertDialog?.dismiss() }
            }
            val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rootView = inflater.inflate(R.layout.dialog_alarm, null).apply {
                val dayLetters = resources.getStringArray(R.array.week_day_letters).toList() as ArrayList<String>
                val dayIndexes = arrayListOf(0, 1, 2, 3, 4, 5, 6)
                if (config.isSundayFirst) {
                    dayIndexes.moveLastItemToFront()
                }
                edit_alarm_days_holder.removeAllViews()
                dayIndexes.forEach {
                    val pow = 2.0.pow(it.toDouble()).toInt()
                    val day = layoutInflater.inflate(R.layout.alarm_day, edit_alarm_days_holder, false) as TextView
                    day.text = dayLetters[it]

                    val isDayChecked = temporaryAlarm.days and pow != 0
                    day.background = getProperDayDrawable(isDayChecked)

                    day.setTextColor(if (isDayChecked) config.backgroundColor else config.textColor)
                    day.setOnClickListener {
                        EasyDiaryDbHelper.beginTransaction()
                        val selectDay = temporaryAlarm.days and pow == 0
                        temporaryAlarm.days = if (selectDay) {
                            temporaryAlarm.days.addBit(pow)
                        } else {
                            temporaryAlarm.days.removeBit(pow)
                        }
                        day.background = getProperDayDrawable(selectDay)
                        day.setTextColor(if (selectDay) config.backgroundColor else config.textColor)
                        alarm_days.text = getSelectedDaysString(temporaryAlarm.days)
                        EasyDiaryDbHelper.commitTransaction()
                    }

                    edit_alarm_days_holder.addView(day)
                }
                alarm_days.text = getSelectedDaysString(temporaryAlarm.days)
                alarm_days.setTextColor(config.textColor)
                alarm_switch.isChecked = temporaryAlarm.isEnabled
                alarmDescription.setText(temporaryAlarm.label)
                edit_alarm_time.text = getFormattedTime(temporaryAlarm.timeInMinutes * 60, false, true)

                edit_alarm_time.setOnClickListener {
                    TimePickerDialog(this@run, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        temporaryAlarm.timeInMinutes = hourOfDay * 60 + minute
                        edit_alarm_time.text = getFormattedTime(temporaryAlarm.timeInMinutes * 60, false, true)
                    }, temporaryAlarm.timeInMinutes / 60, temporaryAlarm.timeInMinutes % 60, DateFormat.is24HourFormat(this@run)).show()
                }
                alarm_switch.setOnCheckedChangeListener { _, isChecked ->
                    temporaryAlarm.isEnabled = isChecked
                }
                when (storedAlarm == null) {
                    true -> { deleteAlarm.visibility = View.GONE }
                    false -> {
                        deleteAlarm.setOnClickListener {
                            alertDialog?.dismiss()
                            EasyDiaryDbHelper.beginTransaction()
                            storedAlarm.deleteFromRealm()
                            EasyDiaryDbHelper.commitTransaction()
                            updateAlarmList()
                        }
                    }
                }

                if (this is ViewGroup) {
                    this.setBackgroundColor(config.backgroundColor)
                    initTextSize(this)
                    updateTextColors(this)
                    updateAppViews(this)
                    FontUtils.setFontsTypeface(this@run, this@run.assets, null, this)
                }
            }
            alertDialog = builder.create().apply {
                val customTitle = when (temporaryAlarm.workMode) {
                    Alarm.WORK_MODE_DIARY_WRITING -> "다이어리 쓰기 알림 설정"
                    Alarm.WORK_MODE_DIARY_BACKUP_LOCAL -> "디바이스 저장소 백업 설정"
                    else -> ""
                }
                updateAlertDialog(this, null, rootView, customTitle)
            }
        }
    }

    private fun bindEvent() {
        mActivity.run {
            nextAlarm.setOnClickListener {
                val nextAlarm = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val triggerTimeMillis = (getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock?.triggerTime ?: 0
                    when (triggerTimeMillis > 0) {
                        true -> DateUtils.getFullPatternDateWithTime(triggerTimeMillis)
                        false -> "Alarm info is not exist."
                    }
                } else {
                    Settings.System.getString(contentResolver, Settings.System.NEXT_ALARM_FORMATTED)
                }

                toast(nextAlarm, Toast.LENGTH_LONG)
            }

            addWritingAlarm.setOnClickListener { openAlarmDialog(EasyDiaryDbHelper.createTemporaryAlarm()) }
            addLocalBackupAlarm.setOnClickListener {
                openAlarmDialog(EasyDiaryDbHelper.createTemporaryAlarm(Alarm.WORK_MODE_DIARY_BACKUP_LOCAL))
            }
        }
    }

    fun updateAlarmList() {
        mAlarmList.clear()
        mAlarmList.addAll(EasyDiaryDbHelper.readAlarmAll())
        mAlarmAdapter.notifyDataSetChanged()
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.circle_background_filled else R.drawable.circle_background_stroke
        val drawable = ContextCompat.getDrawable(mActivity, drawableId)
        drawable!!.applyColorFilter(mActivity.config.textColor)
        return drawable
    }

    private fun initProperties() {
        mActivity.config.use24HourFormat = false
        val calendar = Calendar.getInstance(Locale.getDefault())
        var minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60
        minutes += calendar.get(Calendar.MINUTE)

        val tempAlarm = Alarm(0)
        if (EasyDiaryDbHelper.countAlarmAll() == 0L) {
            EasyDiaryDbHelper.insertAlarm(tempAlarm)
        }
    }

    companion object {
        const val ALARM_ID = "alarm_id"
    }
}