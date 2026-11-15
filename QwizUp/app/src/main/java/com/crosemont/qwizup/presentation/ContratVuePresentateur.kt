package com.crosemont.qwizup.presentation

interface ContratVuePresentateur {

    // Base commune
    interface Vue
    interface Presentateur<V : Vue> {
        fun attacherVue(vue: V)
        fun detacherVue()
    }

    // Catégories de quiz (utilisées par Découvrir et l'éditeur de Créer)
    enum class CategorieQuiz { SCIENCES, HISTOIRE, LANGUES, GEOGRAPHIE, INFORMATIQUE, AUTRE }

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
    enum class AccesQuiz { TOUT_LE_MONDE, AVEC_CODE }
    enum class TypeQuestion { CHOIX_MULTIPLE, VRAI_FAUX, REPONSE_ECRITE }
    enum class ErreurEditeur { TITRE_VIDE, AUCUNE_QUESTION }

    interface IVueEditeur : Vue {
        fun afficherEnTete(nouveauQuiz: Boolean)
        fun afficherInfosQuiz(titre: String, description: String)
        fun afficherCategorieChoisie(categorie: CategorieQuiz)
        fun afficherCouverture(categorie: CategorieQuiz)   // image automatique selon la catégorie
        fun afficherDifficulteChoisie(difficulte: Difficulte)
        fun afficherAccesChoisi(acces: AccesQuiz)
        fun viderQuestions()
        fun ajouterQuestion(
            index: Int,
            texte: String,
            type: TypeQuestion,
            reponses: List<String>,        // 4 réponses (choix multiple seulement)
            bonneReponse: Int,             // choix multiple : 0 à 3 ; vrai ou faux : 0 = vrai, 1 = faux
            reponseAttendue: String        // réponse écrite seulement
        )
        fun afficherBonneReponse(index: Int, bonneReponse: Int)
        fun afficherNombreQuestions(nombre: Int)
        fun afficherErreur(erreur: ErreurEditeur)
        fun afficherErreurQuestion(numero: Int)
        fun effacerErreurs()
        fun ouvrirAppareilPhoto()
        fun ouvrirGalerie()
        fun confirmerSauvegarde()
        fun confirmerPublication(titre: String)
        fun demanderConfirmationSuppression(index: Int)
        fun demanderConfirmationQuitter()
        fun fermer()
    }

    interface IPresentateurEditeur : Presentateur<IVueEditeur> {
        fun chargerQuiz(quizId: Int?)   // null = nouveau quiz
        fun surTitreModifie(titre: String)
        fun surDescriptionModifiee(description: String)
        fun surCategorieCliquee(categorie: CategorieQuiz)
        fun surDifficulteCliquee(difficulte: Difficulte)
        fun surAccesClique(acces: AccesQuiz)
        fun surPrendrePhotoClique()
        fun surGalerieClique()
        fun surAjouterQuestionClique()
        fun surSupprimerQuestionClique(index: Int)
        fun surTypeQuestionClique(index: Int, type: TypeQuestion)
        fun surTexteQuestionModifie(index: Int, texte: String)
        fun surReponseModifiee(index: Int, numeroReponse: Int, texte: String)
        fun surBonneReponseCliquee(index: Int, numeroReponse: Int)
        fun surReponseAttendueModifiee(index: Int, texte: String)
        fun surBrouillonClique()
        fun surPublierClique()
        fun surSuppressionConfirmee(index: Int)
        fun surFermerClique()
        fun surQuitterConfirme()
    }

}