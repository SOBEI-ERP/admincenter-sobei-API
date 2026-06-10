package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusDenunciaConverter implements AttributeConverter<StatusDenuncia, String> {

    @Override
    public String convertToDatabaseColumn(StatusDenuncia attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public StatusDenuncia convertToEntityAttribute(String dbData) {
        return dbData == null ? null : StatusDenuncia.fromValue(dbData);
    }
}
