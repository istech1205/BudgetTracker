package com.istech.expensestracker.ui.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.istech.expensestracker.R
import com.istech.expensestracker.databinding.ItemExpenseBinding
import com.istech.expensestracker.model.Expense

/**
 * PagingDataAdapter for displaying expenses in a RecyclerView.
 */
class ExpenseAdapter :
    PagingDataAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {
    var onUpdateClick: ((Expense) -> Unit)? = null
    var onDeleteClick: ((Expense) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, onUpdateClick, onDeleteClick) }
    }

    /**
     * ViewHolder for an expense item.
     */
    class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            expense: Expense,
            onUpdateClick: ((Expense) -> Unit)?,
            onDeleteClick: ((Expense) -> Unit)?
        ) {
            binding.tvAmount.text = buildString {
                append(binding.root.context.getString(R.string.rupees))
                append(expense.amount)
            }
            binding.tvCategory.text = expense.category
            binding.tvDate.text =
                java.text.SimpleDateFormat("dd MMM yyyy").format(java.util.Date(expense.date))
            binding.tvNote.text = expense.note ?: ""

            binding.ivMore.setOnClickListener {
                val popupMenu = PopupMenu(binding.ivMore.context, binding.ivMore)
                popupMenu.menu.add("Update")
                popupMenu.menu.add("Delete")
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.title) {
                        "Update" -> {
                            onUpdateClick?.invoke(expense)
                            true
                        }

                        "Delete" -> {
                            onDeleteClick?.invoke(expense)
                            true
                        }

                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean =
            oldItem == newItem
    }
} 