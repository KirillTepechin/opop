package opopproto.service;

import opopproto.data.syllabus.SyllabusData;
import opopproto.docChecker.*;
import opopproto.docGenerator.DocGenerateProvider;
import opopproto.model.GenerateResult;
import opopproto.model.InspectionResult;
import opopproto.repository.GenerateResultRepository;
import opopproto.repository.HeadRepository;
import opopproto.repository.InspectionResultRepository;
import opopproto.util.Documents;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class OpopService {

    @Autowired
    private ValidationStateChecker validationStateChecker;
    @Autowired
    private ComplianceStateChecker complianceStateChecker;
    @Autowired
    private Documents documents;
    @Autowired
    private DocGenerateProvider docGenerateProvider;
    @Autowired
    private HeadRepository headRepository;
    @Autowired
    private GenerateResultRepository generateResultRepository;
    @Autowired
    private InspectionResultRepository inspectionResultRepository;

    @Autowired
    private SyllabusData syllabusData;
    @Transactional
    public ErrorState upload(MultipartFile file) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Создание временного файла для сохранения загруженного архива
        File zipFile = new File(documents.getTMP_PATH()+ "/"+ file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(zipFile)) {
            fos.write(file.getBytes());
        }
        ValidationState validationState;
        // Распаковка архива
        Charset CP866 = Charset.forName("CP866");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile), CP866)) {
            validationState = validationStateChecker.check(zis);
        }
        ComplianceState complianceState;
        if(validationState.isValid()){
            complianceState = complianceStateChecker.check();
            saveInspection(username, zipFile, complianceState);
            // Удаление временного файла
            zipFile.delete();
            FileUtils.cleanDirectory(new File(documents.getDOCS_PATH()));
            return complianceState;
        }
        else {            // Удаление временного файла
            saveInspection(username, zipFile, validationState);
            zipFile.delete();
            FileUtils.cleanDirectory(new File(documents.getDOCS_PATH()));
            return validationState;
        }
    }

    @Transactional
    public byte[] generate(MultipartFile file, Boolean charCheckbox, Boolean rpdCheckbox, Boolean fosCheckbox) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        docGenerateProvider.generate(file, charCheckbox, rpdCheckbox, fosCheckbox);

        String fileName = saveGenerateAndReturnFilename(username);
        try(InputStream in = new FileInputStream(documents.getZIP_GEN_PATH()+ "/" + username + "/" + fileName + ".zip");) {
            return IOUtils.toByteArray(in);
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
    private String saveGenerateAndReturnFilename(String username) throws IOException {
        File userDir = new File(documents.getZIP_GEN_PATH() + "/" + username);
        if(!userDir.exists()){
            FileUtils.forceMkdir(userDir);
        }
        UUID uuid = UUID.randomUUID();
        FileOutputStream fos = new FileOutputStream(documents.getZIP_GEN_PATH()+"/" + username + "/" + uuid + ".zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        File fileToZip = new File(documents.getDOCS_GEN_PATH());
        zipFile(fileToZip, "ОПОП", zipOut);
        zipOut.close();
        fos.close();

        FileUtils.cleanDirectory(new File(documents.getDOCS_GEN_PATH()));

        GenerateResult generateResult = GenerateResult.builder()
                .specialty(syllabusData.getSyllabusTitle().getSpecialty())
                .profile(syllabusData.getSyllabusTitle().getProfile())
                .qualification(syllabusData.getSyllabusTitle().getQualification())
                .forms(syllabusData.getSyllabusTitle().getEducationForm())
                .head(headRepository.findByLogin(username))
                .startYear(syllabusData.getSyllabusTitle().getStartYear())
                .docUuid(uuid.toString())
                .build();
        generateResultRepository.save(generateResult);

        return uuid.toString();
    }

    private void saveInspection(String username, File zipFile, ValidationState validationState) throws IOException {
        File userDir = new File(documents.getZIP_INS_PATH() + "/" + username);
        if(!userDir.exists()){
            FileUtils.forceMkdir(userDir);
        }

        FileUtils.moveFileToDirectory(zipFile, userDir, false);
        String filename = zipFile.getName();
        UUID docUuid = UUID.randomUUID();
        new File(userDir.getPath()+"/"+filename)
                .renameTo(new File(userDir.getPath() + "/" + docUuid + ".zip"));

        InspectionResult inspectionResult = InspectionResult.builder()
                .fosPackageFound(validationState.isFosPackageFound())
                .rpdPackageFound(validationState.isRpdPackageFound())
                .characteristicsFound(validationState.isCharacteristicsFound())
                .syllabusFound(validationState.isSyllabusFound())
                .characteristicsInRightExtension(validationState.isCharacteristicsInRightExtension())
                .syllabusInRightExtension(validationState.isSyllabusInRightExtension())
                .fosInWrongFormat(validationState.getFosInWrongFormat())
                .rpdInWrongFormat(validationState.getRpdInWrongFormat())
                .unknownDocuments(validationState.getUnknownDocuments())
                .isValid(false)
                .head(headRepository.findByLogin(username))
                .docUuid(docUuid.toString())
                .filename(filename)
                .build();

        inspectionResultRepository.save(inspectionResult);
    }

    private void saveInspection(String username, File zipFile, ComplianceState complianceState) throws IOException {
        File userDir = new File(documents.getZIP_INS_PATH() + "/" + username);
        if(!userDir.exists()){
            FileUtils.forceMkdir(userDir);
        }

        FileUtils.moveFileToDirectory(zipFile, userDir, false);
        String filename = zipFile.getName();
        UUID docUuid = UUID.randomUUID();
        new File(userDir.getPath()+"/"+filename)
                .renameTo(new File(userDir.getPath() + "/" + docUuid + ".zip"));

        InspectionResult inspectionResult = InspectionResult.builder()
                .characteristicErrors(complianceState.getCharacteristicErrors())
                .fosErrors(complianceState.getFosErrors())
                .rpdErrors(complianceState.getRpdErrors())
                .isValid(true)
                .isOk(complianceState.isOk())
                .head(headRepository.findByLogin(username))
                .docUuid(docUuid.toString())
                .filename(filename)
                .build();

        inspectionResultRepository.save(inspectionResult);
    }

}
