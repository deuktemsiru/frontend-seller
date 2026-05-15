package com.example.deuktemsiru_seller.ui.notification

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentNotificationBinding
import com.example.deuktemsiru_seller.network.NotificationApiResponse
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SendNotificationRequest
import kotlinx.coroutines.launch

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private var selectedTarget = "regular" // "regular" or "nearby"
    private var selectedRadiusKm = 3

    private val defaultPhrases = mutableListOf("마감 특가!", "오늘만 할인", "선착순!")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        binding.tvPreviewStoreName.text = session.nickname.ifBlank { "내 가게" }

        setupTargetSelection()
        setupMessageInput()
        setupQuickPhrases()
        if (session.isLoggedIn()) loadHistory()

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text?.toString()?.trim() ?: ""
            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!session.isLoggedIn()) return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle("알림 발송 확인")
                .setMessage("단골 고객들에게 알림을 보내시겠어요?")
                .setPositiveButton("보내기") { _, _ -> sendNotification(message) }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun setupTargetSelection() {
        updateTargetUI()

        binding.optionRegular.setOnClickListener {
            selectedTarget = "regular"
            updateTargetUI()
        }
        binding.optionNearby.setOnClickListener {
            selectedTarget = "nearby"
            updateTargetUI()
        }

        binding.btnRadius3.setOnClickListener { selectedRadiusKm = 3; updateRadiusUI() }
        binding.btnRadius5.setOnClickListener { selectedRadiusKm = 5; updateRadiusUI() }
        binding.btnRadius10.setOnClickListener { selectedRadiusKm = 10; updateRadiusUI() }
    }

    private fun updateTargetUI() {
        val isRegular = selectedTarget == "regular"
        binding.optionRegular.setBackgroundResource(if (isRegular) R.drawable.bg_card_primary_border else R.drawable.bg_card_white)
        binding.optionNearby.setBackgroundResource(if (!isRegular) R.drawable.bg_card_primary_border else R.drawable.bg_card_white)
        binding.indicatorRegular.setBackgroundResource(if (isRegular) R.drawable.bg_rounded_primary else R.drawable.bg_rounded_muted)
        binding.indicatorNearby.setBackgroundResource(if (!isRegular) R.drawable.bg_rounded_primary else R.drawable.bg_rounded_muted)
        binding.layoutNearbyOptions.visibility = if (!isRegular) View.VISIBLE else View.GONE
        if (!isRegular) updateRadiusUI()
    }

    private fun updateRadiusUI() {
        val activeRes = R.drawable.bg_button_primary
        val inactiveRes = R.drawable.bg_rounded_muted
        val activeColor = requireContext().getColor(R.color.white)
        val inactiveColor = requireContext().getColor(R.color.text_sub)

        listOf(
            Triple(binding.btnRadius3, 3, "btn_radius_3"),
            Triple(binding.btnRadius5, 5, "btn_radius_5"),
            Triple(binding.btnRadius10, 10, "btn_radius_10"),
        ).forEach { (btn, km, _) ->
            btn.setBackgroundResource(if (km == selectedRadiusKm) activeRes else inactiveRes)
            btn.setTextColor(if (km == selectedRadiusKm) activeColor else inactiveColor)
        }

        binding.radiusMapView.radiusKm = selectedRadiusKm
        val approxCount = when (selectedRadiusKm) { 3 -> 12; 5 -> 28; else -> 67 }
        binding.tvNearbyCount.text = "반경 ${selectedRadiusKm}km 내 약 ${approxCount}명"
    }

    private fun setupMessageInput() {
        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val count = s?.length ?: 0
                binding.tvCharCount.text = "$count/40"
                binding.tvPreviewMessage.text = s?.toString() ?: ""
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupQuickPhrases() {
        renderPhraseChips()

        binding.btnAddPhrase.setOnClickListener {
            val et = EditText(requireContext()).apply {
                hint = "문구 입력 (최대 15자)"; maxLines = 1
            }
            AlertDialog.Builder(requireContext())
                .setTitle("자주 쓰는 문구 추가")
                .setView(et)
                .setPositiveButton("추가") { _, _ ->
                    val phrase = et.text?.toString()?.trim() ?: ""
                    if (phrase.isNotBlank() && phrase.length <= 15) {
                        defaultPhrases.add(phrase)
                        renderPhraseChips()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun renderPhraseChips() {
        binding.phraseChipsContainer.removeAllViews()
        defaultPhrases.forEach { phrase ->
            val chip = TextView(requireContext()).apply {
                text = phrase
                textSize = 12f
                setTextColor(requireContext().getColor(R.color.text_sub))
                setBackgroundResource(R.drawable.bg_chip_orange)
                val hPad = (12 * resources.displayMetrics.density).toInt()
                val vPad = (4 * resources.displayMetrics.density).toInt()
                setPadding(hPad, vPad, hPad, vPad)
                val lp = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (34 * resources.displayMetrics.density).toInt())
                lp.marginEnd = (8 * resources.displayMetrics.density).toInt()
                lp.bottomMargin = (6 * resources.displayMetrics.density).toInt()
                layoutParams = lp
                gravity = android.view.Gravity.CENTER
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    val current = binding.etMessage.text?.toString() ?: ""
                    if (current.length + phrase.length <= 40) {
                        binding.etMessage.setText(if (current.isBlank()) phrase else "$current $phrase")
                        binding.etMessage.setSelection(binding.etMessage.text?.length ?: 0)
                    }
                }
            }
            binding.phraseChipsContainer.addView(chip)
        }
    }

    private fun sendNotification(message: String) {
        lifecycleScope.launch {
            runCatching {
                val result = RetrofitClient.api.sendNotification(
                    SendNotificationRequest(
                        message = message,
                        targetType = if (selectedTarget == "nearby") "NEARBY" else "REGULAR",
                        radiusKm = if (selectedTarget == "nearby") selectedRadiusKm else null,
                    )
                ).data
                Toast.makeText(requireContext(), "${result?.recipientCount ?: 0}명에게 발송 완료!", Toast.LENGTH_LONG).show()
                binding.etMessage.text?.clear()
                loadHistory()
            }.onFailure {
                Toast.makeText(requireContext(), "알림 발송에 실패했어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            runCatching {
                renderHistory(RetrofitClient.api.getNotifications().data ?: emptyList())
            }.onFailure {
                renderHistory(emptyList())
            }
        }
    }

    private fun renderHistory(items: List<NotificationApiResponse>) {
        binding.notificationHistoryContainer.removeAllViews()
        if (items.isEmpty()) {
            binding.notificationHistoryContainer.addView(historyText("아직 발송한 알림이 없어요"))
            return
        }
        items.take(5).forEach { item ->
            binding.notificationHistoryContainer.addView(
                historyText("${item.message}\n${item.recipientCount}명 대상 · ${formatSentAt(item.sentAt)}")
            )
        }
    }

    private fun historyText(text: String): TextView = TextView(requireContext()).apply {
        this.text = text; textSize = 13f
        setTextColor(requireContext().getColor(R.color.text_sub))
        setPadding(0, 8.dp, 0, 8.dp)
    }

    private fun formatSentAt(value: String): String = value.replace('T', ' ').take(16)

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
