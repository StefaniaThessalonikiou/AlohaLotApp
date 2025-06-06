package com.example.alohalotapp;

import static android.view.View.GONE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.alohalotapp.admin.FirebaseAdminHelperClass;
import com.example.alohalotapp.admin.ParkingSpace;
import com.example.alohalotapp.orders.OrderHelperClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private TextView balanceText;
    private Button payButton;
    private int balance;
    private int amountToPay;
    private SessionManager sessionManager;
    private String userId;

    private OrderHelperClass orderHelper;

    private FirebaseAdminHelperClass fireBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Μετάφερε την αρχικοποίηση εδώ για να έχει έγκυρο context
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        fireBaseHelper = new FirebaseAdminHelperClass();
        orderHelper = orderHelper.getInstance();

        if (userId == null) {
            Toast.makeText(this, "No user logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_payment);

        balanceText = findViewById(R.id.balance_text);
        payButton = findViewById(R.id.pay_button);

        Intent intent = getIntent();
        amountToPay = intent.getIntExtra("amountToPay", 5); // default
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://alohalot-e2fd9-default-rtdb.asia-southeast1.firebasedatabase.app/");

        // Φόρτωσε το υπόλοιπο και ενημέρωσε UI
        loadBalance();
        payButton.setText("Pay $" + amountToPay);

        payButton.setOnClickListener(v -> {
            if (balance >= amountToPay) {
                payButton.setVisibility(GONE);
                balance -= amountToPay;
                saveBalance();
                balanceText.setText("Your balance: " + balance + " $");
                Toast.makeText(PaymentActivity.this, "Payment of $" + amountToPay + " successful!", Toast.LENGTH_SHORT).show();
                updateUserStatsInFirebase(amountToPay, database);
                orderHelper.addOrder(PaymentActivity.this, database, amountToPay, userId, getIntent().getStringExtra("parkingName"));
            } else {
                Toast.makeText(PaymentActivity.this, "Insufficient balance! Go to wallet!", Toast.LENGTH_SHORT).show();
            }

            // Επιστροφή στο StartActivity μετά από 3 δευτερόλεπτα
            new android.os.Handler().postDelayed(() -> {
                Intent i = new Intent(PaymentActivity.this, StartActivity.class);
                startActivity(i);
                finish();
            }, 1000);
        });
    }

    private void loadBalance() {
        SharedPreferences prefs = getSharedPreferences("wallet_prefs", MODE_PRIVATE);
        balance = prefs.getInt("balance_" + userId, 0);
        balanceText.setText("Your balance: " + balance + " $");
    }

    private void saveBalance() {
        getSharedPreferences("wallet_prefs", MODE_PRIVATE)
                .edit()
                .putInt("balance_" + userId, balance)
                .apply();
    }

    private void updateUserStatsInFirebase(int amountPaid, FirebaseDatabase database) {
        DatabaseReference userRef = database.getReference("users").child(userId);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // 1. Retrieve current stats
                Double currentAmountSpent = snapshot.child("amountSpent").getValue(Double.class);
                Long currentPointsLong = snapshot.child("points").getValue(Long.class);

                double currentAmount = currentAmountSpent != null ? currentAmountSpent : 0.0;
                int currentPoints = currentPointsLong != null ? currentPointsLong.intValue() : 0;

                double newAmountSpent = currentAmount + amountPaid;
                int newPoints = currentPoints + amountPaid;

                // 2. Update paymentStats: Paid3, Paid5, Paid11
                Long paid3 = snapshot.child("paymentStats").child("Paid3").getValue(Long.class);
                Long paid5 = snapshot.child("paymentStats").child("Paid5").getValue(Long.class);
                Long paid11 = snapshot.child("paymentStats").child("Paid11").getValue(Long.class);

                long newPaid3 = paid3 != null ? paid3 : 0;
                long newPaid5 = paid5 != null ? paid5 : 0;
                long newPaid11 = paid11 != null ? paid11 : 0;

                if (amountPaid == 3) newPaid3++;
                else if (amountPaid == 5) newPaid5++;
                else if (amountPaid == 11) newPaid11++;

                Map<String, Object> paymentStatsUpdates = new HashMap<>();
                paymentStatsUpdates.put("Paid3", newPaid3);
                paymentStatsUpdates.put("Paid5", newPaid5);
                paymentStatsUpdates.put("Paid11", newPaid11);

                // 3. Update usageStats for the specific parking
                String parkingName = getIntent().getStringExtra("parkingName");
                Long usages = snapshot.child("usageStats").child(parkingName).getValue(Long.class);
                long newUsages = usages != null ? usages + 1 : 1;

                Map<String, Object> usageStatsUpdates = new HashMap<>();
                DataSnapshot usageStatsSnapshot = snapshot.child("usageStats");
                for (DataSnapshot usage : usageStatsSnapshot.getChildren()) {
                    String key = usage.getKey();
                    Long value = usage.getValue(Long.class);
                    usageStatsUpdates.put(key, value != null ? value : 0L);
                }
                usageStatsUpdates.put(parkingName, newUsages);

                // 4. Prepare all updates for the user node
                Map<String, Object> updates = new HashMap<>();
                updates.put("amountSpent", newAmountSpent);
                updates.put("points", newPoints);
                updates.put("paymentStats", paymentStatsUpdates);
                updates.put("usageStats", usageStatsUpdates);

                // 5. Send updates to Firebase
                userRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "User stats updated!", Toast.LENGTH_SHORT).show();

                            // 6. Increment totalParkings
                            Long totalParkings = snapshot.child("totalParkings").getValue(Long.class);
                            long updatedTotalParkings = totalParkings != null ? totalParkings + 1 : 1;

                            userRef.child("totalParkings").setValue(updatedTotalParkings)
                                    .addOnSuccessListener(aVoid2 -> Log.d("Firebase", "totalParkings updated"))
                                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to update totalParkings", e));
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to update stats", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show());
    }
}
