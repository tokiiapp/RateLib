package com.rate.ratedialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.RotationRatingBar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ahulr on 24-10-2016.
 */

public class RatingDialog extends AppCompatDialog implements RatingBar.OnRatingBarChangeListener, View.OnClickListener {

    private static final String SESSION_COUNT = "session_count";
    private static final String RATE5 = "rate_5";
    private static final String RATED = "rated";
    private static final String DATE_FIRST = "date_count";
    private static final String SHOW_NEVER = "show_never";

    /** Follow the device / app night mode. */
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    private String MyPrefs = "RatingDialog";
    private SharedPreferences sharedpreferences;
    private Activity context;
    private Context uiContext;
    private Builder builder;
    private ImageView ivEmoji;
    private TextView tvTitle, tvContent, tvNegative, tvPositive;
    private RotationRatingBar ratingBar;
    private LinearLayout ratingButtons;
    Button btnRate;
    TextView btnLate;
    private float threshold;
    private int session;
    private int date;
    private boolean ignoreRated = false;
    private boolean thresholdPassed = true;
    public int starnumber = 0;
    private int accentColor;

    public RatingDialog(Activity context, Builder builder) {
        super(context);
        this.context = context;
        this.builder = builder;

        this.session = builder.session;
        this.threshold = builder.threshold;
        this.date = builder.date;
        this.ignoreRated = builder.ignoreRated;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        uiContext = RateUi.themedContext(context, builder.theme);
        setContentView(LayoutInflater.from(uiContext).inflate(R.layout.dialog_rating, null));

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int width = Math.min((int) (screenWidth * 0.88f), RateUi.dp(context, 360));
        getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        ivEmoji = findViewById(R.id.dialog_rating_emoji);
        tvTitle = findViewById(R.id.dialog_rating_title);
        tvContent = (TextView) findViewById(R.id.dialog_rating_content);
        tvNegative = (TextView) findViewById(R.id.dialog_rating_button_negative);
        tvPositive = (TextView) findViewById(R.id.dialog_rating_button_positive);
        ratingBar = (RotationRatingBar) findViewById(R.id.dialog_rating_rating_bar);
        ratingButtons = (LinearLayout) findViewById(R.id.dialog_rating_buttons);
        btnRate = findViewById(R.id.btnRate);
        btnLate = findViewById(R.id.btnLate);
        init();
    }

