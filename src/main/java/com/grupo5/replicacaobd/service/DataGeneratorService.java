package com.grupo5.replicacaobd.service;

import com.grupo5.replicacaobd.model.Cliente;
import com.grupo5.replicacaobd.model.Pedido;
import com.grupo5.replicacaobd.model.PedidoItem;
import com.grupo5.replicacaobd.model.Produto;
import com.grupo5.replicacaobd.repository.ClienteRepository;
import com.grupo5.replicacaobd.repository.PedidoRepository;
import com.grupo5.replicacaobd.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DataGeneratorService {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final QueryService queryService;
    private final Random random = new Random();

    private final List<String> NOMES_CLIENTES = List.of("João Silva", "Maria Oliveira", "Pedro Santos", "Ana Costa", "Carlos Souza", "Beatriz Lima", "Ricardo Alves", "Fernanda Rocha");
    private final List<String> PRODUTOS_DESC = List.of("Notebook Dell", "Mouse Gamer", "Teclado Mecânico", "Monitor 24p", "Headset USB", "Webcam HD", "Cadeira Ergonômica", "Impressora Laser");
    private final List<String> CATEGORIAS = List.of("Informática", "Periféricos", "Escritório", "Eletrônicos");

    @Scheduled(fixedDelay = 15000)
    public void generateData() {
        System.out.println("\n--- [FLUXO DE ESCRITA - PRIMÁRIO] ---");

        // 1. Cadastro de clientes
        String nomeAleatorio = NOMES_CLIENTES.get(random.nextInt(NOMES_CLIENTES.size()));
        Cliente cliente = new Cliente();
        cliente.setNome(nomeAleatorio);
        cliente.setEmail(nomeAleatorio.toLowerCase().replace(" ", ".") + random.nextInt(1000) + "@grupo5.com");
        cliente = clienteRepository.save(cliente);
        System.out.println("Cliente Inserido: [ID: " + cliente.getId() + ", Nome: " + cliente.getNome() + ", Email: " + cliente.getEmail() + "]");

        // 2. Cadastro de produtos
        Produto produto = new Produto();
        produto.setDescricao(PRODUTOS_DESC.get(random.nextInt(PRODUTOS_DESC.size())));
        produto.setCategoria(CATEGORIAS.get(random.nextInt(CATEGORIAS.size())));
        produto.setValor(BigDecimal.valueOf(50 + random.nextDouble() * 2000).setScale(2, RoundingMode.HALF_UP));
        produto.setEstoque(random.nextInt(50) + 1);
        produto = produtoRepository.save(produto);
        System.out.println("Produto Inserido: [ID: " + produto.getId() + ", Descrição: " + produto.getDescricao() + ", Valor: R$ " + produto.getValor() + ", Estoque: " + produto.getEstoque() + "]");

        // 3. Criação de pedidos
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus("FINALIZADO");
        
        List<PedidoItem> itens = new ArrayList<>();
        PedidoItem item = new PedidoItem();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(random.nextInt(3) + 1);
        item.setValorUnitario(produto.getValor());
        itens.add(item);
        
        pedido.setItens(itens);
        pedido.setValorTotal(item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
        
        pedido = pedidoRepository.save(pedido);
        System.out.println("Pedido Gravado: [ID: " + pedido.getId() + ", Cliente: " + cliente.getNome() + ", Valor Total: " + pedido.getValorTotal() + ", Qtd Itens: " + itens.size() + "]");

        // 4. Consultas na réplica (READ)
        System.out.println("\n--- [FLUXO DE LEITURA - RÉPLICAS] ---");
        try {
            // Pequeno delay para garantir que a replicação ocorreu no MySQL (se houver latência)
            Thread.sleep(1000); 

            // 4.1 & 4.2 - Buscar pedido e itens
            Pedido p = queryService.getPedidoById(pedido.getId());
            if (p != null) {
                System.out.println("Consulta Pedido: ID " + p.getId() + ", Cliente: " + p.getCliente().getNome() + ", Valor: " + p.getValorTotal() + ", Status: " + p.getStatus());
                for (PedidoItem pi : p.getItens()) {
                    System.out.println("  > Item: " + pi.getProduto().getDescricao() + ", Quantidade: " + pi.getQuantidade());
                }
            }

            // 4.3 - Histórico do cliente (últimos 5)
            List<Pedido> historico = queryService.getPedidosByClienteId(cliente.getId());
            System.out.println("Histórico do Cliente (ID " + cliente.getId() + "):");
            historico.stream().limit(5).forEach(hp -> 
                System.out.println("  - Pedido " + hp.getId() + " - R$ " + hp.getValorTotal())
            );

            // 4.4 - Relatório agregado
            Map<String, Object> relatorio = queryService.getRelatorioVendas();
            System.out.println("Relatório Agregado:");
            System.out.println("  - Quantidade total de pedidos: " + relatorio.get("total_pedidos"));
            System.out.println("  - Valor total vendido: R$ " + relatorio.get("valor_total_vendas"));
        } catch (Exception e) {
            System.err.println("Erro ao realizar consultas na réplica: " + e.getMessage());
        }
        System.out.println("-------------------------------------\n");
    }
}
