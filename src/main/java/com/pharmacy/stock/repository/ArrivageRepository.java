package com.pharmacy.stock.repository;

import com.pharmacy.stock.model.Arrivage;
import com.pharmacy.stock.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ArrivageRepository extends JpaRepository<Arrivage, Long> {
    // FIFO : ordre par date d'entrée
    List<Arrivage> findByProduitAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(
            Produit produit, Integer quantite
    );

    // Lots périmés
    List<Arrivage> findByDatePeremptionBefore(LocalDateTime date);
}