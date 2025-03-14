package com.whatisbai.Controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.whatisbai.Entities.Medicine;
import com.whatisbai.Entities.Plants;
import com.whatisbai.Entities.PlantsInMedicine;
import com.whatisbai.Entities.Users;
import com.whatisbai.Repositories.MedicineRepository;
import com.whatisbai.Repositories.PlantsInMedicineRepository;
import com.whatisbai.Repositories.PlantsRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/medicines")
@SessionAttributes("plantsInQueue")
public class MedicineController {

    @Autowired
    private MedicineRepository medicineRepository;
    
    @Autowired
    private PlantsRepository plantsRepository;
    
    @Autowired
    private PlantsInMedicineRepository plantsInMedicineRepository;

    @ModelAttribute("plantsInQueue")
    public List<PlantsInMedicine> initializePlantsInQueue() {
        return new ArrayList<>();
    }

    @GetMapping("/new")
    public String showForm(Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("medicine", new Medicine());
        List<Plants> plantsList = plantsRepository.findAll();
        model.addAttribute("plants", plantsList);
        model.addAttribute("userRole", user.getRole().getRoleName());
        
        return "medicineform";
    }

    @PostMapping("/add-plant")
    public String addPlantToQueue(@RequestParam int plantId,
                                @RequestParam("quantity") double quantity,
                                @RequestParam("unit") String unit,
                                @ModelAttribute("plantsInQueue") List<PlantsInMedicine> plantsInQueue) {
        Plants plant = plantsRepository.findById(plantId).orElseThrow();
        PlantsInMedicine plantInMedicine = new PlantsInMedicine();
        plantInMedicine.setPlant(plant);
        plantInMedicine.setQuantity(quantity);
        plantInMedicine.setUnit(unit);
        plantsInQueue.add(plantInMedicine);
        return "redirect:/medicines/new"; // Redirect to avoid form resubmission on refresh
    }


    @PostMapping("/remove-plant")
    public String removePlantFromQueue(@RequestParam int index,
                                       @ModelAttribute("plantsInQueue") List<PlantsInMedicine> plantsInQueue) {
        if (index >= 0 && index < plantsInQueue.size()) {
            plantsInQueue.remove(index);
        }
        return "redirect:/medicines/new";
    }

    @PostMapping("/save")
    public String saveMedicine(@ModelAttribute Medicine medicine,
                            @RequestParam("plantsInQueue") String plantsInQueueData,
                            SessionStatus sessionStatus, RedirectAttributes redirectAttributes) {
        
        if (medicineRepository.findByMedicineName(medicine.getMedName()) != null) {
            redirectAttributes.addFlashAttribute("message", "มีสูตรยานี้อยู่แล้ว");
            return "redirect:/medicines/new";
        }
            
        medicineRepository.save(medicine);

        if (!plantsInQueueData.equals("")) {
            String[] plantEntries = plantsInQueueData.split(",");
            List<Integer> plantInserted = new ArrayList<>();
            for (String entry : plantEntries) {
                String[] parts = entry.split(":");
                int plantId = Integer.parseInt(parts[0]);
                if (plantInserted.contains(plantId)) {
                    continue;
                }
                double quantity = Double.parseDouble(parts[1]);
                String unit = parts[2];
                Plants plant = plantsRepository.findById(plantId).orElseThrow();
                PlantsInMedicine plantInMedicine = new PlantsInMedicine();
                plantInMedicine.setPlant(plant);
                plantInMedicine.setQuantity(quantity);
                plantInMedicine.setUnit(unit);
                plantInMedicine.setMedicine(medicine);
                plantsInMedicineRepository.save(plantInMedicine);
                plantInserted.add(plantId);
            }
        }
        sessionStatus.setComplete();
        return "redirect:/medicines/new";
    }

    @GetMapping("/find")
    public String findMedicine(Model model) {
        return "medicinesearch";
    }

    @GetMapping("/search-plants")
    @ResponseBody
    public List<Map<String, Object>> searchPlants(@RequestParam("term") String searchTerm) {
        List<Plants> plants = plantsRepository.findByPlantNameContaining(searchTerm);

        // Convert Plant objects to a list of Maps for autocomplete (label, value)
        return plants.stream()
        .map(plant -> {
            Map<String, Object> plantMap = new HashMap<>();
            plantMap.put("label", plant.getPlantsName().getPlantName() + ", " + plant.getPlantUnit());
            plantMap.put("value", plant.getPlantId());
            return plantMap;
        })
        .collect(Collectors.toList());
    }

