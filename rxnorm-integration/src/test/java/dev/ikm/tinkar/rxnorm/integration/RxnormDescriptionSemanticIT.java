package dev.ikm.tinkar.rxnorm.integration;

import dev.ikm.maven.RxnormData;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RxnormDescriptionSemanticIT extends AbstractIntegrationTest {

    /**
     * Test RxnormDescriptions Semantics.
     *
     * @result Reads content from file and validates Description of Semantics by calling private method assertConcept().
     */
    @Test
    @Disabled // TODO
    public void testRxnormDescriptionSemantics() throws IOException {
        String errorFile = "target/failsafe-reports/Rxnorm_Descriptions_not_found.txt";
        String absolutePath = rxnormOwlFileName;
        int notFound = processOwlFile(absolutePath, errorFile);

        assertEquals(0, notFound, "Unable to find " + notFound + " Rxnorm Description semantics. Details written to " + errorFile);
    }

    @Override
    protected boolean assertOwlElement(RxnormData rxnormData) {
        if (rxnormData.getId() != null) {
            // Generate UUID based on RxNorm ID
            UUID conceptUuid = conceptUuid(rxnormData.getId());
            EntityProxy.Concept concept = EntityProxy.Concept.make(PublicIds.of(conceptUuid));
//            EntityProxy.Concept concept =
//                    EntityProxy.Concept.make(PublicIds.of(UuidT5Generator.get(UUID.fromString(namespaceString), rxnormData.getId() + "rxnorm")));
            StateSet stateActive = StateSet.ACTIVE;
            StampCalculator stampCalcActive = StampCalculatorWithCache
                    .getCalculator(StampCoordinateRecord.make(stateActive, Coordinates.Position.LatestOnDevelopment()));
            PatternEntityVersion latestDescriptionPattern = (PatternEntityVersion) Calculators.Stamp.DevelopmentLatest().latest(TinkarTerm.DESCRIPTION_PATTERN).get();
            boolean rxnormName = !rxnormData.getRxnormName().isEmpty();
            boolean rxnormSynonym = !rxnormData.getRxnormSynonym().isEmpty();
            boolean rxnormPrescribableSynonym = !rxnormData.getPrescribableSynonym().isEmpty();

            AtomicBoolean matchedName = new AtomicBoolean(!rxnormName);
            AtomicBoolean matchedSynonym = new AtomicBoolean(!rxnormSynonym);
            AtomicBoolean matchedPrescribableSynonym = new AtomicBoolean(!rxnormPrescribableSynonym);
            AtomicInteger counter = new AtomicInteger(0);

//            if (concept.description() != null) {
//                System.out.println("(concept.description(): " + concept.nid() +" - "+ concept.description());
//            }

//            ConceptRecord entity = EntityService.get().getEntityFast(conceptUuid);
//            if (entity == (null)) {
//                return true;
//            } else {
//                System.out.println(entity);
//            }

            EntityService.get().forEachSemanticForComponentOfPattern(concept.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid(), semanticEntity -> {
                Latest<SemanticEntityVersion> latestActive = stampCalcActive.latest(semanticEntity);

                counter.incrementAndGet();
                if (latestActive.isPresent()) {
                    String textForDesc = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.TEXT_FOR_DESCRIPTION, latestActive.get());
                    Component descCaseSignificance = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE, latestActive.get());
                    Component descType = latestDescriptionPattern.getFieldWithMeaning(TinkarTerm.DESCRIPTION_TYPE, latestActive.get());

                    if (rxnormName) {
                        matchedName.set(textForDesc.equals(rxnormData.getRxnormName())
                                && descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE)
                                && (descType.equals(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE))
                        );
                    }

                    if (rxnormSynonym) {
                        matchedSynonym.set(textForDesc.equals(rxnormData.getRxnormSynonym())
                                && descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE)
                                && (descType.equals(REGULAR_NAME_DESCRIPTION_TYPE))
                        );
                    }

                    if (rxnormPrescribableSynonym) {
                        matchedPrescribableSynonym.set(textForDesc.equals(rxnormData.getPrescribableSynonym())
                                && descCaseSignificance.equals(DESCRIPTION_NOT_CASE_SENSITIVE)
                                && (descType.equals(REGULAR_NAME_DESCRIPTION_TYPE))
                        );
                    }

                }

            });

            if (counter.get() == 0) {
                System.out.println("counter is Zero for: " +concept +" - "+ concept.description());
            }

//            if (!(matchedName.get() || matchedSynonym.get() || matchedPrescribableSynonym.get())) {
//                System.out.println("matchedName.get(): " + matchedName.get() +" - matchedSynonym.get(): "+ matchedSynonym.get() +" - matchedPrescribableSynonym.get(): "+ matchedPrescribableSynonym.get()); // TOTAL 28
//            }

            return (matchedName.get() && matchedSynonym.get() && matchedPrescribableSynonym.get());
        }
//        System.out.println("rxnormData.getId() == null: "); // TOTAL 28
        return false;
    }

}
