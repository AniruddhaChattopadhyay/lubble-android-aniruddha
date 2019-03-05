package in.lubble.app.quiz.roulette;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import github.hellocsl.cursorwheel.CursorWheelLayout;
import in.lubble.app.R;

import java.util.ArrayList;
import java.util.List;

public class RouletteActiv extends AppCompatActivity {

    public static void open(Context context) {
        context.startActivity(new Intent(context, RouletteActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roulette);

        final CursorWheelLayout cursorWheelLayout = findViewById(R.id.test_circle_menu_right);

        String[] res = new String[]{"Beer Cafe", "Simon Says", "BOHO", "Fenny's", "Social", "Beer Cafe", "Tilt", "Gilly's Redefined", "Barleyz", "Maggi"};
        List<MenuItemData> menuItemDatas = new ArrayList<>();
        for (int i = 0; i < res.length; i++) {
            menuItemDatas.add(new MenuItemData(res[i]));
        }

        SimpleTextAdapter simpleTextAdapter = new SimpleTextAdapter(this, menuItemDatas, Gravity.CENTER);
        cursorWheelLayout.setAdapter(simpleTextAdapter);
    }

    public class SimpleTextAdapter extends CursorWheelLayout.CycleWheelAdapter {

        private List<MenuItemData> mMenuItemDatas;
        private LayoutInflater mLayoutInflater;
        private Context mContext;
        public static final int INDEX_SPEC = 9;
        private int mGravity;

        public SimpleTextAdapter(Context context, List<MenuItemData> menuItemDatas) {
            this(context, menuItemDatas, Gravity.CENTER);
        }

        public SimpleTextAdapter(Context context, List<MenuItemData> menuItemDatas, int gravity) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            mMenuItemDatas = menuItemDatas;
            mGravity = gravity;
        }

        @Override
        public int getCount() {
            return mMenuItemDatas == null ? 0 : mMenuItemDatas.size();
        }

        @Override
        public View getView(View parent, int position) {
            MenuItemData item = getItem(position);
            View root = mLayoutInflater.inflate(R.layout.wheel_menu_item, null, false);
            TextView textView = root.findViewById(R.id.wheel_menu_item_tv);
            textView.setVisibility(View.VISIBLE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textView.setText(item.mTitle);
            if (textView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) textView.getLayoutParams()).gravity = mGravity;
            }
            if (position == INDEX_SPEC) {
                textView.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            }
            return root;
        }

        @Override
        public MenuItemData getItem(int position) {
            return mMenuItemDatas.get(position);
        }

    }

}
