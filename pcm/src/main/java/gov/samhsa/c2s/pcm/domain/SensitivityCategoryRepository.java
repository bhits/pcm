package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.config.SpringContext;
import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import gov.samhsa.c2s.pcm.infrastructure.VssService;
import gov.samhsa.c2s.pcm.infrastructure.dto.ValueSetCategoryDto;
import gov.samhsa.c2s.pcm.service.exception.InvalidSensitivityCategoryException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.Assert;

import java.util.Optional;

public interface SensitivityCategoryRepository extends JpaRepository<SensitivityCategory, Long> {
    Optional<SensitivityCategory> findOneByIdentifierSystemAndIdentifierValue(String system, String value);

    default SensitivityCategory saveAndGet(String system, String value) {
        Assert.hasText(system, "system must have text");
        Assert.hasText(value, "value must have text");
        return findOneByIdentifierSystemAndIdentifierValue(system, value)
                .orElseGet(() -> {
                    final VssService vssService = SpringContext.getBean(VssService.class);
                    final ValueSetCategoryDto valueSetCategoryDto = vssService.getValueSetCategories().stream()
                            .filter(category -> system.equals(category.getSystem()) && value.equals(category.getCode()))
                            .findAny().orElseThrow(InvalidSensitivityCategoryException::new);
                    final SensitivityCategory sensitivityCategory = SensitivityCategory.builder()
                            .identifier(Identifier.builder().system(valueSetCategoryDto.getSystem()).value(valueSetCategoryDto.getCode()).build())
                            .description(valueSetCategoryDto.getDescription())
                            .display(valueSetCategoryDto.getDisplayName())
                            .build();
                    return save(sensitivityCategory);
                });
    }
}
