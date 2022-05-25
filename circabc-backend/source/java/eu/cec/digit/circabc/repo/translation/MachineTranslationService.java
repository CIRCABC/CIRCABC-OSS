package eu.cec.digit.circabc.repo.translation;

public interface MachineTranslationService {

    void sendMessage(
            String applicationName,
            String departmentNumber,
            String documentToTranslate,
            String domains,
            String errorCallback,
            String externalReference,
            String institution,
            String originalFileName,
            String outputFormat,
            int priority,
            String requesterCallback,
            String requestType,
            String sourceLanguage,
            String targetLanguage,
            String targetTranslationPath,
            String textToTranslate,
            String username);
}