    @GetMapping("/search-medicines")
    @ResponseBody
    public List<Map<String, Object>> searchMedicines(@RequestParam("term") String searchTerm) {
        List<Medicine> medicines = medicineRepository.findByMedicineNameContaining(searchTerm);

        // Return results
        return medicines.stream()
            .map(medicine -> {
                Map<String, Object> medicineMap = new HashMap<>();
                medicineMap.put("label", medicine.getMedName());
                medicineMap.put("value", medicine.getMedId());
                return medicineMap;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/initial-medicines")
    @ResponseBody
    public List<Map<String, Object>> getInitialMedicines() {
        List<Medicine> medicines = medicineRepository.findFirst30ByOrderByMedIdAsc();

        return medicines.stream()
            .map(medicine -> {
                Map<String, Object> medicineMap = new HashMap<>();
                medicineMap.put("label", medicine.getMedName());
                medicineMap.put("value", medicine.getMedId());
                return medicineMap;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public String getMedicineDetail(@PathVariable("id") Integer id, Model model) {
        List<PlantsInMedicine> plantsInMedicine = plantsInMedicineRepository.findByMedicineId(id);
        model.addAttribute("plantsInMedicine", plantsInMedicine);
        return "medicinedetail";
    }

    @GetMapping("/findplantinmedicine/{id}")
    public String viewPlantInMedicine(@PathVariable("id") Integer id, Model model) {
        List <PlantsInMedicine> plantsInMedicines = plantsInMedicineRepository.findByPlantsNameId(id);
        for (var e: plantsInMedicines){
            System.out.println(e.getPlant().getPlantsName().getPlantName());
        }
        model.addAttribute("plantsInMedicine", plantsInMedicines);
        return "viewplantinmedicine";
    }

    @PostMapping("/update")
    public String updateMedicine(@ModelAttribute Medicine medicine,
                                @RequestParam("plantsInQueue") String plantsInQueueData,
                                SessionStatus sessionStatus, RedirectAttributes redirectAttributes) {
        
        // Fetch the existing medicine
        Medicine existingMedicine = medicineRepository.findById(medicine.getMedId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid medicine ID: " + medicine.getMedId()));

        Medicine checkMedicine = medicineRepository.findByMedicineName(medicine.getMedName());
        if (checkMedicine != null && checkMedicine.getMedId() != medicine.getMedId()) {
            redirectAttributes.addAttribute("message", "มีชื่อสูตรยานี้อยู่แล้ว");
            return "redirect:/medicines/edit/" + medicine.getMedId();
        }
        medicineRepository.save(medicine);

        // Fetch current PlantsInMedicine entries
        List<PlantsInMedicine> currentPlantsInMedicine = plantsInMedicineRepository.findByMedicine(existingMedicine);
        Set<Integer> currentPlantIds = currentPlantsInMedicine.stream()
                .map(PlantsInMedicine::getPlantMedId)
                .collect(Collectors.toSet());

        String[] plantEntries = plantsInQueueData.split(",");
        List<PlantsInMedicine> newPlants = new ArrayList<>();
        
        for (String entry : plantEntries) {
            String[] parts = entry.split(":");
            int plantMedId = Integer.parseInt(parts[0]);
            int plantId = Integer.parseInt(parts[1]);
            Plants inPlant = plantsRepository.findById(plantId).orElse(null);
            double quantity = Double.parseDouble(parts[2]);
            String unit = parts[3];

            // Create new PlantsInMedicine instance
            PlantsInMedicine inPlantMed = new PlantsInMedicine();
            inPlantMed.setPlantMedId(plantMedId);
            inPlantMed.setPlant(inPlant);
            inPlantMed.setQuantity(quantity);
            inPlantMed.setUnit(unit);
            inPlantMed.setMedicine(existingMedicine);
            
            // Add new entry if it doesn't exist in currentPlantsInMedicine
            if (!currentPlantIds.contains(plantMedId)) {
                newPlants.add(inPlantMed);
            }
        }

        // Save new plants to the database
        if (!newPlants.isEmpty()) {
            plantsInMedicineRepository.saveAll(newPlants);
        }

        // Remove any plants that are no longer in the updated list
        for (PlantsInMedicine existingPlant : currentPlantsInMedicine) {
            if (!Arrays.stream(plantEntries).anyMatch(entry -> entry.startsWith(existingPlant.getPlantMedId() + ":"))) {
                plantsInMedicineRepository.delete(existingPlant);
            }
        }

        sessionStatus.setComplete();
        return "redirect:/medicines/viewall";
    }


}



