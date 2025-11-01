package com.tvbrowser.mobile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tvbrowser.mobile.data.repository.HistoryRepository
import com.tvbrowser.mobile.data.repository.PairedTVRepository
import com.tvbrowser.mobile.data.repository.TVConnectionRepository

/**
 * Factory for creating ViewModels with repository dependencies
 */
class EnhancedViewModelFactory(
    private val context: Context,
    private val historyRepository: HistoryRepository,
    private val pairedTVRepository: PairedTVRepository,
    private val tvConnectionRepository: TVConnectionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EnhancedMobileViewModel::class.java) -> {
                EnhancedMobileViewModel(
                    context = context,
                    historyRepository = historyRepository,
                    pairedTVRepository = pairedTVRepository,
                    tvConnectionRepository = tvConnectionRepository
                ) as T
            }
            modelClass.isAssignableFrom(MobileViewModel::class.java) -> {
                MobileViewModel(context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
