package com.example.lab1.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab1.Extentions.AppDataStore
import com.example.lab1.Extentions.BaseFragment
import com.example.lab1.Extentions.CharacterAdapter
import com.example.lab1.R
import com.example.lab1.databinding.ActivityHomeBinding
import com.example.lab1.network.HomeViewModel
import com.example.lab1.network.SettingsViewModel
import com.example.lab1.network.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("binding is null")

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(AppDataStore.getInstance(requireContext()).dataStore)
    }

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var characterAdapter: CharacterAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация SettingsViewModel с общим DataStore
        settingsViewModel = SettingsViewModel(AppDataStore.getInstance(requireContext()).dataStore)

        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
        setupToolbar()
    }

    private fun setupRecyclerView() {
        characterAdapter = CharacterAdapter(emptyList())
        binding.recycle.adapter = characterAdapter
        binding.recycle.layoutManager = LinearLayoutManager(context)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.characters.collectLatest { characters ->
                    binding.tvEmpty.visibility =
                        if (characters.isEmpty()) View.VISIBLE else View.GONE
                    characterAdapter.updateData(characters)

                    // Передаем данные для резервного копирования
                    settingsViewModel.updateCharacters(characters)

                    // Создаем backup ТОЛЬКО если файл не существует ни в одном из хранилищ
                    if (!settingsViewModel.isFileInExternalStorage.value && !settingsViewModel.isFileInInternalStorage.value) {
                        createAutomaticBackup(characters)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collectLatest { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.swipeRefresh.isRefreshing = isLoading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collectLatest { error ->
                    if (!error.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.retryLoad()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_settings -> {
                    findNavController().navigate(R.id.action_home_to_settings)
                    true
                }
                else -> false
            }
        }
    }

    private fun createAutomaticBackup(characters: List<com.example.lab1.Extentions.Character>) {
        if (characters.isNotEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch {
                // Получаем имя файла из настроек
                val backupFileName = settingsViewModel.getCurrentBackupFileName()

                // Создаем backup только если файла нет ни в одном хранилище
                if (!settingsViewModel.backupFileExists(requireContext(), backupFileName) &&
                    !settingsViewModel.internalBackupFileExists(requireContext(), backupFileName)) {

                    val result = settingsViewModel.createBackupFile(requireContext(), characters, backupFileName)

                    if (result) {
                        // Показываем уведомление только при первом создании
                        Toast.makeText(
                            requireContext(),
                            "Резервная копия создана: $backupFileName",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")
//
//    private val viewModel: HomeViewModel by viewModels {
//        ViewModelFactory(requireContext().dataStore)
//    }
//
//    private lateinit var characterAdapter: CharacterAdapter
//    private lateinit var settingsViewModel: SettingsViewModel
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = ActivityHomeBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Инициализация SettingsViewModel
//        settingsViewModel = SettingsViewModel(requireContext().dataStore)
//
//        setupRecyclerView()
//        setupObservers()
//        setupSwipeRefresh()
//        setupToolbar()
//
//    }
//
//    private fun setupRecyclerView() {
//        characterAdapter = CharacterAdapter(emptyList())
//        binding.recycle.adapter = characterAdapter
//        binding.recycle.layoutManager = LinearLayoutManager(context)
//    }
//
//    private fun setupObservers() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.characters.collectLatest { characters ->
//                    binding.tvEmpty.visibility = if (characters.isEmpty()) View.VISIBLE else View.GONE
//                    characterAdapter.updateData(characters)
//
//                    settingsViewModel.updateCharacters(characters)
//                }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.isLoading.collectLatest { isLoading ->
//                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//                    binding.swipeRefresh.isRefreshing = isLoading
//                }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.error.collectLatest { error ->
//                    if (!error.isNullOrEmpty()) {
//                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        }
//    }
//
//    private fun setupSwipeRefresh() {
//        binding.swipeRefresh.setOnRefreshListener {
//            viewModel.retryLoad()
//        }
//    }
//
//    private fun setupToolbar() {
//        binding.toolbar.setOnMenuItemClickListener { item ->
//            when (item.itemId) {
//                R.id.menu_settings -> {
//                    findNavController().navigate(R.id.action_home_to_settings)
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

//    private val viewModel: HomeViewModel by viewModels { ViewModelFactory() }
//
//    private lateinit var characterAdapter: CharacterAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = ActivityHomeBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupRecyclerView()
//        setupObservers()
//        setupSwipeRefresh()
//    }
//
//    private fun setupRecyclerView() {
//        characterAdapter = CharacterAdapter(emptyList())
//        binding.recycle.adapter = characterAdapter
//        binding.recycle.layoutManager = LinearLayoutManager(context)
//    }
//
//    private fun setupObservers() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.characters.collectLatest { characters ->
//                    binding.tvEmpty.visibility =
//                        if (characters.isEmpty()) View.VISIBLE else View.GONE
//                    characterAdapter.updateData(characters)
//                }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.isLoading.collectLatest { isLoading ->
//                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//                    binding.swipeRefresh.isRefreshing = isLoading
//                }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.error.collectLatest { error ->
//                    if (!error.isNullOrEmpty()) {
//                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        }
//    }
//
//    private fun setupSwipeRefresh() {
//        binding.swipeRefresh.setOnRefreshListener {
//            viewModel.retryLoad()
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}