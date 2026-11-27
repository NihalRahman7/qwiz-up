package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.presentation.discover.ContratDétailQuiz.IPrésentateurDétailQuiz
import com.crosemont.qwizup.presentation.discover.ContratDétailQuiz.IVueDétailQuiz
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class PrésentateurDétailQuiz(
    private val modèle: ContratDécouvrir.IModèleDécouvrir,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,
) : IPrésentateurDétailQuiz {

    override fun attacherVue(vue: IVueDétailQuiz) {}
    override fun detacherVue() {}
    override fun chargerQuiz(quizId: Int) {}
    override fun surFavoriClique() {}
    override fun surJouerClique() {}
    override fun surRetourClique() {}
}