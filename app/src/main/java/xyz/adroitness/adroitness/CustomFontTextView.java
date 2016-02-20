package xyz.adroitness.adroitness;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomFontTextView extends TextView {

    private static Typeface tfs = null;
    private final String defaultTypeFace = "fonts/NotoSans.ttf";
    private Context c;

    public CustomFontTextView(Context c) {
        super(c);
        this.c = c;
        if (tfs == null) {
            tfs = Typeface.createFromAsset(c.getAssets(),
                    defaultTypeFace);
        }
        setTypeface(tfs);

    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.c = context;
        if (tfs == null) {
            tfs = Typeface.createFromAsset(c.getAssets(),
                    defaultTypeFace);
        }
        setTypeface(tfs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.c = context;
        if (tfs == null) {
            tfs = Typeface.createFromAsset(c.getAssets(),
                    defaultTypeFace);
        }
        setTypeface(tfs);
    }

}