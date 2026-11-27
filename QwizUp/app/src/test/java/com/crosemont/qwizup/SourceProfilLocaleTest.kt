package com.crosemont.qwizup

import com.crosemont.qwizup.accèsAuxDonnées.SourceProfilLocale
import com.crosemont.qwizup.domaine.entites.Profil
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

class SourceProfilLocaleTest {

    private lateinit var fichierProfil: File
    private lateinit var fichierPhoto: File

    private val maya = Profil("Maya", "maya_t", "maya@exemple.ca")

    @Before
    fun préparer() {
        // Des fichiers temporaires qui n'existent pas encore, comme au premier lancement de l'app
        fichierProfil = File.createTempFile("profil", ".txt")
        fichierPhoto = File.createTempFile("photo", ".jpg")
        fichierProfil.delete()
        fichierPhoto.delete()
    }

    @After
    fun nettoyer() {
        fichierProfil.delete()
        fichierPhoto.delete()
    }

    private fun nouvelleSource() = SourceProfilLocale(fichierProfil, fichierPhoto)

    @Test
    fun `Étant donné qu'aucun fichier n'existe lorsqu'on lit le profil, on obtient null`() {
        assertNull(nouvelleSource().lireProfil())
    }

    @Test
    fun `Étant donné un profil écrit lorsqu'on relit avec une nouvelle source, on obtient le même profil`() {
        nouvelleSource().écrireProfil(maya)

        // Une nouvelle source sur les mêmes fichiers, comme quand on ferme et qu'on rouvre l'app
        assertEquals(maya, nouvelleSource().lireProfil())
    }

    @Test
    fun `Étant donné qu'aucune photo n'existe lorsqu'on lit la photo, on obtient null`() {
        assertNull(nouvelleSource().lirePhoto())
    }

    @Test
    fun `Étant donné une photo écrite lorsqu'on la relit, on obtient les mêmes octets`() {
        val photo = byteArrayOf(1, 2, 3, 4)
        nouvelleSource().écrirePhoto(photo)

        assertArrayEquals(photo, nouvelleSource().lirePhoto())
    }

    @Test
    fun `Étant donné une photo lorsqu'on écrit null, on obtient plus de photo`() {
        val source = nouvelleSource()
        source.écrirePhoto(byteArrayOf(1, 2, 3))

        source.écrirePhoto(null)

        assertNull(source.lirePhoto())
    }

    @Test
    fun `Étant donné un profil et une photo lorsqu'on efface, on obtient ni profil ni photo`() {
        val source = nouvelleSource()
        source.écrireProfil(maya)
        source.écrirePhoto(byteArrayOf(1, 2, 3))

        source.effacer()

        assertNull(source.lireProfil())
        assertNull(source.lirePhoto())
    }
}