package com.example.todolist.presentation.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.core.widget.CompoundButtonCompat
import androidx.databinding.BindingAdapter
import com.example.todolist.data.util.DateToString
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView
import java.util.*

@SuppressLint("SetTextI18n")
@BindingAdapter("count")
fun setCount(materialTextView: MaterialTextView, count: Int) {
    materialTextView.text = "Кол-во: $count"
}

@BindingAdapter("view_color")
fun setColor(view: View, color: String) {
    view.setBackgroundColor(Color.parseColor(color))
}

@BindingAdapter("check_status", "view_color")
fun setCheckStatus(materialCheckBox: MaterialCheckBox, status: Boolean, color: String) {
    materialCheckBox.setOnCheckedChangeListener(null)
    materialCheckBox.isChecked = status
    CompoundButtonCompat.setButtonTintList(
        materialCheckBox,
        ColorStateList.valueOf(Color.parseColor(color))
    )
}

@SuppressLint("SetTextI18n")
@BindingAdapter("set_date")
fun setDate(dueDate: MaterialTextView, date: Date) {
    dueDate.text = "До : " + DateToString.convertDateToString(date)
}

@SuppressLint("SetTextI18n")
@BindingAdapter("priority", "view_color")
fun setPriority(chip: Chip, priority: Int, color: String) {
    when (priority) {
        0 -> chip.text = "Низкая"
        1 -> chip.text = "Средняя"
        else -> chip.text = "Высокая"
    }
    chip.chipStrokeColor = ColorStateList.valueOf(Color.parseColor(color))
}