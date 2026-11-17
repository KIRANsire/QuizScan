package com.quizscan.app.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quizscan.app.QuizScanApplication
import com.quizscan.app.R
import com.quizscan.app.databinding.FragmentQuizBinding
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizViewModel by viewModels {
        val app = requireActivity().application as QuizScanApplication
        QuizViewModelFactory(this, app.quizRepository, arguments)
    }

    private lateinit var optionAdapter: OptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackPressHandler()
        setupRecyclerView()
        setupListeners()
        observeState()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            }
        )
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.exit_quiz_title)
            .setMessage(R.string.exit_quiz_message)
            .setPositiveButton(R.string.exit_confirm) { _, _ ->
                findNavController().popBackStack()
            }
            .setNegativeButton(R.string.exit_cancel, null)
            .show()
    }

    private fun setupRecyclerView() {
        optionAdapter = OptionAdapter { selectedOption ->
            viewModel.selectOption(selectedOption)
        }
        binding.rvOptions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = optionAdapter
            itemAnimator = null
        }
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            viewModel.onNextOrFinishClicked()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loadState.collect { state ->
                        when (state) {
                            is QuizLoadState.Loading -> {
                                binding.layoutLoading.visibility = View.VISIBLE
                                binding.layoutError.visibility = View.GONE
                                setQuizViewsVisibility(View.GONE)
                            }
                            is QuizLoadState.Success -> {
                                binding.layoutLoading.visibility = View.GONE
                                binding.layoutError.visibility = View.GONE
                                setQuizViewsVisibility(View.VISIBLE)
                                renderCurrentQuestion()
                            }
                            is QuizLoadState.NotEnoughQuestions -> {
                                binding.layoutLoading.visibility = View.GONE
                                setQuizViewsVisibility(View.GONE)
                                binding.layoutError.visibility = View.VISIBLE
                                binding.tvErrorMessage.setText(R.string.error_not_enough_questions)
                                binding.btnErrorAction.setText(R.string.go_back)
                                binding.btnErrorAction.setOnClickListener {
                                    findNavController().popBackStack()
                                }
                            }
                            is QuizLoadState.RateLimited -> {
                                binding.layoutLoading.visibility = View.GONE
                                setQuizViewsVisibility(View.GONE)
                                binding.layoutError.visibility = View.VISIBLE
                                binding.tvErrorMessage.setText(R.string.error_rate_limited)
                                binding.btnErrorAction.setText(R.string.retry)
                                binding.btnErrorAction.setOnClickListener {
                                    viewModel.loadQuestions()
                                }
                            }
                            is QuizLoadState.Error -> {
                                binding.layoutLoading.visibility = View.GONE
                                setQuizViewsVisibility(View.GONE)
                                binding.layoutError.visibility = View.VISIBLE
                                binding.tvErrorMessage.text = state.message
                                binding.btnErrorAction.setText(R.string.retry)
                                binding.btnErrorAction.setOnClickListener {
                                    viewModel.loadQuestions()
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.currentQuestionIndex.collect {
                        renderCurrentQuestion()
                    }
                }

                launch {
                    viewModel.isOptionLocked.collect { isLocked ->
                        val currentIdx = viewModel.currentQuestionIndex.value
                        if (isLocked) {
                            binding.btnNext.visibility = View.VISIBLE
                            if (currentIdx >= 9) {
                                binding.btnNext.setText(R.string.finish_quiz)
                            } else {
                                binding.btnNext.setText(R.string.next_question)
                            }
                        } else {
                            binding.btnNext.visibility = View.GONE
                        }
                        renderCurrentQuestion()
                    }
                }

                launch {
                    viewModel.navigationEvent.collect { event ->
                        when (event) {
                            is QuizNavigationEvent.FinishQuiz -> {
                                val bundle = android.os.Bundle().apply {
                                    putInt("score", event.score)
                                    putString("category", event.category)
                                    putString("difficulty", event.difficulty)
                                    putInt("categoryId", event.categoryId)
                                }
                                findNavController().navigate(R.id.action_quizFragment_to_resultFragment, bundle)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setQuizViewsVisibility(visibility: Int) {
        binding.tvQuestionCounter.visibility = visibility
        binding.progressBarQuiz.visibility = visibility
        binding.tvQuestionText.visibility = visibility
        binding.rvOptions.visibility = visibility
    }

    private fun renderCurrentQuestion() {
        val question = viewModel.getCurrentQuestion() ?: return
        val index = viewModel.currentQuestionIndex.value
        val isLocked = viewModel.isOptionLocked.value
        val selectedOption = viewModel.selectedOption.value

        binding.tvQuestionCounter.text = getString(R.string.question_counter, index + 1)
        binding.progressBarQuiz.progress = index + 1
        binding.tvQuestionText.text = question.questionText

        optionAdapter.submitList(question.shuffledOptions)
        optionAdapter.setQuizState(
            correct = question.correctAnswer,
            selected = selectedOption,
            locked = isLocked
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
