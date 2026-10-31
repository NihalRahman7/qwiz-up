package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.ModeleQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.SectionCreer

class PresentateurCreate(
    var modeleQuiz: ModeleQuiz = ModeleQuiz(),
) : ContratVuePresentateur.IPresentateurCreate {

    private var vue: ContratVuePresentateur.IVueCreate? = null
    private val sectionsOuvertes = mutableMapOf(
        SectionCreer.BROUILLONS to true,
        SectionCreer.PUBLIES to true
    )

    override fun attacherVue(vue: ContratVuePresentateur.IVueCreate) {
        this.vue = vue
        afficherSection(SectionCreer.BROUILLONS)
        afficherSection(SectionCreer.PUBLIES)
    }

    override fun detacherVue() {
        vue = null
    }

    private fun afficherSection(section: SectionCreer) {
        val (cartes, messageVide) = when (section) {
            SectionCreer.BROUILLONS -> modeleQuiz.obtenirBrouillons() to "Aucun brouillon pour l'instant"
            SectionCreer.PUBLIES -> modeleQuiz.obtenirPublies() to "Aucun quiz publié pour l'instant"
        }

        vue?.viderSection(section)
        if (cartes.isEmpty()) {
            vue?.afficherMessageVide(section, messageVide)
        } else {
            cartes.forEach { vue?.ajouterCarteQuiz(section, it.id, it.titre) }
        }
        vue?.basculerSection(section, sectionsOuvertes.getValue(section))
    }

    override fun surNouveauQuizClique() {
        vue?.ouvrirEditeur(null)
    }

    override fun surContinuerClique(quizId: Int) {
        vue?.ouvrirEditeur(quizId)
    }

    override fun surDeroulerClique(section: SectionCreer) {
        val ouverte = !sectionsOuvertes.getValue(section)
        sectionsOuvertes[section] = ouverte
        vue?.basculerSection(section, ouverte)
    }

    override fun surVoirToutClique() {
        vue?.ouvrirTousMesQuiz()
    }
}
