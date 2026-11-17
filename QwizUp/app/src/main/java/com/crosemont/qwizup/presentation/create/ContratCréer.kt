package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.presentation.ContratVuePresentateur.AccesQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Difficulte
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Presentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Vue
import com.crosemont.qwizup.presentation.ModeleQuiz.CarteCreate
import com.crosemont.qwizup.presentation.ModeleQuiz.QuizCreer

/** Contrat de l'écran Créer (liste des quiz) et de l'éditeur de quiz. */
interface ContratCréer {

    enum class OngletCreer { BROUILLONS, PUBLIES }
    enum class ErreurEditeur { TITRE_VIDE, CATEGORIE_VIDE, AUCUNE_QUESTION, QUESTION_INCOMPLETE }

    // ----- Écran Créer : les brouillons et les quiz publiés -----

    interface IVueCréer : Vue {
        fun afficherCompteurs(nbBrouillons: Int, nbPublies: Int)
        fun afficherOngletChoisi(onglet: OngletCreer)
        fun viderCartes()
        fun ajouterCarteQuiz(id: Int, titre: String, nbQuestions: Int, estPublie: Boolean)
        fun afficherListeVide(onglet: OngletCreer)
        fun ouvrirEditeur(quizId: Int?)   // null = nouveau quiz
        fun afficherErreurChargement()    // le modèle n'a pas pu répondre
    }

    interface IPrésentateurCréer : Presentateur<IVueCréer> {
        fun surNouveauQuizClique()
        fun surOngletClique(onglet: OngletCreer)
        fun surQuizClique(quizId: Int)   // « Continuer » ou « Modifier »
    }

    // ----- Éditeur de quiz (s'ouvre depuis Créer avec « Nouveau quiz » ou « Continuer ») -----

    interface IVueÉditeur : Vue {
        fun afficherEnTete(nouveauQuiz: Boolean)
        fun afficherInfosQuiz(titre: String, description: String)
        fun afficherCategorieChoisie(categorie: CategorieQuiz)
        fun afficherCouverture(categorie: CategorieQuiz)   // image automatique selon la catégorie
        fun afficherCategoriePerso(visible: Boolean, nom: String)   // champ « Précise la catégorie » (Autre)
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
        fun afficherErreurChargement()   // le quiz n'a pas pu être ouvert
        fun afficherErreurSauvegarde()   // le quiz n'a pas pu être enregistré
    }

    interface IPrésentateurÉditeur : Presentateur<IVueÉditeur> {
        fun chargerQuiz(quizId: Int?)   // null = nouveau quiz
        fun surTitreModifie(titre: String)
        fun surDescriptionModifiee(description: String)
        fun surCategorieCliquee(categorie: CategorieQuiz)
        fun surCategoriePersoModifiee(nom: String)
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

    // ----- Modèle (partagé par l'écran Créer et l'éditeur) -----

    // Les fonctions sont « suspend » : elles peuvent prendre du temps (fichier, réseau),
    // donc les présentateurs les appellent dans une coroutine, sur un fil d'arrière-plan.
    interface IModèleCréer {
        suspend fun obtenirBrouillons(): List<CarteCreate>
        suspend fun obtenirPubliés(): List<CarteCreate>
        suspend fun obtenirQuiz(quizId: Int): QuizCreer
        suspend fun sauvegarderQuiz(quiz: QuizCreer): Int   // retourne l'id du quiz enregistré
    }
}
