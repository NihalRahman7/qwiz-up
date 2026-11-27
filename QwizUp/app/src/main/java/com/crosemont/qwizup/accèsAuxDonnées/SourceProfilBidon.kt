package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.domaine.entites.Profil

/** Source "bidon" : simule la vraie source en gardant le profil et la photo en mémoire. */
class SourceProfilBidon(
    private var profil: Profil? = null,
    private var photo: ByteArray? = null
) : SourceProfil {

    override fun lireProfil(): Profil? = profil

    override fun écrireProfil(profil: Profil) {
        this.profil = profil
    }

    override fun lirePhoto(): ByteArray? = photo

    override fun écrirePhoto(photo: ByteArray?) {
        this.photo = photo
    }

    override fun effacer() {
        profil = null
        photo = null
    }
}