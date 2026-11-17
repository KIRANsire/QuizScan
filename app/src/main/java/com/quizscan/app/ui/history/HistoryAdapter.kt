package com.quizscan.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quizscan.app.R
import com.quizscan.app.data.local.ScoreEntity
import com.quizscan.app.databinding.ItemHistoryBinding
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<ScoreEntity, HistoryAdapter.HistoryViewHolder>(ScoreDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScoreEntity) {
            val context = binding.root.context
            binding.tvHistoryCategory.text = item.category
            binding.tvHistoryScore.text = context.getString(R.string.score_display, item.score)

            val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
            val formattedDate = dateFormat.format(Date(item.dateMillis))
            val difficultyCapitalized = item.difficulty.replaceFirstChar { it.uppercase() }

            binding.tvHistoryDetails.text = context.getString(
                R.string.history_item_details,
                formattedDate,
                difficultyCapitalized
            )
        }
    }

    object ScoreDiffCallback : DiffUtil.ItemCallback<ScoreEntity>() {
        override fun areItemsTheSame(oldItem: ScoreEntity, newItem: ScoreEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScoreEntity, newItem: ScoreEntity): Boolean {
            return oldItem == newItem
        }
    }
}
