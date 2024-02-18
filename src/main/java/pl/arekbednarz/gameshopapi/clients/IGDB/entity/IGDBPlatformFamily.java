package pl.arekbednarz.gameshopapi.clients.IGDB.entity;

public enum IGDBPlatformFamily {
    NINTENDO(5),
    XBOX(2),
    PLAYSTATION(1);

    private int id;

    IGDBPlatformFamily(int id) {
        this.id = id;
    }
}
