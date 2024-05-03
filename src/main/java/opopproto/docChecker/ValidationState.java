package opopproto.docChecker;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class ValidationState implements ErrorState {

    boolean fosPackageFound = false;
    boolean rpdPackageFound = false;;
    boolean characteristicsFound = false;
    boolean characteristicsInRightExtension = true;
    boolean syllabusFound = false;
    boolean syllabusInRightExtension = true;

    List<String> fosInWrongFormat = new ArrayList<>();
    List<String> rpdInWrongFormat = new ArrayList<>();
    List<String> unknownDocuments = new ArrayList<>();

    public boolean isValid() {
        return fosPackageFound && rpdPackageFound &&
                characteristicsFound && characteristicsInRightExtension &&
                syllabusFound && syllabusInRightExtension &&
                fosInWrongFormat.isEmpty() && rpdInWrongFormat.isEmpty();
    }

}
