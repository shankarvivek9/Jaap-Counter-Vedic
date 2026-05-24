package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MantraDao {
    @Query("SELECT * FROM mantras ORDER BY id ASC")
    fun getAllMantras(): Flow<List<Mantra>>

    @Query("SELECT * FROM mantras WHERE id = :id")
    suspend fun getMantraById(id: Int): Mantra?

    @Query("SELECT * FROM mantras WHERE name = :name LIMIT 1")
    suspend fun getMantraByName(name: String): Mantra?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMantra(mantra: Mantra)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(mantras: List<Mantra>)

    @Update
    suspend fun updateMantra(mantra: Mantra)

    @Query("UPDATE mantras SET countSelected = countSelected + 1 WHERE name = :name")
    suspend fun incrementMantraSelection(name: String)

    @Delete
    suspend fun deleteMantra(mantra: Mantra)
}

@Dao
interface JaapSessionDao {
    @Query("SELECT * FROM jaap_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<JaapSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: JaapSession)

    @Query("DELETE FROM jaap_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("SELECT SUM(count) FROM jaap_sessions")
    fun getTotalJaapCount(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM jaap_sessions")
    fun getTotalSessionsCount(): Flow<Int?>

    @Query("SELECT SUM(durationSeconds) FROM jaap_sessions")
    fun getTotalDurationSeconds(): Flow<Long?>
}
