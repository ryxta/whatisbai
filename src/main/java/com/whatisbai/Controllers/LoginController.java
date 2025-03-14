package com.whatisbai.Controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.whatisbai.Entities.Medicine;
import com.whatisbai.Entities.Plants;
import com.whatisbai.Entities.PlantsInMedicine;
import com.whatisbai.Entities.PlantsName;
import com.whatisbai.Entities.RecordImage;
import com.whatisbai.Entities.Records;
import com.whatisbai.Entities.Role;
import com.whatisbai.Entities.TrainingData;
import com.whatisbai.Entities.TrainingPlantsName;
import com.whatisbai.Entities.Users;
import com.whatisbai.Repositories.MedicineRepository;
import com.whatisbai.Repositories.PlantsInMedicineRepository;
import com.whatisbai.Repositories.PlantsNameRepository;
import com.whatisbai.Repositories.PlantsRepository;
import com.whatisbai.Repositories.RecordImageRepository;
import com.whatisbai.Repositories.RecordsRepository;
import com.whatisbai.Repositories.RoleRepository;
import com.whatisbai.Repositories.TrainingDataRepository;
import com.whatisbai.Repositories.TrainingPlantsNameRepository;
import com.whatisbai.Repositories.UsersRepository;

import jakarta.servlet.http.HttpSession;


