package com.crosemont.qwizup.presentation.profil

import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.presentation.profil.ContratProfil.IModèleProfil
import com.crosemont.qwizup.presentation.profil.ContratProfil.IPrésentateurProfil
import com.crosemont.qwizup.presentation.profil.ContratProfil.IVueProfil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// Présentateur du profil : charge le profil, la photo et les stats, et réagit aux clics
class PrésentateurProfil(
    private val modèle: IModèleProfil,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,     // pour mettre la vue à jour
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,     // pour appeler le modèle
) : IPrésentateurProfil {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueProfil? = null

    // Les coroutines du présentateur vivent tant que la vue est attachée
    private var coroutines: CoroutineScope? = null

    // Le nom affiché actuel, pour remplir la feuille « Modifier »
    private var nomActuel = ""

    override fun attacherVue(vue: IVueProfil) {
        this.vue = vue
        coroutines = CoroutineScope(filPrincipal + Job())
        lancer { afficherTout() }
    }

    override fun detacherVue() {
        coroutines?.cancel()
        coroutines = null
        vue = null
    }

    override fun surModifierClique() {
        vue?.ouvrirModification(nomActuel)
    }

    override fun surNomEnregistré(nom: String) {
        lancer {
            try {
                withContext(filArrièrePlan) { modèle.changerNomAffiché(nom) }
            } catch (e: ServiceException) {
                // Nom vide ou trop long : la feuille reste ouverte
                vue?.afficherNomInvalide()
                return@lancer
            }
            vue?.fermerModification()
            afficherTout()
        }
    }

    override fun surPhotoChoisie(photo: ByteArray) {
        changerPhoto(photo)
    }

    override fun surPhotoRetirée() {
        changerPhoto(null)
    }

    override fun surMesQuizClique() {
        vue?.ouvrirMesQuiz()
    }

    override fun surLangueClique() {
        vue?.ouvrirLangue()
    }

    override fun surÀProposClique() {
        vue?.ouvrirÀPropos()
    }

    override fun surDéconnexionClique() {
        lancer {
            withContext(filArrièrePlan) { modèle.déconnecter() }
            vue?.ouvrirBienvenue()
        }
    }

    override fun surRetourClique() {
        vue?.fermer()
    }

    private fun changerPhoto(photo: ByteArray?) {
        lancer {
            withContext(filArrièrePlan) { modèle.changerPhoto(photo) }
            vue?.afficherPhoto(photo)
            vue?.fermerModification()
        }
    }

    // Charge le profil, la photo et les stats, puis les affiche
    private suspend fun afficherTout() {
        val profil = withContext(filArrièrePlan) { modèle.obtenirProfil() }
        if (profil == null) {
            nomActuel = ""
            vue?.afficherInvité()
        } else {
            nomActuel = profil.nomAffiché
            vue?.afficherProfil(profil.nomAffiché, profil.nomUtilisateur, profil.courriel)
        }

        val photo = withContext(filArrièrePlan) { modèle.obtenirPhoto() }
        vue?.afficherPhoto(photo)

        val stats = withContext(filArrièrePlan) { modèle.obtenirStatistiques() }
        vue?.afficherStatistiques(stats.série, stats.nbQuizComplètes, stats.tauxRéussite)
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