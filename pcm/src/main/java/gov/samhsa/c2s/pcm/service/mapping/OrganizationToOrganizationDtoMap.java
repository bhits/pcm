package gov.samhsa.c2s.pcm.service.mapping;

import gov.samhsa.c2s.pcm.domain.Organization;
import gov.samhsa.c2s.pcm.service.dto.OrganizationDto;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrganizationToOrganizationDtoMap extends PropertyMap<Organization, OrganizationDto> {
    @Autowired
    private IdentifierToIdentifierSetDtoConverter identifierToIdentifierSetDtoConverter;

    @Override
    protected void configure() {
        using(identifierToIdentifierSetDtoConverter).map(source.getProvider().getIdentifier()).setIdentifiers(null);
    }
}
