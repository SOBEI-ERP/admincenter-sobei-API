package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

@Converter(autoApply = true)
public class TipoDenunciaConverter implements AttributeConverter<TipoDenuncia, PGobject> {

    @Override
    public PGobject convertToDatabaseColumn(TipoDenuncia attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            PGobject dbObject = new PGobject();
            dbObject.setType("tipo_denuncia");
            dbObject.setValue(attribute.getValue());
            return dbObject;
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao converter TipoDenuncia para banco de dados", e);
        }
    }

    @Override
    public TipoDenuncia convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null) {
            return null;
        }
        return TipoDenuncia.fromValue(dbData.getValue());
    }
}
