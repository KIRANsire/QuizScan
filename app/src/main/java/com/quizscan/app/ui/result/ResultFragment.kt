package com.quizscan.app.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.quizscan.app.QuizScanApplication
import com.quizscan.app.R
import com.quizscan.app.databinding.FragmentResultBinding

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResultViewModel by viewModels {
        val app = requireActivity().application as QuizScanApplication
        ResultViewModelFactory(this, app.scoreRepository, arguments)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvScore.text = getString(R.string.score_display, viewModel.score)
        binding.tvResultCategory.text = getString(R.string.result_category_label, viewModel.category)
        binding.tvResultDifficulty.text = getString(R.string.result_difficulty_label, viewModel.difficulty.replaceFirstChar { it.uppercase() })

        binding.btnPlayAgain.setOnClickListener {
            val bundle = android.os.Bundle().apply {
                putInt("categoryId", viewModel.categoryId)
                putString("difficulty", viewModel.difficulty)
            }
            findNavController().navigate(R.id.action_resultFragment_to_quizFragment, bundle)
        }

        binding.btnHome.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
