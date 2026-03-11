package com.example.svarp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HealthDecisionEngine {

    // Risk levels
    public static final String SAFE = "SAFE";
    public static final String MODERATE = "MODERATE";
    public static final String DANGER = "DANGER";

    // Emergency keywords that immediately trigger DANGER
    private static final Set<String> EMERGENCY_KEYWORDS = new HashSet<>(Arrays.asList(
            "chest pain", "heart attack", "can't breathe", "suicide", "unconscious",
            "bleeding heavily", "severe bleeding", "stroke", "seizure", "not breathing",
            "passed out", "severe chest", "pressure chest", "left arm pain"
    ));

    // Main entry point - analyze user input
    public HealthAssessment analyzeInput(String userInput, List<String> selectedSymptoms) {
        String combinedInput = (userInput + " " + String.join(" ", selectedSymptoms)).toLowerCase();

        // Step 1: Emergency scan
        if (isEmergency(combinedInput)) {
            return createEmergencyResponse();
        }

        // Step 2: Check symptom combinations first, then individual symptoms
        return analyzeSymptoms(combinedInput, selectedSymptoms);
    }

    private boolean isEmergency(String input) {
        for (String keyword : EMERGENCY_KEYWORDS) {
            if (input.contains(keyword)) return true;
        }
        return false;
    }

    private HealthAssessment createEmergencyResponse() {
        return new HealthAssessment(
                DANGER,
                "Potential Medical Emergency",
                "Symptoms indicate a potentially serious condition requiring immediate medical attention.",
                Arrays.asList(
                        "Contact emergency services immediately",
                        "Do not attempt to drive yourself",
                        "Remain calm and rest in a comfortable position",
                        "Have someone stay with you if possible"
                ),
                true
        );
    }

    // NEW: Analyze combinations first, then individual symptoms
    private HealthAssessment analyzeSymptoms(String input, List<String> symptoms) {
        // Convert to Set for easier checking
        Set<String> symptomSet = new HashSet<>();
        for (String s : symptoms) {
            symptomSet.add(s.toLowerCase());
        }

        // Check input text too
        String[] inputWords = input.split(" ");
        for (String word : inputWords) {
            symptomSet.add(word.toLowerCase());
        }

        boolean hasFever = symptomSet.contains("fever");
        boolean hasHeadache = symptomSet.contains("headache") || input.contains("headache");
        boolean hasStiffNeck = input.contains("stiff neck") || input.contains("neck pain");
        boolean hasCough = symptomSet.contains("cough") || input.contains("cough");
        boolean hasFatigue = symptomSet.contains("fatigue") || input.contains("tired") || input.contains("fatigue");
        boolean hasBodyAche = symptomSet.contains("body ache") || input.contains("body ache") || input.contains("body pain");
        boolean hasNausea = symptomSet.contains("nausea") || input.contains("nausea") || input.contains("vomiting");
        boolean hasStomachPain = symptomSet.contains("stomach pain") || input.contains("stomach pain") || input.contains("abdominal");
        boolean hasDizziness = symptomSet.contains("dizziness") || input.contains("dizzy") || input.contains("dizziness");
        boolean hasRash = symptomSet.contains("skin rash") || input.contains("rash");
        boolean hasSoreThroat = symptomSet.contains("sore throat") || input.contains("sore throat");

        // DANGER COMBINATIONS
        if (hasFever && hasHeadache && hasStiffNeck) {
            return new HealthAssessment(
                    DANGER,
                    "Possible Meningitis",
                    "The combination of fever, headache, and neck stiffness requires immediate medical evaluation.",
                    Arrays.asList(
                            "Seek emergency care immediately",
                            "Avoid bright lights during transport",
                            "Note when symptoms started",
                            "Do not delay seeking care"
                    ),
                    true
            );
        }

        if (hasFever && hasRash && (input.contains("spreading") || input.contains("high"))) {
            return new HealthAssessment(
                    DANGER,
                    "Fever with Spreading Rash",
                    "Fever accompanied by a rapidly spreading rash may indicate a serious infection.",
                    Arrays.asList(
                            "Go to emergency room immediately",
                            "Document rash progression with photos if possible",
                            "Note any recent medications or new foods",
                            "Do not apply creams or ointments before evaluation"
                    ),
                    true
            );
        }

        // MODERATE COMBINATIONS
        if (hasFever && hasBodyAche && hasFatigue) {
            return new HealthAssessment(
                    MODERATE,
                    "Possible Viral Illness",
                    "These symptoms commonly occur together with viral infections such as influenza.",
                    Arrays.asList(
                            "Rest and maintain adequate hydration",
                            "Monitor temperature every 4-6 hours",
                            "Consider paracetamol for fever and discomfort",
                            "Consult a physician if symptoms persist beyond 48 hours",
                            "Avoid close contact with others to prevent transmission"
                    ),
                    false
            );
        }

        if (hasFever && hasCough && hasSoreThroat) {
            return new HealthAssessment(
                    MODERATE,
                    "Respiratory Infection",
                    "These symptoms suggest a respiratory infection that may require medical evaluation.",
                    Arrays.asList(
                            "Rest and drink plenty of fluids",
                            "Use warm salt water gargles for throat discomfort",
                            "Monitor for breathing difficulties",
                            "Schedule a medical appointment if fever persists beyond 3 days",
                            "Practice good respiratory hygiene"
                    ),
                    false
            );
        }

        if (hasStomachPain && hasNausea && hasDizziness) {
            return new HealthAssessment(
                    MODERATE,
                    "Gastrointestinal Disturbance",
                    "This combination may indicate dehydration, food-related illness, or inner ear issues.",
                    Arrays.asList(
                            "Sip oral rehydration solution or clear fluids slowly",
                            "Rest in a comfortable position",
                            "Avoid solid foods until nausea subsides",
                            "Monitor for signs of dehydration",
                            "Seek medical care if unable to retain fluids"
                    ),
                    false
            );
        }

        if (hasHeadache && hasDizziness && hasFatigue) {
            return new HealthAssessment(
                    MODERATE,
                    "Tension or Migraine Symptoms",
                    "These symptoms often occur together with stress, dehydration, or migraine conditions.",
                    Arrays.asList(
                            "Rest in a quiet, darkened room",
                            "Ensure adequate hydration",
                            "Apply cold or warm compress to head and neck",
                            "Consider stress reduction techniques",
                            "Consult a physician if this is a recurring pattern"
                    ),
                    false
            );
        }

        if (hasCough && hasFatigue && hasBodyAche) {
            return new HealthAssessment(
                    MODERATE,
                    "Possible Respiratory Viral Illness",
                    "These symptoms commonly present together with viral respiratory infections.",
                    Arrays.asList(
                            "Rest and maintain fluid intake",
                            "Use humidifier or steam inhalation",
                            "Monitor for fever development",
                            "Consider medical evaluation if symptoms worsen",
                            "Allow adequate recovery time before resuming normal activities"
                    ),
                    false
            );
        }

        // If no combination matched, check individual symptoms
        return runDecisionTree(input, symptoms);
    }

    private HealthAssessment runDecisionTree(String input, List<String> symptoms) {
        // HEADACHE BRANCH
        if (input.contains("headache") || input.contains("head pain") || input.contains("migraine")) {
            return assessHeadache(input, symptoms);
        }

        // FEVER BRANCH
        if (input.contains("fever") || input.contains("temperature") || input.contains("hot body")) {
            return assessFever(input, symptoms);
        }

        // COLD/COUGH BRANCH
        if (input.contains("cold") || input.contains("cough") || input.contains("sore throat") ||
                input.contains("runny nose") || input.contains("congestion")) {
            return assessCold(input, symptoms);
        }

        // FATIGUE BRANCH
        if (input.contains("tired") || input.contains("fatigue") || input.contains("weakness") ||
                input.contains("no energy")) {
            return assessFatigue(input, symptoms);
        }

        // NAUSEA BRANCH
        if (input.contains("nausea") || input.contains("vomiting") || input.contains("feeling sick")) {
            return assessNausea(input, symptoms);
        }

        // STOMACH PAIN BRANCH
        if (input.contains("stomach") || input.contains("stomach pain") || input.contains("abdominal") ||
                input.contains("belly pain") || input.contains("tummy")) {
            return assessStomachPain(input, symptoms);
        }

        // BODY ACHE BRANCH
        if (input.contains("body ache") || input.contains("body pain") || input.contains("muscle pain") ||
                input.contains("sore muscles") || input.contains("body hurt")) {
            return assessBodyAche(input, symptoms);
        }

        // DIZZINESS BRANCH
        if (input.contains("dizzy") || input.contains("dizziness") || input.contains("lightheaded") ||
                input.contains("spinning") || input.contains("vertigo")) {
            return assessDizziness(input, symptoms);
        }

        // SKIN RASH BRANCH
        if (input.contains("rash") || input.contains("skin") || input.contains("itching") ||
                input.contains("red spots") || input.contains("hives")) {
            return assessSkinRash(input, symptoms);
        }

        // EYE DISCOMFORT BRANCH
        if (input.contains("eye") || input.contains("eye pain") || input.contains("red eye") ||
                input.contains("eye discomfort") || input.contains("eye irritation")) {
            return assessEyeDiscomfort(input, symptoms);
        }

        // TOOTHACHE BRANCH
        if (input.contains("tooth") || input.contains("toothache") || input.contains("dental") ||
                input.contains("gum pain") || input.contains("jaw pain")) {
            return assessToothache(input, symptoms);
        }

        // Default fallback
        return new HealthAssessment(
                MODERATE,
                "Unspecified Condition",
                "Based on the provided information, a specific assessment cannot be determined. General wellness measures are recommended.",
                Arrays.asList(
                        "Ensure adequate rest and hydration",
                        "Monitor symptoms over the next 24 hours",
                        "Consult a healthcare provider if symptoms persist or worsen",
                        "Maintain a record of any new or changing symptoms"
                ),
                false
        );
    }

    private HealthAssessment assessHeadache(String input, List<String> symptoms) {
        boolean severe = input.contains("severe") || input.contains("worst") || input.contains("unbearable");
        boolean sudden = input.contains("sudden") || input.contains("abrupt") || input.contains("out of nowhere");
        boolean migraine = input.contains("migraine") || input.contains("throbbing") || input.contains("one side");
        boolean tension = input.contains("tension") || input.contains("stress") || input.contains("band around");
        boolean fever = input.contains("fever") || symptoms.contains("Fever");
        boolean neckStiff = input.contains("stiff neck") || input.contains("neck pain");

        if (severe && sudden) {
            return new HealthAssessment(
                    DANGER,
                    "Thunderclap Headache",
                    "A sudden severe headache requires immediate evaluation to rule out serious conditions.",
                    Arrays.asList(
                            "Seek emergency care immediately",
                            "Avoid taking aspirin or blood-thinning medications",
                            "Note the exact time of onset",
                            "Bring a complete list of current medications"
                    ),
                    true
            );
        }

        if (migraine) {
            return new HealthAssessment(
                    MODERATE,
                    "Probable Migraine",
                    "Symptoms are consistent with migraine presentation, characterized by throbbing pain often localized to one side.",
                    Arrays.asList(
                            "Rest in a dark, quiet environment",
                            "Apply cold compress to the forehead",
                            "Maintain hydration with small, frequent sips of water",
                            "Over-the-counter analgesics may provide relief",
                            "Consult a physician if this is the first occurrence"
                    ),
                    false
            );
        }

        if (tension || (!severe && !sudden)) {
            return new HealthAssessment(
                    SAFE,
                    "Tension Headache",
                    "Presentation is consistent with tension-type headache, often associated with stress or muscle tension.",
                    Arrays.asList(
                            "Practice relaxation techniques or meditation",
                            "Apply warm compress to neck and shoulder muscles",
                            "Take regular breaks from screen-based activities",
                            "Perform gentle neck stretching exercises",
                            "Consider over-the-counter pain relief if necessary"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                MODERATE,
                "Headache",
                "Headache presentation is non-specific. Monitoring and conservative management are recommended.",
                Arrays.asList(
                        "Rest in a quiet environment",
                        "Ensure adequate hydration",
                        "Limit caffeine and alcohol consumption",
                        "Consult a physician if symptoms persist beyond 48 hours"
                ),
                false
        );
    }

    private HealthAssessment assessFever(String input, List<String> symptoms) {
        boolean high = input.contains("high") || input.contains("103") || input.contains("104") ||
                input.contains("very hot") || input.contains("burning");
        boolean rash = input.contains("rash") || input.contains("spots");
        boolean confusion = input.contains("confused") || input.contains("confusion") || input.contains("delirious");
        boolean days = input.contains("3 days") || input.contains("4 days") || input.contains("week");

        if (high && (rash || confusion)) {
            return new HealthAssessment(
                    DANGER,
                    "High Fever with Complications",
                    "High-grade fever with associated rash or altered mental status requires urgent evaluation.",
                    Arrays.asList(
                            "Proceed to emergency department immediately",
                            "Document any medications taken prior to onset",
                            "Bring temperature records if available",
                            "Do not delay seeking care"
                    ),
                    true
            );
        }

        if (days) {
            return new HealthAssessment(
                    MODERATE,
                    "Persistent Fever",
                    "Fever persisting for several days may indicate an underlying bacterial infection or other condition requiring evaluation.",
                    Arrays.asList(
                            "Schedule a medical appointment promptly",
                            "Maintain hydration with electrolyte-containing fluids",
                            "Monitor temperature at regular intervals",
                            "Document any associated symptoms",
                            "Rest and avoid strenuous activities"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                SAFE,
                "Common Fever",
                "Presentation is consistent with a viral illness. Self-limiting course is expected.",
                Arrays.asList(
                        "Ensure adequate rest and sleep",
                        "Maintain fluid intake with water, broth, or juice",
                        "Paracetamol may be used for comfort if needed",
                        "Wear lightweight clothing",
                        "Anticipate resolution within 2-3 days"
                ),
                false
        );
    }

    private HealthAssessment assessCold(String input, List<String> symptoms) {
        boolean severe = input.contains("severe") || input.contains("can't breathe") || input.contains("wheezing");
        boolean days = input.contains("week") || input.contains("10 days") || input.contains("2 weeks");

        if (severe) {
            return new HealthAssessment(
                    MODERATE,
                    "Severe Respiratory Symptoms",
                    "Respiratory difficulty suggests a condition beyond simple upper respiratory infection.",
                    Arrays.asList(
                            "Seek medical evaluation within 24 hours",
                            "Use humidification or steam inhalation",
                            "Sleep with head elevated",
                            "Monitor respiratory status closely",
                            "Seek emergency care if breathing deteriorates"
                    ),
                    false
            );
        }

        if (days) {
            return new HealthAssessment(
                    MODERATE,
                    "Persistent Respiratory Symptoms",
                    "Symptoms extending beyond the typical duration for a common cold may indicate sinusitis or allergic rhinitis.",
                    Arrays.asList(
                            "Schedule a medical consultation",
                            "Consider saline nasal irrigation",
                            "Evaluate for potential allergic triggers",
                            "Allow adequate rest and recovery time"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                SAFE,
                "Common Cold",
                "Presentation is consistent with viral upper respiratory infection. Self-limiting course expected.",
                Arrays.asList(
                        "Rest and maintain warmth",
                        "Consume warm fluids such as tea or soup",
                        "Use saline gargles for throat discomfort",
                        "Over-the-counter cold preparations may provide symptomatic relief",
                        "Anticipate improvement within 5-7 days"
                ),
                false
        );
    }

    private HealthAssessment assessFatigue(String input, List<String> symptoms) {
        boolean severe = input.contains("severe") || input.contains("extreme") || input.contains("can't get up");
        boolean weeks = input.contains("weeks") || input.contains("months") || input.contains("long time");

        if (severe && weeks) {
            return new HealthAssessment(
                    MODERATE,
                    "Chronic Fatigue",
                    "Persistent severe fatigue warrants evaluation for underlying medical or psychological conditions.",
                    Arrays.asList(
                            "Schedule a comprehensive medical examination",
                            "Laboratory evaluation may be indicated",
                            "Review sleep quality and duration",
                            "Consider evaluation for mood disorders",
                            "Gradual reintroduction of physical activity may be beneficial"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                SAFE,
                "Temporary Fatigue",
                "Presentation is consistent with situational fatigue, commonly due to inadequate sleep or stress.",
                Arrays.asList(
                        "Prioritize 7-9 hours of sleep",
                        "Engage in light physical activity such as walking",
                        "Maintain regular, balanced meals",
                        "Ensure adequate hydration",
                        "Limit caffeine intake after mid-day"
                ),
                false
        );
    }

    private HealthAssessment assessNausea(String input, List<String> symptoms) {
        boolean blood = input.contains("blood") || input.contains("vomiting blood") || input.contains("coffee ground");
        boolean severe = input.contains("can't keep down") || input.contains("everything comes out");
        boolean pain = input.contains("stomach pain") || input.contains("abdominal pain") || input.contains("cramping");

        if (blood) {
            return new HealthAssessment(
                    DANGER,
                    "Gastrointestinal Bleeding",
                    "Vomiting of blood is a medical emergency requiring immediate intervention.",
                    Arrays.asList(
                            "Contact emergency services immediately",
                            "Do not consume food or liquids",
                            "Maintain upright or side-lying position",
                            "Bring all current medications to the hospital"
                    ),
                    true
            );
        }

        if (severe && pain) {
            return new HealthAssessment(
                    MODERATE,
                    "Severe Gastric Distress",
                    "Persistent vomiting with abdominal pain may indicate infection or obstruction.",
                    Arrays.asList(
                            "Seek medical evaluation promptly",
                            "Take small sips of oral rehydration solution",
                            "Avoid solid foods temporarily",
                            "Monitor for signs of dehydration"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                SAFE,
                "Mild Nausea",
                "Presentation is consistent with mild gastrointestinal upset, often self-limiting.",
                Arrays.asList(
                        "Sip ginger tea or clear fluids",
                        "Consume bland foods such as crackers or toast",
                        "Avoid fatty, spicy, or dairy products",
                        "Rest in a well-ventilated area",
                        "Anticipate resolution within 24-48 hours"
                ),
                false
        );
    }

    private HealthAssessment assessStomachPain(String input, List<String> symptoms) {
        boolean severe = input.contains("severe") || input.contains("intense") || input.contains("unbearable");
        boolean blood = input.contains("blood") || input.contains("black stool") || input.contains("vomiting blood");
        boolean fever = input.contains("fever") || symptoms.contains("Fever");

        if (blood) {
            return new HealthAssessment(
                    DANGER,
                    "Possible Internal Bleeding",
                    "Presence of blood in stool or vomit requires immediate medical evaluation.",
                    Arrays.asList(
                            "Proceed to emergency department immediately",
                            "Do not consume food or beverages",
                            "Document timing and appearance of bleeding",
                            "Bring all current medications"
                    ),
                    true
            );
        }

        if (severe && fever) {
            return new HealthAssessment(
                    MODERATE,
                    "Severe Abdominal Pain",
                    "Severe pain with fever may indicate infection or inflammatory process requiring evaluation.",
                    Arrays.asList(
                            "Seek medical evaluation within 6 hours",
                            "Avoid analgesics that may mask symptoms",
                            "Avoid solid food intake",
                            "Rest in a comfortable position"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                SAFE,
                "Upset Stomach",
                "Presentation is consistent with mild indigestion or gastrointestinal irritation.",
                Arrays.asList(
                        "Sip warm water or herbal tea",
                        "Consume bland, easily digestible foods",
                        "Avoid spicy, fatty, or dairy products",
                        "Apply warm compress to abdomen",
                        "Anticipate improvement within 24 hours"
                ),
                false
        );
    }

    private HealthAssessment assessBodyAche(String input, List<String> symptoms) {
        boolean fever = input.contains("fever") || symptoms.contains("Fever");
        boolean severe = input.contains("severe") || input.contains("can't move") || input.contains("excruciating");

        if (severe && fever) {
            return new HealthAssessment(
                    MODERATE,
                    "Body Pain with Fever",
                    "Widespread pain with fever is consistent with viral illness or influenza.",
                    Arrays.asList(
                            "Maintain bed rest",
                            "Paracetamol may be used for fever and discomfort",
                            "Ensure adequate fluid intake",
                            "Consult a physician if fever persists beyond 48 hours"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                SAFE,
                "Muscle Soreness",
                "Presentation is consistent with post-exertional or mild viral muscle soreness.",
                Arrays.asList(
                        "Engage in gentle stretching",
                        "Warm bath may provide relief",
                        "Maintain adequate hydration",
                        "Light massage of affected areas",
                        "Allow affected muscles to rest"
                ),
                false
        );
    }

    private HealthAssessment assessDizziness(String input, List<String> symptoms) {
        boolean fainting = input.contains("fainted") || input.contains("passed out") || input.contains("blackout");
        boolean chestPain = input.contains("chest") || input.contains("heart");
        boolean severe = input.contains("severe") || input.contains("spinning") || input.contains("can't stand");

        if (fainting || (severe && chestPain)) {
            return new HealthAssessment(
                    DANGER,
                    "Serious Dizziness",
                    "Syncope or severe dizziness with chest symptoms requires urgent evaluation.",
                    Arrays.asList(
                            "Assume recumbent position immediately",
                            "Contact emergency services",
                            "Avoid operating machinery or vehicles",
                            "Have someone remain with you"
                    ),
                    true
            );
        }

        return new HealthAssessment(
                SAFE,
                "Lightheadedness",
                "Presentation is consistent with benign causes such as dehydration or orthostatic hypotension.",
                Arrays.asList(
                        "Assume seated or supine position",
                        "Consume water or electrolyte-containing fluids",
                        "Have a light snack",
                        "Rise slowly from seated or lying positions",
                        "Rest until symptoms resolve"
                ),
                false
        );
    }

    private HealthAssessment assessSkinRash(String input, List<String> symptoms) {
        boolean breathing = input.contains("can't breathe") || input.contains("throat closing") || input.contains("swelling");
        boolean fever = input.contains("fever") || symptoms.contains("Fever");
        boolean spreading = input.contains("spreading fast") || input.contains("getting bigger");

        if (breathing) {
            return new HealthAssessment(
                    DANGER,
                    "Allergic Reaction",
                    "Rash with respiratory compromise suggests anaphylaxis, a life-threatening emergency.",
                    Arrays.asList(
                            "Contact emergency services immediately",
                            "Use epinephrine auto-injector if available",
                            "Assume supine position with legs elevated",
                            "This is a life-threatening condition"
                    ),
                    true
            );
        }

        if (fever && spreading) {
            return new HealthAssessment(
                    MODERATE,
                    "Rash with Systemic Symptoms",
                    "Spreading rash with fever may indicate infection or serious drug reaction.",
                    Arrays.asList(
                            "Seek medical evaluation within 24 hours",
                            "Document rash progression photographically",
                            "Avoid scratching; keep nails trimmed",
                            "Note any new medications or foods"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                SAFE,
                "Minor Skin Irritation",
                "Presentation is consistent with contact dermatitis, heat rash, or mild allergic reaction.",
                Arrays.asList(
                        "Apply cool compresses",
                        "Use mild emollient or calamine lotion",
                        "Avoid scratching the affected area",
                        "Wear loose, breathable clothing",
                        "Anticipate resolution within 2-3 days"
                ),
                false
        );
    }

    private HealthAssessment assessEyeDiscomfort(String input, List<String> symptoms) {
        boolean severePain = input.contains("severe pain") || input.contains("can't open eye");
        boolean vision = input.contains("blurry") || input.contains("can't see") || input.contains("vision loss");
        boolean chemical = input.contains("chemical") || input.contains("cleaner") || input.contains("acid");

        if (chemical) {
            return new HealthAssessment(
                    DANGER,
                    "Chemical Eye Exposure",
                    "Chemical exposure to the eye requires immediate irrigation and emergency care.",
                    Arrays.asList(
                            "Irrigate eye with clean water continuously for 15 minutes",
                            "Hold eyelids open during irrigation",
                            "Proceed to emergency department immediately",
                            "Bring the chemical container for identification"
                    ),
                    true
            );
        }

        if (severePain || vision) {
            return new HealthAssessment(
                    MODERATE,
                    "Serious Eye Condition",
                    "Eye pain or visual disturbance requires prompt ophthalmologic evaluation.",
                    Arrays.asList(
                            "Seek eye care professional today",
                            "Avoid rubbing the eye",
                            "Discontinue contact lens use",
                            "Use sunglasses if photophobic"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                SAFE,
                "Eye Strain",
                "Presentation is consistent with digital eye strain or mild ocular surface irritation.",
                Arrays.asList(
                        "Practice the 20-20-20 rule: every 20 minutes, look 20 feet away for 20 seconds",
                        "Use lubricating eye drops",
                        "Apply warm compress to closed eyelids",
                        "Reduce screen brightness and glare",
                        "Ensure adequate sleep"
                ),
                false
        );
    }

    private HealthAssessment assessToothache(String input, List<String> symptoms) {
        boolean swelling = input.contains("swollen") || input.contains("puffy") || input.contains("swelling");
        boolean fever = input.contains("fever") || symptoms.contains("Fever");
        boolean severe = input.contains("severe") || input.contains("excruciating") || input.contains("can't sleep");

        if (swelling && fever) {
            return new HealthAssessment(
                    MODERATE,
                    "Dental Infection",
                    "Swelling with fever suggests odontogenic infection requiring treatment.",
                    Arrays.asList(
                            "Seek dental evaluation within 24 hours",
                            "Rinse with warm saline solution",
                            "Paracetamol may be used for discomfort",
                            "Avoid applying heat to the swelling",
                            "This condition requires professional intervention"
                    ),
                    false
            );
        }

        return new HealthAssessment(
                SAFE,
                "Dental Sensitivity",
                "Presentation is consistent with dental caries, sensitivity, or gingival irritation.",
                Arrays.asList(
                        "Schedule dental appointment within 2-3 days",
                        "Rinse with warm saline three times daily",
                        "Avoid extreme temperatures and sugary foods",
                        "Use soft-bristled toothbrush with gentle technique",
                        "Over-the-counter analgesics may be used if needed"
                ),
                false
        );
    }

    public static class HealthAssessment {
        public String riskLevel;
        public String condition;
        public String explanation;
        public List<String> actionSteps;
        public boolean isEmergency;

        public HealthAssessment(String risk, String condition, String explanation,
                                List<String> actions, boolean emergency) {
            this.riskLevel = risk;
            this.condition = condition;
            this.explanation = explanation;
            this.actionSteps = actions;
            this.isEmergency = emergency;
        }
    }
}