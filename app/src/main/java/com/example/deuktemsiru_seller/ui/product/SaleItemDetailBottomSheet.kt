package com.example.deuktemsiru_seller.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.databinding.BottomSheetSaleItemDetailBinding
import com.example.deuktemsiru_seller.network.SaleItemApiResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson

class SaleItemDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSaleItemDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(item: SaleItemApiResponse) = SaleItemDetailBottomSheet().apply {
            arguments = Bundle().apply { putString("item", Gson().toJson(item)) }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(R.drawable.bg_bottom_sheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetSaleItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = Gson().fromJson(arguments?.getString("item"), SaleItemApiResponse::class.java) ?: return

        binding.tvDetailEmoji.text = item.emoji ?: "🛍️"
        binding.tvDetailName.text = item.name
        binding.tvDetailPickupTime.text = "픽업 ${item.displayPickupTime}"
        binding.tvDetailDiscountPrice.text = "%,d원".format(item.discountedPrice)
        binding.tvDetailOriginalPrice.text = "(%,d원)".format(item.originalPrice)
        binding.tvDetailStock.text = "${item.remainingItems} / ${item.totalItems}개"
        binding.tvDetailTime.text = item.displayPickupTime

        val (statusText, bgRes, textColor) = when (item.status) {
            "AVAILABLE" -> Triple("판매중", R.drawable.bg_rounded_success_light, requireContext().getColor(R.color.success))
            "SOLD_OUT"  -> Triple("품절",   R.drawable.bg_rounded_danger,         0xFFFFFFFF.toInt())
            else        -> Triple("종료",   R.drawable.bg_rounded_muted,          requireContext().getColor(R.color.text_muted))
        }
        binding.tvDetailStatus.text = statusText
        binding.tvDetailStatus.setBackgroundResource(bgRes)
        binding.tvDetailStatus.setTextColor(textColor)

        binding.btnGoToProduct.setOnClickListener {
            (activity as? MainActivity)?.navigateToProduct()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
