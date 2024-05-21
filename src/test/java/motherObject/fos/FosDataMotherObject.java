package motherObject.fos;

import opopproto.data.fos.Evaluate;
import opopproto.data.fos.FosData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FosDataMotherObject {

    private String fosName = "DEFAULT_FOS_NAME";
    private String titleEqualsDiscipline = "DEFAULT_TITLE_EQUALS_DISCIPLINE";
    private Map<String, Boolean> appendixesExisting = new HashMap<>();
    private List<Evaluate> evaluates = new ArrayList<>();


    public FosDataMotherObject withFosName(String fosName) {
        this.fosName = fosName;
        return this;
    }

    public FosDataMotherObject withTitleEqualsDiscipline(String titleEqualsDiscipline) {
        this.titleEqualsDiscipline = titleEqualsDiscipline;
        return this;
    }

    public FosDataMotherObject withAppendixesExisting(Map<String, Boolean> appendixesExisting) {
        this.appendixesExisting = appendixesExisting;
        return this;
    }

    public FosDataMotherObject withEvaluates(List<Evaluate> evaluates) {
        this.evaluates = evaluates;
        return this;
    }

    public FosData build() {
        FosData fosData = new FosData();
        fosData.setFosName(fosName);
        fosData.setTitleEqualsDiscipline(titleEqualsDiscipline);
        fosData.setAppendixesExisting(appendixesExisting);
        fosData.setEvaluates(evaluates);
        return fosData;
    }
}
