

package app.coronawarn.server.common.federation.client.download;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Converter for converting federation gateway HTTP responses into {@link BatchDownloadResponse} objects.
 */
public class FederationGatewayHttpMessageConverter extends AbstractHttpMessageConverter<BatchDownloadResponse> {

  /**
   * The media-type for protobuf {@code application/protobuf}.
   */
  public static final MediaType PROTOBUF = new MediaType("application", "protobuf", StandardCharsets.UTF_8);
  public static final String HEADER_BATCH_TAG = "batchTag";
  public static final String HEADER_NEXT_BATCH_TAG = "nextBatchTag";
  public static final String EMPTY_HEADER = "null";

  public FederationGatewayHttpMessageConverter() {
    setSupportedMediaTypes(Collections.singletonList(PROTOBUF));
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return BatchDownloadResponse.class.isAssignableFrom(clazz);
  }

  @Override
  protected BatchDownloadResponse readInternal(Class<? extends BatchDownloadResponse> clazz, HttpInputMessage message)
      throws IOException {
    String batchTag = getHeader(message, HEADER_BATCH_TAG)
        .orElseThrow(() -> new HttpMessageNotReadableException("Missing " + HEADER_BATCH_TAG + " header.", message));
    Optional<String> nextBatchTag = getHeader(message, HEADER_NEXT_BATCH_TAG);

    try (InputStream body = message.getBody()) {
      DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.parseFrom(body);
      return new BatchDownloadResponse(diagnosisKeyBatch, batchTag, nextBatchTag);
    } catch (InvalidProtocolBufferException e) {
      throw new HttpMessageNotReadableException("Failed to parse protocol buffers message", e, message);
    }
  }

  private Optional<String> getHeader(HttpInputMessage response, String header) {
    String headerString = response.getHeaders().getFirst(header);
    return (!EMPTY_HEADER.equals(headerString))
        ? Optional.ofNullable(headerString)
        : Optional.empty();
  }

  @Override
  protected void writeInternal(BatchDownloadResponse message, HttpOutputMessage outputMessage) {
    throw new UnsupportedOperationException();
  }
}