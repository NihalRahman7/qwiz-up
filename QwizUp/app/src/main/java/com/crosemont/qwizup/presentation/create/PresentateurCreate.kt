package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.OngletCreer

class PresentateurCreate(
    var modeleQuiz: ModeleQuiz = ModeleQuiz(),
) : ContratVuePresentateur.IPresentateurCreate {

    private var vue: ContratVuePresentateur.IVueCreate? = null
    private var ongletActif = OngletCreer.BROUILLONS

    override fun attacherVue(vue: ContratVuePresentateur.IVueCreate) {
        this.vue = vue
        vue.afficherCompteurs(modeleQuiz.obtenirBrouillons().size, modeleQuiz.obtenirPublies().size)
        afficherOnglet()
    }

    override fun detacherVue() {
        vue = null
    }

    override fun surNouveauQuizClique() {
        vue?.ouvrirEditeur(null)
    }

    override fun surOngletClique(onglet: OngletCreer) {
        ongletActif = onglet
        afficherOnglet()
    }

    override fun surQuizClique(quizId: Int) {
        vue?.ouvrirEditeur(quizId)
    }

    private fun afficherOnglet() {
        val (cartes, messageVide) = when (ongletActif) {
            OngletCreer.BROUILLONS -> modeleQuiz.obtenirBrouillons() to "Aucun brouillon pour l'instant"
            OngletCreer.PUBLIES -> modeleQuiz.obtenirPublies() to "Aucun quiz publié pour l'instant"
        }

        vue?.afficherOngletChoisi(ongletActif)
        vue?.viderCartes()
        if (cartes.isEmpty()) {
            vue?.afficherMessageVide(messageVide)
        } else {
            cartes.forEach { vue?.ajouterCarteQuiz(it.id, it.titre, it.nbQuestions, it.estPublie) }
        }
    }
}
