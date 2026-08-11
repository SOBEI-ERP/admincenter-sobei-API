package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PrioridadeChamadoConverter implements AttributeConverter<PrioridadeChamado, String> {

    @Override
    public String convertToDatabaseColumn(PrioridadeChamado attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public PrioridadeChamado convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PrioridadeChamado.fromValue(dbData);
    }
}
