package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusVagaConverter implements AttributeConverter<StatusVaga, String> {

    @Override
    public String convertToDatabaseColumn(StatusVaga attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StatusVaga convertToEntityAttribute(String dbData) {
        return dbData == null ? null : StatusVaga.fromValue(dbData);
    }
}
