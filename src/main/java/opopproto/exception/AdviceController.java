package opopproto.exception;

import opopproto.util.Documents;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@ControllerAdvice(annotations = RestController.class)
public class AdviceController {
    @Autowired
    private Documents documents;
    @ExceptionHandler(InvalidSyllabusException.class)
    public ResponseEntity<Object> handleSyllabusException(Throwable e) throws IOException {
        FileUtils.cleanDirectory(new File(documents.getDOCS_GEN_PATH()));
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknownException(Throwable e) throws IOException {
        FileUtils.cleanDirectory(new File(documents.getTMP_PATH()));
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
