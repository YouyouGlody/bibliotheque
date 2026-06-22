// 📦 Même package que UserRepo
package com.bibliotheque.bibliotheque.repositories;

// Import de l'entité Membre
import com.bibliotheque.bibliotheque.entities.Membre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Optional vient du package java.util
// Il représente une valeur qui PEUT être présente ou absente
// C'est la manière moderne d'éviter les NullPointerException
import java.util.Optional;

@Repository
public interface MembreRepo extends JpaRepository<Membre, Long> {

    // ===== MÉTHODE CUSTOM 1 =====
    Optional<Membre> findByEmail(String email);
    // Cette méthode n'existe pas dans JpaRepository de base.
    // On la déclare ici et Spring Data JPA la génère automatiquement
    // en analysant son NOM.
    //
    // Comment Spring Data comprend "findByEmail" ?
    //   "find"    → SELECT
    //   "By"      → WHERE
    //   "Email"   → colonne "email"
    //
    // Résultat : SELECT * FROM users WHERE email = ? (avec JOIN sur membres)
    //
    // On retourne Optional<Membre> et non Membre directement car :
    //   → Si l'email existe en BDD   : Optional contient le Membre ✅
    //   → Si l'email n'existe pas    : Optional est vide (pas de NullPointerException) ✅
    //   → Si on retournait Membre    : on aurait null si pas trouvé → risque de crash ❌
    //
    // Cette méthode sera utilisée à DEUX endroits :
    //   1. Dans InscriptionService  → vérifier qu'un email n'est pas déjà pris
    //   2. Dans UserDetailsServiceImpl → retrouver un membre lors de la connexion


    // ===== MÉTHODE CUSTOM 2 =====
    boolean existsByEmail(String email);
    // Spring Data génère : SELECT COUNT(*) > 0 FROM users WHERE email = ?
    // Retourne true si l'email existe déjà, false sinon.
    //
    // On aurait pu utiliser findByEmail().isPresent() pour la même chose,
    // mais existsByEmail est plus efficace car :
    //   → findByEmail charge TOUT l'objet Membre depuis la BDD
    //   → existsByEmail fait juste un COUNT, beaucoup plus léger
    //
    // On utilisera cette méthode dans InscriptionService pour
    // vérifier rapidement si un email est déjà pris.


    // ===== MÉTHODE CUSTOM 3 =====
    boolean existsByNumeroCarte(String numeroCarte);
    // Même logique : vérifie si un numéro de carte est déjà utilisé.
    // Spring Data génère : SELECT COUNT(*) > 0 FROM membres WHERE numero_carte = ?
    // Utile pour éviter les doublons de numéro de carte à l'inscription.
}
