package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IModèleInscription
import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IPrésentateurInscription
import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IVueInscription
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class PrésentateurInscription(
    private val modèle: IModèleInscription,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,     // pour mettre la vue à jour
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,     // pour appeler le modèle
) : IPrésentateurInscription {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueInscription? = null

    // Les coroutines du présentateur vivent tant que la vue est attachée
    private var coroutines: CoroutineScope? = null

    override fun attacherVue(vue: IVueInscription) {
        this.vue = vue
        coroutines = CoroutineScope(filPrincipal + Job())
        // Au départ, tout est vide : bouton désactivé, pas de crochet vert
        vue.activerCréation(false)
        vue.afficherMotsDePasseIdentiques(false)
    }

    override fun detacherVue() {
        coroutines?.cancel()
        coroutines = null
        vue = null
    }

    // Un mot de passe valide a 8 caractères ou plus, avec au moins un chiffre
    private fun estMotDePasseValide(motDePasse: String): Boolean =
        motDePasse.length >= 8 && motDePasse.any { it.isDigit() }

    override fun surChampsModifiés(nom: String, courriel: String, motDePasse: String, confirmation: String) {
        val identiques = confirmation.isNotEmpty() && confirmation == motDePasse
        vue?.afficherMotsDePasseIdentiques(identiques)

        // Bouton actif seulement quand tout est valide
        vue?.activerCréation(
            nom.isNotBlank() && courriel.contains("@") && estMotDePasseValide(motDePasse) && identiques
        )
    }

    // Crée le profil sur l'appareil, puis ouvre l'Accueil
    // TODO : créer aussi le compte avec l'API (équipe des comptes)
    override fun surCréerCompteClique(nom: String, courriel: String, motDePasse: String) {
        lancer {
            withContext(filArrièrePlan) { modèle.créerProfil(nom, courriel) }
            vue?.ouvrirAccueil()
        }
    }

    override fun surDéjàCompteClique() {
        vue?.ouvrirConnexion()
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