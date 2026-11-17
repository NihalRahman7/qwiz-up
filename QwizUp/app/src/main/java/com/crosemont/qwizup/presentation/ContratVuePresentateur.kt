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

    // Partagés par les quiz créés (Créer) et l'écran de jeu (Question)
    enum class Difficulte { FACILE, MOYEN, DIFFICILE }
    enum class AccesQuiz { TOUT_LE_MONDE, AVEC_CODE }
    enum class TypeQuestion { CHOIX_MULTIPLE, VRAI_FAUX, REPONSE_ECRITE }
}
