package gov.samhsa.c2s.pcm.config;

import gov.samhsa.c2s.pcm.domain.Organization;
import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifierDto;
import gov.samhsa.c2s.pcm.service.dto.OrganizationDto;
import gov.samhsa.c2s.pcm.service.dto.PractitionerDto;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Configuration
public class ModelMapperConfig {

    /**
     * Initializes {@link ModelMapper} with available {@link PropertyMap} instances.
     *
     * @param propertyMaps
     * @return
     */
    @Bean
    public ModelMapper modelMapper(List<PropertyMap> propertyMaps) {
        final ModelMapper modelMapper = new ModelMapper();
        propertyMaps.stream().filter(Objects::nonNull).forEach(modelMapper::addMappings);
        return modelMapper;
    }

    // PropertyMaps

    /**
     * Customizes mapping of properties from {@link Organization} to {@link OrganizationDto}
     */
    @Component
    static class OrganizationToOrganizationDtoMap extends PropertyMap<Organization, OrganizationDto> {
        @Autowired
        private IdentifierToSetOfIdentifierDtoConverter identifierToSetOfIdentifierDtoConverter;

        @Override
        protected void configure() {
            using(identifierToSetOfIdentifierDtoConverter).map(source).setIdentifiers(null);
        }
    }

    /**
     * Customizes mapping of properties from {@link FlattenedSmallProviderDto} to {@link OrganizationDto}
     */
    @Component
    static class FlattenedSmallProviderDtoToOrganizationDtoMap extends PropertyMap<FlattenedSmallProviderDto, OrganizationDto> {
        @Autowired
        private FlattenedSmallProviderDtoToIdentifiersDtoConverter flattenedSmallProviderDtoToIdentifiersDtoConverter;

        @Override
        protected void configure() {
            using(flattenedSmallProviderDtoToIdentifiersDtoConverter).map(source).setIdentifiers(null);
            map().setName(source.getOrganizationName());
            skip().setProviderType(null);
            map().getAddress().setLine1(source.getFirstLinePracticeLocationAddress());
            map().getAddress().setLine2(source.getSecondLinePracticeLocationAddress());
            map().getAddress().setCity(source.getPracticeLocationAddressCityName());
            map().getAddress().setState(source.getPracticeLocationAddressStateName());
            map().getAddress().setPostalCode(source.getPracticeLocationAddressPostalCode());
            map().getAddress().setCountry(source.getPracticeLocationAddressCountryCode());
        }
    }

    /**
     * Customizes mapping of properties from {@link FlattenedSmallProviderDto} to {@link PractitionerDto}
     */
    @Component
    static class FlattenedSmallProviderDtoToPractitionerDtoMap extends PropertyMap<FlattenedSmallProviderDto, PractitionerDto> {
        @Autowired
        private FlattenedSmallProviderDtoToIdentifiersDtoConverter flattenedSmallProviderDtoToIdentifiersDtoConverter;

        @Override
        protected void configure() {
            using(flattenedSmallProviderDtoToIdentifiersDtoConverter).map(source).setIdentifiers(null);
            skip().setProviderType(null);
            map().getAddress().setLine1(source.getFirstLinePracticeLocationAddress());
            map().getAddress().setLine2(source.getSecondLinePracticeLocationAddress());
            map().getAddress().setCity(source.getPracticeLocationAddressCityName());
            map().getAddress().setState(source.getPracticeLocationAddressStateName());
            map().getAddress().setPostalCode(source.getPracticeLocationAddressPostalCode());
            map().getAddress().setCountry(source.getPracticeLocationAddressCountryCode());
        }
    }

    // Converters

    /**
     * Converts {@link Identifier to {@link Set} of {@link IdentifierDto}}
     */
    @Component
    static class IdentifierToSetOfIdentifierDtoConverter extends AbstractConverter<Identifier, Set<IdentifierDto>> {
        @Override
        protected Set<IdentifierDto> convert(Identifier source) {
            return Stream.of(IdentifierDto.of(source.getSystem(), source.getValue())).collect(toSet());
        }
    }

    /**
     * Converts {@link FlattenedSmallProviderDto to {@link Set} of {@link IdentifierDto}}
     */
    @Component
    static class FlattenedSmallProviderDtoToIdentifiersDtoConverter extends AbstractConverter<FlattenedSmallProviderDto, Set<IdentifierDto>> {
        @Override
        protected Set<IdentifierDto> convert(FlattenedSmallProviderDto source) {
            return Stream.of(IdentifierDto.of(source.getSystem(), source.getNpi())).collect(toSet());
        }
    }
}
