package com.ghostcheck.controller;

import com.ghostcheck.entity.BreachRecord;
import com.ghostcheck.entity.ScanRecord;
import com.ghostcheck.entity.UserProfile;
import com.ghostcheck.service.ScanService;
import com.ghostcheck.service.UserProfileService;
import com.ghostcheck.service.dto.UserProfileDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
public class ScanController {

    private final ScanService scanService;
    private final UserProfileService userProfileService;

    public ScanController(ScanService scanService, UserProfileService userProfileService) {
        this.scanService = scanService;
        this.userProfileService = userProfileService;
    }

    // GET / → index.html
    @GetMapping("/")
    public String index(Model model) {
        // ...existing code...
        model.addAttribute("profiles", userProfileService.listProfiles());
        return "index";
    }

    // POST /scan → triggers a scan for provided email & name
    @PostMapping("/scan")
    public String triggerScan(@RequestParam("fullName") String fullName,
                              @RequestParam("email") String email,
                              Model model) {
        UserProfileDto dto = new UserProfileDto(fullName, email);
        UserProfile profile = userProfileService.createProfile(dto);

        ScanRecord scanRecord = scanService.performScan(profile.getId());

        model.addAttribute("profile", profile);
        model.addAttribute("scan", scanRecord);
        model.addAttribute("breaches", scanRecord.getBreachRecords()); // assumes mapped collection
        return "scan";
    }

    // GET /scan/{id} → view scan results
    @GetMapping("/scan/{id}")
    public String viewScan(@PathVariable("id") UUID scanId, Model model) {
        // ...existing code...
        // Minimal lookup: if ScanService returns ScanRecord via repository, prefer a dedicated method.
        // For simplicity, load via performScan result expectation or introduce a fetch. Assuming repository exists:
        // model attributes below expect that ScanRecord has getUserProfile() and getBreachRecords()
        ScanRecord scan = scanService.getScanRecord(scanId); // implement a read-only getter in ScanService if not present
        UserProfile profile = scan.getUserProfile();
        List<BreachRecord> breaches = scan.getBreachRecords();

        model.addAttribute("profile", profile);
        model.addAttribute("scan", scan);
        model.addAttribute("breaches", breaches);
        return "scan";
    }

    // GET /profile/{id} → view user profile
    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable("id") UUID profileId, Model model) {
        UserProfile profile = userProfileService.getProfile(profileId);
        model.addAttribute("profile", profile);
        return "profile";
    }
}

