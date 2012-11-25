package hr.punintended.cashmemory.util;

import java.io.IOException;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.appspot.api.services.userendpoint.Userendpoint;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.AbstractGoogleClient;
import com.google.api.client.googleapis.services.AbstractGoogleClient.Builder;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * Common utilities.
 */
public class CloudEndpointUtils {

  public static final boolean LOCAL_ANDROID_RUN = false;
  private static final String LOCAL_APP_ENGINE_SERVER_URL = "http://192.168.1.10:8888";

  private static Userendpoint userEndpoint;

  public static Userendpoint getUserEndpoint() {
    if (userEndpoint == null) {
      Builder userEndpointBuilder = new Userendpoint.Builder(
          newCompatibleTransport(), new JacksonFactory(),
          new HttpRequestInitializer() {
            public void initialize(HttpRequest httpRequest) {}
          });
      userEndpoint = (Userendpoint) CloudEndpointUtils.updateBuilder(
          userEndpointBuilder).build();
    }

    return userEndpoint;
  }

  private static <B extends AbstractGoogleClient.Builder> B updateBuilder(
      B builder) {
    if (LOCAL_ANDROID_RUN) {
      builder.setRootUrl(LOCAL_APP_ENGINE_SERVER_URL + "/_ah/api/");
    }
    // only enable GZip when connecting to remote server
    final boolean enableGZip = builder.getRootUrl().startsWith("https:");
    builder
        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {

          @Override
          public void initialize(AbstractGoogleClientRequest<?> request)
              throws IOException {
            request.setDisableGZipContent(!enableGZip);
          }

        });
    return builder;
  }

  /**
   * Logs the given message and shows an error alert dialog with it.
   * 
   * @param activity
   *          activity
   * @param tag
   *          log tag to use
   * @param message
   *          message to log and show or {@code null} for none
   */
  public static void logAndShow(Activity activity, String tag, String message) {
    Log.e(tag, message);
    showError(activity, message);
  }

  /**
   * Logs the given throwable and shows an error alert dialog with its message.
   * 
   * @param activity
   *          activity
   * @param tag
   *          log tag to use
   * @param t
   *          throwable to log and show
   */
  public static void logAndShow(Activity activity, String tag, Throwable t) {
    Log.e(tag, "Error", t);
    String message = t.getMessage();
    if (t instanceof GoogleJsonResponseException) {
      GoogleJsonError details = ((GoogleJsonResponseException) t).getDetails();
      if (details != null) {
        message = details.getMessage();
      }
    }
    showError(activity, message);
  }

  /**
   * Shows an error alert dialog with the given message.
   * 
   * @param activity
   *          activity
   * @param message
   *          message to show or {@code null} for none
   */
  public static void showError(final Activity activity, String message) {
    final String errorMessage = message == null ? "Error" : "[Error ] "
        + message;
    Resources resources = activity.getResources();
    activity.runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
      }
    });
  }

  private static HttpTransport newCompatibleTransport() {
    return isGingerbreadOrHigher() ? new NetHttpTransport()
        : new ApacheHttpTransport();
  }

  /** Returns whether the SDK version is Gingerbread (2.3) or higher. */
  private static boolean isGingerbreadOrHigher() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
  }
}
