package com.example.svarp;

import java.util.*;
public class HealthDecisionEngine {

    // ─── Risk level ───────────────────────────────────────────────────────────────
    public enum RiskLevel { SAFE, MODERATE, DANGER }

    // ─── Symptom enum ─────────────────────────────────────────────────────────────
    public enum Symptom {
        FEVER, COUGH, HEADACHE, FATIGUE, SORE_THROAT, VOMITING,
        BODY_ACHE, DIZZINESS, SKIN_RASH, EYE_DISCOMFORT, TOOTHACHE,
        CHEST_PAIN, SHORTNESS_OF_BREATH, NAUSEA, WEAKNESS, WEIGHT_LOSS,
        BLOOD_IN_STOOL, BLOOD_IN_URINE, DIARRHEA, STOMACH_ACHE
    }

    // Symptoms that are always serious
    private static final Set<Symptom> DANGER_SYMPTOMS = new HashSet<>(Arrays.asList(
            Symptom.CHEST_PAIN, Symptom.SHORTNESS_OF_BREATH,
            Symptom.BLOOD_IN_STOOL, Symptom.BLOOD_IN_URINE, Symptom.WEIGHT_LOSS
    ));

    private static final Set<String> EMERGENCY_KEYWORDS = new HashSet<>(Arrays.asList(
            "chest pain", "heart attack", "can't breathe", "suicide", "unconscious",
            "bleeding heavily", "severe bleeding", "stroke", "seizure", "not breathing",
            "passed out", "severe chest", "pressure chest", "left arm pain"
    ));

    // ─── Assessment result ────────────────────────────────────────────────────────
    public static class HealthAssessment {
        public RiskLevel riskLevel;
        public String condition;
        public String explanation;
        public List<String> actionSteps;
        public boolean isEmergency;

        public HealthAssessment(RiskLevel risk, String condition, String explanation,
                                List<String> actions, boolean emergency) {
            this.riskLevel    = risk;
            this.condition    = condition;
            this.explanation  = explanation;
            this.actionSteps  = actions;
            this.isEmergency  = emergency;
        }
    }

    // ─── Language flag ────────────────────────────────────────────────────────────
    private final boolean isHindi;

    public HealthDecisionEngine(boolean isHindi) { this.isHindi = isHindi; }
    public HealthDecisionEngine()                { this.isHindi = false;   }

