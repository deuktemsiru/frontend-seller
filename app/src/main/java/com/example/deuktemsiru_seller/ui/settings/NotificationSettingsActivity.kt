package com.example.deuktemsiru_seller.ui.settings

import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deuktemsiru_seller.databinding.ActivityNotificationSettingsBinding

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private val prefs by lazy { getSharedPreferences("notif_settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.switchNewOrder.isChecked = prefs.getBoolean("new_order", true)
        binding.switchPickupComplete.isChecked = prefs.getBoolean("pickup_complete", true)
        binding.switchSaleComplete.isChecked = prefs.getBoolean("sale_complete", false)

        val listener = CompoundButton.OnCheckedChangeListener { button, checked ->
            when (button.id) {
                binding.switchNewOrder.id -> prefs.edit().putBoolean("new_order", checked).apply()
                binding.switchPickupComplete.id -> prefs.edit().putBoolean("pickup_complete", checked).apply()
                binding.switchSaleComplete.id -> prefs.edit().putBoolean("sale_complete", checked).apply()
            }
        }
        binding.switchNewOrder.setOnCheckedChangeListener(listener)
        binding.switchPickupComplete.setOnCheckedChangeListener(listener)
        binding.switchSaleComplete.setOnCheckedChangeListener(listener)
    }

    companion object {
        fun isEnabled(context: android.content.Context, key: String): Boolean =
            context.getSharedPreferences("notif_settings", MODE_PRIVATE).getBoolean(key, key != "sale_complete")
    }
}
