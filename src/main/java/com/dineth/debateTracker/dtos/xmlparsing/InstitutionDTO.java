package com.dineth.debateTracker.dtos.xmlparsing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor

public class InstitutionDTO {
    private String id;
    private String name;
    private String reference;
    private Long dbId;

    public InstitutionDTO(String id, String name, String reference) {
        this.id = id;
        this.name = name;
        this.reference = reference;
    }
}
