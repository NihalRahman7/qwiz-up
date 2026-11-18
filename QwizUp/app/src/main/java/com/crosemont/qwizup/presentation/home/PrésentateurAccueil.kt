package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.presentation.home.ContratAccueil.IPrésentateurAccueil
import com.crosemont.qwizup.presentation.home.ContratAccueil.IVueAccueil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// Présentateur de l'accueil : reçoit les clics de la vue et décide quoi faire
class PrésentateurAccueil(
    private val modele: ContratAccueil.IModèleAccueil,
    private val filPrincipal: CoroutineContext = Dispatchers.Main, // pour mettre la vue à jour
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO, // pour appeler le modèle
) : IPrésentateurAccueil {

    // La vue attachée (null quand l'écran n'est pas affiché)
    private var vue: IVueAccueil? = null

    // Les coroutines du présentateur vivent tant que la vue est attachée
    private var corountines: CoroutineScope? = null

    override fun attacherVue(vue: IVueAccueil) {
        this.vue = vue
        corountines = CoroutineScope(filPrincipal + Job())
        // À chaque retour sur l'accueil (ex. après une partie), les stats sont rechargées
        chargerStatistiques()
    }

    override fun detacherVue() {
        corountines?.cancel()
        corountines = null
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

    private fun chargerStatistiques() {
        corountines?.launch {
            try {
                val stats = withContext(filArrièrePlan) { modele.obtenirStatistiques() }
                vue?.afficherStatistiques(stats.série, stats.nbQuizComplètes, stats.tauxRéussite)
            } catch (e: CancellationException) {
                vue?.afficherErreur()
            } catch (e: Exception) {
                vue?.afficherErreur()
            }
        }
    }
}