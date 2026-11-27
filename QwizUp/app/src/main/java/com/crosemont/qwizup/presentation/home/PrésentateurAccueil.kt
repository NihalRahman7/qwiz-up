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
        // Au départ, le champ du code est vide : le bouton Jouer est désactivé
        vue.activerJouerCode(false)
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

    // Un code valide contient exactement 5 lettres ou chiffres (sans accents)
    private fun estFormatValide(code: String): Boolean =
        code.length == LONGUEUR_CODE && code.all { it.isLetterOrDigit() && it.code < 128 }

    // Le texte du champ a changé : on efface l'erreur, le bouton s'active seulement avec 5 caractères
    override fun surCodeModifié(code: String) {
        vue?.effacerErreurCode()
        vue?.activerJouerCode(estFormatValide(code.trim()))
    }

    // « Jouer » : on cherche le quiz publié qui a ce code (majuscules ou minuscules)
    override fun surJouerCodeClique(code: String) {
        val codePropre = code.trim().uppercase()
        if (!estFormatValide(codePropre)) {
            vue?.afficherCodeInvalide()
            return
        }
        corountines?.launch {
            try {
                val quizId = withContext(filArrièrePlan) { modele.trouverQuizParCode(codePropre) }
                if (quizId == null) vue?.afficherCodeInvalide() else vue?.ouvrirQuiz(quizId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                vue?.afficherErreur()
            }
        }
    }

    // L'avatar a été touché : on ouvre le Profil
    override fun surProfilClique() {
        vue?.ouvrirProfil()
    }

    private fun chargerStatistiques() {
        corountines?.launch {
            try {
                val stats = withContext(filArrièrePlan) { modele.obtenirStatistiques() }
                vue?.afficherStatistiques(stats.série, stats.nbQuizComplètes, stats.tauxRéussite)

                // Un quiz réussi à 100 % disparaît de « Continuer »
                val enCours = withContext(filArrièrePlan) { modele.obtenirQuizEnCours() }
                vue?.afficherQuizEnCours(enCours)
            } catch (e: CancellationException) {
                vue?.afficherErreur()
            } catch (e: Exception) {
                vue?.afficherErreur()
            }
        }
    }

    companion object {
        const val LONGUEUR_CODE = 5
    }
}