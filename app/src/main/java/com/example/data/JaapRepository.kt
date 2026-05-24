package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class JaapRepository(private val database: AppDatabase) {
    private val mantraDao = database.mantraDao()
    private val jaapSessionDao = database.jaapSessionDao()

    val allMantras: Flow<List<Mantra>> = mantraDao.getAllMantras()
    val allSessions: Flow<List<JaapSession>> = jaapSessionDao.getAllSessions()
    
    val totalJaapCount: Flow<Int?> = jaapSessionDao.getTotalJaapCount()
    val totalSessionsCount: Flow<Int?> = jaapSessionDao.getTotalSessionsCount()
    val totalDurationSeconds: Flow<Long?> = jaapSessionDao.getTotalDurationSeconds()

    suspend fun checkAndSeedMantras() {
        val existing = mantraDao.getAllMantras().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val presets = listOf(
                Mantra(
                    name = "Om",
                    deity = "Cosmic Absolute (Brahman)",
                    translation = "The primordial sacred rumble vibration of the universe.",
                    isPreset = true
                ),
                Mantra(
                    name = "Om Namah Shivaya",
                    deity = "Lord Shiva",
                    translation = "I bow to the Divine Inner Self, the Auspicious Guardian.",
                    isPreset = true
                ),
                Mantra(
                    name = "Om Gam Ganapataye Namaha",
                    deity = "Lord Ganesha",
                    translation = "Salutations to the supreme lord of wisdom and remover of obstacles.",
                    isPreset = true
                ),
                Mantra(
                    name = "Gayatri Mantra",
                    deity = "Savitr (Sun Devta)",
                    translation = "We meditate on the brilliant solar light; may it illumine our intellect.",
                    isPreset = true
                ),
                Mantra(
                    name = "Hare Krishna Mahamantra",
                    deity = "Lord Krishna & Rama",
                    translation = "O cosmic source of all pleasure and divine energy, guide my soul.",
                    isPreset = true
                ),
                Mantra(
                    name = "Om Namo Narayanaya",
                    deity = "Lord Vishnu",
                    translation = "I bow to the supreme preserver of cosmic order and protector of worlds.",
                    isPreset = true
                ),
                Mantra(
                    name = "Om Dum Durgayei Namaha",
                    deity = "Maa Durga",
                    translation = "Salutations to the invincible mother goddess who protects from negativity.",
                    isPreset = true
                ),
                Mantra(
                    name = "Om Hanumate Namaha",
                    deity = "Lord Hanuman",
                    translation = "I bow to the master of pure devotion and cosmic protective strength.",
                    isPreset = true
                ),
                Mantra(
                    name = "Maha Mrityunjaya Mantra",
                    deity = "Lord Shiva (Rudra)",
                    translation = "We worship the three-eyed Lord; release us from mortality and grant liberation.",
                    isPreset = true
                )
            )
            mantraDao.insertAll(presets)
        }
    }

    suspend fun insertSession(session: JaapSession) {
        jaapSessionDao.insertSession(session)
    }

    suspend fun deleteSessionById(id: Int) {
        jaapSessionDao.deleteSessionById(id)
    }

    suspend fun insertMantra(mantra: Mantra) {
        mantraDao.insertMantra(mantra)
    }

    suspend fun deleteMantra(mantra: Mantra) {
        mantraDao.deleteMantra(mantra)
    }

    suspend fun incrementMantraSelection(name: String) {
        mantraDao.incrementMantraSelection(name)
    }
}
