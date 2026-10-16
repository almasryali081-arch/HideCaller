package com.example.hidecaller

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var input: EditText
    private lateinit var group: RadioGroup
    private var pendingNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        root.addView(TextView(this).apply {
            text = "اتصال بدون إظهار رقمي"
            textSize = 22f
            gravity = Gravity.CENTER
        })

        input = EditText(this).apply {
            hint = "الرقم المراد الاتصال به"
            inputType = InputType.TYPE_CLASS_PHONE
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            gravity = Gravity.CENTER
            textSize = 20f
        }
        root.addView(input)

        root.addView(TextView(this).apply {
            text = "رمز الإخفاء (اختر ما يناسب شبكتك):"
            setPadding(0, pad, 0, 0)
        })

        group = RadioGroup(this)
        val codes = listOf(
            "#31#  (أغلب دول العالم والعرب)",
            "*67  (أمريكا الشمالية)",
            "*31#  (بعض الشبكات)"
        )
        codes.forEachIndexed { i, label ->
            group.addView(RadioButton(this).apply {
                text = label
                id = 100 + i
            })
        }
        group.check(100)
        root.addView(group)

        root.addView(Button(this).apply {
            text = "اتصل الآن"
            setOnClickListener { startCall() }
        })

        setContentView(root)
    }

    private fun prefix(): String = when (group.checkedRadioButtonId) {
        101 -> "*67"
        102 -> "*31#"
        else -> "#31#"
    }

    private fun startCall() {
        val number = input.text.toString().replace(" ", "").trim()
        if (number.isEmpty()) {
            Toast.makeText(this, "أدخل الرقم أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            pendingNumber = number
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 1)
            return
        }
        placeCall(number)
    }

    private fun placeCall(number: String) {
        val uri = Uri.parse("tel:" + Uri.encode(prefix() + number))
        try {
            startActivity(Intent(Intent.ACTION_CALL, uri))
        } catch (e: SecurityException) {
            Toast.makeText(this, "لا توجد صلاحية الاتصال", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            pendingNumber?.let { placeCall(it) }
        } else {
            Toast.makeText(this, "الصلاحية مطلوبة للاتصال", Toast.LENGTH_SHORT).show()
        }
        pendingNumber = null
    }
}
