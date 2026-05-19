package com.example.deuktemsiru_seller.ui.notification

import android.os.Bundle
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
import com.example.deuktemsiru_seller.network.NotificationTarget
import com.example.deuktemsiru_seller.network.NotificationApiResponse
import com.example.deuktemsiru_seller.util.dp
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SendNotificationRequest
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.setSelectedChip
import com.example.deuktemsiru_seller.util.simpleTextWatcher
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.visibleIf
import kotlinx.coroutines.launch

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private var selectedTarget = NotificationTarget.Regular
    private var selectedRadiusKm = 3

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
                toast("메시지를 입력해주세요.")
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
            selectedTarget = NotificationTarget.Regular
            updateTargetUI()
        }
        binding.optionNearby.setOnClickListener {
            selectedTarget = NotificationTarget.Nearby
            updateTargetUI()
        }

        binding.btnRadius3.setOnClickListener { selectedRadiusKm = 3; updateRadiusUI() }
        binding.btnRadius5.setOnClickListener { selectedRadiusKm = 5; updateRadiusUI() }
        binding.btnRadius10.setOnClickListener { selectedRadiusKm = 10; updateRadiusUI() }
    }

    private fun updateTargetUI() {
        val isRegular = selectedTarget == NotificationTarget.Regular
        binding.optionRegular.setBackgroundResource(if (isRegular) R.drawable.bg_card_primary_border else R.drawable.bg_card_white)
        binding.optionNearby.setBackgroundResource(if (!isRegular) R.drawable.bg_card_primary_border else R.drawable.bg_card_white)
        binding.indicatorRegular.setBackgroundResource(if (isRegular) R.drawable.bg_rounded_primary else R.drawable.bg_rounded_muted)
        binding.indicatorNearby.setBackgroundResource(if (!isRegular) R.drawable.bg_rounded_primary else R.drawable.bg_rounded_muted)
        binding.layoutNearbyOptions.visibleIf(!isRegular)
        if (!isRegular) updateRadiusUI()
    }

    private fun updateRadiusUI() {
        listOf(
            Pair(binding.btnRadius3, 3),
            Pair(binding.btnRadius5, 5),
            Pair(binding.btnRadius10, 10),
        ).forEach { (btn, km) ->
            btn.setSelectedChip(
                selected = km == selectedRadiusKm,
                selectedBackground = R.drawable.bg_button_primary,
                unselectedBackground = R.drawable.bg_rounded_muted,
            )
        }

        binding.radiusMapView.radiusKm = selectedRadiusKm
        val approxCount = when (selectedRadiusKm) { 3 -> 12; 5 -> 28; else -> 67 }
        binding.tvNearbyCount.text = "반경 ${selectedRadiusKm}km 내 약 ${approxCount}명"
    }

    private fun setupMessageInput() {
        binding.etMessage.addTextChangedListener(simpleTextWatcher { text ->
            binding.tvCharCount.text = "${text.length}/40"
            binding.tvPreviewMessage.text = text
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
                        val updated = loadPhrases().also { it.add(phrase) }
                        savePhrases(updated)
                        renderPhraseChips()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun loadPhrases(): MutableList<String> {
        val saved = phrasePrefs().getString("phrases", null)
        return saved?.split("|")?.filter { it.isNotBlank() }?.toMutableList()
            ?: mutableListOf("마감 특가!", "오늘만 할인", "선착순!")
    }

    private fun savePhrases(phrases: List<String>) {
        phrasePrefs().edit().putString("phrases", phrases.joinToString("|")).apply()
    }

    private fun phrasePrefs() =
        requireContext().getSharedPreferences("notification_phrases", android.content.Context.MODE_PRIVATE)

    private fun renderPhraseChips() {
        binding.phraseChipsContainer.renderChildren(
            items = loadPhrases(),
            emptyView = { View(requireContext()) },
        ) { phrase ->
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
            chip
        }
    }

    private fun sendNotification(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val result = RetrofitClient.api.sendNotification(
                        SendNotificationRequest(
                            message = message,
                        targetType = selectedTarget.apiValue,
                        radiusKm = if (selectedTarget == NotificationTarget.Nearby) selectedRadiusKm else null,
                    )
                ).data
                toast("${result?.recipientCount ?: 0}명에게 발송 완료!", Toast.LENGTH_LONG)
                binding.etMessage.text?.clear()
                loadHistory()
            }.onFailure {
                toast("알림 발송에 실패했어요.")
            }
        }
    }

    private fun loadHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                renderHistory(RetrofitClient.api.getNotifications().data ?: emptyList())
            }.onFailure {
                renderHistory(emptyList())
            }
        }
    }

    private fun renderHistory(items: List<NotificationApiResponse>) {
        binding.notificationHistoryContainer.renderChildren(
            items = items.take(5),
            emptyView = { historyText("아직 발송한 알림이 없어요") },
            itemView = { item -> historyText("${item.message}\n${item.recipientCount}명 대상 · ${formatSentAt(item.sentAt)}") },
        )
    }

    private fun historyText(text: String): TextView = TextView(requireContext()).apply {
        this.text = text; textSize = 13f
        setTextColor(requireContext().getColor(R.color.text_sub))
        setPadding(0, 8.dp, 0, 8.dp)
    }

    private fun formatSentAt(value: String): String = value.replace('T', ' ').take(16)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
