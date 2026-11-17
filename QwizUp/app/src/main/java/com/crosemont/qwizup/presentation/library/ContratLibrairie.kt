package com.crosemont.qwizup.presentation.library

import com.crosemont.qwizup.presentation.ContratVuePresentateur.Presentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Vue
import com.crosemont.qwizup.presentation.ModeleQuiz

interface ContratLibrairie {
    enum class OngletLibrairie { FAVORIS, COMPLETES, CREES }
    enum class TypeBadge { PROGRESSION, NOUVEAU, SCORE }

    interface IVueLibrary: Vue {
        fun viderCartes()
        fun ajouterCarteQuiz(
            id: Int,
            titre: String,
            sousTitre: String,
            badge: String?,
            typeBadge: TypeBadge
        )
        fun afficherMessageVide(message: String)
        fun ouvrirDetails(quizId: Int, titre: String)
    }


    interface IPresentateurLibrary: Presentateur<IVueLibrary>{
        fun surOngletClique(onglet: OngletLibrairie)
        fun surQuizClique(quizId: Int, titre: String)
    }

    interface IModeleLibrairie {
        suspend fun obtenirQuiz(onglet: OngletLibrairie): List<ModeleQuiz.CarteLibrary>
    }
}