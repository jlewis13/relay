package io.forsta.securesms.jobs;

import android.content.Context;
import android.support.annotation.NonNull;

import androidx.work.Data;
import androidx.work.WorkerParameters;
import io.forsta.securesms.crypto.MasterSecret;
import io.forsta.securesms.database.DatabaseFactory;
import io.forsta.securesms.database.RecipientPreferenceDatabase;
import io.forsta.securesms.dependencies.InjectableType;
import io.forsta.securesms.jobmanager.JobParameters;
import io.forsta.securesms.jobmanager.SafeData;
import io.forsta.securesms.jobs.requirements.MasterSecretRequirement;
import io.forsta.securesms.recipients.Recipients;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.multidevice.BlockedListMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import io.forsta.securesms.dependencies.TextSecureCommunicationModule;

public class MultiDeviceBlockedUpdateJob extends MasterSecretJob implements InjectableType {

  private static final long serialVersionUID = 1L;

  private static final String TAG = MultiDeviceBlockedUpdateJob.class.getSimpleName();

  @Inject transient TextSecureCommunicationModule.TextSecureMessageSenderFactory messageSenderFactory;

  public MultiDeviceBlockedUpdateJob(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
    super(context, workerParameters);
  }

  public MultiDeviceBlockedUpdateJob(Context context) {
    super(context, JobParameters.newBuilder()
                                .withNetworkRequirement()
                                .withMasterSecretRequirement()
                                .withGroupId(MultiDeviceBlockedUpdateJob.class.getSimpleName())
                                .create());
  }

  @Override
  public void onRun(MasterSecret masterSecret)
      throws IOException, UntrustedIdentityException
  {
    RecipientPreferenceDatabase database      = DatabaseFactory.getRecipientPreferenceDatabase(context);
    SignalServiceMessageSender  messageSender = messageSenderFactory.create();
    RecipientPreferenceDatabase.BlockedReader reader        = database.readerForBlocked(database.getBlocked());
    List<String>                blocked       = new LinkedList<>();

    Recipients recipients;

    while ((recipients = reader.getNext()) != null) {
      if (recipients.isSingleRecipient()) {
        blocked.add(recipients.getPrimaryRecipient().getAddress());
      }
    }

    messageSender.sendMessage(SignalServiceSyncMessage.forBlocked(new BlockedListMessage(blocked)));
  }

  @Override
  public boolean onShouldRetryThrowable(Exception exception) {
    if (exception instanceof PushNetworkException) return true;
    return false;
  }

  @Override
  public void onAdded() {

  }

  @NonNull
  @Override
  protected Data serialize(@NonNull Data.Builder dataBuilder) {
    return dataBuilder.build();
  }

  @Override
  protected void initialize(@NonNull SafeData data) {

  }

  @Override
  public void onCanceled() {

  }
}
