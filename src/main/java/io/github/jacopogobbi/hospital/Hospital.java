package io.github.jacopogobbi.hospital;

import io.github.jacopogobbi.hospital.domain.Divinity;
import io.github.jacopogobbi.hospital.domain.Drug;
import io.github.jacopogobbi.hospital.domain.Patient;
import io.github.jacopogobbi.hospital.domain.State;
import io.github.jacopogobbi.hospital.utils.CLI;
import io.github.jacopogobbi.hospital.utils.Tuple;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Hospital {
    private final Divinity divinity;

    Hospital(Divinity divinity) {
        this.divinity = divinity;
    }

    void treatPatients(String[] args) {
        try {
            final Tuple<String, String> patAndDrugs = CLI.validate(args);
            // Retrieves patients
            final String[] patients = patAndDrugs._1.split(",");
            // Retrieves drugs (if present) and get their values into a supplier
            final String[] drugsStr = patAndDrugs._2 != null ? patAndDrugs._2.split(",") : new String[]{};
            final Supplier<Stream<Drug>> drugs = () -> Arrays.stream(drugsStr).map(Drug::valueOf);
            // Administer drugs to patients
            final List<Patient> treatedPatients = Arrays.stream(patients)
                    .map(state -> {
                        Patient p = new Patient(State.valueOf(state));
                        drugs.get().forEach(p::administer);
                        return p;
                    })
                    .collect(Collectors.toList());
            // Get the state of patients after treatment
            final State[] states = treatedPatients
                    .stream()
                    .map(Patient::getResultingState)
                    .toArray(State[]::new);
            if (divinity.appear()) {
                for (int i = 0; i < states.length; i++) {
                    if (states[i] == State.X) {
                        states[i] = State.H;
                        break;
                    }
                }
            }
            // Group patients states in a map holding their frequency, thanks to use a linked hashmap, the entry set
            // will be in order thus helping us print the elements out always in the same order
            Map<State, Integer> statesFreq = new LinkedHashMap<State, Integer>() {{
                put(State.F, 0);
                put(State.H, 0);
                put(State.D, 0);
                put(State.T, 0);
                put(State.X, 0);
            }};
            for (State s : states)
                statesFreq.put(s, statesFreq.get(s) + 1);
            // Creates a map from the string
            final String resultsString = statesFreq
                    .keySet()
                    .stream()
                    .map(state -> state.toString() + ":" + Long.toString(statesFreq.get(state)))
                    .collect(Collectors.joining(","));
            // Outputs the results
            System.out.println(resultsString);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}