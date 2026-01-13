package com.xaiforge.application.service;

import com.xaiforge.common.dto.ChangePasswordRequest;
import com.xaiforge.common.dto.UpdatePreferencesRequest;
import com.xaiforge.common.dto.UpdateProfileRequest;
import com.xaiforge.common.exception.InvalidCredentialsException;
import com.xaiforge.common.exception.UserNotFoundException;
import com.xaiforge.common.exception.ValidationException;
import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.domain.user.entity.UserProfile;
import com.xaiforge.domain.user.entity.UserPreferences;
import com.xaiforge.infrastructure.persistence.dataset.DatasetRepository;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import com.xaiforge.infrastructure.persistence.user.UserPreferencesRepository;
import com.xaiforge.infrastructure.persistence.user.UserProfileRepository;
import com.xaiforge.infrastructure.persistence.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Transactional
public class UserApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(UserApplicationService.class);
    
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    public UserApplicationService(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            UserPreferencesRepository preferencesRepository,
            DatasetRepository datasetRepository,
            MLModelRepository modelRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.preferencesRepository = preferencesRepository;
        this.datasetRepository = datasetRepository;
        this.modelRepository = modelRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }
    
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Check if email is already taken by another user
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ValidationException("email", "Email is already in use");
            }
            user.setEmail(request.email());
        }
        
        // Get or create profile
        UserProfile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserProfile newProfile = new UserProfile();
                newProfile.setUser(user);
                return newProfile;
            });
        
        if (request.firstName() != null) {
            profile.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            profile.setLastName(request.lastName());
        }
        if (request.organization() != null) {
            profile.setOrganization(request.organization());
        }
        if (request.role() != null) {
            profile.setRole(request.role());
        }
        
        profileRepository.save(profile);
        user.setProfile(profile);
        userRepository.save(user);
    }
    
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
    
    @Transactional
    public void updatePreferences(Long userId, UpdatePreferencesRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Get or create preferences
        UserPreferences preferences = preferencesRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserPreferences newPrefs = new UserPreferences();
                newPrefs.setUser(user);
                return newPrefs;
            });
        
        if (request.theme() != null) {
            preferences.setTheme(request.theme());
        }
        if (request.accentColor() != null) {
            preferences.setAccentColor(request.accentColor());
        }
        if (request.notificationPreferences() != null) {
            preferences.setNotificationPreferences(request.notificationPreferences());
        }
        
        preferencesRepository.save(preferences);
        user.setPreferences(preferences);
        userRepository.save(user);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long userId) {
        log.info("Starting account deletion for user {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found: {}", userId);
                return new UserNotFoundException(userId);
            });
        
        try {
            // Step 1: Get all models owned by the user and delete their files
            List<MLModel> userModels = modelRepository.findByDatasetOwnerId(userId);
            log.info("Found {} models to delete for user {}", userModels.size(), userId);
            
            for (MLModel model : userModels) {
                try {
                    if (model.getSerializedModelPath() != null && !model.getSerializedModelPath().isEmpty()) {
                        Path modelPath = Paths.get(model.getSerializedModelPath());
                        if (Files.exists(modelPath)) {
                            Files.delete(modelPath);
                            log.info("Deleted model file: {}", model.getSerializedModelPath());
                        } else {
                            log.warn("Model file does not exist: {}", model.getSerializedModelPath());
                        }
                    }
                } catch (IOException e) {
                    log.error("Failed to delete model file: {}", model.getSerializedModelPath(), e);
                    // Continue with deletion even if file deletion fails
                }
            }
            
            // Step 2: Get all datasets owned by the user and delete their files
            List<Dataset> datasets = datasetRepository.findByOwnerId(userId);
            log.info("Found {} datasets to delete for user {}", datasets.size(), userId);
            
            for (Dataset dataset : datasets) {
                try {
                    if (dataset.getFilePath() != null && !dataset.getFilePath().isEmpty()) {
                        Path datasetPath = Paths.get(dataset.getFilePath());
                        if (Files.exists(datasetPath)) {
                            Files.delete(datasetPath);
                            log.info("Deleted dataset file: {}", dataset.getFilePath());
                        } else {
                            log.warn("Dataset file does not exist: {}", dataset.getFilePath());
                        }
                    }
                } catch (IOException e) {
                    log.error("Failed to delete dataset file: {}", dataset.getFilePath(), e);
                    // Continue with deletion even if file deletion fails
                }
            }
            
            // Step 3: Delete all models from database (must be before datasets due to foreign key)
            // Note: model_features will be automatically deleted via @ElementCollection
            if (!userModels.isEmpty()) {
                for (MLModel model : userModels) {
                    modelRepository.delete(model);
                }
                modelRepository.flush(); // Ensure deletion is committed
                log.info("Deleted {} models from database for user {}", userModels.size(), userId);
            }
            
            // Step 4: Delete all datasets from database
            // Note: dataset_headers will be automatically deleted via @ElementCollection if it exists
            if (!datasets.isEmpty()) {
                for (Dataset dataset : datasets) {
                    datasetRepository.delete(dataset);
                }
                datasetRepository.flush(); // Ensure deletion is committed
                log.info("Deleted {} datasets from database for user {}", datasets.size(), userId);
            }
            
            // Step 5: Delete user (this will cascade delete via ON DELETE CASCADE:
            // - user_profiles
            // - user_preferences
            // - prediction_records (via user_id)
            // - activity_logs
            // - api_keys
            // - notifications
            userRepository.delete(user);
            userRepository.flush(); // Ensure deletion is committed
            log.info("Successfully deleted user account: {}", userId);
            
        } catch (UserNotFoundException e) {
            log.error("User not found during deletion: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Error during account deletion for user {}", userId, e);
            throw new RuntimeException("Failed to delete account: " + e.getMessage(), e);
        }
    }
}
