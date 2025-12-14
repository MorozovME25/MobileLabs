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
import com.example.lab1.network.CharacterRepository
import com.example.lab1.network.HomeViewModel
import com.example.lab1.network.SettingsViewModel
import com.example.lab1.network.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeFragment : BaseFragment() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("binding is null")

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(
            CharacterRepository.getInstance(requireContext()),
            AppDataStore.getInstance(requireContext()).dataStore
        )
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

        settingsViewModel = SettingsViewModel(AppDataStore.getInstance(requireContext()).dataStore)

        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
        setupToolbar()
    }

    private fun setupRecyclerView() {
        characterAdapter = CharacterAdapter(
            emptyList(),
            hasMoreData = true,
            isLoadingMore = false
        ) {
            viewModel.loadMoreCharacters()
        }
        binding.recycle.adapter = characterAdapter
        binding.recycle.layoutManager = LinearLayoutManager(context)

    }

    private fun setupObservers() {
        // Наблюдаем за персонажами - теперь это реактивный поток
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.characters.collectLatest { characters ->
                    Timber.d("Получены все персонажи для отображения: ${characters.size}")
                    binding.tvEmpty.visibility = if (characters.isEmpty()) View.VISIBLE else View.GONE

                    // Обновляем ВСЕ данные в адаптере
                    characterAdapter.updateData(characters)

                    settingsViewModel.updateCharacters(characters)
                }
            }
        }

        // Наблюдаем за состоянием загрузки
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collectLatest { isLoading ->
                    binding.progressBar.visibility = if (isLoading && !viewModel.isInitialLoading.value) View.VISIBLE else View.GONE
                    binding.swipeRefresh.isRefreshing = isLoading && !viewModel.isInitialLoading.value

                    // Обновляем состояние футера (загрузка новых данных из API)
                    if (!isLoading) {
                        characterAdapter.updateLoadingState(
                            isLoading = isLoading,
                            hasMore = viewModel.hasMoreData.value ?: true
                        )
                    }
                }
            }
        }

        // Наблюдаем за состоянием hasMoreData
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hasMoreData.collectLatest { hasMore ->
                    Timber.d("Состояние hasMoreData для загрузки из API: $hasMore")
                    characterAdapter.updateLoadingState(
                        isLoading = viewModel.isLoading.value,
                        hasMore = hasMore
                    )
                }
            }
        }

        // Наблюдаем за ошибками
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collectLatest { error ->
                    if (!error.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Наблюдаем за начальной загрузкой
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isInitialLoading.collectLatest { isInitialLoading ->
                    binding.progressBar.visibility = if (isInitialLoading) View.VISIBLE else View.GONE
                    if (!isInitialLoading) {
                        characterAdapter.updateLoadingState(
                            isLoading = viewModel.isLoading.value,
                            hasMore = viewModel.hasMoreData.value ?: true
                        )
                    }
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshCharacters()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}