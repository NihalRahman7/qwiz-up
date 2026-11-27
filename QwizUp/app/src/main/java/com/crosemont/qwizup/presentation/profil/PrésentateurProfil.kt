package com.crosemont.qwizup.presentation.profil

import com.crosemont.qwizup.presentation.profil.ContratProfil.IModèleProfil
import com.crosemont.qwizup.presentation.profil.ContratProfil.IPrésentateurProfil
import com.crosemont.qwizup.presentation.profil.ContratProfil.IVueProfil
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class PrésentateurProfil(
    private val modèle: IModèleProfil,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,
) : IPrésentateurProfil {

    override fun attacherVue(vue: IVueProfil) {}
    override fun detacherVue() {}
    override fun surModifierClique() {}
    override fun surNomEnregistré(nom: String) {}
    override fun surPhotoChoisie(photo: ByteArray) {}
    override fun surPhotoRetirée() {}
    override fun surMesQuizClique() {}
    override fun surLangueClique() {}
    override fun surÀProposClique() {}
    override fun surDéconnexionClique() {}
    override fun surRetourClique() {}
}