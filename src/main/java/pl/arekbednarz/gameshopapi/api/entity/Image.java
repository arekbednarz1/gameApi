package pl.arekbednarz.gameshopapi.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.Session;

import java.util.List;

import static pl.arekbednarz.gameshopapi.utils.HibernateUtil.getSessionFactory;


@Entity
@Table(name = "games_image",schema = "gamestock_base_db")
public @Data class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "image_data")
    private byte[] imageData;
//
//    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "image")
//    List<GameStock> gamesOnStock;

    public void saveNewImage(byte[] imageData){
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Image image = new Image();
        session.persist(image);
        session.getTransaction().commit();
    }
}
