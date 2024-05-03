package opopproto.data.characteristic;

import lombok.Data;
import opopproto.domain.Discipline;
import opopproto.domain.Standard;
import opopproto.domain.Competence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CharacteristicTableData {

    private String poopContainsSpecialty;
    private String fgosContainsSpecialty;

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
}
