package io.forsta.ccsm.messaging;

import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceGroup;

import java.util.List;

import io.forsta.securesms.crypto.MasterSecretUnion;

public class IncomingMessage extends IncomingMediaMessage {

  public IncomingMessage(String from, String to, String body, long sentTimeMillis, long expriesIn) {
    super(from, to, body, sentTimeMillis, expriesIn);
  }

  public IncomingMessage(MasterSecretUnion masterSecret, String from, String to, long sentTimeMillis, int subscriptionId, long expiresIn, boolean expirationUpdate, Optional<String> relay, Optional<String> body, Optional<SignalServiceGroup> group, Optional<List<SignalServiceAttachment>> attachments, String messageRef, int voteCount, String messageId) {
    super(masterSecret, from, to, sentTimeMillis, subscriptionId, expiresIn, expirationUpdate, relay, body, group, attachments, messageRef, voteCount, messageId);
  }
}
