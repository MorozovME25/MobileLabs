package com.example.lab1.Fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.lab1.Extentions.AppDataStore
import com.example.lab1.Extentions.BaseFragment
import com.example.lab1.R
import com.example.lab1.databinding.ActivitySettingsBinding
import com.example.lab1.network.SettingsViewModel
import com.example.lab1.network.SettingsViewModelFactory
import com.example.lab1.network.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@Suppress("DEPRECATION")
class SettingsFragment : BaseFragment() {

    private var _binding: ActivitySettingsBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("binding is null")

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(AppDataStore.getInstance(requireContext()).dataStore)
    }

    private lateinit var backupFileName: String
    private var isBackupFileExists = false
    private var isRestoreFileExists = false

    // Для выбора файла для восстановления
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedFileUri ->
            // Оборачиваем suspend функцию в корутину
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.restoreFromExternalFile(requireContext(), selectedFileUri)
                updateBackupStatus()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backupFileName = "got_characters_backup_17.txt"

        setupToolbar()
        setupObservers()
        setupClickListeners()
        loadInitialSettings()
        checkAndRequestStoragePermission()
        updateBackupStatus()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userName.collectLatest { name ->
                    binding.etUsername.setText(name)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isDarkThemeEnabled.collectLatest { isEnabled ->
                    binding.switchDarkTheme.isChecked = isEnabled
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.backupFileName.collectLatest { fileName ->
                    binding.etBackupFilename.setText(fileName)
                    backupFileName = fileName
                    updateBackupStatus()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showNotifications.collectLatest { show ->
                    binding.switchNotifications.isChecked = show
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fontSize.collectLatest { size ->
                    when (size) {
                        12f -> binding.rbSmall.isChecked = true
                        16f -> binding.rbMedium.isChecked = true
                        20f -> binding.rbLarge.isChecked = true
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.backupStatus.collectLatest { status ->
                    binding.tvBackupStatus.text = status
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.restoreStatus.collectLatest { status ->
                    binding.tvRestoreStatus.text = status
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

//        binding.btnCreateBackup.setOnClickListener {
//            createBackup()
//        }

        binding.btnDeleteBackup.setOnClickListener {
            deleteBackup()
        }

        binding.btnRestoreBackup.setOnClickListener {
            restoreBackup()
        }

        binding.switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkThemeEnabled(isChecked)
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
        }

        binding.radioGroupFontSize.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_small -> viewModel.setFontSize(12f)
                R.id.rb_medium -> viewModel.setFontSize(16f)
                R.id.rb_large -> viewModel.setFontSize(20f)
            }
        }

        binding.btnSettings.setOnClickListener {
            openAppSettings()
        }

        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadInitialSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            val context = requireContext()
            val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

            // Загружаем настройки из Shared Preferences
            viewModel.setFontSize(sharedPreferences.getFloat("font_size", 16f))
            viewModel.setNotificationsEnabled(sharedPreferences.getBoolean("notifications_enabled", true))

            binding.etBackupFilename.setText(backupFileName)
        }
    }

    private fun saveSettings() {
        val userName = binding.etUsername.text.toString().trim()
        val backupFileName = binding.etBackupFilename.text.toString().trim()

        if (userName.isEmpty()) {
            Toast.makeText(requireContext(), "Введите имя пользователя", Toast.LENGTH_SHORT).show()
            return
        }

        if (backupFileName.isEmpty()) {
            Toast.makeText(requireContext(), "Введите имя файла резервной копии", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.setUserName(userName)
        viewModel.setBackupFileName(backupFileName)

        // Сохраняем в Shared Preferences
        val sharedPreferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("notifications_enabled", binding.switchNotifications.isChecked)
            apply()
        }
    }

    private fun checkAndRequestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Разрешение на доступ к файлам")
                    .setMessage("Приложению необходим доступ к файлам для создания резервных копий")
                    .setPositiveButton("Разрешить") { _, _ ->
                        requestPermissions(
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            1
                        )
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        } else {
            updateBackupStatus()
        }
    }

    private fun createBackup() {
        viewLifecycleOwner.lifecycleScope.launch {
            val backupFileName = viewModel.getCurrentBackupFileName()

            // Проверяем, нет ли уже файла в каком-либо хранилище
            if (viewModel.isFileInExternalStorage.value || viewModel.isFileInInternalStorage.value) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Резервная копия уже существует")
                    .setMessage("Файл резервной копии уже существует в одном из хранилищ. Хотите создать новую копию, заменив существующую?")
                    .setPositiveButton("Да, заменить") { _, _ ->
                        createNewBackup(backupFileName)
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
                return@launch
            }

            createNewBackup(backupFileName)
        }
    }

    private fun createNewBackup(fileName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val characters = viewModel.characters.value

            if (characters.isNotEmpty()) {
                val result = viewModel.createBackupFile(requireContext(), characters, fileName)
                if (result) {
                    Toast.makeText(requireContext(), "Резервная копия создана успешно!", Toast.LENGTH_LONG).show()
                    updateBackupStatus()
                } else {
                    Toast.makeText(requireContext(), "Ошибка создания резервной копии", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Нет данных для создания резервной копии", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteBackup() {
        // Удаляем только если файл во внешнем хранилище
        if (!viewModel.isFileInExternalStorage.value) {
            Toast.makeText(requireContext(), "Нет файла для удаления во внешнем хранилище", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удаление резервной копии")
            .setMessage("Вы уверены, что хотите удалить резервную копию из внешнего хранилища? Файл будет перемещен во внутреннее хранилище для возможности восстановления.")
            .setPositiveButton("Удалить и переместить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val backupFileName = viewModel.getCurrentBackupFileName()
                    val result = viewModel.moveBackupToInternalStorage(requireContext(), backupFileName)
                    if (result) {
                        Toast.makeText(requireContext(), "Файл перемещен во внутреннее хранилище", Toast.LENGTH_LONG).show()
                        updateBackupStatus()
                    } else {
                        Toast.makeText(requireContext(), "Ошибка перемещения файла", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun restoreBackup() {
        // Восстанавливаем только если файл во внутреннем хранилище
        if (!viewModel.isFileInInternalStorage.value) {
            Toast.makeText(requireContext(), "Нет файла для восстановления во внутреннем хранилище", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Восстановление резервной копии")
            .setMessage("Вы уверены, что хотите восстановить резервную копию из внутреннего хранилища? Файл будет перемещен во внешнее хранилище и станет доступным для просмотра.")
            .setPositiveButton("Восстановить и переместить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val backupFileName = viewModel.getCurrentBackupFileName()
                    val result = viewModel.restoreBackupFromInternalStorage(requireContext(), backupFileName)
                    if (result) {
                        Toast.makeText(requireContext(), "Файл восстановлен во внешнее хранилище", Toast.LENGTH_LONG).show()
                        updateBackupStatus()
                    } else {
                        Toast.makeText(requireContext(), "Ошибка восстановления файла", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateBackupStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            val backupFileName = viewModel.getCurrentBackupFileName()
            val backupFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), backupFileName)
            val internalBackupFile = File(requireContext().filesDir, backupFileName)

            viewModel.updateBackupStatus(backupFile, internalBackupFile)
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etOldPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_old_password)
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_new_password)
        val etConfirmPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_confirm_password)

        AlertDialog.Builder(requireContext())
            .setTitle("Изменение пароля")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val oldPassword = etOldPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword.length < 6) {
                    Toast.makeText(requireContext(), "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Здесь можно добавить проверку старого пароля
                Toast.makeText(requireContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка открытия настроек приложения", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}