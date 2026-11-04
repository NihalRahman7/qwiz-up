package com.crosemont.qwizup.presentation.rejoindre

import com.crosemont.qwizup.ModeleQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.IPresentateurRejoindre
import com.crosemont.qwizup.presentation.ContratVuePresentateur.IVueRejoindre

class PresentateurRejoindre(
    private val modele: ModeleQuiz = ModeleQuiz()
) : IPresentateurRejoindre {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueRejoindre? = null

    override fun attacherVue(vue: IVueRejoindre) {
        this.vue = vue
        // Au départ, le champ est vide : le bouton Rejoindre est désactivé
        vue.activerRejoindre(false)
    }

    override fun detacherVue() {
        vue = null
    }

    // Un code valide contient exactement 6 chiffres. //
    private fun estFormatValide(code: String): Boolean =
        code.length == 6 && code.all { it.isDigit() }

    // Le texte du champ a changé : bouton actif seulement avec 6 chiffres. //
    override fun surCodeModifie(code: String) {
        vue?.effacerErreur()
        vue?.activerRejoindre(estFormatValide(code))
    }

    // L'utilisateur a touché Rejoindre : on cherche le quiz qui correspond au code. //
    override fun surRejoindreClique(code: String) {
        ouvrirQuizDuCode(code.trim())
    }

    // L'utilisateur veut scanner : on demande à la vue d'ouvrir la caméra. //
    override fun surScannerClique() {
        vue?.ouvrirScanneur()
    }

    // La caméra a lu un code QR : on garde seulement les chiffres, puis on cherche le quiz. //
    override fun surCodeScanne(contenu: String) {
        ouvrirQuizDuCode(contenu.filter { it.isDigit() })
    }

    override fun surRetourClique() {
        vue?.fermer()
    }

    // Ouvre le quiz si le code existe, sinon affiche l'erreur. Sert au code tapé ET au code scanné. //
    private fun ouvrirQuizDuCode(code: String) {
        val quizId = if (estFormatValide(code)) modele.trouverQuizParCode(code) else null

        if (quizId == null) {
            vue?.afficherCodeInvalide()
        } else {
            vue?.ouvrirQuiz(quizId)
        }
    }
}