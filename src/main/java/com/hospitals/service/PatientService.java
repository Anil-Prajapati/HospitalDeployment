package com.hospitals.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospitals.email.EmailNotification;
import com.hospitals.model.Patient;
import com.hospitals.repository.PatientRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private EmailNotification emailNotification;

    public Iterable<Patient> getAll() {
        log.info("Fetching all patients");
        return patientRepository.findAll();
    }

    public Patient getSingleData(int id) {
        log.info("Fetching patient with ID: {}", id);
        return patientRepository.findById(id).orElse(new Patient());
    }

    public Patient create(Patient patient) {
        log.info("Creating new patient: {}", patient.getPatientName());
        
        patient.setPatientDate(new Date());
        
        String emailContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                ".container { width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2); }" +
                ".header { background-color: #4CAF50; padding: 10px 0; color: white; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { padding: 25px; }" +
                ".footer { text-align: center; font-size: 12px; color: #777777; margin-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                    "<div class='header'>" +
                        "<h1>Welcome to Sunita Hospital!</h1>" +
                    "</div>" +
                    "<div class='content'>" +
                        "<p>Hello " + patient.getPatientName() + ",</p>" +
                        "<p>Congratulations! Your appointment has been successfully booked at Sunita Hospital.</p>" +
                        "<p><strong>Appointment Details:</strong></p>" +
                        "<p>- Appointment Book Date: " + patient.getPatientDate() + "</p>" +
                        "<p>- Appointment Book Time: " + patient.getPatientDOB() + "</p>" +
                        "<p>We look forward to seeing you at the appointment.</p>" +
                        "<p>If you have any questions or need to reschedule, please contact us.</p>" +
                    "</div>" +
                    "<div class='footer'>" +
                        "<p>Best regards,<br>The Sunita Hospital Team</p>" +
                        "<p>8081489506</p>" +
                    "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        // Send the email notification
        log.debug("Sending email to: {}", patient.getPetientEmail());
        emailNotification.mailSender("Patient Appointment Booked", emailContent, patient.getPetientEmail());
        
        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient saved successfully: {}", savedPatient.getPatientName());
        return savedPatient;
    }

    public Patient update(int id, String patientstatus) {
        log.info("Updating patient with ID: {} with status: {}", id, patientstatus);
        Patient patient = patientRepository.findById(id).orElse(new Patient());
        patient.setPatientstatus(patientstatus);
        Patient updatedPatient = patientRepository.save(patient);
        log.info("Patient status updated successfully: {}", updatedPatient.getPatientName());
        return updatedPatient;
    }

    public Patient updateDescription(int id, String descriptionDetails) {
        log.info("Updating description for patient with ID: {}: {}", id, descriptionDetails);
        Patient description = patientRepository.findById(id).orElse(new Patient());
        description.setDescriptionDetails(descriptionDetails);
        Patient updatedDescription = patientRepository.save(description);
        log.info("Patient description updated successfully: {}", updatedDescription.getPatientName());
        return updatedDescription;
    }

    public Map<String, Object> calculatePaidAmountMetrics() {
        log.info("Calculating paid amount metrics");

        Map<String, Object> metrics = new HashMap<>();

        Integer total = patientRepository.calculateTotalPaidAmount();
        Integer profit = patientRepository.calculateProfit();
        Integer loss = patientRepository.calculateLoss();
        Double average = patientRepository.calculateAveragePaidAmount();

        metrics.put("totalPaidAmount", total);
        metrics.put("profit", profit);
        metrics.put("loss", loss);
        metrics.put("averagePaidAmount", average);

        log.info("Total Paid Amount: {}", total);
        log.info("Profit: {}", profit);
        log.info("Loss: {}", loss);
        log.info("Average Paid Amount: {}", average);

        return metrics;
    }
}
