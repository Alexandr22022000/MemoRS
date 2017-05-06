package com.redstar.zolotov.memors.Activitys;

/*
Активность сохраненок
№3
 */

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.redstar.zolotov.memors.Fragments.ImagesFragment;
import com.redstar.zolotov.memors.R;

import java.io.*;
import java.util.ArrayList;

public class SaveImageActivity extends AppCompatActivity {
    private int full;
    private int imagesCount;
    private ImageView bigImage;
    private ArrayList<ImageView> imagesArray, imagesArrayDel;
    private LinearLayout layout;
    private ArrayList<LinearLayout> layouts = new ArrayList<>();
    private ArrayList<RelativeLayout> imagesArrayLayouts;
    private ArrayList<Bitmap> bitmaps;
    private ArrayList<String> imageFiles;
    private ArrayList<Integer> imageCounts;
    private boolean isIntent;
    private String imageInCamera;

    /*
    ---При старте №1---
    * Вычесление размера дисплея
    * Чтение из Main
    * Раскодирование строки в ArrayList
    * Вывод Image на ImageView
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saveimage_layout);

        layout = (LinearLayout) findViewById(R.id.main_layout);
        bigImage = (ImageView) findViewById(R.id.image_big);

        try {
            DisplayMetrics metricsB = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metricsB);
            full = (int) (metricsB.widthPixels / (this.getResources().getDisplayMetrics().density / 2));

            if (savedInstanceState == null) {
                isIntent = getIntent().getBooleanExtra("StartType", false);

                BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MainActivity.MAIN_FILE)));
                R.readLine();
                String line = R.readLine();
                R.close();

                bitmaps = new ArrayList<>();
                imageFiles = new ArrayList<>();
                imageCounts = new ArrayList<>();
                line = (line == null) ? "" : line;
                line = (line.equals("null") ? "" : line);

                if (!line.isEmpty()) {
                    int start = 0, end;
                    String dataFile;
                    while (line.indexOf("|", start) != -1) {
                        end = line.indexOf("~", start + 1);
                        dataFile = line.substring(start + 1, end);

                        imageFiles.add(dataFile.substring(0, dataFile.indexOf("|", 0)));
                        imageCounts.add(Integer.parseInt(dataFile.substring(dataFile.indexOf("|", 0) + 1, dataFile.length())));

                        start = end;
                    }

                    for (String fileName : imageFiles) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(fileName)));
                            bitmap = ImagesFragment.ComperesImage(bitmap, 200);
                            bitmaps.add(bitmap);
                        }
                        catch (FileNotFoundException e) {
                            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show();
                            bitmaps.add(null);
                        }
                    }
                }
            }
            else {
                bitmaps = savedInstanceState.getParcelableArrayList("bitmaps");
                imageFiles = savedInstanceState.getStringArrayList("imageFiles");
                imageCounts = savedInstanceState.getIntegerArrayList("imageCounts");
                isIntent = savedInstanceState.getBoolean("isIntent");
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(this, e, 310);
        }

        Update();
    }

    /*
    ---Обработка ответа на запрос №2---
    * Проверка успешности запроса
    * Коды:
    *   0 - Галерея
    *   1 - Проводник
    *   2 - Камера
    * 0, 1:
    *   Сохранение файла
    *   Получение bitmap
    * 2:
    *   Получение uri
    *   Если android ниже 5:
    *       Получить bitmap обычным способом
    *   Иначе:
    *       Получить bitmap через fileProvider
    *       Сохранить файл обычным способом
    *       Получить bitmap обычным способом
    *
    * Обновление
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        try {
            switch (requestCode) {
                case 0:
                case 1:
                    Uri uri = data.getData();
                    uri = SaveImage(uri);

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    bitmap = ImagesFragment.ComperesImage(bitmap, 200);

                    bitmaps.add(bitmap);
                    VisibilityImage(imageFiles.get(imageFiles.size() - 1));
                    break;

                case 2:
                    Uri image = Uri.fromFile(new File(imageInCamera));
                    Bitmap bitmap2;

                    if (Build.VERSION.SDK_INT < 21) {
                        bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                    }
                    else {
                        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),NotepadActivity.CreateImageName());
                        bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), image);

                        FileOutputStream streamsave = new FileOutputStream(imageFile);
                        bitmap2.compress(Bitmap.CompressFormat.JPEG, 85, streamsave);
                        streamsave.close();

                        imageInCamera = imageFile.getAbsolutePath();
                    }

                    bitmap2 = ImagesFragment.ComperesImage(bitmap2, 200);

                    imageFiles.add(imageInCamera);
                    imageCounts.add(0);
                    bitmaps.add(bitmap2);

                    VisibilityImage(imageInCamera);
                    break;
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(this, e, 320);
        }

        ChangeMainFile();
        Update();
    }

    /*
    ---Создание меню №3---
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.saveimage_menu, menu);
        return true;
    }

    /*
    ---Обработка нажатий в меню №4---
    * Back: завершить работу Activity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_back:
                finish();
                return true;
        }
        return false;
    }

    /*
    ---Сохранение состояния при повороте №5---
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("bitmaps", bitmaps);
        outState.putStringArrayList("imageFiles", imageFiles);
        outState.putIntegerArrayList("imageCounts", imageCounts);
        outState.putBoolean("isIntent", isIntent);
    }

    /*
    ---Обработка нажатий на статичные кнопки №6---
    * BigImage: закрыть BigImage
     */
    public void PressKey (View v) {
        switch (v.getId()) {
            case R.id.image_big:
                bigImage.setVisibility(-1);
                break;
        }
    }

