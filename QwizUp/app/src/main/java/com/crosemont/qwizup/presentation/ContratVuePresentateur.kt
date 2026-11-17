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
        fun afficherQuizBientot(titre: String)   // le quiz n'a pas encore de questions
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
    enum class AccesQuiz { TOUT_LE_MONDE, AVEC_CODE }
    // Partagé : utilisé aussi par l'écran Question
    enum class TypeQuestion { CHOIX_MULTIPLE, VRAI_FAUX, REPONSE_ECRITE }
    enum class ErreurEditeur { TITRE_VIDE, AUCUNE_QUESTION, QUESTION_INCOMPLETE }

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
            bonnesReponses: Set<Int>,      // choix multiple : une ou plusieurs de 0 à 3 ; vrai ou faux : {0} = vrai, {1} = faux
            reponseAttendue: String        // réponse écrite seulement
        )
        fun afficherBonnesReponses(index: Int, bonnesReponses: Set<Int>)
        fun afficherNombreQuestions(nombre: Int)
        fun afficherErreur(erreur: ErreurEditeur)
        fun afficherErreurQuestion(numero: Int)
        fun effacerErreurs()
        fun ouvrirAppareilPhoto()
        fun ouvrirGalerie()
        fun confirmerSauvegarde()
        fun confirmerPublication(titre: String)
        fun demanderConfirmationSuppression(index: Int)
        fun afficherPublication(estPublie: Boolean)   // coche ou décoche la case « Publier »
        /**
         * Texte du bouton selon la case, bouton gris s'il n'est pas actif,
         * et message sous le bouton pour dire ce qui manque (raison = null : aucun message).
         */
        fun afficherBoutonEnregistrer(publier: Boolean, actif: Boolean, raison: ErreurEditeur?, numeroQuestion: Int)
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
        fun surPublicationCochee(cochee: Boolean)
        fun surEnregistrerClique()
        fun surSuppressionConfirmee(index: Int)
        fun surFermerClique()
        fun surQuitterConfirme()
    }
}