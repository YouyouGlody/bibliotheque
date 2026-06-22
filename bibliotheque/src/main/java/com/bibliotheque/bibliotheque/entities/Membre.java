// 📦 Même package que User.java — ils font partie du même modèle de données
package com.bibliotheque.bibliotheque.entities;

// ========== IMPORTS JPA ==========
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// ========== IMPORTS LOMBOK ==========
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// ========== ANNOTATIONS LOMBOK ==========
@Getter
@Setter
@NoArgsConstructor   // obligatoire pour JPA
@AllArgsConstructor  // pratique pour les tests et la création manuelle

// ========== ANNOTATIONS JPA ==========
@Entity
// Cette classe est aussi une entité JPA → elle aura sa propre table en BDD

@Table(name = "membres")
// Le nom de la table en BDD sera "membres"

@PrimaryKeyJoinColumn(name = "user_id")
// C'est l'annotation qui "connecte" la table "membres" à la table "users"
// Elle dit : "la clé primaire de 'membres' s'appelle 'user_id'
//             ET c'est aussi une clé étrangère qui pointe vers 'users'"
//
// Résultat en BDD :
// ┌─────────────────────────────────┐
// │ TABLE users                     │
// │ id | email | motDePasse | role  │
// │ 1  | a@b.c | $2a$...    | MEMBRE│
// └────────────────┬────────────────┘
//                  │  user_id = 1
// ┌────────────────▼────────────────┐
// │ TABLE membres                   │
// │ user_id | nom   | prenom | ...  │
// │ 1       | Dupont| Jean   | ...  │
// └─────────────────────────────────┘
//
// Quand on charge un Membre, JPA fait automatiquement une JOIN
// entre les deux tables pour reconstituer l'objet complet.

public class Membre extends User {
// "extends User" = Membre HÉRITE de User
// Cela signifie que Membre possède AUTOMATIQUEMENT tous les champs de User :
//   → id, email, motDePasse, role, actif, dateInscription
//   → ET toutes les méthodes de UserDetails (getPassword, getUsername, etc.)
// On n'a besoin de déclarer ici QUE les champs SPÉCIFIQUES au Membre


    // ===== CHAMP 1 : Nom =====
    // Champ spécifique au Membre, pas présent dans User
    // Sera stocké dans la table "membres", pas dans "users"
    private String nom;


    // ===== CHAMP 2 : Prénom =====
    private String prenom;


    // ===== CHAMP 3 : Téléphone =====
    private String telephone;


    // ===== CHAMP 4 : Ville =====
    private String ville;


    // ===== CHAMP 5 : Numéro de carte de bibliothèque =====
    // Champ métier spécifique à notre application bibliothèque
    // Unique pour chaque membre
    @Column(unique = true)
    // unique = true → JPA crée une contrainte UNIQUE en BDD sur cette colonne
    // Impossible d'avoir deux membres avec le même numéro de carte
    private String numeroCarte;


    // ===== CHAMP 6 : Liste des emprunts =====
    // Un membre peut emprunter plusieurs livres → relation One-To-Many
    // "mappedBy = membre" signifie que c'est la classe Emprunt qui
    // porte la clé étrangère (emprunt.membre_id), pas cette table.
    // cascade = CascadeType.ALL → si on supprime un Membre,
    //   tous ses emprunts sont aussi supprimés automatiquement
    // orphanRemoval = true → si on retire un emprunt de la liste,
    //   il est aussi supprimé de la BDD
    //
    // NOTE : On suppose qu'une classe Emprunt existera plus tard.
    // Pour l'instant, on la commente pour ne pas avoir d'erreur de compilation.
    //
    // @OneToMany(mappedBy = "membre", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Emprunt> emprunts = new ArrayList<>();

}