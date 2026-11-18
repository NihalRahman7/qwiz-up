package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.entites.RésultatPartie
import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsBidon
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ServiceStatistiquesTest {

    // On fixe la date du jour pour que les tests donnent toujours le même résultat
    private val aujourdhui = LocalDate.of(2026, 11, 15)

    // Crée un service avec une source bidon qui contient les parties données
    private fun service(vararg resultats: RésultatPartie) =
        ServiceStatistiques(SourceRésultatsBidon(resultats.toList())) { aujourdhui }

    // Crée une partie jouée il y a un certain nombre de jours
    private fun partie(quizId: Int, score: Int, total: Int, ilYaJours: Long) =
        RésultatPartie(quizId, score, total, aujourdhui.minusDays(ilYaJours))

    @Test
    fun `Étant donné qu'aucune partie n'a été jouée lorsqu'on obtient les statistiques, on obtient des zéros`() {
        val stats = service().obtenirStatistiques()

        assertEquals(0, stats.série)
        assertEquals(0, stats.nbQuizComplètes)
        assertEquals(0, stats.tauxRéussite)
    }

    @Test
    fun `Étant donné des parties aujourd'hui, hier et avant-hier lorsqu'on obtient la série, on obtient 3`() {
        val stats = service(
            partie(1, 5, 5, 0),
            partie(2, 5, 5, 1),
            partie(3, 5, 5, 2)
        ).obtenirStatistiques()

        assertEquals(3, stats.série)
    }

    @Test
    fun `Étant donné des parties hier et avant-hier mais pas aujourd'hui lorsqu'on obtient la série, on obtient 2`() {
        val stats = service(partie(1, 5, 5, 1), partie(2, 5, 5, 2)).obtenirStatistiques()

        assertEquals(2, stats.série)
    }

    @Test
    fun `Étant donné que la dernière partie date de 3 jours lorsqu'on obtient la série, on obtient 0`() {
        val stats = service(partie(1, 5, 5, 3)).obtenirStatistiques()

        assertEquals(0, stats.série)
    }

    @Test
    fun `Étant donné le même quiz joué deux fois lorsqu'on compte les quiz complétés, on obtient 1`() {
        val stats = service(partie(1, 2, 5, 0), partie(1, 5, 5, 0)).obtenirStatistiques()

        assertEquals(1, stats.nbQuizComplètes)
    }

    @Test
    fun `Étant donné 8 sur 10 et 2 sur 10 lorsqu'on calcule la réussite, on obtient 50`() {
        val stats = service(partie(1, 8, 10, 0), partie(2, 2, 10, 0)).obtenirStatistiques()

        assertEquals(50, stats.tauxRéussite)
    }

    @Test
    fun `Étant donné une partie enregistrée lorsqu'on obtient les statistiques, on obtient une série de 1`() {
        val service = service()

        service.enregistrerPartie(quizId = 1, score = 4, total = 5)
        val stats = service.obtenirStatistiques()

        assertEquals(1, stats.série)
        assertEquals(1, stats.nbQuizComplètes)
        assertEquals(80, stats.tauxRéussite)
    }

    @Test(expected = ServiceException::class)
    fun `Étant donné un score plus grand que le total lorsqu'on enregistre, on obtient une ServiceException`() {
        service().enregistrerPartie(quizId = 1, score = 6, total = 5)
    }

    @Test(expected = ServiceException::class)
    fun `Étant donné un quiz sans question lorsqu'on enregistre, on obtient une ServiceException`() {
        service().enregistrerPartie(quizId = 1, score = 0, total = 0)
    }
}