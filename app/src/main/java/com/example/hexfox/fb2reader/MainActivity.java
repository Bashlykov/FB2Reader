package com.example.hexfox.fb2reader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;
    File file;

    static final String TAG = "myLogs";

    ViewPager pager;
    ArrayList<EntryPage> pagesBook;
    TextView tvPage;
    MyFragmentPagerAdapter pagerAdapter;
    int curNumPage;

    AlertDialog.Builder builder;
    AlertDialog.Builder builderSizeFont;
    AlertDialog.Builder builderLineSpace;

    PageFragment currentPage;
    PageFragment nextPage;
    PageFragment prevPage;
    PageFragment prevPrevPage;

    private static final int MENU_ITEM_0 = 0;
    private static final int MENU_ITEM_1 = 1;
    private static final int MENU_ITEM_2 = 2;
    private static final int MENU_ITEM_3 = 3;

    int numPageBookOpen = -1;
    int checkedItemFont = 0;
    int checkedItemSizeFont = 1;
    int checkedItemLineSpace = 0;

    String dirFonts;
    String fontPath;
    String[] filesFonts;
    String[] sizesFonts = {"12","14","16","18"};
    String[] linesSpaces = {"1","1.5","2"};

    final char IMAGE = 'I';
    final char TEXT = 'T';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            findDirs();
        }

        // setup the alert builder
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Выбор шрифта");
        dirFonts = "font";
        try {
            filesFonts = getAssets().list(dirFonts);
        }catch (IOException e){
            Log.d(TAG, e.getMessage());
        }
        fontPath = dirFonts + "/" + filesFonts[checkedItemFont];

        builderSizeFont = new AlertDialog.Builder(this);
        builderSizeFont.setTitle("Выбор размера шрифта");

        builderLineSpace = new AlertDialog.Builder(this);
        builderLineSpace.setTitle("Выбор межстрочного интервала");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ITEM_0, Menu.NONE, "Список книг");
        menu.add(Menu.NONE, MENU_ITEM_1, Menu.NONE, "Шрифт");
        menu.add(Menu.NONE, MENU_ITEM_2, Menu.NONE, "Размер шрифта");
        menu.add(Menu.NONE, MENU_ITEM_3, Menu.NONE, "Межстрочный интервал");
        return true;
    }

    //Обработка меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_0:
                pager.setVisibility(View.INVISIBLE);
                return true;
            case MENU_ITEM_1:

                try{
                    filesFonts = getAssets().list(dirFonts);
                    // add a radio button list
                    builder.setSingleChoiceItems(filesFonts, checkedItemFont, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // user checked an item
                            checkedItemFont = which;
                        }
                    });
                    // add OK and Cancel buttons
                    builder.setPositiveButton("Применить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // user clicked OK
                            fontPath = dirFonts + "/" + filesFonts[checkedItemFont];
                            if(currentPage != null)
                                currentPage.setFontPage(fontPath);
                            if(nextPage != null)
                                nextPage.setFontPage(fontPath);
                            if(prevPage != null)
                                prevPage.setFontPage(fontPath);
                            if(prevPrevPage != null)
                                prevPrevPage.setFontPage(fontPath);

                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Отмена", null);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case MENU_ITEM_2:
                try{
                    // add a radio button list
                    builderSizeFont.setSingleChoiceItems(sizesFonts, checkedItemSizeFont, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // user checked an item
                            checkedItemSizeFont = which;
                        }
                    });
                    // add OK and Cancel buttons
                    builderSizeFont.setPositiveButton("Применить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // user clicked OK
                            if(currentPage != null)
                                currentPage.setSizeFont(Float.parseFloat(sizesFonts[checkedItemSizeFont]));
                            if(nextPage != null)
                                nextPage.setSizeFont(Float.parseFloat(sizesFonts[checkedItemSizeFont]));
                            if(prevPage != null)
                                prevPage.setSizeFont(Float.parseFloat(sizesFonts[checkedItemSizeFont]));
                            if(prevPrevPage != null)
                                prevPrevPage.setSizeFont(Float.parseFloat(sizesFonts[checkedItemSizeFont]));
                            dialog.dismiss();
                        }
                    });
                    builderSizeFont.setNegativeButton("Отмена", null);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                // create and show the alert dialog
                AlertDialog dialogSizeFont = builderSizeFont.create();
                dialogSizeFont.show();
                return true;
            case MENU_ITEM_3:
                try{
                    // add a radio button list
                    builderLineSpace.setSingleChoiceItems(linesSpaces, checkedItemLineSpace, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // user checked an item
                            checkedItemLineSpace = which;
                        }
                    });
                    // add OK and Cancel buttons
                    builderLineSpace.setPositiveButton("Применить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // user clicked OK
                            if(currentPage != null)
                                currentPage.setLineSpace(Float.parseFloat(linesSpaces[checkedItemLineSpace]));
                            if(nextPage != null)
                                nextPage.setLineSpace(Float.parseFloat(linesSpaces[checkedItemLineSpace]));
                            if(prevPage != null)
                                prevPage.setLineSpace(Float.parseFloat(linesSpaces[checkedItemLineSpace]));
                            if(prevPrevPage != null)
                                prevPrevPage.setLineSpace(Float.parseFloat(linesSpaces[checkedItemLineSpace]));
                            dialog.dismiss();
                        }
                    });
                    builderLineSpace.setNegativeButton("Отмена", null);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                // create and show the alert dialog
                AlertDialog dialogLineSpace = builderLineSpace.create();
                dialogLineSpace.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findDirs(){
        File sdcard = Environment.getExternalStorageDirectory();
        // будем искать в папке tmp
        String dir = sdcard.getAbsolutePath() + "/Download";
        // в этой папке будем искать файлы с расширением
        String ext = ".fb2";
        // вызываем метод поиска файлов с расширением
        findFiles(dir, ext);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findDirs();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    // метод поиска
    private void findFiles(final String dir, String ext) {

        ListView listView = findViewById(R.id.listView);

        File file = new File(dir);
        if(!file.exists()) {
            //"папка не существует";
        }

        MyFileNameFilter myFilter = new MyFileNameFilter(ext);
        String[] names;
        final ArrayAdapter<String> adapter;
        File[] listFiles = file.listFiles(myFilter);

        if(listFiles == null)
            return;

        if(listFiles.length == 0){
            //не содержит файлов с расширением
        }else{
            names = new String[listFiles.length];
            int i=0;
            for(File f : listFiles) {
                names[i] = f.getName();
                i++;
            }
            adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, names);
            listView.setAdapter(adapter);


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    if(numPageBookOpen == position){
                        pager.setVisibility(View.VISIBLE);
                        return;
                    }


                    pagesBook = new ArrayList<>();
                    try {
                        // парсим xml файл в список объектов Student
                        parseXML(dir + "/" + adapter.getItem(position));

                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }

                    setPager(pagesBook);

                    numPageBookOpen = position;
                    Log.d(TAG, "click: position = " + position + ", id = "
                            + id + ", name = " + adapter.getItem(position));
                }
            });
        }
    }

    public ViewPager getPager(){ return pager; }

    private void setPager(ArrayList<EntryPage> allPages){
        pager = findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), allPages);
        pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected, position = " + position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        public int count;
        ArrayList<EntryPage> allPages;
        int step;
        final int LEFT = 1;
        final int RIGHT = 2;
        int direct;
        int directChanged;

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<EntryPage> allPages) {
            super(fm);
            this.count = allPages.size();
            this.allPages = allPages;
            step = 0;
            direct = RIGHT;
            directChanged = RIGHT;
        }

        @Override
        public Fragment getItem(int position) {

            // Алгоритм перелистывания страниц
            // Создается первая страница, затем сразу вторая (step == 0, step == 1)
            // Для перелистывания в любом направлении: предыдущей странице присваиваться текущая,
            // текущей следующая, а последующая создается. (step == 2)
            // Если направление меняется, то переходим на (step == 3), где предыдущей страницей становится
            // пред-предыдущая, текущей становится предыдущая, а последующая создается.

            //Проверка направления перелистывания
            if(position < curNumPage){
                directChanged = LEFT;
            }else{
                directChanged = RIGHT;
            }

            //Если поменялось направление, переходим к (step == 3)
            if(directChanged != direct){
                direct = directChanged;
                step = 3;
            }

            if(step == 0) {
                currentPage = PageFragment.newInstance(position,
                        allPages.get(position).getData(), allPages.get(position).getTypeData());
                nextPage = currentPage;
                //nextPage.setFontPage(fontPath);
                step = 1;
            }
            else if(step == 1){
                nextPage = PageFragment.newInstance(position,
                        allPages.get(position).getData(), allPages.get(position).getTypeData());
                //nextPage.setFontPage(fontPath);
                step = 2;
            }
            else if(step == 2){
                prevPage = currentPage;
                currentPage = nextPage;
                nextPage = PageFragment.newInstance(position,
                        allPages.get(position).getData(), allPages.get(position).getTypeData());
                //nextPage.setFontPage(fontPath);
            }
            else if(step == 3){
                prevPrevPage = currentPage;
                currentPage = prevPage;
                nextPage = PageFragment.newInstance(position,
                        allPages.get(position).getData(), allPages.get(position).getTypeData());
                //nextPage.setFontPage(fontPath);
                step = 2;
            }

            curNumPage = position;
            return nextPage;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position,
                                   Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "стр. " + ++position + " из " + count;
        }
    }

    // Реализация интерфейса FileNameFilter
    public static class MyFileNameFilter implements FilenameFilter {

        private String ext;

        public MyFileNameFilter(String ext){

            this.ext = ext.toLowerCase();
        }

        @Override
        public boolean accept(File dir, String name) {

            return name.toLowerCase().endsWith(ext);
        }
    }

    private Map<String, String> getBinaries(String file) throws ParserConfigurationException, IOException, OutOfMemoryError {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputStream inputStream = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String encoding = "utf-8";

        try {
            String line = br.readLine();
            encoding = line.substring(line.indexOf("encoding=\"") + 10, line.indexOf("\"?>"));
        } catch (Exception var12) {
            var12.printStackTrace();
        }

        Map<String, String> binaries = new HashMap();
        NodeList binary;
        try{
            Document doc = db.parse(new InputSource(new InputStreamReader(inputStream, encoding)));
            binary = doc.getElementsByTagName("binary");

            for(int item = 0; item < binary.getLength(); ++item) {

                NamedNodeMap map = binary.item(item).getAttributes();

                for(int index = 0; index < map.getLength(); ++index) {
                    Node attr = map.item(index);
                    if (attr.getNodeName().equals("id")) {
                        binaries.put(attr.getNodeValue(),
                                binary.item(item).getChildNodes().item(0).getNodeValue());
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return binaries;
    }

    private void parseXML(String fileName) throws XmlPullParserException, IOException {

        Map<String, String> binaries = null;
        try{
            binaries = getBinaries(fileName);
        }catch (Exception e){
            e.printStackTrace();
        }

        XmlPullParser parser = Xml.newPullParser();
        // получаем доступ к xml файлу
        InputStream inputStream = new FileInputStream(fileName);
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);

        // получаем первое событие в xml файле
        int eventType = parser.getEventType();
        String tagOpen = "";
        String tagClose = "";
        String parentTag = "";
        EntryPage entryPage = new EntryPage();
        SpannableStringBuilder stringPage = new SpannableStringBuilder();
        SpannableStringBuilder pieceTextFromPrevPage = new SpannableStringBuilder();
        boolean toNewStrFlag = false;
        final int MAX_LEN_PAGE = 800;
        String imageString;
        int numCallEndTag = 0;


        // Отдельный парсинг раздела описания <description>
        boolean notEnd = true;
        while(notEnd){
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    String value;
                    String nameTag = parser.getName();
                    switch (nameTag) {
                        case "annotation":
                            parentTag = nameTag;
                            break;
                        case "p":
                            if(parentTag.equals("annotation")) {
                                tagOpen = "<p align=\"center\"><i>";
                                tagClose = "</i></p>";
                            }
                            break;
                        case "image":
                            String tmp = parser.getAttributeValue(null, "l:href");
                            value = tmp.replace("#", "");
                            if (binaries != null)
                                if (binaries.containsKey(value)) {
                                    imageString = binaries.get(value);
                                    EntryPage _entryPage = new EntryPage();
                                    _entryPage.set(IMAGE, imageString);
                                    pagesBook.add(_entryPage);
                                    //Log.d(TAG, valImage);
                                }
                            break;
                        /*
                        case "book-title":
                            break;
                        case "subtitle":
                            break;
                        case "first-name":
                            break;
                        case "middle-name":
                            break;
                        case "last-name":
                            break;
                        */
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if(parser.getName().equals("description")){
                        notEnd = false;
                    }
                    break;
                case XmlPullParser.TEXT:
                    if(parentTag.equals("annotation")) {
                        SpannableStringBuilder str = new SpannableStringBuilder();
                        str.append(Html.fromHtml(tagOpen + parser.getText() + tagClose));
                        EntryPage ep = new EntryPage();
                        ep.set(TEXT, str);
                        pagesBook.add(ep);
                        parentTag = "";
                    }
                    break;

            }
            eventType = parser.next();
        }

        parentTag = "";
        // Далее парсинг всей книги
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                // открывайющий тэг
                case XmlPullParser.START_TAG:
                    //Log.d(TAG, "START_TAG: " + parser.getName());
                    String value;
                    String nameTag = parser.getName();
                    switch (nameTag) {
                        case "p":
                            if(parentTag.equals("title")) {
                                tagOpen = "<br /><h2>";
                                tagClose = "</2>";
                            }else if(parentTag.equals("annotation")) {
                                tagOpen = "<p align=\"center\"><i>";
                                tagClose = "</i></p>";
                            }else if(parentTag.equals("epigraph")){
                                tagOpen = "<p align=\"right\"><i>";
                                tagClose = "</i></p>";
                            }else{
                                tagOpen = "<span>";
                                tagClose = "</span><br/>";
                            }
                            break;
                        case "v":
                            if(parentTag.equals("stanza")){
                                tagOpen = "<br /><i align=\"center\">";
                                tagClose = "</i>";
                            }
                            break;
                        case "emphasis":
                            if(parentTag.equals("strong")){
                                tagOpen = "<strong><em>";
                                tagClose = "</em></strong>";
                            }else {
                                tagOpen = "<em>";
                                tagClose = "</em>";
                            }
                            break;
                        case "subtitle":
                            tagOpen = "<br /><h4>";
                            tagClose = "</4>";
                            break;
                        case "text-author":
                            tagOpen = "<p align=\"right\"><i>";
                            tagClose = "</i></p>";
                        case "stanza":
                            parentTag = nameTag;
                            break;
                        case "title":
                            parentTag = nameTag;
                            break;
                        case "strong":
                            parentTag = nameTag;
                            break;
                        case "annotation":
                            parentTag = nameTag;
                            break;
                        case "epigraph":
                            parentTag = nameTag;
                            break;
                        case "cite":
                            parentTag = nameTag;
                            break;
                        case "empty-line":
                            tagOpen = "<br />";
                            tagClose = "";
                            break;
                        case "binary": // пропускаем т.к. это картинки, уже пропарсены и находятся в памяти
                            return;
                        case "image":
                            String tmp = parser.getAttributeValue(null, "l:href");
                            value = tmp.replace("#", "");
                            if (binaries != null)
                                if (binaries.containsKey(value)) {
                                    imageString = binaries.get(value);
                                    EntryPage _entryPage = new EntryPage();
                                    _entryPage.set(IMAGE, imageString);
                                    pagesBook.add(_entryPage);
                                    //Log.d(TAG, valImage);
                                }
                            break;
                    }
                // закрывающий тэг
                case XmlPullParser.END_TAG:

                    if(parser.getName().equals(parentTag)){
                        ++numCallEndTag;
                        if(numCallEndTag == 2) {
                            numCallEndTag = 0;
                            parentTag = "";
                            //Log.d(TAG, "END_TAG: " + parser.getName());
                        }
                    }

                    break;
                // если это содержимое тэга
                case XmlPullParser.TEXT:

                    if(!parser.getText().isEmpty()){
                        //Log.d(TAG, tagOpen + parser.getText() + tagClose);

                        // Делим текст по страницам по MAX_LEN_PAGE симвовлов
                        if(toNewStrFlag) {
                            stringPage.append(Html.fromHtml(pieceTextFromPrevPage
                                    + tagOpen + parser.getText() + tagClose));
                            toNewStrFlag = false;
                        }else {
                            stringPage.append(Html.fromHtml(tagOpen + parser.getText() + tagClose));
                        }

                        if(stringPage.length() >= MAX_LEN_PAGE) {
                            if(stringPage.length() > MAX_LEN_PAGE) {

                                // Если попали на слово, то переносим его на след. страницу
                                int lenPage = MAX_LEN_PAGE;
                                while(stringPage.charAt(lenPage) != ' '){
                                    --lenPage;
                                }
                                pieceTextFromPrevPage = (SpannableStringBuilder)
                                        stringPage.subSequence(lenPage, stringPage.length());

                                stringPage = (SpannableStringBuilder)
                                        stringPage.subSequence(0, lenPage);

                                toNewStrFlag = true;
                            }

                            entryPage.set(TEXT, stringPage);
                            pagesBook.add(entryPage);
                            stringPage = new SpannableStringBuilder();
                            entryPage = new EntryPage();

                        }
                        tagOpen = "";
                        tagClose = "";
                    }
                    break;

                default:
                    break;
            }

            // переходим к следующему событию внутри XML
            eventType = parser.next();
            //if(pagesBook.size() == 5)
              //  break;
        }

    }

    class EntryPage {

        private Character typeData;
        private CharSequence data;
        public EntryPage(){}

        public EntryPage(Character typeData,
                         CharSequence data){
            this.typeData = typeData;
            this.data = data;
        }

        public void set(Character typeData,
                         CharSequence data){
            this.typeData = typeData;
            this.data = data;
        }

        public  void setTypeData(Character typeData){
            this.typeData = typeData;
        }

        public  void setData(CharSequence data){
            this.data = data;
        }

        public Character getTypeData(){
            return this.typeData;
        }

        public CharSequence getData(){
            return  this.data;
        }
    }
}

