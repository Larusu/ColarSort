package com.colarsort.app.adapters

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.colarsort.app.databinding.ItemWorkerBinding
import com.colarsort.app.models.Users
import com.colarsort.app.repository.UsersRepo
import com.colarsort.app.utils.UtilityHelper.showCustomToast

class UserAdapter(
    private val users: ArrayList<Users>,
    private val usersRepo: UsersRepo
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWorkerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.binding.tvUsername.text = user.username
        holder.binding.tvUserRole.text = user.role

        holder.binding.ivDeleteUser.setOnClickListener {
            val context = holder.itemView.context

            AlertDialog.Builder(context)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Delete") { dialog, _ ->

                    // Prevent from deleting manager role

                    if (user.role == "Manager") {
                        AlertDialog.Builder(context)
                            .setTitle("Error")
                            .setMessage("You cannot delete the Manager role.")
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .show()
                        return@setPositiveButton
                    }

                    val success = usersRepo.deleteUser(user.id!!.toInt()) // safe now


                    if (success) {
                        val pos = holder.adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            users.removeAt(pos)
                            notifyItemRemoved(pos)
                        }
                        showCustomToast(context as Activity, "User deleted successfully")
                        dialog.dismiss()
                    } else {
                        showCustomToast(context as Activity, "Failed to delete user")
                    }
                    dialog.dismiss()
                }
                .show()
        }

    }

    override fun getItemCount(): Int {
        return users.size
    }
}

