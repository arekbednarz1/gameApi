package pl.arekbednarz.gameshopapi.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "game_publisher",schema = "gamestock_base_db",indexes = {
        @Index(columnList = "publisher_name",name = "publisher_name_idx")
})
public @Data class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="publisher_name",nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL,mappedBy = "publisher")
    private Set<Game> publishedGames;

}
