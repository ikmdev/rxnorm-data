package dev.ikm.maven;

import dev.ikm.tinkar.common.id.*;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.terms.EntityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RxnormUtility {
    public static final String RXNORM_MODULE = "eaeb4482-4c5b-4c0c-9d41-fa021bd10c30";
    public static final String SNOMED_IDENTIFIER_PUBLIC_ID = "a7035c97-c728-4e9e-99bb-21d3f5384583";
    public static final String RXCUID_IDENTIFIER_PUBLIC_ID = "edcf08eb-f5c1-4893-9a51-53a9d8201d8f";
    public static final String NDC_IDENTIFIER_PUBLIC_ID = "aea76ea1-5ee2-485f-8a4e-5d94610fe84f";
    public static final String VU_IDENTIFIER_PUBLIC_ID = "0385c5c5-9749-4625-8c83-b638bfec8fda";
    public static final String QUALITATIVE_DISTINCTION_PATTERN = "d2a8460f-0676-4f0b-aa0f-b6275b5506a0";
    public static final String QUANTITY_PATTERN = "05a40a30-1456-42a2-aa27-c902fe4185d8";
    public static final String SCHEDULE_PATTERN = "e52dd008-06db-4a8d-940c-6988bf42d26a";
    public static final String HUMAN_DRUG_PATTERN = "3a85992d-820d-4d7a-9ec3-e2af7a7bbfb3";
    public static final String VET_DRUG_PATTERN = "590fc8df-8cd8-4338-80b0-7ca2bceeb677";
    public static final String TALLMAN_SYNONYM_PATTERN = "8b5f06c8-4ec5-4918-8a6c-fa11fe41264a";
    
    public static final String QUALITATIVE_PATTERN_DISTINCTION_MEANING = "b33b4b92-124c-4bb8-81a7-4d92c714e784";
    public static final String QUALITATIVE_PATTERN_LANGUAGE_MEANING = "cd56cceb-8507-5ae5-a928-16079fe6f832";
    
    public static final String QUANTITY_PATTERN_DRUG_MEANING = "91089d31-5893-4033-9d07-fb03555c0b96";
    
    public static final String SCHEDULE_PATTERN_DRUG_MEANING = "60c0239e-7f89-496f-beab-e0c2cb5fb6a3";
    public static final String SCHEDULE_PATTERN_LANGUAGE_MEANING = "1561f545-d7e0-404a-8063-3ba6dc483410";
    
    public static final String HUMAN_DRUG_PATTERN_LANGUAGE_MEANING = "a31169af-8b9a-4ef3-9cce-fef79434358e";
    
    public static final String VETERINARIAN_DRUG_PATTERN_LANGUAGE_MEANING = "1cc614bd-7d47-4ae0-811d-aa3a2874652d";
  
    public static final String TALLMAN_SYNONYM_PATTERN_LANGUAGE_MEANING = "2db79d40-280a-4697-8b7b-284d1f4de275";
    
    private static final Logger LOG = LoggerFactory.getLogger(RxnormUtility.class.getSimpleName());
    

    /**
     *
     * @param file
     * @return Content of file into a String
     * @throws IOException
     */
    public static String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * Extracts RxNorm attributes from the OWL content
     */
    public static List<RxnormData> extractRxnormData(String owlContent) {
        List<RxnormData> attributes = new ArrayList<>();

        // Split the content by "# Class:" to identify each concept section
        String[] classBlocks = owlContent.split("# Class: ");

        // Skip the first block as it's before the first "# Class:"
        for (int i = 1; i < classBlocks.length; i++) {
            String block = classBlocks[i];
            // Extract the URI from the first line
            // Format: <http://mor.nlm.nih.gov/RXNORM/996062> (meclizine hydrochloride 25 MG Oral Film)
            Matcher uriMatcher = Pattern.compile("<([^>]+)>").matcher(block);

            if (uriMatcher.find()) {
                String uri = uriMatcher.group(1);

                RxnormData concept = new RxnormData(uri);

                // Extract annotations
                extractAnnotations(block, concept);

                // Extract EquivalentClasses
                extractEquivalentClasses(block, concept);
                extractRdfsLabel(block, concept);
                extractSubClassOf(block,concept);

                attributes.add(concept);

            }
        }

        return attributes;
    }


    /**
     * Extracts EquivalentClasses from a class block
     */
    /**
     * Extracts annotations from a class block
     */
    public static void extractAnnotations(String block, RxnormData data) {
        // Extract RxNorm_Name
        Matcher rxnormNameMatcher = Pattern.compile(":RxNorm_Name <[^>]+> \"([^\"]*)\"").matcher(block);
        if (rxnormNameMatcher.find()) {
            data.setRxnormName(rxnormNameMatcher.group(1));
        }

        // Extract RxNorm_Synonym
        Matcher rxnormSynonymMatcher = Pattern.compile(":RxNorm_Synonym <[^>]+> \"([^\"]*)\"").matcher(block);
        if (rxnormSynonymMatcher.find()) {
            data.setRxnormSynonym(rxnormSynonymMatcher.group(1));
        }

        // Extract Prescribable_Synonym
        Matcher prescribableSynonymMatcher = Pattern.compile(":Prescribable_Synonym <[^>]+> \"([^\"]*)\"").matcher(block);
        if (prescribableSynonymMatcher.find()) {
            data.setPrescribableSynonym(prescribableSynonymMatcher.group(1));
        }

        // Extract SNOMED CT identifier
        Matcher snomedCtMatcher = Pattern.compile("oboInOwl:hasDbXref <[^>]+> \"SNOMEDCT:\\s*([^\"]*)\"").matcher(block);
        if (snomedCtMatcher.find()) {
            data.setSnomedCtId(snomedCtMatcher.group(1));
        }

        // Extract RxCUI identifier
        Matcher rxCuiMatcher = Pattern.compile("oboInOwl:hasDbXref <[^>]+> \"RxCUI:\\s*([^\"]*)\"").matcher(block);
        if (rxCuiMatcher.find()) {
            data.setRxCuiId(rxCuiMatcher.group(1));
        }

        // Extract VUID identifier
        Matcher vuidMatcher = Pattern.compile("oboInOwl:hasDbXref <[^>]+> \"VUID:\\s*([^\"]*)\"").matcher(block);
        if (vuidMatcher.find()) {
            data.setVuidId(vuidMatcher.group(1));
        }

        // Extract NDC codes with their start and end dates
        Matcher ndcMatcher = Pattern.compile("AnnotationAssertion\\(Annotation\\(:endDate \"(\\d+)\"\\) Annotation\\(:startDate \"\\d+\"\\) :ndc <[^>]+> \"([^\"]*)\"\\)").matcher(block);
        while (ndcMatcher.find()) {
            String endDate = ndcMatcher.group(1);
            String ndcCode = ndcMatcher.group(2);
            data.addNdcCodeWithEndDate(ndcCode, endDate);
        }

        // Extract QUALITATIVE_DISTINCTION
        Matcher qualitativeDistinctionMatcher = Pattern.compile(":QUALITATIVE_DISTINCTION <[^>]+> \"([^\"]*)\"").matcher(block);
        if (qualitativeDistinctionMatcher.find()) {
            data.setQualitativeDistinction(qualitativeDistinctionMatcher.group(1));
        }

        // Extract QUANTITY
        Matcher quantityMatcher = Pattern.compile(":QUANTITY <[^>]+> \"([^\"]*)\"").matcher(block);
        if (quantityMatcher.find()) {
            data.setQuantity(quantityMatcher.group(1));
        }

        // Extract SCHEDULE
        Matcher scheduleMatcher = Pattern.compile(":SCHEDULE <[^>]+> \"([^\"]*)\"").matcher(block);
        if (scheduleMatcher.find()) {
            data.setSchedule(scheduleMatcher.group(1));
        }

        // Extract HUMAN_DRUG
        Matcher humanDrugMatcher = Pattern.compile(":HUMAN_DRUG <[^>]+> \"([^\"]*)\"").matcher(block);
        if (humanDrugMatcher.find()) {
            data.setHumanDrug(humanDrugMatcher.group(1));
        }

        // Extract VET_DRUG
        Matcher vetDrugMatcher = Pattern.compile(":VET_DRUG <[^>]+> \"([^\"]*)\"").matcher(block);
        if (vetDrugMatcher.find()) {
            data.setVetDrug(vetDrugMatcher.group(1));
        }

        // Extract TALLMAN_SYNONYM (can have multiple)
        Matcher tallmanSynonymMatcher = Pattern.compile(":Tallman_Synonym <[^>]+> \"([^\"]*)\"").matcher(block);
        while (tallmanSynonymMatcher.find()) {
            data.addTallmanSynonym(tallmanSynonymMatcher.group(1));
        }

    }

    /**
     * Extracts rdfs:label from a class block
     */
    public static void extractRdfsLabel(String block, RxnormData data) {
        Matcher rdfsLabelMatcher = Pattern.compile("rdfs:label <[^>]+> \"([^\"]*)\"").matcher(block);
        if (rdfsLabelMatcher.find()) {
            data.setRdfsLabel(rdfsLabelMatcher.group(1));
        }
    }

    /**
     * Extracts SubClassOf from a class block
     */
    public static void extractSubClassOf(String block, RxnormData concept) {
        int startIndex = block.indexOf("SubClassOf(");
        if (startIndex != -1) {
            int openParenCount = 1;
            int endIndex = startIndex + "SubClassOf(".length();

            while (openParenCount > 0 && endIndex < block.length()) {
                char c = block.charAt(endIndex);
                if (c == '(') {
                    openParenCount++;
                } else if (c == ')') {
                    openParenCount--;
                }
                endIndex++;
            }

            if (openParenCount == 0) {
                String fullSubClassOf = block.substring(startIndex, endIndex);
                concept.setSubClassOfStr(fullSubClassOf);
            }
        }
    }


    /**
     * Extracts EquivalentClasses from a class block
     */
    public static void extractEquivalentClasses(String block, RxnormData concept) {
        // Extract the entire EquivalentClasses block with nested parentheses
        int startIndex = block.indexOf("EquivalentClasses(");
        if (startIndex != -1) {
            // Find the matching closing parenthesis by counting opening and closing parentheses
            int openParenCount = 1;
            int endIndex = startIndex + "EquivalentClasses(".length();

            while (openParenCount > 0 && endIndex < block.length()) {
                char c = block.charAt(endIndex);
                if (c == '(') {
                    openParenCount++;
                } else if (c == ')') {
                    openParenCount--;
                }
                endIndex++;
            }

            if (openParenCount == 0) {
                // Extract the full block including "EquivalentClasses"
                String fullEquivalentClasses = block.substring(startIndex, endIndex);
                concept.setEquivalentClassesStr(fullEquivalentClasses);
            }
        }
    }

    /**
     * Parses the timestamp from the file name
     * Expected format: something-YYYY-MM-DD-something.owl
     */
    public static long parseTimeFromFileName(String fileName) {
        try {
            // Extract the date portion using regex
            Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
            Matcher matcher = pattern.matcher(fileName);

            if (matcher.find()) {
                String dateStr = matcher.group(1);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.parse(dateStr).getTime();
            } else {
                LOG.warn("Could not extract date from file name: " + fileName);
                return System.currentTimeMillis(); // Fallback to current time
            }
        } catch (ParseException e) {
            LOG.error("Error parsing date from file name: " + fileName, e);
            return System.currentTimeMillis(); // Fallback to current time
        }
    }

    public static EntityProxy.Pattern makePatternProxy(UUID namespace, String description) {
        return EntityProxy.Pattern.make(description, UuidT5Generator.get(namespace, description));
    }
    public static EntityProxy.Concept makeConceptProxy(UUID namespace, String description) {
        return EntityProxy.Concept.make(description, UuidT5Generator.get(namespace, description));
    }
    public static EntityProxy.Concept getSnomedIdentifierConcept(){
        return EntityProxy.Concept.make(PublicIds.of(UUID.fromString(SNOMED_IDENTIFIER_PUBLIC_ID)));
    }
    public static EntityProxy.Concept getRxcuidConcept(){
        return EntityProxy.Concept.make(PublicIds.of(UUID.fromString(RXCUID_IDENTIFIER_PUBLIC_ID)));
    }
    public static EntityProxy.Concept getVuidConcept(){
        return EntityProxy.Concept.make(PublicIds.of(UUID.fromString(VU_IDENTIFIER_PUBLIC_ID)));
    }
    public static EntityProxy.Concept getNdcIdentifierConcept(){
        return EntityProxy.Concept.make(PublicIds.of(UUID.fromString(NDC_IDENTIFIER_PUBLIC_ID)));
    }
    public static EntityProxy.Pattern getQualitativeDistinctionPattern(){
        return EntityProxy.Pattern.make(PublicIds.of(UUID.fromString(QUALITATIVE_DISTINCTION_PATTERN)));
    }
    public static EntityProxy.Pattern getQuantityPattern(){
        return EntityProxy.Pattern.make(PublicIds.of(UUID.fromString(QUANTITY_PATTERN)));
    }
    public static EntityProxy.Pattern getSchedulePattern(){
        return EntityProxy.Pattern.make(PublicIds.of(UUID.fromString(SCHEDULE_PATTERN)));
    }
    public static EntityProxy.Pattern getHumanDrugPattern(){
        return EntityProxy.Pattern.make(PublicIds.of(UUID.fromString(HUMAN_DRUG_PATTERN)));
    }
    public static EntityProxy.Pattern getVetDrugPattern(){
        return EntityProxy.Pattern.make(PublicIds.of(UUID.fromString(VET_DRUG_PATTERN)));
    }
    public static EntityProxy.Pattern getTallmanSynonymPattern(){
        return EntityProxy.Pattern.make(PublicIds.of(UUID.fromString(TALLMAN_SYNONYM_PATTERN)));
    }
    public static String transformOwlString(UUID namespace, String owlString) {
        // First, let's handle URIs in the entire string
        Pattern uriPattern = Pattern.compile("<(http://[^>]+)>");
        Matcher matcher = uriPattern.matcher(owlString);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String uri = matcher.group(1);
            String replacement;
            // Process the URI based on its format
            if (uri.startsWith("http://snomed.info/id/")) {
                String id = uri.substring("http://snomed.info/id/".length());
                final EntityProxy.Concept concept;
                if (id.endsWith("-FS")) {
                    concept = makeConceptProxy(namespace, id + "rxnorm");
                } else {
                    concept = makeConceptProxy(namespace, id);
                }
                replacement = ":[" + concept.publicId().asUuidArray()[0] + "]";
            } else if (uri.startsWith("http://mor.nlm.nih.gov/RXNORM/")) {
                // RxNorm ID
                String id = uri.substring("http://mor.nlm.nih.gov/RXNORM/".length());
                EntityProxy.Concept concept = makeConceptProxy(namespace, id + "rxnorm");
                replacement = ":[" + concept.publicId().asUuidArray()[0] + "]";
            } else {
                // Unknown URI type, keep as is
                replacement = "<" + uri + ">";
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        // handle the DataHasValue expressions
        String partialResult = result.toString();
        Pattern dataValuePattern = Pattern.compile("DataHasValue\\(:[\\[](.*?)[\\]]\\s+\"([^\"]*)\"\\^\\^xsd:([^\\)]+)\\)|DataHasValue\\(<(http://[^>]+)>\\s+\"([^\"]*)\"\\^\\^xsd:([^\\)]+)\\)");
        matcher = dataValuePattern.matcher(partialResult);
        result = new StringBuffer();

        while (matcher.find()) {
            String replacement;
            if (matcher.group(1) != null) {
                // Already transformed URI
                String conceptId = matcher.group(1);
                String value = matcher.group(2);
                String dataType = matcher.group(3);
                replacement = "DataHasValue(:[" + conceptId + "] \"" + value + "\"^^xsd:" + dataType + ")";
            } else {
                // Original URI format
                String uri = matcher.group(4);
                String value = matcher.group(5);
                String dataType = matcher.group(6);

                if (uri.startsWith("http://snomed.info/id/")) {
                    String id = uri.substring("http://snomed.info/id/".length());
                    final EntityProxy.Concept concept;
                    if (id.endsWith("-FS")) {
                        concept = makeConceptProxy(namespace, id + "rxnorm");
                    } else {
                        concept = makeConceptProxy(namespace, id);
                    }

                    replacement = "DataHasValue(:[" + concept.publicId().asUuidArray()[0] + "] \"" +
                            value + "\"^^xsd:" + dataType + ")";
                } else {
                    // Keep original format if URI type is unknown
                    replacement = "DataHasValue(<" + uri + "> \"" + value + "\"^^xsd:" + dataType + ")";
                }
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static String transformOwlStringSubClassesOf(UUID namespace, String owlString) {
        // First, let's handle URIs in the entire string
        Pattern uriPattern = Pattern.compile("<(http://[^>]+)>");
        Matcher matcher = uriPattern.matcher(owlString);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String uri = matcher.group(1);
            String replacement;
            // Process the URI based on its format
            if (uri.startsWith("http://snomed.info/id/")) {
                String id = uri.substring("http://snomed.info/id/".length());
                final EntityProxy.Concept concept;
                if (id.endsWith("-FS")) {
                    concept = makeConceptProxy(namespace, id + "rxnorm");
                } else {
                    concept = makeConceptProxy(namespace, id);
                }

                replacement = ":[" + concept.publicId().asUuidArray()[0] + "]";
            } else if (uri.startsWith("http://mor.nlm.nih.gov/RXNORM/")) {
                // RxNorm ID
                String id = uri.substring("http://mor.nlm.nih.gov/RXNORM/".length());
                EntityProxy.Concept concept = makeConceptProxy(namespace, id + "rxnorm");
                replacement = ":[" + concept.publicId().asUuidArray()[0] + "]";
            } else {
                // Unknown URI type, keep as is
                replacement = "<" + uri + ">";
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static UUID generateUUID(UUID namespace, String id) {
        return UuidT5Generator.get(namespace, id);
    }
}
