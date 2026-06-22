// 📦 Les services vont dans leur propre package.
// La couche service contient TOUTE la logique métier de l'application.
// Elle fait le lien entre le Controller (qui reçoit les requêtes)
// et le Repository (qui parle à la BDD).
// Règle d'or : le Controller ne parle JAMAIS directement au Repository.
//              Il passe TOUJOURS par le Service.
package com.bibliotheque.bibliotheque.services;

// ========== IMPORTS DE NOS CLASSES ==========
import com.bibliotheque.bibliotheque.dtos.InscriptionRequest;
// Le DTO qu'on reçoit du Controller (données brutes du client).

import com.bibliotheque.bibliotheque.entities.Membre;
// L'entité qu'on va créer et sauvegarder en BDD

import com.bibliotheque.bibliotheque.entities.Role;
// L'enum pour assigner le rôle MEMBRE automatiquement

import com.bibliotheque.bibliotheque.repositories.MembreRepo;
// Le repository pour accéder à la BDD

// ========== IMPORTS LOMBOK ==========
import lombok.RequiredArgsConstructor;
// Génère le constructeur d'injection pour tous les champs "final"

// ========== IMPORTS SPRING ==========
import org.springframework.security.crypto.password.PasswordEncoder;
// Pour hacher le mot de passe AVANT de le sauvegarder en BDD
// Spring injecte automatiquement le BCryptPasswordEncoder
// qu'on a déclaré comme @Bean dans BeanConfig.

import org.springframework.stereotype.Service;
// @Service = marque cette classe comme composant de la couche service
// Spring la détecte, la crée et la rend disponible pour l'injection


@Service
// Spring crée une instance de cette classe au démarrage
// et l'injecte dans InscriptionController automatiquement

@RequiredArgsConstructor
// Génère :
// public InscriptionService(MembreRepo membreRepo, PasswordEncoder passwordEncoder) {
//     this.membreRepo = membreRepo;
//     this.passwordEncoder = passwordEncoder;
// }
public class InscriptionService {

    // ===== INJECTION 1 : Le Repository =====
    // Pour sauvegarder le nouveau membre en BDD
    // et vérifier si un email/numeroCarte existe déjà
    private final MembreRepo membreRepo;

    // ===== INJECTION 2 : Le PasswordEncoder =====
    // Pour hacher le mot de passe avec BCrypt
    // Spring injecte le BCryptPasswordEncoder déclaré dans BeanConfig
    // car c'est le seul @Bean de type PasswordEncoder dans notre projet
    private final PasswordEncoder passwordEncoder;


    // ==============================================================
    // MÉTHODE PRINCIPALE : inscrire un nouveau membre
    // ==============================================================
    public void inscrire(InscriptionRequest request) {
        // On retourne void car on n'a pas besoin de renvoyer le Membre créé.
        // Le Controller retournera juste un message de succès.
        // (Plus tard on pourrait retourner un token JWT directement)

        // ===================================================
        // ÉTAPE 1 : Vérifications AVANT de créer quoi que ce soit
        // ===================================================
        // On vérifie toutes les contraintes métier en premier.
        // Si une vérification échoue, on lance une exception
        // et on n'exécute RIEN de ce qui suit.
        // C'est le principe du "fail fast" : échouer tôt et clairement.


        // ----- Vérification 1 : Email déjà utilisé ? -----
        if (membreRepo.existsByEmail(request.getEmail())) {
            // existsByEmail() fait un COUNT en BDD → très rapide
            // Si true → cet email est déjà enregistré
            throw new IllegalArgumentException(
                    "Cet email est déjà utilisé : " + request.getEmail()
            );
            // IllegalArgumentException = exception standard Java
            // pour signaler qu'un argument fourni est invalide.
            // Plus tard on créera des exceptions personnalisées
            // (ex: EmailDejaUtiliseException) pour des messages d'erreur
            // plus précis renvoyés au client en JSON.
        }


        // ----- Vérification 2 : Numéro de carte déjà utilisé ? -----
        if (membreRepo.existsByNumeroCarte(request.getNumeroCarte())) {
            throw new IllegalArgumentException(
                    "Ce numéro de carte est déjà attribué : " + request.getNumeroCarte()
            );
        }


        // ----- Vérification 3 : Mot de passe assez long ? -----
        if (request.getMotDePasse().length() < 8) {
            // Règle métier : le mot de passe doit avoir au moins 8 caractères.
            // On vérifie AVANT de hacher car après le hachage
            // on ne peut plus vérifier la longueur originale.
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères"
            );
        }


