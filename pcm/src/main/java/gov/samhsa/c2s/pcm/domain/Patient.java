package gov.samhsa.c2s.pcm.domain;

import lombok.Data;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Audited
public class Patient {
    @Id
    private Long id;

    @OneToMany(mappedBy = "patient")
    @NotAudited
    private List<Consent> consents = new ArrayList<>();

    @ManyToMany
    @NotAudited
    private List<Provider> providers = new ArrayList<>();
}
