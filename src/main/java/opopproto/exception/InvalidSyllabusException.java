package opopproto.exception;

public class InvalidSyllabusException extends RuntimeException{
    public InvalidSyllabusException(){
        super("Содержание учебного плана невалидно");
    }
}
