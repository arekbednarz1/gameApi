package pl.arekbednarz.gameshopapi.api.entity.converter;

import jakarta.persistence.AttributeConverter;
import pl.arekbednarz.gameshopapi.api.enums.Platform;

import static pl.arekbednarz.gameshopapi.api.enums.Platform.getPlatformByName;

public class PlatformConverter implements AttributeConverter<Platform,String> {
    @Override
    public String convertToDatabaseColumn(Platform platform) {
        return platform.getPlatformName();
    }
    @Override
    public Platform convertToEntityAttribute(String platformName) {
        return getPlatformByName(platformName);
    }
}
