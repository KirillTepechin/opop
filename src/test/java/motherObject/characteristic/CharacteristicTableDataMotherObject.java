package motherObject.characteristic;

import opopproto.data.characteristic.CharacteristicTableData;
import opopproto.domain.Competence;
import opopproto.domain.Discipline;
import opopproto.domain.Standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacteristicTableDataMotherObject {
    private String poopContainsSpecialty = "DEFAULT_POOP_SPECIALTY";
    private String fgosContainsSpecialty = "DEFAULT_FGOS_SPECIALTY";

    private List<String> taskTypes = new ArrayList<>();
    private List<Competence> uCompetences = new ArrayList<>();
    private List<Competence> opCompetences = new ArrayList<>();
    private List<Competence> pCompetences = new ArrayList<>();
    private Map<String,List<Competence>> pCompetencesTaskTypes = new HashMap<>();
    private Map<String,List<Standard>> taskTypesStandards = new HashMap<>();
    private List<Competence> matrixCompetenceDisciplines = new ArrayList<>();

    private List<Discipline> baseDisciplines = new ArrayList<>();
    private List<Discipline> varyDisciplines = new ArrayList<>();
    private List<Discipline> facDisciplines = new ArrayList<>();

    private List<Standard> appendixAData = new ArrayList<>();
    private List<Standard> appendixBData = new ArrayList<>();


    public CharacteristicTableDataMotherObject withUCompetence(Competence uCompetence) {
        this.uCompetences.add(uCompetence);
        return this;
    }

    public CharacteristicTableDataMotherObject withPCompetence(Competence pCompetence) {
        this.pCompetences.add(pCompetence);
        return this;
    }


    public CharacteristicTableData build() {
        CharacteristicTableData characteristicTableData = new CharacteristicTableData();
        characteristicTableData.setPoopContainsSpecialty(poopContainsSpecialty);
        characteristicTableData.setFgosContainsSpecialty(fgosContainsSpecialty);
        characteristicTableData.setTaskTypes(taskTypes);
        characteristicTableData.setUCompetences(uCompetences);
        characteristicTableData.setPCompetences(pCompetences);
        // set other fields
        return characteristicTableData;
    }
}
