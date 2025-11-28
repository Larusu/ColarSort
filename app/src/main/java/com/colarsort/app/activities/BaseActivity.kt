package com.colarsort.app.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.colarsort.app.R
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.repository.UsersRepo
import com.colarsort.app.session.SessionManager
import com.colarsort.app.utils.UtilityHelper.showCustomToast
import com.google.android.material.button.MaterialButton
import androidx.core.graphics.drawable.toDrawable

open class BaseActivity : AppCompatActivity() {

    protected lateinit var sessionManager : SessionManager
    protected lateinit var dbHelper : DatabaseHelper
    protected lateinit var usersRepo: UsersRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)
        usersRepo = UsersRepo(dbHelper)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // calls overridden finish()
            }
        })
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun startActivity(intent: Intent, options: Bundle?) {
        super.startActivity(intent, options)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    protected fun showPopupMenu(view: View) {

        // Allow only manager to have access to add user menu
        when (sessionManager.getRole()) {
            "Manager" -> {
                val popup = PopupMenu(this, view)
                popup.menuInflater.inflate(R.menu.hamburger_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {

                        R.id.add_worker -> {

                            val dialogView = layoutInflater.inflate(R.layout.dialog_add_worker, null)

                            val etName = dialogView.findViewById<EditText>(R.id.et_worker_name)
                            val etPassword = dialogView.findViewById<EditText>(R.id.et_worker_password)
                            val btnAdd = dialogView.findViewById<MaterialButton>(R.id.btn_add_worker)

                            val dialog = AlertDialog.Builder(this)
                            .setView(dialogView)
                            .setCancelable(true)
                            .create()
                            dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                            dialog.show()

                            btnAdd.setOnClickListener {

                                val name = etName.text.toString().trim()
                                val password = etPassword.text.toString().trim()

                                if (name.isEmpty() || password.isEmpty()) {
                                    showCustomToast(this, "Please fill all fields")
                                    return@setOnClickListener
                                }

                                val success = usersRepo.assignWorker(name, password)
                                if (success) {
                                    showCustomToast(this, "Worker added successfully")
                                    dialog.dismiss()
                                } else {
                                    showCustomToast(this, "Failed to add worker")
                                }
                            }
                        }

                        R.id.log_out -> showLogoutDialog()
                        else -> return@setOnMenuItemClickListener false
                    }

                    true
                }
                popup.show()
            }

            "Worker" -> {
                val popup = PopupMenu(this, view)
                popup.menuInflater.inflate(R.menu.worker_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.log_out -> showLogoutDialog()
                        else -> false
                    }
                    true
                }
                popup.show()
            }
        }
    }
    protected fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                showCustomToast(this, "Logged out successfully")
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

}
