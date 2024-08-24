package com.example.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private MaterialButton translatedBtn;

    private TextInputEditText sourceEdt;
    private int historyCounter = 0;

    private ImageView micIV;
    private TextView translatedTV;
    private Switch darkModeSwitch;
    private Button pasteButton;
    private FloatingActionButton copyButton;
    private Button swapLanguagesButton;
    private Button optionsBtn, historyButton;
    private TextView wordOfTheDayTV;
    private SharedPreferences sharedPreferences;
    static final String PREF_NAME = "TranslationHistory";
    private ArrayList<String> wordList = new ArrayList<>();
    private void loadWordsFromResource() {
        InputStream inputStream = getResources().openRawResource(R.raw.word_of_the_day);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                wordList.add(line);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showRandomWord() {
        if (!wordList.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(wordList.size());
            String randomWord = wordList.get(randomIndex);
            wordOfTheDayTV.setText(randomWord);
        }
    }


    String[] fromLanguages = {"From", "English", "Hindi", "Bengali", "Gujarati", "Kannada", "Marathi", "Tamil", "Telugu", "Urdu", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Catalan", "Czech", "Welsh", "Danish", "German", "Greek", "Spanish", "Estonian", "Persian", "Finnish", "French", "Irish", "Hebrew", "Croatian", "Haitian Creole", "Hungarian", "Indonesian", "Icelandic", "Italian", "Japanese", "Korean", "Lithuanian", "Latvian", "Malay", "Maltese", "Dutch", "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Albanian", "Swedish", "Swahili", "Thai", "Turkish", "Ukrainian", "Vietnamese", "Chinese"};

    String[] toLanguages = {"To", "English", "Hindi", "Bengali", "Gujarati", "Kannada", "Marathi", "Tamil", "Telugu", "Urdu", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Catalan", "Czech", "Welsh", "Danish", "German", "Greek", "Spanish", "Estonian", "Persian", "Finnish", "French", "Irish", "Hebrew", "Croatian", "Haitian Creole", "Hungarian", "Indonesian", "Icelandic", "Italian", "Japanese", "Korean", "Lithuanian", "Latvian", "Malay", "Maltese", "Dutch", "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Albanian", "Swedish", "Swahili", "Thai", "Turkish", "Ukrainian", "Vietnamese", "Chinese"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromLanguageCode, toLanguageCode = 0;


    public void onSwapLanguagesClick(View view) {
        // Find references to both Spinners
        Spinner fromSpinner = findViewById(R.id.idFromSpinner);
        Spinner toSpinner = findViewById(R.id.idToSpinner);

        // Swap the selected items of the two Spinners
        int fromPosition = fromSpinner.getSelectedItemPosition();
        int toPosition = toSpinner.getSelectedItemPosition();

        fromSpinner.setSelection(toPosition);
        toSpinner.setSelection(fromPosition);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idEditSource);
        translatedBtn = findViewById(R.id.idBtnTranslate);
        micIV = findViewById(R.id.idIVMic);
        historyButton = findViewById(R.id.historyButton);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        translatedTV = findViewById(R.id.idTVTranslatedTV);
        pasteButton = findViewById(R.id.idBtnPaste);
        optionsBtn = findViewById(R.id.idBtnOptions);
        copyButton = findViewById(R.id.idBtnCopy);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        swapLanguagesButton = findViewById(R.id.idBtnSwapLanguages);
        darkModeSwitch.setChecked(isDarkModeEnabled());
        wordOfTheDayTV = findViewById(R.id.wordOfTheDayTV);
        loadWordsFromResource();
        showRandomWord();

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(historyIntent);
            }
        });

        translatedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "PLZ ENTER YOUR TEXT TO TRANSLATE", Toast.LENGTH_SHORT).show();
                } else if (fromLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Plz select source language", Toast.LENGTH_SHORT).show();
                } else if (toLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Plz select language to translate", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromLanguageCode, toLanguageCode, sourceEdt.getText().toString());
                }
            }
        });


        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableDarkMode();
            } else {
                enableLightMode();
            }
            recreate(); // Recreate the activity to apply the selected theme
        });



        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Copy the translated text to the clipboard
                String translatedText = translatedTV.getText().toString();
                if (!translatedText.isEmpty()) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboardManager != null) {
                        ClipData clipData = ClipData.newPlainText("Translated Text", translatedText);
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(MainActivity.this, "Translated text copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Button aboutButton = findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutActivity(v);
            }
        });


        optionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a dialog with two options
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Feedback");
                dialog.setItems(new String[]{"Twitter", "Instagram"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Open the Help website
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/_nitin__pandit_"));
                            startActivity(browserIntent);
                        } else if (which == 1) {
                            // Open the About website
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/_nitin__pandit_/"));
                            startActivity(browserIntent);
                        }
                    }
                });
                dialog.show();
            }
        });



        // Find views
        TextInputEditText sourceEdt = findViewById(R.id.idEditSource);
        Button clearButton = findViewById(R.id.clearButton);

        // Set click listener for the clear button
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the text in the TextInputEditText
                sourceEdt.setText("");
            }
        });

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translatedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "PLZ ENTER YOUR TEXT TO TRANSLATE", Toast.LENGTH_SHORT).show();
                } else if (fromLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Plz select source language", Toast.LENGTH_SHORT).show();
                } else if (toLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Plz select language to translate", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromLanguageCode, toLanguageCode, sourceEdt.getText().toString());
                }
            }
        });

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text");
                try {
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        pasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    CharSequence pasteData = item.getText();
                    if (pasteData != null) {
                        sourceEdt.setText(pasteData);
                    } else {
                        Toast.makeText(MainActivity.this, "Nothing to paste", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Nothing to paste", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void openAboutActivity(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && result.size() > 0) {
                    sourceEdt.setText(result.get(0));
                }
            }
        }
    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String source) {
        translatedTV.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTV.setText("Translating");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                        TranslationHistoryDbHelper dbHelper = new TranslationHistoryDbHelper(MainActivity.this);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("source_text", source);
                        values.put("translated_text", s);
                        long newRowId = db.insert("translation_history", null, values);
                        db.close();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to translate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail to download the model " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int getLanguageCode(String toLanguage) {
        int languageCode = 0;
        switch (toLanguage) {
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Gujarati":
                languageCode = FirebaseTranslateLanguage.GU;
                break;
            case "Kannada":
                languageCode = FirebaseTranslateLanguage.KN;
                break;
            case "Marathi":
                languageCode = FirebaseTranslateLanguage.MR;
                break;
            case "Tamil":
                languageCode = FirebaseTranslateLanguage.TA;
                break;
            case "Telugu":
                languageCode = FirebaseTranslateLanguage.TE;
                break;
            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languageCode = FirebaseTranslateLanguage.BG;
                break;
            case "Catalan":
                languageCode = FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languageCode = FirebaseTranslateLanguage.CY;
                break;
            case "Danish":
                languageCode = FirebaseTranslateLanguage.DA;
                break;
            case "German":
                languageCode = FirebaseTranslateLanguage.DE;
                break;
            case "Greek":
                languageCode = FirebaseTranslateLanguage.EL;
                break;
            case "Spanish":
                languageCode = FirebaseTranslateLanguage.ES;
                break;
            case "Estonian":
                languageCode = FirebaseTranslateLanguage.ET;
                break;
            case "Persian":
                languageCode = FirebaseTranslateLanguage.FA;
                break;
            case "Finnish":
                languageCode = FirebaseTranslateLanguage.FI;
                break;
            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "Irish":
                languageCode = FirebaseTranslateLanguage.GA;
                break;
            case "Hebrew":
                languageCode = FirebaseTranslateLanguage.HE;
                break;
            case "Croatian":
                languageCode = FirebaseTranslateLanguage.HR;
                break;
            case "Haitian Creole":
                languageCode = FirebaseTranslateLanguage.HT;
                break;
            case "Hungarian":
                languageCode = FirebaseTranslateLanguage.HU;
                break;
            case "Indonesian":
                languageCode = FirebaseTranslateLanguage.ID;
                break;
            case "Icelandic":
                languageCode = FirebaseTranslateLanguage.IS;
                break;
            case "Italian":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Japanese":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "Korean":
                languageCode = FirebaseTranslateLanguage.KO;
                break;
            case "Lithuanian":
                languageCode = FirebaseTranslateLanguage.LT;
                break;
            case "Latvian":
                languageCode = FirebaseTranslateLanguage.LV;
                break;
            case "Malay":
                languageCode = FirebaseTranslateLanguage.MS;
                break;
            case "Maltese":
                languageCode = FirebaseTranslateLanguage.MT;
                break;
            case "Dutch":
                languageCode = FirebaseTranslateLanguage.NL;
                break;
            case "Norwegian":
                languageCode = FirebaseTranslateLanguage.NO;
                break;
            case "Polish":
                languageCode = FirebaseTranslateLanguage.PL;
                break;
            case "Portuguese":
                languageCode = FirebaseTranslateLanguage.PT;
                break;
            case "Romanian":
                languageCode = FirebaseTranslateLanguage.RO;
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                break;
            case "Slovak":
                languageCode = FirebaseTranslateLanguage.SK;
                break;
            case "Slovenian":
                languageCode = FirebaseTranslateLanguage.SL;
                break;
            case "Albanian":
                languageCode = FirebaseTranslateLanguage.SQ;
                break;
            case "Swedish":
                languageCode = FirebaseTranslateLanguage.SV;
                break;
            case "Swahili":
                languageCode = FirebaseTranslateLanguage.SW;
                break;
            case "Thai":
                languageCode = FirebaseTranslateLanguage.TH;
                break;
            case "Turkish":
                languageCode = FirebaseTranslateLanguage.TR;
                break;
            case "Ukrainian":
                languageCode = FirebaseTranslateLanguage.UK;
                break;
            case "Vietnamese":
                languageCode = FirebaseTranslateLanguage.VI;
                break;
            case "Chinese":
                languageCode = FirebaseTranslateLanguage.ZH;
                break;
            default:
                languageCode = 0;
                break;
        }
        return languageCode;
    }

    private boolean isDarkModeEnabled() {
        int nightModeFlags = getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void enableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void enableLightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
