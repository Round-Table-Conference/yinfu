package com.example.yinfuapp.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yinfuapp.data.model.MedicationReminder
import com.example.yinfuapp.databinding.ItemMedicationReminderBinding

class MedicationAdapter(
    private val onTakenChanged: (MedicationReminder, Boolean) -> Unit
) : ListAdapter<MedicationReminder, MedicationAdapter.MedicationViewHolder>(MedicationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationReminderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MedicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MedicationViewHolder(private val binding: ItemMedicationReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(medication: MedicationReminder) {
            binding.medicationName.text = medication.name
            binding.medicationDosage.text = medication.dosage
            binding.medicationTime.text = medication.time
            binding.medicationFrequency.text = medication.frequency
            binding.medicationRemark.text = medication.remark?.ifEmpty { "无备注" } ?: "无备注"

            binding.medicationTaken.setOnCheckedChangeListener(null)
            binding.medicationTaken.isChecked = medication.isTaken == 1
            binding.medicationTaken.setOnCheckedChangeListener { _, isChecked ->
                onTakenChanged(medication, isChecked)
            }
        }
    }

    class MedicationDiffCallback : DiffUtil.ItemCallback<MedicationReminder>() {
        override fun areItemsTheSame(oldItem: MedicationReminder, newItem: MedicationReminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MedicationReminder, newItem: MedicationReminder): Boolean {
            return oldItem == newItem
        }
    }
}