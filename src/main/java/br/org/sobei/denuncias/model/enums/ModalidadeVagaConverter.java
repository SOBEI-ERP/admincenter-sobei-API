package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ModalidadeVagaConverter implements AttributeConverter<ModalidadeVaga, String> {

    @Override
    public String convertToDatabaseColumn(ModalidadeVaga attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ModalidadeVaga convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ModalidadeVaga.fromValue(dbData);
    }
}
