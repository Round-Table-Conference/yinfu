package com.example.yinfuapp.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yinfuapp.data.model.EmergencyContact
import com.example.yinfuapp.databinding.ItemEmergencyContactBinding

class EmergencyContactAdapter(
    private val onContactClick: (EmergencyContact) -> Unit,
    private val onDeleteClick: ((EmergencyContact) -> Unit)? = null
) : ListAdapter<EmergencyContact, EmergencyContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    private var isEditMode = false

    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemEmergencyContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(private val binding: ItemEmergencyContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: EmergencyContact) {
            binding.contactName.text = "${contact.relationship ?: "联系人"} - ${contact.name}"
            binding.contactPhone.text = contact.phone

            binding.contactDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                onContactClick(contact)
            }

            binding.contactDelete.setOnClickListener {
                onDeleteClick?.invoke(contact)
            }
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem == newItem
        }
    }
}