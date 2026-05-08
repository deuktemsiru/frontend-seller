package com.example.deuktemsiru_seller.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentMyPageBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SampleData
import com.example.deuktemsiru_seller.ui.settings.NotificationSettingsActivity
import com.example.deuktemsiru_seller.ui.store.StoreFragment

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = SessionManager(requireContext())
        binding.tvStoreName.text = session.storeName.ifBlank { "내 가게" }

        val emoji = SampleData.findById(session.sellerId)?.store?.emoji ?: "🏪"
        binding.tvStoreEmoji.text = emoji

        binding.itemStoreInfo.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.deuktemsiru_seller.R.id.fragment_container, StoreFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.itemAccountInfo.setOnClickListener {
            startActivity(Intent(requireContext(), AccountInfoActivity::class.java))
        }

        binding.itemNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationSettingsActivity::class.java))
        }

        binding.itemCustomerService.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("고객센터")
                .setMessage("운영시간: 평일 09:00 – 18:00\n전화: 1588-0000\n이메일: help@siru.co.kr")
                .setPositiveButton("확인", null)
                .show()
        }

        binding.itemPolicy.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("서비스 이용약관")
                .setMessage("득템시루 판매자 서비스 이용약관\n\n판매자는 본 서비스를 통해 마감 할인 상품을 등록하고 관리할 수 있습니다.\n\n자세한 약관은 공식 웹사이트를 참고해주세요.")
                .setPositiveButton("확인", null)
                .show()
        }

        binding.itemLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠어요?")
                .setPositiveButton("로그아웃") { _, _ ->
                    session.clear()
                    RetrofitClient.disableSampleMode()
                    RetrofitClient.authToken = null
                    requireActivity().recreate()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        binding.tvVersion.text = "득템시루 판매자 v1.0  |  Seller ID #${session.sellerId}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
