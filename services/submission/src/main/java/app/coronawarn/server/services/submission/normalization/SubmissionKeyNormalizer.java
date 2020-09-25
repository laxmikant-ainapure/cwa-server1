

package app.coronawarn.server.services.submission.normalization;

import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.TekFieldDerivations;
import java.util.Objects;

public final class SubmissionKeyNormalizer implements DiagnosisKeyNormalizer {

  private TekFieldDerivations tekFieldMappings;

  public SubmissionKeyNormalizer(SubmissionServiceConfig config) {
    tekFieldMappings = config.getTekFieldDerivations();
  }

  @Override
  public NormalizableFields normalize(NormalizableFields fieldsAndValues) {
    Integer trlValue = fieldsAndValues.getTransmissionRiskLevel();
    Integer dsos = fieldsAndValues.getDaysSinceOnsetOfSymptoms();

    throwIfAllRequiredFieldsMissing(trlValue, dsos);

    if (isMissing(dsos)) {
      dsos = tekFieldMappings.deriveDsosFromTrl(trlValue);
    }

    return NormalizableFields.of(trlValue, dsos);
  }

  private void throwIfAllRequiredFieldsMissing(Integer trlValue, Integer dsos) {
    if (isMissing(trlValue) && isMissing(dsos)) {
      throw new IllegalArgumentException("Normalization of key values failed. A key was provided with"
          + " both 'transmissionRiskLevel' and 'daysSinceOnsetOfSymptoms' fields missing");
    }
  }

  private boolean isMissing(Integer fieldValue) {
    return Objects.isNull(fieldValue);
  }
}