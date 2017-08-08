package gov.samhsa.c2s.pcm.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class I18nMessage {
    @Id
    @GeneratedValue
    private long id;

    private String key;

    private String description;

    private String message;

    private String locale;
}