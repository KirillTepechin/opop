package opopproto.docChecker.rpd;

import opopproto.data.characteristic.CharacteristicData;
import opopproto.data.rpd.AppendixData;
import opopproto.data.rpd.EvaluateCompetences;
import opopproto.data.rpd.RpdData;
import opopproto.data.syllabus.SyllabusData;
import opopproto.domain.*;
import opopproto.util.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RpdComplianceStateChecker {
    @Autowired
    private Documents documents;
    public List<String> check(List<RpdData> rpdDataList, SyllabusData syllabusData,
                              CharacteristicData characteristicData){
        List<String> errors = new ArrayList<>();

        String titleErrors = checkTitle(rpdDataList, syllabusData);
        String table1Errors = checkTable1(rpdDataList, syllabusData);
        String containsDiscipline = checkContainsDiscipline(rpdDataList, syllabusData);
        String competencesErrors = checkCompetences(rpdDataList, syllabusData, characteristicData);
        String table3Errors = checkTable3(rpdDataList, syllabusData);
        String evaluatesErrors = checkEvaluates(rpdDataList, syllabusData, characteristicData);
        String appendixErrors = checkAppendix(rpdDataList, syllabusData);

        if(titleErrors!=null)
            errors.add(titleErrors);
        if(table1Errors!=null)
            errors.add(table1Errors);
        if(containsDiscipline!=null)
            errors.add(containsDiscipline);
        if(competencesErrors!=null)
            errors.add(competencesErrors);
        if(table3Errors!=null)
            errors.add(table3Errors);
        if(evaluatesErrors!=null)
            errors.add(evaluatesErrors);
        if(appendixErrors!=null)
            errors.add(appendixErrors);

        return errors;
    }
    private String checkTitle(List<RpdData> rpdDataList, SyllabusData syllabusData){
        List<String> titleErrors = new ArrayList<>();
        for (var rpdData: rpdDataList) {
            Discipline syllabusRpdDiscipline = syllabusData.getDisciplinesData().getAllDisciplines().stream()
                    .filter(discipline -> (discipline.getIndex() + " " + discipline.getName())
                            .equals(rpdData.getRpdName())).toList().get(0);

            String titleDisciplineNameIndexSyllabus = syllabusRpdDiscipline.getIndex().trim()+ " " + syllabusRpdDiscipline.getName().trim();
            String titleDisciplineNameSyllabus = syllabusRpdDiscipline.getName().trim();

            try {
                if(rpdData.isPractice()){
                    String correctTitle;
                    if(rpdData.isEduPractice()){
                        correctTitle = "Учебная практика: " + titleDisciplineNameSyllabus;
                    }
                    else {
                        correctTitle = "Производственная практика: " + titleDisciplineNameSyllabus;
                    }
                    if(!correctTitle.equalsIgnoreCase(rpdData.getRpdTitle().getTitleEqualsDiscipline())){
                        titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное наименование практики");
                    }
                }
                else{
                    String titleDisciplineNameRpd = rpdData.getRpdTitle().getTitleEqualsDiscipline().trim();
                    if(!titleDisciplineNameSyllabus.equals(titleDisciplineNameRpd)
                            && !titleDisciplineNameIndexSyllabus.equals(titleDisciplineNameRpd)){
                        titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное название дисциплины");
                    }
                }
            }
            catch (NullPointerException e){
                titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное название дисциплины");
            }
            String titleLevelSyllabus = getLevelByQualification(syllabusData.getSyllabusTitle().getQualification().trim());

            try {
                String titleLevelRpd = rpdData.getRpdTitle().getTitleEqualsLevel().trim();
                if(!titleLevelRpd.toLowerCase().contains(titleLevelSyllabus.toLowerCase())){
                    titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректный уровень образования, корректный уровень - " + titleLevelSyllabus);
                }
            }
            catch (NullPointerException e){
                titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректный уровень образования, корректный уровень - " + titleLevelSyllabus);
            }

            String titleQualificationSyllabus = syllabusData.getSyllabusTitle().getQualification().trim();
            try {
                String titleQualificationRpd = rpdData.getRpdTitle().getTitleEqualsQualification().trim();
                if(!titleQualificationSyllabus.equalsIgnoreCase(titleQualificationRpd)){
                    titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректный уровень квалификации, корректный уровень - " + titleQualificationSyllabus);
                }
            }
            catch (NullPointerException e){
                titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректный уровень квалификации, корректный уровень - " + titleQualificationSyllabus);
            }
            String titleSpecialitySyllabus = syllabusData.getSyllabusTitle().getSpecialty().trim();

            try {
                String titleSpecialityRpd = rpdData.getRpdTitle().getTitleEqualsSpeciality().trim();
                if(!titleSpecialitySyllabus.equalsIgnoreCase(titleSpecialityRpd)){
                    titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное направление подготовки, корректное направление подготовки - " + titleSpecialitySyllabus);
                }
            }
            catch (NullPointerException e){
                titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное направление подготовки, корректное направление подготовки - " + titleSpecialitySyllabus);
            }


            String titleProfileSyllabus = syllabusData.getSyllabusTitle().getProfile().trim();
            try {
                String titleProfileRpd = rpdData.getRpdTitle().getTitleEqualsProfile().trim();
                if(!titleProfileSyllabus.equalsIgnoreCase(titleProfileRpd)){
                    titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректный профиль подготовки, корректный профиль - " + titleProfileSyllabus);
                }
            }
            catch (NullPointerException e){
                titleErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректный профиль подготовки, корректный профиль - " + titleProfileSyllabus);
            }

        }
        if(!titleErrors.isEmpty()){
            return "<b>Ошибки в титульных листах РПД</b>.<br><br>"+ String.join("<br><br>",titleErrors);
        }

        return null;
    }
    private String checkTable1(List<RpdData> rpdDataList, SyllabusData syllabusData){
        List<String> tableErrors = new ArrayList<>();
        for (var rpdData: rpdDataList) {
            Discipline syllabusRpdDiscipline = syllabusData.getDisciplinesData().getAllDisciplines().stream()
                    .filter(discipline -> (discipline.getIndex() + " " + discipline.getName())
                            .equals(rpdData.getRpdName())).toList().get(0);
            List<VolumeSemester> volumesBySemesterSyllabus = syllabusRpdDiscipline.getVolumeData().getVolumesBySemester();
            List<VolumeSemester> volumesBySemestersRpd = rpdData.getVolumeSemesters();
            for (var volumeSemesterS :volumesBySemesterSyllabus) {
                VolumeSemester volumeSemesterR;
                try {
                    volumeSemesterR = volumesBySemestersRpd.stream()
                            .filter(volumeSemester -> volumeSemester.getSemester() == volumeSemesterS.getSemester())
                            .toList().get(0);
                }
                catch (IndexOutOfBoundsException e){
                    tableErrors.add("Не найден обьем дисциплины в "  + volumeSemesterS.getSemester() + " семестре");
                    continue;
                }
                if(volumeSemesterS.getContactWork()!=0){
                    if(volumeSemesterS.getContactWork() != volumeSemesterR.getContactWork()){
                        tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                                "' количество часов <i>контактной работы</i> в " + volumeSemesterS.getSemester() + " семестре несовпадает с планом. План - " +
                                volumeSemesterS.getContactWork() + " ч");
                    }
                }
                if(volumeSemesterS.getLectures()!=0){
                    if(volumeSemesterS.getLectures() != volumeSemesterR.getLectures()){
                        tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                                "' количество часов <i>лекционного типа</i> в " + volumeSemesterS.getSemester() + " семестре несовпадает с планом. План - " +
                                volumeSemesterS.getLectures() + " ч");
                    }
                }
                if(volumeSemesterS.getPw()!=0){
                    if(volumeSemesterS.getPw() != volumeSemesterR.getPw()){
                        tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                                "' количество часов <i>практического типа</i> в " + volumeSemesterS.getSemester() + " семестре несовпадает с планом. План - " +
                                volumeSemesterS.getPw() + " ч");
                    }
                }
                if(volumeSemesterS.getLw()!=0){
                    if(volumeSemesterS.getLw() != volumeSemesterR.getLw()){
                        tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                                "' количество часов <i>лабораторных занятий</i> в " + volumeSemesterS.getSemester() + " семестре несовпадает с планом. План - " +
                                volumeSemesterS.getLw() + " ч");
                    }
                }
                if(volumeSemesterS.getIw() != volumeSemesterR.getIw()){
                    tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                            "' количество часов <i>самостоятельной работы</i> в " + volumeSemesterS.getSemester() + " семестре несовпадает с планом. План - " +
                            volumeSemesterS.getIw() + " ч");
                }
                if(volumeSemesterS.getControl() != volumeSemesterR.getControl()){
                    tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                            "' количество часов <i>контроля</i> в " + volumeSemesterS.getSemester() + " семестре несовпадает с планом. План - " +
                            volumeSemesterS.getControl() + " ч");
                }
                if(volumeSemesterS.getTotal() != volumeSemesterR.getTotal()){
                    tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                            "' количество <i>итоговых</i> часов в " + volumeSemesterS.getSemester() + " семестре несовпадает с планом. План - " +
                            volumeSemesterS.getTotal() + " ч");
                }
                if(volumeSemesterS.getZeCount() != volumeSemesterR.getZeCount()){
                    tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                            "' количество <i>зачетных единиц</i>  в " + volumeSemesterS.getSemester() + " семестре несовпадает с планом. План - " +
                            volumeSemesterS.getZeCount() + " з.е");
                }
            }
        }
        if(!tableErrors.isEmpty()){
            return "<b>Ошибки в таблице обьема дисциплины (Таблица 1) РПД</b>.<br><br>"+ String.join("<br><br>",tableErrors);
        }

        return null;
    }
    private String checkContainsDiscipline(List<RpdData> rpdDataList, SyllabusData syllabusData){
        List<String> errors = new ArrayList<>();
        for (var rpdData:rpdDataList) {
            Discipline syllabusRpdDiscipline = syllabusData.getDisciplinesData().getAllDisciplines().stream()
                    .filter(discipline -> (discipline.getIndex() + " " + discipline.getName())
                            .equals(rpdData.getRpdName())).toList().get(0);
            if(rpdData.isPractice()){
                String correctTitle;
                if(rpdData.isEduPractice()){
                    correctTitle = "Учебная практика: " + syllabusRpdDiscipline.getName();
                }
                else {
                    correctTitle = "Производственная практика: " + syllabusRpdDiscipline.getName();
                }
                try {
                    if(!rpdData.getSection3ContainsDiscipline1().toLowerCase().contains(correctTitle.toLowerCase())){
                        errors.add(rpdData.getRpdName());
                    }
                }
                catch (NullPointerException e){
                    errors.add(rpdData.getRpdName());
                }
            }
            else{
                try {
                    if(!rpdData.getSection3ContainsDiscipline1().contains(syllabusRpdDiscipline.getName())){
                        errors.add(rpdData.getRpdName());
                    }
                }
                catch (NullPointerException e){
                    errors.add(rpdData.getRpdName());
                }

            }

        }
        if(!errors.isEmpty()){
            return "<b>В разделе 3 не указано/неправильно указано название дисциплины/практики</b>.<br><br> Документы: <br>"+ String.join("<br><br>",errors);
        }
        return null;
    }
    private String checkCompetences(List<RpdData> rpdDataList, SyllabusData syllabusData,
                                            CharacteristicData characteristicData){
        List<String> competencesErrors = new ArrayList<>();

        for (var rpdData:rpdDataList) {
            List<Competence> syllabusDisciplineCompetences = syllabusData.getDisciplinesData().getAllDisciplines().stream()
                    .filter(discipline -> (discipline.getIndex() + " " + discipline.getName())
                            .equals(rpdData.getRpdName())).toList().get(0).getCompetences();

            if(rpdData.getCompetences() == null){
                competencesErrors.add("В РПД документе '" + rpdData.getRpdName() + "' отсутствуют компетенции");
                continue;
            }

            if(rpdData.getCompetences().size() > syllabusDisciplineCompetences.size()){
                competencesErrors.add("В РПД документе '" + rpdData.getRpdName() + "' количество компетенций больше чем в плане");
            }
            if(rpdData.getCompetences().size() < syllabusDisciplineCompetences.size()){
                competencesErrors.add("В РПД документе '" + rpdData.getRpdName() + "' количество компетенций меньше чем в плане");
            }

            for (var syllabusDisComp: syllabusDisciplineCompetences) {
                Competence rpdDisComp;
                try {
                    rpdDisComp = rpdData.getCompetences().stream()
                            .filter(competence -> competence.getIndex().equals(syllabusDisComp.getIndex())).toList().get(0);
                }
                catch (IndexOutOfBoundsException e){
                    competencesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                            "' не найдена компетенция " + syllabusDisComp.getIndex());
                    continue;
                }
                if(!syllabusDisComp.equals(rpdDisComp)){
                    competencesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                            "' именование компетенции с кодом " + syllabusDisComp.getIndex() +
                            " не совпадает с планом");
                }
                Competence characteristicDisComp;
                try {
                    List<Competence> characteristicCompetences = new ArrayList<>();
                    characteristicCompetences.addAll(characteristicData.getTableData().getUCompetences());
                    characteristicCompetences.addAll(characteristicData.getTableData().getPCompetences());
                    characteristicCompetences.addAll(characteristicData.getTableData().getOpCompetences());

                    characteristicDisComp = characteristicCompetences.stream()
                            .filter(competence -> competence.getIndex().equals(rpdDisComp.getIndex())).toList().get(0);
                }
                catch (IndexOutOfBoundsException e){
                    continue;
                }

                if(rpdDisComp.getIds().size() > characteristicDisComp.getIds().size()){
                    competencesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                            "' количество индикаторов компетенции "+ characteristicDisComp.getIndex() +
                            " больше чем в характеристике");
                }
                if(rpdData.getCompetences().size() < syllabusDisciplineCompetences.size()){
                    competencesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                            "' количество индикаторов компетенции "+ characteristicDisComp.getIndex() +
                            " меньше чем в характеристике");
                }

                for (var characteristicId: characteristicDisComp.getIds()) {
                    ID rpdId;
                    try {
                        rpdId = rpdDisComp.getIds().stream()
                                .filter(id -> id.getIndex().equals(characteristicId.getIndex())).toList().get(0);
                    }
                    catch (IndexOutOfBoundsException e){
                        competencesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                                "' не найден индикатор компетенции " + characteristicDisComp.getIndex() +
                                " " + characteristicId.getIndex());
                        continue;
                    }
                    if(!rpdId.getName().startsWith(characteristicId.getName())){
                        competencesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                                "' именование индикатора компетенции " + characteristicDisComp.getIndex() +
                                " " + characteristicId.getIndex() + " не совпадает с характеристикой");
                    }
                }
            }
        }


        if(!competencesErrors.isEmpty()){
            return "<b>Ошибки в таблице компетенций РПД</b>.<br><br>"+ String.join("<br><br>",competencesErrors);
        }

        return null;
    }
    private String checkTable3(List<RpdData> rpdDataList, SyllabusData syllabusData){
        List<String> tableErrors = new ArrayList<>();

        for (var rpdData: rpdDataList) {

            if(!rpdData.isPractice()){

                VolumeSemester rpdDataVolumeTotal = rpdData.getVolumeTotal();

                Discipline disciplineSyllabus = syllabusData.getDisciplinesData().getAllDisciplines().stream()
                        .filter(discipline -> (discipline.getIndex() + " " + discipline.getName())
                                .equals(rpdData.getRpdName())).toList().get(0);
                VolumeTotal syllabusVolumeTotal = disciplineSyllabus.getVolumeData().getOverallVolume();
                List<VolumeSemester> volumesBySemesterSyllabus = disciplineSyllabus.getVolumeData().getVolumesBySemester();
                int syllabusLectures = volumesBySemesterSyllabus.stream().mapToInt(VolumeSemester::getLectures).sum();
                int syllabusPw = volumesBySemesterSyllabus.stream().mapToInt(VolumeSemester::getPw).sum();
                int syllabusLw = volumesBySemesterSyllabus.stream().mapToInt(VolumeSemester::getLw).sum();
                //TODO: здесь пока считаем СР как контроль+СР
                int syllabusIw = volumesBySemesterSyllabus.stream().mapToInt(value -> value.getControl()+value.getIw()).sum();


                if(syllabusLectures!=rpdDataVolumeTotal.getLectures()){
                    tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                            "' количество часов <i>лекционного типа</i> несовпадает с планом. План - " +
                            syllabusLectures + " ч");
                }
                if(syllabusPw!=rpdDataVolumeTotal.getPw()){
                    tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                            "' количество часов <i>практического типа</i> несовпадает с планом. План - " +
                            syllabusPw + " ч");
                }
                if(syllabusLw!=rpdDataVolumeTotal.getLw()){
                    tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                            "' количество часов <i>лабораторных занятий</i> несовпадает с планом. План - " +
                            syllabusLw + " ч");
                }
                if(syllabusIw!=rpdDataVolumeTotal.getIw()){
                    tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                            "' количество часов <i>самостоятельной работы</i> несовпадает с планом. План - " +
                            syllabusIw + " ч (Считается как Контроль+СР)");
                }
                if(syllabusVolumeTotal.getTotal()!=rpdDataVolumeTotal.getTotal()){
                    tableErrors.add("В РПД документе '"+ rpdData.getRpdName() +
                            "' количество <i>итоговых</i> часов несовпадает с планом. План - " +
                            syllabusVolumeTotal.getTotal() + " ч");
                }
            }
        }

        if(!tableErrors.isEmpty()){
            return "<b>Ошибки в таблице содержания дисциплины (Таблица 3) РПД</b>.<br><br>"+ String.join("<br><br>",tableErrors);
        }

        return null;
    }
    private String checkEvaluates(List<RpdData> rpdDataList, SyllabusData syllabusData,
                                  CharacteristicData characteristicData){
        List<String> evaluatesErrors = new ArrayList<>();

        for (var rpdData:rpdDataList) {
            List<Competence> syllabusDisciplineCompetences = syllabusData.getDisciplinesData().getAllDisciplines().stream()
                    .filter(discipline -> (discipline.getIndex() + " " + discipline.getName())
                            .equals(rpdData.getRpdName())).toList().get(0).getCompetences();

            if(rpdData.getEvaluateCompetences() == null){
                evaluatesErrors.add("В РПД документе '" + rpdData.getRpdName() + "' отсутствуют оценочные средства");
                continue;
            }
            int rpdEvalCompSize = rpdData.getEvaluateCompetences().stream()
                    .collect(Collectors.groupingBy(EvaluateCompetences::getCompetenceIndex))
                    .size();
            if(rpdEvalCompSize > syllabusDisciplineCompetences.size()){
                evaluatesErrors.add("В РПД документе '" + rpdData.getRpdName() + "' количество компетенций больше чем в плане");
            }
            if(rpdEvalCompSize < syllabusDisciplineCompetences.size()){
                evaluatesErrors.add("В РПД документе '" + rpdData.getRpdName() + "' количество компетенций меньше чем в плане");
            }

            for (var syllabusDisComp: syllabusDisciplineCompetences) {
                var rpdDisCompetences = rpdData.getEvaluateCompetences().stream()
                            .filter(evaluateCompetences -> evaluateCompetences.getCompetenceIndex()
                                    .equalsIgnoreCase(syllabusDisComp.getIndex())).toList();

                if(rpdDisCompetences.isEmpty()){
                    evaluatesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                            "' не найдена компетенция " + syllabusDisComp.getIndex());
                    continue;
                }
                Competence characteristicDisComp;
                try {
                    List<Competence> characteristicCompetences = new ArrayList<>();
                    characteristicCompetences.addAll(characteristicData.getTableData().getUCompetences());
                    characteristicCompetences.addAll(characteristicData.getTableData().getPCompetences());
                    characteristicCompetences.addAll(characteristicData.getTableData().getOpCompetences());

                    characteristicDisComp = characteristicCompetences.stream()
                            .filter(competence -> competence.getIndex().equals(rpdDisCompetences.get(0).getCompetenceIndex())).toList().get(0);
                }
                catch (IndexOutOfBoundsException e){
                    continue;
                }
                if(rpdDisCompetences.size() > characteristicDisComp.getIds().size()){
                    evaluatesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                            "' количество индикаторов компетенции "+ characteristicDisComp.getIndex() +
                            " больше чем в характеристике");
                }
                if(rpdDisCompetences.size() < characteristicDisComp.getIds().size()){
                    evaluatesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                            "' количество индикаторов компетенции "+ characteristicDisComp.getIndex() +
                            " меньше чем в характеристике");
                }

                for (var characteristicId: characteristicDisComp.getIds()) {
                    try {
                        rpdDisCompetences.stream()
                                .filter(id -> id.getIdCompetence().startsWith(characteristicId.getIndex())).toList().get(0);
                    }
                    catch (IndexOutOfBoundsException e){
                        evaluatesErrors.add("В РПД документе '" + rpdData.getRpdName() +
                                "' не найден индикатор компетенции " + characteristicDisComp.getIndex() +
                                " " + characteristicId.getIndex());
                    }
                }
            }
        }

        if(!evaluatesErrors.isEmpty()){
            return "<b>Ошибки в таблице оценочных средств РПД</b>.<br><br>"+ String.join("<br><br>",evaluatesErrors);
        }

        return null;
    }
    private String checkAppendix(List<RpdData> rpdDataList, SyllabusData syllabusData){
        List<String> appendixErrors = new ArrayList<>();

        for (var rpdData: rpdDataList) {
            AppendixData appendixData = rpdData.getAppendixData();
            if(appendixData==null){
                appendixErrors.add("В РПД документе '" + rpdData.getRpdName() + "' не найдено Приложение А");
                continue;
            }
            Discipline syllabusRpdDiscipline = syllabusData.getDisciplinesData().getAllDisciplines().stream()
                    .filter(discipline -> (discipline.getIndex() + " " + discipline.getName())
                            .equals(rpdData.getRpdName())).toList().get(0);

            if(rpdData.isPractice()){
                String correctTitle;
                if(rpdData.isEduPractice()){
                    correctTitle = "Учебная практика: " + syllabusRpdDiscipline.getName();
                }
                else {
                    correctTitle = "Производственная практика: " + syllabusRpdDiscipline.getName();
                }
                if(!correctTitle.equalsIgnoreCase(appendixData.getEqualsDiscipline())){
                    appendixErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное название дисциплины");
                }
            }
            else if(!syllabusRpdDiscipline.getName().equals(appendixData.getEqualsDiscipline())){
                appendixErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное название дисциплины");
            }
            if(!appendixData.getEqualsLevel().toLowerCase().contains(syllabusData.getSyllabusTitle().getQualification().toLowerCase())){
                appendixErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректный уровень образования");
            }
            if(!syllabusData.getSyllabusTitle().getQualification().equalsIgnoreCase(appendixData.getEqualsQualification())){
                appendixErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректную квалификацию");
            }
            if(!syllabusData.getSyllabusTitle().getSpecialty().equals(appendixData.getEqualsSpeciality())){
                appendixErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное направление подготовки");
            }
            if(!syllabusData.getSyllabusTitle().getProfile().equals(appendixData.getEqualsProfile())){
                appendixErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректный профиль направления");
            }
            if(syllabusRpdDiscipline.getVolumeData().getOverallVolume().getZeCount() != appendixData.getZeCount()){
                appendixErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное количество зачетных единиц");
            }
            if(syllabusRpdDiscipline.getVolumeData().getOverallVolume().getTotal() != appendixData.getTotalHours()){
                appendixErrors.add("РПД документ '" + rpdData.getRpdName() + "' содержит не корректное количество часов");
            }
            //Форма аттестации
            var syllabusCFs = syllabusRpdDiscipline.getVolumeData().getControlForm();

            if(syllabusCFs.size()>appendixData.getControlForms().size()){
                appendixErrors.add("В РПД документе '" + rpdData.getRpdName() +
                        "' количество форм промежуточной аттестации меньше чем в плане");
            }
            if(syllabusCFs.size()<appendixData.getControlForms().size()){
                appendixErrors.add("В РПД документе '" + rpdData.getRpdName() +
                        "' количество форм промежуточной аттестации больше чем в плане");
            }
            for (var syllabusCF:syllabusCFs) {
                if(!appendixData.getControlForms().stream().map(String::toLowerCase).toList().contains(syllabusCF)){
                    appendixErrors.add("РПД документ '" + rpdData.getRpdName() +
                            "' не содержит форму промежуточной аттестации <i>" + syllabusCF +"</i>");
                }
            }

            //Формируемые комп
            var syllabusComps = syllabusRpdDiscipline.getCompetences();

            if(syllabusComps.size()>appendixData.getCompetencesIndexes().size()){
                appendixErrors.add("В РПД документе '" + rpdData.getRpdName() +
                        "' количество формируемых компетенций больше чем в плане");
            }
            if(syllabusComps.size()<appendixData.getCompetencesIndexes().size()){
                appendixErrors.add("В РПД документе '" + rpdData.getRpdName() +
                        "' количество формируемых компетенций меньше чем в плане");
            }
            for (var syllabusComp:syllabusComps) {
                if(!appendixData.getCompetencesIndexes().stream().map(ci->ci.trim().toLowerCase()).toList()
                        .contains(syllabusComp.getIndex().trim().toLowerCase())){
                    appendixErrors.add("РПД документ '" + rpdData.getRpdName() +
                            "' не содержит формируемую компетенцию <i>" + syllabusComp.getIndex() +"</i>");
                }
            }
        }

        if(!appendixErrors.isEmpty()){
            return "<b>Ошибки в Приложении А РПД</b>.<br><br>"+ String.join("<br><br>",appendixErrors);
        }

        return null;
    }
    public void removeB3(Map<String, File> rpd){
        List<String> keysToRemove = new ArrayList<>();
        for (var f :rpd.entrySet()) {
            if(f.getKey().startsWith("Б3")){
                keysToRemove.add(f.getKey());
            }
        }
        keysToRemove.forEach(key->documents.getRpd().remove(key));
    }
    public String checkNaming(Map<String, File> rpd, List<Discipline> syllabusDisciplines){
        String errorMes="";
        ArrayList<String> errorsIndDeficit = new ArrayList<>();
        Map<String, File> clearRpd = new HashMap<>();
        for (var disc: syllabusDisciplines) {
            String name = disc.getIndex()+" "+disc.getName()+".docx";
            if(!rpd.containsKey(name)){
                errorsIndDeficit.add(disc.getIndex());
            }
            else{
                clearRpd.put(name, rpd.get(name));
            }
        }
        if(!errorsIndDeficit.isEmpty()){
            errorMes+="<b>В РПД пакете нехватает дисциплин</b> с индексами ("+String.join(", ", errorsIndDeficit)+")<br>";
            errorMes+="Проверьте индексы и именование дисциплин в пакете с РПД документами.<br>";
        }
        if(!rpd.isEmpty()){
            documents.setRpd(clearRpd);
//            errorMes+="В РПД пакете обнаружены лишние дисциплины с индексами ("+String.join(", ",
//                    errorsIndRedundant.stream().map(key->key.split(" ")[0]).toList())+")";
        }
        if(!errorMes.isBlank()){
            return errorMes;
        }
        return null;
    }

    private String getLevelByQualification(String qualification){
        if(qualification.equalsIgnoreCase("магистр")){
            return "магистратура";
        } else if (qualification.equalsIgnoreCase("бакалавр")) {
            return "бакалавриат";
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
