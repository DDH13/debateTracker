package com.dineth.debateTracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor @AllArgsConstructor
@Setter @Getter
public class DebaterMergeInfoDTO {
    Long id;
    String firstName;
    String lastName;
    String fullName;
    String phone;
    Integer roundsDebated;
    List<String> teams;
}
