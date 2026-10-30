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
    // TODO: Faire le contrat qui gère le DiscoverFragment

    // Create
    enum class SectionCreer { BROUILLONS, PUBLIES }

    interface VueCreate : Vue {
        fun viderSection(section: SectionCreer)
        fun ajouterCarteQuiz(section: SectionCreer, id: Int, titre: String)
        fun afficherMessageVide(section: SectionCreer, message: String)
        fun basculerSection(section: SectionCreer, ouverte: Boolean)
        fun ouvrirEditeur(quizId: Int?)   // null = nouveau quiz
        fun ouvrirTousMesQuiz()
    }

    interface PresentateurCreate : Presentateur<VueCreate> {
        fun surNouveauQuizClique()
        fun surContinuerClique(quizId: Int)
        fun surDeroulerClique(section: SectionCreer)
        fun surVoirToutClique()
    }

    // Library
    enum class OngletLibrairie { FAVORIS, COMPLETES, CREES }
    enum class TypeBadge { PROGRESSION, NOUVEAU, SCORE }

    interface VueLibrary: Vue {
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

    interface PresentateurLibrary: Presentateur<VueLibrary>{
        fun surOngletClique(onglet: OngletLibrairie)
        fun surQuizClique(quizId: Int)
    }
}