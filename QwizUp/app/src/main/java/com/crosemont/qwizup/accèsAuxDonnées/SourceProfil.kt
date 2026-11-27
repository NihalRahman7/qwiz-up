package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.domaine.entites.Profil

/**
 * Source de données du profil de l'utilisateur et de sa photo.
 */
interface SourceProfil {
    // Aucun profil n'a encore ete creer
    fun lireProfil(): Profil?

    fun écrireProfil(profil: Profil)

    // Aucune photo
    fun lirePhoto(): ByteArray?

    // Retirer la photo
    fun écrirePhoto(photo: ByteArray?)

    // Efface le profil et la photo (deconnexion)
    fun effacer()
}