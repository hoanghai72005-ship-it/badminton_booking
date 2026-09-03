package com.example.badmintonbooking.viewmodel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.badmintonbooking.data.TimeSlot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
public class BookingViewModel extends ViewModel {
    private final MutableLiveData<List<TimeSlot>> timeSlotsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final DatabaseReference databaseReference;

    private final MutableLiveData<String> bookingResultLiveData = new MutableLiveData<>();

    public LiveData<String> getBookingResult() {
        return bookingResultLiveData;
    }

    public void bookTimeSlot(String slotId) {
        DatabaseReference slotRef = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("timeSlots").child(slotId);

        slotRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                TimeSlot slot = currentData.getValue(TimeSlot.class);
                if (slot == null) {
                    return com.google.firebase.database.Transaction.success(currentData);
                }

                if ("AVAILABLE".equals(slot.getStatus())) {
                    slot.setStatus("BOOKED");
                    currentData.setValue(slot);
                    return com.google.firebase.database.Transaction.success(currentData);
                } else {
                    return com.google.firebase.database.Transaction.abort();
                }
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    bookingResultLiveData.setValue("Đặt sân thành công!");
                } else {
                    bookingResultLiveData.setValue("Rất tiếc, sân vừa bị người khác đặt mất!");
                }
            }
        });
    }

    public BookingViewModel() {
        databaseReference = FirebaseDatabase.getInstance("https://badminton-booking-9e3a2-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("timeSlots");

    }

    public LiveData<List<TimeSlot>> getTimeSlots() {
        return timeSlotsLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadTimeSlotsForDate(String courtId, String date) {
        databaseReference.orderByChild("courtId").equalTo(courtId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<TimeSlot> slots = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            TimeSlot slot = dataSnapshot.getValue(TimeSlot.class);
                            if (slot != null && slot.getDate().equals(date)) {
                                slots.add(slot);
                            }
                        }
                        timeSlotsLiveData.setValue(slots);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorLiveData.setValue("Lỗi tải dữ liệu: " + error.getMessage());
                    }
                });
    }

}
