package com.crosemont.qwizup.presentation

interface ContratVuePresentateur {

    // Base commune
    interface Vue
    interface Presentateur<V : Vue> {
        fun attacherVue(vue: V)
        fun detacherVue()
    }

    // Home
    // TODO: Faire le contrat qui gère le HomeFragment

    // Discover
    enum class CategorieQuiz { SCIENCES, HISTOIRE, LANGUES, GEOGRAPHIE }

    interface IVueDiscover : Vue {
        fun viderCartes()
        fun ajouterCarteQuiz(
            id: Int,
            categorie: CategorieQuiz,
            titre: String,
            description: String,
            nbQuestions: Int,
            dureeMinutes: Int,
            estFavori: Boolean
        )
        fun afficherAucunResultat()
        fun afficherCategorieChoisie(categorie: CategorieQuiz?)   // null = « Tous »
        fun ouvrirQuiz(quizId: Int, titre: String)
    }

    interface IPresentateurDiscover : Presentateur<IVueDiscover> {
        fun surRechercheModifiee(texte: String)
        fun surCategorieCliquee(categorie: CategorieQuiz?)   // null = « Tous »
        fun surFavoriClique(quizId: Int)
        fun surJouerClique(quizId: Int)
    }

    // Create
    enum class SectionCreer { BROUILLONS, PUBLIES }

    interface IVueCreate : Vue {
        fun viderSection(section: SectionCreer)
        fun ajouterCarteQuiz(section: SectionCreer, id: Int, titre: String)
        fun afficherMessageVide(section: SectionCreer, message: String)
        fun basculerSection(section: SectionCreer, ouverte: Boolean)
        fun ouvrirEditeur(quizId: Int?)   // null = nouveau quiz
        fun ouvrirTousMesQuiz()
    }

    interface IPresentateurCreate : Presentateur<IVueCreate> {
        fun surNouveauQuizClique()
        fun surContinuerClique(quizId: Int)
        fun surDeroulerClique(section: SectionCreer)
        fun surVoirToutClique()
    }

    // Library
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
        fun ouvrirDetails(quizId: Int)
    }

    interface IPresentateurLibrary: Presentateur<IVueLibrary>{
        fun surOngletClique(onglet: OngletLibrairie)
        fun surQuizClique(quizId: Int)
    }
}