// 📦 Même package que UserDetailsServiceImpl — c'est de la configuration Spring Security
package com.bibliotheque.bibliotheque.configurations;

// ========== IMPORTS LOMBOK ==========
import lombok.RequiredArgsConstructor;

// ========== IMPORTS SPRING ==========
import org.springframework.context.annotation.Bean;
// @Bean dit à Spring : "cette méthode retourne un objet que tu dois gérer.
// Crée-le une seule fois, garde-le en mémoire, et injecte-le partout où on en a besoin."
// C'est comme dire à Spring : "ajoute cet objet à ta boîte à outils".

import org.springframework.context.annotation.Configuration;
// @Configuration dit à Spring : "cette classe contient des configurations.
// Scanne toutes les méthodes @Bean qu'elle contient et enregistre-les."

// ========== IMPORTS SPRING SECURITY ==========
import org.springframework.security.authentication.AuthenticationManager;
// AuthenticationManager = le "chef d'orchestre" de l'authentification.
// C'est lui qu'on appellera plus tard pour déclencher une connexion manuellement
// (utile quand on implémentera le login avec JWT)

import org.springframework.security.authentication.AuthenticationProvider;
// AuthenticationProvider = le "vérificateur".
// Il reçoit les credentials (email + mot de passe),
// charge l'utilisateur via UserDetailsService,
// et vérifie le mot de passe via PasswordEncoder.

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// DaoAuthenticationProvider = implémentation concrète de AuthenticationProvider
// "Dao" = Data Access Object → il accède aux données via notre UserDetailsService
// C'est la combinaison de :
//   UserDetailsService (pour charger l'utilisateur depuis la BDD)
//   + PasswordEncoder  (pour vérifier le mot de passe)

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// AuthenticationConfiguration = classe Spring qui contient la configuration
// globale de l'authentification. On l'utilise pour récupérer l'AuthenticationManager.

import org.springframework.security.core.userdetails.UserDetailsService;
// On injecte UserDetailsService (l'interface) et non UserDetailsServiceImpl (la classe)
// directement. Pourquoi ?
//   → Bonne pratique : dépendre de l'interface, pas de l'implémentation
//   → Spring injecte automatiquement UserDetailsServiceImpl car c'est
//     la seule classe qui implémente UserDetailsService dans notre projet

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// BCryptPasswordEncoder = l'algorithme de hachage qu'on va utiliser.
// BCrypt est un algorithme lent par conception → rend les attaques par force brute très difficiles
// Exemple : "monMotDePasse123" → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

import org.springframework.security.crypto.password.PasswordEncoder;
// PasswordEncoder = l'interface. BCryptPasswordEncoder en est une implémentation.
// On retourne l'interface pour rester flexible (on pourrait changer d'algo plus tard).


@Configuration
// Spring va scanner cette classe et enregistrer tous les @Bean qu'elle contient

@RequiredArgsConstructor
// Génère le constructeur :
// public BeanConfig(UserDetailsService userDetailsService) {
//     this.userDetailsService = userDetailsService;
// }
// Spring injecte automatiquement UserDetailsServiceImpl ici

public class BeanConfig {

    // ===== INJECTION DE UserDetailsService =====
    // "final" obligatoire pour @RequiredArgsConstructor
    // Spring injecte notre UserDetailsServiceImpl automatiquement
    // car c'est la seule classe @Service qui implémente UserDetailsService
    private final UserDetailsService userDetailsService;



    // ==============================================================
    // BEAN 1 : PasswordEncoder
    // ==============================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder est l'encodeur de mots de passe le plus utilisé
        // dans les applications Spring Boot professionnelles.
        //
        // Comment BCrypt fonctionne :
        //
        //   LORS DE L'INSCRIPTION :
        //   "monMotDePasse123" ──BCrypt──► "$2a$10$xyz..." (stocké en BDD)
        //
        //   LORS DE LA CONNEXION :
        //   "monMotDePasse123" ──BCrypt──► comparé avec "$2a$10$xyz..." en BDD
        //   BCrypt sait comparer sans décrypter (hachage à sens unique)
        //
        // Pourquoi BCrypt est sécurisé ?
        //   → Chaque hachage est UNIQUE même pour le même mot de passe
        //     (grâce au "sel" aléatoire intégré)
        //   → Intentionnellement LENT → attaque par force brute prend des années
        //   → Impossible de retrouver le mot de passe original depuis le hash
        //
        // Le chiffre 10 dans "$2a$10$..." = le "cost factor"
        // Plus il est élevé, plus le hachage est lent (et sécurisé)
        // 10 est la valeur par défaut, bon équilibre sécurité/performance
        return new BCryptPasswordEncoder();
    }


    // ==============================================================
    // BEAN 2 : AuthenticationProvider
    // ==============================================================
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // DaoAuthenticationProvider est l'implémentation standard pour
        // les authentifications basées sur une base de données (DAO = Data Access Object)
        //
        // On lui passe notre UserDetailsService directement dans le constructeur.
        // Depuis Spring Security 6, c'est la manière recommandée.
        DaoAuthenticationProvider daoAuthenticationProvider =
                new DaoAuthenticationProvider(userDetailsService);
        // Maintenant daoAuthenticationProvider sait :
        //   → Comment charger un utilisateur depuis la BDD (via UserDetailsService)

        // On lui dit quel encodeur utiliser pour vérifier les mots de passe
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        // Maintenant daoAuthenticationProvider sait aussi :
        //   → Comment comparer le mot de passe tapé avec le hash en BDD (via BCrypt)

        // Ce provider fait donc TOUT le travail d'authentification :
        //   1. Reçoit email + mot de passe
        //   2. Appelle userDetailsService.loadUserByUsername(email) → charge le Membre
        //   3. Appelle passwordEncoder.matches(motDePasse, hashEnBDD) → vérifie le mdp
        //   4. Vérifie isEnabled(), isAccountNonLocked(), etc.
        //   5. Retourne le résultat : authentifié ✅ ou refusé ❌
        return daoAuthenticationProvider;
    }


    // ==============================================================
    // BEAN 3 : AuthenticationManager
    // ==============================================================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // AuthenticationManager est la façade de haut niveau pour l'authentification.
        // C'est lui qu'on injectera plus tard dans le service de connexion
        // pour déclencher une authentification manuellement.
        //
        // On ne le crée pas nous-mêmes — on le récupère depuis la configuration
        // globale de Spring Security (authenticationConfiguration).
        // Spring l'a déjà configuré avec notre AuthenticationProvider ci-dessus.
        //
        // Pourquoi on en a besoin ?
        //   Plus tard, quand on fera le login :
        //
        //   ConnexionService {
        //       authenticationManager.authenticate(
        //           new UsernamePasswordAuthenticationToken(email, motDePasse)
        //       );
        //       // Si ça passe → générer un token JWT et le retourner
        //   }
        //
        // Pour l'instant (inscription seulement), on ne l'utilise pas encore.
        // Mais on le déclare maintenant pour éviter des erreurs de configuration.
        return authenticationConfiguration.getAuthenticationManager();
    }
}