        // ===================================================
        // ÉTAPE 2 : Construire l'entité Membre
        // ===================================================
        // Toutes les vérifications sont passées.
        // On peut maintenant créer l'objet Membre.
        //
        // On utilise new Membre() + les setters plutôt qu'un constructeur
        // avec tous les arguments, car c'est plus lisible et moins fragile
        // (l'ordre des paramètres n'a pas d'importance avec les setters).
        Membre nouveauMembre = new Membre();

        // ----- Champs venant du DTO (données fournies par le client) -----
        nouveauMembre.setNom(request.getNom());
        nouveauMembre.setPrenom(request.getPrenom());
        nouveauMembre.setEmail(request.getEmail());
        nouveauMembre.setTelephone(request.getTelephone());
        nouveauMembre.setVille(request.getVille());
        nouveauMembre.setNumeroCarte(request.getNumeroCarte());

        // ----- Le mot de passe : TOUJOURS hacher avant de stocker -----
        nouveauMembre.setMotDePasse(
                passwordEncoder.encode(request.getMotDePasse())
        );
        // passwordEncoder.encode("monMotDePasse123")
        //   → "$2a$10$N9qo8uLOickgx2ZMRZoMye..." (hash BCrypt unique)
        //
        // JAMAIS : nouveauMembre.setMotDePasse(request.getMotDePasse())
        //          ← stocker le mot de passe en clair = faille de sécurité critique !
        //
        // Même si quelqu'un accède à ta BDD, il ne verra que des hashs
        // impossibles à déchiffrer → les mots de passe des utilisateurs
        // restent protégés.


        // ----- Champs gérés côté SERVEUR (jamais fournis par le client) -----
        nouveauMembre.setRole(Role.MEMBRE);
        // Le rôle est TOUJOURS assigné côté serveur, JAMAIS depuis le DTO.
        // Si on laissait le client choisir son rôle → faille de sécurité !
        // Tout le monde pourrait se créer un compte ADMIN.

        nouveauMembre.setActif(true);
        // Le compte est actif dès l'inscription.
        // Plus tard on pourrait mettre false et envoyer un email
        // de confirmation pour activer le compte.

        // Note : dateInscription est déjà initialisée à LocalDateTime.now()
        // directement dans la classe User → pas besoin de la setter ici.
        // Note : id est généré automatiquement par la BDD → pas besoin non plus.


        // ===================================================
        // ÉTAPE 3 : Sauvegarder en base de données
        // ===================================================
        membreRepo.save(nouveauMembre);
        // save() fait automatiquement un INSERT en BDD.
        // Grâce à l'héritage JOINED, JPA fait en réalité DEUX INSERT :
        //
        //   INSERT INTO users
        //   (email, motDePasse, role, actif, dateInscription)
        //   VALUES (?, ?, ?, ?, ?)
        //
        //   INSERT INTO membres
        //   (user_id, nom, prenom, telephone, ville, numeroCarte)
        //   VALUES (?, ?, ?, ?, ?, ?)
        //
        // Tout ça en une seule ligne de code grâce à JPA ! ✅
    }


    // ==============================================================
    // MÉTHODE UTILITAIRE : vérifier si un email existe déjà
    // ==============================================================
    public boolean emailExiste(String email) {
        // Méthode publique qu'on pourrait appeler depuis d'autres services
        // ou controllers si besoin de vérifier un email sans inscrire.
        return membreRepo.existsByEmail(email);
    }
}