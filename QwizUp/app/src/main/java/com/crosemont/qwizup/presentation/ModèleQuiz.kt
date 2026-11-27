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
        val quizIdApi: String = "",
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
        val categoriePerso: String = "",    // nom de la catégorie quand catégorie = AUTRE (ex. : Sport)
        val code: String = ""
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
        val typeBadge: ContratLibrairie.TypeBadge,
        val catégorie: ContratVuePrésentateur.CatégorieQuiz? = null,
        val code: String? = null,
        val estCréé: Boolean = false
    )

    /**
     * Les quiz créés par l'utilisateur, lus dans la même source que l'écran Créer.
     * Pont temporaire (comme pour l'écran de jeu) en attendant que la Librairie passe par un service.
     */
    fun obternirCrees(): List<CarteLibrary> =
        SourceQuizCréésBidon.lireMaintenant().map { quiz ->
            val nb = quiz.questions.size
            val questions = if (nb == 1) "1 question" else "$nb questions"
            val accès = if (quiz.accès == ContratVuePrésentateur.AccèsQuiz.TOUT_LE_MONDE) "Public" else "Privé"
            CarteLibrary(
                id = quiz.id,
                titre = quiz.titre,
                // Un brouillon n'est encore visible par personne : pas de Public / Privé
                sousTitre = if (quiz.estPublié) "$questions · $accès" else questions,
                badge = if (quiz.estPublié) "Publié" else "Brouillon",
                typeBadge = if (quiz.estPublié) ContratLibrairie.TypeBadge.SCORE else ContratLibrairie.TypeBadge.NOUVEAU,
                catégorie = quiz.catégorie,
                code = quiz.code.ifBlank { null },
                estCréé = true
            )
        }

    // Question (écran de jeu)
    /**
     * Retourne le quiz à jouer, avec les mêmes données que l'écran Créer.
     * Les réponses écrites sont retirées pour l'instant : le jeu gère seulement
     * le choix multiple et le vrai ou faux.
     */
    fun obtenirQuizÀJouer(quizId: Int): QuizCréer? =
        (obtenirQuizCréer(quizId) ?: quizDécouvrirÀJouer[quizId])?.let { quiz ->   // aussi les quiz 4 et 5 de Découvrir
            quiz.copy(
                questions = quiz.questions.filter {
                    it.type != ContratVuePrésentateur.TypeQuestion.REPONSE_ECRITE
                }
            )
        }


    // Questions des quiz de Découvrir qui ne viennent pas de l'écran Créer (4 et 5)
    private fun choixDécouvrir(texte: String, réponses: List<String>, vararg bonnes: Int) =
        QuestionCréer(texte, ContratVuePrésentateur.TypeQuestion.CHOIX_MULTIPLE, réponses, bonnes.toSet())

    private fun vraiFauxDécouvrir(texte: String, vrai: Boolean) =
        QuestionCréer(texte, ContratVuePrésentateur.TypeQuestion.VRAI_FAUX, bonnesRéponses = setOf(if (vrai) 0 else 1))

    private val quizDécouvrirÀJouer: Map<Int, QuizCréer> = mapOf(
        4 to QuizCréer(
            4, "La Révolution tranquille", "Les grands changements du Québec des années 1960.",
            ContratVuePrésentateur.CatégorieQuiz.HISTOIRE, ContratVuePrésentateur.Difficulté.MOYEN,
            ContratVuePrésentateur.AccèsQuiz.TOUT_LE_MONDE,
            listOf(
                choixDécouvrir("En quelle décennie la Révolution tranquille a-t-elle commencé?",
                    listOf("1940", "1950", "1960", "1970"), 2),
                choixDécouvrir("Quel premier ministre est élu en 1960, au début de la Révolution tranquille?",
                    listOf("Maurice Duplessis", "Jean Lesage", "René Lévesque", "Robert Bourassa"), 1),
                choixDécouvrir("Quel slogan a marqué l'élection de 1962?",
                    listOf("Maîtres chez nous", "Je me souviens", "Égalité ou indépendance", "Vive le Québec libre"), 0),
                vraiFauxDécouvrir("Le ministère de l'Éducation du Québec a été créé en 1964.", vrai = true),
                vraiFauxDécouvrir("Hydro-Québec a été fondée pendant la Révolution tranquille.", vrai = false)
            ),
            estPublié = true
        ),
        5 to QuizCréer(
            5, "Verbes irréguliers anglais", "Be, go, take… retrouve le passé des verbes les plus utilisés.",
            ContratVuePrésentateur.CatégorieQuiz.LANGUES, ContratVuePrésentateur.Difficulté.FACILE,
            ContratVuePrésentateur.AccèsQuiz.TOUT_LE_MONDE,
            listOf(
                choixDécouvrir("Quel est le passé simple de « go »?", listOf("goed", "went", "gone", "going"), 1),
                choixDécouvrir("Quel est le participe passé de « take »?", listOf("took", "taked", "taken", "takes"), 2),
                choixDécouvrir("Quel est le passé simple de « buy »?", listOf("buyed", "bought", "brought", "buys"), 1),
                choixDécouvrir("Lesquels sont des verbes irréguliers?", listOf("eat", "play", "write", "walk"), 0, 2),
                vraiFauxDécouvrir("Le passé simple de « cut » est « cut ».", vrai = true)
            ),
            estPublié = true
        )
    )

    // Rejoindre : le code de 5 caractères que le quiz reçoit à sa publication (voir ServiceCréation)
    /** Retourne l'id du quiz publié qui a ce code (majuscules ou minuscules), ou null s'il n'existe pas. */
    fun trouverQuizParCode(code: String): Int? =
        SourceQuizCréésBidon.lireMaintenant()
            .find { it.estPublié && it.code.equals(code.trim(), ignoreCase = true) }
            ?.id
}