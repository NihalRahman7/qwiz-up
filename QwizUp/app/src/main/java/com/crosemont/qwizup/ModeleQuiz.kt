package com.crosemont.qwizup
import com.crosemont.qwizup.presentation.ContratVuePresentateur

class ModeleQuiz {
    // Home
    // TODO: Faire le modèle avec des données codées en dur pour le fragment Home

    // Discover
    // TODO: Faire le modèle avec des données codées en dur pour le fragment Discover

    // Create
    // TODO: Faire le modèle avec des données codées en dur pour le fragment Create

    // Library
    data class CarteLibrary(
        val id: Int,
        val titre: String,
        val sousTitre: String,
        val badge: String?,
        val typeBadge: ContratVuePresentateur.TypeBadge
    )

    // Données codées en dur
    private val favoris = listOf(
        CarteLibrary(1, "Le système solaire", "Science - 10 questions", "4/5", ContratVuePresentateur.TypeBadge.PROGRESSION),
        CarteLibrary(2, "Capitales d'Europe", "Géographie - 12 questions", "Nouveau", ContratVuePresentateur.TypeBadge.NOUVEAU),
        CarteLibrary(3, "Verbe irréguliers anglais", "Langues - 15 questions", "87%", ContratVuePresentateur.TypeBadge.SCORE)
    )

    fun obternirFavoris(): List<CarteLibrary> = favoris
    fun obternirCompletes(): List<CarteLibrary> = emptyList()
    fun obternirCrees(): List<CarteLibrary> = emptyList()
}