package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.domaine.entites.Profil
import java.io.File

/**
 * Source "locale" : le profil est sauvegardé dans un fichier sur l'appareil
 * (nom affiché, nom d'utilisateur et courriel, un par ligne),
 * et la photo dans un autre fichier. Ils restent là quand on ferme l'application.
 */
class SourceProfilLocale(
    private val fichierProfil: File,
    private val fichierPhoto: File
) : SourceProfil {

    override fun lireProfil(): Profil? {
        if (!fichierProfil.exists()) return null   // Aucun profil n'a encore été créé
        val lignes = fichierProfil.readLines()
        if (lignes.size < 3) return null
        return Profil(lignes[0], lignes[1], lignes[2])
    }

    override fun écrireProfil(profil: Profil) {
        fichierProfil.writeText(
            listOf(profil.nomAffiché, profil.nomUtilisateur, profil.courriel).joinToString("\n")
        )
    }

    override fun lirePhoto(): ByteArray? =
        if (fichierPhoto.exists()) fichierPhoto.readBytes() else null

    override fun écrirePhoto(photo: ByteArray?) {
        if (photo == null) fichierPhoto.delete() else fichierPhoto.writeBytes(photo)
    }

    override fun effacer() {
        fichierProfil.delete()
        fichierPhoto.delete()
    }

    companion object {
        const val NOM_FICHIER_PROFIL = "profil.txt"
        const val NOM_FICHIER_PHOTO = "photo_profil.jpg"
    }
}