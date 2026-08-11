package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusChamadoConverter implements AttributeConverter<StatusChamado, String> {

    @Override
    public String convertToDatabaseColumn(StatusChamado attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StatusChamado convertToEntityAttribute(String dbData) {
        return dbData == null ? null : StatusChamado.fromValue(dbData);
    }
}
