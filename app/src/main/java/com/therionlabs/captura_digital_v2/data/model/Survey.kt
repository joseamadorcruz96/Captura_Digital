package com.therionlabs.captura_digital_v2.data.model

data class Survey(
    val id: Int,
    val captadorId: Int,
    val gender: String,
    val age: Int,
    val interests: List<String>
)