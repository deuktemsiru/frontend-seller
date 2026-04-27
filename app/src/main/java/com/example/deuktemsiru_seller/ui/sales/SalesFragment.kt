package com.example.deuktemsiru_seller.ui.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.databinding.FragmentSalesBinding

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabWeekly.setOnClickListener { selectPeriod(0) }
        binding.tabMonthly.setOnClickListener { selectPeriod(1) }
        binding.tabYearly.setOnClickListener { selectPeriod(2) }
    }

    private fun selectPeriod(index: Int) {
        val tabs = listOf(binding.tabWeekly, binding.tabMonthly, binding.tabYearly)
        tabs.forEachIndexed { i, tab ->
            if (i == index) {
                tab.setTextColor(requireContext().getColor(R.color.white))
                tab.setBackgroundResource(R.drawable.bg_button_primary)
            } else {
                tab.setTextColor(requireContext().getColor(R.color.text_muted))
                tab.background = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
