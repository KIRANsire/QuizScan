package com.quizscan.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.quizscan.app.QuizScanApplication
import com.quizscan.app.R
import com.quizscan.app.databinding.FragmentHomeBinding
import com.quizscan.app.model.Category
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as QuizScanApplication
        HomeViewModelFactory(app.quizRepository)
    }

    private var categories: List<Category> = emptyList()
    private var selectedCategoryId: Int? = null
    private var selectedDifficulty: String? = null

    private val difficulties = listOf("easy", "medium", "hard")

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

        setupUserHeader()
        setupListeners()
        observeState()
    }

    private fun setupUserHeader() {
        val user = viewModel.getCurrentUser()
        if (user == null) {
            findNavController().navigate(R.id.action_homeFragment_to_authFragment)
            return
        }

        val displayName = user.displayName ?: user.email ?: getString(R.string.app_name)
        binding.tvUserName.text = getString(R.string.user_greeting, displayName)
    }

    private fun setupListeners() {
        binding.btnScanQr.setOnClickListener {
            startQrScanner()
        }

        binding.btnRetryCategories.setOnClickListener {
            viewModel.loadCategories()
        }

        binding.btnViewHistory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }

        val difficultyLabels = listOf(
            getString(R.string.difficulty_easy),
            getString(R.string.difficulty_medium),
            getString(R.string.difficulty_hard)
        )
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, difficultyLabels)
        binding.actvDifficulty.setAdapter(difficultyAdapter)

        binding.actvDifficulty.setOnItemClickListener { _, _, position, _ ->
            selectedDifficulty = difficulties[position]
            updateStartButtonState()
        }

        binding.btnStartQuiz.setOnClickListener {
            val catId = selectedCategoryId
            val diff = selectedDifficulty
            if (catId != null && diff != null) {
                viewModel.startManualQuiz(catId, diff)
            }
        }
    }

    private fun updateStartButtonState() {
        binding.btnStartQuiz.isEnabled = selectedCategoryId != null && selectedDifficulty != null
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoriesState.collect { state ->
                        when (state) {
                            is CategoriesUiState.Loading -> {
                                binding.layoutCategoriesLoading.visibility = View.VISIBLE
                                binding.layoutCategoriesError.visibility = View.GONE
                                binding.btnScanQr.isEnabled = false
                            }
                            is CategoriesUiState.Success -> {
                                binding.layoutCategoriesLoading.visibility = View.GONE
                                binding.layoutCategoriesError.visibility = View.GONE
                                binding.btnScanQr.isEnabled = true
                                categories = state.categories
                                setupCategoryDropdown(state.categories)
                            }
                            is CategoriesUiState.Error -> {
                                binding.layoutCategoriesLoading.visibility = View.GONE
                                binding.layoutCategoriesError.visibility = View.VISIBLE
                                binding.btnScanQr.isEnabled = false
                            }
                        }
                    }
                }

                launch {
                    viewModel.navEvents.collect { event ->
                        when (event) {
                            is HomeNavigationEvent.StartQuiz -> {
                                val bundle = android.os.Bundle().apply {
                                    putInt("categoryId", event.categoryId)
                                    putString("difficulty", event.difficulty)
                                }
                                findNavController().navigate(R.id.action_homeFragment_to_quizFragment, bundle)
                            }
                            is HomeNavigationEvent.ShowToast -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupCategoryDropdown(categoryList: List<Category>) {
        val names = categoryList.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        binding.actvCategory.setAdapter(adapter)

        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategoryId = categoryList[position].id
            updateStartButtonState()
        }
    }

    private fun startQrScanner() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(requireActivity(), options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                viewModel.handleQrScanned(barcode.rawValue)
            }
            .addOnCanceledListener {
                // Cancelled scan is handled gracefully
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message ?: getString(R.string.error_invalid_qr), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
