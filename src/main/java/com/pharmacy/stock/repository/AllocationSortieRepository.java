package com.pharmacy.stock.repository;

import com.pharmacy.stock.model.AllocationSortie;
import com.pharmacy.stock.model.Arrivage;
import com.pharmacy.stock.model.Mouvement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationSortieRepository extends JpaRepository<AllocationSortie, Long> {
    List<AllocationSortie> findByMouvementSortie(Mouvement mouvement);
    List<AllocationSortie> findByArrivage(Arrivage arrivage);
}
