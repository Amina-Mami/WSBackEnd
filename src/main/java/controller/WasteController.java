package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.WasteService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/wastes")
public class WasteController {

    @Autowired
    private WasteService wasteService;




    @GetMapping(value = "/wastes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getWastes() {
        String result = wasteService.queryWastes();
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/wastes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addWaste(@RequestBody Map<String, Object> newWaste) {
        String wasteId = (String) newWaste.get("waste_id");
        String type = (String) newWaste.get("type");
        double weight = Double.parseDouble(newWaste.get("weight").toString());
        String status = (String) newWaste.get("status");
        String createdAt = (String) newWaste.get("createdAt");


        wasteService.addWaste(wasteId, type, weight, status, createdAt);


        Map<String, Object> response = new HashMap<>();
        response.put("waste_id", wasteId);
        response.put("type", type);
        response.put("weight", weight);
        response.put("status", status);
        response.put("createdAt", createdAt);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @PutMapping(value = "/wastes/{wasteId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateWaste(
            @PathVariable String wasteId,
            @RequestBody Map<String, Object> updatedWaste) {


        String newType = (String) updatedWaste.get("type");
        double newWeight = Double.parseDouble(updatedWaste.get("weight").toString());
        String newStatus = (String) updatedWaste.get("status");
        String createdAt = (String) updatedWaste.get("createdAt");


        wasteService.updateWaste(wasteId, newType, newWeight, newStatus, createdAt);

        return ResponseEntity.ok("Déchet mis à jour avec succès !");
    }


    @DeleteMapping("/waste/{wasteId}")
    public ResponseEntity<String> deleteWaste(@PathVariable String wasteId) {
        try {
            wasteService.deleteWasteById(wasteId);
            return ResponseEntity.ok("Déchet supprimé avec succès: " + wasteId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du déchet: " + e.getMessage());
        }
    }

    @GetMapping(value = "/type", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getWastes(
            @RequestParam(required = false) String type) {
        String result;

        if (type != null && !type.isEmpty()) {
            result = wasteService.queryWastesByType(type);
        } else {
            result = wasteService.queryWastes();
        }

        return ResponseEntity.ok(result);
    }

}