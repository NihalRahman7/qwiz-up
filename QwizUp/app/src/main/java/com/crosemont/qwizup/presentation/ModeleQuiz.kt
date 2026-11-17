package com.crosemont.qwizup.presentation

import com.crosemont.qwizup.sourceDeDonnees.SourceQuizCréésBidon
import com.crosemont.qwizup.presentation.library.ContratLibrairie
import java.text.Normalizer

class ModeleQuiz {
    // Home
    // TODO: Faire le modèle avec des données codées en dur pour le fragment Home

    // Discover
    data class CarteDiscover(
        val id: Int,
        val categorie: ContratVuePresentateur.CategorieQuiz,
        val titre: String,
        val description: String,
        val nbQuestions: Int,
        val dureeMinutes: Int,
        val estFavori: Boolean = false
    )

    // Données codées en dur
    private val quizDiscover = mutableListOf(
        CarteDiscover(
            3, ContratVuePresentateur.CategorieQuiz.GEOGRAPHIE, "Capitales d’Europe",
            "Associe chaque pays à sa capitale, de Lisbonne à Helsinki.", 12, 6
        ),
        CarteDiscover(
            1, ContratVuePresentateur.CategorieQuiz.SCIENCES, "Le système solaire",
            "Planètes, lunes et distances : teste tes connaissances.", 10, 5, estFavori = true
        ),
        CarteDiscover(
            4, ContratVuePresentateur.CategorieQuiz.HISTOIRE, "La Révolution tranquille",
            "Les grands changements du Québec des années 1960.", 8, 4
        ),
        CarteDiscover(
            5, ContratVuePresentateur.CategorieQuiz.LANGUES, "Verbes irréguliers anglais",
            "Be, go, take… retrouve le passé des verbes les plus utilisés.", 15, 7
        )
    )

    /**
     * Retourne les quiz de la catégorie choisie (null = tous) dont le titre ou la description
     * contient le texte recherché. On ignore les majuscules et les accents.
     */
    fun obtenirQuizDiscover(
        categorie: ContratVuePresentateur.CategorieQuiz?,
        recherche: String
    ): List<CarteDiscover> {
        val texte = simplifier(recherche.trim())
        return quizDiscover.filter { quiz ->
            val bonneCategorie = categorie == null || quiz.categorie == categorie
            val contientTexte = texte.isEmpty() ||
                simplifier(quiz.titre).contains(texte) ||
                simplifier(quiz.description).contains(texte)
            bonneCategorie && contientTexte
        }
    }

    fun obtenirQuizDiscoverParId(quizId: Int): CarteDiscover? =
        quizDiscover.find { it.id == quizId }

    /** Ajoute le quiz aux favoris s'il n'y est pas, sinon le retire. */
    fun basculerFavoriDiscover(quizId: Int) {
        val index = quizDiscover.indexOfFirst { it.id == quizId }
        if (index != -1) {
            val quiz = quizDiscover[index]
            quizDiscover[index] = quiz.copy(estFavori = !quiz.estFavori)
        }
    }

    /** Met le texte en minuscules et enlève les accents (« Révolution » devient « revolution »). */
    private fun simplifier(texte: String): String =
        Normalizer.normalize(texte, Normalizer.Form.NFD)
            .filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }
            .lowercase()

    // Create
    data class CarteCreate(
        val id: Int,
        val titre: String,
        val estPublie: Boolean,  // false = brouillon, true = publié
        val nbQuestions: Int
    )

    data class QuestionCreer(
        val texte: String,
        val type: ContratVuePresentateur.TypeQuestion,
        val reponses: List<String> = listOf("", "", "", ""),   // choix multiple : A, B, C, D
        val bonnesReponses: Set<Int> = setOf(0),   // choix multiple : une ou plusieurs de 0 à 3 ; vrai ou faux : {0} = vrai, {1} = faux
        val reponseAttendue: String = ""    // réponse écrite
    ) {
        /** Une question est complète quand elle a un texte et une bonne réponse remplie. */
        fun estComplete(): Boolean = texte.isNotBlank() && when (type) {
            // Au moins une bonne réponse cochée, et chaque réponse cochée doit être remplie
            ContratVuePresentateur.TypeQuestion.CHOIX_MULTIPLE -> bonnesReponses.isNotEmpty() &&
                bonnesReponses.all { reponses.getOrElse(it) { "" }.isNotBlank() }
            ContratVuePresentateur.TypeQuestion.VRAI_FAUX -> true
            ContratVuePresentateur.TypeQuestion.REPONSE_ECRITE -> reponseAttendue.isNotBlank()
        }
    }

    data class QuizCreer(
        val id: Int,                        // 0 = nouveau quiz pas encore sauvegardé
        val titre: String,
        val description: String,
        val categorie: ContratVuePresentateur.CategorieQuiz,
        val difficulte: ContratVuePresentateur.Difficulte,
        val acces: ContratVuePresentateur.AccesQuiz,
        val questions: List<QuestionCreer>,
        val estPublie: Boolean
    )

    /**
     * Les quiz créés sont maintenant gardés par la source de données de Créer (SourceQuizCréésBidon).
     * Pont temporaire : l'écran de jeu les lit encore ici, en attendant de passer lui aussi par le service.
     */
    fun obtenirQuizCreer(quizId: Int): QuizCreer? =
        SourceQuizCréésBidon.lireMaintenant().find { it.id == quizId }

    // Library
    data class CarteLibrary(
        val id: Int,
        val titre: String,
        val sousTitre: String,
        val badge: String?,
        val typeBadge: ContratLibrairie.TypeBadge
    )

    // Données codées en dur
    private val favoris = listOf(
        CarteLibrary(1, "Le système solaire", "Science - 10 questions", "4/5", ContratLibrairie.TypeBadge.PROGRESSION),
        CarteLibrary(2, "Capitales d'Europe", "Géographie - 12 questions", "Nouveau", ContratLibrairie.TypeBadge.NOUVEAU),
        CarteLibrary(3, "Verbe irréguliers anglais", "Langues - 15 questions", "87%", ContratLibrairie.TypeBadge.SCORE)
    )

    fun obternirFavoris(): List<CarteLibrary> = favoris
    fun obternirCompletes(): List<CarteLibrary> = emptyList()
    fun obternirCrees(): List<CarteLibrary> = emptyList()


    // Question (écran de jeu)
    /**
     * Retourne le quiz à jouer, avec les mêmes données que l'écran Créer.
     * Les réponses écrites sont retirées pour l'instant : le jeu gère seulement
     * le choix multiple et le vrai ou faux.
     */
    fun obtenirQuizAJouer(quizId: Int): QuizCreer? =
        obtenirQuizCreer(quizId)?.let { quiz ->
            quiz.copy(
                questions = quiz.questions.filter {
                    it.type != ContratVuePresentateur.TypeQuestion.REPONSE_ECRITE
                }
            )
        }


    // Rejoindre (code à 6 chiffres donné par l'enseignant·e)
    // Données codées en dur : chaque code ouvre un quiz (même numérotation que Créer)
    private val codesQuiz = mapOf(
        "111111" to 1,   // Le système solaire
        "222222" to 2,   // Bases de Kotlin
        "333333" to 3    // Capitales d'Europe
    )
    /** Retourne l'id du quiz qui correspond au code, ou null si le code n'existe pas. */
    fun trouverQuizParCode(code: String): Int? = codesQuiz[code.trim()]
}