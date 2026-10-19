package dev.ikm.maven;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RxnormData {
    /** URI prefix of RxNorm vocabulary classes (their integer suffix is the RxCUI). */
    public static final String RXNORM_URI_PREFIX = "http://mor.nlm.nih.gov/RXNORM/";

    private String id;
    private String uri;

    private String rxnormName = "";
    private String rxnormSynonym = "";
    private String prescribableSynonym = "";

    private String snomedCtId = "";
    /**
     * Resolved RxCUI for identifier composition. Populated by {@link #normalizeRxCui()} after
     * extraction: the class id for every RxNorm-population class (a class's RxCUI is
     * definitionally its own integer id), "" for non-RxNorm classes (e.g. SNOMED formal-spec
     * shadow classes, which receive no RxCUI).
     */
    private String rxCuiId = "";
    /** RxCUI value as annotated in the OWL source ("" when absent) — kept verbatim for fidelity. */
    private String rxCuiAnnotation = "";
    private String vuidId = "";
    private List<String> ndcCodes = new ArrayList<>();
    private Map<String, String> ndcCodesWithEndDates = new HashMap<>();

    private String qualitativeDistinction = "";
    private String quantity = "";
    private String schedule = "";
    private String humanDrug = "";
    private String vetDrug = "";
    private List<String> tallmanSynonyms = new ArrayList<>();
    private String equivalentClassesStr = "";
    private String rdfsLabel = "";
    private String subClassOfStr = "";

    public RxnormData(String uri) {
        this.uri = uri;
        if (uri.startsWith(RXNORM_URI_PREFIX)) {
            this.id = uri.substring(RXNORM_URI_PREFIX.length());
        } else {
            this.id = uri.substring("http://snomed.info/id/".length());
        }
    }

    public String getId() {
        return id;
    }

    public void setRxnormName(String rxnormName) {
        this.rxnormName = rxnormName;
    }

    public void setRxnormSynonym(String rxnormSynonym) {
        this.rxnormSynonym = rxnormSynonym;
    }

    public void setPrescribableSynonym(String prescribableSynonym) {
        this.prescribableSynonym = prescribableSynonym;
    }

    public void setSnomedCtId(String snomedCtId) {
        this.snomedCtId = snomedCtId;
    }

    public void setRxCuiId(String rxCuiId) {
        this.rxCuiId = rxCuiId;
    }

    /** RxCUI value exactly as annotated in the OWL source ("" when the source carries none). */
    public String getAnnotatedRxCui() {
        return rxCuiAnnotation;
    }

    void setRxCuiAnnotation(String rxCuiAnnotation) {
        this.rxCuiAnnotation = rxCuiAnnotation == null ? "" : rxCuiAnnotation;
    }

    /** True when the OWL source carried a RxCUI annotation for this class. */
    public boolean isRxCuiAnnotated() {
        return !rxCuiAnnotation.isEmpty();
    }

    /**
     * True when the source annotated a RxCUI that differs from the class's own id — a
     * data-quality anomaly (a class's RxCUI is definitionally its own id). Verified: 0 of the
     * 17,230 annotated classes in the 2024-04-10 pilot OWL. The annotated value is still
     * composed as an additional identifier by the pipeline so no source data is dropped.
     */
    public boolean isRxCuiMismatch() {
        return isRxCuiAnnotated() && !rxCuiAnnotation.equals(rxCuiId);
    }

    /** True when the class belongs to the RxNorm population (vs a SNOMED shadow class). */
    public boolean isRxnormClass() {
        return uri != null && uri.startsWith(RXNORM_URI_PREFIX);
    }

    /**
     * Resolves this class's RxCUI for identifier composition; call once after extraction.
     *
     * <p>An RxNorm class's RxCUI is definitionally the integer id of the class itself (the RxCUI
     * embedded in the class URI), so for RxNorm-population classes the resolved value is the
     * class id, independent of the annotation. The annotation is corroboration, not identity:
     * for 100% of annotated classes it equals the class id, and a divergent annotation (see
     * {@link #isRxCuiMismatch()}) must never override the identity. Non-RxNorm classes (e.g.
     * SNOMED formal-spec shadow classes, whose ids end in "-FS") receive no RxCUI.
     */
    public void normalizeRxCui() {
        if (isRxnormClass() && id != null && !id.isEmpty()) {
            this.rxCuiId = id;
        }
    }

    public void setVuidId(String vuidId) {
        this.vuidId = vuidId;
    }

    public void addNdcCode(String ndcCode) {
        this.ndcCodes.add(ndcCode);
    }

    public void setQualitativeDistinction(String qualitativeDistinction) {
        this.qualitativeDistinction = qualitativeDistinction;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public void setHumanDrug(String humanDrug) {
        this.humanDrug = humanDrug;
    }

    public void setVetDrug(String vetDrug) {
        this.vetDrug = vetDrug;
    }

    public void setRdfsLabel(String rdfsLabel) {
        this.rdfsLabel = rdfsLabel;
    }

    public void setSubClassOfStr(String subClassOfStr) {
        this.subClassOfStr = subClassOfStr;
    }

    public void addTallmanSynonym(String tallmanSynonym) {
        this.tallmanSynonyms.add(tallmanSynonym);
    }

    public void setEquivalentClassesStr(String equivalentClassesStr) {
        this.equivalentClassesStr = equivalentClassesStr;
    }
    public void addNdcCodeWithEndDate(String ndcCode, String endDate) {
        this.ndcCodes.add(ndcCode); // Keep original list for backward compatibility
        this.ndcCodesWithEndDates.put(ndcCode, endDate);
    }

    public Map<String, String> getNdcCodesWithEndDates() {
        return ndcCodesWithEndDates;
    }


    public String getRxnormName() {
        return rxnormName;
    }

    public String getRxnormSynonym() {
        return rxnormSynonym;
    }

    public String getPrescribableSynonym() {
        return prescribableSynonym;
    }

    public String getSnomedCtId() {
        return snomedCtId;
    }

    /**
     * Resolved RxCUI for identifier composition — populated by {@link #normalizeRxCui()}:
     * the class id for every RxNorm-population class, "" for non-RxNorm classes.
     */
    public String getRxCuiId() {
        return rxCuiId;
    }

    public String getVuidId() {
        return vuidId;
    }

    public List<String> getNdcCodes() {
        return ndcCodes;
    }

    public String getQualitativeDistinction(){
        return qualitativeDistinction;
    }

    public String getQuantity(){
        return quantity;
    }

    public String getSchedule(){
        return schedule;
    }

    public String getHumanDrug() {
        return  humanDrug;
    }

    public String getVetDrug() {
        return vetDrug;
    }

    public List<String> getTallmanSynonyms(){
        return tallmanSynonyms;
    }

    public String getEquivalentClassesStr() {
        return equivalentClassesStr;
    }

    public String getRdfsLabel() {
        return rdfsLabel;
    }

    public String getSubClassOfStr() {
        return subClassOfStr;
    }

    @Override
    public String toString() {
        return this.id +" - "+ this.uri +" - "+ this.rxnormName +" - "+ this.snomedCtId +" - "+ this.rxCuiId
                + (isRxCuiMismatch() ? " (annotatedRxCui=" + rxCuiAnnotation + ")" : "") +" - "+ this.vuidId;
    }

}
