package com.example.youtubeautoskip;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    static final String PREFS = "settings";
    static final String KEY_EXTRA_LABELS = "extra_labels";

    private TextView status;
    private EditText labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private View buildUi() {
        int pad = dp(24);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, dp(36), pad, pad);

        TextView title = new TextView(this);
        title.setText("YouTube Auto Skip");
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);

        TextView explanation = new TextView(this);
        explanation.setText("This app has no internet permission. Its accessibility service is restricted to YouTube and only tries to press a visible ad-skip control.");
        explanation.setTextSize(16);
        explanation.setPadding(0, dp(12), 0, dp(20));
        root.addView(explanation);

        status = new TextView(this);
        status.setTextSize(18);
        status.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(status);

        Button accessibility = new Button(this);
        accessibility.setText("Open Accessibility Settings");
        accessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = dp(14);
        root.addView(accessibility, buttonParams);

        TextView labelTitle = new TextView(this);
        labelTitle.setText("Optional additional skip labels");
        labelTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        labelTitle.setTextSize(16);
        labelTitle.setPadding(0, dp(28), 0, dp(6));
        root.addView(labelTitle);

        TextView labelHelp = new TextView(this);
        labelHelp.setText("One per line. Useful if your YouTube language uses a label the detector does not recognize. English defaults are built in.");
        labelHelp.setTextSize(14);
        root.addView(labelHelp);

        labels = new EditText(this);
        labels.setMinLines(4);
        labels.setGravity(android.view.Gravity.TOP);
        labels.setHint("Example:\nSkip\nSkip ad");
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        labels.setText(prefs.getString(KEY_EXTRA_LABELS, ""));
        root.addView(labels, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(130)));

        Button save = new Button(this);
        save.setText("Save Labels");
        save.setOnClickListener(v -> {
            prefs.edit().putString(KEY_EXTRA_LABELS, labels.getText().toString()).apply();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        });
        root.addView(save, buttonParams);

        Space spacer = new Space(this);
        root.addView(spacer, new LinearLayout.LayoutParams(1, dp(20)));

        TextView note = new TextView(this);
        note.setText("If YouTube changes its UI, the detector may need an update. The app deliberately avoids coordinate-based tapping so it does not click arbitrary screen locations.");
        note.setTextSize(13);
        root.addView(note);

        return root;
    }

    private void updateStatus() {
        boolean enabled = isServiceEnabled(this, AdSkipAccessibilityService.class);
        status.setText(enabled ? "Status: enabled" : "Status: disabled");
    }

    static boolean isServiceEnabled(Context context, Class<?> serviceClass) {
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (TextUtils.isEmpty(enabledServices)) return false;

        ComponentName target = new ComponentName(context, serviceClass);
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabledServices);
        while (splitter.hasNext()) {
            ComponentName component = ComponentName.unflattenFromString(splitter.next());
            if (target.equals(component)) return true;
        }
        return false;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
