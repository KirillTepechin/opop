package opopproto.docChecker;

import lombok.Data;

import java.util.List;

@Data
public class ComplianceState implements ErrorState {
    private List<String> characteristicErrors;
    private List<String> fosErrors;
    @Override
    public boolean isValid() {
        return true;
    }
    public boolean isOk(){return characteristicErrors.isEmpty();}
}
