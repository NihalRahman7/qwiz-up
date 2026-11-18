package com.crosemont.qwizup

import com.crosemont.qwizup.accèsAuxDonnées.SourceFavorisBidon
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsBidon
import com.crosemont.qwizup.domaine.entites.RésultatPartie
import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie
import com.crosemont.qwizup.presentation.library.ContratLibrairie.TypeBadge
import com.crosemont.qwizup.presentation.library.ModèleLibrairie
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Onglet Complétés de la Librairie : un quiz y apparaît seulement
 * quand on l'a réussi à 100 % (toutes les bonnes réponses).
 */
class QuizComplétésTest {

    private val aujourdhui = LocalDate.of(2026, 11, 17)

    private fun partie(quizId: Int, score: Int, total: Int, ilYaJours: Long = 0) =
        RésultatPartie(quizId, score, total, aujourdhui.minusDays(ilYaJours))

    private fun service(vararg parties: RésultatPartie) =
        ServiceStatistiques(SourceRésultatsBidon(parties.toList())) { aujourdhui }

    // Le catalogue par défaut contient les quiz créés : 1 = Le système solaire (5 questions)
    private fun cartesComplétés(vararg parties: RésultatPartie) = runBlocking {
        ModèleLibrairie(
            ServiceFavoris(SourceFavorisBidon(emptySet())),
            serviceStatistiques = service(*parties)
        ).obtenirQuiz(OngletLibrairie.COMPLÉTÉS)
    }

    // ----- Service -----

    @Test
    fun `Étant donné qu'aucune partie n'a été jouée lorsqu'on obtient les quiz complétés, on obtient une liste vide`() {
        assertTrue(service().obtenirQuizComplétés().isEmpty())
    }

    @Test
    fun `Étant donné un quiz réussi à 5 sur 5 lorsqu'on obtient les quiz complétés, on obtient ce quiz`() {
        assertEquals(listOf(1), service(partie(1, 5, 5)).obtenirQuizComplétés())
    }

    @Test
    fun `Étant donné un quiz fini à 4 sur 5 lorsqu'on obtient les quiz complétés, il n'est pas complété`() {
        assertTrue(service(partie(1, 4, 5)).obtenirQuizComplétés().isEmpty())
    }

    @Test
    fun `Étant donné un quiz raté puis réussi à 100 % lorsqu'on obtient les quiz complétés, on obtient ce quiz`() {
        assertEquals(listOf(1), service(partie(1, 3, 5, 1), partie(1, 5, 5)).obtenirQuizComplétés())
    }

    @Test
    fun `Étant donné un quiz réussi à 100 % deux fois lorsqu'on obtient les quiz complétés, il apparaît une seule fois`() {
        assertEquals(listOf(1), service(partie(1, 5, 5, 1), partie(1, 5, 5)).obtenirQuizComplétés())
    }

    @Test
    fun `Étant donné deux quiz réussis à des jours différents lorsqu'on obtient les quiz complétés, le plus récent est en premier`() {
        val complétés = service(partie(2, 6, 6, 1), partie(1, 5, 5)).obtenirQuizComplétés()

        assertEquals(listOf(1, 2), complétés)
    }

    // ----- Modèle de la Librairie -----

    @Test
    fun `Étant donné un quiz réussi à 100 % lorsqu'on ouvre l'onglet Complétés, on obtient sa carte avec le badge 100 %`() {
        val carte = cartesComplétés(partie(1, 5, 5)).single()

        assertEquals(1, carte.id)
        assertEquals("Le système solaire", carte.titre)
        assertEquals("5 questions · toutes bonnes", carte.sousTitre)
        assertEquals("100 %", carte.badge)
        assertEquals(TypeBadge.PROGRESSION, carte.typeBadge)
    }

    @Test
    fun `Étant donné seulement des quiz pas réussis à 100 % lorsqu'on ouvre l'onglet Complétés, on obtient aucune carte`() {
        assertTrue(cartesComplétés(partie(1, 4, 5), partie(2, 0, 6)).isEmpty())
    }

    @Test
    fun `Étant donné un résultat pour un quiz qui n'existe plus lorsqu'on ouvre l'onglet Complétés, ce résultat est ignoré`() {
        val cartes = cartesComplétés(partie(999, 3, 3), partie(1, 5, 5))

        assertEquals(listOf(1), cartes.map { it.id })
    }
}
