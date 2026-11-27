package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.domaine.entites.Profil
import java.io.File

class SourceProfilLocale(
    private val fichierProfil: File,
    private val fichierPhoto: File
) : SourceProfil {

    override fun lireProfil(): Profil? = TODO()

    override fun écrireProfil(profil: Profil) = TODO()

    override fun lirePhoto(): ByteArray? = TODO()

    override fun écrirePhoto(photo: ByteArray?) = TODO()

    override fun effacer() = TODO()

    companion object {
        const val NOM_FICHIER_PROFIL = "profil.txt"
        const val NOM_FICHIER_PHOTO = "photo_profil.jpg"
    }
}