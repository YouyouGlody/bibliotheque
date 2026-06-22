// 📦 Cette classe va dans le package "configurations" car elle fait partie
// de la configuration de Spring Security.
// Elle n'est pas un service métier (pas de logique d'inscription, de modification...)
// Son seul rôle : dire à Spring Security où chercher les utilisateurs en BDD.
package com.bibliotheque.bibliotheque.configurations;

// Import du repository qu'on vient de créer à l'étape 5
import com.bibliotheque.bibliotheque.repositories.MembreRepo;

// Import Lombok
import lombok.RequiredArgsConstructor;
// @RequiredArgsConstructor génère un constructeur avec tous les champs "final"
// C'est la manière moderne de faire l'injection de dépendances dans Spring
// Au lieu d'écrire @Autowired, on déclare le champ en "final" et Lombok + Spring
// s'occupent du reste automatiquement

// Imports Spring Security
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
// UserDetailsService = interface de Spring Security avec UNE SEULE méthode à implémenter :
//   → loadUserByUsername(String username)
// Spring Security appelle cette méthode automatiquement lors d'une tentative de connexion

import org.springframework.security.core.userdetails.UsernameNotFoundException;
// Exception standard de Spring Security à lancer quand l'utilisateur n'est pas trouvé

// @Service indique à Spring que cette classe est un composant de service
// Spring va la créer automatiquement et la rendre disponible pour l'injection
import org.springframework.stereotype.Service;


@Service
// Grâce à @Service, Spring détecte automatiquement cette classe
// et la rend disponible comme bean Spring.
// BeanConfig.java pourra ensuite l'injecter via "UserDetailsService userDetailsService"

@RequiredArgsConstructor
// Génère ce constructeur automatiquement :
// public UserDetailsServiceImpl(MembreRepo membreRepo) {
//     this.membreRepo = membreRepo;
// }
// Ce constructeur permet à Spring d'injecter MembreRepo automatiquement

public class UserDetailsServiceImpl implements UserDetailsService {
// "implements UserDetailsService" = on dit à Spring Security :
// "C'est MA classe qui sait comment charger un utilisateur depuis la BDD"
// Spring Security l'utilisera automatiquement lors de chaque tentative de connexion


    // ===== INJECTION DU REPOSITORY =====
    // "final" est obligatoire pour que @RequiredArgsConstructor génère
    // le constructeur d'injection. Sans "final", Lombok l'ignore.
    private final MembreRepo membreRepo;


    // ===== LA MÉTHODE CENTRALE =====
    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        // ATTENTION au nom trompeur : "loadUserByUsername" est le nom imposé par l'interface
        // mais dans NOTRE application, le "username" est en réalité l'EMAIL.
        // Spring Security appelle cette méthode avec ce que l'utilisateur
        // a tapé dans le champ "identifiant" lors de la connexion.
        //
        // Flux complet lors d'une connexion :
        //
        //  1. Client envoie : POST /api/auth/connexion
        //                     Body: { "email": "jean@gmail.com", "motDePasse": "1234" }
        //
        //  2. Spring Security intercepte la requête
        //
        //  3. Spring Security appelle loadUserByUsername("jean@gmail.com")
        //                                                 ↑
        //                                         c'est le paramètre "userEmail" ici
        //
        //  4. On cherche le membre en BDD par son email
        //
        //  5. On retourne le Membre trouvé (qui implémente UserDetails)
        //
        //  6. Spring Security compare le mot de passe tapé avec le hash en BDD
        //     via le PasswordEncoder (BCrypt) configuré dans BeanConfig
        //
        //  7. Si tout correspond → connexion autorisée ✅
        //     Sinon              → connexion refusée  ❌


        // ===== RECHERCHE EN BDD =====
        return this.membreRepo.findByEmail(userEmail)
                // membreRepo.findByEmail(userEmail) retourne un Optional<Membre>
                // Deux cas possibles :
                //   → Optional contient un Membre  : l'email existe en BDD ✅
                //   → Optional est vide            : l'email n'existe pas  ❌

                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun membre trouvé avec l'email : " + userEmail
                ));
        // .orElseThrow() :
        //   → Si Optional contient un Membre  : le retourne directement ✅
        //   → Si Optional est vide            : lance UsernameNotFoundException ❌
        //
        // Pourquoi UsernameNotFoundException et pas RuntimeException ?
        //   → C'est l'exception STANDARD de Spring Security pour ce cas
        //   → Spring Security la gère automatiquement et retourne une
        //     réponse HTTP 401 (Non autorisé) au client
        //
        // Pourquoi on peut retourner Membre directement au lieu de UserDetails ?
        //   → Parce que Membre étend User qui implémente UserDetails !
        //   → Membre EST un UserDetails (polymorphisme Java)
        //   → Pas besoin de conversion
    }
}