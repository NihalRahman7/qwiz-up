
package com.crosemont.qwizup

import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ModeleQuiz.CarteDiscover
import com.crosemont.qwizup.presentation.discover.ContratDécouvrir
import com.crosemont.qwizup.presentation.discover.PrésentateurDécouvrir
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrésentateurDécouvrirTest {

    private lateinit var vue: FausseVue
    private lateinit var modèle: FauxModèle
    private lateinit var présentateur: PrésentateurDécouvrir

    @Before
    fun préparer() {
        vue = FausseVue()
        modèle = FauxModèle()
        présentateur = PrésentateurDécouvrir(
            modèle,
            filPrincipal = Dispatchers.Unconfined + CoroutineName("principal"),
            filArrièrePlan = Dispatchers.Unconfined + CoroutineName("arrière-plan")
        )
    }

    @Test
    fun `Étant donné qu'un modèle contient deux quiz lorsqu'on attache la vue, on obtient Tous et les deux quiz`() {
        présentateur.attacherVue(vue)

        assertEquals(listOf<CategorieQuiz?>(null), vue.catégoriesAffichées)
        assertEquals(listOf("Le système solaire", "La Révolution tranquille"), vue.titresAffichés)
    }

    @Test
    fun `Étant donné que deux quiz sont disponibles lorsqu'on choisit Sciences, on obtient seulement le quiz de sciences`() {
        présentateur.attacherVue(vue)

        présentateur.choisirCatégorie(CategorieQuiz.SCIENCES)

        assertEquals(CategorieQuiz.SCIENCES, vue.catégoriesAffichées.last())
        assertEquals(listOf("Le système solaire"), vue.titresAffichés)
    }

    @Test
    fun `Étant donné que deux quiz sont disponibles lorsqu'on recherche un mot, on obtient le quiz qui le contient`() {
        présentateur.attacherVue(vue)

        présentateur.rechercher("tranquille")

        assertEquals(listOf("La Révolution tranquille"), vue.titresAffichés)
    }

    @Test
    fun `Étant donné que deux quiz sont disponibles lorsqu'on recherche un mot absent, on obtient le message aucun résultat`() {
        présentateur.attacherVue(vue)

        présentateur.rechercher("zzz")

        assertTrue(vue.aucunRésultat)
        assertTrue(vue.titresAffichés.isEmpty())
    }

    @Test
    fun `Étant donné qu'un quiz n'est pas favori lorsqu'on bascule son favori, on obtient ce quiz en favori`() {
        présentateur.attacherVue(vue)

        présentateur.basculerFavori(1)

        assertEquals(setOf("Le système solaire"), modèle.favoris)
        assertEquals(true, vue.favorisParId[1])
        assertEquals(false, vue.favorisParId[2])
    }

    @Test
    fun `Étant donné qu'un quiz est favori lorsqu'on bascule son favori, on obtient ce quiz sans favori`() {
        modèle.favoris.add("Le système solaire")
        présentateur.attacherVue(vue)

        présentateur.basculerFavori(1)

        assertTrue(modèle.favoris.isEmpty())
        assertEquals(false, vue.favorisParId[1])
    }

    @Test
    fun `Étant donné qu'un quiz contient des questions lorsqu'on veut jouer, on obtient l'ouverture du quiz`() {
        présentateur.attacherVue(vue)

        présentateur.jouer(1)

        assertEquals("Le système solaire", vue.quizOuvert)
        assertNull(vue.quizBientôt)
    }

    @Test
    fun `Étant donné qu'un quiz ne contient aucune question lorsqu'on veut jouer, on obtient le message bientôt`() {
        présentateur.attacherVue(vue)

        présentateur.jouer(2)

        assertEquals("La Révolution tranquille", vue.quizBientôt)
        assertNull(vue.quizOuvert)
    }

    @Test
    fun `Étant donné qu'un quiz n'est pas affiché lorsqu'on veut jouer, on obtient aucune action`() {
        présentateur.attacherVue(vue)

        présentateur.jouer(99)

        assertNull(vue.quizOuvert)
        assertNull(vue.quizBientôt)
    }

    @Test
    fun `Étant donné qu'un modèle est en panne lorsqu'on attache la vue, on obtient un message d'erreur`() {
        modèle.enPanne = true

        présentateur.attacherVue(vue)

        assertTrue(vue.erreurAffichée)
        assertTrue(vue.titresAffichés.isEmpty())
    }

    @Test
    fun `Étant donné qu'une vue est détachée lorsqu'on fait des actions, on obtient aucun appel à la vue`() {
        présentateur.attacherVue(vue)
        présentateur.detacherVue()
        val appelsAvant = vue.nbAppels

        présentateur.rechercher("zzz")
        présentateur.choisirCatégorie(CategorieQuiz.HISTOIRE)
        présentateur.basculerFavori(1)
        présentateur.jouer(1)

        assertEquals(appelsAvant, vue.nbAppels)
    }

    @Test
    fun `Étant donné qu'un fil d'arrière-plan est configuré lorsqu'on utilise l'écran, on obtient tous les appels au modèle sur ce fil`() {
        présentateur.attacherVue(vue)
        présentateur.basculerFavori(1)
        présentateur.jouer(1)

        assertFalse(modèle.filsUtilisés.isEmpty())
        assertTrue(modèle.filsUtilisés.all { it == "arrière-plan" })
    }

    private class FauxModèle : ContratDécouvrir.IModèleDécouvrir {
        private val quiz = listOf(
            CarteDiscover(1, CategorieQuiz.SCIENCES, "Le système solaire", "Planètes et lunes", 10, 5),
            CarteDiscover(2, CategorieQuiz.HISTOIRE, "La Révolution tranquille", "Le Québec des années 1960", 8, 4)
        )
        val favoris = mutableSetOf<String>()
        val filsUtilisés = mutableListOf<String?>()
        var enPanne = false

        override suspend fun obtenirQuiz(catégorie: CategorieQuiz?, recherche: String): List<CarteDiscover> {
            noterLeFil()
            if (enPanne) throw IllegalStateException("Le modèle est en panne")
            return quiz
                .filter { catégorie == null || it.categorie == catégorie }
                .filter { it.titre.contains(recherche, ignoreCase = true) }
                .map { it.copy(estFavori = it.titre in favoris) }
        }

        override suspend fun basculerFavori(titre: String) {
            noterLeFil()
            if (!favoris.remove(titre)) favoris.add(titre)
        }

        override suspend fun estJouable(quizId: Int): Boolean {
            noterLeFil()
            return quizId == 1   // seul « Le système solaire » a des questions
        }

        private suspend fun noterLeFil() {
            filsUtilisés.add(currentCoroutineContext()[CoroutineName]?.name)
        }
    }

    private class FausseVue : ContratDécouvrir.IVueDécouvrir {
        val titresAffichés = mutableListOf<String>()
        val favorisParId = mutableMapOf<Int, Boolean>()
        val catégoriesAffichées = mutableListOf<CategorieQuiz?>()
        var aucunRésultat = false
        var quizOuvert: String? = null
        var quizBientôt: String? = null
        var erreurAffichée = false
        var nbAppels = 0

        override fun viderQuiz() {
            nbAppels++
            titresAffichés.clear()
            favorisParId.clear()
            aucunRésultat = false
        }

        override fun ajouterQuiz(
            id: Int,
            catégorie: CategorieQuiz,
            titre: String,
            description: String,
            nbQuestions: Int,
            duréeMinutes: Int,
            estFavori: Boolean
        ) {
            nbAppels++
            titresAffichés.add(titre)
            favorisParId[id] = estFavori
        }

        override fun afficherAucunRésultat() {
            nbAppels++
            aucunRésultat = true
        }

        override fun afficherCatégorieChoisie(catégorie: CategorieQuiz?) {
            nbAppels++
            catégoriesAffichées.add(catégorie)
        }

        override fun ouvrirQuiz(quizId: Int, titre: String) {
            nbAppels++
            quizOuvert = titre
        }

        override fun afficherQuizBientôt(titre: String) {
            nbAppels++
            quizBientôt = titre
        }

        override fun afficherErreur() {
            nbAppels++
            erreurAffichée = true
        }

        override fun afficherChargement(visible: Boolean) {
            nbAppels++
        }
    }
}
