package com.grupo5.replicacaobd.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "produto")
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String descricao;
    private String categoria;
    private BigDecimal valor;
    private Integer estoque;
    
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
    
    @Column(name = "criado_por")
    private String criadoPor;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        criadoPor = "grupo5";
    }
}
