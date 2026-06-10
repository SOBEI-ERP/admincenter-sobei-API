package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoConclusaoConverter implements AttributeConverter<TipoConclusao, String> {

    @Override
    public String convertToDatabaseColumn(TipoConclusao attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TipoConclusao convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TipoConclusao.fromValue(dbData);
    }
}
