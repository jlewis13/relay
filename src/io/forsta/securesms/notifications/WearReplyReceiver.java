/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.forsta.securesms.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.RemoteInput;

import io.forsta.securesms.crypto.MasterSecret;
import io.forsta.securesms.database.DatabaseFactory;
import io.forsta.securesms.database.MessagingDatabase.MarkedMessageInfo;
import io.forsta.securesms.database.RecipientPreferenceDatabase.RecipientsPreferences;
import io.forsta.securesms.recipients.RecipientFactory;
import io.forsta.securesms.recipients.Recipients;
import io.forsta.ccsm.messaging.MessageSender;
import io.forsta.securesms.sms.OutgoingTextMessage;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.List;

/**
 * Get the response text from the Wearable Device and sends an message as a reply
 */
public class WearReplyReceiver extends MasterSecretBroadcastReceiver {

  public static final String TAG                 = WearReplyReceiver.class.getSimpleName();
  public static final String REPLY_ACTION        = "io.forsta.securesms.notifications.WEAR_REPLY";
  public static final String RECIPIENT_IDS_EXTRA = "recipient_ids";

  @Override
  protected void onReceive(final Context context, Intent intent,
                           final @Nullable MasterSecret masterSecret)
  {
    if (!REPLY_ACTION.equals(intent.getAction())) return;

    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

    if (remoteInput == null) return;

    final long[]       recipientIds = intent.getLongArrayExtra(RECIPIENT_IDS_EXTRA);
    final CharSequence responseText = remoteInput.getCharSequence(MessageNotifier.EXTRA_VOICE_REPLY);
    final Recipients   recipients   = RecipientFactory.getRecipientsForIds(context, recipientIds, false);

    if (masterSecret != null && responseText != null) {
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          long threadId;

//          Optional<RecipientsPreferences> preferences = DatabaseFactory.getRecipientPreferenceDatabase(context).getRecipientsPreferences(recipientIds);
//          int  subscriptionId = preferences.isPresent() ? preferences.get().getDefaultSubscriptionId().or(-1) : -1;
//          long expiresIn      = preferences.isPresent() ? preferences.get().getExpireMessages() * 1000 : 0;
//
//          OutgoingTextMessage reply = new OutgoingTextMessage(recipients, responseText.toString(), expiresIn, subscriptionId);
//          threadId = MessageSender.send(context, masterSecret, reply, -1, false);
//
//          List<MarkedMessageInfo> messageIds = DatabaseFactory.getThreadDatabase(context).setRead(threadId);
//          MessageNotifier.updateNotification(context, masterSecret);
//          MarkReadReceiver.process(context, messageIds);

          return null;
        }
      };
      // .execute(); disabled. Obsolete code path
    }

  }
}
