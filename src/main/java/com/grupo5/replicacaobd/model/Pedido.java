package com.grupo5.replicacaobd.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    @Column(name = "valor_total")
    private BigDecimal valorTotal;
    
    private String status;
    
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
    
    @Column(name = "criado_por")
    private String criadoPor;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PedidoItem> itens;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        criadoPor = "grupo5";
    }
}
