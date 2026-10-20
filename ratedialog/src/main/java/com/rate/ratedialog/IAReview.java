package com.rate.ratedialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.willy.ratingbar.BaseRatingBar;

public class IAReview {
    private static volatile IAReview INSTANCE;

    public static synchronized IAReview getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new IAReview();
        }
        return INSTANCE;
    }

    ReviewManager manager;
    ReviewInfo reviewInfo;

    public void showIAReview(Activity context) {


        manager = ReviewManagerFactory.create(context);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(context, reviewInfo);
                flow.addOnCompleteListener(task2 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });

            } else {
                // There was some problem, log or handle the error code.
                Toast.makeText(context, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();

                String reviewErrorCode = task.getException().toString();
            }
        });
    }


    void openDialogFeedback(Activity activity, String name, int logo, String email, int starnumber, int accentColor, int theme) {

        // Khởi tạo an toàn: Bỏ R.style.DialogStyle để hệ thống tự động dùng theme tương thích của project hiện tại (AndroidX hoặc Support cũ).
        BottomSheetDialog bottomSheerDialog = new BottomSheetDialog(activity);

        // Workaround: Đặt lại thuộc tính adjustResize bằng code để duy trì hành vi bàn phím đẩy layout lên thay vì dùng file style.xml.
        if (bottomSheerDialog.getWindow() != null) {
            bottomSheerDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        bottomSheerDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // Make the sheet container transparent so the rounded top corners of our layout show through.
                FrameLayout bottomSheet = bottomSheerDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    bottomSheet.setBackgroundColor(Color.TRANSPARENT);
                    BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setSkipCollapsed(true);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        View parentView = LayoutInflater.from(RateUi.themedContext(activity, theme)).inflate(R.layout.dialog_feedback, null);
        bottomSheerDialog.setContentView(parentView);
        bottomSheerDialog.show();
        TextView tvcount = parentView.findViewById(R.id.count);
        ImageView imglogo = parentView.findViewById(R.id.logo);
        TextView tvAppName = parentView.findViewById(R.id.tvAppName);
        EditText body = parentView.findViewById(R.id.body);
        BaseRatingBar dialog_rating_rating_bar = parentView.findViewById(R.id.dialog_rating_rating_bar);
        dialog_rating_rating_bar.setRating(starnumber);
        body.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvcount.setText(String.valueOf((s.length())) + "/500");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (name == null || name.isEmpty()) {
            name = activity.getApplicationInfo().loadLabel(activity.getPackageManager()).toString();
        }
        tvAppName.setText(name);
        imglogo.setClipToOutline(true);
        if (logo != 0) {
            imglogo.setImageDrawable(ContextCompat.getDrawable(activity, logo));
        } else {
            imglogo.setImageDrawable(activity.getApplicationInfo().loadIcon(activity.getPackageManager()));
        }
        final String appName = name;

        Button btnCancel = parentView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheerDialog.dismiss();
            }
        });

        Button btnSubmit = parentView.findViewById(R.id.btnSubmit);
        btnSubmit.setBackground(RateUi.filledButton(activity, accentColor));
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (body.getText().toString().trim().length() <= 0) {
                    body.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake));
                    body.requestFocus();
                    Toast.makeText(activity, R.string.rating_dialog_feedback_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                bottomSheerDialog.dismiss();

                Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
                selectorIntent.setData(Uri.parse("mailto:"));
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback App " + appName);
                emailIntent.putExtra(Intent.EXTRA_TEXT, "" + body.getText().toString());
                emailIntent.setSelector(selectorIntent);
                activity.startActivity(Intent.createChooser(emailIntent, "Send email..."));


//                Intent intent = new Intent(Intent.ACTION_SENDTO);
//                intent.setData(Uri.parse("mailto:"+email)); // only email apps should handle this
//                intent.putExtra(Intent.EXTRA_EMAIL, email);
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback App "+ name);
//                intent.putExtra(Intent.EXTRA_TEXT   , ""+body.getText().toString());
//                if (intent.resolveActivity(activity.getPackageManager()) != null) {
//                    activity.startActivity(intent);
//                }


//                Intent i = new Intent(Intent.ACTION_SENDTO);
//                i.setType("message/rfc822");
//                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{email});
//                i.putExtra(Intent.EXTRA_SUBJECT, "Feedback App "+ name);
//                i.putExtra(Intent.EXTRA_TEXT   , ""+body.getText().toString());
//                try {
//                   activity.startActivity(Intent.createChooser(i, "Send feedback..."));
//                } catch (android.content.ActivityNotFoundException ex) {
//                    Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//                }

            }
        });


//        LayoutInflater inflater = activity.getLayoutInflater();
//
//        View alertLayout = inflater.inflate(R.layout.dialog_feedback, null);
//        androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(activity);
//        alert.setView(alertLayout);
////        alert.setCancelable(false);
//        androidx.appcompat.app.AlertDialog dialog = alert.create();
//
//        Button btnSubmit = alertLayout.findViewById(R.id.btnSubmit);
//        btnSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        dialog.show();
    }
}