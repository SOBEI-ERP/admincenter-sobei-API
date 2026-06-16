package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PrioridadeDenunciaConverter implements AttributeConverter<PrioridadeDenuncia, String> {

    @Override
    public String convertToDatabaseColumn(PrioridadeDenuncia attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public PrioridadeDenuncia convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PrioridadeDenuncia.fromValue(dbData);
    }
}
