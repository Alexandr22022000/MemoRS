package com.redstar.zolotov.memors.Objects;

/*
Объект записи действий с кортинками
№5
* Сохранение
* Фото
* Счетчик ссылок (не реализованно)
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import com.redstar.zolotov.memors.Activitys.MainActivity;
import com.redstar.zolotov.memors.Activitys.NotepadActivity;
import com.redstar.zolotov.memors.Fragments.ImagesFragment;

import java.io.*;

public class Actions {
    private NotepadActivity context;
    private Uri uri;
    public String file;
    /*
    * Типы:
    * 0 - камера
    * 1 - файл
    * 2 - ссылка
    * 3 - удаление
     */
    private int id, type;
    public Boolean isOpen = false;

    /*
    ---Конструктор для сохранения картинки №1---
     */
    public Actions (Uri uri, int id) {
        this.uri = uri;
        this.id = id;
        type = 1;
    }

    /*
    ---Конструктор для камеры и сохраненок №2---
     */
    public Actions (String file, int type) {
        this.file = file;
        this.type = type;
    }

    /*
    ---Применение изменений №3---
     */
    public void Save (NotepadActivity context, ImagesFragment fragment) {
        this.context = context;

        try {
            if (type == 1) {
                Uri newUri = SaveImage(uri);
                if (newUri == null) {
                    return;
                }

                if (id == -1) {
                    fragment.mainImage = newUri.toString();
                }
                else {
                    fragment.imagesUri.set(id, newUri.toString());
                }
            }

            try {
            /*Чтение из файла Main*/
                BufferedReader R = new BufferedReader(new InputStreamReader(context.openFileInput(MainActivity.MAIN_FILE)));
                String cotalog = R.readLine();
                String images = R.readLine();
                R.close();

                images = (images == null) ? "" : images;
                images = (images.equals("null") ? "" : images);
                if (type > 1) {
                    if (images.indexOf(file, 0) == -1) {
                        return;
                    }

                    int i = images.indexOf(file, 0);
                    int start = images.indexOf("|", i) + 1, end = images.indexOf("~", i);
                    i = Integer.parseInt(images.substring(start, end));
                    images = images.substring(0, start) + ((type == 2) ? (i + 1) : (i - 1)) + images.substring(end, images.length());
                }
                else {
                /*Запись в строку новой картинки*/
                    if (images.indexOf(file, 0) == -1) {
                        images = (images.isEmpty() ? "~" : images) + file + "|1~";
                    }
                }

            /*Запись строк в Main*/
                OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(MainActivity.MAIN_FILE, Context.MODE_PRIVATE));
                osw.write(cotalog + "\n" + images);
                osw.close();
            }
            catch (Exception e) {
                MainActivity.ErrorOutput(context, e, 531);
            }

            if (isOpen && (type < 2)) {
                SetVisibilityImage(file);
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 530);
        }
    }

    /*
    ---Откат изменений №4---
    * Если камера, то удалить файл картинки
     */
    public void BackUp () {
        try {
            if (type == 0) {
                new File(file).delete();
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 540);
        }
    }

    /*
    ---Сохранение резервной копии картинки №5---
     */
    private Uri SaveImage (Uri uri) {
        try {
            File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), NotepadActivity.CreateImageName());
            image.createNewFile();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

            FileOutputStream streamsave = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, streamsave);
            streamsave.close();

            file = image.getAbsolutePath();
            return Uri.fromFile(image);
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 550);
        }
        return null;
    }

    /*
    ---Открытее доступа к картинки №6---
     */
    private void SetVisibilityImage (String name) {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(new File(name)));
            context.sendBroadcast(intent);
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(context, e, 560);
        }
    }
}
