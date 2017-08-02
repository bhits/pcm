package gov.samhsa.c2s.pcm.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class I18nMessage {
    @Id
    @GeneratedValue
    private long id;

    private String code;

    private String en;

    private String es;

}