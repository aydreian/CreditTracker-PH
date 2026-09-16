package com.example.credittrackph.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.credittrackph.data.db.dao.ProfileDao
import com.example.credittrackph.data.db.entity.ProfileEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _allProfiles = MutableStateFlow<List<ProfileEntity>>(emptyList())
    val allProfiles: StateFlow<List<ProfileEntity>> = _allProfiles.asStateFlow()

    private val _mainUser = MutableStateFlow<ProfileEntity?>(null)
    val mainUser: StateFlow<ProfileEntity?> = _mainUser.asStateFlow()
    
    // We can use this to block the UI if the main user is not set up
    private val _isMainUserSetupRequired = MutableStateFlow(false)
    val isMainUserSetupRequired: StateFlow<Boolean> = _isMainUserSetupRequired.asStateFlow()

    init {
        viewModelScope.launch {
            profileDao.getAllProfiles().collect { profiles ->
                _allProfiles.value = profiles
                val main = profiles.find { it.isMainUser }
                _mainUser.value = main
                _isMainUserSetupRequired.value = main == null
            }
        }
    }

    fun setMainUser(name: String) {
        viewModelScope.launch {
            if (_mainUser.value == null) {
                val profile = ProfileEntity(name = name, isMainUser = true)
                profileDao.insertProfile(profile)
            }
        }
    }

    fun addFamilyMember(name: String) {
        viewModelScope.launch {
            val profile = ProfileEntity(name = name, isMainUser = false)
            profileDao.insertProfile(profile)
        }
    }
}
