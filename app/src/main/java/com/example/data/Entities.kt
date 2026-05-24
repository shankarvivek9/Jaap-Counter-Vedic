package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "mantras")
data class Mantra(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val deity: String,
    val translation: String,
    val isPreset: Boolean = false,
    val countSelected: Int = 0
) : Serializable

@Entity(tableName = "jaap_sessions")
data class JaapSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mantraName: String,
    val deity: String,
    val count: Int,
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val malaType: String = "Rudraksha",
    val completedMalas: Int = count / 108
) : Serializable
