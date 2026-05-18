package com.example.deuktemsiru_seller.util

import android.text.Editable
import android.text.TextWatcher

fun simpleTextWatcher(onChanged: (String) -> Unit): TextWatcher = object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        onChanged(s?.toString().orEmpty())
    }
    override fun afterTextChanged(s: Editable?) = Unit
}
