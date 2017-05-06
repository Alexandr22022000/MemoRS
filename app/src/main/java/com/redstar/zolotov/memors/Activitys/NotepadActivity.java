package com.redstar.zolotov.memors.Activitys;

/*
Активность редактора заметок
№2
 */

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import com.redstar.zolotov.memors.Fragments.ImagesFragment;
import com.redstar.zolotov.memors.R;

import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class NotepadActivity extends AppCompatActivity {
    private EditText name, text;
    private FragmentManager manager;
    private ImagesFragment fragment1;
    private MenuItem imagesKeyItem;
    private float previousTouch = 0;
    public int full;
    public String  id = "";

    /*
    ---При создании №1---
    * Установка костомного скрола на EditText
    * Измерение размеров дсиплея
    * Загруска данных из файла (если вход из каталога)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notepad_layout);

        name = (EditText) findViewById(R.id.editText_name);
        text = (EditText) findViewById(R.id.editText_text);

        try {
            text.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    try {
                        if (text.getLineCount() < 23) {
                            text.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                        else {
                            boolean isUp = previousTouch < motionEvent.getRawY();
                            previousTouch = motionEvent.getRawY();

                            int heightMax;
                            if (text.getLineCount() == 23) {
                                heightMax = 15;
                            }
                            else {
                                heightMax = ((text.getLineCount() - 23) * 42) + 15;
                            }

                            if ((text.getScrollY() == 0) || (text.getScrollY() >= heightMax)) {
                                if (text.getScrollY() == 0) {
                                    text.getParent().requestDisallowInterceptTouchEvent(!isUp);
                                }
                                else {
                                    text.getParent().requestDisallowInterceptTouchEvent(isUp);
                                }
                            }
                            else {
                                text.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                        }
                    }
                    catch (Exception e) {
                        MainActivity.ErrorOutput(NotepadActivity.this, e, 211);
                        text.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    return false;
                }
            });

            DisplayMetrics metricsB = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metricsB);
            full = (int) (metricsB.widthPixels / (this.getResources().getDisplayMetrics().density / 2));

            id = getIntent().getIntExtra("id", 0) + "";

            try {
                BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(id)));

                name.setText(R.readLine());
                String mainImage = R.readLine(), images = R.readLine();
                R.readLine();

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = R.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                R.close();
                text.setText(sb.toString());

                manager = getSupportFragmentManager();
                fragment1 = new ImagesFragment(this, mainImage, images);
            }
            catch (Exception e) {
                Toast.makeText(NotepadActivity.this, "Ошибка откытия файла " + e.toString(), Toast.LENGTH_SHORT).show();
            }
            manager.beginTransaction().add(R.id.layout_box, fragment1, ImagesFragment.TAG).commit();
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(NotepadActivity.this, e, 210);
        }
    }

    /*
    ---Создание меню №2---
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notepad_menu, menu);
        imagesKeyItem = menu.findItem(R.id.item_image_fragment);
        return true;
    }

    /*
    ---Обработка нажатий в меню №3---
    * Сохранение данных в файл (если выброно "Save")
    *   Проверка имени файла
    *   Запуск сохранения в fragment (для actions)
    *   Запись текста и Uri картинок в файл
    *   Изменение имени в главном файле
    * Или выход (если выбрано "Back")
    * Или переключение фрагмента (если "images_fragments")
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                try {
                    if (!MainActivity.CeekName(name.getText().toString())) {
                        Toast.makeText(this, "Не корректное имя файла", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    fragment1.Save();

                    String imageLine = "";
                    for (String anImagesUri : fragment1.imagesUri) {
                        imageLine = (imageLine.isEmpty() ? "" : (imageLine + "@")) + anImagesUri;
                    }

                    OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(id, Context.MODE_PRIVATE));
                    osw.write(name.getText().toString() + "\n" + fragment1.mainImage + "\n" + imageLine + "\n" + text.getText().toString());  //добавить запрос на Image и записать его результат сюда
                    osw.close();

                    BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MainActivity.MAIN_FILE)));
                    String line = R.readLine();
                    imageLine = R.readLine();
                    R.close();

                    int brace, pointComa, openBracce, i, ii = 0;

                    while (line.indexOf("//" + id, ii) != -1) {
                        ii = line.indexOf("//" + id, line.indexOf("{", 0));

                        brace = line.substring(0, ii).lastIndexOf("}", line.length());
                        pointComa = line.substring(0, ii).lastIndexOf(";", line.length());
                        openBracce = line.substring(0, ii).lastIndexOf("{", line.length());

                        i = brace > pointComa ? brace : pointComa;
                        i = i > openBracce ? i : openBracce;
                        i = i + 1;

                        line = line.substring(0, i) + name.getText().toString() + line.substring(ii, line.length());

                        ii = ii + 2;
                    }

                    osw = new OutputStreamWriter(openFileOutput(MainActivity.MAIN_FILE, Context.MODE_PRIVATE));
                    osw.write(line + "\n" + imageLine);
                    osw.close();

                    Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    MainActivity.ErrorOutput(NotepadActivity.this, e, 230);
                }
                return true;

            case R.id.menu_back:
                finish();
                return true;

            case R.id.item_image_fragment:
                if (fragment1.isHidden()) {
                    manager.beginTransaction().show(fragment1).commit();
                    imagesKeyItem.setTitle("Скрыть изображения");
                }
                else {
                    manager.beginTransaction().hide(fragment1).commit();
                    imagesKeyItem.setTitle("Покозать изображения");
                }
                return true;
        }
        return false;
    }

    /*
    ---Действие при повороте №4---
    * сохранение старого разрешения
    * измерение нового
    * запуск обновления картинок
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            int oldFull = full;

            DisplayMetrics metricsB = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metricsB);
            full = (int) (metricsB.widthPixels / (this.getResources().getDisplayMetrics().density / 2));

            fragment1.Update(oldFull);
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(NotepadActivity.this, e, 240);
        }
    }

    /*
    ---Генерация имени файла №5---
    */
    public static String CreateImageName () throws Exception {
        GregorianCalendar calendar = new GregorianCalendar();

        String name = calendar.get(Calendar.DAY_OF_MONTH) + ":";
        name = name + calendar.get(Calendar.MONTH) + ":";
        name = name + calendar.get(Calendar.YEAR) + "-";
        name = name + calendar.get(Calendar.HOUR_OF_DAY) + ":";
        name = name + calendar.get(Calendar.MINUTE) + ":";
        name = name + calendar.get(Calendar.SECOND);

        return ("ImageMemoRS-" + name + ".jpg");
    }
}
