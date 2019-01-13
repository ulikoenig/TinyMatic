package de.ebertp.HomeDroid.Activities;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.widget.SeekBar;

import de.ebertp.HomeDroid.R;

public class ColorPickerActivity extends ThemedDialogActivity {

    protected void onCreate(android.os.Bundle savedInstanceState) {
        setContentView(R.layout.activity_color_picker);

        LinearGradient test = new LinearGradient(0.f, 0.f, 500.f, 0.0f,

                new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
                        0xFF0000FF, 0xFFFF0000},

//                new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF,
//                        0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF},
                null, Shader.TileMode.CLAMP);
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setShader(test);

        SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setProgressDrawable(shape);


        super.onCreate(savedInstanceState);
    }


}
