package com.crosemont.qwizup.domaine.entites

data class Quiz(
    val titre: String,
    val catégorie: String,
    val nbQuestions: Int,
    val description: String = ""
)
