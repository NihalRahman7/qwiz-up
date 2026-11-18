package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.sourceDeDonnees.SourceFavoris
import com.crosemont.qwizup.sourceDeDonnees.SourceFavorisBidon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ServiceFavorisTest {

    private class SourceEnPanne(private val peutLire: Boolean = false) : SourceFavoris {
        override fun lireFavoris(): Set<String> {
            if (!peutLire) throw IOException("Fichier illisible")
            return emptySet()
        }

        override fun écrireFavoris(titres: Set<String>) {
            throw IOException("Fichier protégé")
        }
    }

    @Test
    fun `Étant donné qu'aucun favori n'existe lorsqu'on demande les favoris, on obtient un ensemble vide`() {
        val service = ServiceFavoris(SourceFavorisBidon())

        assertTrue(service.obtenirFavoris().isEmpty())
    }

    @Test
    fun `Étant donné qu'aucun favori n'existe lorsqu'on bascule un quiz, on obtient ce quiz dans les favoris`() {
        val service = ServiceFavoris(SourceFavorisBidon())

        service.basculerFavori("Le système solaire")

        assertEquals(setOf("Le système solaire"), service.obtenirFavoris())
    }

    @Test
    fun `Étant donné qu'un quiz est favori lorsqu'on le bascule, on obtient les favoris sans ce quiz`() {
        val service = ServiceFavoris(SourceFavorisBidon(setOf("Le système solaire")))

        service.basculerFavori("Le système solaire")

        assertTrue(service.obtenirFavoris().isEmpty())
    }

    @Test
    fun `Étant donné que deux favoris existent lorsqu'on en bascule un, on obtient l'autre encore favori`() {
        val service =
            ServiceFavoris(SourceFavorisBidon(setOf("Le système solaire", "Bases de Kotlin")))

        service.basculerFavori("Le système solaire")

        assertEquals(setOf("Bases de Kotlin"), service.obtenirFavoris())
    }

    @Test(expected = ServiceException::class)
    fun `Étant donné qu'un titre est vide lorsqu'on bascule un favori, on obtient une ServiceException`() {
        val service = ServiceFavoris(SourceFavorisBidon())

        service.basculerFavori("   ")
    }

    @Test(expected = ServiceException::class)
    fun `Étant donné qu'une source est illisible lorsqu'on demande les favoris, on obtient une ServiceException`() {
        val service = ServiceFavoris(SourceEnPanne())

        service.obtenirFavoris()
    }

    @Test(expected = ServiceException::class)
    fun `Étant donné qu'une source est protégée lorsqu'on bascule un quiz, on obtient une ServiceException`() {
        val service = ServiceFavoris(SourceEnPanne(peutLire = true))

        service.basculerFavori("Le système solaire")
    }
}
