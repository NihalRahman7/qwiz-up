package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.presentation.ContratVuePrésentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.OngletCreer
import com.crosemont.qwizup.presentation.ModèleQuiz

class PrésentateurCreate(
    var modèleQuiz: ModèleQuiz = ModèleQuiz(),
) : ContratVuePrésentateur.IPrésentateurCreate {

    private var vue: ContratVuePrésentateur.IVueCreate? = null
    private var ongletActif = OngletCreer.BROUILLONS

    override fun attacherVue(vue: ContratVuePrésentateur.IVueCreate) {
        this.vue = vue
        vue.afficherCompteurs(modèleQuiz.obtenirBrouillons().size, modèleQuiz.obtenirPubliés().size)
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
            OngletCreer.BROUILLONS -> modèleQuiz.obtenirBrouillons() to "Aucun brouillon pour l'instant"
            OngletCreer.PUBLIES -> modèleQuiz.obtenirPubliés() to "Aucun quiz publié pour l'instant"
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
