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

import java.time.Instant;
import java.util.*;

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
        try {
            model.addAttribute("profiles", userProfileService.listProfiles());
        } catch (Exception ex) {
            model.addAttribute("profiles", java.util.Collections.emptyList());
            model.addAttribute("errorMessage", "We hit a snag. Please try again from the home page. If the problem persists, the database may be temporarily unavailable.");
        }
        return "index";
    }

    // POST /scan → triggers a scan for provided email & name
    @PostMapping("/scan")
    public String triggerScan(@RequestParam("fullName") String fullName,
                              @RequestParam("email") String email,
                              Model model) {
        try {
            UserProfileDto dto = new UserProfileDto(fullName, email);
            UserProfile profile = userProfileService.createProfile(dto);
            ScanRecord scanRecord = scanService.performScan(profile.getId());

            model.addAttribute("profile", profile);
            model.addAttribute("scan", scanRecord);
            model.addAttribute("breaches", scanRecord.getBreachRecords());
            return "results";
        } catch (Exception ex) {
            // Show friendly message on home page; do not change URLs or credentials
            model.addAttribute("errorMessage", "We hit a snag. Please try again from the home page. If the problem persists, the database may be temporarily unavailable.");
            try {
                model.addAttribute("profiles", userProfileService.listProfiles());
            } catch (Exception ignored) {
                model.addAttribute("profiles", java.util.Collections.emptyList());
            }
            return "index";
        }
    }

    // GET /scan/{id} → view scan results
    @GetMapping("/scan/{id}")
    public String viewScan(@PathVariable("id") UUID scanId, Model model) {
        try {
            ScanRecord scan = scanService.getScanRecord(scanId);
            UserProfile profile = scan.getUserProfile();
            List<BreachRecord> breaches = scan.getBreachRecords();

            model.addAttribute("profile", profile);
            model.addAttribute("scan", scan);
            model.addAttribute("breaches", breaches);
            return "results";
        } catch (Exception ex) {
            model.addAttribute("error", "Unable to load scan: " + ex.getMessage());
            return "results"; // show resilient template rather than 500
        }
    }

    // GET /profile/{id} → view user profile
    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable("id") UUID profileId, Model model) {
        UserProfile profile = userProfileService.getProfile(profileId);
        model.addAttribute("profile", profile);
        return "profile";
    }

    @GetMapping("/scan")
    public String scanGetRedirect() {
        return "redirect:/";
    }
}
