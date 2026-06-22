// 📦 Les DTOs ont leur propre package séparé des entités.
// DTO = Data Transfer Object
// Ce package contient tous les objets qui "voyagent" entre le client et le serveur.
package com.bibliotheque.bibliotheque.dtos;

// ========== IMPORTS LOMBOK ==========
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// @Data est une annotation Lombok "tout-en-un". Elle génère automatiquement :
//   → tous les @Getter
//   → tous les @Setter
//   → @ToString        (pour afficher l'objet en String)
//   → @EqualsAndHashCode (pour comparer deux objets)
// C'est pratique pour les DTOs car ce sont de simples conteneurs de données
// qui n'ont pas besoin de logique complexe.
@Data
@AllArgsConstructor  // constructeur avec tous les champs (utile pour les tests)
@NoArgsConstructor   // constructeur vide (nécessaire pour que Jackson puisse
// désérialiser le JSON entrant en objet Java)

public class InscriptionRequest {
// Cette classe représente exactement ce que le client (Postman, frontend, etc.)
// doit envoyer dans le corps (body) de la requête HTTP POST pour s'inscrire.
//
// Exemple de JSON envoyé par le client :
// {
//     "nom": "Dupont",
//     "prenom": "Jean",
//     "email": "jean.dupont@gmail.com",
//     "motDePasse": "monMotDePasse123",
//     "telephone": "699123456",
//     "ville": "Yaoundé",
//     "numeroCarte": "BIB-2026-001"
// }


    // ===== CHAMP 1 : Nom =====
    // Reçoit le nom de famille envoyé par le client
    private String nom;


    // ===== CHAMP 2 : Prénom =====
    private String prenom;


    // ===== CHAMP 3 : Email =====
    // Sera utilisé comme identifiant de connexion.
    // On vérifiera dans le service qu'il n'existe pas déjà en BDD.
    private String email;


    // ===== CHAMP 4 : Mot de passe =====
    // IMPORTANT : ici le mot de passe arrive en CLAIR depuis le client.
    // C'est normal — la connexion est sécurisée par HTTPS en production.
    // C'est le SERVICE qui se chargera de le hacher avec BCrypt
    // AVANT de le sauvegarder en BDD. Jamais on ne stocke un mot de passe en clair.
    private String motDePasse;


    // ===== CHAMP 5 : Téléphone =====
    private String telephone;


    // ===== CHAMP 6 : Ville =====
    private String ville;


    // ===== CHAMP 7 : Numéro de carte =====
    // Identifiant unique de la carte de bibliothèque du membre
    private String numeroCarte;

}