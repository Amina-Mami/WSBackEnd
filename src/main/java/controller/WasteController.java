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

        String type = (String) newWaste.get("type");
        double weight = Double.parseDouble(newWaste.get("weight").toString());
        String status = (String) newWaste.get("status");
        String createdAt = (String) newWaste.get("createdAt");


        wasteService.addWaste(type, weight, status, createdAt);


        Map<String, Object> response = new HashMap<>();
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


        String newType = (String) updatedWaste.get("hasType");
        if (newType == null) {
            return ResponseEntity.badRequest().body("Le champ 'type' est requis.");
        }

        Double newWeight;
        try {
            newWeight = Double.parseDouble(updatedWaste.get("hasWeight").toString());
        } catch (NullPointerException | NumberFormatException e) {
            return ResponseEntity.badRequest().body("Le champ 'weight' est requis et doit être un nombre.");
        }

        String newStatus = (String) updatedWaste.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest().body("Le champ 'status' est requis.");
        }

        String createdAt = (String) updatedWaste.get("createdAt");
        if (createdAt == null) {
            return ResponseEntity.badRequest().body("Le champ 'createdAt' est requis.");
        }


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



    @GetMapping(value = "/waste/{wasteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getWasteById(@PathVariable String wasteId) {
        String waste = wasteService.getWasteById(wasteId);

        if (waste != null && !waste.contains("error")) {
            return ResponseEntity.ok(waste);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(waste);
        }
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getWastesByStatus(
            @RequestParam(required = false) String status) {
        String result;

        if (status != null && !status.isEmpty()) {
            result = wasteService.queryWastesByStatus(status);
        } else {
            result = "{\"error\": \"Veuillez fournir un statut\"}";
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }





}