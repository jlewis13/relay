package io.forsta.securesms.components;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pnikosis.materialishprogress.ProgressWheel;

//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
import io.forsta.ccsm.api.model.ForstaMessage;
import io.forsta.securesms.R;
import io.forsta.securesms.database.AttachmentDatabase;
import io.forsta.securesms.database.model.MediaMmsMessageRecord;
import io.forsta.securesms.database.model.MessageRecord;
import io.forsta.securesms.events.PartProgressEvent;
import io.forsta.securesms.mms.DocumentSlide;
import io.forsta.securesms.mms.Slide;
import io.forsta.securesms.mms.SlideClickListener;
import io.forsta.securesms.util.SaveAttachmentTask;
import io.forsta.securesms.util.Util;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.List;

public class DocumentView extends FrameLayout {

  private static final String TAG = DocumentView.class.getSimpleName();

  private final @NonNull AnimatingToggle controlToggle;
  private final @NonNull ImageView       downloadButton;
  private final @NonNull ProgressWheel   downloadProgress;
  private final @NonNull View            documentBackground;
  private final @NonNull View            container;
  private final @NonNull TextView        fileName;
  private final @NonNull TextView        fileSize;
  private final @NonNull TextView        document;

  private @Nullable SlideClickListener downloadListener;
  private @Nullable SlideClickListener viewListener;
  private @Nullable DocumentSlide      documentSlide;

  public DocumentView(@NonNull Context context) {
    this(context, null);
  }

  public DocumentView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DocumentView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    inflate(context, R.layout.document_view, this);

    this.container          =                   findViewById(R.id.document_container);
    this.controlToggle      = (AnimatingToggle) findViewById(R.id.control_toggle);
    this.downloadButton     = (ImageView)       findViewById(R.id.download);
    this.downloadProgress   = (ProgressWheel)   findViewById(R.id.download_progress);
    this.fileName           = (TextView)        findViewById(R.id.file_name);
    this.fileSize           = (TextView)        findViewById(R.id.file_size);
    this.documentBackground =                   findViewById(R.id.document_background);
    this.document           = (TextView)        findViewById(R.id.document);

    if (attrs != null) {
      TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DocumentView, 0, 0);
      setTint(typedArray.getColor(R.styleable.DocumentView_documentForegroundTintColor, Color.WHITE),
          typedArray.getColor(R.styleable.DocumentView_documentBackgroundTintColor, Color.WHITE));
      container.setBackgroundColor(typedArray.getColor(R.styleable.DocumentView_documentWidgetBackground, Color.TRANSPARENT));
      typedArray.recycle();
    }
  }

  public void setDownloadClickListener(@Nullable SlideClickListener listener) {
    this.downloadListener = listener;
  }

  public void setDocumentClickListener(@Nullable SlideClickListener listener) {
    this.viewListener = listener;
  }

  public void setDocument(final @NonNull DocumentSlide documentSlide, String fileName)
  {
    this.documentSlide = documentSlide;
    this.fileName.setText(fileName);
    this.fileSize.setText(Util.getPrettyFileSize(documentSlide.getFileSize()));
    this.document.setText(getFileType(documentSlide.getFileName()));
    Log.w(TAG, "Pending: " + documentSlide.isPendingDownload() + " Type: " + documentSlide.getContentType() + " Size: " + documentSlide.getFileSize() + " Name: " + fileName);

    if (documentSlide.isPendingDownload()) {
      controlToggle.displayQuick(downloadButton);
      downloadButton.setOnClickListener(new DownloadClickedListener(documentSlide));
      fileSize.setText("Download");
      if (downloadProgress.isSpinning()) downloadProgress.stopSpinning();
    } else if (documentSlide.getTransferState() == AttachmentDatabase.TRANSFER_PROGRESS_STARTED) {
      if (documentSlide.getFileSize() > 0) {
        setTint(getResources().getColor(R.color.grey_500), getResources().getColor(R.color.blue_500));
        controlToggle.display(documentBackground);
        if (downloadProgress.isSpinning()) downloadProgress.stopSpinning();
      } else {
        controlToggle.displayQuick(downloadProgress);
        downloadProgress.spin();
      }
    } else {
      controlToggle.displayQuick(documentBackground);
      if (downloadProgress.isSpinning()) downloadProgress.stopSpinning();
    }

    this.setOnClickListener(new OpenClickedListener(documentSlide));
  }

  public void setTint(int foregroundTint, int backgroundTint) {
    DrawableCompat.setTint(this.document.getBackground(), backgroundTint);
    DrawableCompat.setTint(this.documentBackground.getBackground(), foregroundTint);

    this.document.setTextColor(foregroundTint);
    this.fileName.setTextColor(foregroundTint);
    this.fileSize.setTextColor(foregroundTint);

    this.downloadButton.setColorFilter(foregroundTint);
    this.downloadProgress.setBarColor(foregroundTint);
  }

  @Override
  public void setFocusable(boolean focusable) {
    super.setFocusable(focusable);
    this.downloadButton.setFocusable(focusable);
  }

  @Override
  public void setClickable(boolean clickable) {
    super.setClickable(clickable);
    this.downloadButton.setClickable(clickable);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    this.downloadButton.setEnabled(enabled);
  }

  private @NonNull String getFileType(Optional<String> fileName) {
    if (!fileName.isPresent()) return "";

    String[] parts = fileName.get().split("\\.");

    if (parts.length < 2) {
      return "";
    }

    String suffix = parts[parts.length - 1];

    if (suffix.length() <= 3) {
      return suffix;
    }

    return "";
  }

//  @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
  @SuppressWarnings("unused")
  public void onEventAsync(final PartProgressEvent event) {
    if (documentSlide != null && event.attachment.equals(this.documentSlide.asAttachment())) {
      Util.runOnMain(new Runnable() {
        @Override
        public void run() {
          downloadProgress.setInstantProgress(((float) event.progress) / event.total);
        }
      });
    }
  }

  private class DownloadClickedListener implements View.OnClickListener {
    private final @NonNull DocumentSlide slide;

    private DownloadClickedListener(@NonNull DocumentSlide slide) {
      this.slide = slide;
    }

    @Override
    public void onClick(View v) {
      if (downloadListener != null) downloadListener.onClick(v, slide);
    }
  }

  private class OpenClickedListener implements View.OnClickListener {
    private final @NonNull DocumentSlide slide;

    private OpenClickedListener(@NonNull DocumentSlide slide) {
      this.slide = slide;
    }

    @Override
    public void onClick(View v) {
      if (!slide.isPendingDownload() && !slide.isInProgress() && viewListener != null) {
        viewListener.onClick(v, slide);
      }
    }
  }
}