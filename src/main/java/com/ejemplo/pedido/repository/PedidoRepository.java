package com.ejemplo.pedido.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.ejemplo.pedido.Pedido;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.items i LEFT JOIN FETCH i.articulo")
    List<Pedido> findAllWithItems();
}
