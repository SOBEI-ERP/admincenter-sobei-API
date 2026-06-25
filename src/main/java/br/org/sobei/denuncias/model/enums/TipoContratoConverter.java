package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoContratoConverter implements AttributeConverter<TipoContrato, String> {

    @Override
    public String convertToDatabaseColumn(TipoContrato attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TipoContrato convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TipoContrato.fromValue(dbData);
    }
}
