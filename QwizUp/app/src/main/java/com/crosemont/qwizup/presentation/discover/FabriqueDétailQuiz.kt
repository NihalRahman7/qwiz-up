package com.crosemont.qwizup.presentation.discover

import java.io.File

/**
 * Assemble l'écran Détail du quiz : il utilise le même modèle que Découvrir.
 */
object FabriqueDétailQuiz {

    fun créerPrésentateur(dossierDeLApp: File): ContratDétailQuiz.IPrésentateurDétailQuiz =
        PrésentateurDétailQuiz(FabriqueDécouvrir.créerModèle(dossierDeLApp))
}