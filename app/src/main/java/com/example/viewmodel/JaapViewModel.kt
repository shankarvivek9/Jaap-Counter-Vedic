package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.OmSoundGenerator
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JaapViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = JaapRepository(database)

    // Sound generator
    private val omSoundGenerator = OmSoundGenerator()

    // Database Flows
    val mantras: StateFlow<List<Mantra>> = repository.allMantras
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<JaapSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalJaaps: StateFlow<Int> = MutableStateFlow(0).apply {
        viewModelScope.launch {
            repository.totalJaapCount.collect { value = it ?: 0 }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSessions: StateFlow<Int> = MutableStateFlow(0).apply {
        viewModelScope.launch {
            repository.totalSessionsCount.collect { value = it ?: 0 }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDurationSeconds: StateFlow<Long> = MutableStateFlow(0L).apply {
        viewModelScope.launch {
            repository.totalDurationSeconds.collect { value = it ?: 0L }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // UI Interactive States
    private val _selectedMantra = MutableStateFlow<Mantra?>(null)
    val selectedMantra = _selectedMantra.asStateFlow()

    private val _currentJaapCount = MutableStateFlow(0)
    val currentJaapCount = _currentJaapCount.asStateFlow()

    private val _completedMalas = MutableStateFlow(0)
    val completedMalas = _completedMalas.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive = _isSessionActive.asStateFlow()

    private val _selectedMalaMaterial = MutableStateFlow("Rudraksha") // Rudraksha, Sandalwood, Tulsi, Kamal Gatta
    val selectedMalaMaterial = _selectedMalaMaterial.asStateFlow()

    private val _omSynthRunning = MutableStateFlow(false)
    val omSynthRunning = _omSynthRunning.asStateFlow()

    private val _autoPlayMode = MutableStateFlow(false)
    val autoPlayMode = _autoPlayMode.asStateFlow()

    private val _autoPlayIntervalMs = MutableStateFlow(4000L) // default 4 seconds per jaap
    val autoPlayIntervalMs = _autoPlayIntervalMs.asStateFlow()

    private val _showSaveSuccessDialog = MutableStateFlow(false)
    val showSaveSuccessDialog = _showSaveSuccessDialog.asStateFlow()

    private var timerJob: Job? = null
    private var autoPlayJob: Job? = null

    init {
        viewModelScope.launch {
            // Seed base Sanskrit pre-sets on first launch
            repository.checkAndSeedMantras()
            // Set the first preset mantra as default selected
            mantras.collect { list ->
                if (list.isNotEmpty() && _selectedMantra.value == null) {
                    _selectedMantra.value = list.first()
                }
            }
        }
    }

    fun selectMantra(mantra: Mantra) {
        viewModelScope.launch {
            repository.incrementMantraSelection(mantra.name)
            _selectedMantra.value = mantra
            // Reset counters for the fresh mantra session
            resetCurrentSessionCounters()
        }
    }

    fun selectMalaMaterial(material: String) {
        _selectedMalaMaterial.value = material
        triggerHaptic(60, 200)
    }

    fun setAutoPlayInterval(ms: Long) {
        _autoPlayIntervalMs.value = ms
        if (_autoPlayMode.value) {
            restartAutoPlay()
        }
    }

    fun incrementCount() {
        if (!_isSessionActive.value) {
            startSessionTimer()
        }

        val nextCount = _currentJaapCount.value + 1
        _currentJaapCount.value = nextCount

        // 108 chants completed = 1 Full Mala
        if (nextCount > 0 && nextCount % 108 == 0) {
            _completedMalas.value = nextCount / 108
            // Celebratory high haptics (triple sacred chime vibrating feedback)
            triggerMalaCompletionHaptic()
        } else {
            // Light wood/bead touch vibration feedback
            triggerSingleBeadHaptic()
        }
    }

    fun decrementCount() {
        if (_currentJaapCount.value > 0) {
            val nextCount = _currentJaapCount.value - 1
            _currentJaapCount.value = nextCount
            _completedMalas.value = nextCount / 108
            triggerHaptic(25, 100)
        }
    }

    fun resetCount() {
        _currentJaapCount.value = 0
        _completedMalas.value = 0
        triggerHaptic(80, 150)
    }

    fun toggleOmSynth() {
        val currentState = _omSynthRunning.value
        if (currentState) {
            omSoundGenerator.stop()
            _omSynthRunning.value = false
        } else {
            omSoundGenerator.start()
            _omSynthRunning.value = true
        }
    }

    fun toggleAutoPlay() {
        val currentState = _autoPlayMode.value
        if (currentState) {
            stopAutoPlay()
        } else {
            startAutoPlay()
        }
    }

    private fun startAutoPlay() {
        _autoPlayMode.value = true
        if (!_isSessionActive.value) {
            startSessionTimer()
        }
        restartAutoPlay()
    }

    private fun stopAutoPlay() {
        _autoPlayMode.value = false
        autoPlayJob?.cancel()
        autoPlayJob = null
    }

    private fun restartAutoPlay() {
        autoPlayJob?.cancel()
        autoPlayJob = viewModelScope.launch {
            while (_autoPlayMode.value) {
                delay(_autoPlayIntervalMs.value)
                incrementCount()
            }
        }
    }

    private fun startSessionTimer() {
        _isSessionActive.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isSessionActive.value) {
                delay(1000)
                _elapsedSeconds.value = _elapsedSeconds.value + 1
            }
        }
    }

    fun pauseSession() {
        _isSessionActive.value = false
        _autoPlayMode.value = false
        timerJob?.cancel()
        autoPlayJob?.cancel()
        timerJob = null
        autoPlayJob = null
    }

    fun saveCurrentSession() {
        val count = _currentJaapCount.value
        if (count == 0) return

        val mName = _selectedMantra.value?.name ?: "Om"
        val mDeity = _selectedMantra.value?.deity ?: "Brahman"
        val duration = _elapsedSeconds.value
        val material = _selectedMalaMaterial.value

        viewModelScope.launch {
            val session = JaapSession(
                mantraName = mName,
                deity = mDeity,
                count = count,
                durationSeconds = duration,
                malaType = material,
                completedMalas = count / 108
            )
            repository.insertSession(session)

            // Show success confirmation card
            _showSaveSuccessDialog.value = true
            
            // Turn off audio synthesizer and play timers once done
            pauseSession()
            omSoundGenerator.stop()
            _omSynthRunning.value = false
            
            // Keep the record window open for visual aesthetics, let user clear it
        }
    }

    fun closeSuccessDialog() {
        _showSaveSuccessDialog.value = false
        resetCurrentSessionCounters()
    }

    fun resetCurrentSessionCounters() {
        _currentJaapCount.value = 0
        _completedMalas.value = 0
        _elapsedSeconds.value = 0L
        _isSessionActive.value = false
        _autoPlayMode.value = false
        autoPlayJob?.cancel()
        timerJob?.cancel()
        autoPlayJob = null
        timerJob = null
    }

    fun addCustomMantra(name: String, deity: String, translation: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newMantra = Mantra(
                name = name.trim(),
                deity = deity.ifBlank { "Sadhaka Heart" }.trim(),
                translation = translation.ifBlank { "Devotional intention of the sacred mantra." }.trim(),
                isPreset = false
            )
            repository.insertMantra(newMantra)
            _selectedMantra.value = newMantra
            resetCurrentSessionCounters()
        }
    }

    fun deleteMantra(mantra: Mantra) {
        if (mantra.isPreset) return // preset models cannot be deleted
        viewModelScope.launch {
            repository.deleteMantra(mantra)
            if (_selectedMantra.value?.name == mantra.name) {
                // select a preset default
                val currentList = mantras.value
                val fallback = currentList.firstOrNull { it.isPreset }
                if (fallback != null) {
                    selectMantra(fallback)
                }
            }
        }
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            repository.deleteSessionById(sessionId)
        }
    }

    // --- IMMERSIVE SPIRITUAL HAPTIC VIBRATION PROFILES ---

    private fun triggerSingleBeadHaptic() {
        // Pure sandlewood / rudraksha physical click simulation: light, fast 22ms tap
        triggerHaptic(22, 110)
    }

    private fun triggerMalaCompletionHaptic() {
        // High vibration celebration representing Cosmic Bell: Ring-Ring pattern (three elegant long-pulses)
        viewModelScope.launch {
            triggerHaptic(180, 255)
            delay(280)
            triggerHaptic(180, 255)
            delay(280)
            triggerHaptic(300, 255)
        }
    }

    private fun triggerHaptic(durationMs: Long, amplitude: Int) {
        val context = getApplication<Application>()
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val wave = VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                vibrator?.vibrate(wave)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.d("JaapViewModel", "Haptic vibrator not triggered: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Release synthesis sound engine
        omSoundGenerator.stop()
    }
}

class JaapViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JaapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JaapViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
