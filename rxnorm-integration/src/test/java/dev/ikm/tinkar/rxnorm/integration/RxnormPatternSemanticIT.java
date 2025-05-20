package dev.ikm.tinkar.rxnorm.integration;

import dev.ikm.maven.RxnormData;
import dev.ikm.maven.RxnormUtility;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RxnormPatternSemanticIT extends AbstractIntegrationTest {

    /**
     * Test RxnormConcepts Semantics.
     *
     * @result Reads content from file and validates Pattern Semantics by calling private method assertConcept().
     */
    @Test
    public void testRxnormPatternSemantics() throws IOException {
        String sourceFilePath = "../rxnorm-origin/";
        String errorFile = "target/failsafe-reports/Rxnorm_Pattern_not_found.txt";
        String absolutePath = rxnormOwlFileName; 
        int notFound = processOwlFile(absolutePath, errorFile);

        assertEquals(0, notFound, "Unable to find " + notFound + " Rxnorm Pattern semantics. Details written to " + errorFile);
    }

    @Override
    protected boolean assertOwlElement(RxnormData rxnormData) {
        String rxnormId = rxnormData.getId();

        int count = 0;
        AtomicInteger innerCount = new AtomicInteger(0);
        
        if (!rxnormData.getSnomedCtId().isEmpty()) {
            count++;
        }
        if (!rxnormData.getRxCuiId().isEmpty()) {
            count++;
        }
        if (!rxnormData.getVuidId().isEmpty()) {
            count++;
        }
        if(!rxnormData.getNdcCodesWithEndDates().isEmpty()){
			 for (Map.Entry<String, String> entry : rxnormData.getNdcCodesWithEndDates().entrySet()) {
		          count++;
			 }
        }
           
        StateSet stateActive = StateSet.ACTIVE;
        StateSet stateInActive = StateSet.INACTIVE;
        
		PatternEntityVersion latestIdentifierPattern = (PatternEntityVersion) Calculators.Stamp.DevelopmentLatest()
				.latest(TinkarTerm.IDENTIFIER_PATTERN).get();
		
		EntityProxy.Concept concept;
		AtomicBoolean latestExists = new AtomicBoolean(false);
		
		if(rxnormId != null) {
			//StampPositionRecord stampPosition = StampPositionRecord.make(timeForStamp, TinkarTerm.DEVELOPMENT_PATH.nid());
	        //StampCalculator stampCalc = StampCoordinateRecord.make(state, stampPosition).stampCalculator();
	        StampCalculator stampCalcActive = StampCalculatorWithCache
	               .getCalculator(StampCoordinateRecord.make(stateActive, Coordinates.Position.LatestOnDevelopment()));

	        StampCalculator stampCalcInActive = StampCalculatorWithCache
		               .getCalculator(StampCoordinateRecord.make(stateInActive, Coordinates.Position.LatestOnDevelopment()));
	        
			concept = EntityProxy.Concept.make(PublicIds.of(uuid(rxnormId)));
			
	        EntityService.get().forEachSemanticForComponentOfPattern(concept.nid(), TinkarTerm.IDENTIFIER_PATTERN.nid(), semanticEntity -> {
	        	Latest<SemanticEntityVersion> latestActive = stampCalcActive.latest(semanticEntity);
	        	Latest<SemanticEntityVersion> latestInActive = stampCalcInActive.latest(semanticEntity);
	        	
	        	if (latestActive.isPresent()) {
	        		if (!rxnormData.getSnomedCtId().isEmpty()) {
                        Component component = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestActive.get());
                        String value = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE, latestActive.get());
                        if (rxnormData.getSnomedCtId().equals(value) && RxnormUtility.getSnomedIdentifierConcept().equals(component)) {
                            latestExists.set(true);
                            innerCount.addAndGet(1);
                        }
                    }
	        		
	        		if(!rxnormData.getRxCuiId().isEmpty()){
                        Component component = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestActive.get());
                        String value = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE, latestActive.get());
                        if (rxnormData.getRxCuiId().equals(value) && RxnormUtility.getRxcuidConcept().equals(component)) {
                            latestExists.set(true);
                            innerCount.addAndGet(1);
                        }
                    }
	        		
	        		if(!rxnormData.getRxCuiId().isEmpty()){
                        Component component = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestActive.get());
                        String value = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE, latestActive.get());
                        if (rxnormData.getVuidId().equals(value) && RxnormUtility.getVuidConcept().equals(component)) {
                            latestExists.set(true);
                            innerCount.addAndGet(1);
                        }
                    }
	        		
	        		 if(!rxnormData.getNdcCodesWithEndDates().isEmpty()){
	        			for (Map.Entry<String, String> entry : rxnormData.getNdcCodesWithEndDates().entrySet()) {
	        				
	                        String ndcCode = entry.getKey();
	                        String endDate = entry.getValue();
	                        
	                        Component component = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestActive.get());
	                        String value = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE, latestActive.get());
	                        if (ndcCode.equals(value) && RxnormUtility.getNdcIdentifierConcept().equals(component)) {
	                            latestExists.set(true);
	                            innerCount.addAndGet(1);
	                        }
	        			}       			
                   }
	        	} 
	        	
	        	if (latestInActive.isPresent()) {
	        		 if(!rxnormData.getNdcCodesWithEndDates().isEmpty()) {
	        			for (Map.Entry<String, String> entry : rxnormData.getNdcCodesWithEndDates().entrySet()) {
	        				
	                        String ndcCode = entry.getKey();
	                        String endDate = entry.getValue();
	                        
	                        Component component = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestInActive.get());
	                        String value = latestIdentifierPattern.getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE, latestInActive.get());
	                        if (ndcCode.equals(value) && RxnormUtility.getNdcIdentifierConcept().equals(component)) {
	                            latestExists.set(true);
	                            innerCount.addAndGet(1);
	                        }
	        			}       			
                    }   
	        	} 
	        });    
	     		      
		}
		
		System.out.println("Count : " + count);
		System.out.println("innerCount : " + innerCount + "\n");
		
		if(count == 0) {
			latestExists.set(true);
		}
		
        return latestExists.get() && count == innerCount.get();  
    }    
}
