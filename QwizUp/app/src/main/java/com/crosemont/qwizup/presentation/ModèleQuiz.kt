package com.crosemont.qwizup.presentation

import com.crosemont.qwizup.accèsAuxDonnées.QuizÀJouerBidon
import com.crosemont.qwizup.accèsAuxDonnées.SourceQuizCréésBidon
import com.crosemont.qwizup.domaine.service.ServiceQuiz
import com.crosemont.qwizup.presentation.library.ContratLibrairie
import java.text.Normalizer

class ModèleQuiz(private val service: ServiceQuiz = ServiceQuiz()) {

    // Discover
    data class CarteDiscover(
        val id: Int,
        val catégorie: ContratVuePrésentateur.CatégorieQuiz,
        val titre: String,
        val description: String,
        val nbQuestions: Int,
        val dureeMinutes: Int,
        val estFavori: Boolean = false,
        val quizIdApi: String = ""
    )

    /**
     * Favoris de Découvrir (par identifiant de l'API)
     */
    private val favorisDiscover = mutableSetOf("bidon-1")

    /**
     * Liste remplie par le service
     */
    @Volatile
    private var quizDiscover: List<CarteDiscover> = emptyList()

    /**
     * Va chercher les quiz via la couche service (À appeler depuis une coroutine)
     */
    suspend fun chargerQuizDiscover() {
        quizDiscover = service.obtenirQuiz().map { quiz ->
            CarteDiscover(
                id = quiz.id,
                catégorie = versCatégorie(quiz.catégorie),
                titre = quiz.titre,
                description = quiz.description,
                nbQuestions = quiz.nbQuestions,
                dureeMinutes = (quiz.nbQuestions / 2).coerceAtLeast(1),
                estFavori = quiz.quizIdApi in favorisDiscover,
                quizIdApi = quiz.quizIdApi
            )
        }
    }

    /**
     * Retourne les quiz de la catégorie choisie (null = tous) dont le titre ou la description
     * contient le texte recherché. On ignore les majuscules et les accents.
     */
    fun obtenirQuizDiscover(
        catégorie: ContratVuePrésentateur.CatégorieQuiz?,
        recherche: String
    ): List<CarteDiscover> {
        val texte = simplifier(recherche.trim())
        return quizDiscover.filter { quiz ->
            val bonneCategorie = catégorie == null || quiz.catégorie == catégorie
            val contientTexte = texte.isEmpty() ||
                    simplifier(quiz.titre).contains(texte) ||
                    simplifier(quiz.description).contains(texte)
            bonneCategorie && contientTexte
        }
    }

    private fun versCatégorie(texte: String): ContratVuePrésentateur.CatégorieQuiz =
        when (simplifier(texte)) {
            "sciences", "science" -> ContratVuePrésentateur.CatégorieQuiz.SCIENCES
            "histoire" -> ContratVuePrésentateur.CatégorieQuiz.HISTOIRE
            "langues" -> ContratVuePrésentateur.CatégorieQuiz.LANGUES
            "geographie" -> ContratVuePrésentateur.CatégorieQuiz.GÉOGRAPHIE
            "informatique" -> ContratVuePrésentateur.CatégorieQuiz.INFORMATIQUE
            else -> ContratVuePrésentateur.CatégorieQuiz.AUTRE
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
        val estPublié: Boolean,  // false = brouillon, true = publié
        val nbQuestions: Int
    )

    data class QuestionCréer(
        val texte: String,
        val type: ContratVuePrésentateur.TypeQuestion,
        val réponses: List<String> = listOf("", "", "", ""),   // choix multiple : A, B, C, D
        val bonnesRéponses: Set<Int> = setOf(0),   // choix multiple : une ou plusieurs de 0 à 3 ; vrai ou faux : {0} = vrai, {1} = faux
        val réponseAttendue: String = ""    // réponse écrite
    ) {
        /** Une question est complète quand elle a un texte et une bonne réponse remplie. */
        fun estComplete(): Boolean = texte.isNotBlank() && when (type) {
            // Au moins une bonne réponse cochée, et chaque réponse cochée doit être remplie
            ContratVuePrésentateur.TypeQuestion.CHOIX_MULTIPLE -> bonnesRéponses.isNotEmpty() &&
                bonnesRéponses.all { réponses.getOrElse(it) { "" }.isNotBlank() }
            ContratVuePrésentateur.TypeQuestion.VRAI_FAUX -> true
            ContratVuePrésentateur.TypeQuestion.REPONSE_ECRITE -> réponseAttendue.isNotBlank()
        }
    }

    data class QuizCréer(
        val id: Int,                        // 0 = nouveau quiz pas encore sauvegardé
        val titre: String,
        val description: String,
        val catégorie: ContratVuePrésentateur.CatégorieQuiz,
        val difficulté: ContratVuePrésentateur.Difficulté,
        val accès: ContratVuePrésentateur.AccèsQuiz,
        val questions: List<QuestionCréer>,
        val estPublié: Boolean,
        val categoriePerso: String = ""     // nom de la catégorie quand catégorie = AUTRE (ex. : Sport)
    )

    /**
     * Les quiz créés sont maintenant gardés par la source de données de Créer (SourceQuizCréésBidon).
     * Pont temporaire : l'écran de jeu les lit encore ici, en attendant de passer lui aussi par le service.
     */
    fun obtenirQuizCréer(quizId: Int): QuizCréer? =
        SourceQuizCréésBidon.lireMaintenant().find { it.id == quizId }

    // Library
    data class CarteLibrary(
        val id: Int,
        val titre: String,
        val sousTitre: String,
        val badge: String?,
        val typeBadge: ContratLibrairie.TypeBadge
    )

    /**
     * Les quiz créés par l'utilisateur, lus dans la même source que l'écran Créer.
     * Pont temporaire (comme pour l'écran de jeu) en attendant que la Librairie passe par un service.
     */
    fun obternirCrees(): List<CarteLibrary> =
        SourceQuizCréésBidon.lireMaintenant().map { quiz ->
            val nb = quiz.questions.size
            CarteLibrary(
                id = quiz.id,
                titre = quiz.titre,
                sousTitre = if (nb == 1) "1 question" else "$nb questions",
                badge = null,
                typeBadge = ContratLibrairie.TypeBadge.NOUVEAU   // pas affiché quand badge = null
            )
        }

    // Question (écran de jeu)
    /**
     * Retourne le quiz à jouer, avec les mêmes données que l'écran Créer.
     * Les réponses écrites sont retirées pour l'instant : le jeu gère seulement
     * le choix multiple et le vrai ou faux.
     */
    fun obtenirQuizÀJouer(quizId: Int): QuizCréer? =
        (obtenirQuizCréer(quizId) ?: QuizÀJouerBidon.trouver(quizId))?.let { quiz ->
            quiz.copy(
                questions = quiz.questions.filter {
                    it.type != ContratVuePrésentateur.TypeQuestion.REPONSE_ECRITE
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