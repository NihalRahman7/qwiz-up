package com.crosemont.qwizup

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz.CarteLibrary
import com.crosemont.qwizup.presentation.library.ContratLibrairie
import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie
import com.crosemont.qwizup.presentation.library.ContratLibrairie.TypeBadge
import com.crosemont.qwizup.presentation.library.PrésentateurLibrairie
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrésentateurLibrairieTest {

    private lateinit var vue: FausseVue
    private lateinit var modele: FauxModèle
    private lateinit var presentateur: PrésentateurLibrairie

    @Before
    fun preparer() {
        vue = FausseVue()
        modele = FauxModèle()
        presentateur = PrésentateurLibrairie(
            modele,
            filPrincipal = Dispatchers.Unconfined + CoroutineName("principal"),
            filArrièrePlan = Dispatchers.Unconfined + CoroutineName("arrière-plan")
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
        presentateur.surOngletClique(OngletLibrairie.COMPLÉTÉS)

        assertEquals(listOf("arrière-plan", "arrière-plan"), modele.filsUtilises)
    }

    @Test
    fun `Étant donné que la vue est détachée lorsqu'on change d'onglet, on obtient aucun appel à la vue`() {
        presentateur.attacherVue(vue)
        presentateur.detacherVue()
        val appelsAvant = vue.nbAppels

        presentateur.surOngletClique(OngletLibrairie.CRÉES)

        assertEquals(appelsAvant, vue.nbAppels)
    }

    @Test
    fun `Étant donné un quiz publié dans Mes quiz lorsqu'on ouvre l'onglet, on obtient son code`() {
        presentateur.attacherVue(vue)

        presentateur.surOngletClique(OngletLibrairie.CRÉES)

        assertEquals("K7M4Q", vue.codesAffichés[5])
    }

    @Test
    fun `Étant donné un quiz dans Mes quiz lorsqu'on touche Modifier, on obtient l'éditeur sur ce quiz`() {
        presentateur.attacherVue(vue)

        presentateur.surModifierClique(5)

        assertEquals(5, vue.éditeurOuvert)
    }

    private class FauxModèle : ContratLibrairie.IModèleLibrairie {
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

        // « Mes quiz » : un quiz publié avec son code
        var créés = listOf(
            CarteLibrary(
                5, "La Révolution tranquille", "4 questions · Public", "Publié", TypeBadge.SCORE,
                catégorie = CatégorieQuiz.HISTOIRE, code = "K7M4Q", estCréé = true
            )
        )
        var enPanne = false
        val filsUtilises = mutableListOf<String?>()

        override suspend fun obtenirQuiz(onglet: OngletLibrairie): List<CarteLibrary> {
            filsUtilises.add(currentCoroutineContext()[CoroutineName]?.name)
            if (enPanne) throw IllegalStateException("Le modèle est en panne")
            return when (onglet) {
                OngletLibrairie.FAVORIS -> favoris
                OngletLibrairie.CRÉES -> créés
                else -> emptyList()
            }
        }
    }

    private class FausseVue : ContratLibrairie.IVueLibrary {
        val titresAffiches = mutableListOf<String>()
        val codesAffichés = mutableMapOf<Int, String?>()
        var messageVide: String? = null
        var éditeurOuvert: Int? = null
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
            typeBadge: TypeBadge,
            catégorie: CatégorieQuiz?,
            code: String?,
            estCréé: Boolean
        ) {
            nbAppels++
            titresAffiches.add(titre)
            codesAffichés[id] = code
        }

        override fun afficherMessageVide(message: String) {
            nbAppels++
            messageVide = message
        }

        override fun ouvrirDétails(quizId: Int, titre: String) {
            nbAppels++
        }

        override fun ouvrirÉditeur(quizId: Int) {
            nbAppels++
            éditeurOuvert = quizId
        }
    }
}