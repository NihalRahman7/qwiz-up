package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.domaine.service.ServiceStatistiques

class ModèleAccueil(
    private val serviceStatistiques: ServiceStatistiques
) : ContratAccueil.IModèleAccueil {

    override suspend fun obtenirStatistiques(): Statistiques =
        serviceStatistiques.obtenirStatistiques()
}