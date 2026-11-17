package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.entites.ResultatPartie
import com.crosemont.qwizup.sourceDeDonnees.SourceResultatsLocale
import java.io.File
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SourceResultatsLocaleTest {
    private lateinit var fichier: File

    @Before
    fun preparer() {
        // Un fichier temporaire qui n'existe pas encore, comme au premier lancement de l'app
        fichier = File.createTempFile("resultats", ".txt")
        fichier.delete()
    }

    @After
    fun nettoyer() {
        fichier.delete()
    }

    @Test
    fun `Étant donné qu'aucun fichier n'existe lorsqu'on lit les résultats, on obtient une liste vide`() {
        assertTrue(SourceResultatsLocale(fichier).lireResultats().isEmpty())
    }

    @Test
    fun `Étant donné deux résultats ajoutés lorsqu'on les relit, on obtient les deux dans l'ordre`() {
        val source = SourceResultatsLocale(fichier)
        val premier = ResultatPartie(1, 4, 5, LocalDate.of(2026, 11, 14))
        val deuxieme = ResultatPartie(2, 3, 5, LocalDate.of(2026, 11, 15))

        source.ajouterResultat(premier)
        source.ajouterResultat(deuxieme)

        assertEquals(listOf(premier, deuxieme), source.lireResultats())
    }

    @Test
    fun `Étant donné un résultat ajouté lorsqu'on relit avec une nouvelle source, on obtient le même résultat`() {
        val resultat = ResultatPartie(1, 4, 5, LocalDate.of(2026, 11, 15))
        SourceResultatsLocale(fichier).ajouterResultat(resultat)

        // Une nouvelle source sur le même fichier, comme quand on ferme et qu'on rouvre l'app
        val sourceApresRedemarrage = SourceResultatsLocale(fichier)

        assertEquals(listOf(resultat), sourceApresRedemarrage.lireResultats())
    }
}