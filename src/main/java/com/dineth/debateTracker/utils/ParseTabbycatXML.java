package com.dineth.debateTracker.utils;

import com.dineth.debateTracker.dtos.xmlparsing.*;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
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

        } catch (Exception e) {
            log.error("Error parsing XML file: " + e.getMessage());
        }
    }

    public TournamentDTO getTournamentDTO(Document document) {
        try {
            Node tournament = document.getElementsByTagName("tournament").item(0);
            String fullName = tournament.getAttributes().getNamedItem("name").getNodeValue();
            String shortName = tournament.getAttributes().getNamedItem("short").getNodeValue();
            return new TournamentDTO(fullName, shortName);
        } catch (Exception e) {
            log.error("Error parsing tournament DTO: " + e.getMessage());
            throw e;
        }
    }

    public List<BreakCategoryDTO> getBreakCategoryDTOs(Document document) {
        try {
            NodeList breakCategoryList = document.getElementsByTagName("break-category");
            List<BreakCategoryDTO> breakCategoryDTOs = new ArrayList<>();
            for (int i = 0; i < breakCategoryList.getLength(); i++) {
                Node breakCategory = breakCategoryList.item(i);
                if (breakCategory.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap attributes = breakCategory.getAttributes();
                    String id = attributes.getNamedItem("id").getNodeValue();
                    String name = breakCategory.getTextContent();
                    breakCategoryDTOs.add(new BreakCategoryDTO(id, name));
                }
            }
            return breakCategoryDTOs;
        } catch (Exception e) {
            log.error("Error parsing break category DTO: " + e.getMessage());
            throw e;
        }

    }

    public List<TeamDTO> getTeamDTOs(Document document) {
        try {
            NodeList teamList = document.getElementsByTagName("team");
            List<TeamDTO> teamDTOs = new ArrayList<>();
            for (int i = 0; i < teamList.getLength(); i++) {
                Node team = teamList.item(i);
                if (team.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap attributes = team.getAttributes();
                    String teamName = attributes.getNamedItem("name").getNodeValue();
                    String teamCode = attributes.getNamedItem("code").getNodeValue();
                    String teamId = attributes.getNamedItem("id").getNodeValue();
                    String breakEligibilities = attributes.getNamedItem("break-eligibilities").getNodeValue();
                    List<DebaterDTO> debaterDTOs = getDebaterDTOs(team);
                    TeamDTO temp = new TeamDTO(teamId, teamName, teamCode, debaterDTOs, breakEligibilities);
                    teamDTOs.add(temp);
                }
            }
            return teamDTOs;
        } catch (Exception e) {
            log.error("Error parsing team DTO: " + e.getMessage());
            throw e;
        }
    }

    public List<DebaterDTO> getDebaterDTOs(Node team) {
        List<DebaterDTO> debaterDTOs = new ArrayList<>();
        NodeList speakerList = team.getChildNodes();
        for (int j = 0; j < speakerList.getLength(); j++) {
            Node speaker = speakerList.item(j);
            if (speaker.getNodeType() == Node.ELEMENT_NODE) {
                DebaterDTO temp = getDebaterDTO(speaker);
                debaterDTOs.add(temp);
            }
        }
        return debaterDTOs;
    }

    private static DebaterDTO getDebaterDTO(Node speaker) {
        try {
            NamedNodeMap speakerAttributes = speaker.getAttributes();
            String speakerId = speakerAttributes.getNamedItem("id").getNodeValue();
            String speakerName = speaker.getTextContent();
            Node institutions = speakerAttributes.getNamedItem("institutions");
            String speakerInstitutionId;
            if (institutions != null) {
                speakerInstitutionId = institutions.getNodeValue();
            } else {
                speakerInstitutionId = "";
            }
            String categories = speakerAttributes.getNamedItem("categories").getNodeValue();
            return new DebaterDTO(speakerId, speakerName, speakerInstitutionId, categories);
        } catch (Exception e) {
            log.error("Error parsing debater DTO: " + e.getMessage());
            throw e;
        }
    }

    public List<JudgeDTO> getJudgeDTOs(Document document) {
        try {
            NodeList adjudicatorList = document.getElementsByTagName("adjudicator");
            List<JudgeDTO> judgeDTOs = new ArrayList<>();
            for (int j = 0; j < adjudicatorList.getLength(); j++) {
                Node adjudicator = adjudicatorList.item(j);
                if (adjudicator.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap attributes = adjudicator.getAttributes();
                    String adjName = attributes.getNamedItem("name").getNodeValue();
                    String adjId = attributes.getNamedItem("id").getNodeValue();
                    Float adjScore = Float.valueOf(attributes.getNamedItem("score").getNodeValue());
                    Boolean core = Boolean.valueOf(attributes.getNamedItem("core").getNodeValue());
                    Boolean independent = Boolean.valueOf(attributes.getNamedItem("independent").getNodeValue());
                    JudgeDTO temp = new JudgeDTO(adjId, adjName, adjScore, core, independent);
                    judgeDTOs.add(temp);
                    temp.setFeedback(getFeedbackDTOs(adjudicator));
                }
            }
            return judgeDTOs;
        } catch (Exception e) {
            log.error("Error parsing judge DTO: " + e.getMessage());
            throw e;
        }
    }

    public List<FeedbackDTO> getFeedbackDTOs(Node judge) {
        NodeList feedbackList = judge.getChildNodes();
        List<FeedbackDTO> feedbackDTOs = new ArrayList<>();
        for (int k = 0; k < feedbackList.getLength(); k++) {
            FeedbackDTO feedbackDTO = new FeedbackDTO();
            Node feedback = feedbackList.item(k);
            try {
                if (feedback.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap feedbackAttributes = feedback.getAttributes();
                    String debateId = feedbackAttributes.getNamedItem("debate").getNodeValue();
                    Float overallRating = Float.valueOf(feedbackAttributes.getNamedItem("score").getNodeValue());
                    Node sourceTeamId = feedbackAttributes.getNamedItem("source-team");
                    Node sourceJudgeId = feedbackAttributes.getNamedItem("source-adjudicator");
                    feedbackDTO.setTargetJudgeId(judge.getAttributes().getNamedItem("id").getNodeValue());
                    if (sourceTeamId != null) feedbackDTO.setSourceTeamId(sourceTeamId.getNodeValue());
                    else feedbackDTO.setSourceJudgeId(sourceJudgeId.getNodeValue());
                    feedbackDTO.setDebateId(debateId);
                    feedbackDTO.setOverallRating(overallRating);
                    //get feedback question values
                    for (int l = 0; l < feedback.getChildNodes().getLength(); l++) {
                        Node feedbackValues = feedback.getChildNodes().item(l);
                        if (feedbackValues.getNodeType() == Node.ELEMENT_NODE) {
                            NamedNodeMap feedbackValuesAttributes = feedbackValues.getAttributes();
                            String question = feedbackValuesAttributes.getNamedItem("question").getNodeValue();

                            switch (question) {
                                case "Q37":
                                    feedbackDTO.setClashEvaluation(Float.valueOf(feedbackValues.getTextContent()));
                                    break;
                                case "Q36":
                                    feedbackDTO.setClashOrganization(Float.valueOf(feedbackValues.getTextContent()));
                                    break;
                                case "Q34":
                                    feedbackDTO.setTrackingArguments(Float.valueOf(feedbackValues.getTextContent()));
                                    break;
                                case "Q2":
                                    feedbackDTO.setComments(feedbackValues.getTextContent());
                                    break;
                                case "Q1":
                                    feedbackDTO.setAgree(feedbackValues.getTextContent());
                                    break;
                            }
                        }
                    }
                    feedbackDTOs.add(feedbackDTO);
                }
            } catch (Exception e) {
                log.error("Error parsing feedback DTO: " + e.getMessage());
            }
        }
        return feedbackDTOs;

    }

    public List<InstitutionDTO> getInstitutionDTOs(Document document) {
        try {
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
        } catch (Exception e) {
            log.error("Error parsing institution DTO: " + e.getMessage());
            throw e;
        }
    }

    public List<MotionDTO> getMotionDTOs(Document document) {
        try {
            List<MotionDTO> motionDTOs = new ArrayList<>();
            NodeList motionList = document.getElementsByTagName("motion");
            for (int i = 0; i < motionList.getLength(); i++) {
                Node motion = motionList.item(i);
                if (motion.getNodeType() == Node.ELEMENT_NODE) {
                    MotionDTO temp = getMotionDTO(motion);
                    motionDTOs.add(temp);
                }
            }
            return motionDTOs;
        } catch (Exception e) {
            log.error("Error parsing motion DTO: " + e.getMessage());
            throw e;
        }
    }

    private static MotionDTO getMotionDTO(Node motion) {
        try {
            NamedNodeMap attributes = motion.getAttributes();
            String motionId = attributes.getNamedItem("id").getNodeValue();
            String motionCode = attributes.getNamedItem("reference").getNodeValue();
            String infoslide = "";
            NodeList mc = motion.getChildNodes();
            String motionText = mc.item(0).getTextContent();
            if (mc.getLength() > 1) {
                infoslide = mc.item(1).getTextContent();
            }
            return new MotionDTO(motionId, motionText, infoslide, motionCode);
        } catch (Exception e) {
            log.error("Error parsing motion DTO: " + e.getMessage());
            throw e;
        }
    }

    public List<RoundDTO> getRoundsDTO(Document document) {
        try {
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
        } catch (Exception e) {
            log.error("Error parsing round DTO: " + e.getMessage());
            throw e;
        }
    }

    private List<DebateDTO> getDebatesDTO(Node round) {
        try {
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
        } catch (Exception e) {
            log.error("Error parsing debate DTO: " + e.getMessage());
            throw e;
        }
    }

    private List<SideDTO> getSidesDTO(Node debate) {
        try {
            List<SideDTO> sideDTOs = new ArrayList<>();
            NodeList sidesList = debate.getChildNodes();
            for (int i = 0; i < sidesList.getLength(); i++) {
                Node side = sidesList.item(i);
                if (side.getNodeType() == Node.ELEMENT_NODE && side.getNodeName().equals("side")) {
                    NamedNodeMap attributes = side.getAttributes();
                    String teamId = attributes.getNamedItem("team").getNodeValue();
                    List<FinalTeamBallotDTO> finalTeamBallotDTOs = getFinalTeamBallotDTOs(side);
                    List<SpeechDTO> speechDTOs = new ArrayList<>();
                    //check if there are speech nodes
                    NodeList sideChildNodes = side.getChildNodes();
                    for (int j = 0; j < sideChildNodes.getLength(); j++) {
                        Node sideChildNode = sideChildNodes.item(j);
                        if (sideChildNode.getNodeType() == Node.ELEMENT_NODE && sideChildNode.getNodeName().equals("speech")) {
                            speechDTOs = getSpeechDTOs(side);
                            break;
                        }
                    }
                    sideDTOs.add(new SideDTO(teamId, finalTeamBallotDTOs, speechDTOs));
                }
            }
            return sideDTOs;
        } catch (Exception e) {
            log.error("Error parsing side DTO: " + e.getMessage());
            throw e;
        }
    }

    private List<SpeechDTO> getSpeechDTOs(Node side) {
        try {
            List<SpeechDTO> speechDTOs = new ArrayList<>();
            NodeList speechList = side.getChildNodes();
            for (int i = 0; i < speechList.getLength(); i++) {
                Node speech = speechList.item(i);
                if (speech.getNodeType() == Node.ELEMENT_NODE && speech.getNodeName().equals("speech")) {
                    NamedNodeMap attributes = speech.getAttributes();
                    String debaterId = attributes.getNamedItem("speaker").getNodeValue();
                    boolean reply = Boolean.parseBoolean(attributes.getNamedItem("reply").getNodeValue());
                    Integer speakerPosition = speechDTOs.size() + 1;
                    List<IndividualSpeechBallotDTO> individualSpeechBallotDTOs = getIndividualSpeechBallotDTOs(speech);
                    speechDTOs.add(new SpeechDTO(debaterId, reply, individualSpeechBallotDTOs, speakerPosition));
                }
            }
            return speechDTOs;
        } catch (Exception e) {
            log.error("Error parsing speech DTO: " + e.getMessage());
            throw e;
        }
    }

    private List<IndividualSpeechBallotDTO> getIndividualSpeechBallotDTOs(Node speech) {
        try {
            List<IndividualSpeechBallotDTO> individualSpeechBallotDTOs = new ArrayList<>();
            NodeList ballotList = speech.getChildNodes();
            for (int i = 0; i < ballotList.getLength(); i++) {
                Node ballot = ballotList.item(i);
                if (ballot.getNodeType() == Node.ELEMENT_NODE && ballot.getNodeName().equals("ballot")) {
                    NamedNodeMap attributes = ballot.getAttributes();
                    try {
                        String adjudicatorId = attributes.getNamedItem("adjudicators").getNodeValue();
                        double score = Double.parseDouble(ballot.getTextContent());
                        individualSpeechBallotDTOs.add(new IndividualSpeechBallotDTO(adjudicatorId, score));
                    } catch (Exception e) {
                        log.error("Error parsing individual speech ballot DTO: " + e.getMessage());
                    }
                }
            }
            return individualSpeechBallotDTOs;
        } catch (Exception e) {
            log.error("Error parsing individual speech ballot DTO: " + e.getMessage());
            throw e;
        }
    }

    private List<FinalTeamBallotDTO> getFinalTeamBallotDTOs(Node side) {
        try {
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
        } catch (Exception e) {
            log.error("Error parsing final team ballot DTO: " + e.getMessage());
            throw e;
        }
    }


}