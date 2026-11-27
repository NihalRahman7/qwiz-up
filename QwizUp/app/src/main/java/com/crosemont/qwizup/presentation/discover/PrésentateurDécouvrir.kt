package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz.CarteDiscover
import com.crosemont.qwizup.presentation.discover.ContratDécouvrir.MeilleurScore
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrésentateurDécouvrir(
    private val modèle: ContratDécouvrir.IModèleDécouvrir,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,   // pour mettre la vue à jour
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,   // pour appeler le modèle
) : ContratDécouvrir.IPrésentateurDécouvrir {

    private var vue: ContratDécouvrir.IVueDécouvrir? = null

    // Les coroutines du présentateur vivent tant que la vue est attachée
    private var coroutines: CoroutineScope? = null
    private var chargement: Job? = null

    // Filtres choisis par l'utilisateur
    private var catégorieChoisie: CatégorieQuiz? = null   // null = « Tous »
    private var seulementFavoris = false
    private var recherche = ""

    // Les quiz affichés en ce moment
    private var quizAffichés: List<CarteDiscover> = emptyList()

    override fun attacherVue(vue: ContratDécouvrir.IVueDécouvrir) {
        this.vue = vue
        coroutines = CoroutineScope(filPrincipal + Job())
        afficherFiltre()
        afficherQuiz()
    }

    override fun detacherVue() {
        coroutines?.cancel()   // arrête le travail en cours : la vue n'existe plus
        coroutines = null
        vue = null
    }

    override fun rechercher(texte: String) {
        recherche = texte
        afficherQuiz()
    }

    override fun choisirCatégorie(catégorie: CatégorieQuiz?) {
        catégorieChoisie = catégorie
        seulementFavoris = false
        afficherFiltre()
        afficherQuiz()
    }

    override fun choisirFavoris() {
        catégorieChoisie = null
        seulementFavoris = true
        afficherFiltre()
        afficherQuiz()
    }

    override fun basculerFavori(quizId: Int) {
        val quiz = quizAffichés.find { it.id == quizId } ?: return
        lancer {
            withContext(filArrièrePlan) { modèle.basculerFavori(quiz.titre) }
            afficherQuiz()
        }
    }

    override fun jouer(quizId: Int) {
        val quiz = quizAffichés.find { it.id == quizId } ?: return
        lancer {
            // Seuls les quiz qui ont des questions peuvent être joués
            val jouable = withContext(filArrièrePlan) { modèle.estJouable(quizId) }
            if (jouable) {
                vue?.ouvrirQuiz(quizId, quiz.titre)
            } else {
                vue?.afficherQuizBientôt(quiz.titre)
            }
        }
    }

    // Une carte a été touchée : on ouvre son détail
    override fun ouvrirDétail(quizId: Int) {
        vue?.ouvrirDétail(quizId)
    }

    private fun afficherFiltre() {
        if (seulementFavoris) vue?.afficherFavorisChoisis() else vue?.afficherCatégorieChoisie(catégorieChoisie)
    }

    // Demande au modèle les quiz qui correspondent aux filtres, puis les envoie à la vue
    private fun afficherQuiz() {
        chargement?.cancel()   // une nouvelle demande remplace celle qui est en cours
        chargement = lancer {
            vue?.afficherChargement(true)
            try {
                val (tous, scores) = withContext(filArrièrePlan) {
                    modèle.obtenirQuiz(catégorieChoisie, recherche) to meilleursScores()
                }
                val quiz = if (seulementFavoris) tous.filter { it.estFavori } else tous
                quizAffichés = quiz

                vue?.viderQuiz()
                if (quiz.isEmpty()) {
                    if (seulementFavoris) vue?.afficherAucunFavori() else vue?.afficherAucunRésultat()
                } else {
                    quiz.forEach {
                        vue?.ajouterQuiz(
                            it.id, it.catégorie, it.titre, it.description,
                            it.nbQuestions, it.dureeMinutes, it.estFavori, scores[it.id]
                        )
                    }
                }
            } finally {
                // Si une nouvelle recherche a remplacé celle-ci, c'est elle qui cachera l'indicateur
                if (currentCoroutineContext().isActive) vue?.afficherChargement(false)
            }
        }
    }

    // Les scores sont un bonus : s'ils ne peuvent pas être lus, on affiche quand même les quiz
    private suspend fun meilleursScores(): Map<Int, MeilleurScore> =
        try {
            modèle.obtenirMeilleursScores()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyMap()
        }

    // Lance une coroutine sur le fil principal. Si le modèle échoue, la vue affiche une erreur.
    private fun lancer(travail: suspend () -> Unit): Job? =
        coroutines?.launch {
            try {
                travail()
            } catch (e: CancellationException) {
                throw e   // la coroutine a été annulée : ce n'est pas une erreur
            } catch (e: Exception) {
                vue?.afficherErreur()
            }
        }
}
