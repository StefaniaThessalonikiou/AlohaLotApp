package com.example.alohalotapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.alohalotapp.db.CardDatabase;

public class AddCardActivity extends AppCompatActivity {

    private EditText cardNumberEditText, expirationDateEditText, cvcEditText, cardholderNameEditText;
    private Button saveCardBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_card);

        // Initialize views
        cardNumberEditText = findViewById(R.id.cardNumber);
        expirationDateEditText = findViewById(R.id.expirationDate);
        cvcEditText = findViewById(R.id.cvc);
        cardholderNameEditText = findViewById(R.id.cardholderName);
        saveCardBtn = findViewById(R.id.saveCardBtn);

        // Handle save button click
        saveCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCardDetails();
            }
        });
    }

    private void saveCardDetails() {
        String cardNumber = cardNumberEditText.getText().toString();
        String expirationDate = expirationDateEditText.getText().toString();
        String cvc = cvcEditText.getText().toString();
        String cardholderName = cardholderNameEditText.getText().toString();

        CardDatabase db = new CardDatabase(this);
        db.insert(cardholderName, cardNumber, expirationDate);
        db.close();

        Toast.makeText(this, "Card saved successfully!", Toast.LENGTH_SHORT).show();
        finish(); // Return to previous screen
    }
}
