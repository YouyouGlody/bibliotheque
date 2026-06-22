// 📦 Les repositories ont leur propre package.
// Ils constituent la couche d'accès aux données (DAL = Data Access Layer).
// Leur seul rôle : parler à la base de données. Rien d'autre.
package com.bibliotheque.bibliotheque.repositories;

// Import de l'entité concernée
import com.bibliotheque.bibliotheque.entities.User;

// JpaRepository vient de Spring Data JPA (spring-boot-starter-data-jpa dans pom.xml)
import org.springframework.data.jpa.repository.JpaRepository;

// @Repository indique à Spring que cette interface est un composant
// de la couche d'accès aux données
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
// On étend JpaRepository avec deux paramètres :
//   → User  : le type de l'entité gérée
//   → Long  : le type de la clé primaire (notre champ "id" est un Long)
//
// En étendant JpaRepository, on hérite GRATUITEMENT de toutes ces méthodes :
//   → save(user)          : INSERT ou UPDATE en BDD
//   → findById(id)        : SELECT WHERE id = ?
//   → findAll()           : SELECT * FROM users
//   → deleteById(id)      : DELETE WHERE id = ?
//   → existsById(id)      : vérifie si un enregistrement existe
//   → count()             : COUNT(*)
//   → ... et bien d'autres
//
// On n'écrit AUCUNE requête SQL — Spring Data les génère automatiquement !

    // Pas de méthode custom ici pour UserRepo.
    // Les méthodes héritées de JpaRepository suffisent pour l'instant.
}