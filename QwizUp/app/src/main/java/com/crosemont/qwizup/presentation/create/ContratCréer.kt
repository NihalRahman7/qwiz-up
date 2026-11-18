package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.AccèsQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Difficulté
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue
import com.crosemont.qwizup.presentation.ModèleQuiz.CarteCreate
import com.crosemont.qwizup.presentation.ModèleQuiz.QuizCréer

/** Contrat de l'écran Créer (liste des quiz) et de l'éditeur de quiz. */
interface ContratCréer {

    enum class OngletCreer { BROUILLONS, PUBLIES }
    enum class ErreurEditeur { TITRE_VIDE, CATEGORIE_VIDE, AUCUNE_QUESTION, QUESTION_INCOMPLETE }

    // ----- Écran Créer : les brouillons et les quiz publiés -----

    interface IVueCréer : Vue {
        fun afficherCompteurs(nbBrouillons: Int, nbPublies: Int)
        fun afficherOngletChoisi(onglet: OngletCreer)
        fun viderCartes()
        fun ajouterCarteQuiz(id: Int, titre: String, nbQuestions: Int, estPublié: Boolean)
        fun afficherListeVide(onglet: OngletCreer)
        fun ouvrirEditeur(quizId: Int?)   // null = nouveau quiz
        fun afficherErreurChargement()    // le modèle n'a pas pu répondre
    }

    interface IPrésentateurCréer : Présentateur<IVueCréer> {
        fun surNouveauQuizClique()
        fun surOngletClique(onglet: OngletCreer)
        fun surQuizClique(quizId: Int)   // « Continuer » ou « Modifier »
    }

    // ----- Éditeur de quiz (s'ouvre depuis Créer avec « Nouveau quiz » ou « Continuer ») -----

    interface IVueÉditeur : Vue {
        fun afficherEnTete(nouveauQuiz: Boolean)
        fun afficherInfosQuiz(titre: String, description: String)
        fun afficherCategorieChoisie(catégorie: CatégorieQuiz)
        fun afficherCouverture(catégorie: CatégorieQuiz)   // image automatique selon la catégorie
        fun afficherCategoriePerso(visible: Boolean, nom: String)   // champ « Précise la catégorie » (Autre)
        fun afficherDifficulteChoisie(difficulté: Difficulté)
        fun afficherAccesChoisi(accès: AccèsQuiz)
        fun viderQuestions()
        fun ajouterQuestion(
            index: Int,
            texte: String,
            type: TypeQuestion,
            réponses: List<String>,        // 4 réponses (choix multiple seulement)
            bonnesRéponses: Set<Int>,      // choix multiple : une ou plusieurs de 0 à 3 ; vrai ou faux : {0} = vrai, {1} = faux
            réponseAttendue: String        // réponse écrite seulement
        )
        fun afficherBonnesReponses(index: Int, bonnesRéponses: Set<Int>)
        fun afficherNombreQuestions(nombre: Int)
        fun afficherErreur(erreur: ErreurEditeur)
        fun afficherErreurQuestion(numero: Int)
        fun effacerErreurs()
        fun confirmerSauvegarde()
        fun confirmerPublication(titre: String)
        fun demanderConfirmationSuppression(index: Int)
        fun afficherPublication(estPublié: Boolean)   // coche ou décoche la case « Publier »
        /**
         * Texte du bouton selon la case, bouton gris s'il n'est pas actif,
         * et message sous le bouton pour dire ce qui manque (raison = null : aucun message).
         */
        fun afficherBoutonEnregistrer(publier: Boolean, actif: Boolean, raison: ErreurEditeur?, numeroQuestion: Int)
        /**
         * Dialogue « Quitter sans sauvegarder? ». Le bouton pour enregistrer apparaît seulement
         * si le quiz peut être enregistré, et il dit ce qu'il va faire (publier ou brouillon).
         */
        fun demanderConfirmationQuitter(peutEnregistrer: Boolean, publier: Boolean)
        fun fermer()
        fun afficherErreurChargement()   // le quiz n'a pas pu être ouvert
        fun afficherErreurSauvegarde()   // le quiz n'a pas pu être enregistré
    }

    interface IPrésentateurÉditeur : Présentateur<IVueÉditeur> {
        fun chargerQuiz(quizId: Int?)   // null = nouveau quiz
        fun surTitreModifie(titre: String)
        fun surDescriptionModifiee(description: String)
        fun surCategorieCliquee(catégorie: CatégorieQuiz)
        fun surCategoriePersoModifiee(nom: String)
        fun surDifficulteCliquee(difficulté: Difficulté)
        fun surAccesClique(accès: AccèsQuiz)
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
        suspend fun obtenirQuiz(quizId: Int): QuizCréer
        suspend fun sauvegarderQuiz(quiz: QuizCréer): Int   // retourne l'id du quiz enregistré
    }
}
