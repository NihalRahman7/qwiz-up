package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PresentateurDiscover(
    var modeleQuiz: ModeleQuiz = ModeleQuiz(),
) : ContratVuePresentateur.IPresentateurDiscover {

    private var vue: ContratVuePresentateur.IVueDiscover? = null
    private var scope: CoroutineScope? = null

    // Filtres choisis par l'utilisateur
    private var categorieChoisie: CategorieQuiz? = null   // null = « Tous »
    private var recherche = ""

    override fun attacherVue(vue: ContratVuePresentateur.IVueDiscover) {
        this.vue = vue
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        charger()
    }

    override fun detacherVue() {
        scope?.cancel()
        scope = null
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

    // Cherche les quiz et les envoie à l'écran
    private fun charger() {
        scope?.launch {
            vue?.afficherChargement(true)
            try {
                modeleQuiz.chargerQuizDiscover()
                afficherQuiz()
            } catch (e: ServiceException) {
                vue?.afficherErreur(e.message ?: "Erreur inconnue")
            } finally {
                vue?.afficherChargement(false)
            }
        }
    }
}
