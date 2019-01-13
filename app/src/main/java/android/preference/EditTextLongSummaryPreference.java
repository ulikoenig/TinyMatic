package android.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class EditTextLongSummaryPreference extends EditTextPreference {

    public EditTextLongSummaryPreference(Context context) {
        super(context);
    }

    public EditTextLongSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextLongSummaryPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        summaryView.setMaxLines(10);
    }
}
