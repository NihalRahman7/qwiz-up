package com.crosemont.qwizup.presentation.profil

import com.crosemont.qwizup.domaine.entites.Profil
import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue

// Contrat de l'écran Profil et de sa feuille « Modifier mon profil »
interface ContratProfil {

    interface IVueProfil : Vue {
        // Affiche le nom, le @nom d'utilisateur et le courriel
        fun afficherProfil(nomAffiché: String, nomUtilisateur: String, courriel: String)

        // Aucun profil : affiche « Invité »
        fun afficherInvité()

        // Affiche la photo, ou l'initiale si null
        fun afficherPhoto(photo: ByteArray?)

        // Affiche la série, le nombre de quiz complétés et le taux de réussite
        fun afficherStatistiques(série: Int, nbQuizComplétés: Int, tauxRéussite: Int)

        // Ouvre la feuille « Modifier mon profil » avec le nom actuel
        fun ouvrirModification(nomAffiché: String)

        // Ferme la feuille « Modifier mon profil »
        fun fermerModification()

        // Le nom tapé dans la feuille n'est pas valide (vide ou trop long)
        fun afficherNomInvalide()

        fun ouvrirMesQuiz()
        fun ouvrirLangue()
        fun ouvrirÀPropos()

        // Après la déconnexion : retour à Bienvenue
        fun ouvrirBienvenue()

        fun fermer()

        // Le modèle n'a pas pu répondre
        fun afficherErreur()
    }

    interface IPrésentateurProfil : Présentateur<IVueProfil> {
        // « Modifier » ou le bouton caméra
        fun surModifierClique()

        // Dans la feuille : Enregistrer avec ce nom
        fun surNomEnregistré(nom: String)

        // Dans la feuille : une photo a été prise ou choisie
        fun surPhotoChoisie(photo: ByteArray)

        // Dans la feuille : Retirer la photo
        fun surPhotoRetirée()

        fun surMesQuizClique()
        fun surLangueClique()
        fun surÀProposClique()
        fun surDéconnexionClique()
        fun surRetourClique()
    }

    interface IModèleProfil {
        suspend fun obtenirProfil(): Profil?
        suspend fun obtenirPhoto(): ByteArray?
        suspend fun obtenirStatistiques(): Statistiques
        suspend fun changerNomAffiché(nom: String)
        suspend fun changerPhoto(photo: ByteArray?)
        suspend fun déconnecter()
    }
}