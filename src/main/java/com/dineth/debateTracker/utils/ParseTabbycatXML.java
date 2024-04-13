package com.dineth.debateTracker.utils;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.institution.Institution;
import com.dineth.debateTracker.judge.Judge;
import com.dineth.debateTracker.motion.Motion;
import com.dineth.debateTracker.team.Team;
import com.dineth.debateTracker.tournament.Tournament;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

public class ParseTabbycatXML {
    String xmlPath;
    public Document document;

    public ParseTabbycatXML(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public void parseXML() {
        try {
            // Parse the XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(this.xmlPath); // Specify the path to your XML file

            // Normalize the XML Structure
            document.getDocumentElement().normalize();

            System.out.println("Parsed XML file: " + this.xmlPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Tournament getTournament(Document document) {
        Node tournament = document.getElementsByTagName("tournament").item(0);
        String fullName = tournament.getAttributes().getNamedItem("name").getNodeValue();
        String shortName = tournament.getAttributes().getNamedItem("short").getNodeValue();

        return new Tournament(fullName, shortName);
    }

    public ImmutablePair<List<Debater>, List<Team>> getTeamsAndSpeakers(Document document) {
        List<Debater> debaters = new ArrayList<>();
        List<Team> teams = new ArrayList<>();
        NodeList teamList = document.getElementsByTagName("team");
        for (int i = 0; i < teamList.getLength(); i++) {
            Node team = teamList.item(i);

            if (team.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = team.getAttributes();
                String teamName = attributes.getNamedItem("name").getNodeValue();
                String teamCode = attributes.getNamedItem("code").getNodeValue();
                String teamId = attributes.getNamedItem("id").getNodeValue();

                Team tempTeam = new Team(teamId, teamName, teamCode, null, null);

                List<Debater> teamDebaters = new ArrayList<>();
                List<String> institutionIdsOfDebatersInTeam = new ArrayList<>();
                NodeList speakerList = team.getChildNodes();
                for (int j = 0; j < speakerList.getLength(); j++) {
                    Node speaker = speakerList.item(j);
                    if (speaker.getNodeType() == Node.ELEMENT_NODE) {
                        NamedNodeMap speakerAttributes = speaker.getAttributes();
                        String speakerId = speakerAttributes.getNamedItem("id").getNodeValue();
                        String speakerName = speaker.getTextContent();
                        String speakerInstitutionId = speakerAttributes.getNamedItem("institutions").getNodeValue();
                        if (!institutionIdsOfDebatersInTeam.contains(speakerInstitutionId)) {
                            institutionIdsOfDebatersInTeam.add(speakerInstitutionId);
                        }
                        ImmutablePair<String, String> names = StringUtil.splitName(speakerName);

                        Debater tempDebater = new Debater(speakerId, names.getLeft(), names.getRight(), null);

                        teamDebaters.add(tempDebater);

                    }
                    tempTeam.setDebaters(teamDebaters);
                }
                //TODO set institution and account for swing
                if (institutionIdsOfDebatersInTeam.size() == 1) {
                    tempTeam.setInstitutionId(institutionIdsOfDebatersInTeam.get(0));
                }
//                else {
//                    tempTeam.setInstitution(new Institution("swing", "Swing", "swing"));
//                }
                teams.add(tempTeam);
                debaters.addAll(teamDebaters);
            }
        }
        return new ImmutablePair<>(debaters, teams);
    }

    public List<Judge> getJudges(Document document) {
        NodeList adjudicatorList = document.getElementsByTagName("adjudicator");
        List<Judge> judges = new ArrayList<>();
        for (int j = 0; j < adjudicatorList.getLength(); j++) {
            Node adjudicator = adjudicatorList.item(j);
            if (adjudicator.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = adjudicator.getAttributes();
                String adjName = attributes.getNamedItem("name").getNodeValue();
                ImmutablePair<String, String> names = StringUtil.splitName(adjName);
                Float adjScore = Float.valueOf(attributes.getNamedItem("score").getNodeValue());
                String adjId = attributes.getNamedItem("id").getNodeValue();

                Judge temp = new Judge(adjId, adjScore, names.getLeft(), names.getRight());
                judges.add(temp);
            }
        }
        return judges;
    }

    public List<Institution> getInstitutions(Document document) {
        NodeList institutionList = document.getElementsByTagName("institution");
        List<Institution> institutions = new ArrayList<>();
        for (int i = 0; i < institutionList.getLength(); i++) {
            Node institution = institutionList.item(i);
            if (institution.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = institution.getAttributes();
                String institutionCode = attributes.getNamedItem("reference").getNodeValue();
                String institutionId = attributes.getNamedItem("id").getNodeValue();
                String institutionName = institution.getTextContent();

                Institution temp = new Institution(institutionId, institutionName, institutionCode);
                institutions.add(temp);
            }
        }
        return institutions;
    }

    public List<Motion> getMotions(Document document) {
        List<Motion> motions = new ArrayList<>();
        NodeList motionList = document.getElementsByTagName("motion");
        for (int i = 0; i < motionList.getLength(); i++) {
            Node motion = motionList.item(i);
            if (motion.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = motion.getAttributes();
                String motionId = attributes.getNamedItem("id").getNodeValue();
                String motionCode = attributes.getNamedItem("reference").getNodeValue();
                String infoslide = "";
                NodeList mc = motion.getChildNodes();
                String motionText = mc.item(0).getTextContent();
                if (mc.getLength() > 1) {
                    infoslide = mc.item(1).getTextContent();
                }
                Motion temp = new Motion(motionText, motionId, infoslide, motionCode);
                motions.add(temp);
            }
        }
        return motions;
    }

    public void getRounds(Document document) {
        NodeList roundList = document.getElementsByTagName("round");
        for (int i = 0; i < roundList.getLength(); i++) {
            Node round = roundList.item(i);
            if (round.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = round.getAttributes();
                String roundId = attributes.getNamedItem("abbreviation").getNodeValue();
                String roundName = attributes.getNamedItem("name").getNodeValue();
                Boolean isBreakRound = Boolean.valueOf(attributes.getNamedItem("elimination").getNodeValue());
                System.out.println("Round: " + roundName + ", ID: " + roundId + ", Break Round: " + isBreakRound);
                getRooms(round);
            }
        }
    }

    private void getRooms(Node round) {
        NodeList debatesList = round.getChildNodes();
        for (int i = 0; i < debatesList.getLength(); i++) {
            Node debate = debatesList.item(i);
            if (debate.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = debate.getAttributes();
                String debateId = attributes.getNamedItem("id").getNodeValue();
                String debateVenue = attributes.getNamedItem("venue").getNodeValue();
                String debateMotion = attributes.getNamedItem("motion").getNodeValue();
                System.out.println("\tDebate: " + debateId + ", Venue: " + debateVenue + ", Motion: " + debateMotion);


            }
        }
    }
}


