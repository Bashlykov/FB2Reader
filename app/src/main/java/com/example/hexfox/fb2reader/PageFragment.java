package com.example.hexfox.fb2reader;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PageFragment extends Fragment {
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_PAGE_TEXT = "arg_page_text";
    static final String ARGUMENT_PAGE_TYPE = "arg_page_type";
    static final char IMAGE = 'I';
    static final char TEXT = 'T';
    int pageNumber;
    int backColor;
    CharSequence pageText;
    WebView wvPage;
    ViewPager pager;
    static String fontPath;
    static int sizeFont = 14;
    static float lineSpace = 1.5f;
    char typeData = TEXT;
    int width;
    int height;

    final class IJavascriptHandler {
        IJavascriptHandler() {
        }

        @JavascriptInterface
        public void setSizeImage(String widthImage, String heightImage) {
            width = Integer.valueOf(widthImage);
            height = Integer.valueOf(heightImage);
            Toast.makeText(getActivity().getApplicationContext(),
                    "size: "+widthImage+ " " +heightImage, Toast.LENGTH_SHORT).show();
        }
    }

    void setHtmlStylePage(CharSequence pageText, String fontPath, int sizeFont, float lineSpace){

        String scriptTag = "";
        String idImage = "";
        //Находим id картинки и меняем ее размер при необходимости
        if(typeData == IMAGE) {
            String regex = "^<img\\s*id=[\\\"](\\w*\\.*\\w*)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(pageText);
            if(matcher.find())
                idImage = matcher.group(1);
            scriptTag = "<script>function androidResponse(){" +
                        "var img = document.getElementById(\""+idImage+"\");" +
                        "var width = img.clientWidth;" +
                        "var height = img.clientHeight;" +
                        "if(img && img.style) {" +
                        "if(width > 320){img.style.width = '320px';}" +
                        "if(height > 445){img.style.height = '445px';}}" +
                        "AndroidFunction.setSizeImage(width, height);}</script>";
        }

        String startTag = "<html><head><style type=\"text/css\">" +
                        "@font-face {font-family: my_font;src: url('"+fontPath+"');}" +
                        "body {font-family: my_font; text-align: justify; " +
                        "font-size: "+sizeFont+"px; line-height: "+lineSpace+";}</style>" +
                        "</head><body onLoad=\"javascript:return androidResponse();\">";

        String closeTag = "</body></html>";
        String myHtmlString = startTag + scriptTag + pageText + closeTag;
        wvPage.loadDataWithBaseURL("file:///android_asset/", myHtmlString,
                "text/html", "utf-8", null);
        wvPage.addJavascriptInterface(new IJavascriptHandler(), "AndroidFunction");
        //wvPage.loadUrl("javascript:androidResponse();void(0);");
    }

    public void setFontPage(String font){
        if (font != null) {
            fontPath = font;
            setHtmlStylePage(pageText, fontPath, sizeFont, lineSpace);
        }
    }

    public void setSizeFont(int size){
        sizeFont = size;
        setHtmlStylePage(pageText, fontPath, sizeFont, lineSpace);
        //wvPage.getSettings().setTextZoom(wvPage.getSettings().getTextZoom() + 10);
    }

    public void setLineSpace(float space){
        lineSpace = space;
        setHtmlStylePage(pageText, fontPath, sizeFont, lineSpace);
    }

    static PageFragment newInstance(int page, CharSequence text, char typeData) {
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putCharSequence(ARGUMENT_PAGE_TEXT, text);
        arguments.putChar(ARGUMENT_PAGE_TYPE, typeData);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        pageText = getArguments().getCharSequence(ARGUMENT_PAGE_TEXT);
        typeData = getArguments().getChar(ARGUMENT_PAGE_TYPE);
        backColor = Color.argb(255, 245, 245, 220);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, null);
        wvPage = view.findViewById(R.id.wvPage);
        pager = getActivity().findViewById(R.id.pager);
        wvPage.setBackgroundColor(backColor);
        wvPage.getSettings().setBuiltInZoomControls(true);
        wvPage.getSettings().setDisplayZoomControls(false);
        wvPage.getSettings().setJavaScriptEnabled(true);  // включили JavaScript
        wvPage.getSettings().setDomStorageEnabled(true);  // включили localStorage и т.п.
        wvPage.getSettings().setSupportZoom(false);
        wvPage.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);
        setHtmlStylePage(pageText, fontPath, sizeFont, lineSpace);

        return view;
    }
}
