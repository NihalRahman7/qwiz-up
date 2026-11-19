package com.crosemont.qwizup.presentation

interface ContratVuePrésentateur {

    // Base commune
    interface Vue
    interface Présentateur<V : Vue> {
        fun attacherVue(vue: V)
        fun detacherVue()
    }

    // Catégories de quiz (utilisées par Découvrir et l'éditeur de Créer)
    enum class CatégorieQuiz { SCIENCES, HISTOIRE, LANGUES, GÉOGRAPHIE, INFORMATIQUE, AUTRE }

    // Partagés par les quiz créés (Créer) et l'écran de jeu (Question)
    enum class Difficulté { FACILE, MOYEN, DIFFICILE }
    enum class AccèsQuiz { TOUT_LE_MONDE, AVEC_CODE }
    enum class TypeQuestion { CHOIX_MULTIPLE, VRAI_FAUX, REPONSE_ECRITE }
}
