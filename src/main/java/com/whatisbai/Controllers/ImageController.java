package com.whatisbai.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;

import com.whatisbai.Classes.*;
import com.whatisbai.Entities.PlantsName;
import com.whatisbai.Entities.RecordImage;
import com.whatisbai.Entities.Records;
import com.whatisbai.Repositories.PlantsNameRepository;
import com.whatisbai.Repositories.RecordImageRepository;
import com.whatisbai.Repositories.RecordsRepository;


@Controller
public class ImageController {

    private String defaultImagePath = "C:/Users/Lnwza007X/OneDrive/Desktop/final_project/whatisbai/src/main/resources/static/imagetotrain/";

    @Autowired
    PlantsNameRepository plantsNameRepository;

    @Autowired
    RecordsRepository recordsRepository;

    @Autowired
    RecordImageRepository recordImageRepository;
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // Define the destination path where the file will be saved
            String filePath = "C:/Users/Lnwza007X/OneDrive/Desktop/final_project/whatisbai/webfilecollect/" + file.getOriginalFilename();
            
            // Create the destination file
            java.io.File destFile = new java.io.File(filePath);
            
            // Ensure the parent directories exist
            destFile.getParentFile().mkdirs();
            
            // Transfer the uploaded file content to the destination
            try (InputStream inputStream = file.getInputStream();
                OutputStream outputStream = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            // Call the Python service using the MultipartFile
            List<ModelOutput> modelOutput = callPythonService(file);  // Call with MultipartFile

            // Step 3: Handle empty model output
            if (modelOutput.isEmpty()) {
                List<PlantsName> plantsNameList = plantsNameRepository.findAllByPlantNameInModelNotNullOrEmpty();
                model.addAttribute("plantsNameList", plantsNameList);
                model.addAttribute("filePath", filePath);
                return "notfoundplantform";
            }

            for (ModelOutput m : modelOutput) {
                PlantsName newPlantsName = plantsNameRepository.findByPlantNameInModel(m.getClassName());
                m.setPlantsName(newPlantsName);
            }

            model.addAttribute("modelOutput", modelOutput);
            return "plantpredict";

        } catch (IOException e) {
            e.printStackTrace();
            return "errorform";
        }
    }



    public List<ModelOutput> callPythonService(MultipartFile file) {
        String url = "http://localhost:5000/classify";  // Flask service URL
        RestTemplate restTemplate = new RestTemplate();
        List<ModelOutput> modelOutput = new ArrayList<>();
    
        try {
            // Create HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    
            // Create the request body with the image
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();  // Ensure the original filename is preserved
                }
            });
    
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    
            // Send the POST request to the Python Flask service
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    
            // Parse the response
            if (response.getStatusCode() == HttpStatus.OK) {
                String output = response.getBody().trim();
                JSONArray jsonArray = new JSONArray(output);
    
                // Loop through the JSON array to construct the ModelOutput list
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String className = jsonObject.getString("class");
                    double confidence = jsonObject.getDouble("confidence");
                    ModelOutput newModelOutput = new ModelOutput(className, confidence);
                    modelOutput.add(newModelOutput);
                }
            } else {
                // Handle non-200 responses
                System.err.println("Error: Received response status " + response.getStatusCode() + 
                    " with body: " + response.getBody());
            }
        } catch (IOException e) {
            System.err.println("Error while reading the file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
        }
    
        return modelOutput;
    }
    

    @GetMapping("/plantsname/{id}")
    public String showPlantDetails(@PathVariable("id") Integer id, Model model) {
        PlantsName plant = plantsNameRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid plant ID: " + id));
        model.addAttribute("plant", plant);
        return "plantdesc";
    }

    @PostMapping("/submitwithname")
    public String handleThankYouFormSubmission(
            @RequestParam("filePath") String filePath,
            @RequestParam("plantNameSubmit") String plantNameSubmit,
            Model model) {

        PlantsName newPlantName = new PlantsName();
        if (StringUtils.hasText(plantNameSubmit)) {
            PlantsName plantsNameIfExist = plantsNameRepository.findByPlantName(plantNameSubmit);
            if (plantsNameIfExist == null) {
                newPlantName.setPlantName(plantNameSubmit);
                newPlantName.setPlantNameVerify(false);
                plantsNameRepository.save(newPlantName);
            } else {
                newPlantName = plantsNameIfExist;
            }
        } else {
            newPlantName = null;
        }
        Records newRecord = saveRecordToDatabase(newPlantName);
        String plantFolderPath = determinePlantFolderPath(newRecord.getPlantsName());

        if (!createFolderIfNotExists(plantFolderPath)) {
            model.addAttribute("errorMessage", "Failed to create folder: " + plantFolderPath);
            return "errorPage";
        }

        if (!moveFile(filePath, plantFolderPath)) {
            model.addAttribute("errorMessage", "Failed to move the file.");
            return "errorPage";
        }
        saveRecordImageToDatabase(newRecord, plantFolderPath, filePath);

        return "thankyouform";
    }

    @PostMapping("/plantsname/{id}")
    public String plantVerifyByClick(@PathVariable("id") Integer id, @RequestParam("filePath") String filePath, Model model) {
        PlantsName plantsName = plantsNameRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid plant ID: " + id));
        Records newRecord = saveRecordToDatabase(plantsName);
        String plantFolderPath = determinePlantFolderPath(newRecord.getPlantsName());

        if (!createFolderIfNotExists(plantFolderPath)) {
            model.addAttribute("errorMessage", "Failed to create folder: " + plantFolderPath);
            return "errorPage";
        }

        if (!moveFile(filePath, plantFolderPath)) {
            model.addAttribute("errorMessage", "Failed to move the file.");
            return "errorPage";
        }
        saveRecordImageToDatabase(newRecord, plantFolderPath, filePath);

        return "thankyouform";
    }

    private Records saveRecordToDatabase(PlantsName newPlantName) {
        Records newRecord = new Records();
        if (newPlantName != null) {
            newRecord.setPlantsName(newPlantName);
        }
        newRecord.setRecordVerify(false);
        newRecord.setRecordTimestamp(LocalDateTime.now());
        recordsRepository.save(newRecord);

        return newRecord;
    }

    private String determinePlantFolderPath(PlantsName plantName) {
        String plantFolderPath;
        if (!(plantName == null)) {
            if (plantName.getPlantNameInModel() == null) {
                plantFolderPath = defaultImagePath + plantName.getPlantName();
            } else {
                plantFolderPath = defaultImagePath + plantName.getPlantNameInModel();
            }
        } else {
            plantFolderPath = defaultImagePath + "nolabel";
        }
        return plantFolderPath;
    }

    private boolean createFolderIfNotExists(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return true;
    }

    private boolean moveFile(String sourceFilePath, String targetFolderPath) {
        Path sourcePath = Paths.get(sourceFilePath);
        Path targetPath = Paths.get(targetFolderPath, sourcePath.getFileName().toString());

        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved successfully to: " + targetPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveRecordImageToDatabase(Records record, String plantFolderPath, String filePath) {
        String relativePath = plantFolderPath.replace("E:/final_project/whatisbai/src/main/resources/static", "");
        RecordImage newRecordImage = new RecordImage();
        newRecordImage.setRecords(record);
        newRecordImage.setRecImagePath(relativePath + "/" +Paths.get(filePath).getFileName());
        recordImageRepository.save(newRecordImage);
    }

    @GetMapping("/uploadfordataset")
    public String uploadForDataset() {
        return "uploadfordataset";
    }

    @PostMapping("/uploadfordataset")
    public String saveDataset(@RequestParam("submitPlantName") String submitPlantName, 
            @RequestParam("files") MultipartFile[] files, 
            Model model) throws InterruptedException {
        PlantsName newPlantsName = new PlantsName();
        PlantsName plantsNameIfExist = plantsNameRepository.findByPlantName(submitPlantName);
        if (plantsNameIfExist == null) {
            newPlantsName.setPlantName(submitPlantName);
            newPlantsName.setPlantNameVerify(false);
            plantsNameRepository.save(newPlantsName);
        } else {
            newPlantsName = plantsNameIfExist;
        }
        Records newRecord = saveRecordToDatabase(newPlantsName);
        List<String> imagePathList = new ArrayList<>();
        for (MultipartFile file: files) {
            try {
                String filePath = "E:/final_project/whatisbai/tmpimg/" + file.getOriginalFilename();
                file.transferTo(new java.io.File(filePath));
                imagePathList.add(filePath);
            } catch (IOException e) {
                return "errorform";
            }
        }

        String plantFolderPath = determinePlantFolderPath(newRecord.getPlantsName());

        if (!createFolderIfNotExists(plantFolderPath)) {
            model.addAttribute("errorMessage", "Failed to create folder: " + plantFolderPath);
            return "errorform";
        }
        
        for (String path: imagePathList) {
            if (!moveFile(path, plantFolderPath)) {
                model.addAttribute("errorMessage", "Failed to move the file.");
                return "errorform";
            }
            saveRecordImageToDatabase(newRecord, plantFolderPath, path);
        }
        return "thankyouform";
    }
}
