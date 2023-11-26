package pl.arekbednarz.gameshopapi.api.enums;

import java.util.List;

public enum Platform {
    PC("PC"),
    MAC("macOS"),
    PS2("PlayStation 2"),
    PS3("PlayStation 3"),
    PS4("Playstation 4"),
    PS5("PlayStation 5"),
    XBOX("Xbox Classic"),
    XBOX360("Xbox 360"),
    XBOX_ONE("Xbox One"),
    XBOX_SERIES("Xbox Series S/X"),
    NINTENDO_SWITCH("Nintendo Switch"),
    NINTENDO_WII("Nintendo Wii/Wii U"),
    NINTENDO_DS("Nintendo DS/3DS"),
    OTHER_PLATFORM("Inne platformy");

    private String platformName;

    Platform(String platformName) {
        this.platformName = platformName;
    }

    public String getPlatformName() {
        return platformName;
    }

    public static Platform getPlatformByName(final String name){
        return allPlatforms()
            .stream()
            .filter(platform -> platform.getPlatformName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(OTHER_PLATFORM);
    }

    public static List<Platform>filterPlatformsByName(final List<String>platformNames){
        return allPlatforms().stream().filter(platform -> platformNames.contains(platform.getPlatformName())).toList();

    }

    public static List<Platform> allPlatforms(){
        return List.of( PC, MAC, PS2, PS3, PS4, PS5, XBOX, XBOX360, XBOX_ONE, XBOX_SERIES, NINTENDO_SWITCH, NINTENDO_WII, NINTENDO_DS);
    }

    public static List<String> allPlatformNames(){
        return allPlatforms()
            .stream()
            .map(Platform::getPlatformName)
            .toList();
    }
}
