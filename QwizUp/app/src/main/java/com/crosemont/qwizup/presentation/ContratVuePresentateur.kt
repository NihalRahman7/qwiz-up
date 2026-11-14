package com.crosemont.qwizup.presentation

interface ContratVuePresentateur {

    // Base commune
    interface Vue
    interface Presentateur<V : Vue> {
        fun attacherVue(vue: V)
        fun detacherVue()
    }

    // Home
    interface IVueHome : Vue {
        /** Ouvre l'écran de jeu pour le quiz choisi. */
        fun ouvrirQuiz(quizId: Int)

        /** Ouvre l'écran pour rejoindre un quiz avec un code. */
        fun ouvrirRejoindre()
    }

    interface IPresentateurHome : Presentateur<IVueHome> {
        /** L'utilisateur a touché un bouton ▶ ou la carte de suggestion. */
        fun surQuizClique(quizId: Int)

        /** L'utilisateur a touché le bouton « Code ». */
        fun surCodeClique()
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
    }

    // Question (écran de jeu, s'ouvre avec le bouton ▶ de l'Accueil)
    enum class NiveauResultat { EXCELLENT, BIEN, A_PRATIQUER }

    interface IVueQuestion : Vue {
        /** Affiche une question et ses réponses (réponses vides pour vrai ou faux et réponse écrite). */
        fun afficherQuestion(
            titreQuiz: String,
            numero: Int,
            total: Int,
            enonce: String,
            type: TypeQuestion,
            reponses: List<String>
        )

        /** Met à jour la barre de progression (0 à 100). */
        fun afficherProgression(pourcentage: Int)

        /** Colore la réponse choisie en vert. */
        fun afficherBonneReponse(indice: Int)

        /** Colore la réponse choisie en orange et la bonne réponse en vert. */
        fun afficherMauvaiseReponse(indiceChoisi: Int, indiceBonneReponse: Int)

        /** Active le bouton Suivant ; à la dernière question, il devient « Voir mon résultat ». */
        fun activerSuivant(estDerniere: Boolean)

        /** Affiche l'écran de fin avec le score. */
        fun afficherResultat(titreQuiz: String, score: Int, total: Int, niveau: NiveauResultat)

        /** Retourne à l'écran précédent. */
        fun fermer()
    }

    interface IPresentateurQuestion : Presentateur<IVueQuestion> {
        fun surDemarrer(quizId: Int)
        fun surReponseChoisie(indice: Int)   // choix multiple : 0 à 3 ; vrai ou faux : 0 = vrai, 1 = faux
        fun surSuivantClique()
        fun surRejouerClique()
        fun surQuitterClique()
    }

    // Rejoindre (s'ouvre avec le bouton « Code » de l'Accueil)
    interface IVueRejoindre : Vue {
        /** Active ou désactive le bouton Rejoindre (actif seulement avec 6 chiffres). */
        fun activerRejoindre(actif: Boolean)

        /** Affiche un message d'erreur sous le champ : le code n'existe pas. */
        fun afficherCodeInvalide()

        /** Retire le message d'erreur. */
        fun effacerErreur()

        /** Ouvre la caméra pour scanner un code QR. */
        fun ouvrirScanneur()

        /** Ouvre l'écran de jeu pour le quiz trouvé. */
        fun ouvrirQuiz(quizId: Int)

        /** Retourne à l'écran précédent. */
        fun fermer()
    }

    interface IPresentateurRejoindre : Presentateur<IVueRejoindre> {
        fun surCodeModifie(code: String)
        fun surRejoindreClique(code: String)
        fun surScannerClique()
        fun surCodeScanne(contenu: String)
        fun surRetourClique()
    }
}