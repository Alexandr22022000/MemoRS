package com.redstar.zolotov.memors.Fragments;

/*
Фрагмент картинок
№4
 */

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.redstar.zolotov.memors.Activitys.MainActivity;
import com.redstar.zolotov.memors.Activitys.NotepadActivity;
import com.redstar.zolotov.memors.Activitys.SaveImageActivity;
import com.redstar.zolotov.memors.Objects.Actions;
import com.redstar.zolotov.memors.R;

import java.io.*;
import java.util.ArrayList;

public class ImagesFragment extends Fragment {
    public static final String TAG = "ImagesFragmentTag";

    private boolean isStart = true;
    private int imagesCount;
    private NotepadActivity context;
    private ImageView imageBig;
    private ArrayList<ImageView> imagesArray, imagesArrayDel;
    private LinearLayout layout;
    private ArrayList<LinearLayout> layouts;
    private ArrayList<RelativeLayout> imagesArrayLayouts;
    private ArrayList<Bitmap> bitmaps;
    private Bitmap mainBitmap;
    private ArrayList<Actions> actions = new ArrayList<>();
    private String imageInCamera;
    public ArrayList<String> imagesUri;
    public String mainImage = "";

    /*
    ---Конструктор №1---
    * Раскодирование строки images и получение bitmap
    * Получение bitmap из mainImage
     */
    public ImagesFragment (NotepadActivity context, String mainImage, String images) {
        this.context = context;
        this.mainImage = mainImage;

        bitmaps = new ArrayList<>();
        imagesUri = new ArrayList<>();

        try {
            if (!images.isEmpty()) {
                int start = 0, end;
                while (start != -1) {
                    end = images.indexOf("@", start);
                    imagesUri.add(images.substring(start, (end == -1) ? images.length() : end ));
                    start = (end == -1) ? -1 : (end + 1);
                }

                for (String image : imagesUri) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(image));
                        bitmap = ComperesImage(bitmap, 200);
                        bitmaps.add(bitmap);
                    }
                    catch (FileNotFoundException e) {
                        Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show();
                        bitmaps.add(null);
                    }
                }
            }

            if (!mainImage.isEmpty()) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(mainImage));
                    bitmap = ComperesImage(bitmap, 200);
                    mainBitmap = bitmap;
                } catch (FileNotFoundException e) {
                    Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show();
                    mainBitmap = null;
                }
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 410);
        }
    }

    /*
    ---При создании---
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.images_fragment, null);
    }

    /*
    ---При старте №2---
    * Проверка на первый старт
    * Получение ссылок на view элементы
    * Назначение действий на кнопки
    * Запуск обновления
     */
    @Override
    public void onStart() {
        super.onStart();

        if (!isStart) {
            return;
        }
        isStart = false;

        try {
            layout = (LinearLayout) getActivity().findViewById(R.id.layout_main);
            layouts = new ArrayList<>();
            imagesArray = new ArrayList<>();
            imagesArrayDel = new ArrayList<>();
            imagesArrayLayouts = new ArrayList<>();
            imagesArray.add((ImageView) getActivity().findViewById(R.id.image0));
            imagesArray.add((ImageView) getActivity().findViewById(R.id.image1));
            imagesArrayDel.add((ImageView) getActivity().findViewById(R.id.image0_del));
            imagesArrayDel.add((ImageView) getActivity().findViewById(R.id.image1_del));
            imagesArrayLayouts.add((RelativeLayout) getActivity().findViewById(R.id.image0_layout));
            imagesArrayLayouts.add((RelativeLayout) getActivity().findViewById(R.id.image1_layout));
            layouts.add((LinearLayout) getActivity().findViewById(R.id.layout0));
            imageBig = (ImageView) getActivity().findViewById(R.id.image_big);

            imagesArray.get(0).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mainImage.isEmpty()) {
                        OpenImageIntent(true);
                    }
                    else {
                        OpenBigImage(0);
                    }
                }
            });

            imagesArray.get(1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (imagesCount == 0) {
                        OpenImageIntent(false);
                    }
                    else {
                        OpenBigImage(1);
                    }
                }
            });

            imagesArrayDel.get(0).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainImage = "";
                    Update(-1);
                }
            });

            imagesArrayDel.get(1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DelImage(1);
                    Update(-1);
                }
            });

            imageBig.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageBig.setVisibility(-1);
                }
            });
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 420);
        }

        Update(-1);
    }

    /*
    ---При уничтожении Activity №3---
    * Запуск BackUp в Actions
    * Очистка Actions
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Actions action : actions) {
            action.BackUp();
        }
        actions = null;
    }

    /*
    ---Обработка ответов на запросы №4---
    * Проверка на успешность запроса
    * Если код 0: работа с галереей, вывод в Main
    * Если код 1: работа с галереей, вывод в Array
    * Если код 2: работа с проводником, вывод в Main
    * Если код 3: работа с проводником, вывод в Array
    * Если код 4: работа с камерой, вывод в Main
    * Если код 5: работа с камерой, вывод в Array
    * Если код 6: работа с сохраненками, вывод в Main
    * Если код 7: работа с сохраненками, вывод в Array
    * Запуск обновления
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case 0:
            case 1:
            case 2:
            case 3:
                FolderOrGalleryResult(data, requestCode);
                break;

            case 4:
                CameraResult(true);
                break;

            case 5:
                CameraResult(false);
                break;

            case 6:
                UseSaveImage(data, true);
                break;

            case 7:
                UseSaveImage(data, false);
                break;
        }
        Update(-1);
    }

    /*
    ---Выполнение Actions (при Save в Activity) №5---
     */
    public void Save () {
        for (Actions action : actions) {
            action.Save(context, this);
        }
        actions = new ArrayList<>();
    }

    /*
    ---Удаление картинки №7---
    * Удаление ее Bitmap
    * Удаление ссылки из ArrayList
     */
    private void DelImage (int id) {
        try {
            imagesUri.remove(id - 1);
            bitmaps.remove(id - 1);
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 470);
        }
    }

    /*
    ---Открытие большой картинки №8---
    * Загрузка Bitmap
    * Запуск сжатия до 1200
    * Открытие большого ImageView
     */
    private void OpenBigImage (int id) {
        try {
            Bitmap bitmap;

            if (id == 0) {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(mainImage));
            }
            else {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imagesUri.get(id - 1)));
            }

            bitmap = ComperesImage(bitmap, 1200);

            if (bitmap != null) {
                imageBig.setVisibility(1);
                imageBig.setImageBitmap(bitmap);
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(getContext(), e, 480);
        }
    }

    /*
    ---Генерация картинки №9---
    * Генерация View объектов
    * Генерация id
    * Привязка объектов
    * Привязка действий при нажатии
    * Установка размеров и выравнивания
     */
    private void AddImage (int id, LinearLayout layout) {
        try {
            id = id + 2;
            RelativeLayout imageLayout = new RelativeLayout(context);
            ImageView image = new ImageView(context), imageDel = new ImageView(context);

            imageLayout.setId(100000 + id);
            image.setId(101000 + id);
            imageDel.setId(102000 + id);

            imageLayout.addView(image);
            imageLayout.addView(imageDel);
            layout.addView(imageLayout);

            imagesArray.add(image);
            imagesArrayDel.add(imageDel);
            imagesArrayLayouts.add(imageLayout);

            image.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View view) {
                    if (imagesCount == view.getId() - 101000 - 1) {
                        OpenImageIntent(false);
                    }
                    else {
                        OpenBigImage(view.getId() - 101000);
                    }
                }
            });

            imageDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DelImage(view.getId() - 102000);
                    Update(-1);
                }
            });

            imageDel.setImageResource(R.mipmap.del);

            float dp = this.getResources().getDisplayMetrics().density / 2;

            image.getLayoutParams().height = (int)(220 * dp);
            image.getLayoutParams().width = (int)(220 * dp);
            imageDel.getLayoutParams().height = (int)(100 * dp);
            imageDel.getLayoutParams().width = (int)(100 * dp);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageDel.getLayoutParams();
            params.leftMargin = (int)(120 * dp);
            imageDel.setLayoutParams(params);
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 490);
        }
    }

    /*
    ---Открытие окна для выбора способа добовления картинки №10---
    * Если старый (<19) Android - только галерея
    * 1 - запрос к камере (для android 5 и выше создание файла через fileProvider)
    * 2 - запрос к галереи
    * 3 - запрос в проводник
    * 4 - запрос в сохраненки
     */
    private void OpenImageIntent (final boolean isMain) {
        try {
            if (Build.VERSION.SDK_INT > 22) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            }

            if (Build.VERSION.SDK_INT < 19) {
                Intent intent1 = new Intent(Intent.ACTION_PICK);
                intent1.setType("image/*");
                startActivityForResult(intent1, isMain ? 0 : 1);
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder
                        .setItems(new String[]{"Камера", "Галерея", "Проводник", "Сохраненые"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        try {
                                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                            if (Build.VERSION.SDK_INT < 21) {
                                                File image = File.createTempFile(NotepadActivity.CreateImageName(), ".jpeg", context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                                                imageInCamera = image.getAbsolutePath();
                                                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                                            }
                                            else {
                                                File image = new File(context.getFilesDir(), "default_image.jpeg");
                                                imageInCamera = image.getAbsolutePath();
                                                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(context, "com.redstar.zolotov.memors.fileprovider", image));
                                            }

                                            startActivityForResult(intent, (isMain ? 0 : 1) + 4);
                                        } catch (Exception e) {
                                            MainActivity.ErrorOutput(getContext(), e, 4101);
                                        }
                                        break;

                                    case 1:
                                        try {
                                            Intent intent1 = new Intent(Intent.ACTION_PICK);
                                            intent1.setType("image/*");
                                            startActivityForResult(intent1, (isMain ? 0 : 1));
                                        }
                                        catch (Exception e) {
                                            MainActivity.ErrorOutput(getContext(), e, 4102);
                                        }
                                        break;

                                    case 2:
                                        try {
                                            Intent intent2 = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                                            intent2.addCategory(Intent.CATEGORY_OPENABLE);
                                            intent2.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                            intent2.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                                            intent2.setType("image/*");
                                            startActivityForResult(intent2, (isMain ? 0 : 1) + 2);
                                        }
                                        catch (Exception e) {
                                            MainActivity.ErrorOutput(getContext(), e, 4103);
                                        }
                                        break;

                                    case 3:
                                        try {
                                            Intent intent = new Intent(getContext(), SaveImageActivity.class);
                                            intent.putExtra("StartType", true);
                                            startActivityForResult(intent, (isMain ? 0 : 1) + 6);
                                        }
                                        catch (Exception e) {
                                            MainActivity.ErrorOutput(getContext(), e, 4104);
                                        }
                                        break;
                                }
                            }
                        })
                        .setCancelable(true);

                builder.create().show();
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(getContext(), e, 4100);
        }
    }

    /*
    ---Окно с выбором сохранения картинки №11---
    * Если старый (<19) Android - нечего не днлать
    * Да - создать Action для сохранения, запуск запроса на открытее доступа
    * Нет - Если 1 или 2 (проводник), то сохранение прав доступа, иначе нечего
     */
    private void SaveImage (final Uri uri, final Intent data, final int id) {
        if (Build.VERSION.SDK_INT > 18) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder
                        .setTitle("Создать резервную копию?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Actions action = new Actions(uri, ((id == 0) || (id == 2)) ? -1 : (bitmaps.size() - 1));
                                actions.add(action);
                                VisibilityImage();
                            }
                        })
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (id > 1) {
                                    try {
                                        int takeFlags = data.getFlags();
                                        takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                                    }
                                    catch (Exception e) {
                                        MainActivity.ErrorOutput(context, e, 4111);
                                    }
                                }
                            }
                        })
                        .setCancelable(false);
                builder.create().show();
            }
            catch (Exception e) {
                MainActivity.ErrorOutput(context, e, 4110);
            }
        }
    }

    /*
    ---Открытиее доступа к сохраненой  картинки №12---
    * Да  - ничего
    * Нет - Установка последнего Action на isOpen - true
     */
    private void VisibilityImage () {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder
                    .setTitle("Скрыть файл?")
                    .setPositiveButton("Да", null)
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            actions.get(actions.size() - 1).isOpen = true;
                        }
                    })
                    .setCancelable(false);
            builder.create().show();
        }
        catch (Exception e){
            MainActivity.ErrorOutput(context, e, 4120);
        }
    }

    /*
    ---Полученние картинки из проводника или галереи №13---
     */
    private void FolderOrGalleryResult (Intent data, int type) {
        try {
            Uri uri = data.getData();

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            bitmap = ComperesImage(bitmap, 200);

            if ((type == 0) || (type == 2)) {
                mainBitmap = bitmap;
                mainImage = uri.toString();
            }
            else {
                bitmaps.add(bitmap);
                imagesUri.add(uri.toString());
            }

            SaveImage(uri, data, type);
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 4130);
        }
    }

    /*
    ---Получение картинки из камеры №14---
    * получение uri
    * если android меньше 5:
    *   получение bitmap обычным способом
    * иначе:
    *   получение bitmap через fileProvider
    *   запись в файл обычным чпособом
    *   получение bitmap обычным способом
    * запись в Action
    * установка bitmap
     */
    private void CameraResult (boolean isMain) {
        try {
            Uri image = Uri.fromFile(new File(imageInCamera));
            Bitmap bitmap;

            if (Build.VERSION.SDK_INT < 21) {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), image);
            }
            else {
                File imageFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),NotepadActivity.CreateImageName());
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), image);

                FileOutputStream streamsave = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, streamsave);
                streamsave.close();

                imageInCamera = imageFile.getAbsolutePath();
                image = Uri.fromFile(imageFile);
            }

            Actions action = new Actions(imageInCamera, 0);
            actions.add(action);

            bitmap = ComperesImage(bitmap, 200);

            if (isMain) {
                mainBitmap = bitmap;
                mainImage = image.toString();
            }
            else {
                bitmaps.add(bitmap);
                imagesUri.add(image.toString());
            }

            VisibilityImage();
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 4140);
        }
    }

    /*
    ---Получение сохраненой картинки №15---
     */
    private void UseSaveImage (Intent data, boolean isMain) {
        try {
            String file = data.getStringExtra("image");
            actions.add(new Actions(file, 2));
            Uri image = Uri.fromFile(new File(file));

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), image);
            bitmap = ComperesImage(bitmap, 200);

            if (isMain) {
                mainBitmap = bitmap;
                mainImage = image.toString();
            }
            else {
                bitmaps.add(bitmap);
                imagesUri.add(image.toString());
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 4150);
        }
    }

    /*
    ---Обновление картинок №6---
    * Удаление всех картинок в верхнем ряду, кроме первых двух
    * Удаление всех строк кроме первой
    * Очистка массивов картинок
    * Если Main пуст, то значек добавления, иначе вывести картинку (если картинки нет, то вывести notFound)
    * Если Array пуст, то значек добавления
    * Иначе:
    *   Генерация картинок в первом ряду (и занесение их в массив)
    *   Генерация других рядов и картинок в них (и занесение их в массив)
    *   Вывести картинку в каждый ImageView (если картинки нет, то вывести notFound)
    *   Вывод значка добавления в последний ImageView
     */
    public void Update(int oldFull) {
        try {
            int i = 1;
            for (int i1 = 680; (i1 <= ((oldFull != -1) ? oldFull : context.full)) && (imagesCount >= i); i1 = i1 + 220) {
                i++;
                layouts.get(0).removeView(imagesArrayLayouts.get(i));
            }

            while (1 < layouts.size()) {
                layout.removeView(layouts.get(1));
                layouts.remove(1);
            }

            while (2 < imagesArray.size()) {
                imagesArray.remove(2);
                imagesArrayDel.remove(2);
                imagesArrayLayouts.remove(2);
            }

            if (mainImage.isEmpty()) {
                imagesArray.get(0).setImageResource(R.mipmap.add_image);
                imagesArrayDel.get(0).setVisibility(-1);
            }
            else {
                if (mainBitmap != null) {
                    imagesArray.get(0).setImageBitmap(mainBitmap);
                }
                else {
                    imagesArray.get(0).setImageResource(R.mipmap.image_not_found);
                }
                imagesArrayDel.get(0).setVisibility(1);
            }

            if (imagesUri.size() == 0) {
                imagesArray.get(1).setImageResource(R.mipmap.add_image);
                imagesArrayDel.get(1).setVisibility(-1);
                imagesCount = 0;
            }
            else {
                imagesCount = bitmaps.size();

                int id = 0;
                for (int i1 = 680; (i1 < context.full) && (id <= imagesCount - 1); i1 = i1 + 220) {
                    AddImage(id, layouts.get(0));
                    id++;
                }

                while (id <= imagesCount - 1) {
                    layouts.add( new LinearLayout(getContext()));
                    layout.addView(layouts.get(layouts.size() - 1));

                    for (int i1 = 236;  (i1 < context.full) && (id <= imagesCount - 1); i1 = i1 + 220) {
                        AddImage(id, layouts.get(layouts.size() - 1));
                        id++;
                    }
                }

                i = 1;
                for (; i <= bitmaps.size(); i++) {
                    if (bitmaps.get(i - 1) == null) {
                        imagesArray.get(i).setImageResource(R.mipmap.image_not_found);
                    }
                    else {
                        imagesArray.get(i).setImageBitmap(bitmaps.get(i - 1));
                    }
                    imagesArrayDel.get(i).setVisibility(1);
                }

                imagesArray.get(i).setImageResource(R.mipmap.add_image);
                imagesArrayDel.get(i).setVisibility(-1);
                imagesArray.get(i).setVisibility(1);
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 460);
        }
    }

    /*
    ---Сжатие bitmap №0---
     */
    public static Bitmap ComperesImage(Bitmap bitmap, int size) throws Exception {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        float di;
        if (w > h) {
            di = (float)h / w;
            bitmap = Bitmap.createScaledBitmap(bitmap, size, Math.round(size * di), false);
        }
        else {
            di = (float)w / h;
            bitmap = Bitmap.createScaledBitmap(bitmap,  Math.round(size * di), size, false);
        }
        return bitmap;
    }
}
