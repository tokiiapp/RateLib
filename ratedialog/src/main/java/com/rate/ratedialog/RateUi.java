package com.rate.ratedialog;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

final class RateUi {

    private RateUi() {
    }

    static int dp(Context context, float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    /**
     * Accepts either a color resource id (R.color.x) or a raw color int, falling back to
     * {@code fallbackRes} when nothing was set.
     */
    static int resolveColor(Context context, int value, int fallbackRes) {
        if (value == 0) {
            return ContextCompat.getColor(context, fallbackRes);
        }
        try {
            return ContextCompat.getColor(context, value);
        } catch (Resources.NotFoundException e) {
            return value;
        }
    }

    /**
     * Wraps the activity so the dialog's resources resolve with the requested night mode,
     * independent of the rest of the app.
     */
    static Context themedContext(Activity activity, int theme) {
        if (theme != RatingDialog.THEME_LIGHT && theme != RatingDialog.THEME_DARK) {
            return activity;
        }
        int themeRes = 0;
        try {
            themeRes = activity.getPackageManager()
                    .getActivityInfo(activity.getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        if (themeRes == 0) {
            themeRes = androidx.appcompat.R.style.Theme_AppCompat_DayNight;
        }
        Configuration config = new Configuration();
        config.uiMode = theme == RatingDialog.THEME_DARK
                ? Configuration.UI_MODE_NIGHT_YES
                : Configuration.UI_MODE_NIGHT_NO;
        ContextThemeWrapper wrapper = new ContextThemeWrapper(activity, themeRes);
        wrapper.applyOverrideConfiguration(config);
        return wrapper;
    }

    static Drawable filledButton(Context context, int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(dp(context, 14));
        shape.setColor(color);
        return new RippleDrawable(ColorStateList.valueOf(Color.argb(60, 255, 255, 255)), shape, null);
    }
}
