package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.Patient;
import gov.samhsa.c2s.pcm.domain.PatientRepository;
import gov.samhsa.c2s.pcm.infrastructure.PhrService;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ConsentServiceImpl implements ConsentService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PhrService phrService;

    @Override
    @Transactional
    public List<ConsentDto> getConsents(Long patientId) {
        final Patient patient = patientRepository.saveAndGet(patientId);
        return patient.getConsents().stream()
                .map(consent -> new ConsentDto())
                .collect(toList());
    }
}
