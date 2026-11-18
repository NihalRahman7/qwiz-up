package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.domaine.service.ServiceStatistiques

class ModeleAccueil(
    private val serviceStatistiques: ServiceStatistiques
) : ContratAccueil.IModeleAccueil {

    override suspend fun obtenirStatistiques(): Statistiques =
        serviceStatistiques.obtenirStatistiques()
}