    /*
    ---Обновление №7---
    * Удаление всех строк
    * Очистка всех массивов
    * Генерация картинок
    * Вывод картинок из Bitmaps
    * Если не запрос: Вывод Add в последней картинки
     */
    private void Update() {
        try {
            for (LinearLayout lineLayout : layouts) {
                layout.removeView(lineLayout);
            }

            layouts = new ArrayList<>();
            imagesArray = new ArrayList<>();
            imagesArrayDel = new ArrayList<>();
            imagesArrayLayouts = new ArrayList<>();

            imagesCount = bitmaps.size();
            int id = 0;
            while (id <= ((isIntent) ? (imagesCount - 1) : imagesCount)) {
                layouts.add(new LinearLayout(this));
                layout.addView(layouts.get(layouts.size() - 1));

                for (int i1 = 236;  (i1 < full) && (id <= ((isIntent) ? (imagesCount - 1) : imagesCount)); i1 = i1 + 220) {
                    AddImage(id, layouts.get(layouts.size() - 1));
                    id++;
                }
            }

            for (int i = 0; i < bitmaps.size(); i++) {
                if (bitmaps.get(i) == null) {
                    imagesArray.get(i).setImageResource(R.mipmap.image_not_found);
                }
                else {
                    imagesArray.get(i).setImageBitmap(bitmaps.get(i));
                }

                if (!isIntent) {
                    imagesArrayDel.get(i).setVisibility(1);
                }
            }

            if (!isIntent) {
                imagesArrayDel.get(imagesArrayDel.size() - 1).setVisibility(-1);
                imagesArray.get(imagesArray.size() -1).setImageResource(R.mipmap.add_image);
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(SaveImageActivity.this, e, 370);
        }
    }

    /*
    ---Генерация картинки №8---
    * Генерация View объектов
    * Генерация id
    * Привязка объектов
    * Привязка действий при нажатии
    * Установка размеров и выравнивания
    * Если не запрос, то вышереречисленное для Del
     */
    private void AddImage (int id, LinearLayout layout) {
        try {
            RelativeLayout imageLayout = new RelativeLayout(this);
            ImageView image = new ImageView(this), imageDel = new ImageView(this);

            imageLayout.setId(100000 + id);
            image.setId(101000 + id);

            imageLayout.addView(image);
            layout.addView(imageLayout);

            imagesArray.add(image);
            imagesArrayLayouts.add(imageLayout);

            image.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View view) {
                    if (isIntent) {
                        Intent intent = new Intent();
                        intent.putExtra("image", imageFiles.get(view.getId() - 101000));    //ответ на запрос
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    else {
                        if (imagesCount == view.getId() - 101000) {
                            OpenImageIntent();
                        }
                        else {
                            OpenBigImage(view.getId() - 101000);
                        }
                    }
                }
            });

            float dp = this.getResources().getDisplayMetrics().density / 2;

            image.getLayoutParams().height = (int)(220 * dp);
            image.getLayoutParams().width = (int)(220 * dp);

            if (isIntent) {
                imagesArrayDel.add(null);
            }
            else {
                imageDel.setId(102000 + id);
                imageLayout.addView(imageDel);
                imagesArrayDel.add(imageDel);

                imageDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SaveImageActivity.this);
                        builder
                                .setTitle("Вы уверены?")
                                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DelFile(view.getId() - 102000);
                                    }
                                })
                                .setNegativeButton("Нет", null)
                                .setCancelable(true);

                        builder.create().show();
                    }
                });

                imageDel.setImageResource(R.mipmap.del);
                imageDel.getLayoutParams().height = (int)(100 * dp);
                imageDel.getLayoutParams().width = (int)(100 * dp);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageDel.getLayoutParams();
                params.leftMargin = (int)(120 * dp);
                imageDel.setLayoutParams(params);
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(SaveImageActivity.this, e, 380);
        }
    }

    /*
    ---Удаление файла №9---
    * Удаление самого файла
    * Удаление ссылок на него
    * Запуск записи изменений в Main
    * Вызов обновления
     */
    private void DelFile (int id) {
        try {
            new File(imageFiles.get(id)).delete();

            imageFiles.remove(id);
            bitmaps.remove(id);
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(SaveImageActivity.this, e, 390);
        }

        ChangeMainFile();
        Update();
    }

    /*
    ---Открытие большой картинки №10---
     */
    private void OpenBigImage (int id) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(imageFiles.get(id))));
            bitmap = ImagesFragment.ComperesImage(bitmap, 1200);
            bigImage.setImageBitmap(bitmap);
            bigImage.setVisibility(1);
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(SaveImageActivity.this, e, 3100);
        }
    }

    /*
    ---Вывод окна выбора источника картинки №11---
    * Если "Камера": создание файла записи (для версий android 5 и выше через fileProvider), запуск камеры (код 2)
    * Если "Галерея": запуск галереи (код 0)
    * Если "Проводник": запуск проводника (код 1)
     */
    private void OpenImageIntent () {
        if (Build.VERSION.SDK_INT > 22) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setItems(new String[]{"Камера", "Галерея", "Проводник"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                try {
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                    if (Build.VERSION.SDK_INT < 21) {
                                        File image = File.createTempFile(NotepadActivity.CreateImageName(), ".jpeg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                                        imageInCamera = image.getAbsolutePath();
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                                    }
                                    else {
                                        File image = new File(getFilesDir(), "default_image.jpeg");
                                        imageInCamera = image.getAbsolutePath();
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(SaveImageActivity.this, "com.redstar.zolotov.memors.fileprovider", image));
                                    }

                                    startActivityForResult(intent, 2);
                                } catch (Exception e) {
                                    MainActivity.ErrorOutput(SaveImageActivity.this, e, 3111);
                                }
                                break;

                            case 1:
                                try {
                                    Intent intent1 = new Intent(Intent.ACTION_PICK);
                                    intent1.setType("image/*");
                                    startActivityForResult(intent1, 0);
                                }
                                catch (Exception e) {
                                    MainActivity.ErrorOutput(SaveImageActivity.this, e, 3112);
                                }
                                break;

                            case 2:
                                try {
                                    Intent intent2 = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                                    intent2.addCategory(Intent.CATEGORY_OPENABLE);
                                    intent2.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent2.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                                    intent2.setType("image/*");
                                    startActivityForResult(intent2, 1);
                                }
                                catch (Exception e) {
                                    MainActivity.ErrorOutput(SaveImageActivity.this, e, 3113);
                                }
                                break;
                        }
                    }
                })
                .setCancelable(true);

        builder.create().show();
    }

    /*
    ---Сохранение файла картинки №12---
    * Создание файла
    * Запись данных в файл
    * Запись ссылки в ArrayList
     */
    private Uri SaveImage (Uri uri) {
        try {
            File image = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),NotepadActivity.CreateImageName());
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            FileOutputStream streamsave = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, streamsave);
            streamsave.close();

            imageFiles.add(image.getAbsolutePath());
            imageCounts.add(0);
            return Uri.fromFile(image);
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(SaveImageActivity.this, e, 3120);
        }
        return null;
    }

    /*
    ---Запись данных в Main №13---
    * Чтение line из Main
    * Создание imageLine из ArrayList
    * Запись line и imageLine
     */
    private void ChangeMainFile () {
        try {
            BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MainActivity.MAIN_FILE)));
            String line = R.readLine();
            R.close();

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < imageFiles.size(); i++) {
                builder
                        .append((i == 0) ? "~" : "")
                        .append(imageFiles.get(i))
                        .append("|").append(imageCounts.get(i))
                        .append("~");
            }

            OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(MainActivity.MAIN_FILE, Context.MODE_PRIVATE));
            osw.write(line + "\n" + builder.toString());
            osw.close();
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(SaveImageActivity.this, e, 3130);
        }
    }

    /*
    ---Окно выбора приватности картинки №14---
     */
    private void VisibilityImage (final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Скрыть файл?")
                .setPositiveButton("Да", null)
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(new File(name)));
                        sendBroadcast(intent);
                    }
                })
                .setCancelable(false);
        builder.create().show();
    }
}
