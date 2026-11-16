package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.presentation.home.ContratAccueil.IPresentateurAccueil
import com.crosemont.qwizup.presentation.home.ContratAccueil.IVueAccueil

// Présentateur de l'accueil : reçoit les clics de la vue et décide quoi faire
class PresentateurAccueil : IPresentateurAccueil {

    // La vue attachée (null quand l'écran n'est pas affiché)
    private var vue: IVueAccueil? = null

    override fun attacherVue(vue: IVueAccueil) {
        this.vue = vue
    }

    override fun detacherVue() {
        vue = null
    }

    // Un quiz a été choisi : on demande à la vue d'ouvrir le jeu
    override fun surQuizClique(quizId: Int) {
        vue?.ouvrirQuiz(quizId)
    }

    // Le bouton « Code » a été cliqué : on ouvre l'écran Rejoindre
    override fun surCodeClique() {
        vue?.ouvrirRejoindre()
    }
}