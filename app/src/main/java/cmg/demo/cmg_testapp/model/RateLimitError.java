package cmg.demo.cmg_testapp.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * This class is required to parse rate limit error returned with status code 403.
 */

@Parcel
public class RateLimitError {

    @SerializedName("message")
    String message;

    @SerializedName("documentationUrl")
    String documentationUrl;

    public String getMessage() {
        return message;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }
}
