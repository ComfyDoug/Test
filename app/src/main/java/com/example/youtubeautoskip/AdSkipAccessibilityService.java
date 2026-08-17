package com.example.youtubeautoskip;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AdSkipAccessibilityService extends AccessibilityService {
    private static final String YOUTUBE_PACKAGE = "com.google.android.youtube";
    private static final long CLICK_DEBOUNCE_MS = 900L;

    private static final Set<String> DEFAULT_LABELS = new HashSet<>(Arrays.asList(
            "skip ad",
            "skip ads",
            "skip",
            "skip video ad"
    ));

    private long lastClickAt = 0L;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        if (!YOUTUBE_PACKAGE.contentEquals(event.getPackageName())) return;
        if (SystemClock.uptimeMillis() - lastClickAt < CLICK_DEBOUNCE_MS) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        try {
            AccessibilityNodeInfo candidate = findSkipNode(root);
            if (candidate == null) return;

            AccessibilityNodeInfo clickable = findClickableAncestor(candidate);
            if (clickable != null && clickable.isVisibleToUser() && clickable.isEnabled()) {
                boolean clicked = clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                if (clicked) lastClickAt = SystemClock.uptimeMillis();
            }
        } finally {
            root.recycle();
        }
    }

    private AccessibilityNodeInfo findSkipNode(AccessibilityNodeInfo root) {
        Set<String> labels = loadLabels();
        ArrayDeque<AccessibilityNodeInfo> queue = new ArrayDeque<>();
        queue.add(AccessibilityNodeInfo.obtain(root));

        while (!queue.isEmpty()) {
            AccessibilityNodeInfo node = queue.removeFirst();
            try {
                if (node.isVisibleToUser() && looksLikeSkipControl(node, labels)) {
                    return AccessibilityNodeInfo.obtain(node);
                }

                for (int i = 0; i < node.getChildCount(); i++) {
                    AccessibilityNodeInfo child = node.getChild(i);
                    if (child != null) queue.add(child);
                }
            } finally {
                node.recycle();
            }
        }
        return null;
    }

    private boolean looksLikeSkipControl(AccessibilityNodeInfo node, Set<String> labels) {
        String viewId = normalize(node.getViewIdResourceName());
        String text = normalize(node.getText());
        String description = normalize(node.getContentDescription());

        // Resource IDs are the most language-independent signal when YouTube exposes one.
        if (!viewId.isEmpty() && viewId.contains("skip") &&
                (viewId.contains("ad") || isButtonLike(node))) {
            return true;
        }

        if (labels.contains(text) || labels.contains(description)) {
            return isButtonLike(node) || node.isClickable() || hasClickableAncestor(node);
        }

        // Conservative phrase match for accessibility labels such as "Skip ad, button".
        for (String label : labels) {
            if (label.length() >= 4 &&
                    ((text.startsWith(label + " ") || description.startsWith(label + " ")))) {
                return isButtonLike(node) || node.isClickable() || hasClickableAncestor(node);
            }
        }

        return false;
    }

    private boolean isButtonLike(AccessibilityNodeInfo node) {
        CharSequence className = node.getClassName();
        if (className == null) return false;
        String value = className.toString().toLowerCase(Locale.ROOT);
        return value.contains("button");
    }

    private boolean hasClickableAncestor(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo current = AccessibilityNodeInfo.obtain(node);
        try {
            for (int i = 0; i < 4 && current != null; i++) {
                if (current.isClickable()) return true;
                AccessibilityNodeInfo parent = current.getParent();
                current.recycle();
                current = parent;
            }
            return false;
        } finally {
            if (current != null) current.recycle();
        }
    }

    private AccessibilityNodeInfo findClickableAncestor(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo current = AccessibilityNodeInfo.obtain(node);
        node.recycle();

        for (int depth = 0; depth < 5 && current != null; depth++) {
            if (current.isClickable()) return current;
            AccessibilityNodeInfo parent = current.getParent();
            current.recycle();
            current = parent;
        }
        if (current != null) current.recycle();
        return null;
    }

    private Set<String> loadLabels() {
        Set<String> labels = new HashSet<>(DEFAULT_LABELS);
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        String extra = prefs.getString(MainActivity.KEY_EXTRA_LABELS, "");
        if (extra != null) {
            for (String line : extra.split("\\R")) {
                String normalized = normalize(line);
                if (!normalized.isEmpty()) labels.add(normalized);
            }
        }
        return labels;
    }

    private String normalize(CharSequence value) {
        if (value == null) return "";
        return value.toString()
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    @Override
    public void onInterrupt() {
        // No ongoing feedback to interrupt.
    }
}
