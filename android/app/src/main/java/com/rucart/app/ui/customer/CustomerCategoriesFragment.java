package com.rucart.app.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rucart.app.R;
import com.rucart.app.api.ApiClient;
import com.rucart.app.model.ApiResponse;
import com.rucart.app.model.Category;
import com.rucart.app.ui.customer.adapter.CategoryAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class CustomerCategoriesFragment extends Fragment {

    private RecyclerView rvAllCategories;
    private CategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_categories, container, false);

        rvAllCategories = view.findViewById(R.id.rvAllCategories);
        rvAllCategories.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new CategoryAdapter(category -> {
            if (getActivity() instanceof CustomerMainActivity) {
                ((CustomerMainActivity) getActivity()).navigateToTab(R.id.nav_customer_home);
            }
        });
        rvAllCategories.setAdapter(adapter);

        loadCategories();

        return view;
    }

    private void loadCategories() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    adapter.setCategories(getFullBlinkitCategories(response.body().getData()));
                } else {
                    adapter.setCategories(getFullBlinkitCategories(null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                adapter.setCategories(getFullBlinkitCategories(null));
            }
        });
    }

    private List<Category> getFullBlinkitCategories(List<Category> backendCategories) {
        List<Category> list = new ArrayList<>();
        if (backendCategories != null && !backendCategories.isEmpty()) {
            list.addAll(backendCategories);
        } else {
            list.addAll(getFallbackCategories());
        }
        addIfMissing(list, 101L, "Bakery & Biscuits", "cat_bakery_biscuits");
        addIfMissing(list, 102L, "Breakfast & Instant", "cat_instant_food");
        addIfMissing(list, 103L, "Tea & Coffee", "cat_tea_coffee");
        addIfMissing(list, 104L, "Cold Drinks & Juices", "cat_cold_drinks");
        addIfMissing(list, 105L, "Sweet Tooth", "cat_sweet_tooth");
        addIfMissing(list, 106L, "Atta, Rice & Dal", "cat_atta_rice_dal");
        addIfMissing(list, 107L, "Masala & Oil", "cat_masala_oil");
        addIfMissing(list, 108L, "Cleaning Essentials", "cat_cleaning");
        addIfMissing(list, 109L, "Personal Care", "cat_personal_care");
        addIfMissing(list, 110L, "Baby Care", "cat_baby_care");
        addIfMissing(list, 111L, "Pet Care", "cat_pet_care");
        return list;
    }

    private void addIfMissing(List<Category> list, Long id, String name, String icon) {
        for (Category c : list) {
            if (c.getName() != null && c.getName().equalsIgnoreCase(name)) return;
        }
        list.add(new Category(id, name, icon));
    }

    private List<Category> getFallbackCategories() {
        List<Category> list = new ArrayList<>();
        list.add(new Category(1L, "Fruits & Veg", "ic_vegetables"));
        list.add(new Category(2L, "Dairy & Eggs", "ic_dairy"));
        list.add(new Category(3L, "Snacks & Drinks", "ic_snacks"));
        list.add(new Category(4L, "Household", "ic_household"));
        return list;
    }
}
