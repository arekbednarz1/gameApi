package pl.arekbednarz.gameshopapi.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "games", schema = "gamestock_base_db",indexes = {
        @Index(columnList = "game_title",name = "game_title_idx")
})
public @Data class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="game_title",nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.MERGE)
    private List<Publisher> publisher;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "game")
    private List<GameStock> gamesOnStock;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private List<Genre> genres;

    @Column(name="rawg_ID",unique = true)
    private Long rawgId;

}
