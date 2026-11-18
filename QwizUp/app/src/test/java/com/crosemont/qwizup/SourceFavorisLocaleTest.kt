package com.crosemont.qwizup

import com.crosemont.qwizup.sourceDeDonnees.SourceFavorisLocale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class SourceFavorisLocaleTest {

    private lateinit var fichier: File

    @Before
    fun préparer() {
        // Un fichier temporaire qui n'existe pas encore, comme au premier lancement de l'app
        fichier = File.createTempFile("favoris", ".txt")
        fichier.delete()
    }

    @After
    fun nettoyer() {
        fichier.delete()
    }

    @Test
    fun `Étant donné qu'aucun fichier n'existe lorsqu'on lit les favoris, on obtient un ensemble vide`() {
        val source = SourceFavorisLocale(fichier)

        assertTrue(source.lireFavoris().isEmpty())
    }

    @Test
    fun `Étant donné que des favoris sont écrits lorsqu'on les relit, on obtient les mêmes titres`() {
        val source = SourceFavorisLocale(fichier)

        source.écrireFavoris(setOf("Le système solaire", "La Révolution tranquille"))

        assertEquals(setOf("Le système solaire", "La Révolution tranquille"), source.lireFavoris())
    }

    @Test
    fun `Étant donné que des favoris sont écrits lorsqu'on relit avec une nouvelle source, on obtient les mêmes titres`() {
        SourceFavorisLocale(fichier).écrireFavoris(setOf("La Révolution tranquille"))

        // Une nouvelle source sur le même fichier, comme quand on ferme et qu'on rouvre l'app
        val sourceAprèsRedémarrage = SourceFavorisLocale(fichier)

        assertEquals(setOf("La Révolution tranquille"), sourceAprèsRedémarrage.lireFavoris())
    }

    @Test
    fun `Étant donné que d'anciens favoris existent lorsqu'on en écrit de nouveaux, on obtient seulement les nouveaux`() {
        val source = SourceFavorisLocale(fichier)
        source.écrireFavoris(setOf("Le système solaire"))

        source.écrireFavoris(setOf("Verbes irréguliers anglais"))

        assertEquals(setOf("Verbes irréguliers anglais"), source.lireFavoris())
    }

    @Test
    fun `Étant donné qu'un favori existe lorsqu'on écrit un ensemble vide, on obtient un ensemble vide`() {
        val source = SourceFavorisLocale(fichier)
        source.écrireFavoris(setOf("Le système solaire"))

        source.écrireFavoris(emptySet())

        assertTrue(source.lireFavoris().isEmpty())
    }
}
