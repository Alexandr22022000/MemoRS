package com.redstar.zolotov.memors.Adapters;

/*
Адаптер вывода листа файлов и папок
№5
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.redstar.zolotov.memors.Activitys.MainActivity;
import com.redstar.zolotov.memors.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FolderAdapter extends BaseAdapter {
    private String[] names;
    private int[] ids;
    private boolean[] types;
    private Context context;

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /*
    ---Вывод View №1---
     */
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.folder_item, viewGroup, false);
        }

        TextView buttonOpen = (TextView) view.findViewById(R.id.button_open);
        ImageView buttonDel = (ImageView) view.findViewById(R.id.button_del);
        ImageView typeImage = (ImageView) view.findViewById(R.id.image_type);

        if (types[i]) {
            String image = "";
            try {
                BufferedReader R = new BufferedReader(new InputStreamReader(context.openFileInput(ids[i] + "")));
                R.readLine();
                image = R.readLine();
                R.close();
            }
            catch (Exception e) {
                Toast.makeText(context, "Ошибка открытия файла! " + e.toString(), Toast.LENGTH_SHORT).show();
            }

            if (image.isEmpty()) {
                typeImage.setImageResource(R.mipmap.file);
            }
            else {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(image));
                    int w = bitmap.getWidth(), h = bitmap.getHeight();
                    float di;
                    if (w > h) {
                        di = (float)h / w;
                        bitmap = Bitmap.createScaledBitmap(bitmap, 100, Math.round(100 * di), false);
                    }
                    else {
                        di = (float)w / h;
                        bitmap = Bitmap.createScaledBitmap(bitmap,  Math.round(100 * di), 100, false);
                    }
                    typeImage.setImageBitmap(bitmap);
                }
                catch (Exception e) {
                    MainActivity.ErrorOutput(context, e, 510);
                }
            }
        }
        else {
            typeImage.setImageResource(R.mipmap.folder);
        }


        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (types[i]) {
                    ((MainActivity) context).OpenFile(ids[i]);
                }
                else {
                    ((MainActivity) context).SetFolder(ids[i]);
                }
            }
        });

        typeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (types[i]) {
                    ((MainActivity) context).OpenFile(ids[i]);
                }
                else {
                    ((MainActivity) context).SetFolder(ids[i]);
                }
            }
        });

        buttonDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) context).Del(types[i], ids[i]);
            }
        });

        buttonOpen.setText(names[i]);
        return view;
    }

    public FolderAdapter (Context context, String[] names, int[] ids, boolean[] types) {
        this.context = context;
        this.names = names;
        this.ids = ids;
        this.types = types;
    }
}
