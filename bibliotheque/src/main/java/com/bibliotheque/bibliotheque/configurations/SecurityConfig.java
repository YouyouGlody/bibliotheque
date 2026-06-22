// 📦 Même package que BeanConfig et UserDetailsServiceImpl
package com.bibliotheque.bibliotheque.configurations;

// ========== IMPORTS LOMBOK ==========
import lombok.RequiredArgsConstructor;

// ========== IMPORTS SPRING ==========
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ========== IMPORTS SPRING SECURITY ==========
import org.springframework.security.authentication.AuthenticationProvider;
// On injecte l'AuthenticationProvider qu'on a créé dans BeanConfig
// Spring l'injecte automatiquement car on l'a déclaré comme @Bean

import org.springframework.security.config.Customizer;
// Customizer.withDefaults() = utilise la configuration par défaut de Spring
// pour certaines fonctionnalités (comme CORS)

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// HttpSecurity = l'objet principal de configuration de Spring Security
// C'est lui qu'on "configure" pour définir toutes les règles de sécurité HTTP

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// AbstractHttpConfigurer::disable = façon moderne de désactiver une fonctionnalité
// On l'utilise pour désactiver CSRF

import org.springframework.security.config.http.SessionCreationPolicy;
// SessionCreationPolicy = politique de gestion des sessions HTTP
// STATELESS = pas de session côté serveur (obligatoire pour une API REST)

import org.springframework.security.web.SecurityFilterChain;
// SecurityFilterChain = la "chaîne de filtres" de Spring Security
// Chaque requête HTTP passe à travers cette chaîne avant d'atteindre ton controller
// C'est ici qu'on définit TOUTES les règles de sécurité


@Configuration
// Cette classe contient des @Bean de configuration Spring Security

@RequiredArgsConstructor
// Génère le constructeur :
// public SecurityConfig(AuthenticationProvider authenticationProvider) {
//     this.authenticationProvider = authenticationProvider;
// }

public class SecurityConfig {

    // ===== INJECTION DE AuthenticationProvider =====
    // On injecte le provider créé dans BeanConfig (DaoAuthenticationProvider)
    // Spring sait lequel injecter car il n'y en a qu'un seul @Bean de ce type
    private final AuthenticationProvider authenticationProvider;


    // ==============================================================
    // LE BEAN PRINCIPAL : SecurityFilterChain
    // ==============================================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // HttpSecurity est injecté automatiquement par Spring Security.
        // C'est l'objet qu'on va "configurer" étape par étape.
        //
        // Imagine HttpSecurity comme un formulaire de règles de sécurité :
        // On remplit chaque section (CSRF, CORS, autorisations, sessions...)
        // et à la fin on appelle .build() pour valider et appliquer tout.

        return httpSecurity

                // ===================================================
                // RÈGLE 1 : Désactiver CSRF
                // ===================================================
                .csrf(AbstractHttpConfigurer::disable)
                // CSRF = Cross-Site Request Forgery (Falsification de requête intersite)
                // C'est une attaque où un site malveillant envoie des requêtes
                // à ton API en se faisant passer pour un utilisateur connecté.
                //
                // Protection CSRF = Spring génère un token secret à chaque session
                // et l'exige dans chaque requête POST/PUT/DELETE.
                //
                // POURQUOI ON LA DÉSACTIVE ICI ?
                // La protection CSRF est utile pour les applications avec des
                // formulaires HTML classiques (sessions, cookies).
                // Pour une API REST avec authentification par token (JWT) :
                //   → Pas de session côté serveur
                //   → Pas de cookie d'authentification
                //   → Donc pas de risque CSRF
                //   → La protection CSRF est inutile ET gênerait nos requêtes API
                //
                // RÉSUMÉ : API REST + JWT → désactiver CSRF ✅
                //          Application web classique avec sessions → garder CSRF ✅


                // ===================================================
                // RÈGLE 2 : Configurer CORS
                // ===================================================
                .cors(Customizer.withDefaults())
                // CORS = Cross-Origin Resource Sharing (Partage de ressources cross-origine)
                // C'est la politique des navigateurs qui BLOQUE par défaut
                // les requêtes venant d'un domaine différent.
                //
                // Exemple sans CORS configuré :
                //   Frontend sur http://localhost:3000
                //   Backend  sur http://localhost:8080
                //   → Le navigateur bloque la requête ! ❌ (origines différentes)
                //
                // Avec Customizer.withDefaults() :
                //   Spring utilise le bean CorsConfigurationSource s'il existe
                //   ou accepte toutes les origines par défaut (ok pour le dev)
                //
                // En production, on créerait un @Bean CorsConfigurationSource
                // pour autoriser uniquement les domaines spécifiques du frontend.


