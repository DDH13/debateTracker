package com.dineth.debateTracker.utils;

import com.dineth.debateTracker.debater.Debater;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class ParseCSV {
    public String path;

    public ParseCSV(String path) {
        this.path = path;
    }

    public List<Debater> parseDebaterInfo() {
        try {
            List<Debater> debaters = new ArrayList<>();
            CSVReader reader = new CSVReader(new FileReader("src/main/resources/static/Debater_Information.csv"));
            String[] line;
            String phoneNumber;
            //skip the first line
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                Debater debater = new Debater();
                try {
                    phoneNumber = StringUtil.parsePhoneNumber(line[4]);
                    debater.setPhone(phoneNumber);
                    log.debug(", Phone Number: " + phoneNumber);
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage());
                }
                try {
                    String firstName = StringUtil.capitalizeName(line[1]).trim();
                    debater.setFirstName(firstName);
                    log.debug(", First Name: " + firstName);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                try {
                    String lastName = StringUtil.capitalizeName(line[2]).trim();
                    debater.setLastName(lastName);
                    log.debug(", Last Name: " + lastName);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                try {
                    SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = parser.parse(line[3]);
                    log.debug(", DOB: " + date);
                    debater.setBirthdate(date);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                try {
                    debater.setEmail(line[5].trim());
                    log.debug(", Email: " + line[5]);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                try {
                    debater.setDistrict(line[7].trim());
                    log.debug(", District: " + line[7]);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                try {
                    String gender = StringUtil.parseGender(line[8]);
                    debater.setGender(gender);
                    log.debug(", Gender: " + debater.getGender());
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage());
                }
                try{
                    if (line.length > 9 && line[9] != null && !line[9].trim().isEmpty()) {
                        String fullName = line[9].trim();
                        debater.setFullName(fullName);
                        log.debug(", Full Name: " + debater.getFullName());
                    } else {
                        log.debug(", Full Name missing");
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                debaters.add(debater);
            }
            reader.close();
            return debaters;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
