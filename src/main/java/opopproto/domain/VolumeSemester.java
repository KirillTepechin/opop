package opopproto.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VolumeSemester extends Volume{
    private int semester;
    private int lectures;
    private int lw;
    private int pw;

    private int contactWork;
    private int total;

    public int getContactWork(){
        if(contactWork!=0){
            return contactWork;
        }
        else{
            return lw+pw+lectures;
        }
    }
    public int getTotal(){
        if(total!=0){
            return total;
        }
        else{
            return getContactWork() + getControl() + getIw();
        }
    }
}
