package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.presentation.ModeleQuiz.CarteCreate
import com.crosemont.qwizup.presentation.create.ContratCréer.OngletCreer
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrésentateurCréer(
    private val modèle: ContratCréer.IModèleCréer,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,   // pour mettre la vue à jour
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,   // pour appeler le modèle
) : ContratCréer.IPrésentateurCréer {

    private var vue: ContratCréer.IVueCréer? = null

    // Les coroutines du présentateur vivent tant que la vue est attachée
    private var coroutines: CoroutineScope? = null

    private var ongletActif = OngletCreer.BROUILLONS
    private var brouillons: List<CarteCreate> = emptyList()
    private var publiés: List<CarteCreate> = emptyList()

    override fun attacherVue(vue: ContratCréer.IVueCréer) {
        this.vue = vue
        coroutines = CoroutineScope(filPrincipal + Job())
        vue.afficherOngletChoisi(ongletActif)
        charger()
    }

    override fun detacherVue() {
        coroutines?.cancel()   // arrête le travail en cours : la vue n'existe plus
        coroutines = null
        vue = null
    }

    override fun surNouveauQuizClique() {
        vue?.ouvrirEditeur(null)
    }

    override fun surOngletClique(onglet: OngletCreer) {
        ongletActif = onglet
        afficherOnglet()
    }

    override fun surQuizClique(quizId: Int) {
        vue?.ouvrirEditeur(quizId)
    }

    // Demande au modèle les brouillons et les quiz publiés, en arrière-plan
    private fun charger() {
        lancer {
            val (nouveauxBrouillons, nouveauxPubliés) = withContext(filArrièrePlan) {
                modèle.obtenirBrouillons() to modèle.obtenirPubliés()
            }
            brouillons = nouveauxBrouillons
            publiés = nouveauxPubliés
            vue?.afficherCompteurs(brouillons.size, publiés.size)
            afficherOnglet()
        }
    }

    private fun afficherOnglet() {
        val cartes = if (ongletActif == OngletCreer.BROUILLONS) brouillons else publiés
        vue?.afficherOngletChoisi(ongletActif)
        vue?.viderCartes()
        if (cartes.isEmpty()) {
            vue?.afficherListeVide(ongletActif)
        } else {
            cartes.forEach { vue?.ajouterCarteQuiz(it.id, it.titre, it.nbQuestions, it.estPublie) }
        }
    }

    // Lance une coroutine sur le fil principal. Si le modèle échoue, la vue affiche une erreur.
    private fun lancer(travail: suspend () -> Unit): Job? =
        coroutines?.launch {
            try {
                travail()
            } catch (e: CancellationException) {
                throw e   // la coroutine a été annulée : ce n'est pas une erreur
            } catch (e: Exception) {
                vue?.afficherErreurChargement()
            }
        }
}
