package com.dineth.debateTracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor @AllArgsConstructor
@Setter @Getter
public class DebaterMergeInfoDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private Integer roundsDebated;
    private List<String> teams;
}
