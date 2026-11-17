package com.crosemont.qwizup.domaine.entites

data class Quiz(
    val id: Int,             // Numéro interne, utilisé par la présentation
    val quizIdApi: String,   // Identifiant de l'API
    val titre: String,
    val categorie: String,
    val nbQuestions: Int,
    val description: String = ""
)
