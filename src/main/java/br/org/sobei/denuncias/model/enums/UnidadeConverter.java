package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UnidadeConverter implements AttributeConverter<Unidade, String> {

    @Override
    public String convertToDatabaseColumn(Unidade attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDescricao();
    }

    @Override
    public Unidade convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Unidade.fromDescricao(dbData);
    }
}
