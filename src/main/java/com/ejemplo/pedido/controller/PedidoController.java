package com.ejemplo.pedido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import com.ejemplo.articulos.model.Articulo;
import com.ejemplo.articulos.service.ArticuloService;
import com.ejemplo.pedido.Pedido;
import com.ejemplo.pedido.PedidoItem;
import com.ejemplo.pedido.repository.PedidoRepository;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ArticuloService articuloService;

    @GetMapping
    public ResponseEntity<List<Pedido>> listarPedidos() {
        try {
            List<Pedido> pedidos = pedidoRepository.findAllWithItems();
            System.out.println("Pedidos recuperados: " + pedidos.size());
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@RequestBody Pedido pedido) {
        try {
            // Validar datos básicos
            if (pedido.getNombreCliente() == null || pedido.getNombreCliente().isEmpty() ||
                    pedido.getDniCliente() == null || pedido.getDniCliente().isEmpty() ||
                    pedido.getItems() == null || pedido.getItems().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Validar y cargar artículos reales desde la base
            for (PedidoItem item : pedido.getItems()) {
                if (item.getArticulo() == null || item.getArticulo().getId() == null) {
                    return ResponseEntity.badRequest().build();
                }

                Articulo articulo = articuloService.obtenerArticuloPorId(item.getArticulo().getId())
                        .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
                item.setArticulo(articulo);
                item.setPedido(pedido);
            }

            // Calcular el total antes de guardar
            double total = pedido.getItems().stream()
                    .mapToDouble(item -> item.getArticulo().getPrecio() * item.getCantidad())
                    .sum();
            pedido.setTotal(total);

            // Guardar el pedido con los artículos actualizados y el total calculado
            Pedido savedPedido = pedidoRepository.save(pedido);
            return ResponseEntity.ok(savedPedido);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public Pedido obtenerPedido(@PathVariable int id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    @DeleteMapping("/{id}")
    public void eliminarPedido(@PathVariable int id) {
        pedidoRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Pedido> actualizarPedido(@PathVariable int id, @RequestBody Pedido pedidoActualizado) {
        System.out.println("------ Llamada a actualizarPedido ------");
        System.out.println("ID recibido en path: " + id);
        System.out.println("ID en body pedidoActualizado: " + pedidoActualizado.getId());
        System.out.println("Nombre cliente en body: " + pedidoActualizado.getNombreCliente());
        System.out.println("Cantidad de items recibidos: "
                + (pedidoActualizado.getItems() != null ? pedidoActualizado.getItems().size() : "null"));

        try {
            Pedido pedidoExistente = pedidoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            System.out.println("Pedido existente encontrado con id: " + pedidoExistente.getId());

            pedidoExistente.setNombreCliente(pedidoActualizado.getNombreCliente());
            pedidoExistente.setDniCliente(pedidoActualizado.getDniCliente());

            if (pedidoActualizado.getItems() == null || pedidoActualizado.getItems().isEmpty()) {
                System.out.println("Error: items vacíos o nulos");
                return ResponseEntity.badRequest().build();
            }

            pedidoExistente.getItems().clear();
            for (PedidoItem item : pedidoActualizado.getItems()) {
                Articulo articulo = articuloService.obtenerArticuloPorId(item.getArticulo().getId())
                        .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
                item.setArticulo(articulo);
                item.setPedido(pedidoExistente);
                pedidoExistente.getItems().add(item);
            }

            double total = pedidoExistente.getItems().stream()
                    .mapToDouble(item -> item.getArticulo().getPrecio() * item.getCantidad())
                    .sum();
            pedidoExistente.setTotal(total);

            System.out.println("Pedido actualizado correctamente con id: " + pedidoExistente.getId());
            return ResponseEntity.ok(pedidoExistente);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
