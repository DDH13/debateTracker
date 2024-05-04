package com.dineth.debateTracker.debater;

import com.dineth.debateTracker.utils.CustomExceptions;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class DebaterService {
    private final DebaterRepository debaterRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public DebaterService(DebaterRepository debaterRepository, RestTemplateBuilder restTemplateBuilder) {
        this.debaterRepository = debaterRepository;
        this.restTemplate = restTemplateBuilder.build();
    }

    public List<Debater> getDebaters() {
        return debaterRepository.findAll();
    }

    public Debater addDebater(Debater debater) {
        Debater temp = checkIfDebaterExists(debater);
        if (temp != null) {
            return temp;
        }
        return debaterRepository.save(debater);
    }

    public Debater findDebaterById(Long id) {
        return debaterRepository.findById(id).orElse(null);
    }

    public Debater checkIfDebaterExists(Debater debater) {
        List<Debater> debaters;
        if (debater.getBirthdate()!=null){
            debaters = debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCaseAndBirthdate(debater.getFirstName(), debater.getLastName(), debater.getBirthdate());
        } else {
            debaters = debaterRepository.findDebatersByFirstNameEqualsIgnoreCaseAndLastNameEqualsIgnoreCase(debater.getFirstName(), debater.getLastName());
        }
        //TODO adjust code to accommodate debaters with same first name and last name
        if (debaters.size() == 1) {
            return debaters.get(0);
        } else if (debaters.size() > 1) {
            throw new CustomExceptions.MultipleDebatersFoundException("Multiple debaters found with the same name. Birthdate is required to identify the debater.");
        } else {
            return null;
        }
    }

    public void getDebatersFromAPI() {
        String API_KEY = "3a3362750dc52748956565d9f991b358dbd3d428";
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + API_KEY);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange("https://slsdceng.calicotab.com/api/v1/tournaments/slsdc2023/speakers", HttpMethod.GET, entity, String.class);
//            System.out.println(response.getBody());
            var body = response.getBody();
            List<Debater> debaters;
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            debaters = objectMapper.readValue(body, objectMapper.getTypeFactory().constructCollectionType(List.class, Debater.class));

            System.out.println(debaters);
            //length
            System.out.println(debaters.size());


            //save to db
            for (Debater debater : debaters) {
                this.addDebater(debater);
            }

//            {"id":233,"url":"https://slsdceng.calicotab.com/api/v1/tournaments/slsdc2023/speakers/233","team":"https://slsdceng.calicotab.com/api/v1/tournaments/slsdc2023/teams/61","categories":[],"_links":{"checkin":"https://slsdceng.calicotab.com/api/v1/tournaments/slsdc2023/speakers/233/checkin"},"name":"Yuhani Jayawardana","email":"yuhanijayawardana@gmail.com","phone":"","anonymous":false,"code_name":"67395536","url_key":"ihaolw6m","gender":"","pronoun":""}




//            if (debaters != null) {
//                for (Debater debater : debaters) {
//                    this.addNewDebater(debater);
//                }
//            } else {
//                System.out.println("No debaters found");
//            }
        } catch (Exception e) {
            System.out.println("Error fetching debaters: " + e.getMessage());
        }

    }

}