    // ═════════════════════════════════════════════════════════════════════════════
    // ENTRY POINT 1 — UI path (enum symptoms, e.g. from checkbox / tap UI)
    // ═════════════════════════════════════════════════════════════════════════════
    public HealthAssessment analyzeInput(List<Symptom> selectedSymptoms) {
        if (selectedSymptoms == null || selectedSymptoms.isEmpty()) {
            return isHindi
                    ? new HealthAssessment(RiskLevel.SAFE, "कोई लक्षण नहीं चुना गया",
                    "कृपया अपने लक्षण चुनें ताकि हम आपकी स्थिति का आकलन कर सकें।",
                    Arrays.asList("उन लक्षणों पर टैप करें जो आप महसूस कर रहे हैं"), false)
                    : new HealthAssessment(RiskLevel.SAFE, "No Symptoms Selected",
                    "Please select your symptoms so we can assess your condition.",
                    Arrays.asList("Tap on the symptoms you are experiencing"), false);
        }

        List<Symptom> dangerFound = new ArrayList<>();
        List<Symptom> mildFound   = new ArrayList<>();

        for (Symptom s : selectedSymptoms) {
            if (DANGER_SYMPTOMS.contains(s)) dangerFound.add(s);
            else                             mildFound.add(s);
        }

        int total = selectedSymptoms.size();

        if (!dangerFound.isEmpty() && total >= 2) return buildDangerResponse(dangerFound, mildFound);
        if (!dangerFound.isEmpty() && total == 1) return buildDangerAloneResponse(dangerFound.get(0));
        if (total >= 2)                           return buildModerateResponse(mildFound);
        return buildSafeResponse(mildFound.get(0));
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // ENTRY POINT 2 — Voice path (raw transcribed text + optional string symptom list)
    //
    // Flow:
    //   1. Emergency keyword fast-path
    //   2. Extra danger combos unique to voice (meningitis, anaphylaxis, etc.)
    //   3. Convert text → List<Symptom>  →  hand off to UI engine (analyzeInput)
    //   4. Fallback to keyword-only decision tree for anything not mapped to an enum
    // ═════════════════════════════════════════════════════════════════════════════
    public HealthAssessment analyzeInput(String userInput, List<String> rawSymptoms) {
        if (userInput   == null) userInput   = "";
        if (rawSymptoms == null) rawSymptoms = new ArrayList<>();

        String combined = (userInput + " " + String.join(" ", rawSymptoms)).toLowerCase();

        // ── Step 1: Emergency keyword fast-path ───────────────────────────────
        if (containsEmergencyKeyword(combined)) {
            return buildVoiceEmergencyResponse();
        }

        // ── Step 2: Extra danger combos only checked in voice path ────────────
        HealthAssessment extraDanger = checkVoiceOnlyDangerCombos(combined, rawSymptoms);
        if (extraDanger != null) return extraDanger;

        // ── Step 3: Map text → enum symptoms, then reuse full UI engine ───────
        List<Symptom> mapped = mapTextToSymptoms(combined);
        if (!mapped.isEmpty()) {
            return analyzeInput(mapped);
        }

        // ── Step 4: Fallback keyword decision tree for un-mappable input ──────
        return runVoiceFallbackTree(combined, rawSymptoms);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Voice helpers
    // ─────────────────────────────────────────────────────────────────────────────

    private boolean containsEmergencyKeyword(String input) {
        for (String kw : EMERGENCY_KEYWORDS) {
            if (input.contains(kw)) return true;
        }
        return false;
    }

    /** Generic emergency response used when an EMERGENCY_KEYWORD is matched in voice input. */
    private HealthAssessment buildVoiceEmergencyResponse() {
        return isHindi
                ? new HealthAssessment(RiskLevel.DANGER, "संभावित चिकित्सा आपात स्थिति",
                "आपके लक्षण गंभीर हैं और तुरंत चिकित्सा सहायता की ज़रूरत है।",
                Arrays.asList(
                        "तुरंत 102 / 108 पर कॉल करें",
                        "खुद गाड़ी न चलाएं",
                        "शांत रहें और आराम से बैठें या लेटें",
                        "कोई आपके साथ रहे"
                ), true)
                : new HealthAssessment(RiskLevel.DANGER, "Potential Medical Emergency",
                "Symptoms indicate a potentially serious condition requiring immediate medical attention.",
                Arrays.asList(
                        "Contact emergency services (102 / 108) immediately",
                        "Do not attempt to drive yourself",
                        "Remain calm and rest in a comfortable position",
                        "Have someone stay with you"
                ), true);
    }

    /**
     * Danger combinations that only make sense when voice/text context is available
     * (e.g. "stiff neck", "spreading rash", "fainted", "chemical in eye", "vomiting blood").
     * Returns null if none match — caller continues to normal flow.
     */
    private HealthAssessment checkVoiceOnlyDangerCombos(String input, List<String> symptoms) {
        boolean hasFever      = input.contains("fever")      || input.contains("bukhar");
        boolean hasHeadache   = input.contains("headache")   || input.contains("sir dard");
        boolean hasStiffNeck  = input.contains("stiff neck") || input.contains("neck pain") || input.contains("gardan dard");
        boolean hasRash       = input.contains("rash")       || input.contains("daane")     || input.contains("chamdi par daag");
        boolean hasSpreading  = input.contains("spreading")  || input.contains("getting bigger") || input.contains("phail");
        boolean hasFainting   = input.contains("fainted")    || input.contains("passed out")|| input.contains("blackout") || input.contains("behosh");
        boolean hasChestPain  = input.contains("chest")      || input.contains("seena");
        boolean hasBreathing  = input.contains("can't breathe") || input.contains("throat closing") || input.contains("swelling");
        boolean hasVomBlood   = input.contains("vomiting blood") || input.contains("blood") && input.contains("vomit");
        boolean hasChemical   = input.contains("chemical")   || input.contains("cleaner")   || input.contains("acid");
        boolean hasEye        = input.contains("eye");
        boolean hasSudden     = input.contains("sudden")     || input.contains("out of nowhere") || input.contains("abrupt");
        boolean hasSevere     = input.contains("severe")     || input.contains("worst")     || input.contains("unbearable");

        // Meningitis triad
        if (hasFever && hasHeadache && hasStiffNeck) {
            return isHindi
                    ? new HealthAssessment(RiskLevel.DANGER, "संभावित मेनिंजाइटिस",
                    "बुखार, सिरदर्द और गर्दन का अकड़ना — यह तीनों मिलकर आपात स्थिति हो सकते हैं।",
                    Arrays.asList(
                            "तुरंत अस्पताल जाएं",
                            "रास्ते में तेज़ रोशनी से बचें",
                            "लक्षण कब शुरू हुए — नोट करें",
                            "देर बिल्कुल न करें"
                    ), true)
                    : new HealthAssessment(RiskLevel.DANGER, "Possible Meningitis",
                    "Fever, headache, and neck stiffness together require immediate medical evaluation.",
                    Arrays.asList(
                            "Seek emergency care immediately",
                            "Avoid bright lights during transport",
                            "Note when symptoms started",
                            "Do not delay seeking care"
                    ), true);
        }

        // Fever + rapidly spreading rash
        if (hasFever && hasRash && hasSpreading) {
            return isHindi
                    ? new HealthAssessment(RiskLevel.DANGER, "बुखार के साथ फैलते चकत्ते",
                    "तेज़ी से फैलते चकत्ते और बुखार — यह गंभीर संक्रमण का संकेत हो सकता है।",
                    Arrays.asList(
                            "तुरंत अस्पताल जाएं",
                            "चकत्तों की फ़ोटो लें",
                            "हाल ही में ली कोई दवा या खाई कोई नई चीज़ नोट करें",
                            "जांच से पहले कोई क्रीम न लगाएं"
                    ), true)
                    : new HealthAssessment(RiskLevel.DANGER, "Fever with Spreading Rash",
                    "Fever accompanied by a rapidly spreading rash may indicate a serious infection.",
                    Arrays.asList(
                            "Go to emergency room immediately",
                            "Document rash progression with photos if possible",
                            "Note any recent medications or new foods",
                            "Do not apply creams or ointments before evaluation"
                    ), true);
        }

        // Thunderclap headache
        if (hasSevere && hasSudden && hasHeadache) {
            return isHindi
                    ? new HealthAssessment(RiskLevel.DANGER, "अचानक तेज़ सिरदर्द",
                    "अचानक शुरू हुआ बहुत तेज़ सिरदर्द — इसे नज़रअंदाज़ न करें।",
                    Arrays.asList(
                            "तुरंत अस्पताल जाएं",
                            "एस्पिरिन या खून पतला करने वाली दवाएं न लें",
                            "दर्द कब शुरू हुआ — नोट करें",
                            "जो दवाएं ले रहे हैं उनकी लिस्ट साथ रखें"
                    ), true)
                    : new HealthAssessment(RiskLevel.DANGER, "Thunderclap Headache",
                    "A sudden severe headache requires immediate evaluation to rule out serious conditions.",
                    Arrays.asList(
                            "Seek emergency care immediately",
                            "Avoid aspirin or blood-thinning medications",
                            "Note the exact time of onset",
                            "Bring a complete list of current medications"
                    ), true);
        }

        // Anaphylaxis — rash + throat/breathing symptoms
        if (hasRash && hasBreathing) {
            return isHindi
                    ? new HealthAssessment(RiskLevel.DANGER, "एनाफिलेक्सिस (गंभीर एलर्जी)",
                    "चकत्तों के साथ सांस लेने में तकलीफ — यह जानलेवा हो सकता है।",
                    Arrays.asList(
                            "तुरंत 102 / 108 पर कॉल करें",
                            "एपिनेफ्रिन इंजेक्शन हो तो अभी लगाएं",
                            "लेट जाएं और पैर ऊपर करें",
                            "यह जानलेवा स्थिति है — देर बिल्कुल न करें"
                    ), true)
                    : new HealthAssessment(RiskLevel.DANGER, "Possible Anaphylaxis",
                    "Rash with breathing difficulty suggests anaphylaxis — a life-threatening emergency.",
                    Arrays.asList(
                            "Contact emergency services immediately",
                            "Use epinephrine auto-injector if available",
                            "Lie flat with legs elevated",
                            "This is a life-threatening condition — do not wait"
                    ), true);
        }

        // Fainting / syncope with chest symptoms
        if (hasFainting && hasChestPain) {
            return isHindi
                    ? new HealthAssessment(RiskLevel.DANGER, "बेहोशी के साथ सीने में दर्द",
                    "बेहोश होना और सीने में दर्द — यह दिल की गंभीर समस्या हो सकती है।",
                    Arrays.asList(
                            "तुरंत 102 / 108 पर कॉल करें",
                            "लेट जाएं और हिलें नहीं",
                            "खुद गाड़ी बिल्कुल न चलाएं",
                            "कोई आपके साथ रहे"
                    ), true)
                    : new HealthAssessment(RiskLevel.DANGER, "Fainting with Chest Pain",
                    "Fainting alongside chest pain can indicate a serious cardiac event.",
                    Arrays.asList(
                            "Call emergency services (102 / 108) immediately",
                            "Lie down and do not move unnecessarily",
                            "Do not drive yourself under any circumstances",
                            "Have someone stay with you"
                    ), true);
        }

        // Vomiting blood
        if (hasVomBlood) {
            return isHindi
                    ? new HealthAssessment(RiskLevel.DANGER, "उल्टी में खून",
                    "उल्टी में खून आना — यह पेट की गंभीर समस्या है, तुरंत इलाज ज़रूरी है।",
                    Arrays.asList(
                            "तुरंत 102 / 108 पर कॉल करें",
                            "अभी कुछ न खाएं-पिएं",
                            "करवट लेकर लेटें",
                            "जो दवाएं ले रहे हैं उनकी लिस्ट साथ रखें"
                    ), true)
                    : new HealthAssessment(RiskLevel.DANGER, "Vomiting Blood",
                    "Vomiting blood is a medical emergency requiring immediate intervention.",
                    Arrays.asList(
                            "Contact emergency services immediately",
                            "Do not consume food or liquids",
                            "Lie on your side to prevent choking",
                            "Bring a list of all medications you are taking"
                    ), true);
        }

        // Chemical eye exposure
        if (hasChemical && hasEye) {
            return isHindi
                    ? new HealthAssessment(RiskLevel.DANGER, "आँख में रसायन",
                    "आँख में रसायन पड़ना — तुरंत पानी से आँख धोएं और अस्पताल जाएं।",
                    Arrays.asList(
                            "15 मिनट तक साफ पानी से आँख धोते रहें",
                            "धोते समय पलकें खुली रखें",
                            "तुरंत अस्पताल जाएं",
                            "जिस रसायन से हुआ — उसका डिब्बा साथ ले जाएं"
                    ), true)
                    : new HealthAssessment(RiskLevel.DANGER, "Chemical Eye Exposure",
                    "Chemical exposure to the eye requires immediate irrigation and emergency care.",
                    Arrays.asList(
                            "Irrigate eye with clean water continuously for 15 minutes",
                            "Hold eyelids open during irrigation",
                            "Proceed to emergency department immediately",
                            "Bring the chemical container for identification"
                    ), true);
        }

        return null; // no extra danger combo matched
    }

    /**
     * Maps keywords (English + Hindi transliteration) in a combined input string
     * to Symptom enums so the voice path can reuse the full UI engine logic.
     */
    private List<Symptom> mapTextToSymptoms(String input) {
        List<Symptom> result = new ArrayList<>();

        if (input.contains("fever")           || input.contains("bukhar"))                                      result.add(Symptom.FEVER);
        if (input.contains("cough")           || input.contains("khansi"))                                      result.add(Symptom.COUGH);
        if (input.contains("headache")        || input.contains("head pain")  || input.contains("sir dard"))   result.add(Symptom.HEADACHE);
        if (input.contains("fatigue")         || input.contains("tired")      || input.contains("thakan"))     result.add(Symptom.FATIGUE);
        if (input.contains("sore throat")     || input.contains("gala dard")  || input.contains("gale mein")) result.add(Symptom.SORE_THROAT);
        if (input.contains("vomiting")        || input.contains("ulti")       || input.contains("vomit"))      result.add(Symptom.VOMITING);
        if (input.contains("body ache")       || input.contains("body pain")  || input.contains("sharir dard") || input.contains("jism dard")) result.add(Symptom.BODY_ACHE);
        if (input.contains("dizzy")           || input.contains("dizziness")  || input.contains("chakkar"))    result.add(Symptom.DIZZINESS);
        if (input.contains("rash")            || input.contains("daane")      || input.contains("chamdi par")) result.add(Symptom.SKIN_RASH);
        if (input.contains("eye")             || input.contains("aankh"))                                      result.add(Symptom.EYE_DISCOMFORT);
        if (input.contains("tooth")           || input.contains("toothache")  || input.contains("daant"))      result.add(Symptom.TOOTHACHE);
        if (input.contains("chest pain")      || input.contains("seena dard"))                                 result.add(Symptom.CHEST_PAIN);
        if (input.contains("short of breath") || input.contains("breathless") || input.contains("sans lena")) result.add(Symptom.SHORTNESS_OF_BREATH);
        if (input.contains("nausea")          || input.contains("mann ghabrana"))                              result.add(Symptom.NAUSEA);
        if (input.contains("weakness")        || input.contains("weak")       || input.contains("kamzori"))    result.add(Symptom.WEAKNESS);
        if (input.contains("weight loss")     || input.contains("wajan kam"))                                  result.add(Symptom.WEIGHT_LOSS);
        if (input.contains("blood in stool")  || input.contains("mal mein khoon"))                            result.add(Symptom.BLOOD_IN_STOOL);
        if (input.contains("blood in urine")  || input.contains("peshab mein khoon"))                         result.add(Symptom.BLOOD_IN_URINE);
        if (input.contains("diarrhea")        || input.contains("loose motion") || input.contains("dast"))    result.add(Symptom.DIARRHEA);
        if (input.contains("stomach")         || input.contains("stomach pain") || input.contains("pet dard") || input.contains("pet mein dard")) result.add(Symptom.STOMACH_ACHE);

        return result;
    }

    /**
     * Last-resort keyword tree for voice input that couldn't be mapped to any enum
     * (e.g. very short or highly context-specific queries). Mirrors Version 2's
     * individual-symptom branches.
     */
    private HealthAssessment runVoiceFallbackTree(String input, List<String> symptoms) {
        if (input.contains("headache") || input.contains("head pain") || input.contains("migraine") || input.contains("sir dard")) {
            boolean severe  = input.contains("severe")  || input.contains("worst")    || input.contains("unbearable");
            boolean migrain = input.contains("migraine") || input.contains("throbbing") || input.contains("one side");
            if (migrain) return buildSafeResponse(Symptom.HEADACHE); // handled by enum path as MODERATE anyway
            if (severe)  return buildDangerAloneResponse(Symptom.CHEST_PAIN); // shouldn't happen — caught earlier
            return buildSafeResponse(Symptom.HEADACHE);
        }
        if (input.contains("fever") || input.contains("bukhar")) return buildSafeResponse(Symptom.FEVER);
        if (input.contains("cough") || input.contains("khansi")) return buildSafeResponse(Symptom.COUGH);
        if (input.contains("tired") || input.contains("thakan")) return buildSafeResponse(Symptom.FATIGUE);
        if (input.contains("nausea") || input.contains("mann ghabrana")) return buildSafeResponse(Symptom.NAUSEA);
        if (input.contains("stomach") || input.contains("pet dard"))     return buildSafeResponse(Symptom.STOMACH_ACHE);
        if (input.contains("body ache") || input.contains("sharir dard")) return buildSafeResponse(Symptom.BODY_ACHE);
        if (input.contains("dizzy") || input.contains("chakkar"))        return buildSafeResponse(Symptom.DIZZINESS);
        if (input.contains("rash")  || input.contains("daane"))          return buildSafeResponse(Symptom.SKIN_RASH);
        if (input.contains("eye")   || input.contains("aankh"))          return buildSafeResponse(Symptom.EYE_DISCOMFORT);
        if (input.contains("tooth") || input.contains("daant"))          return buildSafeResponse(Symptom.TOOTHACHE);

        // Absolute fallback
        return isHindi
                ? new HealthAssessment(RiskLevel.MODERATE, "अज्ञात स्थिति",
                "दी गई जानकारी के आधार पर कोई सटीक आकलन नहीं हो सका। सामान्य स्वास्थ्य सुझाव दिए जा रहे हैं।",
                Arrays.asList(
                        "आराम करें और पानी पिएं",
                        "अगले 24 घंटे लक्षणों पर नज़र रखें",
                        "बेहतर न हो तो डॉक्टर से मिलें",
                        "नए लक्षण आएं तो नोट करें"
                ), false)
                : new HealthAssessment(RiskLevel.MODERATE, "Unspecified Condition",
                "Based on the information provided, a specific assessment cannot be determined. General wellness measures are recommended.",
                Arrays.asList(
                        "Ensure adequate rest and hydration",
                        "Monitor symptoms over the next 24 hours",
                        "Consult a healthcare provider if symptoms persist or worsen",
                        "Maintain a record of any new or changing symptoms"
                ), false);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // DANGER response builders (used by UI path)
    // ═════════════════════════════════════════════════════════════════════════════

    private HealthAssessment buildDangerResponse(List<Symptom> danger, List<Symptom> mild) {
        return new HealthAssessment(RiskLevel.DANGER,
                getConditionName(danger, mild),
                buildDangerExplanation(danger, mild),
                buildDangerActions(danger, mild),
                true);
    }

    private HealthAssessment buildDangerAloneResponse(Symptom d) {
        if (isHindi) {
            switch (d) {
                case CHEST_PAIN:
                    return new HealthAssessment(RiskLevel.MODERATE, "सीने में दर्द",
                            "सीने में दर्द को नज़रअंदाज़ नहीं करना चाहिए। यह दिल या फेफड़ों की समस्या हो सकती है — आज ही डॉक्टर से मिलें।",
                            Arrays.asList("अभी आराम करें, कोई मेहनत न करें", "आज डॉक्टर से मिलें, देर न करें",
                                    "अगर दर्द बाज़ू या जबड़े तक फैले तो तुरंत 102/108 पर कॉल करें",
                                    "खुद गाड़ी न चलाएं — किसी को साथ ले जाएं"), false);
                case SHORTNESS_OF_BREATH:
                    return new HealthAssessment(RiskLevel.MODERATE, "सांस लेने में तकलीफ",
                            "बिना किसी और लक्षण के सांस की तकलीफ चिंता, अस्थमा या कोई और समस्या हो सकती है। जांच ज़रूरी है।",
                            Arrays.asList("सीधे बैठें और धीरे-धीरे सांस लें", "आज डॉक्टर से मिलें",
                                    "अगर बहुत ज़्यादा तकलीफ हो तो 102/108 पर कॉल करें",
                                    "जब तक जांच न हो, कोई भारी काम न करें"), false);
                case BLOOD_IN_STOOL:
                    return new HealthAssessment(RiskLevel.MODERATE, "मल में खून",
                            "मल में खून आना हमेशा डॉक्टरी जांच की ज़रूरत होती है, भले ही आप ठीक महसूस करें।",
                            Arrays.asList("आज डॉक्टर से मिलें", "खून का रंग और मात्रा नोट करें",
                                    "खुद कोई दवा न लें", "घबराएं नहीं — कई कारण इलाज योग्य होते हैं"), false);
                case BLOOD_IN_URINE:
                    return new HealthAssessment(RiskLevel.MODERATE, "पेशाब में खून",
                            "पेशाब में खून का कारण जानने के लिए जल्दी डॉक्टर से मिलना ज़रूरी है।",
                            Arrays.asList("आज डॉक्टर से मिलें", "खूब पानी पिएं",
                                    "कितनी बार हो रहा है और रंग कैसा है — नोट करें",
                                    "भारी काम से बचें"), false);
                case WEIGHT_LOSS:
                    return new HealthAssessment(RiskLevel.MODERATE, "वज़न का घटना",
                            "बिना कोशिश के वज़न कम होना कभी-कभी शरीर का संकेत हो सकता है। जांच करवाना सही रहेगा।",
                            Arrays.asList("डॉक्टर से अपॉइंटमेंट लें", "वज़न में बदलाव का रिकॉर्ड रखें",
                                    "नियमित खाना खाते रहें", "ब्लड टेस्ट से अक्सर कारण पता चल जाता है"), false);
                default:
                    return new HealthAssessment(RiskLevel.MODERATE, "लक्षण पर ध्यान दें",
                            "यह लक्षण अकेले भी डॉक्टरी जांच की ज़रूरत रखता है।",
                            Arrays.asList("आज डॉक्टर से मिलें", "नए लक्षणों पर नज़र रखें"), false);
            }
        } else {
            switch (d) {
                case CHEST_PAIN:
                    return new HealthAssessment(RiskLevel.MODERATE, "Chest Pain",
                            "Chest pain on its own is something you should get checked out today — it's better to be safe and rule out anything serious.",
                            Arrays.asList("Try to rest and avoid any physical effort for now",
                                    "See a doctor today, don't put it off",
                                    "If the pain gets worse or spreads to your arm or jaw, call 102/108",
                                    "Avoid driving yourself — have someone take you"), false);
                case SHORTNESS_OF_BREATH:
                    return new HealthAssessment(RiskLevel.MODERATE, "Shortness of Breath",
                            "Feeling short of breath without other symptoms could be anxiety, mild asthma, or something worth checking. Don't ignore it.",
                            Arrays.asList("Sit upright and try to breathe slowly and calmly",
                                    "Get it checked by a doctor today",
                                    "If it becomes really difficult to breathe, call 102/108",
                                    "Hold off on any exercise until you've been seen"), false);
                case BLOOD_IN_STOOL:
                    return new HealthAssessment(RiskLevel.MODERATE, "Blood in Stool",
                            "This always needs a doctor's attention, even if you feel otherwise okay. It's probably nothing serious, but worth finding out.",
                            Arrays.asList("Make an appointment to see a doctor today",
                                    "Take note of the color and how much you noticed",
                                    "Don't try to self-medicate for this one",
                                    "Try not to stress — many causes are treatable"), false);
                case BLOOD_IN_URINE:
                    return new HealthAssessment(RiskLevel.MODERATE, "Blood in Urine",
                            "Blood in urine needs to be looked at by a doctor to figure out what's causing it.",
                            Arrays.asList("See a doctor today", "Drink plenty of water in the meantime",
                                    "Note how often it's happening and what color",
                                    "Avoid strenuous activity for now"), false);
                case WEIGHT_LOSS:
                    return new HealthAssessment(RiskLevel.MODERATE, "Unexplained Weight Loss",
                            "Losing weight without trying can sometimes be a sign your body is trying to tell you something. Worth getting it looked at.",
                            Arrays.asList("Book a doctor's appointment when you can",
                                    "Try to keep track of how much weight you've lost and over what time",
                                    "Keep eating regular meals",
                                    "A simple blood test can often point in the right direction"), false);
                default:
                    return new HealthAssessment(RiskLevel.MODERATE, "Symptom Needs Attention",
                            "This symptom is worth getting checked out, even on its own.",
                            Arrays.asList("See a doctor today", "Keep an eye out for any new symptoms"), false);
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // MODERATE response builder
    // ═════════════════════════════════════════════════════════════════════════════

    private HealthAssessment buildModerateResponse(List<Symptom> s) {
        boolean hasFever       = s.contains(Symptom.FEVER);
        boolean hasCough       = s.contains(Symptom.COUGH);
        boolean hasHeadache    = s.contains(Symptom.HEADACHE);
        boolean hasFatigue     = s.contains(Symptom.FATIGUE);
        boolean hasSoreThroat  = s.contains(Symptom.SORE_THROAT);
        boolean hasVomiting    = s.contains(Symptom.VOMITING);
        boolean hasBodyAche    = s.contains(Symptom.BODY_ACHE);
        boolean hasDizziness   = s.contains(Symptom.DIZZINESS);
        boolean hasRash        = s.contains(Symptom.SKIN_RASH);
        boolean hasNausea      = s.contains(Symptom.NAUSEA);
        boolean hasWeakness    = s.contains(Symptom.WEAKNESS);
        boolean hasDiarrhea    = s.contains(Symptom.DIARRHEA);
        boolean hasStomachAche = s.contains(Symptom.STOMACH_ACHE);
        boolean hasEye         = s.contains(Symptom.EYE_DISCOMFORT);
        boolean hasToothache   = s.contains(Symptom.TOOTHACHE);

        if (isHindi) {
            if (hasFever && hasCough && hasSoreThroat)
                return hi("श्वसन संक्रमण",
                        "बुखार, खांसी और गले में दर्द — ये तीनों मिलकर श्वसन संक्रमण के संकेत हैं। आराम से ठीक हो जाएगा।",
                        "गर्म पानी और चाय पिएं, नमक के पानी से गरारे करें",
                        "बुखार के लिए पैरासिटामोल लें", "3 दिन से ज़्यादा बुखार रहे तो डॉक्टर से मिलें",
                        "दूसरों से थोड़ी दूरी बनाएं ताकि संक्रमण न फैले");
            if (hasFever && hasBodyAche && hasFatigue)
                return hi("फ्लू (इन्फ्लुएंजा)",
                        "बुखार, बदन दर्द और थकान — ये फ्लू के क्लासिक लक्षण हैं। 2-3 दिन में ठीक हो जाना चाहिए।",
                        "पूरी तरह आराम करें — शरीर को ज़रूरत है", "पैरासिटामोल से बुखार और दर्द में राहत मिलेगी",
                        "गर्म तरल पदार्थ पिते रहें", "3 दिन बाद भी ठीक न हों तो डॉक्टर से मिलें");
            if (hasFever && hasRash)
                return hi("बुखार के साथ चकत्ते",
                        "बुखार और चकत्ते एक साथ होने पर जांच करवाना सही रहेगा — कुछ संक्रमणों में ऐसा होता है।",
                        "अगले एक दिन में डॉक्टर से मिलें", "चकत्तों की फ़ोटो लेते रहें",
                        "खुजलाएं नहीं", "हाल ही में खाई कोई नई चीज़ या दवा याद करें");
            if (hasFever && hasHeadache)
                return hi("बुखार के साथ सिरदर्द",
                        "बुखार और सिरदर्द अक्सर वायरल इन्फेक्शन में साथ आते हैं। गर्दन अकड़ने पर तुरंत डॉक्टर के पास जाएं।",
                        "शांत और ठंडे कमरे में आराम करें", "पैरासिटामोल लें और खूब पानी पिएं",
                        "अगर गर्दन अकड़े या रोशनी से तकलीफ हो — तुरंत अस्पताल जाएं",
                        "2 दिन बाद भी बुखार हो तो डॉक्टर से मिलें");
            if (hasFever && hasVomiting)
                return hi("बुखार के साथ उल्टी",
                        "यह पेट का संक्रमण या वायरल बीमारी हो सकती है। पानी की कमी न होने दें।",
                        "थोड़ा-थोड़ा ORS या साफ पानी पिते रहें", "उल्टी रुकने तक ठोस खाना न खाएं",
                        "24 घंटे से ज़्यादा उल्टी हो तो डॉक्टर से मिलें",
                        "मुँह सूखना, पेशाब न आना — ये डिहाइड्रेशन के संकेत हैं");
            if (hasFever && hasDiarrhea)
                return hi("पेट का संक्रमण",
                        "बुखार के साथ दस्त — यह आमतौर पर पेट का संक्रमण होता है। पानी पीना सबसे ज़रूरी है।",
                        "ORS पिएं — यह बहुत ज़रूरी है", "हल्का खाना खाएं — चावल, केला, रोटी",
                        "2 दिन से ज़्यादा दस्त हों तो डॉक्टर से मिलें", "हाथ बार-बार धोएं");
            if (hasFever && hasFatigue)
                return hi("वायरल बुखार",
                        "बुखार और थकान — शरीर किसी वायरस से लड़ रहा है। आराम ही सबसे अच्छी दवा है।",
                        "जितना हो सके आराम करें", "खूब पानी और तरल पदार्थ पिएं",
                        "पैरासिटामोल लें अगर तकलीफ हो", "2-3 दिन में ठीक न हो तो डॉक्टर से मिलें");
            if (hasFever && hasSoreThroat)
                return hi("गले का संक्रमण",
                        "बुखार के साथ गले में दर्द — टॉन्सिल या गले का इन्फेक्शन हो सकता है।",
                        "गर्म नमक के पानी से दिन में 3 बार गरारे करें",
                        "शहद वाली गर्म चाय पिएं", "बहुत ज़्यादा सूजन हो तो डॉक्टर से मिलें");
            if (hasFever && hasCough)
                return hi("श्वसन बीमारी",
                        "बुखार और खांसी — यह सर्दी, फ्लू या ऐसा ही कुछ हो सकता है। आराम से ठीक हो जाएगा।",
                        "गर्म तरल पदार्थ पिएं", "भाप लें — खांसी में राहत मिलेगी",
                        "3 दिन बाद भी बुखार हो या सांस लेने में तकलीफ हो तो डॉक्टर से मिलें");
            if (hasNausea && hasVomiting && hasDiarrhea)
                return hi("पेट की बीमारी (गैस्ट्रोएंटेराइटिस)",
                        "जी मिचलाना, उल्टी और दस्त एक साथ — यह पेट का वायरस है। 1-2 दिन में ठीक हो जाएगा।",
                        "थोड़ा-थोड़ा ORS पिते रहें — बहुत ज़रूरी है",
                        "उल्टी बंद होने तक ठोस खाना न खाएं",
                        "48 घंटे बाद भी ठीक न हों तो डॉक्टर से मिलें", "हाथ अच्छी तरह धोएं");
            if (hasStomachAche && hasDiarrhea)
                return hi("पेट का संक्रमण",
                        "पेट दर्द के साथ दस्त — खाने से या हल्के पेट के संक्रमण से हो सकता है।",
                        "ORS या साफ पानी पिते रहें", "हल्का खाना खाएं",
                        "2 दिन बाद भी ठीक न हों तो डॉक्टर से मिलें");
            if (hasNausea && hasVomiting)
                return hi("जी मिचलाना और उल्टी",
                        "खाने से, पेट के वायरस से, या तनाव से हो सकता है। जल्दी ठीक हो जाएगा।",
                        "अदरक वाली चाय या साफ पानी धीरे-धीरे पिएं",
                        "ठोस खाना अभी बंद रखें", "24 घंटे से ज़्यादा उल्टी हो तो डॉक्टर से मिलें");
            if (hasNausea && hasStomachAche)
                return hi("पेट की तकलीफ",
                        "जी मिचलाना और पेट दर्द — आमतौर पर अपच या हल्का पेट का वायरस होता है।",
                        "गर्म पानी या हर्बल चाय पिएं", "मसालेदार और भारी खाना बंद करें",
                        "कुछ घंटों में ठीक हो जाएगा");
            if (hasStomachAche && hasVomiting)
                return hi("पेट खराब",
                        "पेट दर्द के साथ उल्टी — खाने से जहर या पेट का वायरस हो सकता है।",
                        "थोड़ा-थोड़ा पानी या ORS पिएं", "उल्टी बंद होने तक खाना बंद रखें",
                        "दर्द बहुत ज़्यादा हो या उल्टी न रुके तो डॉक्टर से मिलें");
            if (hasCough && hasSoreThroat)
                return hi("सर्दी या गले की जलन",
                        "खांसी और गले में दर्द — आमतौर पर सामान्य सर्दी होती है। खुद ठीक हो जाएगी।",
                        "शहद और अदरक वाली गर्म चाय बहुत फायदेमंद है",
                        "नमक के पानी से गरारे करें", "5-7 दिन में ठीक न हो तो डॉक्टर से मिलें");
            if (hasCough && hasFatigue)
                return hi("सर्दी या हल्का संक्रमण",
                        "खांसी के साथ थकान — शरीर कुछ से लड़ रहा है। आराम ज़रूरी है।",
                        "जितना हो सके आराम करें", "गर्म तरल पदार्थ पिएं",
                        "भाप लें", "बुखार आए या खांसी बढ़े तो डॉक्टर से मिलें");
            if (hasCough && hasBodyAche)
                return hi("वायरल बीमारी",
                        "खांसी और बदन दर्द — वायरल इन्फेक्शन के साथ अक्सर ऐसा होता है।",
                        "आराम करें और खूब पानी पिएं",
                        "पैरासिटामोल से बदन दर्द में राहत मिलेगी",
                        "बुखार आए या हालत बिगड़े तो डॉक्टर से मिलें");
            if (hasHeadache && hasDizziness)
                return hi("सिरदर्द के साथ चक्कर",
                        "यह अक्सर डिहाइड्रेशन, लो ब्लड प्रेशर या माइग्रेन की शुरुआत होती है। घबराएं नहीं।",
                        "तुरंत बैठ जाएं या लेट जाएं", "पानी या इलेक्ट्रोलाइट ड्रिंक पिएं",
                        "कुछ हल्का खाएं", "यह बार-बार हो तो डॉक्टर से ज़रूर बात करें");
            if (hasHeadache && hasFatigue)
                return hi("थकान से सिरदर्द",
                        "सिरदर्द के साथ थकान — अक्सर तनाव, नींद की कमी या डिहाइड्रेशन से होता है।",
                        "आराम करें — शरीर को ब्रेक चाहिए", "पानी पिएं",
                        "माथे पर ठंडा या गर्म कपड़ा रखें", "रात को अच्छी नींद लें");
            if (hasHeadache && hasNausea)
                return hi("संभावित माइग्रेन",
                        "सिरदर्द के साथ जी मिचलाना — यह माइग्रेन हो सकता है।",
                        "अंधेरे और शांत कमरे में लेट जाएं",
                        "माथे पर ठंडा कपड़ा रखें", "धीरे-धीरे पानी पिएं",
                        "पहली बार हो रहा है तो डॉक्टर से मिलें");
            if (hasFatigue && hasWeakness)
                return hi("थकान और कमज़ोरी",
                        "लंबे समय की थकान और कमज़ोरी — एनीमिया, कम शुगर या थायराइड की समस्या हो सकती है।",
                        "ठीक से खाएं — आयरन वाली चीज़ें खाएं", "आराम करें",
                        "एक हफ्ते से ज़्यादा हो तो डॉक्टर से मिलें",
                        "ब्लड टेस्ट से अक्सर कारण पता चलता है");
            if (hasFatigue && hasDizziness)
                return hi("थकान और चक्कर",
                        "थकान के साथ चक्कर — डिहाइड्रेशन, खाना न खाना, या थकान से हो सकता है।",
                        "बैठ जाएं और जल्दी न उठें", "पानी या इलेक्ट्रोलाइट ड्रिंक पिएं",
                        "कुछ हल्का खाएं", "बार-बार हो तो डॉक्टर से मिलें");
            if (hasWeakness && hasDizziness)
                return hi("कमज़ोरी और चक्कर",
                        "कमज़ोरी के साथ चक्कर — लो ब्लड प्रेशर, डिहाइड्रेशन या कम शुगर से हो सकता है।",
                        "तुरंत बैठ जाएं या लेट जाएं", "पानी या इलेक्ट्रोलाइट ड्रिंक पिएं",
                        "कुछ खाएं", "जल्दी ठीक न हो तो डॉक्टर से मिलें");
            if (hasWeakness && hasNausea)
                return hi("कमज़ोरी और जी मिचलाना",
                        "डिहाइड्रेशन, पेट के वायरस, या खाना न खाने से हो सकता है।",
                        "धीरे-धीरे पानी या ORS पिएं", "हल्का खाना खाने की कोशिश करें",
                        "आराम करें", "एक दिन में ठीक न हो तो डॉक्टर से मिलें");
            if (hasNausea && hasDizziness)
                return hi("जी मिचलाना और चक्कर",
                        "डिहाइड्रेशन, कान की समस्या या मोशन सिकनेस से हो सकता है।",
                        "बैठ जाएं और अचानक न हिलें", "पानी धीरे-धीरे पिएं",
                        "ताज़ी हवा में जाएं", "एक दिन से ज़्यादा हो तो डॉक्टर से मिलें");
            if (hasDiarrhea && hasWeakness)
                return hi("दस्त के साथ कमज़ोरी",
                        "दस्त से कमज़ोरी — डिहाइड्रेशन का संकेत है। पानी पीना सबसे ज़रूरी है।",
                        "ORS बहुत ज़रूरी है — तुरंत लें", "हल्का खाना खाएं",
                        "आराम करें", "कमज़ोरी बढ़े या दस्त 2 दिन से ज़्यादा हों तो डॉक्टर से मिलें");
            if (hasDiarrhea && hasNausea)
                return hi("पेट का वायरस",
                        "दस्त और जी मिचलाना — पेट का वायरस या खाने से हो सकता है।",
                        "पानी पीते रहें — ORS लें", "हल्का खाना खाएं",
                        "48 घंटे बाद भी ठीक न हों तो डॉक्टर से मिलें");
            if (hasEye && hasHeadache)
                return hi("आँखों की थकान और सिरदर्द",
                        "स्क्रीन पर ज़्यादा समय बिताने से आँखें थक जाती हैं और सिरदर्द होता है।",
                        "अभी सभी स्क्रीन से ब्रेक लें",
                        "20-20-20 का नियम अपनाएं — हर 20 मिनट में 20 फीट दूर 20 सेकंड देखें",
                        "आई ड्रॉप्स लगाएं", "बार-बार हो तो आँखों की जांच करवाएं");
            if (hasToothache && hasFever)
                return hi("दांत का संक्रमण",
                        "दांत दर्द के साथ बुखार — यह संक्रमण हो सकता है जिसे जल्दी इलाज की ज़रूरत है।",
                        "एक दिन के अंदर दंत चिकित्सक से मिलें",
                        "गर्म नमक के पानी से कुल्ला करें", "पैरासिटामोल से दर्द और बुखार में राहत मिलेगी",
                        "सूजन पर गर्म सेंक न करें");
            if (hasRash && hasWeakness)
                return hi("चकत्ते के साथ कमज़ोरी",
                        "चकत्ते के साथ कमज़ोरी — यह एलर्जी या वायरल इन्फेक्शन हो सकता है।",
                        "एक दिन में डॉक्टर से मिलें", "खुजलाएं नहीं",
                        "आराम करें और पानी पिएं");
            if (hasRash && hasFatigue)
                return hi("चकत्ते के साथ थकान",
                        "वायरल इन्फेक्शन या एलर्जी में ऐसा हो सकता है।",
                        "आराम करें और पानी पिएं", "चकत्ते वाली जगह को ठंडा रखें",
                        "चकत्ते फैलें या बुखार आए तो डॉक्टर से मिलें");
            if (hasDizziness && hasBodyAche)
                return hi("चक्कर के साथ बदन दर्द",
                        "डिहाइड्रेशन या वायरल इन्फेक्शन की शुरुआत हो सकती है।",
                        "आराम करें और खूब पानी पिएं", "कुछ खाएं",
                        "अचानक न हिलें", "बुखार आए या हालत बिगड़े तो डॉक्टर से मिलें");

            // Hindi generic fallback
            StringBuilder sl = new StringBuilder();
            for (int i = 0; i < s.size(); i++) {
                sl.append(formatSymptomName(s.get(i)));
                if (i < s.size() - 2) sl.append(", ");
                else if (i == s.size() - 2) sl.append(" और ");
            }
            return hi("कई लक्षण एक साथ",
                    "आपको " + sl + " हो रहा है। यह अभी आपातकाल नहीं है, लेकिन अगले 24-48 घंटे ध्यान रखें।",
                    "आराम करें और पानी पिएं", "हर कुछ घंटों में अपनी तबीयत देखें",
                    "बेहतर न हो तो डॉक्टर से मिलें", "नए लक्षण आएं तो नोट करें");

        } else {
            if (hasFever && hasCough && hasSoreThroat)
                return en("Likely a Respiratory Infection",
                        "Fever, cough, and sore throat together are pretty classic signs of a respiratory bug. You'll probably feel better in a few days with some rest.",
                        "Get plenty of rest and drink warm fluids — tea, soup, warm water all help",
                        "Gargling with warm salt water a few times a day can soothe the throat",
                        "Paracetamol can help bring the fever down if you're uncomfortable",
                        "If the fever sticks around for more than 3 days, it's worth seeing a doctor");
            if (hasFever && hasBodyAche && hasFatigue)
                return en("Likely the Flu",
                        "Fever, body aches, and fatigue hitting together is a pretty telltale sign of the flu. It's rough but usually passes in a few days.",
                        "Your body needs rest right now — seriously, take it easy",
                        "Paracetamol can help with the fever and aches",
                        "Keep sipping warm fluids throughout the day",
                        "If things aren't improving after 3 days, check in with a doctor");
            if (hasFever && hasRash)
                return en("Fever with Rash",
                        "Fever and a rash together can mean a few different things — some minor, some worth checking. Better to get it looked at.",
                        "Try to see a doctor within the next day or so",
                        "Take some photos of the rash so you can show how it looks over time",
                        "Resist scratching — it'll only irritate the skin more",
                        "Paracetamol is fine for the fever — hold off on ibuprofen until you've been seen");
            if (hasFever && hasHeadache)
                return en("Fever with Headache",
                        "Fever and headache often come together with viral infections. Usually nothing alarming, but keep an eye on it.",
                        "Rest in a cool, quiet room",
                        "Paracetamol can help with both the fever and the headache",
                        "Keep yourself hydrated",
                        "If your neck starts feeling stiff or light bothers your eyes, get seen immediately");
            if (hasFever && hasVomiting)
                return en("Fever with Vomiting",
                        "This combo often points to a stomach bug or viral illness. The main thing to watch here is staying hydrated.",
                        "Sip fluids in small amounts frequently — ORS sachets are great for this",
                        "Hold off on solid food until the vomiting settles down",
                        "Paracetamol can help manage the fever",
                        "If vomiting is going on for more than a day, see a doctor");
            if (hasFever && hasDiarrhea)
                return en("Stomach Infection",
                        "Fever with diarrhea usually means your stomach is dealing with some kind of infection. Staying hydrated is the most important thing.",
                        "ORS is your best friend right now — sip it regularly",
                        "Stick to bland foods like rice, banana, or toast",
                        "If diarrhea continues for more than 2 days, check in with a doctor",
                        "Wash hands frequently — stomach bugs spread easily");
            if (hasFever && hasFatigue)
                return en("Viral Fever",
                        "Feeling feverish and exhausted usually means your body is busy fighting off a virus. Rest is the best medicine here.",
                        "Rest as much as you can — your body is working hard",
                        "Drink plenty of fluids to stay hydrated",
                        "Paracetamol can help if the fever is making you uncomfortable",
                        "See a doctor if it drags on beyond 2-3 days");
            if (hasFever && hasSoreThroat)
                return en("Throat Infection",
                        "Fever with a sore throat could be tonsillitis or a throat infection. Worth getting checked if it doesn't ease up.",
                        "Warm salt water gargles really do help — try 3 times a day",
                        "Drink warm liquids like tea or warm water with honey",
                        "Take paracetamol if the pain or fever is bothering you",
                        "If your throat is very swollen or you're struggling to swallow, see a doctor sooner");
            if (hasFever && hasCough)
                return en("Respiratory Bug",
                        "Fever and cough together suggest a respiratory infection. Should clear up with rest.",
                        "Rest up and drink warm fluids",
                        "Steam inhalation can help ease the cough",
                        "Take paracetamol if the fever is uncomfortable",
                        "See a doctor if breathing feels difficult or fever lasts more than 3 days");
            if (hasNausea && hasVomiting && hasDiarrhea)
                return en("Stomach Bug (Gastroenteritis)",
                        "Nausea, vomiting, and diarrhea all at once is pretty classic gastroenteritis. Not fun, but it usually passes in a day or two.",
                        "Hydration is everything right now — sip ORS or clear fluids little and often",
                        "Don't force yourself to eat until the vomiting settles",
                        "If symptoms are still going strong after 48 hours, it's worth seeing a doctor",
                        "Wash hands carefully — stomach bugs are very contagious");
            if (hasStomachAche && hasDiarrhea)
                return en("Digestive Upset",
                        "Stomach ache with diarrhea is usually something you ate or a mild stomach bug. Should settle down on its own.",
                        "Stay hydrated with ORS or plain water",
                        "Eat light, bland foods — banana, rice, toast are easy on the stomach",
                        "Steer clear of spicy, oily, or heavy food for now",
                        "If it's still going after 2 days, see a doctor");
            if (hasNausea && hasVomiting)
                return en("Nausea and Vomiting",
                        "Could be something you ate, a stomach bug, or even just stress. Usually settles down fairly quickly.",
                        "Sip ginger tea or clear fluids slowly — small amounts at a time",
                        "Don't rush back to solid food — give your stomach a break",
                        "Rest in a well-ventilated spot",
                        "If vomiting keeps going beyond a day, check in with a doctor");
            if (hasNausea && hasStomachAche)
                return en("Stomach Discomfort",
                        "Nausea with stomach ache often points to indigestion or a mild stomach bug.",
                        "Sip warm water or herbal tea slowly",
                        "Avoid heavy, spicy, or oily food",
                        "Rest and let your stomach settle",
                        "Should ease up within a few hours");
            if (hasStomachAche && hasVomiting)
                return en("Stomach Upset",
                        "Stomach pain with vomiting can happen with food poisoning or a stomach bug. The main thing is to stay hydrated.",
                        "Sip clear fluids or ORS in small amounts",
                        "Avoid solid food until vomiting has stopped",
                        "Rest and try to stay calm",
                        "If the pain is severe or vomiting won't stop, get it checked out");
            if (hasCough && hasSoreThroat)
                return en("Cold or Throat Irritation",
                        "Cough and sore throat together usually just mean a common cold. Should clear up on its own.",
                        "Warm drinks with honey are genuinely helpful here",
                        "Salt water gargles can really help ease the throat",
                        "Steam inhalation can help loosen the cough",
                        "See a doctor if it hasn't improved after 5–7 days");
            if (hasCough && hasFatigue)
                return en("Cold or Mild Respiratory Infection",
                        "Feeling tired with a cough is pretty common with a cold or mild chest infection.",
                        "Rest as much as you can — your body needs it",
                        "Stay hydrated with warm fluids",
                        "Steam inhalation can help with the cough",
                        "If you develop a fever or the cough gets worse, see a doctor");
            if (hasCough && hasBodyAche)
                return en("Likely Viral Illness",
                        "Cough and body aches together often come with viral infections like a cold or flu.",
                        "Rest up and drink plenty of fluids",
                        "Warm compress on sore muscles can help",
                        "Paracetamol can ease the body aches",
                        "See a doctor if a fever develops or things get worse");
            if (hasHeadache && hasDizziness)
                return en("Headache with Dizziness",
                        "This combo often comes down to dehydration, low blood pressure, or a migraine starting up.",
                        "Sit or lie down right away — don't push through it",
                        "Drink water or an electrolyte drink",
                        "Have something light to eat if you haven't already",
                        "If this keeps happening regularly, it's worth mentioning to a doctor");
            if (hasHeadache && hasFatigue)
                return en("Tension or Stress Headache",
                        "Headache with fatigue is often just your body telling you it needs a break.",
                        "Take a proper break and rest",
                        "Drink some water — dehydration is a sneaky cause of headaches",
                        "A cool or warm compress on your forehead can help",
                        "Try to get a good night's sleep tonight");
            if (hasHeadache && hasNausea)
                return en("Possible Migraine",
                        "Headache with nausea is a classic migraine combination. Not dangerous, but can feel pretty rough.",
                        "Find a dark, quiet room and rest",
                        "A cold compress on your forehead can help",
                        "Sip water slowly — don't chug it",
                        "If this is the first time, see a doctor to confirm");
            if (hasFatigue && hasWeakness)
                return en("Fatigue and Weakness",
                        "Feeling tired and weak together could mean a few things — anaemia, low blood sugar, or just being run down.",
                        "Eat a proper meal if you haven't — low blood sugar can cause this",
                        "Rest and don't push yourself today",
                        "If this has been going on for more than a week, see a doctor",
                        "A blood test can often identify the cause quite quickly");
            if (hasFatigue && hasDizziness)
                return en("Low Energy and Dizziness",
                        "Feeling fatigued and dizzy often points to dehydration, skipping meals, or just being exhausted.",
                        "Sit down and don't rush to get up",
                        "Eat something and drink water or an electrolyte drink",
                        "Rest properly — your body is telling you it needs a break",
                        "If it keeps happening, it's worth getting a check-up");
            if (hasWeakness && hasDizziness)
                return en("Weakness and Dizziness",
                        "Feeling weak and dizzy together can happen with dehydration, low blood pressure, or low blood sugar.",
                        "Sit or lie down straight away",
                        "Drink water or an electrolyte drink",
                        "Have a light snack",
                        "See a doctor if this doesn't improve quickly or keeps coming back");
            if (hasWeakness && hasNausea)
                return en("Weakness with Nausea",
                        "Feeling weak and nauseous together often happens with dehydration, a stomach bug, or not eating enough.",
                        "Sip fluids slowly — ORS or clear fluids are best",
                        "Try to eat something light when you feel able",
                        "Rest and avoid any physical exertion",
                        "If it's not improving after a day, see a doctor");
            if (hasNausea && hasDizziness)
                return en("Nausea and Dizziness",
                        "This combination can come from dehydration, an inner ear issue, or motion sickness.",
                        "Sit or lie down and avoid sudden movements",
                        "Sip water or an electrolyte drink slowly",
                        "Get some fresh air if you can",
                        "If it's been going on for more than a day, worth getting checked");
            if (hasDiarrhea && hasWeakness)
                return en("Diarrhea with Weakness",
                        "Diarrhea causing weakness is usually a sign of dehydration setting in. The priority here is getting fluids back in.",
                        "ORS is really important here — get some and sip regularly",
                        "Eat bland foods when you can — banana, rice, toast",
                        "Rest and avoid any exertion",
                        "If weakness is getting worse or diarrhea continues beyond 2 days, see a doctor");
            if (hasDiarrhea && hasNausea)
                return en("Stomach Bug",
                        "Diarrhea and nausea together usually mean a stomach bug or something you ate.",
                        "Focus on staying hydrated — ORS or clear fluids",
                        "Eat light and bland when you feel ready",
                        "Rest up",
                        "See a doctor if symptoms continue beyond 48 hours");
            if (hasEye && hasHeadache)
                return en("Eye Strain with Headache",
                        "Eye discomfort and headache together are very commonly caused by too much screen time.",
                        "Step away from screens for a while — give your eyes a proper rest",
                        "Try the 20-20-20 rule: every 20 mins, look 20 feet away for 20 seconds",
                        "Lubricating eye drops can help if your eyes feel dry",
                        "If this is a regular thing, it might be worth getting your eyes tested");
            if (hasToothache && hasFever)
                return en("Possible Dental Infection",
                        "A toothache with fever can sometimes mean there's an infection brewing. Worth getting seen sooner rather than later.",
                        "Try to get a dentist appointment within the next day",
                        "Warm salt water rinses can help ease the discomfort",
                        "Paracetamol can help manage the pain and fever",
                        "This kind of thing usually needs treatment to fully clear up");
            if (hasRash && hasWeakness)
                return en("Rash with Weakness",
                        "A rash alongside feeling weak is worth getting looked at — could be an allergic reaction or viral infection.",
                        "See a doctor within the next day",
                        "Don't scratch the rash",
                        "Rest and stay hydrated",
                        "Note anything new you may have eaten, touched, or taken recently");
            if (hasRash && hasFatigue)
                return en("Rash with Fatigue",
                        "A rash with fatigue can sometimes come with viral infections or allergic reactions.",
                        "Rest and stay hydrated",
                        "Keep the rash area cool and avoid scratching",
                        "If the rash is spreading or you develop a fever, see a doctor",
                        "Think about any new products, foods, or medications recently");
            if (hasDizziness && hasBodyAche)
                return en("Dizziness with Body Ache",
                        "Dizziness and body aches together are often a sign of dehydration or early viral illness.",
                        "Rest and drink plenty of fluids",
                        "Eat something if you haven't — low blood sugar can cause both",
                        "Avoid sudden movements",
                        "See a doctor if a fever develops or things don't improve");

            // English generic fallback
            StringBuilder sl = new StringBuilder();
            for (int i = 0; i < s.size(); i++) {
                sl.append(formatSymptomName(s.get(i)));
                if (i < s.size() - 2) sl.append(", ");
                else if (i == s.size() - 2) sl.append(" and ");
            }
            return en("A Few Things Going On",
                    "You're experiencing " + sl + ". It's not an emergency, but with a few things happening at once it's a good idea to keep an eye on how you feel.",
                    "Rest and stay hydrated — that helps with most things",
                    "Take paracetamol if you're in pain or have a fever",
                    "Monitor how you feel every few hours",
                    "If things aren't improving after 24–48 hours, it's worth seeing a doctor");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // SAFE response builder
    // ═════════════════════════════════════════════════════════════════════════════

    private HealthAssessment buildSafeResponse(Symptom symptom) {
        if (isHindi) {
            switch (symptom) {
                case FEVER: return hi("हल्का बुखार",
                        "हल्का बुखार अकेले आमतौर पर शरीर का किसी छोटे संक्रमण से लड़ने का तरीका है।",
                        "जितना हो सके आराम करें", "खूब पानी, सूप और जूस पिएं",
                        "तकलीफ हो तो पैरासिटामोल लें", "2-3 दिन में ठीक हो जाएगा");
                case COUGH: return hi("हल्की खांसी",
                        "हल्की खांसी अकेले आमतौर पर गले की जलन या सर्दी की शुरुआत होती है।",
                        "शहद वाली गर्म चाय पिएं", "ठंडे पेय और धूल भरी जगह से बचें",
                        "भाप लें", "एक हफ्ते में ठीक न हो तो डॉक्टर से मिलें");
                case HEADACHE: return hi("हल्का सिरदर्द",
                        "हल्का सिरदर्द अकेले आमतौर पर तनाव, पानी की कमी, या स्क्रीन की वजह से होता है।",
                        "पानी पिएं — सबसे पहले यही करें", "स्क्रीन से ब्रेक लें",
                        "माथे पर ठंडा या गर्म कपड़ा रखें", "ज़रूरत हो तो दर्द निवारक लें");
                case FATIGUE: return hi("थकान",
                        "थकान अकेले अक्सर खराब नींद, तनाव या लंबे दिन की वजह से होती है।",
                        "आज रात 7-9 घंटे की अच्छी नींद लें", "थोड़ी देर टहलें",
                        "संतुलित खाना खाएं", "दोपहर 2 बजे के बाद चाय-कॉफी कम करें");
                case SORE_THROAT: return hi("गले में दर्द",
                        "गले में दर्द अकेले आमतौर पर हल्की जलन या सर्दी की शुरुआत होती है।",
                        "दिन में 3 बार गर्म नमक के पानी से गरारे करें",
                        "शहद वाली गर्म चाय पिएं", "ठंडे पेय से बचें",
                        "5 दिन में ठीक न हो तो डॉक्टर से मिलें");
                case VOMITING: return hi("उल्टी",
                        "एक बार उल्टी होना आमतौर पर खाने की वजह से या हल्की पेट की तकलीफ से होता है।",
                        "धीरे-धीरे साफ पानी पिएं", "कुछ देर ठोस खाना न खाएं",
                        "हल्का खाना खाएं जब ठीक लगे — चावल, टोस्ट", "उल्टी जारी रहे तो डॉक्टर से मिलें");
                case BODY_ACHE: return hi("बदन दर्द",
                        "बदन दर्द अकेले आमतौर पर व्यायाम, खराब मुद्रा, या हल्की थकान से होता है।",
                        "हल्की स्ट्रेचिंग करें", "गर्म पानी से नहाएं",
                        "पानी पिएं", "दर्द वाली जगह हल्की मालिश करें");
                case DIZZINESS: return hi("हल्के चक्कर",
                        "हल्के चक्कर अकेले आमतौर पर पानी की कमी, जल्दी उठने, या खाना न खाने से होते हैं।",
                        "तुरंत बैठ जाएं या लेट जाएं", "पानी या इलेक्ट्रोलाइट ड्रिंक पिएं",
                        "कुछ हल्का खाएं", "धीरे-धीरे उठें");
                case SKIN_RASH: return hi("हल्के चकत्ते",
                        "चकत्ते अकेले अक्सर एलर्जी, गर्मी, या किसी चीज़ से छूने से होते हैं।",
                        "ठंडा कपड़ा लगाएं", "कैलामाइन लोशन लगाएं",
                        "खुजलाएं नहीं", "2-3 दिन में ठीक हो जाएगा");
                case EYE_DISCOMFORT: return hi("आँखों में तकलीफ",
                        "आँखों में तकलीफ अकेले आमतौर पर स्क्रीन की थकान या सूखापन से होती है।",
                        "स्क्रीन से ब्रेक लें", "20-20-20 का नियम अपनाएं",
                        "आई ड्रॉप्स लगाएं", "अच्छी नींद लें");
                case TOOTHACHE: return hi("दांत दर्द",
                        "दांत दर्द अकेले आमतौर पर कैविटी, संवेदनशीलता, या मसूड़ों की जलन से होता है।",
                        "गर्म नमक के पानी से दिन में 3 बार कुल्ला करें",
                        "बहुत गर्म, ठंडा, या मीठा खाने से बचें",
                        "दर्द निवारक लें", "2-3 दिन में दंत चिकित्सक से मिलें");
                case NAUSEA: return hi("जी मिचलाना",
                        "जी मिचलाना अकेले खाने से, तनाव से, या हल्की पेट की तकलीफ से होता है।",
                        "अदरक वाली चाय या साफ पानी धीरे-धीरे पिएं",
                        "हल्का खाना खाएं — बिस्कुट, टोस्ट", "ताज़ी हवा में जाएं",
                        "कुछ घंटों में ठीक हो जाएगा");
                case WEAKNESS: return hi("कमज़ोरी",
                        "कमज़ोरी अकेले अक्सर खाना न खाने, पानी की कमी, या थकान से होती है।",
                        "कुछ खाएं — कम शुगर से कमज़ोरी हो सकती है",
                        "पानी या इलेक्ट्रोलाइट ड्रिंक पिएं", "थोड़ा आराम करें",
                        "2 दिन बाद भी ठीक न हो तो डॉक्टर से मिलें");
                case DIARRHEA: return hi("हल्के दस्त",
                        "दस्त अकेले आमतौर पर खाने से या हल्के पेट के वायरस से होते हैं।",
                        "ORS या साफ पानी पिएं", "केला, चावल, टोस्ट जैसा हल्का खाना खाएं",
                        "डेयरी, मसालेदार खाना बंद करें", "2 दिन बाद भी ठीक न हो तो डॉक्टर से मिलें");
                case STOMACH_ACHE: return hi("पेट दर्द",
                        "पेट दर्द अकेले आमतौर पर अपच, गैस, या हल्की खाने की जलन से होता है।",
                        "गर्म पानी या हर्बल चाय पिएं", "पेट पर गर्म कपड़ा रखें",
                        "मसालेदार और भारी खाना बंद करें", "कुछ घंटों में ठीक हो जाएगा");
                default: return hi("हल्का लक्षण",
                        "यह लक्षण अकेले आमतौर पर गंभीर नहीं होता।",
                        "आराम करें और पानी पिएं", "24 घंटे देखें",
                        "बिगड़े तो डॉक्टर से मिलें");
            }
        } else {
            switch (symptom) {
                case FEVER: return en("Mild Fever",
                        "A mild fever on its own usually just means your body is fighting off something minor. Totally normal — just give it some time.",
                        "Rest as much as you can and let your body do its thing",
                        "Drink plenty of fluids — water, soup, juice",
                        "Paracetamol can help if you're feeling uncomfortable",
                        "Should settle down in 2–3 days");
                case COUGH: return en("Mild Cough",
                        "A cough on its own is usually just a throat irritation or the start of a mild cold. Nothing to stress about.",
                        "Warm drinks with honey are genuinely helpful here",
                        "Avoid cold drinks and dusty environments",
                        "Steam inhalation can ease the irritation",
                        "If it's still around after a week, it's worth getting checked");
                case HEADACHE: return en("Mild Headache",
                        "A mild headache on its own is almost always due to stress, dehydration, or too much screen time.",
                        "Drink some water first — dehydration is the most common cause",
                        "Step away from screens and rest for a bit",
                        "A cold or warm compress on your forehead can help",
                        "OTC pain relief works well if needed");
                case FATIGUE: return en("Feeling Tired",
                        "Fatigue on its own is almost always just your body telling you to slow down.",
                        "Prioritise getting a proper night's sleep tonight",
                        "A short walk can actually boost your energy levels",
                        "Eat a balanced meal if you haven't",
                        "Cut back on caffeine after 2 PM");
                case SORE_THROAT: return en("Sore Throat",
                        "A sore throat on its own is usually just minor irritation or the very beginning of a cold.",
                        "Warm salt water gargles a few times a day work really well",
                        "Honey in warm tea is soothing and actually helps",
                        "Avoid cold drinks and ice cream for now",
                        "See a doctor if it's still bothering you after 5 days");
                case VOMITING: return en("Vomiting",
                        "A single bout of vomiting is usually just your body reacting to something you ate.",
                        "Sip clear fluids slowly once it passes",
                        "Give your stomach a break from solid food for a while",
                        "Ease back in with bland food — rice, toast, crackers",
                        "See a doctor if it keeps happening");
                case BODY_ACHE: return en("Body Ache",
                        "Body ache by itself is usually from exercise, sitting in one position too long, or just being run down.",
                        "Gentle stretching or light yoga can help a lot",
                        "A warm bath is genuinely relaxing for muscle aches",
                        "Stay hydrated — it helps muscles recover",
                        "Rest and let your muscles recover");
                case DIZZINESS: return en("Mild Dizziness",
                        "Mild dizziness by itself is almost always dehydration, getting up too fast, or skipping a meal.",
                        "Sit or lie down straight away — don't push through it",
                        "Drink some water or an electrolyte drink",
                        "Have a light snack if you haven't eaten recently",
                        "Should pass in a few minutes");
                case SKIN_RASH: return en("Mild Skin Rash",
                        "A rash by itself is often just contact dermatitis, heat rash, or a mild reaction to something.",
                        "A cool compress on the area helps soothe it",
                        "Calamine lotion or a gentle moisturiser works well",
                        "Try not to scratch — it makes it worse",
                        "Most mild rashes clear up within 2–3 days");
                case EYE_DISCOMFORT: return en("Eye Discomfort",
                        "Eye discomfort by itself is almost always from screen fatigue, dryness, or mild irritation.",
                        "Give your eyes a proper screen break",
                        "The 20-20-20 rule helps: every 20 mins, look 20 feet away for 20 seconds",
                        "Lubricating eye drops feel great and help a lot",
                        "Make sure you're getting enough sleep");
                case TOOTHACHE: return en("Toothache",
                        "Toothache by itself is usually a cavity, sensitivity, or some gum irritation. Not an emergency, but don't ignore it too long.",
                        "Warm salt water rinses a few times a day can help",
                        "Avoid very hot, cold, or sweet foods",
                        "OTC pain relief can tide you over",
                        "Try to see a dentist within the next couple of days");
                case NAUSEA: return en("Mild Nausea",
                        "Nausea on its own is often just from something you ate, stress, or tiredness.",
                        "Ginger tea is genuinely helpful for nausea",
                        "Sip clear fluids — don't gulp",
                        "Bland food helps — crackers, toast, plain rice",
                        "Should ease up within a few hours");
                case WEAKNESS: return en("Feeling Weak",
                        "Weakness on its own often just means you haven't eaten, you're dehydrated, or you're tired.",
                        "Eat something — low blood sugar can make you feel really weak",
                        "Drink water or an electrolyte drink",
                        "Sit down and rest for a bit",
                        "See a doctor if it doesn't improve or keeps coming back");
                case DIARRHEA: return en("Mild Diarrhea",
                        "Diarrhea on its own is usually from something you ate or a mild stomach bug.",
                        "Stay hydrated — ORS is ideal, or plain water",
                        "Stick to bland foods — banana, rice, toast",
                        "Avoid dairy, spicy, or oily food for now",
                        "See a doctor if it's still going after 2 days");
                case STOMACH_ACHE: return en("Stomach Ache",
                        "Stomach ache by itself is very commonly just indigestion, gas, or mild irritation from food.",
                        "Sip warm water or herbal tea",
                        "A warm compress on your tummy can feel really soothing",
                        "Avoid spicy, oily, or heavy food",
                        "Should feel better within a few hours");
                default: return en("Mild Symptom",
                        "This symptom on its own is generally nothing to worry about. Rest up and see how you feel.",
                        "Rest and stay hydrated",
                        "Monitor how you feel over the next day",
                        "See a doctor if it gets worse or doesn't improve");
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // Danger condition name / explanation / action helpers
    // ═════════════════════════════════════════════════════════════════════════════

    private String getConditionName(List<Symptom> danger, List<Symptom> mild) {
        if (isHindi) {
            if (danger.contains(Symptom.CHEST_PAIN) && danger.contains(Symptom.SHORTNESS_OF_BREATH))
                return "संभावित हृदय या फेफड़ों की आपात स्थिति";
            if (danger.contains(Symptom.CHEST_PAIN))           return "सीने में दर्द — तुरंत ध्यान दें";
            if (danger.contains(Symptom.SHORTNESS_OF_BREATH))  return "सांस की तकलीफ — तुरंत ध्यान दें";
            if (danger.contains(Symptom.BLOOD_IN_STOOL))       return "पेट में खून — आपात स्थिति";
            if (danger.contains(Symptom.BLOOD_IN_URINE))       return "पेशाब में खून — जल्दी जांच ज़रूरी";
            if (danger.contains(Symptom.WEIGHT_LOSS))          return "वज़न घटना — जांच ज़रूरी";
            return "गंभीर लक्षण — ध्यान दें";
        } else {
            if (danger.contains(Symptom.CHEST_PAIN) && danger.contains(Symptom.SHORTNESS_OF_BREATH))
                return "Possible Cardiac or Pulmonary Emergency";
            if (danger.contains(Symptom.CHEST_PAIN))           return "Chest Pain with Other Symptoms";
            if (danger.contains(Symptom.SHORTNESS_OF_BREATH))  return "Breathing Difficulty with Other Symptoms";
            if (danger.contains(Symptom.BLOOD_IN_STOOL))       return "Possible Gastrointestinal Bleeding";
            if (danger.contains(Symptom.BLOOD_IN_URINE))       return "Blood in Urine with Other Symptoms";
            if (danger.contains(Symptom.WEIGHT_LOSS))          return "Unexplained Weight Loss with Other Symptoms";
            return "Symptoms That Need Attention";
        }
    }

    private String buildDangerExplanation(List<Symptom> danger, List<Symptom> mild) {
        if (isHindi) {
            if (danger.contains(Symptom.CHEST_PAIN) && mild.contains(Symptom.DIZZINESS))
                return "सीने में दर्द के साथ चक्कर — यह दिल की समस्या हो सकती है। देर न करें।";
            if (danger.contains(Symptom.CHEST_PAIN) && mild.contains(Symptom.WEAKNESS))
                return "सीने में दर्द के साथ कमज़ोरी — यह गंभीर हो सकता है। तुरंत मदद लें।";
            if (danger.contains(Symptom.SHORTNESS_OF_BREATH) && mild.contains(Symptom.FEVER))
                return "सांस की तकलीफ के साथ बुखार — फेफड़ों का संक्रमण हो सकता है। आज ही जांच करवाएं।";
            if (danger.contains(Symptom.SHORTNESS_OF_BREATH) && mild.contains(Symptom.COUGH))
                return "सांस की तकलीफ के साथ खांसी — इसे नज़रअंदाज़ न करें, डॉक्टर से मिलें।";
            if (danger.contains(Symptom.BLOOD_IN_STOOL) && mild.contains(Symptom.WEAKNESS))
                return "मल में खून के साथ कमज़ोरी — शरीर में खून की कमी हो सकती है। तुरंत मदद लें।";
            return "आपके लक्षण गंभीर हैं। कृपया अभी मदद लें — देर न करें।";
        } else {
            if (danger.contains(Symptom.CHEST_PAIN) && mild.contains(Symptom.DIZZINESS))
                return "Chest pain with dizziness can indicate a serious heart condition. Do not wait.";
            if (danger.contains(Symptom.CHEST_PAIN) && mild.contains(Symptom.WEAKNESS))
                return "Chest pain alongside weakness is worth taking seriously. Please get seen as soon as possible.";
            if (danger.contains(Symptom.SHORTNESS_OF_BREATH) && mild.contains(Symptom.FEVER))
                return "Difficulty breathing with a fever can sometimes indicate a lung infection. Best to get checked out today.";
            if (danger.contains(Symptom.SHORTNESS_OF_BREATH) && mild.contains(Symptom.COUGH))
                return "Breathing difficulty combined with a cough needs medical attention.";
            if (danger.contains(Symptom.BLOOD_IN_STOOL) && mild.contains(Symptom.WEAKNESS))
                return "Blood in stool with weakness suggests your body may be losing more blood than it should. Please get seen urgently.";
            return "You have a symptom that needs medical attention, especially alongside your other symptoms. Please don't put this one off.";
        }
    }

    private List<String> buildDangerActions(List<Symptom> danger, List<Symptom> mild) {
        if (isHindi) {
            if (danger.contains(Symptom.CHEST_PAIN))
                return Arrays.asList("तुरंत 102 / 108 पर कॉल करें — देर न करें",
                        "आराम से बैठें या लेटें, शांत रहें",
                        "कपड़े ढीले करें", "खुद गाड़ी न चलाएं",
                        "कोई आपके साथ रहे");
            if (danger.contains(Symptom.SHORTNESS_OF_BREATH))
                return Arrays.asList("अगर सांस बहुत मुश्किल हो तो 102 / 108 पर कॉल करें",
                        "सीधे बैठें — लेटें नहीं", "शांत रहें और धीरे-धीरे सांस लें",
                        "कोई मेहनत का काम न करें", "कोई आपके साथ रहे");
            if (danger.contains(Symptom.BLOOD_IN_STOOL) || danger.contains(Symptom.BLOOD_IN_URINE))
                return Arrays.asList("अभी अस्पताल जाएं या डॉक्टर को बुलाएं",
                        "अभी कुछ खाएं-पिएं नहीं", "खून का रंग और मात्रा नोट करें",
                        "जो दवाएं ले रहे हैं वो साथ ले जाएं",
                        "बहुत कमज़ोरी हो तो खुद गाड़ी न चलाएं");
            return Arrays.asList("आज ही डॉक्टर से मिलें — देर न करें",
                    "हालत बिगड़े तो 102 / 108 पर कॉल करें",
                    "कोई साथ हो तो उन्हें ले जाएं",
                    "जो दवाएं ले रहे हैं उनकी लिस्ट साथ रखें");
        } else {
            if (danger.contains(Symptom.CHEST_PAIN))
                return Arrays.asList("Call emergency services (102 / 108) — don't wait on this one",
                        "Sit or lie down comfortably and try to stay calm",
                        "Loosen any tight clothing",
                        "Don't drive yourself — have someone take you or wait for the ambulance",
                        "Have someone stay with you");
            if (danger.contains(Symptom.SHORTNESS_OF_BREATH))
                return Arrays.asList("Call 102 / 108 if breathing is very difficult",
                        "Sit upright — don't lie flat", "Try to stay calm and breathe slowly",
                        "Don't exert yourself at all", "Have someone stay with you");
            if (danger.contains(Symptom.BLOOD_IN_STOOL) || danger.contains(Symptom.BLOOD_IN_URINE))
                return Arrays.asList("Head to the emergency room or see a doctor urgently",
                        "Don't eat or drink anything for now",
                        "Note the color and how much blood you noticed",
                        "Bring any medications you're currently taking",
                        "If you're feeling very weak, don't drive yourself");
            return Arrays.asList("Please seek medical care today — don't put this off",
                    "Call 102 / 108 if you feel very unwell or things deteriorate quickly",
                    "Have someone go with you if possible",
                    "Bring a list of any medications you're taking");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // Convenience builders
    // ═════════════════════════════════════════════════════════════════════════════

    private HealthAssessment en(String condition, String explanation,
                                String a1, String a2, String a3, String a4) {
        return new HealthAssessment(RiskLevel.MODERATE, condition, explanation,
                Arrays.asList(a1, a2, a3, a4), false);
    }

    private HealthAssessment en(String condition, String explanation,
                                String a1, String a2, String a3) {
        return new HealthAssessment(RiskLevel.MODERATE, condition, explanation,
                Arrays.asList(a1, a2, a3), false);
    }

    private HealthAssessment hi(String condition, String explanation,
                                String a1, String a2, String a3, String a4) {
        return new HealthAssessment(RiskLevel.MODERATE, condition, explanation,
                Arrays.asList(a1, a2, a3, a4), false);
    }

    private HealthAssessment hi(String condition, String explanation,
                                String a1, String a2, String a3) {
        return new HealthAssessment(RiskLevel.MODERATE, condition, explanation,
                Arrays.asList(a1, a2, a3), false);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // Symptom name formatter
    // ═════════════════════════════════════════════════════════════════════════════

    private String formatSymptomName(Symptom symptom) {
        if (isHindi) {
            switch (symptom) {
                case FEVER:               return "बुखार";
                case COUGH:               return "खांसी";
                case HEADACHE:            return "सिरदर्द";
                case FATIGUE:             return "थकान";
                case SORE_THROAT:         return "गले में दर्द";
                case VOMITING:            return "उल्टी";
                case BODY_ACHE:           return "बदन दर्द";
                case DIZZINESS:           return "चक्कर";
                case SKIN_RASH:           return "चकत्ते";
                case EYE_DISCOMFORT:      return "आँखों में तकलीफ";
                case TOOTHACHE:           return "दांत दर्द";
                case CHEST_PAIN:          return "सीने में दर्द";
                case SHORTNESS_OF_BREATH: return "सांस की तकलीफ";
                case NAUSEA:              return "जी मिचलाना";
                case WEAKNESS:            return "कमज़ोरी";
                case WEIGHT_LOSS:         return "वज़न घटना";
                case BLOOD_IN_STOOL:      return "मल में खून";
                case BLOOD_IN_URINE:      return "पेशाब में खून";
                case DIARRHEA:            return "दस्त";
                case STOMACH_ACHE:        return "पेट दर्द";
                default: return symptom.name().toLowerCase();
            }
        } else {
            switch (symptom) {
                case FEVER:               return "fever";
                case COUGH:               return "cough";
                case HEADACHE:            return "headache";
                case FATIGUE:             return "fatigue";
                case SORE_THROAT:         return "sore throat";
                case VOMITING:            return "vomiting";
                case BODY_ACHE:           return "body ache";
                case DIZZINESS:           return "dizziness";
                case SKIN_RASH:           return "skin rash";
                case EYE_DISCOMFORT:      return "eye discomfort";
                case TOOTHACHE:           return "toothache";
                case CHEST_PAIN:          return "chest pain";
                case SHORTNESS_OF_BREATH: return "shortness of breath";
                case NAUSEA:              return "nausea";
                case WEAKNESS:            return "weakness";
                case WEIGHT_LOSS:         return "weight loss";
                case BLOOD_IN_STOOL:      return "blood in stool";
                case BLOOD_IN_URINE:      return "blood in urine";
                case DIARRHEA:            return "diarrhea";
                case STOMACH_ACHE:        return "stomach ache";
                default: return symptom.name().toLowerCase().replace("_", " ");
            }
        }
    }
}