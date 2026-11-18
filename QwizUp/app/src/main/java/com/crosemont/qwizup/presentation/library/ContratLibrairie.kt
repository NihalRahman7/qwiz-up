package com.crosemont.qwizup.presentation.library

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue
import com.crosemont.qwizup.presentation.ModèleQuiz

interface ContratLibrairie {
    enum class OngletLibrairie { FAVORIS, COMPLÉTÉS, CRÉES }
    enum class TypeBadge { PROGRESSION, NOUVEAU, SCORE }

    interface IVueLibrary : Vue {
        fun viderCartes()
        fun ajouterCarteQuiz(
            id: Int,
            titre: String,
            sousTitre: String,
            badge: String?,
            typeBadge: TypeBadge
        )

        fun afficherMessageVide(message: String)
        fun ouvrirDétails(quizId: Int, titre: String)
    }

    interface IPrésentateurLibrary : Présentateur<IVueLibrary> {
        fun surOngletClique(onglet: OngletLibrairie)
        fun surQuizClique(quizId: Int, titre: String)
    }

    interface IModèleLibrairie {
        suspend fun obtenirQuiz(onglet: OngletLibrairie): List<ModèleQuiz.CarteLibrary>
    }
}