    private void init() {

        accentColor = RateUi.resolveColor(uiContext, builder.ratingBarBackgroundColor, R.color.rate_primary);
        btnRate.setBackground(RateUi.filledButton(context, accentColor));
        if (builder.ratingBarBackgroundColor != 0) {
            GradientDrawable emojiBg = (GradientDrawable) ((View) ivEmoji.getParent()).getBackground().mutate();
            emojiBg.setColor(ColorUtils.setAlphaComponent(accentColor, 36));
        }
        setRateEnabled(false);
        ivEmoji.setImageResource(builder.emojis[3]);

        if (builder.isShowLate) {
            btnLate.setVisibility(View.VISIBLE);
        } else {
            btnLate.setVisibility(View.GONE);
        }

        if (builder.btnLate == null || builder.btnLate.equals("")) {
            btnLate.setText(R.string.rating_dialog_maybe_later);
        } else {
            btnLate.setText(builder.btnLate);
        }

        btnLate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder.maybeLaterCallback != null) {
                    builder.maybeLaterCallback.onClick();
                }
                if (builder.isDismiss) {
                    dismiss();
                }
            }
        });

        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder.rateButtonCallback != null) {
                    builder.rateButtonCallback.onClick(starnumber);
                }
                if (starnumber <= 3) {
                    dismiss();
                    IAReview.getInstance().openDialogFeedback(context, builder.appName, builder.logo, builder.email, starnumber, accentColor, builder.theme);
                } else {
                    dismiss();
                    sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
                    if (sharedpreferences.getBoolean(RATE5, true)) {
                        sharedpreferences.edit().putBoolean(RATE5, false).commit();
                        IAReview.getInstance().showIAReview(context);
                    } else {
                        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        sharedpreferences.edit().putBoolean(RATED, true).commit();
                    }


                }
            }
        });

        if (builder.title != null && !builder.title.isEmpty()) {
            tvTitle.setText(builder.title);
        } else if (builder.appName != null && !builder.appName.isEmpty()) {
            tvTitle.setText(context.getString(R.string.rating_dialog_title_app, builder.appName));
        } else {
            tvTitle.setText(R.string.rating_dialog_title_default);
        }
        if (builder.titleTextColor != 0) {
            tvTitle.setTextColor(ContextCompat.getColor(uiContext, builder.titleTextColor));
        }

        tvContent.setText(R.string.rating_dialog_content_default);
        tvPositive.setText(builder.positiveText);
        tvNegative.setText(builder.negativeText);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        int color = typedValue.data;

        tvPositive.setTextColor(builder.positiveTextColor != 0 ? ContextCompat.getColor(context, builder.positiveTextColor) : color);


        if (builder.positiveBackgroundColor != 0) {
            tvPositive.setBackgroundResource(builder.positiveBackgroundColor);

        }
        if (builder.negativeBackgroundColor != 0) {
            tvNegative.setBackgroundResource(builder.negativeBackgroundColor);
        }

        ratingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating, boolean fromUser) {
                int b = Math.round(rating);
                if (b == starnumber) {
                    return;
                }
                starnumber = b;
                switch (b) {
                    case 1:
                        updateRatingState(builder.emojis[0], R.string.rating_dialog_content_1, R.string.rating_dialog_send_feedback);
                        break;
                    case 2:
                        updateRatingState(builder.emojis[1], R.string.rating_dialog_content_2, R.string.rating_dialog_send_feedback);
                        break;
                    case 3:
                        updateRatingState(builder.emojis[2], R.string.rating_dialog_content_3, R.string.rating_dialog_send_feedback);
                        break;
                    case 4:
                        updateRatingState(builder.emojis[3], R.string.rating_dialog_content_4, R.string.rating_dialog_rate_now);
                        break;
                    case 5:
                        updateRatingState(builder.emojis[4], R.string.rating_dialog_content_5, R.string.rating_dialog_rate_now);
                        break;
                    default:
                        updateRatingState(builder.emojis[3], R.string.rating_dialog_content_default, R.string.rating_dialog_rate);
                        break;
                }
                setRateEnabled(b > 0);
            }
        });
        tvPositive.setOnClickListener(this);
        tvNegative.setOnClickListener(this);

        if (session == 1) {
            tvNegative.setVisibility(View.GONE);
        }
    }

    private void updateRatingState(final int emojiRes, int contentRes, int buttonRes) {
        ivEmoji.animate().cancel();
        ivEmoji.animate().alpha(0f).setDuration(90).withEndAction(new Runnable() {
            @Override
            public void run() {
                ivEmoji.setImageResource(emojiRes);
                ivEmoji.animate().alpha(1f).setDuration(160).start();
            }
        }).start();
        tvContent.setText(contentRes);
        btnRate.setText(buttonRes);
    }

    private void setRateEnabled(boolean enabled) {
        btnRate.setEnabled(enabled);
        btnRate.animate().alpha(enabled ? 1f : 0.45f).setDuration(150).start();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.dialog_rating_button_negative) {

            dismiss();
            showNever();

        } else if (view.getId() == R.id.dialog_rating_button_positive) {

            dismiss();

        }

    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {


        if (ratingBar.getRating() >= threshold) {
            thresholdPassed = true;

            if (builder.ratingThresholdClearedListener == null) {
                setRatingThresholdClearedListener();
            }
            builder.ratingThresholdClearedListener.onThresholdCleared(this, ratingBar.getRating(), thresholdPassed);

        } else {
            thresholdPassed = false;

            if (builder.ratingThresholdFailedListener == null) {
                setRatingThresholdFailedListener();
            }
            builder.ratingThresholdFailedListener.onThresholdFailed(this, ratingBar.getRating(), thresholdPassed);
        }

        if (builder.ratingDialogListener != null) {
            builder.ratingDialogListener.onRatingSelected(ratingBar.getRating(), thresholdPassed);
        }
        showNever();
    }

    private void setRatingThresholdClearedListener() {
        builder.ratingThresholdClearedListener = new Builder.RatingThresholdClearedListener() {
            @Override
            public void onThresholdCleared(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                openPlaystore(context);
                dismiss();
            }
        };
    }

    private void setRatingThresholdFailedListener() {
        builder.ratingThresholdFailedListener = new Builder.RatingThresholdFailedListener() {
            @Override
            public void onThresholdFailed(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                openForm();
            }
        };
    }

    private void openForm() {
        ratingButtons.setVisibility(View.GONE);
        ratingBar.setVisibility(View.GONE);
    }

    private void openPlaystore(Context context) {
        final Uri marketUri = Uri.parse(builder.playstoreUrl);
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, marketUri));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "Couldn't find PlayStore on this device", Toast.LENGTH_SHORT).show();
        }
    }


    public TextView getPositiveButtonTextView() {
        return tvPositive;
    }

    public TextView getNegativeButtonTextView() {
        return tvNegative;
    }



    public RotationRatingBar getRatingBarView() {
        return ratingBar;
    }

    @Override
    public void show() {
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
        boolean isRated = sharedpreferences.getBoolean(RATED, false);
        if ((checkIfSessionMatches(session) && (!isRated)) || ignoreRated) {
            super.show();
        }
    }

    int differenceDate = 0;

    private boolean checkIfSessionMatches(int session) {
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);


        String currentTime = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());

        if (sharedpreferences.getBoolean("firstrun", true)) {
            sharedpreferences.edit().putBoolean("firstrun", false).commit();

            if (currentTime != null) {
                sharedpreferences.edit().putString(DATE_FIRST, String.valueOf(currentTime)).commit();
            }
        }

        String currentDate = sharedpreferences.getString(DATE_FIRST, String.valueOf(currentTime));
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

        Date startDate = null;
        try {
            startDate = df.parse(currentDate);
//            startDate = df.parse("01/09/2021");
            String newDateString = df.format(startDate);
            System.out.println(newDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date endDate = null;
        try {
            endDate = df.parse(String.valueOf(currentTime));
//            endDate = df.parse(String.valueOf("03/09/2021"));
            String newDateString = df.format(endDate);
            System.out.println(newDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate != null && endDate != null) {
            differenceDate =
                    ((int) ((endDate.getTime() / (24 * 60 * 60 * 1000))
                            - (int) (startDate.getTime() / (24 * 60 * 60 * 1000))));
        }

        if (session == 1) {
            return true;
        }


        if (sharedpreferences.getBoolean(SHOW_NEVER, false)) {
            return false;
        }

        int count = sharedpreferences.getInt(SESSION_COUNT, 1);

        if (session == count && differenceDate >= date) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SESSION_COUNT, 1);
            editor.commit();
            return true;
        } else if (session > count) {
            count++;
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SESSION_COUNT, count);
            editor.commit();
            return false;
        } else {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SESSION_COUNT, 2);
            editor.commit();
            return false;
        }
    }

    private void showNever() {
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(SHOW_NEVER, true);
        editor.commit();
    }

    public static class Builder {

        private final Activity context;
        private boolean isShowLate;
        private boolean isDismiss;
        private int logo;
        private String appName;
        private String email;
        private MaybeLaterCallback maybeLaterCallback;
        private RateButtonCallback rateButtonCallback;

        private String title, positiveText, negativeText, playstoreUrl, btnLate;
        private int positiveTextColor, negativeTextColor, titleTextColor, ratingBarColor, ratingBarBackgroundColor, feedBackTextColor;
        private int positiveBackgroundColor, negativeBackgroundColor;
        private RatingThresholdClearedListener ratingThresholdClearedListener;
        private RatingThresholdFailedListener ratingThresholdFailedListener;
        private RatingDialogFormListener ratingDialogFormListener;
        private RatingDialogListener ratingDialogListener;
        private Drawable drawable;

        private int session = 1;
        private float threshold = 1;
        private int date = 1;
        private boolean ignoreRated = true;
        private int theme = THEME_SYSTEM;
        private int[] emojis = {
                R.drawable.rate_emoji_1, R.drawable.rate_emoji_2, R.drawable.rate_emoji_3,
                R.drawable.rate_emoji_4, R.drawable.rate_emoji_5
        };

        public interface RatingThresholdClearedListener {
            void onThresholdCleared(RatingDialog ratingDialog, float rating, boolean thresholdCleared);
        }

        public interface RatingThresholdFailedListener {
            void onThresholdFailed(RatingDialog ratingDialog, float rating, boolean thresholdCleared);
        }

        public interface RatingDialogFormListener {
            void onFormSubmitted(String feedback);
        }

        public interface RatingDialogListener {
            void onRatingSelected(float rating, boolean thresholdCleared);
        }

        public Builder(Activity context) {
            this.context = context;
            // Set default PlayStore URL
            this.playstoreUrl = "market://details?id=" + context.getPackageName();
            initText();
        }

        private void initText() {
            positiveText = context.getString(R.string.rating_dialog_maybe_later);
            negativeText = context.getString(R.string.rating_dialog_never);
        }

        public Builder session(int session) {
            this.session = session;
            return this;
        }

        public Builder date(int date) {
            if (date > 0) {
                date = date - 1;
            }
            this.date = date;
            return this;
        }

        public Builder threshold(float threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder ignoreRated(boolean ignoreRated) {
            this.ignoreRated = ignoreRated;
            return this;
        }
        /*public Builder icon(int icon) {
            this.icon = icon;
            return this;
        }*/

        public Builder icon(Drawable drawable) {
            this.drawable = drawable;
            return this;
        }

        public Builder positiveButtonText(String positiveText) {
            this.positiveText = positiveText;
            return this;
        }

        public Builder negativeButtonText(String negativeText) {
            this.negativeText = negativeText;
            return this;
        }

        public Builder titleTextColor(int titleTextColor) {
            this.titleTextColor = titleTextColor;
            return this;
        }

        public Builder positiveButtonTextColor(int positiveTextColor) {
            this.positiveTextColor = positiveTextColor;
            return this;
        }

        public Builder negativeButtonTextColor(int negativeTextColor) {
            this.negativeTextColor = negativeTextColor;
            return this;
        }

        public Builder positiveButtonBackgroundColor(int positiveBackgroundColor) {
            this.positiveBackgroundColor = positiveBackgroundColor;
            return this;
        }

        public Builder negativeButtonBackgroundColor(int negativeBackgroundColor) {
            this.negativeBackgroundColor = negativeBackgroundColor;
            return this;
        }

        public Builder onThresholdCleared(RatingThresholdClearedListener ratingThresholdClearedListener) {
            this.ratingThresholdClearedListener = ratingThresholdClearedListener;
            return this;
        }

        public Builder onThresholdFailed(RatingThresholdFailedListener ratingThresholdFailedListener) {
            this.ratingThresholdFailedListener = ratingThresholdFailedListener;
            return this;
        }

        public Builder onRatingChanged(RatingDialogListener ratingDialogListener) {
            this.ratingDialogListener = ratingDialogListener;
            return this;
        }

        public Builder onRatingBarFormSumbit(RatingDialogFormListener ratingDialogFormListener) {
            this.ratingDialogFormListener = ratingDialogFormListener;
            return this;
        }

        public Builder isShowButtonLater(boolean isShowLate) {
            this.isShowLate = isShowLate;
            return this;
        }

        public Builder isClickLaterDismiss(boolean isDismiss) {
            this.isDismiss = isDismiss;
            return this;
        }

        public Builder setNameApp(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setOnlickMaybeLate(MaybeLaterCallback onlickMaybeLate) {
            this.maybeLaterCallback = onlickMaybeLate;
            return this;
        }

        public Builder setOnlickRate(RateButtonCallback onlickRate) {
            this.rateButtonCallback = onlickRate;
            return this;
        }

        public Builder setIcon(int logo) {
            this.logo = logo;
            return this;
        }

        public Builder setTextButtonLater(String s) {
            this.btnLate = s;
            return this;
        }

        public Builder ratingBarColor(int ratingBarColor) {
            this.ratingBarColor = ratingBarColor;
            return this;
        }

        public Builder ratingButtonColor(int ratingBarBackgroundColor) {
            this.ratingBarBackgroundColor = ratingBarBackgroundColor;
            return this;
        }

        public Builder feedbackTextColor(int feedBackTextColor) {
            this.feedBackTextColor = feedBackTextColor;
            return this;
        }

        /**
         * @param theme {@link #THEME_SYSTEM} (default), {@link #THEME_LIGHT} or {@link #THEME_DARK}
         */
        public Builder theme(int theme) {
            this.theme = theme;
            return this;
        }

        /**
         * Replaces the face shown above the stars: pass exactly 5 drawables, from 1 star to 5 stars.
         */
        public Builder emojiDrawables(int... drawables) {
            if (drawables == null || drawables.length != 5) {
                throw new IllegalArgumentException("emojiDrawables requires exactly 5 drawables");
            }
            this.emojis = drawables.clone();
            return this;
        }

        public Builder playstoreUrl(String playstoreUrl) {
            this.playstoreUrl = playstoreUrl;
            return this;
        }

        public RatingDialog build() {
            return new RatingDialog(context, this);
        }
    }
}
