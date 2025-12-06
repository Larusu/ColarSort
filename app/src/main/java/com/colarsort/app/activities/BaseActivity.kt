package com.colarsort.app.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.colarsort.app.R
import com.colarsort.app.data.repository.UsersRepo
import com.colarsort.app.session.SessionManager
import com.colarsort.app.utils.UtilityHelper.showCustomToast
import com.google.android.material.button.MaterialButton
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.colarsort.app.adapters.UserAdapter
import com.colarsort.app.data.repository.RepositoryProvider
import com.colarsort.app.databinding.DialogViewWorkersBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
open class BaseActivity : AppCompatActivity() {

    protected lateinit var sessionManager : SessionManager
    protected lateinit var usersRepo: UsersRepo

    protected var onImagePicked: ((Uri?) -> Unit)? = null

    protected val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data

                // call child activity callback
                onImagePicked?.invoke(uri)

                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }

    protected fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        imagePickerLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        usersRepo = RepositoryProvider.usersRepo

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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      " +
            "which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      " +
            "contracts for common intents available in\n      " +
            "{@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      " +
            "testing, and allow receiving results in separate, testable classes independent from your\n      " +
            "activity. Use\n      " +
            "{@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      " +
            "passing in a {@link StartActivityForResult} object for the {@link ActivityResultContract}.")
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

    suspend fun <T> runIO(ioBlock: suspend () -> T): T {
        return withContext(Dispatchers.IO) { ioBlock() }
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

                                lifecycleScope.launch {
                                    if(usersRepo.isUsernameExists(name.lowercase()))
                                    {
                                        showCustomToast(this@BaseActivity, "Username already exists")
                                        return@launch
                                    }
                                    val success = usersRepo.assignWorker(name.lowercase(), password)
                                    if (success) {
                                        showCustomToast(this@BaseActivity, "Worker added successfully")
                                        dialog.dismiss()
                                    } else {
                                        showCustomToast(this@BaseActivity, "Failed to add worker")
                                    }
                                }
                            }
                        }

                        R.id.view_workers -> {

                            val dialogBinding = DialogViewWorkersBinding.inflate(layoutInflater)

                            lifecycleScope.launch{
                               val users = usersRepo.getAll()
                                // Setup RecyclerView
                                dialogBinding.rvViewWorkers.adapter = UserAdapter(ArrayList(users), usersRepo, lifecycleScope)
                                dialogBinding.rvViewWorkers.layoutManager =
                                    LinearLayoutManager(this@BaseActivity)

                                val dialog = AlertDialog.Builder(this@BaseActivity)
                                    .setView(dialogBinding.root)
                                    .setCancelable(true)
                                    .create()

                                dialog.window?.setBackgroundDrawable(
                                    Color.TRANSPARENT.toDrawable()
                                )

                                dialog.show()
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
