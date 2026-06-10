package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoDenunciaConverter implements AttributeConverter<TipoDenuncia, String> {

    @Override
    public String convertToDatabaseColumn(TipoDenuncia attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TipoDenuncia convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TipoDenuncia.fromValue(dbData);
    }
}
