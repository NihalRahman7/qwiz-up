package com.crosemont.qwizup.presentation.library

import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie

class PresentateurLibrairie(
        var modeleQuiz: ModeleQuiz = ModeleQuiz(),
    ): ContratLibrairie.IPresentateurLibrary {

        private var vue: ContratLibrairie.IVueLibrary? = null

        override fun surOngletClique(onglet: OngletLibrairie) {
            val (cartes, messageVide) = when (onglet) {
                OngletLibrairie.FAVORIS -> modeleQuiz.obternirFavoris() to "Aucun favori pour l’instant"
                OngletLibrairie.COMPLETES -> modeleQuiz.obternirCompletes() to "Aucun quiz complété"
                OngletLibrairie.CREES -> modeleQuiz.obternirCrees() to "Vous n'avez créé aucun quiz"
            }

            vue?.viderCartes()
            if (cartes.isEmpty()){
                vue?.afficherMessageVide(messageVide)
            } else {
                cartes.forEach {
                    vue?.ajouterCarteQuiz(it.id, it.titre, it.sousTitre, it.badge, it.typeBadge)
                }
            }
        }

        override fun surQuizClique(quizId: Int, titre: String) {
            vue?.ouvrirDetails(quizId, titre)
        }

        override fun attacherVue(vue: ContratLibrairie.IVueLibrary) {
            this.vue = vue
            surOngletClique(OngletLibrairie.FAVORIS)
        }

        override fun detacherVue() {
            vue = null
        }
}