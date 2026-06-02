package com.grupo5.replicacaobd.repository;

import com.grupo5.replicacaobd.model.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {
}
