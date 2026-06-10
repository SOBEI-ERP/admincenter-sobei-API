package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

@Converter(autoApply = true)
public class TipoConclusaoConverter implements AttributeConverter<TipoConclusao, PGobject> {

    @Override
    public PGobject convertToDatabaseColumn(TipoConclusao attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            PGobject dbObject = new PGobject();
            dbObject.setType("tipo_conclusao");
            dbObject.setValue(attribute.getValue());
            return dbObject;
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao converter TipoConclusao para banco de dados", e);
        }
    }

    @Override
    public TipoConclusao convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null) {
            return null;
        }
        return TipoConclusao.fromValue(dbData.getValue());
    }
}
