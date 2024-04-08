package com.dineth.debateTracker.utils;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

    public void getTournament(Document document) {
        Node tournament = document.getElementsByTagName("tournament").item(0);
        System.out.println("Tournament: " + tournament.getAttributes().getNamedItem("name").getNodeValue() + ", Short: " + tournament.getAttributes().getNamedItem("short").getNodeValue());
    }
    public void getTeamsAndSpeakers(Document document) {
        NodeList teamList = document.getElementsByTagName("team");
        for (int i = 0; i < teamList.getLength(); i++) {
            Node team = teamList.item(i);

            if (team.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = team.getAttributes();
                String teamName = attributes.getNamedItem("name").getNodeValue();
                String teamCode = attributes.getNamedItem("code").getNodeValue();
                String teamId = attributes.getNamedItem("id").getNodeValue();
                System.out.println("Team: " + teamName + ", Code: " + teamCode + ", ID: " + teamId);

                NodeList speakerList = team.getChildNodes();
                for (int j = 0; j < speakerList.getLength(); j++) {
                    Node speaker = speakerList.item(j);
                    if (speaker.getNodeType() == Node.ELEMENT_NODE) {
                        NamedNodeMap speakerAttributes = speaker.getAttributes();
                        String speakerId = speakerAttributes.getNamedItem("id").getNodeValue();
                        String speakerName = speaker.getTextContent();
                        String speakerInstitutionId = speakerAttributes.getNamedItem("institutions").getNodeValue();

                        System.out.println("\tSpeaker: " + speakerName + ", ID: " + speakerId + ", Institution: " + speakerInstitutionId);

                    }
                }

            }
        }
    }
    public void getAdjudicators(Document document) {
        NodeList adjudicatorList = document.getElementsByTagName("adjudicator");
        for (int j = 0; j < adjudicatorList.getLength(); j++) {
            Node adjudicator = adjudicatorList.item(j);
            if (adjudicator.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = adjudicator.getAttributes();
                String adjName = attributes.getNamedItem("name").getNodeValue();
                Float adjScore = Float.valueOf(attributes.getNamedItem("score").getNodeValue());
                String adjId = attributes.getNamedItem("id").getNodeValue();
                System.out.println("Adjudicator: " + adjName + ", adjScore: " + adjScore + ", ID: " + adjId);
            }
        }
    }
    public void getInstitutions(Document document) {
        NodeList institutionList = document.getElementsByTagName("institution");
        for (int i = 0; i < institutionList.getLength(); i++) {
            Node institution = institutionList.item(i);
            if (institution.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = institution.getAttributes();
                String institutionCode = attributes.getNamedItem("reference").getNodeValue();
                String institutionId = attributes.getNamedItem("id").getNodeValue();
                String institutionName = institution.getTextContent();
                System.out.println("Institution: " + institutionName + ", Code: " + institutionCode + ", ID: " + institutionId);
            }
        }
    }
    public void getMotions(Document document) {
        NodeList motionList = document.getElementsByTagName("motion");
        for (int i = 0; i < motionList.getLength(); i++) {
            Node motion = motionList.item(i);
            if (motion.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = motion.getAttributes();
                String motionId = attributes.getNamedItem("id").getNodeValue();
                String motionCode = attributes.getNamedItem("reference").getNodeValue();
                String motionText = motion.getTextContent().replace("\n", "").replace("\t", "");
                System.out.println("Motion: " + motionText + ", ID: " + motionId + ", Code: " + motionCode);
            }
        }
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
                getDebates(round);
            }
        }
    }

    private void getDebates(Node round) {
        NodeList debatesList = round.getChildNodes();
        for (int i = 0; i < debatesList.getLength(); i++) {
            Node debate = debatesList.item(i);
            if (debate.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = debate.getAttributes();
                String debateId = attributes.getNamedItem("id").getNodeValue();
                String debateVenue = attributes.getNamedItem("venue").getNodeValue();
                String debateTime = attributes.getNamedItem("motion").getNodeValue();
                System.out.println("\tDebate: " + debateId + ", Venue: " + debateVenue + ", Time: " + debateTime);
            }
        }
    }
}


