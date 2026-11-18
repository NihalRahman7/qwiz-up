package com.crosemont.qwizup

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.AccèsQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Difficulté
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ModèleQuiz.CarteCreate
import com.crosemont.qwizup.presentation.ModèleQuiz.QuestionCréer
import com.crosemont.qwizup.presentation.ModèleQuiz.QuizCréer
import com.crosemont.qwizup.presentation.create.ContratCréer
import com.crosemont.qwizup.presentation.create.ContratCréer.ErreurEditeur
import com.crosemont.qwizup.presentation.create.PrésentateurÉditeur
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Dialogue « Quitter sans sauvegarder? » de l'éditeur :
 * le bouton pour enregistrer apparaît seulement s'il peut marcher,
 * et il dit ce qu'il va faire (« Publier » ou « Enregistrer le brouillon »).
 */
class PrésentateurÉditeurQuitterTest {

    private val question = QuestionCréer("Combien font 2 + 2?", TypeQuestion.CHOIX_MULTIPLE, listOf("3", "4", "5", "6"), setOf(1))

    // Un brouillon déjà enregistré, avec un titre et une question complète
    private val brouillonComplet = QuizCréer(
        1, "Mon quiz", "", CatégorieQuiz.SCIENCES, Difficulté.FACILE, AccèsQuiz.TOUT_LE_MONDE,
        listOf(question), estPublié = false
    )

    /** Ouvre le quiz donné dans l'éditeur, comme quand on clique « Continuer ». */
    private fun ouvrir(quiz: QuizCréer): Pair<PrésentateurÉditeur, FausseVue> {
        val vue = FausseVue()
        val présentateur = PrésentateurÉditeur(FauxModèle(quiz), Dispatchers.Unconfined, Dispatchers.Unconfined)
        présentateur.attacherVue(vue)
        présentateur.chargerQuiz(quiz.id)
        return présentateur to vue
    }

    @Test
    fun `Étant donné un brouillon complet modifié lorsqu'on quitte, le dialogue offre Enregistrer le brouillon`() {
        val (présentateur, vue) = ouvrir(brouillonComplet)
        présentateur.surTitreModifie("Mon quiz modifié")

        présentateur.surFermerClique()

        assertEquals(Dialogue(peutEnregistrer = true, publier = false), vue.dialogue)
    }

    @Test
    fun `Étant donné un quiz dont on a effacé le titre lorsqu'on quitte, le dialogue n'offre pas d'enregistrer`() {
        val (présentateur, vue) = ouvrir(brouillonComplet)
        présentateur.surTitreModifie("")

        présentateur.surFermerClique()

        assertFalse(vue.dialogue!!.peutEnregistrer)
    }

    @Test
    fun `Étant donné un quiz complet avec Publier coché lorsqu'on quitte, le bouton du dialogue dit Publier`() {
        val (présentateur, vue) = ouvrir(brouillonComplet)
        présentateur.surPublicationCochee(true)

        présentateur.surFermerClique()

        assertEquals(Dialogue(peutEnregistrer = true, publier = true), vue.dialogue)
    }

    @Test
    fun `Étant donné un quiz sans question avec Publier coché lorsqu'on quitte, le dialogue n'offre pas de publier`() {
        val (présentateur, vue) = ouvrir(brouillonComplet.copy(questions = emptyList()))
        présentateur.surPublicationCochee(true)

        présentateur.surFermerClique()

        assertFalse(vue.dialogue!!.peutEnregistrer)
    }

    @Test
    fun `Étant donné un quiz sans changement lorsqu'on quitte, l'éditeur se ferme sans dialogue`() {
        val (présentateur, vue) = ouvrir(brouillonComplet)

        présentateur.surFermerClique()

        assertNull(vue.dialogue)
        assertTrue(vue.fermé)
    }

    // ----- Faux objets -----

    data class Dialogue(val peutEnregistrer: Boolean, val publier: Boolean)

    private class FauxModèle(private val quiz: QuizCréer) : ContratCréer.IModèleCréer {
        override suspend fun obtenirBrouillons(): List<CarteCreate> = emptyList()
        override suspend fun obtenirPubliés(): List<CarteCreate> = emptyList()
        override suspend fun obtenirQuiz(quizId: Int): QuizCréer = quiz
        override suspend fun sauvegarderQuiz(quiz: QuizCréer): Int = quiz.id
    }

    private class FausseVue : ContratCréer.IVueÉditeur {
        var dialogue: Dialogue? = null
        var fermé = false

        override fun demanderConfirmationQuitter(peutEnregistrer: Boolean, publier: Boolean) {
            dialogue = Dialogue(peutEnregistrer, publier)
        }
        override fun fermer() { fermé = true }

        // Le reste de la vue ne sert pas dans ces tests
        override fun afficherEnTete(nouveauQuiz: Boolean) {}
        override fun afficherInfosQuiz(titre: String, description: String) {}
        override fun afficherCategorieChoisie(catégorie: CatégorieQuiz) {}
        override fun afficherCouverture(catégorie: CatégorieQuiz) {}
        override fun afficherCategoriePerso(visible: Boolean, nom: String) {}
        override fun afficherDifficulteChoisie(difficulté: Difficulté) {}
        override fun afficherAccesChoisi(accès: AccèsQuiz) {}
        override fun viderQuestions() {}
        override fun ajouterQuestion(
            index: Int, texte: String, type: TypeQuestion,
            réponses: List<String>, bonnesRéponses: Set<Int>, réponseAttendue: String
        ) {}
        override fun afficherBonnesReponses(index: Int, bonnesRéponses: Set<Int>) {}
        override fun afficherNombreQuestions(nombre: Int) {}
        override fun afficherErreur(erreur: ErreurEditeur) {}
        override fun afficherErreurQuestion(numero: Int) {}
        override fun effacerErreurs() {}
        override fun confirmerSauvegarde() {}
        override fun confirmerPublication(titre: String) {}
        override fun demanderConfirmationSuppression(index: Int) {}
        override fun afficherPublication(estPublié: Boolean) {}
        override fun afficherBoutonEnregistrer(publier: Boolean, actif: Boolean, raison: ErreurEditeur?, numeroQuestion: Int) {}
        override fun afficherErreurChargement() {}
        override fun afficherErreurSauvegarde() {}
    }
}
