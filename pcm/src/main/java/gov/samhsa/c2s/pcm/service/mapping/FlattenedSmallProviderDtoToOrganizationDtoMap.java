package gov.samhsa.c2s.pcm.service.mapping;

import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.service.dto.OrganizationDto;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class FlattenedSmallProviderDtoToOrganizationDtoMap extends PropertyMap<FlattenedSmallProviderDto, OrganizationDto> {
    @Autowired
    private FlattenedSmallProviderDtoToIdentifiersDtoConverter flattenedSmallProviderDtoToIdentifiersDtoConverter;

    @Override
    protected void configure() {
        using(flattenedSmallProviderDtoToIdentifiersDtoConverter).map(source).setIdentifiers(null);
        map().setName(source.getOrganizationName());
        map().getAddress().setLine1(source.getFirstLinePracticeLocationAddress());
        map().getAddress().setLine2(source.getSecondLinePracticeLocationAddress());
        map().getAddress().setCity(source.getPracticeLocationAddressCityName());
        map().getAddress().setState(source.getPracticeLocationAddressStateName());
        map().getAddress().setPostalCode(source.getPracticeLocationAddressPostalCode());
        map().getAddress().setCountry(source.getPracticeLocationAddressCountryCode());
        map().setPhoneNumber(source.getPracticeLocationAddressTelephoneNumber());
    }
}
