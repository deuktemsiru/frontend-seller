package com.example.deuktemsiru_seller.ui.settings

import android.os.Bundle
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

        notificationSettings().forEach { setting ->
            setting.switch.isChecked = prefs.getBoolean(setting.key, setting.defaultValue)
            setting.switch.setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(setting.key, checked).apply()
            }
        }
    }

    private data class NotificationSetting(
        val switch: android.widget.CompoundButton,
        val key: String,
        val defaultValue: Boolean,
    )

    private fun notificationSettings() = listOf(
        NotificationSetting(binding.switchNewOrder, "new_order", true),
        NotificationSetting(binding.switchPickupComplete, "pickup_complete", true),
        NotificationSetting(binding.switchSaleComplete, "sale_complete", false),
    )

    companion object {
        fun isEnabled(context: android.content.Context, key: String): Boolean =
            context.getSharedPreferences("notif_settings", MODE_PRIVATE).getBoolean(
                key,
                key in setOf("new_order", "pickup_complete")
            )
    }
}
