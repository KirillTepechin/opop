package opopproto.docChecker;

import opopproto.util.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.compress.utils.FileNameUtils.getExtension;

@Component
public class ValidationStateChecker {
    @Autowired
    private Documents documents;
    private final String DOCS_DEST = "src/main/resources/opopDocs";
    public ValidationState check(ZipInputStream zis) throws Exception {
        ValidationState validationState = new ValidationState();
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            saveEntry(entry, zis);
            String fileName = entry.getName();
            File extractedFile = new File(DOCS_DEST+ "/" + fileName);
            String dir = fileName.split("/")[0];

            if (fileName.startsWith(dir + "/Оценочные средства (ФОС)/") || fileName.startsWith(dir + "/Оценочные_средства_(ФОС)/")) {
                validationState.setFosPackageFound(true);
                //Проверка документов ФОС
                if(!entry.isDirectory()){
                    try {
                        fileName = fileName.split("/")[2];
                        String index = fileName.split(" ")[0];
                        if(validateIndex(index)){
                            if(fileName.endsWith("ФОС.docx")){
                                //Сохраняем в памяти документ
                                documents.getFos().put(fileName, extractedFile);
                            }
                            else{
                                validationState.getFosInWrongFormat().add(fileName);
                            }
                        }
                        else{
                            validationState.getFosInWrongFormat().add(fileName);
                        }
                    }
                    catch (IndexOutOfBoundsException exception){
                        validationState.getFosInWrongFormat().add(fileName);
                    }
                }

            } else if (fileName.startsWith(dir +  "/Рабочие программы дисциплин/") || fileName.startsWith(dir + "/Рабочие_программы_дисциплин/")) {
                validationState.setRpdPackageFound(true);
                //Проверка документов РПД
                if(!entry.isDirectory()){
                    try {
                        fileName = fileName.split("/")[2];
                        String index = fileName.split(" ")[0];
                        if(validateIndex(index) && getExtension(fileName).equals("docx")){
                            //Сохраняем в памяти документ
                            documents.getRpd().put(fileName, extractedFile);
                        }
                        else{
                            validationState.getRpdInWrongFormat().add(fileName);
                        }
                    }
                    catch (IndexOutOfBoundsException exception){
                        validationState.getRpdInWrongFormat().add(fileName);
                    }
                }
            } else if ((fileName.startsWith(dir + "/Характеристика ОПОП") || fileName.startsWith(dir + "/Характеристика_ОПОП"))) {
                validationState.setCharacteristicsFound(true);
                String extension = getExtension(fileName);
                validationState.setCharacteristicsInRightExtension(extension.equals("docx"));
                //Сохраняем в памяти документ
                documents.setCharacteristics(extractedFile);
            } else if (Pattern.matches(dir+"/\\d{2}\\.\\d{2}\\.\\d{2}.*", fileName) && !entry.isDirectory()) {
                validationState.setSyllabusFound(true);
                String extension = getExtension(fileName);
                validationState.setSyllabusInRightExtension(extension.equals("xls") || extension.equals("xlsx"));
                //Сохраняем в памяти документ
                documents.setSyllabus(extractedFile);
            }
            else if(!(dir+ "/").equals(fileName)){
                validationState.getUnknownDocuments().add(entry.getName());
            }
        }


        return validationState;
    }
    private void saveEntry(ZipEntry zipEntry, ZipInputStream zis) throws IOException {
        File destDir = new File(DOCS_DEST);
        File newFile = newFile(destDir, zipEntry);
        byte[] buffer = new byte[1024];

        if (zipEntry.isDirectory()) {
            if (!newFile.isDirectory() && !newFile.mkdirs()) {
                throw new IOException("Failed to create directory " + newFile);
            }
        } else {
            // fix for Windows-created archives
            File parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory " + parent);
            }

            // write file content
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
        }
    }
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }


    private boolean validateIndex(String index) {
        return index.matches("Б3\\.\\d{2}|" +
                "Б[123]\\.([ВО]\\.\\d{2}|В\\.ДВ\\.\\d{2}\\.\\d{2}|[ВО]\\.\\d{2}\\([ПУ]\\))|" +
                "ФТД\\.\\d{2}");
    }

}
