package com.restaurant.mrp_1147050103;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.restaurant.mrp_1147050103.adapter.RestaurantAdapter;
import com.restaurant.mrp_1147050103.data.Constans;
import com.restaurant.mrp_1147050103.data.Session;
import com.restaurant.mrp_1147050103.model.ListRestaurantResponse;
import com.restaurant.mrp_1147050103.model.RegisterResponse;
import com.restaurant.mrp_1147050103.utils.DialogUtils;

import static com.restaurant.mrp_1147050103.data.Constans.GET_LIST_RESTAURANT;
import static com.restaurant.mrp_1147050103.data.Constans.GET_SEARCH_RESTAURANT;

public class DashboardActivity extends AppCompatActivity {
    RestaurantAdapter adapter;
    RecyclerView rv;
    ProgressDialog progressDialog;
    Session session;
    SearchView svRestaurant;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        session = new Session(this);
        progressDialog = new ProgressDialog(this);
        initView();
        initRecyclerView();
        initSearch();
    }
    //Method Search
    private void initSearch(){
        svRestaurant.setOnQueryTextListener(new
                                                    SearchView.OnQueryTextListener() {
                                                        @Override
                                                        public boolean onQueryTextSubmit(String query) {
                                                            loadItem(GET_SEARCH_RESTAURANT + query);
                                                            return true;
                                                        }
                                                        @Override
                                                        public boolean onQueryTextChange(String newText) {
                                                            return false;
                                                        }
                                                    });
        svRestaurant.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                loadItem(GET_LIST_RESTAURANT);
                return false;
            }
        });
    }
    //Method untuk bind view
    private void initView(){
        svRestaurant = findViewById(R.id.sv_restaurant);
    }
    //Method ini digunakan untuk menampilkan menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }
//Menthod ini digunakan untuk menangani kejadian saat OptionMenu diklik

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                startActivity(new Intent(DashboardActivity.this,
                        AddActivity.class));
                break;
            case R.id.menu_delete:
                deleteRestaurant();
                break;
            case R.id.menu_account:
                startActivity(new Intent(DashboardActivity.this,
                        ProfileActivity.class));
                break;
            case R.id.menu_logout:
                session.logoutUser();
                break;
        }
        return true;
    }

    public void deleteRestaurant(){

        DialogUtils.openDialog(this);

        AndroidNetworking.post(Constans.DELETE_RESTORAN+"/"+session.getUserId())
                .build()
                .getAsObject(ListRestaurantResponse.class, new
                        ParsedRequestListener() {
                            @Override
                            public void onResponse(Object response) {
                                if (response instanceof ListRestaurantResponse) {
                                    ListRestaurantResponse res = (ListRestaurantResponse)
                                            response;
                                    if (res.getStatus().equals("success")) {
                                        Toast.makeText(DashboardActivity.this,
                                                "Berhasil Menghapus Restaurant", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(DashboardActivity.this,
                                                "Gagal Menghapus", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                DialogUtils.closeDialog();
                            }
                            @Override
                            public void onError(ANError anError) {
                                Toast.makeText(DashboardActivity.this,
                                        "Terjadi kesalahan API", Toast.LENGTH_SHORT).show();
                                Toast.makeText(DashboardActivity.this,
                                        "Terjadi kesalahan API : "+anError.getCause().toString(),
                                        Toast.LENGTH_SHORT).show();
                                DialogUtils.closeDialog();
                            }
                        });
    }


    //Method untuk set recyclerview
    public void initRecyclerView(){
        adapter = new RestaurantAdapter(this);
        loadItem(GET_LIST_RESTAURANT);
        rv = findViewById(R.id.rv_restaurant);
        rv.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setNestedScrollingEnabled(false);
        rv.hasFixedSize();
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(new
                                               RestaurantAdapter.OnItemClickListener() {
                                                   @Override
                                                   public void onClick(int position) {
                                                       Toast.makeText(DashboardActivity.this, "Clicked Item - " +
                                                               position, Toast.LENGTH_SHORT).show();
                                                   }
                                               });
    }
    //Method untuk load data dari api
    public void loadItem(String url){
//show progress dialog
        progressDialog.setMessage("Please Wait..");
        progressDialog.show();
        AndroidNetworking.get(url)
                .build()
                .getAsObject(ListRestaurantResponse.class, new
                        ParsedRequestListener() {
                            @Override
                            public void onResponse(Object response) {
                                if(response instanceof ListRestaurantResponse){
//disable progress dialog
                                    progressDialog.dismiss();
//null data check
                                    if (((ListRestaurantResponse) response).getData() !=
                                            null && ((ListRestaurantResponse) response).getData().size() > 0){
                                        adapter.swap(((ListRestaurantResponse)
                                                response).getData());
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                            @Override
                            public void onError(ANError anError) {
                                progressDialog.dismiss();
                                Toast.makeText(DashboardActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                            }
                        });
    }
}
