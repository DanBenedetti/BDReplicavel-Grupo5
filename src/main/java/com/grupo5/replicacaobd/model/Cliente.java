package com.grupo5.replicacaobd.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private String email;
    
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
