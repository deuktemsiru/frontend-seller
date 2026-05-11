package com.example.deuktemsiru_seller.ui.notification

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        binding.tvPreviewStoreName.text = session.nickname.ifBlank { "내 가게" }

        if (session.isLoggedIn()) loadHistory()

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val count = s?.length ?: 0
                binding.tvCharCount.text = "$count/40"
                binding.tvPreviewMessage.text = s?.toString() ?: ""
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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

    private fun sendNotification(message: String) {
        lifecycleScope.launch {
            runCatching {
                val result = RetrofitClient.api.sendNotification(SendNotificationRequest(message)).data
                Toast.makeText(
                    requireContext(),
                    "${result?.recipientCount ?: 0}명에게 발송 완료!",
                    Toast.LENGTH_LONG
                ).show()
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

    private fun historyText(text: String): TextView =
        TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setTextColor(requireContext().getColor(R.color.text_sub))
            setPadding(0, 8.dp, 0, 8.dp)
        }

    private fun formatSentAt(value: String): String = value.replace('T', ' ').take(16)

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
