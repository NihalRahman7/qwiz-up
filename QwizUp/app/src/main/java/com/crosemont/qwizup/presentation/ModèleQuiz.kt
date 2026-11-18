package com.crosemont.qwizup.presentation

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
        val estPublie: Boolean,  // false = brouillon, true = publié
        val nbQuestions: Int
    )

    data class QuestionCréer(
        val texte: String,
        val type: ContratVuePrésentateur.TypeQuestion,
        val réponses: List<String> = listOf("", "", "", ""),   // choix multiple : A, B, C, D
        val bonnesRéponses: Set<Int> = setOf(0),   // choix multiple : une ou plusieurs de 0 à 3 ; vrai ou faux : {0} = vrai, {1} = faux
        val réponseAttendue: String = ""    // réponse écrite
    )

    data class QuizCréer(
        val id: Int,                        // 0 = nouveau quiz pas encore sauvegardé
        val titre: String,
        val description: String,
        val catégorie: ContratVuePrésentateur.CatégorieQuiz,
        val difficulté: ContratVuePrésentateur.Difficulté,
        val accès: ContratVuePrésentateur.AccèsQuiz,
        val questions: List<QuestionCréer>,
        val estPublié: Boolean
    )

    companion object {
        private fun choix(texte: String, réponses: List<String>, vararg bonnes: Int) =
            QuestionCréer(
                texte,
                ContratVuePrésentateur.TypeQuestion.CHOIX_MULTIPLE,
                réponses,
                bonnes.toSet()
            )

        private fun vraiFaux(texte: String, vrai: Boolean) =
            QuestionCréer(
                texte,
                ContratVuePrésentateur.TypeQuestion.VRAI_FAUX,
                bonnesRéponses = setOf(if (vrai) 0 else 1)
            )

        private fun ecrite(texte: String, attendue: String) =
            QuestionCréer(
                texte,
                ContratVuePrésentateur.TypeQuestion.REPONSE_ECRITE,
                réponseAttendue = attendue
            )

        // Données codées en dur, partagées par tous les ModeleQuiz :
        // l'écran Créer et l'éditeur voient donc les mêmes quiz
        private val quizCréers = mutableListOf(
            QuizCréer(
                1,
                "Le système solaire",
                "Planètes, lunes et distances.",
                ContratVuePrésentateur.CatégorieQuiz.SCIENCES,
                ContratVuePrésentateur.Difficulté.MOYEN,
                ContratVuePrésentateur.AccèsQuiz.AVEC_CODE,
                listOf(
                    choix(
                        "Quelle est la plus grande planète du système solaire?",
                        listOf("Mars", "Jupiter", "Saturne", "Neptune"), 1
                    ),
                    vraiFaux("La Lune produit sa propre lumière.", vrai = false),
                    choix(
                        "Quelle planète est la plus proche du Soleil?",
                        listOf("Vénus", "Mercure", "Mars", "Terre"),
                        1
                    ),
                    choix(
                        "Combien de planètes compte le système solaire?",
                        listOf("7", "8", "9", "10"),
                        1
                    ),
                    vraiFaux("Mars est surnommée la planète rouge.", vrai = true)
                ),
                estPublié = false
            ),
            QuizCréer(
                2,
                "Bases de Kotlin",
                "Les notions de base du langage Kotlin.",
                ContratVuePrésentateur.CatégorieQuiz.INFORMATIQUE,
                ContratVuePrésentateur.Difficulté.FACILE,
                ContratVuePrésentateur.AccèsQuiz.AVEC_CODE,
                listOf(
                    choix(
                        "Quel mot-clé déclare une variable qu'on ne peut pas réassigner?",
                        listOf("var", "val", "let", "const"), 1
                    ),
                    vraiFaux("Kotlin peut utiliser les bibliothèques Java.", vrai = true),
                    choix(
                        "Quel type représente du texte?",
                        listOf("Int", "Char", "String", "Text"),
                        2
                    ),
                    choix(
                        "Quel mot-clé déclare une fonction?",
                        listOf("function", "def", "fun", "fn"),
                        2
                    ),
                    choix(
                        "Lesquels sont des types de base en Kotlin?",
                        listOf("Int", "String", "Integer", "Boolean"),
                        0,
                        1,
                        3
                    ),
                    vraiFaux("Une variable String peut être null par défaut.", vrai = false)
                ),
                estPublié = false
            ),
            QuizCréer(
                3,
                "Capitales d'Europe",
                "Associe chaque pays à sa capitale.",
                ContratVuePrésentateur.CatégorieQuiz.GÉOGRAPHIE,
                ContratVuePrésentateur.Difficulté.FACILE,
                ContratVuePrésentateur.AccèsQuiz.TOUT_LE_MONDE,
                listOf(
                    choix(
                        "Quelle est la capitale du Portugal?",
                        listOf("Porto", "Lisbonne", "Madrid", "Faro"),
                        1
                    ),
                    choix(
                        "Quelle est la capitale de la Finlande?",
                        listOf("Oslo", "Stockholm", "Helsinki", "Tallinn"),
                        2
                    ),
                    choix(
                        "Lesquelles de ces villes sont des capitales?",
                        listOf("Paris", "Barcelone", "Rome", "Berlin"),
                        0,
                        2,
                        3
                    ),
                    vraiFaux("Genève est la capitale de la Suisse.", vrai = false)
                ),
                estPublié = true
            )
        )
        private var prochainIdCreer = 4
    }

    fun obtenirBrouillons(): List<CarteCreate> =
        quizCréers.filter { !it.estPublié }.map { it.enCarte() }

    fun obtenirPubliés(): List<CarteCreate> = quizCréers.filter { it.estPublié }.map { it.enCarte() }

    fun obtenirQuizCréer(quizId: Int): QuizCréer? = quizCréers.find { it.id == quizId }

    /** Ajoute le quiz s'il est nouveau (id = 0), sinon remplace l'ancienne version. Retourne son id. */
    fun sauvegarderQuizCrée(quiz: QuizCréer): Int {
        val index = quizCréers.indexOfFirst { it.id == quiz.id }
        return if (index == -1) {
            val nouveau = quiz.copy(id = prochainIdCreer++)
            quizCréers.add(nouveau)
            nouveau.id
        } else {
            quizCréers[index] = quiz
            quiz.id
        }
    }

    private fun QuizCréer.enCarte() = CarteCreate(id, titre, estPublié, questions.size)

    // Library
    data class CarteLibrary(
        val id: Int,
        val titre: String,
        val sousTitre: String,
        val badge: String?,
        val typeBadge: ContratLibrairie.TypeBadge
    )

    fun obternirCompletes(): List<CarteLibrary> = emptyList()
    fun obternirCrees(): List<CarteLibrary> = emptyList()

    // Question (écran de jeu)
    /**
     * Retourne le quiz à jouer, avec les mêmes données que l'écran Créer.
     * Les réponses écrites sont retirées pour l'instant : le jeu gère seulement
     * le choix multiple et le vrai ou faux.
     */
    fun obtenirQuizÀJouer(quizId: Int): QuizCréer? =
        obtenirQuizCréer(quizId)?.let { quiz ->
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