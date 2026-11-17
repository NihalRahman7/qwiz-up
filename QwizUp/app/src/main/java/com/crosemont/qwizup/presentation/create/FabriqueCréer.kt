package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.domaine.service.ServiceCréation
import com.crosemont.qwizup.sourceDeDonnees.SourceQuizCréés
import com.crosemont.qwizup.sourceDeDonnees.SourceQuizCréésBidon

/**
 * Assemble les morceaux de Créer : source → service → modèle → présentateur.
 * Chaque morceau reçoit le suivant de l'extérieur (injection de dépendance).
 * Pour changer de source (fichier, API), on change seulement la ligne de la source.
 */
object FabriqueCréer {

    // Une seule source pour l'écran Créer et l'éditeur : ils voient les mêmes quiz
    private val source: SourceQuizCréés by lazy { SourceQuizCréésBidon() }
    private val modèle: ContratCréer.IModèleCréer by lazy { ModèleCréer(ServiceCréation(source)) }

    fun créerPrésentateurCréer(): ContratCréer.IPrésentateurCréer = PrésentateurCréer(modèle)

    fun créerPrésentateurÉditeur(): ContratCréer.IPrésentateurÉditeur = PrésentateurÉditeur(modèle)
}
