package opopproto.docChecker.rpd;

import opopproto.data.rpd.RpdData;
import opopproto.data.syllabus.SyllabusData;
import opopproto.domain.Discipline;
import opopproto.domain.VolumeSemester;
import opopproto.util.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RpdComplianceStateChecker {
    @Autowired
    private Documents documents;
    public List<String> check(List<RpdData> rpdDataList, SyllabusData syllabusData){
        List<String> errors = new ArrayList<>();

        String titleErrors = checkTitle(rpdDataList, syllabusData);
        String table1Errors = checkTable1(rpdDataList, syllabusData);
        String containsDiscipline = checkContainsDiscipline(rpdDataList, syllabusData);

        if(titleErrors!=null)
            errors.add(titleErrors);
        if(table1Errors!=null)
            errors.add(table1Errors);
        if(containsDiscipline!=null)
            errors.add(containsDiscipline);

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
