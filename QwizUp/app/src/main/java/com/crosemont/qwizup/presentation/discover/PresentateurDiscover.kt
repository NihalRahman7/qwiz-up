package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz

class PresentateurDiscover(
    var modeleQuiz: ModeleQuiz = ModeleQuiz(),
) : ContratVuePresentateur.IPresentateurDiscover {

    private var vue: ContratVuePresentateur.IVueDiscover? = null

    // Filtres choisis par l'utilisateur
    private var categorieChoisie: CategorieQuiz? = null   // null = « Tous »
    private var recherche = ""

    override fun attacherVue(vue: ContratVuePresentateur.IVueDiscover) {
        this.vue = vue
        vue.afficherCategorieChoisie(categorieChoisie)
        afficherQuiz()
    }

    override fun detacherVue() {
        vue = null
    }

    override fun surRechercheModifiee(texte: String) {
        recherche = texte
        afficherQuiz()
    }

    override fun surCategorieCliquee(categorie: CategorieQuiz?) {
        categorieChoisie = categorie
        vue?.afficherCategorieChoisie(categorie)
        afficherQuiz()
    }

    override fun surFavoriClique(quizId: Int) {
        modeleQuiz.basculerFavoriDiscover(quizId)
        afficherQuiz()
    }

    override fun surJouerClique(quizId: Int) {
        val quiz = modeleQuiz.obtenirQuizDiscoverParId(quizId) ?: return

        // Seuls les quiz qui ont des questions peuvent être joués
        if (modeleQuiz.obtenirQuizAJouer(quizId) != null) {
            vue?.ouvrirQuiz(quizId, quiz.titre)
        } else {
            vue?.afficherQuizBientot(quiz.titre)
        }
    }

    // Demande au modèle les quiz qui correspondent aux filtres, puis les envoie à la vue
    private fun afficherQuiz() {
        val quiz = modeleQuiz.obtenirQuizDiscover(categorieChoisie, recherche)

        vue?.viderCartes()
        if (quiz.isEmpty()) {
            vue?.afficherAucunResultat()
        } else {
            quiz.forEach {
                vue?.ajouterCarteQuiz(
                    it.id, it.categorie, it.titre, it.description,
                    it.nbQuestions, it.dureeMinutes, it.estFavori
                )
            }
        }
    }
}
