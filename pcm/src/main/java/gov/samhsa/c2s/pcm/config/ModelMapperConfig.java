package gov.samhsa.c2s.pcm.config;

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

}
