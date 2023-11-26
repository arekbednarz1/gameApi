package pl.arekbednarz.gameshopapi.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import pl.arekbednarz.gameshopapi.api.entity.converter.PlatformConverter;
import pl.arekbednarz.gameshopapi.api.enums.Platform;

import java.math.BigDecimal;

@Entity
@Table(name = "games_on_stock",schema = "gamestock_base_db")
public @Data class GameStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game",referencedColumnName = "game_title")
    private Game game;

    @Convert(converter = PlatformConverter.class)
    @Column(name = "game_platform")
    private Platform platform;

//    @Column(name = "price", precision = 1,scale = 2)
    @Column(name = "price")
    private BigDecimal price;

    @Column(name="image_url")
    private String imageUrl;

    @Column(name = "games_count")
    private Long count;

}
