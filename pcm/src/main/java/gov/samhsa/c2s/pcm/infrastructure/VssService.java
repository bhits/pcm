package gov.samhsa.c2s.pcm.infrastructure;

import gov.samhsa.c2s.pcm.infrastructure.dto.ValueSetCategoryDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "vss")
public interface VssService {

    @RequestMapping(value = "/valueSetCategories", method = RequestMethod.GET)
    List<ValueSetCategoryDto> getValueSetCategories();
}
