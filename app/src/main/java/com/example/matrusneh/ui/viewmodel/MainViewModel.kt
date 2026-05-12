package com.example.matrusneh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.matrusneh.data.local.UserEntity
import com.example.matrusneh.data.local.WaterEntity
import com.example.matrusneh.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.matrusneh.worker.ReminderWorker

data class HealthAlert(
    val id: String,
    val title: String,
    val message: String,
    val emoji: String,
    val isDanger: Boolean = false
)

enum class ReminderPeriod { MORNING, AFTERNOON, NIGHT, NONE }

data class SmartReminder(
    val message: String,
    val icon: String,
    val period: ReminderPeriod
)

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    val user: StateFlow<UserEntity?> = repository.user.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val totalKicks: StateFlow<Int> = repository.totalKicks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    // Flows for today's data
    val kicksToday: StateFlow<Int> = flow {
        while(true) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis - 1
            
            emitAll(repository.getKicksForToday(startOfDay, endOfDay))
            delay(60000)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val waterToday: StateFlow<WaterEntity?> = flow {
        while(true) {
            val today = LocalDate.now().toString()
            emitAll(repository.getWaterForDate(today))
            delay(60000)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val healthAlerts: StateFlow<List<HealthAlert>> = combine(
        kicksToday,
        waterToday,
        user
    ) { kicks, water, user ->
        if (user?.healthAlertsEnabled == false) {
            return@combine emptyList()
        }
        
        val alerts = mutableListOf<HealthAlert>()
        
        // 1. Danger Alerts (Priority)
        val waterCount = water?.count ?: 0
        if (waterCount < 4) {
            alerts.add(HealthAlert("water_danger", "Hydration Warning", "⚠ Low water intake today. Please drink more water.", "💧", true))
        }
        
        if (kicks < 5) {
            alerts.add(HealthAlert("kick_danger", "Activity Warning", "⚠ Reduced baby kick activity detected.", "👶", true))
        }
        
        // Mock Danger Alerts for demonstration
        alerts.add(HealthAlert("bp_danger", "BP Warning", "⚠ High blood pressure detected. Please consult a doctor.", "🩺", true))
        alerts.add(HealthAlert("med_danger", "Medication", "⚠ Missed medication reminder.", "💊", true))

        // 2. Normal Health Alerts
        alerts.add(HealthAlert("water_normal", "Hydration", "Drink enough water today 💧", "🥤"))
        alerts.add(HealthAlert("rest_normal", "Rest", "Take proper rest 🤍", "😴"))
        alerts.add(HealthAlert("vitamins_normal", "Vitamins", "Time for your pregnancy vitamins 💊", "💊"))
        alerts.add(HealthAlert("walk_normal", "Exercise", "Go for a short walk 🚶‍♀️", "🚶‍♀️"))
        
        alerts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val smartReminder: StateFlow<SmartReminder?> = flow {
        while (true) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isEvenMinute = Calendar.getInstance().get(Calendar.MINUTE) % 2 == 0
            
            val reminder = when (hour) {
                in 5..11 -> SmartReminder(
                    message = if (isEvenMinute) "💧 Good Morning Amma! Drink enough water today." else "🌞 Start your day with healthy hydration.",
                    icon = "💧",
                    period = ReminderPeriod.MORNING
                )
                in 12..17 -> SmartReminder(
                    message = if (isEvenMinute) "🥗 Time for a healthy and nutritious meal." else "🍎 Don’t skip your afternoon nutrition.",
                    icon = "🥗",
                    period = ReminderPeriod.AFTERNOON
                )
                in 18..22 -> SmartReminder(
                    message = if (isEvenMinute) "😴 Take proper rest for a healthy pregnancy." else "🌙 Time to relax and sleep well.",
                    icon = "😴",
                    period = ReminderPeriod.NIGHT
                )
                else -> null
            }
            emit(reminder)
            delay(60000) // Update every minute
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun getKicksForToday(): StateFlow<Int> = kicksToday

    fun saveUser(name: String, email: String) {
        viewModelScope.launch {
            repository.insertUser(UserEntity(name = name, email = email))
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            repository.updateLanguage(languageCode)
        }
    }

    fun setHealthAlertsPreference(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHealthAlertsPreference(enabled)
        }
    }

    fun setReminderPreference(enabled: Boolean, workManager: WorkManager? = null) {
        viewModelScope.launch {
            repository.updateReminderPreference(enabled)
            
            workManager?.let { wm ->
                if (enabled) {
                    val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                        1, TimeUnit.HOURS
                    ).build()

                    wm.enqueueUniquePeriodicWork(
                        "DailyReminder",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        workRequest
                    )
                } else {
                    wm.cancelUniqueWork("DailyReminder")
                }
            }
        }
    }

    fun addKick() {
        viewModelScope.launch {
            repository.insertKick(System.currentTimeMillis())
        }
    }

    val isDarkMode: StateFlow<Boolean> = repository.user
        .map { it?.isDarkMode ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDarkMode(enabled)
        }
    }

    // Water tracking
    fun getWaterForToday(): StateFlow<WaterEntity?> {
        val today = java.time.LocalDate.now().toString()
        return repository.getWaterForDate(today).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun incrementWater() {
        viewModelScope.launch {
            val today = java.time.LocalDate.now().toString()
            repository.incrementWater(today)
        }
    }

    fun initializeWaterEntry() {
        viewModelScope.launch {
            val today = java.time.LocalDate.now().toString()
            repository.insertWater(WaterEntity(date = today, count = 0))
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearUserData()
        }
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
