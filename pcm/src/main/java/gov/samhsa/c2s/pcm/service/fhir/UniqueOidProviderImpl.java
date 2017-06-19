package gov.samhsa.c2s.pcm.service.fhir;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * The Class UniqueOidProviderImpl.
 */
@Service
public class UniqueOidProviderImpl implements UniqueOidProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see UniqueOidProvider#getOid()
	 */
	@Override
	public String getOid() {
		final UUID uuid = UUID.randomUUID();
		String id = String.valueOf(uuid);

		id = id.replace("-", ".");

		id = id.replace("a", "10");
		id = id.replace("b", "11");
		id = id.replace("c", "12");
		id = id.replace("d", "13");
		id = id.replace("e", "14");
		id = id.replace("f", "15");

		// Removes leading zeroes
		id = id.replaceFirst("^0+(?!$)", "");

		return id;
	}

}