                // ===================================================
                // RÈGLE 3 : Définir les autorisations d'accès
                // ===================================================
                .authorizeHttpRequests(auth -> auth

                                // ----- Routes PUBLIQUES (sans connexion) -----
                                .requestMatchers("/api/auth/**").permitAll()
                                // "/api/auth/**" = toutes les URLs qui commencent par "/api/auth/"
                                // Ex: /api/auth/inscription, /api/auth/connexion
                                // permitAll() = tout le monde peut y accéder sans être connecté
                                //
                                // POURQUOI c'est indispensable ?
                                // Si on ne mettait pas cette règle, Spring Security bloquerait
                                // aussi la route d'inscription → impossible de créer un compte !
                                // C'est le paradoxe : pour s'inscrire, il faut que la route
                                // d'inscription soit accessible sans être inscrit.

                                .requestMatchers("/api/public/**").permitAll()
                                // Exemple d'autres routes publiques :
                                // /api/public/livres → voir les livres sans être connecté

                                // ----- Routes RÉSERVÉES AUX ADMINS -----
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                // hasRole("ADMIN") vérifie que l'utilisateur a le rôle "ROLE_ADMIN"
                                // Spring Security ajoute automatiquement le préfixe "ROLE_"
                                // donc on écrit juste "ADMIN" et non "ROLE_ADMIN"
                                //
                                // Ex: /api/admin/membres → gérer les membres (admin seulement)
                                //     /api/admin/livres  → ajouter/supprimer des livres

                                // ----- Routes RÉSERVÉES AUX MEMBRES -----
                                .requestMatchers("/api/membres/**").hasRole("MEMBRE")
                                // Ex: /api/membres/emprunts → voir ses propres emprunts
                                //     /api/membres/profil   → voir/modifier son profil

                                // ----- Toutes les autres routes -----
                                .anyRequest().authenticated()
                        // Toute URL non mentionnée ci-dessus
                        // nécessite d'être connecté (peu importe le rôle)
                        //
                        // ORDRE IMPORTANT : Spring Security lit les règles
                        // DE HAUT EN BAS et s'arrête à la première qui correspond.
                        // Donc .anyRequest() doit TOUJOURS être en dernier !
                )


                // ===================================================
                // RÈGLE 4 : Politique de gestion des sessions
                // ===================================================
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // SessionCreationPolicy.STATELESS = sans état
                //
                // Il existe plusieurs politiques :
                //
                // ALWAYS     → crée toujours une session (app web classique)
                // IF_REQUIRED→ crée une session si nécessaire (défaut Spring)
                // NEVER      → n'en crée pas mais en utilise une si elle existe
                // STATELESS  → n'en crée JAMAIS et n'en utilise JAMAIS
                //
                // POURQUOI STATELESS pour une API REST ?
                //
                // ❌ Avec sessions (STATEFUL) :
                //    Serveur garde en mémoire : "l'utilisateur 42 est connecté"
                //    Client envoie un cookie de session à chaque requête
                //    Problème : si on a 2 serveurs, ils ne partagent pas les sessions
                //    → Scalabilité impossible
                //
                // ✅ Sans sessions (STATELESS) :
                //    Serveur ne garde RIEN en mémoire entre les requêtes
                //    Client envoie son token JWT à chaque requête
                //    Le token contient toutes les infos nécessaires
                //    → Scalabilité facile, chaque serveur peut traiter n'importe quelle requête


                // ===================================================
                // RÈGLE 5 : Brancher notre AuthenticationProvider
                // ===================================================
                .authenticationProvider(authenticationProvider)
                // On dit à Spring Security d'utiliser NOTRE provider
                // (le DaoAuthenticationProvider configuré dans BeanConfig)
                // et non le provider par défaut de Spring.
                //
                // Sans cette ligne, Spring utiliserait son propre système
                // d'authentification en mémoire (avec un mot de passe généré
                // aléatoirement au démarrage) → pas ce qu'on veut !


                // ===================================================
                // CONSTRUCTION FINALE
                // ===================================================
                .build();
        // Compile toutes les règles définies ci-dessus
        // et retourne la SecurityFilterChain configurée.
        // Spring Security l'appliquera automatiquement à toutes
        // les requêtes HTTP entrantes.
    }
}