package com.voicebudget.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.model.CustomCategoryIcon
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.usecase.AddCustomCategoryUseCase
import com.voicebudget.domain.usecase.DeleteCustomCategoryUseCase
import com.voicebudget.domain.usecase.ObserveCustomCategoriesUseCase
import com.voicebudget.domain.usecase.UpdateCustomCategoryUseCase
import com.voicebudget.presentation.components.customCategoryColorPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CategoryDialogState {
    data object Closed : CategoryDialogState

    data class Editing(
        val id: Long? = null,
        val name: String = "",
        val type: TransactionType = TransactionType.EXPENSE,
        val icon: CustomCategoryIcon = CustomCategoryIcon.TAG,
        val color: Long = customCategoryColorPalette.first(),
        val showNameError: Boolean = false,
    ) : CategoryDialogState
}

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    observeCustomCategoriesUseCase: ObserveCustomCategoriesUseCase,
    private val addCustomCategoryUseCase: AddCustomCategoryUseCase,
    private val updateCustomCategoryUseCase: UpdateCustomCategoryUseCase,
    private val deleteCustomCategoryUseCase: DeleteCustomCategoryUseCase,
) : ViewModel() {

    val categories: StateFlow<List<CustomCategory>> = observeCustomCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _dialogState = MutableStateFlow<CategoryDialogState>(CategoryDialogState.Closed)
    val dialogState: StateFlow<CategoryDialogState> = _dialogState.asStateFlow()

    fun openAddDialog() {
        _dialogState.value = CategoryDialogState.Editing()
    }

    fun openEditDialog(category: CustomCategory) {
        _dialogState.value = CategoryDialogState.Editing(
            id = category.id,
            name = category.name,
            type = category.type,
            icon = category.icon,
            color = category.color,
        )
    }

    fun updateDraft(transform: (CategoryDialogState.Editing) -> CategoryDialogState.Editing) {
        val current = _dialogState.value
        if (current is CategoryDialogState.Editing) {
            _dialogState.value = transform(current)
        }
    }

    fun dismissDialog() {
        _dialogState.value = CategoryDialogState.Closed
    }

    fun confirmSave() {
        val state = _dialogState.value
        if (state !is CategoryDialogState.Editing) return
        val name = state.name.trim()
        if (name.isEmpty()) {
            _dialogState.value = state.copy(showNameError = true)
            return
        }
        val category = CustomCategory(
            id = state.id ?: 0,
            name = name,
            type = state.type,
            icon = state.icon,
            color = state.color,
        )
        viewModelScope.launch {
            if (state.id == null) addCustomCategoryUseCase(category) else updateCustomCategoryUseCase(category)
            _dialogState.value = CategoryDialogState.Closed
        }
    }

    fun delete(category: CustomCategory) {
        viewModelScope.launch { deleteCustomCategoryUseCase(category) }
    }
}
