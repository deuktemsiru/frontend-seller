package com.example.deuktemsiru_seller.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.databinding.FragmentHomeBinding
import com.example.deuktemsiru_seller.ui.registration.MenuRegistrationActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardSales.setOnClickListener {
            (activity as? MainActivity)?.navigateToOrder()
        }

        binding.cardNewOrder.setOnClickListener {
            (activity as? MainActivity)?.navigateToOrder()
        }

        binding.btnRegisterMenu.setOnClickListener {
            startActivity(Intent(requireContext(), MenuRegistrationActivity::class.java))
        }

        binding.btnSendNotification.setOnClickListener {
            (activity as? MainActivity)?.navigateToNotification()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
