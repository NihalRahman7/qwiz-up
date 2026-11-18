package com.crosemont.qwizup

import com.crosemont.qwizup.presentation.ModeleQuiz.CarteLibrary
import com.crosemont.qwizup.presentation.library.ContratLibrairie
import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie
import com.crosemont.qwizup.presentation.library.ContratLibrairie.TypeBadge
import com.crosemont.qwizup.presentation.library.PresentateurLibrairie
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PresentateurLibrairieTest {

    private lateinit var vue: FausseVue
    private lateinit var modele: FauxModele
    private lateinit var presentateur: PresentateurLibrairie

    @Before
    fun preparer() {
        vue = FausseVue()
        modele = FauxModele()
        presentateur = PresentateurLibrairie(
            modele,
            filPrincipal = Dispatchers.Unconfined + CoroutineName("principal"),
            filArrierePlan = Dispatchers.Unconfined + CoroutineName("arrière-plan")
        )
    }

    @Test
    fun `Étant donné que deux favoris existent lorsqu'on attache la vue, on obtient les deux favoris`() {
        presentateur.attacherVue(vue)

        assertEquals(listOf("Le système solaire", "La Révolution tranquille"), vue.titresAffiches)
    }

    @Test
    fun `Étant donné qu'aucun favori n'existe lorsqu'on attache la vue, on obtient le message aucun favori`() {
        modele.favoris = emptyList()

        presentateur.attacherVue(vue)

        assertEquals("Aucun favori pour l’instant", vue.messageVide)
        assertTrue(vue.titresAffiches.isEmpty())
    }

    @Test
    fun `Étant donné que le modèle est en panne lorsqu'on attache la vue, on obtient un message d'erreur`() {
        modele.enPanne = true

        presentateur.attacherVue(vue)

        assertEquals("Impossible de charger les quiz", vue.messageVide)
        assertTrue(vue.titresAffiches.isEmpty())
    }

    @Test
    fun `Étant donné qu'un fil d'arrière-plan est configuré lorsqu'on change d'onglet, on obtient tous les appels au modèle sur ce fil`() {
        presentateur.attacherVue(vue)
        presentateur.surOngletClique(OngletLibrairie.COMPLETES)

        assertEquals(listOf("arrière-plan", "arrière-plan"), modele.filsUtilises)
    }

    @Test
    fun `Étant donné que la vue est détachée lorsqu'on change d'onglet, on obtient aucun appel à la vue`() {
        presentateur.attacherVue(vue)
        presentateur.detacherVue()
        val appelsAvant = vue.nbAppels

        presentateur.surOngletClique(OngletLibrairie.CREES)

        assertEquals(appelsAvant, vue.nbAppels)
    }

    private class FauxModele : ContratLibrairie.IModeleLibrairie {
        var favoris = listOf(
            CarteLibrary(
                1,
                "Le système solaire",
                "Sciences · 10 questions",
                null,
                TypeBadge.NOUVEAU
            ),
            CarteLibrary(
                4,
                "La Révolution tranquille",
                "Histoire · 8 questions",
                null,
                TypeBadge.NOUVEAU
            )
        )
        var enPanne = false
        val filsUtilises = mutableListOf<String?>()

        override suspend fun obtenirQuiz(onglet: OngletLibrairie): List<CarteLibrary> {
            filsUtilises.add(currentCoroutineContext()[CoroutineName]?.name)
            if (enPanne) throw IllegalStateException("Le modèle est en panne")
            return if (onglet == OngletLibrairie.FAVORIS) favoris else emptyList()
        }
    }

    private class FausseVue : ContratLibrairie.IVueLibrary {
        val titresAffiches = mutableListOf<String>()
        var messageVide: String? = null
        var nbAppels = 0

        override fun viderCartes() {
            nbAppels++
            titresAffiches.clear()
            messageVide = null
        }

        override fun ajouterCarteQuiz(
            id: Int,
            titre: String,
            sousTitre: String,
            badge: String?,
            typeBadge: TypeBadge
        ) {
            nbAppels++
            titresAffiches.add(titre)
        }

        override fun afficherMessageVide(message: String) {
            nbAppels++
            messageVide = message
        }

        override fun ouvrirDetails(quizId: Int, titre: String) {
            nbAppels++
        }
    }
}