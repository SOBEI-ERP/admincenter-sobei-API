package br.org.sobei.denuncias.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

@Converter(autoApply = true)
public class StatusDenunciaConverter implements AttributeConverter<StatusDenuncia, PGobject> {

    @Override
    public PGobject convertToDatabaseColumn(StatusDenuncia attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            PGobject dbObject = new PGobject();
            dbObject.setType("estado_denuncia");
            dbObject.setValue(attribute.getValue());
            return dbObject;
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao converter StatusDenuncia para banco de dados", e);
        }
    }

    @Override
    public StatusDenuncia convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null) {
            return null;
        }
        return StatusDenuncia.fromValue(dbData.getValue());
    }
}
