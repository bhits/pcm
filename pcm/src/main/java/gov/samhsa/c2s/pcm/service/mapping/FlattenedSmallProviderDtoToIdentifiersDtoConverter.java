package gov.samhsa.c2s.pcm.service.mapping;

import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifierDto;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Component
public class FlattenedSmallProviderDtoToIdentifiersDtoConverter extends AbstractConverter<FlattenedSmallProviderDto, Set<IdentifierDto>>{

        @Override
        protected Set<IdentifierDto> convert(FlattenedSmallProviderDto source) {
            return Stream.of(IdentifierDto.of(source.getSystem(), source.getNpi())).collect(toSet());
        }
    }
