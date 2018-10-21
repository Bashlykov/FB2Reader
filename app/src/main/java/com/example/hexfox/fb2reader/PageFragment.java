package com.example.hexfox.fb2reader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class PageFragment extends Fragment {
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_PAGE_TEXT = "arg_page_text";
    static final String ARGUMENT_PAGE_TYPE = "arg_page_type";
    static final char IMAGE = 'I';
    static final char TEXT = 'T';
    int pageNumber;
    int backColor;
    CharSequence pageText;
    TextView tvPage;
    ViewPager pager;
    static String fontPath;
    static float sizeFont;
    static float lineSpace;
    static boolean selectedSizeFont = false;
    static boolean selectedLineSpace = false;
    char typeData = TEXT;
    float defaultSizeFont = 14.0f;

    public void setFontPage(String font){
        if (font != null) {
            try {
                tvPage.setTypeface(Typeface.createFromAsset(
                        getActivity().getAssets(), font));
                fontPath = font;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setSizeFont(float size){
        tvPage.setTextSize(size);
        sizeFont = size;
        selectedSizeFont = true;

    }

    public void setLineSpace(float space){
        tvPage.setLineSpacing(0, space);
        lineSpace = space;
        selectedLineSpace = true;
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
        tvPage = view.findViewById(R.id.tvPage);
        pager = getActivity().findViewById(R.id.pager);

        //TextViewCompat.setAutoSizeTextTypeWithDefaults(tvPage, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        tvPage.setBackgroundColor(backColor);
        tvPage.setMovementMethod(new ScrollingMovementMethod());

       if(typeData == IMAGE) {
           InputStream stream = new ByteArrayInputStream(Base64.decode(pageText.toString().getBytes(), Base64.DEFAULT));
           Bitmap bitmap = BitmapFactory.decodeStream(stream); //decode stream to a bitmap image
           Drawable topImage = new BitmapDrawable(getResources(), bitmap);
           tvPage.setCompoundDrawablesWithIntrinsicBounds(null, topImage, null, null);
       } else {
           tvPage.setText(pageText);

           if (fontPath != null)
               tvPage.setTypeface(Typeface.createFromAsset(
                       getActivity().getAssets(), fontPath));
           if (selectedSizeFont)
               tvPage.setTextSize(sizeFont);
           else
               tvPage.setTextSize(defaultSizeFont);

           if (selectedLineSpace)
               tvPage.setLineSpacing(0, lineSpace);
       }
        tvPage.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);

        // Кнопка возврата к списку файлов
        final ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setVisibility(View.INVISIBLE);

        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnBack:
                        //do something
                        break;
                    case R.id.tvPage:
                        if(btnBack.getVisibility() == View.INVISIBLE) {
                            btnBack.setVisibility(View.VISIBLE);
                        }else{
                            btnBack.setVisibility(View.INVISIBLE);
                        }
                        break;
                }
            }
        };
        btnBack.setOnClickListener(ocl);
        tvPage.setOnClickListener(ocl);

        return view;
    }

}
