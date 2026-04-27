package com.pharmacy.stock.repository;

import com.pharmacy.stock.model.Categorie;
import com.pharmacy.stock.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Optional<Produit> findByCodeBarre(String codeBarre);
    List<Produit> findByCategorie(Categorie categorie);
}