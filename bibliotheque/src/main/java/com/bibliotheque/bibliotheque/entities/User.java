// 📦 Package : toutes les entités (classes qui représentent des tables en BDD) vont ici
package com.bibliotheque.bibliotheque.entities;

// ========== IMPORTS JPA (pour la base de données) ==========
import jakarta.persistence.*;          // @Entity, @Table, @Id, @Column, etc.

// ========== IMPORTS LOMBOK (pour éviter le code répétitif) ==========
import lombok.AllArgsConstructor;     // génère un constructeur avec TOUS les champs
import lombok.Getter;                 // génère tous les getters automatiquement
import lombok.NoArgsConstructor;      // génère un constructeur VIDE (obligatoire pour JPA)
import lombok.Setter;                 // génère tous les setters automatiquement

// ========== IMPORTS SPRING SECURITY ==========
// Ces imports viennent du module spring-boot-starter-security dans le pom.xml
import org.springframework.security.core.GrantedAuthority;
// GrantedAuthority = interface qui représente une permission/rôle dans Spring Security

import org.springframework.security.core.authority.SimpleGrantedAuthority;
// SimpleGrantedAuthority = implémentation concrète de GrantedAuthority.
// Elle prend une simple String comme "ROLE_MEMBRE" ou "ROLE_ADMIN".

import org.springframework.security.core.userdetails.UserDetails;
// UserDetails = interface CENTRALE de Spring Security.
// Elle représente les informations d'un utilisateur dont Spring Security a besoin
// pour gérer l'authentification et les autorisations

// ========== IMPORTS JAVA ==========
import java.time.LocalDateTime;       // pour stocker la date d'inscription
import java.util.Collection;         // interface parente des listes/sets
import java.util.List;               // pour retourner la liste des rôles


// ========== ANNOTATIONS LOMBOK ==========
@Getter           // génère : getNom(), getEmail(), getMotDePasse(), etc.
@Setter           // génère : setNom(), setEmail(), setMotDePasse(), etc.
@AllArgsConstructor  // génère : new User(id, email, motDePasse, role, actif, dateInscription)
@NoArgsConstructor   // génère : new User() — OBLIGATOIRE pour que JPA puisse créer des objets

// ========== ANNOTATIONS JPA ==========
@Entity  // Dit à JPA : "cette classe correspond à une table en base de données"

@Table(name = "users")
// Dit à JPA : "le nom de la table en BDD sera 'users'"
// Sans cette annotation, JPA utiliserait le nom de la classe ("User") comme nom de table

@Inheritance(strategy = InheritanceType.JOINED)
// Dit à JPA comment gérer l'héritage entre tables.
// JOINED = chaque classe (User, Membre, Admin) a SA PROPRE TABLE en BDD,
// mais elles sont liées par une clé étrangère (user_id).
// Résultat en BDD :
//   table "users"    → id, email, motDePasse, role, actif, dateInscription
//   table "membres"  → user_id (FK vers users), nom, prenom, telephone...
//   table "admins"   → user_id (FK vers users), ...

// ========== L'INTERFACE USERDETAILS ==========
// "implements UserDetails" est LA décision clé de toute cette architecture.
// En implémentant cette interface, on dit à Spring Security :
// "Ma classe User EST un utilisateur de sécurité, tu peux l'utiliser directement"
// Spring Security va alors appeler les méthodes de cette interface pour :
//   - vérifier le mot de passe
//   - récupérer les rôles
//   - vérifier si le compte est actif/bloqué/expiré
public class User implements UserDetails {

    // ===== CHAMP 1 : L'identifiant unique =====
    @Id
    // @Id dit à JPA : "ce champ est la clé primaire de la table"

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = la BDD génère automatiquement l'id (AUTO_INCREMENT en MySQL)
    // On n'a jamais besoin de définir l'id manuellement
    private Long id;


    // ===== CHAMP 2 : L'email =====
    // L'email sera utilisé comme "nom d'utilisateur" pour se connecter
    // (c'est plus pratique qu'un vrai username dans une app moderne)
    private String email;


    // ===== CHAMP 3 : Le mot de passe =====
    // ATTENTION : ce champ ne contiendra JAMAIS le mot de passe en clair !
    // Il contiendra le mot de passe haché par BCrypt.
    // Exemple : "1234" devient "$2a$10$xyz..." en BDD
    private String motDePasse;


    // ===== CHAMP 4 : Le rôle =====
    @Enumerated(EnumType.STRING)
    // Sans cette annotation, JPA stockerait l'index de l'enum (0, 1, 2...)
    // Avec EnumType.STRING, JPA stocke le nom textuel : "MEMBRE" ou "ADMIN"
    // C'est beaucoup plus lisible en BDD et plus sûr si on réorganise l'enum
    private Role role;


    // ===== CHAMP 5 : L'état du compte =====
    // Si actif = false, l'utilisateur ne peut plus se connecter (compte suspendu)
    // La valeur par défaut est true (actif dès l'inscription)
    private boolean actif = true;


    // ===== CHAMP 6 : La date d'inscription =====
    // LocalDateTime.now() capture automatiquement la date et l'heure actuelles
    // au moment où l'objet User est créé en mémoire
    private LocalDateTime dateInscription = LocalDateTime.now();


    // ================================================================
    // LES MÉTHODES DE L'INTERFACE UserDetails
    // Spring Security EXIGE qu'on implémente ces 6 méthodes.
    // Elles lui donnent toutes les infos dont il a besoin sur l'utilisateur.
    // ================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Cette méthode retourne la liste des rôles/permissions de l'utilisateur.
        // Spring Security l'appelle pour savoir ce que l'utilisateur a le droit de faire.
        //
        // On retourne une liste avec UN SEUL élément : le rôle de l'utilisateur.
        // La convention Spring Security est de préfixer les rôles avec "ROLE_"
        // Donc : Role.MEMBRE  → "ROLE_MEMBRE"
        //        Role.ADMIN   → "ROLE_ADMIN"
        //
        // role.name() retourne le nom textuel de l'enum : "MEMBRE" ou "ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        // Spring Security appelle cette méthode pour récupérer le mot de passe haché
        // et le comparer avec ce que l'utilisateur a tapé lors de la connexion.
        // On retourne notre champ "motDePasse" (qui contient le hash BCrypt)
        return motDePasse;
    }

    @Override
    public String getUsername() {
        // Spring Security appelle cette méthode pour savoir quel champ
        // est utilisé comme "identifiant" de connexion.
        // Dans notre app, c'est l'EMAIL (et non un pseudo/username classique)
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Retourne true = le compte n'expire jamais.
        // On pourrait retourner une logique plus complexe si on voulait
        // des comptes qui expirent après X jours.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Retourne true = le compte n'est jamais verrouillé.
        // On pourrait retourner false après X tentatives de connexion échouées.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Retourne true = les credentials (mot de passe) n'expirent jamais.
        // On pourrait forcer un changement de mot de passe tous les 90 jours.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // C'est LA méthode importante ici !
        // Elle est liée à notre champ "actif".
        // Si actif = true  → isEnabled() = true  → connexion autorisée ✅
        // Si actif = false → isEnabled() = false → connexion refusée ❌
        // C'est comme ça qu'on "suspend" un compte sans le supprimer
        return actif;
    }
}