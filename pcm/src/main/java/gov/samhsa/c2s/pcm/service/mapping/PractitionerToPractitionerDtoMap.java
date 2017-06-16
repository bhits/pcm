package gov.samhsa.c2s.pcm.service.mapping;

import gov.samhsa.c2s.pcm.domain.Practitioner;
import gov.samhsa.c2s.pcm.service.dto.PractitionerDto;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PractitionerToPractitionerDtoMap extends PropertyMap<Practitioner, PractitionerDto> {
    @Autowired
    private IdentifierToIdentifierSetDtoConverter identifierToIdentifierSetDtoConverter;

    @Override
    protected void configure() {
        using(identifierToIdentifierSetDtoConverter).map(source.getProvider().getIdentifier()).setIdentifiers(null);
    }
}
