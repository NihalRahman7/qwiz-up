package com.crosemont.qwizup.presentation.library

import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class PrésentateurLibrairie(
    private val modèle: ContratLibrairie.IModèleLibrairie,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,
) : ContratLibrairie.IPrésentateurLibrary {

    private var vue: ContratLibrairie.IVueLibrary? = null


    private var coroutines: CoroutineScope? = null
    private var chargement: Job? = null

    override fun surOngletClique(onglet: OngletLibrairie) {
        val messageVide = when (onglet) {
            OngletLibrairie.FAVORIS -> "Aucun favori pour l’instant"
            OngletLibrairie.COMPLÉTÉS -> "Aucun quiz complété"
            OngletLibrairie.CRÉES -> "Vous n'avez créé aucun quiz"
        }

        chargement?.cancel()   // un nouvel onglet remplace la demande en cours
        chargement = coroutines?.launch {
            try {

                val cartes = withContext(filArrièrePlan) { modèle.obtenirQuiz(onglet) }

                vue?.viderCartes()
                if (cartes.isEmpty()) {
                    vue?.afficherMessageVide(messageVide)
                } else {
                    cartes.forEach {
                        vue?.ajouterCarteQuiz(it.id, it.titre, it.sousTitre, it.badge, it.typeBadge)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                vue?.viderCartes()
                vue?.afficherMessageVide("Impossible de charger les quiz")
            }
        }
    }

    override fun surQuizClique(quizId: Int, titre: String) {
        vue?.ouvrirDétails(quizId, titre)
    }

    override fun attacherVue(vue: ContratLibrairie.IVueLibrary) {
        this.vue = vue
        coroutines = CoroutineScope(filPrincipal + Job())
        surOngletClique(OngletLibrairie.FAVORIS)
    }

    override fun detacherVue() {
        coroutines?.cancel()   // arrête le travail en cours : la vue n'existe plus
        coroutines = null
        vue = null
    }
}