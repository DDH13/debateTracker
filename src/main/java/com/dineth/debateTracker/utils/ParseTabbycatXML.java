package com.dineth.debateTracker.utils;

import com.dineth.debateTracker.debater.Debater;
import com.dineth.debateTracker.dtos.*;
import com.dineth.debateTracker.judge.Judge;
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
import java.util.Arrays;
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

    public TournamentDTO getTournamentDTO(Document document) {
        Node tournament = document.getElementsByTagName("tournament").item(0);
        String fullName = tournament.getAttributes().getNamedItem("name").getNodeValue();
        String shortName = tournament.getAttributes().getNamedItem("short").getNodeValue();

        return new TournamentDTO(fullName, shortName);
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

    public List<InstitutionDTO> getInstitutionDTOs(Document document) {
        NodeList institutionList = document.getElementsByTagName("institution");
        List<InstitutionDTO> institutionDTOs = new ArrayList<>();
        for (int i = 0; i < institutionList.getLength(); i++) {
            Node institution = institutionList.item(i);
            if (institution.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = institution.getAttributes();
                String institutionCode = attributes.getNamedItem("reference").getNodeValue();
                String institutionId = attributes.getNamedItem("id").getNodeValue();
                String institutionName = institution.getTextContent();

                InstitutionDTO temp = new InstitutionDTO(institutionId, institutionName, institutionCode);
                institutionDTOs.add(temp);
            }
        }
        return institutionDTOs;
    }

    public List<MotionDTO> getMotionDTOs(Document document) {
        List<MotionDTO> motionDTOs = new ArrayList<>();
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
                MotionDTO temp = new MotionDTO(motionId, motionText, infoslide, motionCode);
                motionDTOs.add(temp);
            }
        }
        return motionDTOs;
    }

    public List<RoundDTO> getRoundsDTO(Document document) {
        List<RoundDTO> roundDTOs = new ArrayList<>();
        NodeList roundList = document.getElementsByTagName("round");
        for (int i = 0; i < roundList.getLength(); i++) {
            Node round = roundList.item(i);
            if (round.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = round.getAttributes();
                String roundAbbreviation = attributes.getNamedItem("abbreviation").getNodeValue();
                String roundName = attributes.getNamedItem("name").getNodeValue();
                boolean isBreakRound = Boolean.parseBoolean(attributes.getNamedItem("elimination").getNodeValue());
                double feedbackWeight;
                if (attributes.getNamedItem("feedback_weight") != null) {
                    feedbackWeight = Double.parseDouble(attributes.getNamedItem("feedback_weight").getNodeValue());
                } else {
                    feedbackWeight = 1.0;
                }
                roundDTOs.add(new RoundDTO(roundName, roundAbbreviation, isBreakRound, feedbackWeight, getDebatesDTO(round)));
            }
        }
        return roundDTOs;
    }

    private List<DebateDTO> getDebatesDTO(Node round) {
        List<DebateDTO> debateDTOS = new ArrayList<>();
        NodeList debatesList = round.getChildNodes();
        for (int i = 0; i < debatesList.getLength(); i++) {
            Node debate = debatesList.item(i);
            if (debate.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = debate.getAttributes();
                String debateId = attributes.getNamedItem("id").getNodeValue();
                String debateVenue = attributes.getNamedItem("venue").getNodeValue();
                String adjudicators = attributes.getNamedItem("adjudicators").getNodeValue();
                String chair = attributes.getNamedItem("chair").getNodeValue();
                List<SideDTO> sideDTOs = getSidesDTO(debate);
                debateDTOS.add(new DebateDTO(debateId, adjudicators, chair, debateVenue, null, sideDTOs));
            }

        }
        return debateDTOS;
    }

    private List<SideDTO> getSidesDTO(Node debate) {
        List<SideDTO> sideDTOs = new ArrayList<>();
        NodeList sidesList = debate.getChildNodes();
        for (int i = 0; i < sidesList.getLength(); i++) {
            Node side = sidesList.item(i);
            if (side.getNodeType() == Node.ELEMENT_NODE && side.getNodeName().equals("side")) {
                NamedNodeMap attributes = side.getAttributes();
                String teamId = attributes.getNamedItem("team").getNodeValue();
                List<FinalTeamBallotDTO> finalTeamBallotDTOs = getFinalTeamBallotDTOs(side);
                List<SpeechDTO> speechDTOs = getSpeechDTOs(side);
                sideDTOs.add(new SideDTO(teamId, finalTeamBallotDTOs, speechDTOs));
            }
        }
        return sideDTOs;
    }

    private List<SpeechDTO> getSpeechDTOs(Node side) {
        List<SpeechDTO> speechDTOs = new ArrayList<>();
        NodeList speechList = side.getChildNodes();
        for (int i = 0; i < speechList.getLength(); i++) {
            Node speech = speechList.item(i);
            if (speech.getNodeType() == Node.ELEMENT_NODE && speech.getNodeName().equals("speech")) {
                NamedNodeMap attributes = speech.getAttributes();
                String debaterId = attributes.getNamedItem("speaker").getNodeValue();
                boolean reply = Boolean.parseBoolean(attributes.getNamedItem("reply").getNodeValue());
                List<IndividualSpeechBallotDTO> individualSpeechBallotDTOs = getIndividualSpeechBallotDTOs(speech);
                speechDTOs.add(new SpeechDTO(debaterId, reply, individualSpeechBallotDTOs));
            }
        }
        return speechDTOs;
    }

    private List<IndividualSpeechBallotDTO> getIndividualSpeechBallotDTOs(Node speech) {
        List<IndividualSpeechBallotDTO> individualSpeechBallotDTOs = new ArrayList<>();
        NodeList ballotList = speech.getChildNodes();
        for (int i = 0; i < ballotList.getLength(); i++) {
            Node ballot = ballotList.item(i);
            if (ballot.getNodeType() == Node.ELEMENT_NODE && ballot.getNodeName().equals("ballot")) {
                NamedNodeMap attributes = ballot.getAttributes();
                String adjudicatorId = attributes.getNamedItem("adjudicators").getNodeValue();
                double score = Double.parseDouble(ballot.getTextContent());
                individualSpeechBallotDTOs.add(new IndividualSpeechBallotDTO(adjudicatorId, score));
            }
        }
        return individualSpeechBallotDTOs;
    }

    private List<FinalTeamBallotDTO> getFinalTeamBallotDTOs(Node side) {
        NodeList finalTeamBallotList = side.getChildNodes();
        List<FinalTeamBallotDTO> finalTeamBallotDTOs = new ArrayList<>();
        for (int i = 0; i < finalTeamBallotList.getLength(); i++) {
            Node finalTeamBallot = finalTeamBallotList.item(i);
            if (finalTeamBallot.getNodeType() == Node.ELEMENT_NODE && finalTeamBallot.getNodeName().equals("ballot")) {
                NamedNodeMap attributes = finalTeamBallot.getAttributes();
                String[] adjudicatorIdsArray = attributes.getNamedItem("adjudicators").getNodeValue().split(" ");
                List<String> adjudicatorIds = new ArrayList<>(Arrays.asList(adjudicatorIdsArray));
                boolean ignored = Boolean.parseBoolean(attributes.getNamedItem("ignored").getNodeValue());
                boolean minority = false;
                if (attributes.getNamedItem("minority") != null) {
                    minority = Boolean.parseBoolean(attributes.getNamedItem("minority").getNodeValue());
                }
                double score = 0;
                //check if text content is a double
                if (finalTeamBallot.getTextContent().matches(".*\\d.*")) {
                    score = Double.parseDouble(finalTeamBallot.getTextContent());
                }
                int rank = Integer.parseInt(attributes.getNamedItem("rank").getNodeValue());
                finalTeamBallotDTOs.add(new FinalTeamBallotDTO(adjudicatorIds, minority, ignored, rank, score));
            }
        }
        return finalTeamBallotDTOs;
    }
}