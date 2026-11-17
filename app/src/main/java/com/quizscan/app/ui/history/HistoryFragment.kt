package com.quizscan.app.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.quizscan.app.QuizScanApplication
import com.quizscan.app.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        val app = requireActivity().application as QuizScanApplication
        HistoryViewModelFactory(app.scoreRepository)
    }

    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@HistoryFragment.adapter
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is HistoryUiState.Loading -> {
                            binding.progressBarHistory.visibility = View.VISIBLE
                            binding.tvEmptyHistory.visibility = View.GONE
                            binding.rvHistory.visibility = View.GONE
                        }
                        is HistoryUiState.Success -> {
                            binding.progressBarHistory.visibility = View.GONE
                            if (state.scores.isEmpty()) {
                                binding.tvEmptyHistory.visibility = View.VISIBLE
                                binding.rvHistory.visibility = View.GONE
                            } else {
                                binding.tvEmptyHistory.visibility = View.GONE
                                binding.rvHistory.visibility = View.VISIBLE
                                adapter.submitList(state.scores)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
