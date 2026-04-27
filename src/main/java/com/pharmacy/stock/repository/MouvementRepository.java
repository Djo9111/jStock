package com.pharmacy.stock.repository;

import com.pharmacy.stock.enums.TypeMouvement;
import com.pharmacy.stock.model.Mouvement;
import com.pharmacy.stock.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MouvementRepository extends JpaRepository<Mouvement, Long> {
    List<Mouvement> findByProduitOrderByDateMouvementDesc(Produit produit);
    List<Mouvement> findByTypeMouvement(TypeMouvement type);
}