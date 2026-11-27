package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.presentation.ModèleQuiz.CarteDiscover
import com.crosemont.qwizup.presentation.discover.ContratDétailQuiz.IPrésentateurDétailQuiz
import com.crosemont.qwizup.presentation.discover.ContratDétailQuiz.IVueDétailQuiz
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// Présentateur du détail : réutilise le modèle de Découvrir (mêmes quiz, favoris et scores)
class PrésentateurDétailQuiz(
    private val modèle: ContratDécouvrir.IModèleDécouvrir,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,     // pour mettre la vue à jour
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,     // pour appeler le modèle
) : IPrésentateurDétailQuiz {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueDétailQuiz? = null

    // Les coroutines du présentateur vivent tant que la vue est attachée
    private var coroutines: CoroutineScope? = null

    // Le quiz affiché (null tant qu'il n'est pas chargé)
    private var quiz: CarteDiscover? = null

    override fun attacherVue(vue: IVueDétailQuiz) {
        this.vue = vue
        coroutines = CoroutineScope(filPrincipal + Job())
    }

    override fun detacherVue() {
        coroutines?.cancel()
        coroutines = null
        vue = null
    }

    override fun chargerQuiz(quizId: Int) {
        lancer {
            val trouvé = withContext(filArrièrePlan) { modèle.obtenirQuiz(null, "") }
                .find { it.id == quizId }
            if (trouvé == null) {
                vue?.afficherErreur()
                return@lancer
            }
            quiz = trouvé
            vue?.afficherQuiz(trouvé.catégorie, trouvé.titre, trouvé.description, trouvé.nbQuestions, trouvé.dureeMinutes)
            vue?.afficherFavori(trouvé.estFavori)

            val meilleurScore = withContext(filArrièrePlan) { modèle.obtenirMeilleursScores() }[quizId]
            vue?.afficherMeilleurScore(meilleurScore)
        }
    }

    override fun surFavoriClique() {
        val actuel = quiz ?: return
        lancer {
            withContext(filArrièrePlan) { modèle.basculerFavori(actuel.titre) }
            val nouveau = actuel.copy(estFavori = !actuel.estFavori)
            quiz = nouveau
            vue?.afficherFavori(nouveau.estFavori)
        }
    }

    // Seuls les quiz qui ont des questions peuvent être joués
    override fun surJouerClique() {
        val actuel = quiz ?: return
        lancer {
            val jouable = withContext(filArrièrePlan) { modèle.estJouable(actuel.id) }
            if (jouable) vue?.ouvrirJeu(actuel.id) else vue?.afficherQuizBientôt(actuel.titre)
        }
    }

    override fun surRetourClique() {
        vue?.fermer()
    }

    // Lance une tâche ; si le modèle échoue, la vue affiche un message d'erreur
    private fun lancer(tâche: suspend () -> Unit) {
        coroutines?.launch {
            try {
                tâche()
            } catch (e: CancellationException) {
                throw e   // l'écran a été fermé : on arrête simplement
            } catch (e: Exception) {
                vue?.afficherErreur()
            }
        }
    }
}