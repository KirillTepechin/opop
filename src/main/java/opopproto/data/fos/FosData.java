package opopproto.data.fos;

import lombok.Data;

import java.util.*;

@Data
public class FosData {
    String fosName;
    String titleEqualsDiscipline;
    Map<String, Boolean> appendixesExisting = new HashMap<>();
    List<Evaluate> evaluates = new ArrayList<>();

    public void setTitleEqualsDiscipline(String titleEqualsDiscipline) {
        this.titleEqualsDiscipline = titleEqualsDiscipline
                .replaceAll("\"", "")
                .replaceAll("«","")
                .replace("»", "")
                .trim()
                .replaceAll("\\s+", " ");

        if(checkIndex(this.titleEqualsDiscipline.split(" ")[0])){
            this.titleEqualsDiscipline = titleEqualsDiscipline.substring(titleEqualsDiscipline.indexOf(" ") + 1);
        }
    }

    private boolean checkIndex(String index) {
        return index.matches("Б3\\.\\d{2}|" +
                "Б[123]\\.([ВО]\\.\\d{2}|В\\.ДВ\\.\\d{2}\\.\\d{2}|[ВО]\\.\\d{2}\\([ПУ]\\))|" +
                "ФТД\\.\\d{2}");
    }

}
