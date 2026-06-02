package com.grupo5.replicacaobd.service;

import com.grupo5.replicacaobd.model.Pedido;
import com.grupo5.replicacaobd.model.Produto;
import com.grupo5.replicacaobd.repository.PedidoRepository;
import com.grupo5.replicacaobd.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public Pedido getPedidoById(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Pedido> getPedidosByClienteId(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Produto> getProdutosBaixoEstoque() {
        return produtoRepository.findByEstoqueLessThan(10);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRelatorioVendas() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("total_pedidos", pedidos.size());
        relatorio.put("valor_total_vendas", pedidos.stream()
                .map(Pedido::getValorTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return relatorio;
    }
}
