package opopproto.service;

import opopproto.docChecker.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;

@Service
public class UploadService {

    @Autowired
    private ValidationStateChecker validationStateChecker;
    @Autowired
    private ComplianceStateChecker complianceStateChecker;
    public ErrorState upload(MultipartFile file) throws Exception {
        // Создание временного файла для сохранения загруженного архива
        File tempFile = File.createTempFile("temp", ".zip");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        ValidationState validationState;
        // Распаковка архива
        Charset CP866 = Charset.forName("CP866");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempFile), CP866)) {
            validationState = validationStateChecker.check(zis);
        }
        ComplianceState complianceState;
        if(validationState.isValid()){
            complianceState = complianceStateChecker.check();
            // Удаление временного файла
            tempFile.delete();
            return complianceState;
        }
        else {
            // Удаление временного файла
            tempFile.delete();
            return validationState;
        }
    }
}
