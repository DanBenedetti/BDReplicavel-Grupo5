package com.grupo5.replicacaobd.controller;

import com.grupo5.replicacaobd.model.Pedido;
import com.grupo5.replicacaobd.model.Produto;
import com.grupo5.replicacaobd.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ApiController {

    private final QueryService queryService;

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable Long id) {
        Pedido pedido = queryService.getPedidoById(id);
        return pedido != null ? ResponseEntity.ok(pedido) : ResponseEntity.notFound().build();
    }

    @GetMapping("/clientes/{id}/pedidos")
    public ResponseEntity<List<Pedido>> getPedidosByClienteId(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getPedidosByClienteId(id));
    }

    @GetMapping("/produtos/baixo-estoque")
    public ResponseEntity<List<Produto>> getProdutosBaixoEstoque() {
        return ResponseEntity.ok(queryService.getProdutosBaixoEstoque());
    }

    @GetMapping("/relatorios/vendas")
    public ResponseEntity<Map<String, Object>> getRelatorioVendas() {
        return ResponseEntity.ok(queryService.getRelatorioVendas());
    }
}
