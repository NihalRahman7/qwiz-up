package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IModèleConnexion
import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IPrésentateurConnexion
import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IVueConnexion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class PrésentateurConnexion(
    private val modèle: IModèleConnexion,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,     // pour mettre la vue à jour
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,     // pour appeler le modèle
) : IPrésentateurConnexion {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueConnexion? = null

    // Les coroutines du présentateur vivent tant que la vue est attachée
    private var coroutines: CoroutineScope? = null

    override fun attacherVue(vue: IVueConnexion) {
        this.vue = vue
        coroutines = CoroutineScope(filPrincipal + Job())
        // Au départ, les champs sont vides : le bouton est désactivé
        vue.activerConnexion(false)
    }

    override fun detacherVue() {
        coroutines?.cancel()
        coroutines = null
        vue = null
    }

    // Bouton actif seulement quand les deux champs sont remplis
    override fun surChampsModifiés(nom: String, motDePasse: String) {
        vue?.activerConnexion(nom.isNotBlank() && motDePasse.isNotBlank())
    }

    // Retient l'utilisateur sur l'appareil, puis ouvre l'Accueil
    // TODO : vérifier le nom et le mot de passe avec l'API (équipe des comptes)
    override fun surConnexionClique(nom: String, motDePasse: String) {
        lancer {
            withContext(filArrièrePlan) { modèle.connecter(nom) }
            vue?.ouvrirAccueil()
        }
    }

    override fun surCréerCompteClique() {
        vue?.ouvrirInscription()
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