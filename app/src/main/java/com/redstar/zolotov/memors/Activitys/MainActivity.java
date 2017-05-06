package com.redstar.zolotov.memors.Activitys;

/*
Активность каталога
№1
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.redstar.zolotov.memors.Adapters.FolderAdapter;
import com.redstar.zolotov.memors.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    public static final String MAIN_FILE = "MemoRS_Main_File";
    static private int idFolder = -1;
    private ListView list;
    private TextView catalog;
    private long time = 0;

    /*
    ---При создани №1---
    * Если файла Main нет, то создать файловую систему
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        try {
            list = (ListView) findViewById(R.id.list);
            catalog = (TextView) findViewById(R.id.text_catalog);

            String[] files = fileList();
            boolean isCreate = false;

            for (String file : files) {
                if (file.equals(MAIN_FILE)) {
                    isCreate = true;
                }
            }

            if (!isCreate) {
                try {
                    OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(MAIN_FILE, Context.MODE_PRIVATE));
                    osw.write("1{}" + "\n" + "");
                    osw.close();
                }
                catch (Exception e) {
                    Toast.makeText(this, "Ошибка генерации файловой системы", Toast.LENGTH_SHORT).show();
                }
            }        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 110);
        }
    }

    /*
    ---Генерация меню №2---
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(3).setVisible(Build.VERSION.SDK_INT >= 19);
        return true;
    }

    /*
    ---Обработка нажатий в меню №3---
    * Если Back - возврат в предыдущий каталог
    * Если NewFile - вывод окна создания файла
    * Если NewFolder - вывод окна создания папки
    * Если Creator - вывод окна с информацией об авторе
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.back:
                    if (idFolder != -1) {
                        idFolder = BackFolder(idFolder);
                        UpdataList();
                    }
                    break;

                case R.id.add_file:
                    final EditText editText = new EditText(this);
                    editText.setInputType(1);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder
                            .setTitle("Введите имя нового файла")
                            .setView(editText)
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                }
                            })
                            .setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AddFile(editText.getText().toString());
                                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                    UpdataList();
                                }
                            });
                    builder.create().show();

                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    break;

                case R.id.add_folder:
                    final EditText editText2 = new EditText(this);
                    editText2.setInputType(1);
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                    builder2
                            .setTitle("Введите имя новой папки")
                            .setView(editText2)
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                }
                            })
                            .setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AddFolder(editText2.getText().toString());
                                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                    UpdataList();
                                }
                            });
                    builder2.create().show();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    break;

                case R.id.creator:
                    AlertDialog.Builder builder3 = new AlertDialog.Builder(this);

                    builder3
                            .setTitle("Информация об авторе")
                            .setMessage("Компания: RedStar. Разрабочик: Золотов Александр Александрович. Данное приложение созданно для изучения возможностей IntelliJ IDEA.")
                            .setIcon(R.mipmap.red_star)
                            .setCancelable(true)
                            .setPositiveButton("ОК", null);

                    builder3.create().show();

                    return true;

                case R.id.save_image:
                    Intent intent = new Intent(getApplicationContext(), SaveImageActivity.class);
                    intent.putExtra("StartType", false);
                    startActivity(intent);
                    break;

            /*case R.id.first_run:
                try {
                    OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(MAIN_FILE, Context.MODE_PRIVATE));   //это для отладки НЕ ТРОГАТЬ!!!
                    osw.write("1{}");
                    osw.close();
                }
                catch (Exception e) {
                    Toast.makeText(this, "Ошибка генерации файловой системы", Toast.LENGTH_SHORT).show();
                }
                UpdataList();
                return true;

            case R.id.find_files:
                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                builder3
                        .setMessage(Arrays.toString(fileList()))
                        .setPositiveButton("OK", null);
                builder3.create().show();
                return true;

            case R.id.del_oll_file:
                String[] array = fileList();
                if (array.length < 3) {return true;}
                for (int i = 2; i < array.length; i++) {
                    deleteFile(array[i]);
                }
                return true;*/
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 130);
        }
        return false;
    }

    /*
    ---Обработка нажатия на Back №4---
    * Если не Main кталог, то на каталог выше
    * Иначе выход:
    *   Запись времяни нажатия
    *   Проверка нажатия, если после последнего нажатия не более 2 сек, то выход
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (idFolder != -1) {
                idFolder = BackFolder(idFolder);
                UpdataList();
            }
            else {
                GregorianCalendar calendar = new GregorianCalendar();
                if ((calendar.getTimeInMillis() - time) < 2000) {
                    finish();
                }
                else {
                    Toast.makeText(this, "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
                    time = calendar.getTimeInMillis();
                }
            }
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /*
    ---Запуск обновления при возврате из NotepadActivity №5---
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        UpdataList();
    }

    /*
    ---Обработка выбора папки в каталоге №6---
     */
    public void SetFolder (int id) {
        idFolder = id;
        UpdataList();
    }

    /*
    ---Обработка выбора файла в каталоге №7---
     */
    public void OpenFile (int id) {
        Intent intent = new Intent(this, NotepadActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    /*
    ---Обработка нажатия Del в каталоге №8---
    * Вывод окна "Вы уверены?"
    * Если да, то запуск удаления (файла или папки)
     */
    public void Del (final boolean isFile, final int id) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder
                    .setTitle("Вы уверены?")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (isFile) {
                                DelFile(id);
                            }
                            else {
                                DelFolder(id);
                            }
                            UpdataList();
                        }
                    });

            builder.create().show();
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 180);
        }
    }

    /*
    ---Добавление папки №9---
    * Проверка корректности имени
    * Чтение данных из Main
    * Если текущий каталог не Main, то поиск текущего каталога в строке
    * Добавление новой папки в строку
    * Запись строки в Main
     */
    private void AddFolder (String name) {
        try {
            if (!CeekName(name)) {
                Toast.makeText(this, "Не корректное имя папки", Toast.LENGTH_SHORT).show();
                return;
            }

            int id = GenerateId();

            BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MAIN_FILE)));
            String line = R.readLine();
            String imageFiles = R.readLine();
            R.close();

            String lineMain = line;
            int startIndex = 0;
            int endIndex = 0;
            if (idFolder != -1) {
                startIndex = line.indexOf("{", line.indexOf("/" + idFolder, 0));
                endIndex = FindLastBrace(startIndex, line) + 1;
                line = line.substring(startIndex, endIndex);
            }

            line = line.substring(0, line.length() - 1) + name + "/" + id + "{}" + "}";

            if (idFolder != -1) {
                line = lineMain.substring(0, startIndex) + line + lineMain.substring(endIndex, lineMain.length());
            }

            OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(MAIN_FILE, Context.MODE_PRIVATE));
            osw.write(line + "\n" + imageFiles);
            osw.close();
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 190);
        }
    }

    /*
    ---Вывод вышестоящего каталога №10---
    * Чтение строки из Main
    * Поиск индекса полученого каталога
    * Поиск верхнего каталога
    * Вывод верхнего каталога
     */
    private int BackFolder (int idFolder) {
        try {
            if (idFolder == -1) {return -2;}

            BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MAIN_FILE)));
            String line = R.readLine();
            R.close();

            int i = line.indexOf("/" + idFolder, line.indexOf("{", 0));
            int brace = 0;

            while (brace <= 0) {
                if ((line.lastIndexOf("{", i) > line.lastIndexOf("}", i)) || (line.lastIndexOf("}", i) == -1)) {
                    i = line.lastIndexOf("{", i) - 1;
                    brace = brace + 1;
                }
                else {
                    i = line.lastIndexOf("}", i) - 1;
                    brace = brace - 1;
                }
            }

            if (line.lastIndexOf("/", i) == -1) {
                return -1;
            }
            else {
                return Integer.parseInt(line.substring(line.lastIndexOf("/", i) + 1, i + 1));
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(this, e, 1100);
        }
        return -1;
    }

    /*
    ---Добавление файла №11---
    * Проверка корректности имени
    * Получение строки из Main
    * Если текущий каталог не Main поиск индекса каталога
    * Добавление в каталог файла (в строке)
    * Добавление файла в дерикторию приложения
    * Запись строки в Main
     */
    private void AddFile (String name) {
        try {
            if (!CeekName(name)) {
                Toast.makeText(this, "Не корректное имя файла", Toast.LENGTH_SHORT).show();
                return;
            }

            int id = GenerateId();

            BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MAIN_FILE)));
            String line = R.readLine();
            String imageLine = R.readLine();
            R.close();

            String lineMain = line;
            int startIndex = 0;
            int endIndex = 0;
            if (idFolder != -1) {
                startIndex = line.indexOf("{", line.indexOf("/" + idFolder, 0));
                endIndex = FindLastBrace(startIndex, line) + 1;
                line = line.substring(startIndex, endIndex);
            }

            line  = line.substring(0, line.length() - 1) + name + "//" + id + ";" + "}";

            if (idFolder != -1) {
                line = lineMain.substring(0, startIndex) + line + lineMain.substring(endIndex, lineMain.length());
            }

            OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(MAIN_FILE, Context.MODE_PRIVATE));
            osw.write(line + "\n" + imageLine);
            osw.close();

            osw = new OutputStreamWriter(openFileOutput(id + "", Context.MODE_PRIVATE));
            osw.write(name + "\n" + "" + "\n" + "" + "\n" + "");
            osw.close();
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 1110);
        }
    }

    /*
    ---Удаление файла №12---
    * Получение строки из Main
    * Поиск индекса начала и конца файла
    * Вырезание файла из строки
    * Запись строки в Main
    * Удаление файла из каталога приложения
     */
    private void DelFile (int id) {
        try {
            BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MAIN_FILE)));
            String line = R.readLine();
            String imageLine = R.readLine();
            R.close();

            int i = line.indexOf("/" + id, line.indexOf("{", 0));

            int brace = line.substring(0, i).lastIndexOf("}", line.length());
            int pointComa = line.substring(0, i).lastIndexOf(";", line.length());
            int openBracce = line.substring(0, i).lastIndexOf("{", line.length());

            i = brace > pointComa ? brace : pointComa;
            i = i > openBracce ? i : openBracce;
            i = i + 1;

            int ii = line.indexOf(";", i);

            line = line.substring(0, i) + line.substring(ii + 1, line.length());

            OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(MAIN_FILE, Context.MODE_PRIVATE));
            osw.write(line + "\n" + imageLine);
            osw.close();

            deleteFile(id + "");
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 1120);
        }
    }

    /*
    ---Удаление папки №13---
    * Чтение строки из Main
    * Поиск индекса начала и конца папки
    * Поиск файлов в папки и удаление их
    * Вырезание папки из строки
    * Запись строки в Main
     */
    private void DelFolder (int id) {
        try {
            BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MAIN_FILE)));
            String line = R.readLine();
            String imageLine = R.readLine();
            R.close();

            int i = line.indexOf("/" + id, line.indexOf("{", 0));

            int brace = line.substring(0, i).lastIndexOf("}", line.length());
            int pointComa = line.substring(0, i).lastIndexOf(";", line.length());
            int openBracce = line.substring(0, i).lastIndexOf("{", line.length());

            i = brace > pointComa ? brace : pointComa;
            i = i > openBracce ? i : openBracce;
            i = i + 1;

            int ii = FindLastBrace(line.indexOf("{", i), line) + 1;

            int fii = 0, fi;
            String folderLine = line.substring(i, ii);
            while ((fi = folderLine.indexOf("//", fii)) != -1) {
                fii = folderLine.indexOf(";", fi);

                deleteFile(folderLine.substring(fi + 2, fii));
                fii = fii + 1;
            }

            line = line.substring(0, i) + line.substring(ii, line.length());

            OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(MAIN_FILE, Context.MODE_PRIVATE));
            osw.write(line + "\n" + imageLine);
            osw.close();
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 1130);
        }
    }

    /*
    ---Обновление №14---
    * Чтение строки из Main
    * Если каталог не Main, то вырезать каталог из строки
    * Поиск всех имен, is и тпов в строке и запись в ArrayList
    * Копирование данных из ArrayList в Array
    * Создание нового адаптера для списка
    * Вывод вышесоящих каталогов в навигационную строку
     */
    private void UpdataList () {
        try {
            BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MAIN_FILE)));
            String line = R.readLine();
            String lineMain = line;
            R.close();

            if (idFolder != -1) {
                int startIndex = line.indexOf("{", line.indexOf("/" + idFolder, 0));
                int endIndex = FindLastBrace(startIndex, line) + 1;
                line = line.substring(startIndex, endIndex);
            }

            ArrayList<String> names = new ArrayList<>();
            ArrayList<Integer> ids = new ArrayList<>();
            ArrayList<Boolean> typs = new ArrayList<>();
            int i = line.indexOf("{", 0);

            while (i != (line.length() - 2)) {
                names.add(line.substring(i + 1, line.indexOf("/", i)));

                typs.add(line.substring(line.indexOf("/", i) + 1, line.indexOf("/", i) + 2).equals("/"));
                i = line.indexOf("/", i) + (typs.get(typs.size() - 1) ? 2 : 1);


                ids.add(Integer.parseInt(line.substring(i, typs.get(typs.size() - 1) ? line.indexOf(";", i) : line.indexOf("{", i))));

                i = typs.get(typs.size() - 1) ? line.indexOf(";", i) : FindLastBrace(line.indexOf("{", i), line);
            }

            String[] arrayNames = new String[names.size()];
            for (int ii = 0; ii < names.size(); ii++) {
                arrayNames[ii] = names.get(ii);
            }

            int[] arrayIds = new int[ids.size()];
            for (int ii = 0; ii < names.size(); ii++) {
                arrayIds[ii] = ids.get(ii);
            }

            boolean[] arrayTypes = new boolean[typs.size()];
            for (int ii = 0; ii < names.size(); ii++) {
                arrayTypes[ii] = typs.get(ii);
            }

            list.setAdapter(new FolderAdapter(this, arrayNames, arrayIds, arrayTypes));

            catalog.setText("Main://");
            int id = idFolder;

            while (BackFolder(id) != -2) {
                int ii = lineMain.indexOf("/" + id, 0);

                int brace = lineMain.substring(0, ii).lastIndexOf("}", lineMain.length());
                int pointComa = lineMain.substring(0, ii).lastIndexOf(";", lineMain.length());
                int openBracce = lineMain.substring(0, ii).lastIndexOf("{", lineMain.length());

                i = brace > pointComa ? brace : pointComa;
                i = i > openBracce ? i : openBracce;
                i = i + 1;

                catalog.setText(catalog.getText().toString().substring(0, 7) + lineMain.substring(i, ii) + "/" + catalog.getText().toString().substring(7, catalog.getText().toString().length()));
                id = BackFolder(id);
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 1140);
        }
    }

    /*
    ---Поиск отрезка {} №15---
     */
    private int FindLastBrace (int index, String text) {
        int brace = 0;

        try {
            while (true) {
                if (((text.indexOf("{", index) < text.indexOf("}", index)) && (text.indexOf("{", index) != -1)) || (text.indexOf("}", index) == -1)) {
                    index = text.indexOf("{", index) + 1;
                    brace = brace + 1;
                }
                else {
                    index = text.indexOf("}", index) + 1;
                    brace = brace - 1;
                }

                if (brace == 0) {break;}
            }
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 1150);
        }
        return index - 1;
    }

    /*
    ---Генерация id №16---
     */
    private int GenerateId () {
        int id = 0;
        try {
            BufferedReader R = new BufferedReader(new InputStreamReader(openFileInput(MAIN_FILE)));
            String line = R.readLine();
            String imageLine = R.readLine();
            R.close();

            id = Integer.parseInt(line.substring(0, line.indexOf("{", 0))) + 1;
            line = id + line.substring(line.indexOf("{", 0), line.length());

            OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(MAIN_FILE, Context.MODE_PRIVATE));
            osw.write(line + "\n" + imageLine);
            osw.close();
        }
        catch (Exception e) {
            MainActivity.ErrorOutput(MainActivity.this, e, 1160);
        }
        return id;
    }

    /*
    ---Проверка имени файла №17---
    * Проверка на пустоту
    * Проверка на запрещеные символы
     */
    static boolean CeekName(String name) throws Exception{
        if (name.isEmpty()) {
            return false;
        }

        String[] sumbols = new String[]{"{", "}", "/", ";"};
        for (String symbol : sumbols) {
            if (name.indexOf(symbol, 0) != -1) {
                return false;
            }
        }

        return true;
    }

    /*
    ---Вывод сообщения об ошибки №0---
     */
    public static void ErrorOutput (Context context, Exception e, int no) {
        try {
            EditText text = new EditText(context);
            text.setText(e.toString());
            AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
            builder2
                    .setTitle("Ошибка №" + no + ", пожайлуст отправте код ошибки автору приложения")
                    .setPositiveButton("Ок", null)
                    .setView(text);
            builder2.create().show();
        }
        catch (Exception ee) {
            Toast.makeText(context, "Неопознанная ошибка №" + no, Toast.LENGTH_SHORT).show();
        }
    }
}
