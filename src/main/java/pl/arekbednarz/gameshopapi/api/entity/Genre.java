package pl.arekbednarz.gameshopapi.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "genre", schema = "gamestock_base_db")
public @Data class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String genreName;

    @ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.MERGE)
    private List<Game> games;
}
