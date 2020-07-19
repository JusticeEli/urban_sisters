package com.justice.a2urbansisters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.justice.a2urbansisters.modal.Stock;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.justice.a2urbansisters.Constants.ALL_STOCKS;

public class AddEditStockActivity extends AppCompatActivity {
    private EditText nameEdtTxt, priceEdtTxt;
    private Button submitBtn;
    private ProgressDialog progressDialog;


    private CircleImageView imageView;
    private Uri uri = null;
    private Stock stock = new Stock();

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private boolean editing = false;
    private boolean photoChanged = false;
    private Stock originalStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);
        initWidgets();
        setOnClickListeners();
        checkIfWeAreAddingOrEditing();

    }

    private void checkIfWeAreAddingOrEditing() {
        if (Constants.documentSnapshot != null) {
            originalStock = Constants.documentSnapshot.toObject(Stock.class);
            setDefaultValues();
            editing = true;
        }
    }

    private void setDefaultValues() {
        nameEdtTxt.setText(originalStock.getName());
        priceEdtTxt.setText(originalStock.getPrice() + "");
        Glide.with(this).load(originalStock.getImageUrl()).into(imageView);
        stock.setImageUrl(originalStock.getImageUrl());
    }

    private void initWidgets() {
        nameEdtTxt = findViewById(R.id.nameEdtTxt);
        priceEdtTxt = findViewById(R.id.priceEdtTxt);
        submitBtn = findViewById(R.id.submitBtn);
        imageView = findViewById(R.id.imageView);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving data");
        progressDialog.setMessage("please wait...");
        progressDialog.setCancelable(false);

    }

    private void choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri = result.getUri();
                photoChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        Glide.with(this).load(uri).into(imageView);

    }

    private void setOnClickListeners() {


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editing) {
                    if (uri == null) {
                        Toasty.error(AddEditStockActivity.this, "Please click on the circle to  choose a photo", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (fieldsAreEmpty()) {
                    Toasty.error(AddEditStockActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                getDataFromEdtTxtAndSaveInDatabase();

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });
    }

    private boolean fieldsAreEmpty() {


        if (nameEdtTxt.getText().toString().trim().isEmpty() || priceEdtTxt.getText().toString().trim().isEmpty()) {


            return true;
        }
        return false;
    }

    private void resetEdtTxt() {
        nameEdtTxt.setText("");
        priceEdtTxt.setText("");

    }

    private void getDataFromEdtTxtAndSaveInDatabase() {

        stock.setName(nameEdtTxt.getText().toString());
        stock.setPrice(Long.parseLong(priceEdtTxt.getText().toString()));
        putImageToStorage();

    }

    private void putDataIntoDatabaseAdd() {
        progressDialog.show();

        firebaseFirestore.collection(ALL_STOCKS).add(stock).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {

                    if (task.isSuccessful()) {
                        resetEdtTxt();
                        Toasty.success(AddEditStockActivity.this, "Stock Added ", Toast.LENGTH_SHORT).show();

                    } else {
                        String error = task.getException().getMessage();
                        Toasty.error(AddEditStockActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                    }


                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(AddEditStockActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                }
                progressDialog.dismiss();
            }
        });

    }

    private void putImageToStorage() {

        if (!photoChanged) {
            putDataIntoDatabaseEdit();
            return;
        }


        String photoName = UUID.randomUUID().toString();
        progressDialog.show();
        final StorageReference ref = FirebaseStorage.getInstance().getReference("stocks_images").child(photoName);

        UploadTask uploadTask = ref.putFile(uri);


        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                progressDialog.dismiss();
                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    stock.setImageUrl(downloadUri.toString());
                    Toasty.success(AddEditStockActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
                    if (editing) {
                        putDataIntoDatabaseEdit();
                    } else {
                        putDataIntoDatabaseAdd();
                    }

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(AddEditStockActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });

    }

    private void putDataIntoDatabaseEdit() {
        progressDialog.show();
        Constants.documentSnapshot.getReference().set(stock).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toasty.success(AddEditStockActivity.this, "Stock UPDATED ", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(AddEditStockActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                }

                progressDialog.dismiss();
            }
        });

    }
}