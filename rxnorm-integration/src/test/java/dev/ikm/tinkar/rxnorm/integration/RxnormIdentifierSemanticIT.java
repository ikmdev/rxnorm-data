package dev.ikm.tinkar.rxnorm.integration;

import dev.ikm.maven.RxnormData;
import dev.ikm.maven.RxnormUtility;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RxnormIdentifierSemanticIT extends AbstractIntegrationTest {

    /**
     * Test RxnormConcepts Semantics.
     *
     * @result Reads content from file and validates Identifier Semantics by calling private method assertConcept().
     */
    @Test
    public void testRxnormConceptSemantics() throws IOException {
        String sourceFilePath = "../rxnorm-origin/";
        String errorFile = "target/failsafe-reports/Rxnorm_Identifier_not_found.txt";
        String absolutePath = findFilePath(sourceFilePath, rxnormOwlFileName);  //findFilePath(sourceFilePath, rxnormOwlFileName);
        int notFound = processOwlFile(absolutePath, errorFile);

        assertEquals(0, notFound, "Unable to find " + notFound + " Rxnorm Identifier semantics. Details written to " + errorFile);
    }

    @Override
    protected boolean assertOwlElement(RxnormData rxnormData) {
        String rxnormId = rxnormData.getId();
        String snomedctId = rxnormData.getSnomedCtId();
        String rxCuid = rxnormData.getRxCuiId();
        String vuidCuid = rxnormData.getVuidId();
        
        // Generate UUID based on RxNorm Snomed IDENTIFIER
        // UUID conceptUuid = UuidT5Generator.get(uuid(namespaceString), rxnormId);
        // EntityProxy.Concept concept = EntityProxy.Concept.make(PublicIds.of(conceptUuid));
        // UUID snomedIdentifierUuid = UuidT5Generator.get(uuid(namespaceString), concept.publicId().asUuidArray()[0] + rxnormData.getSnomedCtId() + "ID");
           
        StateSet state = StateSet.ACTIVE;
        StampPositionRecord stampPosition = StampPositionRecord.make(timeForStamp, TinkarTerm.DEVELOPMENT_PATH.nid());
        StampCalculator stampCalc = StampCoordinateRecord.make(state, stampPosition).stampCalculator();
        
        UUID snomedIdentifierUuid = UUID.fromString(RxnormUtility.SNOMED_IDENTIFIER_PUBLIC_ID);
        EntityProxy.Concept ndcIdentifierConcept = RxnormUtility.getNdcIdentifierConcept();
        UUID ndcIdentifierUuid = UUID.fromString(RxnormUtility.NDC_IDENTIFIER_PUBLIC_ID);
        
		PatternEntityVersion latestIdentifierPattern = (PatternEntityVersion) Calculators.Stamp.DevelopmentLatest()
				.latest(TinkarTerm.IDENTIFIER_PATTERN).get();
        
        // EntityProxy.Concept snomedIdentifierConcept = RxnormUtility.getSnomedIdentifierConcept();
        // Generate UUID based on RxNorm ID
		
		EntityProxy.Concept concept;
		AtomicBoolean snomedExists = new AtomicBoolean(true);
		if(rxnormId != null) {
			concept = EntityProxy.Concept.make(PublicIds.of(uuid(rxnormId)));
			
			//concept = EntityProxy.Concept.make(PublicIds.of(UUID.fromString(namespaceString)));
			
			EntityProxy.Concept snomedIdentifier = RxnormUtility.getSnomedIdentifierConcept();
	        EntityService.get().forEachSemanticForComponentOfPattern(concept.nid(), TinkarTerm.IDENTIFIER_PATTERN.nid(), semanticEntity -> {
	        	Latest<SemanticEntityVersion> latest = stampCalc.latest(semanticEntity);
	        	
	        	if (!latest.isPresent()) {
	        		snomedExists.set(false);
	        		// String text = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.TEXT_FOR_DESCRIPTION, latest.get());
	        	} else {
	        		snomedExists.set(true);
	        	}
	        });    
	     		      
		}
		
        return snomedExists.get();  
    }    
}
