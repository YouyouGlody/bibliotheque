// 📦 On déclare dans quel package se trouve ce fichier
// Les enums métier vont dans le package "entities" car ils font partie du modèle de données
package com.bibliotheque.bibliotheque.entities;

// Un "enum" est un type spécial en Java qui représente un ensemble fixe de valeurs constantes.
// On l'utilise ici pour définir les rôles possibles d'un utilisateur dans l'application.
// Avantage : impossible de mettre une valeur invalide (pas de risque d'écrire "CLIANT" par erreur)

public enum Role {

    // Un membre normal de la bibliothèque (peut emprunter des livres)
    MEMBRE,

    // Un administrateur (peut gérer les livres, les membres, etc.)
    ADMIN
}
