package com.dineth.debateTracker.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor

public class InstitutionDTO {
    public String id;
    public String name;
    public String reference;
    public Long dbId;

    public InstitutionDTO(String id, String name, String reference) {
        this.id = id;
        this.name = name;
        this.reference = reference;
    }
}
