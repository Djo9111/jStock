package com.pharmacy.stock.model;

import com.pharmacy.stock.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Utilisateur - Utilisateurs du système
 */
@Entity
@Table(name = "utilisateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long id;

    @Column(name = "nom_complet", nullable = false, length = 100)
    private String nomComplet;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /**
     * Liste des mouvements créés par cet utilisateur
     * Relation bidirectionnelle 1-N avec Mouvement
     */
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<Mouvement> mouvements = new ArrayList<>();

    /**
     * Constructeur sans la liste de mouvements (pour création)
     */
    public Utilisateur(String nomComplet, String email, String motDePasse, Role role) {
        this.nomComplet = nomComplet;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    /**
     * Vérifie si l'utilisateur est un administrateur
     */
    public boolean estAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * Vérifie si l'utilisateur est un gestionnaire
     */
    public boolean estGestionnaire() {
        return this.role == Role.GESTIONNAIRE;
    }

    /**
     * Vérifie si l'utilisateur est un vendeur
     */
    public boolean estVendeur() {
        return this.role == Role.VENDEUR;
    }

    /**
     * Vérifie si l'utilisateur peut modifier le stock
     */
    public boolean peutModifierStock() {
        return this.role == Role.ADMIN || this.role == Role.GESTIONNAIRE;
    }
}