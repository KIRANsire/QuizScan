package com.quizscan.app.ui.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quizscan.app.R
import com.quizscan.app.databinding.ItemOptionBinding

class OptionAdapter(
    private val onOptionClicked: (String) -> Unit
) : ListAdapter<String, OptionAdapter.OptionViewHolder>(OptionDiffCallback) {

    private var correctAnswer: String? = null
    private var selectedAnswer: String? = null
    private var isLocked: Boolean = false

    fun setQuizState(correct: String?, selected: String?, locked: Boolean) {
        correctAnswer = correct
        selectedAnswer = selected
        isLocked = locked
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = getItem(position)
        holder.bind(option)
    }

    inner class OptionViewHolder(
        private val binding: ItemOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: String) {
            val context = binding.root.context
            binding.tvOptionText.text = option

            val strokeWidthNormal = context.resources.getDimensionPixelSize(R.dimen.stroke_width_normal)
            val strokeWidthThick = context.resources.getDimensionPixelSize(R.dimen.stroke_width_thick)

            if (isLocked) {
                binding.cardOption.isClickable = false
                when {
                    option == correctAnswer -> {
                        binding.cardOption.setCardBackgroundColor(
                            ContextCompat.getColor(context, R.color.option_correct_bg)
                        )
                        binding.cardOption.strokeColor = ContextCompat.getColor(context, R.color.option_correct_stroke)
                        binding.cardOption.strokeWidth = strokeWidthThick
                        binding.tvOptionText.setTextColor(
                            ContextCompat.getColor(context, R.color.option_correct_text)
                        )
                    }
                    option == selectedAnswer -> {
                        binding.cardOption.setCardBackgroundColor(
                            ContextCompat.getColor(context, R.color.option_wrong_bg)
                        )
                        binding.cardOption.strokeColor = ContextCompat.getColor(context, R.color.option_wrong_stroke)
                        binding.cardOption.strokeWidth = strokeWidthThick
                        binding.tvOptionText.setTextColor(
                            ContextCompat.getColor(context, R.color.option_wrong_text)
                        )
                    }
                    else -> {
                        binding.cardOption.setCardBackgroundColor(
                            ContextCompat.getColor(context, R.color.option_default_bg)
                        )
                        binding.cardOption.strokeColor = ContextCompat.getColor(context, R.color.option_default_stroke)
                        binding.cardOption.strokeWidth = strokeWidthNormal
                        binding.tvOptionText.setTextColor(
                            ContextCompat.getColor(context, R.color.on_surface)
                        )
                    }
                }
            } else {
                binding.cardOption.isClickable = true
                binding.cardOption.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.option_default_bg)
                )
                binding.cardOption.strokeColor = ContextCompat.getColor(context, R.color.option_default_stroke)
                binding.cardOption.strokeWidth = strokeWidthNormal
                binding.tvOptionText.setTextColor(
                    ContextCompat.getColor(context, R.color.on_surface)
                )

                binding.root.setOnClickListener {
                    onOptionClicked(option)
                }
            }
        }
    }

    object OptionDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}