@Controller
public class LoginController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RecordsRepository recordsRepository;

    @Autowired
    private RecordImageRepository recordImageRepository;

    @Autowired
    private PlantsNameRepository plantsNameRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PlantsInMedicineRepository plantsInMedicineRepository;

    @Autowired
    private PlantsRepository plantsRepository;

    @Autowired
    private TrainingDataRepository trainingDataRepository;

    @Autowired
    private TrainingPlantsNameRepository trainingPlantsNameRepository;

    @Autowired
    private RoleRepository roleRepository;

    private String uploadDirectory = "C:/Users/Lnwza007X/OneDrive/Desktop/final_project/whatisbai/src/main/resources/static";

    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/home";
        }
        return "login"; // Return the name of your login template (e.g., login.html)
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            Model model, HttpSession session) {
        Users user = usersRepository.findByUsername(username);
        //System.out.println(user.getUsername());
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login"; // Return the login template with an error message
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String homePage(HttpSession session, Model model){
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "home";
    }

    @GetMapping("/verifyrecord")
    public String getUnverifiedRecords(Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.getRole().getRoleName().equals("ADMIN") && !user.getRole().getRoleName().equals("EXPERTS")) {
            return "noaccess";
        }
        List<RecordImage> unVerifyRecordImage = recordImageRepository.getUnVerifyRecordImage(); // Fetch unverified records
        
        // Group by recordId
        Map<Integer, List<RecordImage>> groupedByRecordId = unVerifyRecordImage.stream()
            .collect(Collectors.groupingBy(img -> img.getRecords().getRecordId()));
        
        model.addAttribute("groupedByRecordId", groupedByRecordId);
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "verifyrecordform";
    }

    @PostMapping("/deleteRecordImage/{id}")
    public String deleteRecordImage(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        // Find the record by ID
        RecordImage recordImage = recordImageRepository.findById(id).orElse(null);
        Records currentRecords = new Records();

        if (recordImage != null) {
            currentRecords = recordImage.getRecords();
            
            try {
                recordImageRepository.delete(recordImage);
                deleteImage(recordImage);
                redirectAttributes.addFlashAttribute("message", "Image and record deleted successfully.");
            } catch (IOException e) {
                // Handle any errors during file deletion
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Error deleting image file.");
            }
        } else {
            // If the record doesn't exist, add an error message
            redirectAttributes.addFlashAttribute("message", "Record not found.");
        }

        if (currentRecords.getRecordImage().size() == 0) {
            recordsRepository.delete(currentRecords);
        }
        return "redirect:/verifyrecord"; // Redirect back to the verification page
    }

    private void deleteImage(RecordImage recordImage) throws IOException{
        String filePath = uploadDirectory + recordImage.getRecImagePath();
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @PostMapping("/verifyrecord/{id}")
    public String verifyRecordImage(@PathVariable("id") int id, RedirectAttributes redirectAttributes, HttpSession session) {
        Records records = recordsRepository.findById(id).orElse(null);
        if (records.getPlantsName() == null) {
            redirectAttributes.addFlashAttribute("message", "กรุณาเพิ่มชื่อพืชก่อน");
            return "redirect:/verifyrecord";
        }
        if (records.getPlantsName().getPlantNameInModel() == null) {
            redirectAttributes.addFlashAttribute("message", "กรุณาเพิ่มชื่อในโมเดลก่อน");
            return "redirect:/verifyrecord";
        }
        Users verifyBy = (Users) session.getAttribute("user");
        records.setRecordVerify(true);
        records.setUserId(verifyBy);
        recordsRepository.save(records);
        return "redirect:/verifyrecord";
    }

    @GetMapping("/editrecord/{id}")
    public String editRecord(@PathVariable("id") int id, Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.getRole().getRoleName().equals("ADMIN") && !user.getRole().getRoleName().equals("EXPERTS")) {
            return "noaccess";
        }
        Records records = recordsRepository.findById(id).orElse(null);
        model.addAttribute("records", records);
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "editrecord";
    }

    @PostMapping("/updaterecord")
    public String updateRecord(@ModelAttribute Records records) {
        String inputPlantName = records.getPlantsName().getPlantName();
        String inputPlantNameInModel = records.getPlantsName().getPlantNameInModel();
        PlantsName existPlantsName = plantsNameRepository.findByPlantName(inputPlantName);
        List<RecordImage> allRecordImages = recordImageRepository.getByRecords(records);
        if (existPlantsName == null) {
            existPlantsName = new PlantsName();
        }
        existPlantsName.setPlantName(inputPlantName);
        existPlantsName.setPlantNameInModel(inputPlantNameInModel);
        existPlantsName.setPlantNameVerify(true);
        plantsNameRepository.save(existPlantsName);
        records.setPlantsName(existPlantsName);
        recordsRepository.save(records);
        for (RecordImage recImg: allRecordImages) {
            String newPath = changePath(recImg.getRecImagePath(), existPlantsName.getPlantNameInModel());
            recImg.setRecImagePath(newPath);
            recordImageRepository.save(recImg);
        }
        return "delayredirect";
    }
    
    private String changePath(String imagePath, String newFolderName) {
        // Construct the old and new image paths
        String newFolderStringPath = uploadDirectory + "/imagetotrain/" + newFolderName;
        Path oldImagePath = Paths.get(uploadDirectory + imagePath);
        Path newFolderPath = Paths.get(newFolderStringPath);
        Path newImagePath = newFolderPath.resolve(oldImagePath.getFileName());

        try {
            // Print the paths for debugging
            System.out.println("Old Image Path: " + oldImagePath);
            System.out.println("New Folder Path: " + newFolderPath);
            System.out.println("New Image Path: " + newImagePath);

            // Check if the old file exists
            if (!Files.exists(oldImagePath)) {
                System.out.println("The file to move does not exist.");
                return null;
            }

            // Create the new directory if it doesn't exist
            if (!Files.exists(newFolderPath)) {
                Files.createDirectories(newFolderPath);
                System.out.println("Created new directory: " + newFolderPath);
            }

            // Move the file from old path to new path
            Files.move(oldImagePath, newImagePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved successfully.");

            // Get the old folder path
            Path oldFolderPath = oldImagePath.getParent();

            // Check if the old folder is empty
            if (isFolderEmpty(oldFolderPath)) {
                // Delete the old folder if it's empty
                Files.delete(oldFolderPath);
                System.out.println("Old folder deleted: " + oldFolderPath);
            }

            // Return the new image path as a string
            return newFolderStringPath.toString().replace(uploadDirectory, "") + "/" + newImagePath.getFileName();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
            return null;
        }
    }

    private boolean isFolderEmpty(Path folderPath) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(folderPath)) {
            return !dirStream.iterator().hasNext(); // True if the folder is empty
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    @GetMapping("/autocomplete/plants")
    @ResponseBody
    public List<Map<String, String>> getPlantsByPartialName(@RequestParam("term") String term) {
        List<PlantsName> plants = plantsNameRepository.findByPlantNameContaining(term);
        List<Map<String, String>> results = new ArrayList<>();

        for (PlantsName plant : plants) {
            Map<String, String> plantData = new HashMap<>();
            plantData.put("plantName", plant.getPlantName());
            plantData.put("plantNameInModel", plant.getPlantNameInModel());
            results.add(plantData);
        }

        return results; // Return a list of simplified maps
    }


    @GetMapping("/medicines/viewall")
    public String viewAllMedicine(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "viewallmedicine";
    }

    @GetMapping("/medicines/edit/{medId}")
    public String editMedicine(@PathVariable("medId") int medId, Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Medicine medicine = medicineRepository.findById(medId).orElse(null);
        model.addAttribute("medicine", medicine);
        List<PlantsInMedicine> plantsInMedicineList = plantsInMedicineRepository.findByMedicine(medicine);
        model.addAttribute("plantsInMedicine", plantsInMedicineList);
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "editmedicine";
    }

    @PostMapping("/medicines/delete/{medId}")
    public String deleteMedicine(@PathVariable("medId") int medId) {
        Medicine medicine = medicineRepository.findById(medId).orElse(null);
        List<PlantsInMedicine> allPlantsInMedicines = medicine.getPlantsInMedicine();
        for (PlantsInMedicine plantsInMedicine: allPlantsInMedicines) {
            plantsInMedicineRepository.delete(plantsInMedicine);
        }
        medicineRepository.delete(medicine);
        return "redirect:/medicines/viewall";
    }

    @GetMapping("/plants/name/viewall")
    public String viewAllPlantName(Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "viewallplantname";
    }
    
    @GetMapping("plants/name/search-plantsname")
    @ResponseBody
    public List<Map<String, Object>> searchPlantsName(@RequestParam("term") String searchTerm) {
        List<PlantsName> plantsName = plantsNameRepository.findByPlantNameContaining(searchTerm);

        // Return results
        return plantsName.stream()
            .map(plantName -> {
                Map<String, Object> plantsNameMap = new HashMap<>();
                plantsNameMap.put("label", plantName.getPlantName());
                plantsNameMap.put("value", plantName.getPlantNameId());
                return plantsNameMap;
            })
            .collect(Collectors.toList());
    }

    
    @GetMapping("plants/name/initial-plantsname")
    @ResponseBody
    public List<Map<String, Object>> getInitialPlantsName() {
        List<PlantsName> plantsName = plantsNameRepository.findFirst30ByOrderByPlantNameIdAsc();

        return plantsName.stream()
            .map(plantName -> {
                Map<String, Object> plantsNameMap = new HashMap<>();
                plantsNameMap.put("label", plantName.getPlantName());
                plantsNameMap.put("value", plantName.getPlantNameId());
                return plantsNameMap;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/plants/name/edit/{id}")
    public String editPlantsName(@PathVariable("id") int id, Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        PlantsName plantsName = plantsNameRepository.findById(id).orElse(null);
        model.addAttribute("plantsName", plantsName);
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "editplantsname";
    }
    
    @PostMapping("/plants/name/update")
    public String updatePlantsName(@ModelAttribute PlantsName plantsName,
                                    @RequestParam("plantsInQueue") String plantsInQueueData,
                                    @RequestParam("plantNameDesc") String plantNameDesc,
                                    @RequestParam("plantImage") MultipartFile plantImage,
                                    SessionStatus sessionStatus, RedirectAttributes redirectAttributes) {
        
        // Fetch existing PlantsName entity
        PlantsName existingPlantsName = plantsNameRepository.findById(plantsName.getPlantNameId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid plants name ID: " + plantsName.getPlantNameId()));

        // Check for duplicate plant name
        PlantsName checkExistName = plantsNameRepository.findByPlantName(plantsName.getPlantName());
        if (checkExistName != null && plantsName.getPlantNameId() != checkExistName.getPlantNameId()) {
            redirectAttributes.addFlashAttribute("message", "มีพืชชนิดนี้อยู่แล้ว");
            return "redirect:/plants/name/edit/" + plantsName.getPlantNameId();
        }

        // Handle plant image upload
        if (!plantImage.isEmpty()) {
            try {
                String imagePath = saveImage(plantImage);
                existingPlantsName.setPlantShowImagePath(imagePath); // Update existing image path
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("message", "ไม่สามารถบันทึกภาพได้");
                return "redirect:/plants/name/edit/" + plantsName.getPlantNameId();
            }
        }

        // Save updated PlantsName entity
        existingPlantsName.setPlantNameDesc(plantNameDesc);
        plantsNameRepository.save(existingPlantsName);

        // Fetch current Plants entries
        List<Plants> currentPlants = plantsRepository.findByPlantsName(existingPlantsName);
        Set<Integer> currentPlantIds = currentPlants.stream()
                .map(Plants::getPlantId)
                .collect(Collectors.toSet());

        // Parse the new plants entries from the input
        String[] plantEntries = plantsInQueueData.split(",");
        
        // Use a map for easier updates
        Map<Integer, Plants> plantsMap = currentPlants.stream()
                .collect(Collectors.toMap(Plants::getPlantId, plant -> plant));
        
        // Process new plants
        for (String entry : plantEntries) {
            String[] parts = entry.split(":");
            int plantId = Integer.parseInt(parts[0]);
            String plantUnit = parts[1];
            String plantLatinName = parts[2];
            String plantSciName = parts[3];

            if (plantsMap.containsKey(plantId)) {
                // Update existing plant entry
                Plants existingPlant = plantsMap.get(plantId);
                existingPlant.setPlantUnit(plantUnit);
                existingPlant.setPlantLatinName(plantLatinName);
                existingPlant.setPlantSciName(plantSciName);
                plantsRepository.save(existingPlant); // Update the existing record
            } else {
                // Create a new plant entry if it does not exist
                Plants newPlant = new Plants();
                newPlant.setPlantUnit(plantUnit);
                newPlant.setPlantLatinName(plantLatinName);
                newPlant.setPlantSciName(plantSciName);
                newPlant.setPlantsName(existingPlantsName);
                plantsRepository.save(newPlant);
            }
        }

        // Remove plants that are no longer in the queue
        for (Plants existingPlant : currentPlants) {
            if (!Arrays.stream(plantEntries).anyMatch(entry -> entry.startsWith(existingPlant.getPlantId() + ":"))) {
                plantsRepository.delete(existingPlant);
            }
        }

        sessionStatus.setComplete();
        return "redirect:/plants/name/viewall";
    }


    @PostMapping("/plants/name/delete/{id}")
    public String deletePlantsName(@PathVariable("id") int id) {
        PlantsName plantsName = plantsNameRepository.findById(id).orElse(null);
        List<Records> allRecords = plantsName.getRecords();
        for (Records rec: allRecords) {
            rec.setPlantsName(null);
            recordsRepository.save(rec);
        }
        plantsRepository.deleteAll(plantsName.getPlants());
        plantsNameRepository.delete(plantsName);
        return "redirect:/plants/name/viewall";
    }

    @GetMapping("/plants/name/new")
    public String newPlantName(Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        PlantsName newPlantsName = new PlantsName();
        model.addAttribute("plantsName", newPlantsName);
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "newplantsname";
    }

    @PostMapping("/plants/name/save")
    public String saveNewPlantsName(@ModelAttribute PlantsName plantsName,
                                    @RequestParam("plantsInQueue") String plantsInQueueData,
                                    @RequestParam("plantImage") MultipartFile plantImage,
                                    SessionStatus sessionStatus, RedirectAttributes redirectAttributes) {

        if (plantsNameRepository.findByPlantName(plantsName.getPlantName()) != null) {
            redirectAttributes.addFlashAttribute("message", "มีพืชชนิดนี้อยู่แล้ว");
            return "redirect:/plants/name/new";
        }

        // Save the image
        if (!plantImage.isEmpty()) {
            try {
                String imagePath = saveImage(plantImage);
                plantsName.setPlantShowImagePath(imagePath); // Assuming your PlantsName has an imagePath field
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("message", "ไม่สามารถบันทึกภาพได้");
                return "redirect:/plants/name/new";
            }
        }

        String[] plantEntries = plantsInQueueData.split(",");
        List<Plants> newPlants = new ArrayList<>();

        plantsNameRepository.save(plantsName);

        if (plantEntries.length > 0) {
            for (String entry : plantEntries) {
                String[] parts = entry.split(":");
                String plantUnit = parts[0];
                String plantLatinName = parts[1];
                String plantSciName = parts[2];
    
                Plants inPlant = new Plants();
                inPlant.setPlantUnit(plantUnit);
                inPlant.setPlantLatinName(plantLatinName);
                inPlant.setPlantSciName(plantSciName);
                inPlant.setPlantsName(plantsName);
                newPlants.add(inPlant);
            }
        }
        

        plantsRepository.saveAll(newPlants);
        return "redirect:/plants/name/viewall";
    }

    // Method to save the image
    private String saveImage(MultipartFile image) throws IOException {
        // Define the directory to save the images
        String uploadDir = uploadDirectory + "/displayplantimage/";
        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        File destinationFile = new File(uploadDir, fileName);
        image.transferTo(destinationFile);
        return "/displayplantimage/" + fileName; // Update this to the public path where images can be accessed
    }

    
    @GetMapping("/exportdataset")
    public String exportDataset(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        Users user = (Users) session.getAttribute("user");
        if (!user.getRole().getRoleName().equals("ADMIN") && !user.getRole().getRoleName().equals("DEVELOPER")) {
            return "noaccess";
        }
        List<Records> verifiedRecords = recordsRepository.getVerifyRecords();
        List<PlantsName> verifiedPlantsName = new ArrayList<>();
        Map<Integer, List<RecordImage>> plantImageMap = new HashMap<>(); // Use plantNameId as the key
        
        for (Records rec: verifiedRecords) {
            PlantsName plant = rec.getPlantsName();
            if (plant.isPlantNameVerify() && !verifiedPlantsName.contains(plant)) {
                verifiedPlantsName.add(plant);
                List<RecordImage> images = recordImageRepository.getByPlantsNameId(plant.getPlantNameId());
                plantImageMap.put(plant.getPlantNameId(), images);  // Use plantNameId as key
            }
        }
        
        model.addAttribute("verifiedPlantsName", verifiedPlantsName);
        model.addAttribute("plantImageMap", plantImageMap);  // Add the Map to the model
        model.addAttribute("userRole", user.getRole().getRoleName());
        
        return "exportdataset";
    }

    @PostMapping("/exportdataset")
    public ResponseEntity<InputStreamResource> exportSelectedPlants(@RequestParam("selectedPlantIds") List<Integer> selectedPlantIds, Model model, HttpSession session) throws IOException {
        // Create a temporary directory to store the images to be zipped
        Path tempDir = Files.createTempDirectory("export_dataset");
        
        List<PlantsName> exportPlantsNames = new ArrayList<>();
        for (int plantId : selectedPlantIds) {
            PlantsName plantsName = plantsNameRepository.findById(plantId).orElse(null);
            if (plantsName != null) {
                // Create a folder named after the plant's name in model
                Path plantFolder = Files.createDirectory(tempDir.resolve(plantsName.getPlantNameInModel()));
                
                exportPlantsNames.add(plantsName);
                List<RecordImage> images = recordImageRepository.getByPlantsNameId(plantId);
                for (RecordImage image : images) {
                    Path sourcePath = Paths.get(uploadDirectory + image.getRecImagePath()); // Path to the original image
                    if (Files.exists(sourcePath)) {
                        // Copy the image to the plant folder
                        Files.copy(sourcePath, plantFolder.resolve(sourcePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

        // Create the zip file
        Path zipFilePath = tempDir.resolve("dataset.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            Files.walk(tempDir)
                .filter(path -> !Files.isDirectory(path)) // Only files
                .forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(tempDir.relativize(path).toString());
                    try {
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        }

        // Prepare the file for download
        File zipFile = zipFilePath.toFile();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));
        TrainingData newTrainingData = new TrainingData();
        Users user = (Users) session.getAttribute("user");
        newTrainingData.setExportTimestamp(LocalDateTime.now());
        newTrainingData.setUsers(user);
        trainingDataRepository.save(newTrainingData);
        for (PlantsName plantName: exportPlantsNames) {
            TrainingPlantsName newTrainingPlantsName = new TrainingPlantsName();
            newTrainingPlantsName.setPlantsName(plantName);
            newTrainingPlantsName.setTrainingData(newTrainingData);
            trainingPlantsNameRepository.save(newTrainingPlantsName);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipFile.length())
                .body(resource);
    }

    @PostMapping("/deleterecord/{id}")
    public String deleteRecord(@PathVariable int id) {
        Records records = recordsRepository.findById(id).orElse(null);
        List<RecordImage> allRecordImages = records.getRecordImage();
        for (RecordImage recImg: allRecordImages) {
            try {
                deleteImage(recImg);
                recordImageRepository.delete(recImg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        recordsRepository.delete(records);
        return "redirect:/verifyrecord";
    }

    @GetMapping("/viewexportlog")
    public String viewExportLog(Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.getRole().getRoleName().equals("ADMIN") && !user.getRole().getRoleName().equals("DEVELOPER")) {
            return "noaccess";
        }
        List<TrainingData> trainingDataList = trainingDataRepository.findAll();
        
        // Add the list to the model to make it available in the view
        model.addAttribute("trainingDataList", trainingDataList);
        model.addAttribute("userRole", user.getRole().getRoleName());
        
        return "viewexportlog";  // The name of your HTML file
    }

    @GetMapping("/usercontrol")
    public String userControl(Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.getRole().getRoleName().equals("ADMIN")) {
            return "noaccess";
        }
        List<Users> allUsers = usersRepository.findAll();
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("userRole", user.getRole().getRoleName());
        model.addAttribute("currentUser", user);
        return "usercontrol";
    }

    @GetMapping("/edituser/{id}")
    public String editUser(@PathVariable int id, Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.getRole().getRoleName().equals("ADMIN")) {
            return "noaccess";
        }
        Users editUser = usersRepository.findById(id).orElse(null);
        model.addAttribute("editUser", editUser);  // Add user object to the model
        model.addAttribute("roles", roleRepository.findAll());  // Assuming you want to display a list of roles for selection
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "edituser";  // Return the name of the Thymeleaf template
    }

    @PostMapping("/updateuser")
    public String updateUser(@ModelAttribute Users user, @RequestParam("roleId") int roleId, RedirectAttributes redirectAttributes) {
        Users existUsers = usersRepository.findByUsername(user.getUsername());
        if (existUsers != null && existUsers.getUserId() != user.getUserId()) {
            redirectAttributes.addFlashAttribute("message", "มี Username นี้อยู่แล้ว");
            return "redirect:/edituser/" + user.getUserId();
        }
        Role role = roleRepository.findById(roleId).orElse(null);
        user.setRole(role);
        usersRepository.save(user);  // Save the updated user details
        return "redirect:/usercontrol";  // Redirect back to the user list page
    }

    @GetMapping("/adduser")
    public String addUserForm(Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.getRole().getRoleName().equals("ADMIN")) {
            return "noaccess";
        }
        Users newUser = new Users();  // Create a new empty user object
        model.addAttribute("newUser", newUser);  // Add the empty user object to the model
        model.addAttribute("roles", roleRepository.findAll());  // Get all available roles
        return "adduser";  // Return the Thymeleaf template for adding users
    }

    @PostMapping("/adduser")
    public String addUser(Model model, @ModelAttribute Users user, @RequestParam("roleId") int roleId, RedirectAttributes redirectAttributes) {
        if (usersRepository.findByUsername(user.getUsername()) != null) {
            redirectAttributes.addFlashAttribute("message", "มี Username นี้อยู่แล้ว");
            return "redirect:/adduser";
        }
        Role role = roleRepository.findById(roleId).orElse(null);  // Find the role by its ID
        user.setRole(role);  // Assign the selected role to the user
        usersRepository.save(user);  // Save the new user to the database
        model.addAttribute("userRole", user.getRole().getRoleName());
        return "redirect:/usercontrol";  // Redirect to the user list after successful creation
    }

    @PostMapping("/deleteuser/{id}")
    public String deleteUser(@PathVariable int id) {
        Users user = usersRepository.findById(id).orElse(null);
        Users defaultAdmin = usersRepository.findByUsername("defaultadmin");
        List<Records> childRecords = user.getRecords();
        List<TrainingData> childTrainingDatas = user.getTrainingData();
        for (Records rec: childRecords) {
            rec.setUserId(defaultAdmin);
            recordsRepository.save(rec);
        }

        for (TrainingData trainData: childTrainingDatas) {
            trainData.setUsers(defaultAdmin);
            trainingDataRepository.save(trainData);
        }
        usersRepository.delete(user);

        return "redirect:/usercontrol";
    }

}