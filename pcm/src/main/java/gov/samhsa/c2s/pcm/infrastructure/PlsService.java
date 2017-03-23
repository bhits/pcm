package gov.samhsa.c2s.pcm.infrastructure;

import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("pls")
public interface PlsService {

    @RequestMapping(value = "/providers/{npi}", method = RequestMethod.GET)
    FlattenedSmallProviderDto getFlattenedSmallProvider(
            @PathVariable("npi") String npi,
            @RequestParam(value = "projection", defaultValue = PlsService.Projection.FLATTEN_SMALL_PROVIDER) String projection);

    interface Projection {
        String FLATTEN_SMALL_PROVIDER = "FlattenSmallProvider";
    }
}
