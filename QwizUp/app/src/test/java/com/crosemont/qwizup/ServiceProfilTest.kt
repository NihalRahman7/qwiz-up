package com.crosemont.qwizup

import com.crosemont.qwizup.accèsAuxDonnées.SourceProfil
import com.crosemont.qwizup.accèsAuxDonnées.SourceProfilBidon
import com.crosemont.qwizup.domaine.entites.Profil
import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.domaine.service.ServiceProfil
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ServiceProfilTest {

    // Source qui ne marche pas : comme un fichier illisible ou protégé
    private class SourceEnPanne : SourceProfil {
        override fun lireProfil(): Profil? = throw IOException("Fichier illisible")
        override fun écrireProfil(profil: Profil) = throw IOException("Fichier protégé")
        override fun lirePhoto(): ByteArray? = throw IOException("Fichier illisible")
        override fun écrirePhoto(photo: ByteArray?) = throw IOException("Fichier protégé")
        override fun effacer() = throw IOException("Fichier protégé")
    }

    private val maya = Profil("Maya", "maya_t", "maya@exemple.ca")

    private lateinit var source: SourceProfilBidon
    private lateinit var service: ServiceProfil

    @Before
    fun préparer() {
        source = SourceProfilBidon()
        service = ServiceProfil(source)
    }

    @Test
    fun `Étant donné qu'aucun profil n'existe lorsqu'on demande le profil, on obtient null`() {
        assertNull(service.obtenirProfil())
    }

    @Test
    fun `Étant donné une inscription lorsqu'on crée le profil, on obtient le nom d'utilisateur comme nom affiché`() {
        service.créerProfil("maya_t", "maya@exemple.ca")

        assertEquals(Profil("maya_t", "maya_t", "maya@exemple.ca"), service.obtenirProfil())
    }

    @Test
    fun `Étant donné un profil lorsqu'on change le nom affiché, on obtient le nouveau nom sans espaces autour`() {
        source.écrireProfil(maya)

        service.changerNomAffiché("  Maya T.  ")

        assertEquals("Maya T.", service.obtenirProfil()?.nomAffiché)
    }

    @Test
    fun `Étant donné un profil lorsqu'on change le nom pour un nom vide, on obtient une ServiceException et le nom ne change pas`() {
        source.écrireProfil(maya)

        try {
            service.changerNomAffiché("   ")
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            assertEquals("Maya", source.lireProfil()?.nomAffiché)
        }
    }

    @Test
    fun `Étant donné un nom de plus de 30 caractères lorsqu'on le change, on obtient une ServiceException`() {
        source.écrireProfil(maya)

        try {
            service.changerNomAffiché("a".repeat(31))
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            assertEquals("Maya", source.lireProfil()?.nomAffiché)
        }
    }

    @Test
    fun `Étant donné une nouvelle photo lorsqu'on la change, on obtient cette photo`() {
        val photo = byteArrayOf(9, 8, 7)

        service.changerPhoto(photo)

        assertArrayEquals(photo, service.obtenirPhoto())
    }

    @Test
    fun `Étant donné un profil avec photo lorsqu'on se déconnecte, on obtient ni profil ni photo`() {
        source.écrireProfil(maya)
        source.écrirePhoto(byteArrayOf(1, 2, 3))

        service.déconnecter()

        assertNull(service.obtenirProfil())
        assertNull(service.obtenirPhoto())
    }

    @Test(expected = ServiceException::class)
    fun `Étant donné une source illisible lorsqu'on demande le profil, on obtient une ServiceException`() {
        ServiceProfil(SourceEnPanne()).obtenirProfil()
    }

    @Test(expected = ServiceException::class)
    fun `Étant donné une source protégée lorsqu'on change la photo, on obtient une ServiceException`() {
        ServiceProfil(SourceEnPanne()).changerPhoto(byteArrayOf(1))
    }

    @Test
    fun `Étant donné aucun profil lorsqu'on se connecte, on obtient un profil à ce nom sans courriel`() {
        service.connecter("maya_t")

        assertEquals(Profil("maya_t", "maya_t", ""), service.obtenirProfil())
    }

    @Test
    fun `Étant donné le profil de maya_t lorsqu'elle se reconnecte, on obtient son profil gardé tel quel`() {
        source.écrireProfil(maya)

        service.connecter("maya_t")

        assertEquals(maya, service.obtenirProfil())
    }

    @Test
    fun `Étant donné un nom vide lorsqu'on se connecte, on obtient une ServiceException et aucun profil`() {
        try {
            service.connecter("  ")
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            assertNull(source.lireProfil())
        }
    }
}