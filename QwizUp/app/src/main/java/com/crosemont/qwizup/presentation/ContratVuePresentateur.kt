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
    enum class OngletCreer { BROUILLONS, PUBLIES }

    interface IVueCreate : Vue {
        fun afficherCompteurs(nbBrouillons: Int, nbPublies: Int)
        fun afficherOngletChoisi(onglet: OngletCreer)
        fun viderCartes()
        fun ajouterCarteQuiz(id: Int, titre: String, nbQuestions: Int, estPublie: Boolean)
        fun afficherMessageVide(message: String)
        fun ouvrirEditeur(quizId: Int?)   // null = nouveau quiz
    }

    interface IPresentateurCreate : Presentateur<IVueCreate> {
        fun surNouveauQuizClique()
        fun surOngletClique(onglet: OngletCreer)
        fun surQuizClique(quizId: Int)   // « Continuer » ou « Modifier »
    }

    // Éditeur de quiz (s'ouvre depuis Create avec « Nouveau quiz » ou « Continuer »)
    enum class Difficulte { FACILE, MOYEN, DIFFICILE }
    enum class ErreurEditeur { TITRE_VIDE, AUCUNE_QUESTION }

    interface IVueEditeur : Vue {
        fun afficherEnTete(nouveauQuiz: Boolean, estPublie: Boolean)
        fun afficherInfosQuiz(titre: String, description: String)
        fun afficherCategorieChoisie(categorie: CategorieQuiz)
        fun afficherDifficulteChoisie(difficulte: Difficulte)
        fun viderQuestions()
        fun ajouterQuestion(numero: Int, texte: String, estVraiFaux: Boolean, secondes: Int, doublePoints: Boolean)
        fun afficherAucuneQuestion()
        fun afficherNombreQuestions(nombre: Int)
        fun afficherErreur(erreur: ErreurEditeur)
        fun effacerErreurs()
        fun ouvrirAppareilPhoto()
        fun ouvrirGalerie()
        fun ouvrirEditeurQuestion(numero: Int?)   // null = nouvelle question
        fun confirmerSauvegarde()
        fun confirmerPublication(titre: String)
    }

    interface IPresentateurEditeur : Presentateur<IVueEditeur> {
        fun chargerQuiz(quizId: Int?)   // null = nouveau quiz
        fun surTitreModifie(titre: String)
        fun surDescriptionModifiee(description: String)
        fun surCategorieCliquee(categorie: CategorieQuiz)
        fun surDifficulteCliquee(difficulte: Difficulte)
        fun surPrendrePhotoClique()
        fun surGalerieClique()
        fun surAjouterQuestionClique()
        fun surModifierQuestionClique(numero: Int)
        fun surSauvegarderClique()
        fun surPublierClique()
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