package opopproto.data.syllabus;

import lombok.Data;
import opopproto.domain.Competence;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(value =
        org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST,
        proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class SyllabusData {
    private SyllabusTitle syllabusTitle;
    private DisciplinesData disciplinesData;
    private List<Competence> competences;

    public List<Competence> getUCompetences(){
        return competences.stream().filter(competence -> competence.getIndex().trim().startsWith("УК-")).toList();
    }

    public List<Competence> getOpCompetences(){
        return competences.stream().filter(competence -> competence.getIndex().trim().startsWith("ОПК-")).toList();
    }
    public List<Competence> getPCompetences(){
        return competences.stream().filter(competence -> competence.getIndex().trim()
                .matches("(УКи-\\d+|ОПКи-\\d+|ПК-\\d+)*")).toList();
    }
}
