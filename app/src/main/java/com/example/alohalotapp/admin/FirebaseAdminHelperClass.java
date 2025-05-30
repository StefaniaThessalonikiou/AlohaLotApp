package com.example.alohalotapp.admin;

import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.function.Consumer;

public class FirebaseAdminHelperClass {

    private static final String TAG = "FirebaseAdminHelper";
    private static final String DATABASE_URL = "https://alohalot-e2fd9-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final FirebaseDatabase firebaseDatabase;

    public FirebaseAdminHelperClass() {
        firebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL);
    }

    public DatabaseReference getReference(String path) {
        return firebaseDatabase.getReference(path);
    }

    public DatabaseReference getParkingSpacesRef() {
        return getReference("parkingspaces");
    }

    public void addParkingSpace(String name, double lat, double lon, int capacity, String openTime,
                                String closeTime,boolean handiCapped,
                                android.content.Context context) {
        DatabaseReference reference = getParkingSpacesRef();

        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long count = 0;
                if (task.getResult().exists()) {
                    count = task.getResult().getChildrenCount();
                }

                String newParkingId = "Parking" + (count + 1);
                ParkingSpace newSpace = new ParkingSpace(capacity, lat, lon, 0, name, openTime, closeTime,handiCapped );

                reference.child(newParkingId).setValue(newSpace)
                        .addOnCompleteListener(saveTask -> {
                            if (saveTask.isSuccessful()) {
                                Toast.makeText(context, "Parking space added!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to add parking space.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Firebase write failed", e);
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }

    public void loadParkingNames(Consumer<ArrayList<String>> onLoaded, Consumer<String> onError) {
        getParkingSpacesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> names = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.child("Name").getValue(String.class);
                    if (name == null) {
                        name = child.child("name").getValue(String.class);
                    }
                    if (name != null) names.add(name);
                }
                onLoaded.accept(names);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onError.accept(error.getMessage());
            }
        });
    }

    public void loadOpeningHours(Consumer<ArrayList<Pair<String, String>>> onLoaded, Consumer<String> onError) {
        getParkingSpacesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Pair<String, String>> openingHours = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String openingTime = child.child("OpenTime").getValue(String.class);
                    if (openingTime == null) {
                        openingTime = child.child("openTime").getValue(String.class);
                    }

                    String closingTime = child.child("CloseTime").getValue(String.class);
                    if (closingTime == null) {
                        closingTime = child.child("closeTime").getValue(String.class);
                    }

                    if (openingTime != null && closingTime != null) {
                        openingHours.add(new Pair<>(openingTime, closingTime));
                    }
                }

                onLoaded.accept(openingHours);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onError.accept(error.getMessage());
            }
        });
    }

    public void loadCoordinates(Consumer<ArrayList<String>> onLoaded, Consumer<String> onError) {
        getParkingSpacesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> coordinates = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Double latitude = child.child("Latitude").getValue(Double.class);
                    Double longitude = child.child("Longitude").getValue(Double.class);

                    if (latitude == null) {
                        latitude = child.child("latitude").getValue(Double.class);
                    }

                    if (longitude == null) {
                        longitude = child.child("longitude").getValue(Double.class);
                    }

                    StringBuilder coordinateBuilder = new StringBuilder();

                    if (latitude != null && longitude != null) {
                        coordinateBuilder.append(latitude);
                        coordinateBuilder.append(",");
                        coordinateBuilder.append(longitude);

                        coordinates.add(coordinateBuilder.toString());
                    }
                }
                onLoaded.accept(coordinates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onError.accept(error.getMessage());
            }
        });
    }

    public void loadCapacities(Consumer<ArrayList<Integer>> onLoaded, Consumer<String> onError) {
        getParkingSpacesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Integer> capacities = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Integer capacity = child.child("Capacity").getValue(Integer.class);
                    if (capacity == null || capacity == 0) {
                        capacity = child.child("capacity").getValue(Integer.class);
                    }
                    if (capacities != null || capacity != 0) capacities.add(capacity);
                }
                onLoaded.accept(capacities);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onError.accept(error.getMessage());
            }
        });
    }

    public void loadCurrentUsers(Consumer<ArrayList<Integer>> onLoaded, Consumer<String> onError) {
        getParkingSpacesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Integer> currentUsersList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Integer currentUsers = child.child("CurrentUsers").getValue(Integer.class);
                    if (currentUsers == null || currentUsers == 0) {
                        currentUsers = child.child("currentUsers").getValue(Integer.class);
                    }
                    if (currentUsers != null || currentUsers != 0)
                        currentUsersList.add(currentUsers);
                }
                onLoaded.accept(currentUsersList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onError.accept(error.getMessage());
            }
        });
    }

    public void loadIsHandicapped(Consumer<ArrayList<Boolean>> onLoaded, Consumer<String> onError) {
        getParkingSpacesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Boolean> isHandicappedList = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Boolean isHandicapped = child.child("Handicapped").getValue(Boolean.class);

                    if (isHandicapped == null) {
                        isHandicapped = child.child("handicapped").getValue(Boolean.class);
                    }

                    // Default to false if still null
                    isHandicappedList.add(isHandicapped != null && isHandicapped);
                }

                onLoaded.accept(isHandicappedList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onError.accept(error.getMessage());
            }
        });
    }

    public void getParkingSpaceByName(String name, Consumer<ParkingSpaceWithId> onResult, Consumer<String> onError) {
        getParkingSpacesRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot child : task.getResult().getChildren()) {
                    String dbName = child.child("name").getValue(String.class);
                    if (name.equals(dbName)) {
                        ParkingSpace space = child.getValue(ParkingSpace.class);
                        if (space != null) {
                            onResult.accept(new ParkingSpaceWithId(child.getKey(), space));
                            return;
                        }
                    }
                }
                onError.accept("Parking not found");
            } else {
                onError.accept(task.getException().getMessage());
            }
        });
    }

    public void updateParkingSpace(String id, ParkingSpace updated, Runnable onSuccess, Consumer<String> onError) {
        getParkingSpacesRef().child(id).setValue(updated)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public static class ParkingSpaceWithId {
        public String id;
        public ParkingSpace space;

        public ParkingSpaceWithId(String id, ParkingSpace space) {
            this.id = id;
            this.space = space;
        }
    }
}