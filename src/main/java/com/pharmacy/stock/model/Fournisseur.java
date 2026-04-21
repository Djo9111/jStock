package com.pharmacy.stock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Fournisseur - Fournisseurs de produits
 */
@Entity
@Table(name = "fournisseur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fournisseur")
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    /**
     * Liste des mouvements d'entrée fournis par ce fournisseur
     * Relation bidirectionnelle 1-N avec Mouvement
     */
    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL)
    private List<Mouvement> mouvements = new ArrayList<>();

    /**
     * Constructeur sans la liste de mouvements (pour création)
     */
    public Fournisseur(String nom, String telephone, String email, String adresse) {
        this.nom = nom;
        this.telephone = telephone;
        this.email = email;
        this.adresse = adresse;
    }

    /**
     * Retourne le nombre total de mouvements fournis
     */
    public int getNombreMouvements() {
        return mouvements != null ? mouvements.size() : 0;
    }
}